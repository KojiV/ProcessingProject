package koji.projects.area;

import koji.projects.GameObject;
import koji.projects.Image;
import koji.projects.Main;
import koji.projects.Utils;
import koji.projects.character.CollisionObject;
import koji.projects.data.Objective;
import koji.projects.enemies.Enemy;
import koji.projects.enemies.Slime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.event.KeyEvent;

import java.io.*;
import java.util.*;
import java.util.function.BooleanSupplier;

public class Area extends GameObject {
    @Getter
    private PImage currentBackBackground;
    @Getter private PImage currentFrontBackground = null;
    private boolean refreshDraw;

    public Area() {
        x = -Integer.MAX_VALUE;
        y = -Integer.MAX_VALUE;

        for(int i = 0; i < titleScreenImages.length; i++) {
            PImage image = main.loadImage(
                    Main.getPrefix() + "textures/titlescreen/background/frame_" + i + "_delay-0.05s.png"
            );
            image.resize((int) (Main.GAME_SCALE * 1024), (int) (Main.GAME_SCALE * 576));
            titleScreenImages[i] = image;
        }
        currentBackBackground = titleScreenImages[0];
    }

    public Area(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Area(Area area) {
        this(area.getX(), area.getY());
        this.currentBackBackground = area.getCurrentBackBackground();
        this.currentFrontBackground = area.getCurrentFrontBackground();
        this.collisions = area.getCollisions();
        this.enemies = area.getEnemies();
    }

    @Getter private int x, y;
    @Getter private CollisionObject[][] collisions = new CollisionObject[32][18];

    @Getter private List<Enemy> enemies = new ArrayList<>();

    public boolean changeArea(int x, int y) {
        if(!new File(Main.getPrefix() + "textures/areas/area" + x + "_" + y + "_bottom.png").exists())
            return false;
        this.x = x;
        this.y = y;

        enemies.forEach(Enemy::destroy);
        enemies = new ArrayList<>();
        File enemyFile = new File(Main.getPrefix() + "enemies.yml");
        if(enemyFile.exists()) {
            try {
                FileConfiguration enemyFC = YamlConfiguration.loadConfiguration(enemyFile);
                if (enemyFC.contains("area" + x + "_" + y)) {
                    for(String key : Utils.getKeys(enemyFC, "area" + x + "_" + y + ".", false)) {
                        String[] split = enemyFC.getString(key + ".type").split("\\.");
                        String type = split[split.length - 1];
                        if(type.equals("SLIME")) {
                            enemies.add(new Slime(
                                    enemyFC.getInt(key + ".x"),
                                    enemyFC.getInt(key + ".y"),
                                    this,
                                    enemyFC.getDouble(key + ".view-range"),
                                    60
                            ));
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                boolean boo = enemyFile.createNewFile();
                if(!boo) Main.println("Failed to create file...");
            } catch (IOException ex) {
                Main.println("Failed to create file...");
            }
        }

        titleVars[0] = 1;

        getArrow().onAreaUpdate();

        main.translate(0, 0);
        main.rotate(0);
        currentBackBackground = main.loadImage(
                Main.getPrefix() + "textures/areas/area" + x + "_" + y + "_bottom.png"
        );

        currentFrontBackground = main.loadImage(
                Main.getPrefix() + "textures/areas/area" + x + "_" + y + "_top.png"
        );

        topLayerAreas = new HashMap<>();
        for(int x1 = 0; x1 < 32; x1++) {
            for(int y1 = 0; y1 < 18; y1++) {
                int scaledX = x1 * Main.MAP_SCALE;
                int scaledY = y1 * Main.MAP_SCALE;

                int corner1 = Main.MAP_SCALE - 1;
                int[] corners = {
                        scaledX + scaledY * currentFrontBackground.width,
                        scaledX + corner1 + (scaledY + corner1) * currentFrontBackground.width,
                        scaledX + corner1 + scaledY * currentFrontBackground.width,
                        scaledX + (scaledY + corner1) * currentFrontBackground.width
                };

                if(Arrays.stream(corners).anyMatch(c ->
                        main.alpha(currentFrontBackground.pixels[c]) != 0
                )) {
                    List<Integer> list = new ArrayList<>();
                    for(int offsetX = 0; offsetX < Main.MAP_SCALE; offsetX++) {
                        for(int offsetY = 0; offsetY < Main.MAP_SCALE; offsetY++) {
                            int loc = scaledX + offsetX + (scaledY + offsetY) * currentFrontBackground.width;
                            if(main.alpha(currentFrontBackground.pixels[loc]) != 0) {
                                list.add(loc);
                            }
                        }
                    }
                    topLayerAreas.put(new int[] { x1 * Main.MAP_SCALE, y1 * Main.MAP_SCALE }, list);
                }
            }
        }

        collisions = loadCollisions();

        Main.resize(currentBackBackground);
        Main.resize(currentFrontBackground);

        main.translate(0, 0);
        main.rotate(0);
        main.image(currentBackBackground, 0, 0);
        main.image(currentFrontBackground, 0, 0);
        main.translate(0, 0);
        main.rotate(0);

        main.getBar().drawBar();

        main.areaUpdate();

        return true;
    }

    public boolean changeArea(Arrow.Direction direction) {
        return switch (direction) {
            case UP -> changeArea(x, y - 1);
            case DOWN -> changeArea(x, y + 1);
            case LEFT -> changeArea(x - 1, y);
            case RIGHT -> changeArea(x + 1, y);
            default -> false;
        };
    }

    private final PImage[] titleScreenImages = new PImage[28];

    //Index 0 is the timer for moving the gif,
    //Index 1 is what gif it's on
    //Index 2 is the holding for the title rotation
    //Index 3 is the degrees the title is rotated
    private final int[] titleVars = new int[4];
    private boolean titleMovingRight;
    private final int[] titleScreenCords = new int[] { 68, 14 };
    private Image titleScreenArrow = null;
    private TitleScreenOptions option = TitleScreenOptions.NEW;

    @Getter private HashMap<int[], List<Integer>> topLayerAreas = new HashMap<>();

    public List<Image> draw() {
        if (isOnTitleScreen()) {
            if (titleVars[0] == 5) {
                main.translate(0, 0);
                main.rotate(0);
                currentBackBackground = titleScreenImages[titleVars[1]];
                main.image(currentBackBackground, 0, 120);
                main.stroke(255, 0, 124);
                main.fill(255, 0, 124);
                main.rect(0, 0, 1024, 120);


                main.translate(Main.GAME_SCALE * 512, Main.GAME_SCALE * 200);
                main.rotate(Main.radians(titleVars[3]));
                main.textFont(main.getTitleFont());
                main.textSize(Main.GAME_SCALE * 128);
                main.fill(0);
                main.textAlign(main.CENTER);
                main.text("Test", 0, 0);
                main.fill(255);
                main.text("Test", -6, -6);


                main.textSize(Main.GAME_SCALE * 32);
                for(TitleScreenOptions options : TitleScreenOptions.values()) {
                    options.displayText();
                }

                if(titleScreenArrow != null) {
                    titleScreenArrow.setX(titleScreenCords[0]);
                    titleScreenArrow.setY(titleScreenCords[1]);
                    titleScreenArrow.draw();
                } else titleScreenArrow = getArrow().updateImage(Arrow.Direction.LEFT);

                titleVars[0] = 0;

                if (titleVars[1] == 27) titleVars[1] = 0;
                else titleVars[1]++;

                if (titleVars[3] > 7 || titleVars[3] < -2) {
                    if (titleVars[2] == 10) {
                        titleMovingRight = !titleMovingRight;
                        titleVars[2] = 0;
                    } else titleVars[2]++;
                }

                if (titleMovingRight) titleVars[3]++;
                else titleVars[3]--;
            } else titleVars[0]++;
        } else {
            while (titleVars[0] != 0) {
                if(titleVars[0] != 5) {
                    titleVars[0]++;
                    main.image(currentBackBackground, 0, 0);
                    main.image(currentFrontBackground, 0, 0);
                    main.getBar().drawBar();
                }
                else titleVars[0] = 0;
            }
        }
        if(refreshDraw) {
            refreshDraw = false;
            return new ArrayList<>(List.of(new Image(
                    null, 0, 0, 0, 0,
                    32 * Main.MAP_SCALE, 18 * Main.MAP_SCALE)
            ));
        }
        return new ArrayList<>();
    }

    public void keyPressed(KeyEvent event) {
        if(isOnTitleScreen()) {
            int key = event.getKeyCode();
            Set<Integer> previous = new HashSet<>(Arrays.asList(
                    main.getUpKey(), main.getLeftKey(), Main.UP, Main.LEFT
            ));
            Set<Integer> next = new HashSet<>(Arrays.asList(
                    main.getDownKey(), main.getRightKey(), Main.DOWN, Main.RIGHT
            ));

            if(previous.contains(key) || next.contains(key)) {
                option = option.get(previous.contains(key));
                titleScreenCords[1] = option.getArrowCords();
            } else if (key == Main.ENTER && option != TitleScreenOptions.SETTINGS) {
                main.translate(0, 0);
                main.rotate(0);
                if(option == TitleScreenOptions.NEW || !getPlayer().readStats()) {
                    if(new File(Main.getPrefix() + "data/player/player.yml").exists()) {
                        new File(Main.getPrefix() + "data/player/player.yml").delete();
                    }
                    getArrow().setObjective(new Objective());
                    changeArea(0, 0);
                }
            }
        }
        if(event.getKey() == 't') System.out.println(main.frameRate);
        if(event.getKey() == 'g') main.setGridEnabled(!main.isGridEnabled());
        if(event.getKey() == 'j') refreshDraw = true;
    }

    @SneakyThrows public CollisionObject[][] loadCollisions() {
        File f = new File(Main.getPrefix() + "collisions/area" + x + "_" + y + ".txt");
        if(!f.exists()) {
            Main.println("LOADING AREA COLLISIONS, WAIT");

            File directory = new File(Main.getPrefix() + "collisions");
            boolean boo = true;
            if(!directory.exists()) boo = directory.mkdirs();

            boolean success = boo && f.createNewFile();
            if(!success) return new CollisionObject[32][18];

            if(f.canWrite()) {
                try (PrintWriter pw = new PrintWriter(f)) {

                    PImage image = main.loadImage(
                            Main.getPrefix() + "textures/areas/area" + x + "_" + y + "_collisions.png"
                    ).get();
                    PImage btm = main.loadImage(
                            Main.getPrefix() + "textures/areas/area" + x + "_" + y + "_bottom.png"
                    );
                    PImage top = main.loadImage(
                            Main.getPrefix() + "textures/areas/area" + x + "_" + y + "_top.png"
                    );
                    PImage place = main.loadImage(
                            Main.getPrefix() + "textures/collisions/placeholder.png"
                    );

                    for (int x1 = 0; x1 < 32; x1++) {
                        for (int y1 = 0; y1 < 18; y1++) {
                            int getX = Main.MAP_SCALE * x1;
                            int getY = Main.MAP_SCALE * y1;
                            int wh = Main.MAP_SCALE;

                            PImage area = image.get(getX, getY, wh, wh);

                            PGraphics graphic = main.createGraphics(wh, wh);
                            graphic.beginDraw();
                            graphic.image(btm.get(getX, getY, wh, wh), 0, 0);
                            graphic.image(top.get(getX, getY, wh, wh), 0, 0);
                            graphic.image(place, 0, 0);
                            graphic.endDraw();

                            if (Arrays.equals(graphic.pixels, area.pixels)) {
                                pw.write(x1 + " " + y1 + "\n");
                            }
                        }
                    }
                }
            }
        }

        return loadCollisions(x, y);
    }

    @SneakyThrows public CollisionObject[][] loadCollisions(int areaX, int areaY) {
        CollisionObject[][] collisions = new CollisionObject[32][18];
        File f = new File(Main.getPrefix() + "collisions/area" + areaX + "_" + areaY + ".txt");
        if(f.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = Main.split(line, " ");
                int x = Integer.parseInt(split[0]);
                int y = Integer.parseInt(split[1]);
                CollisionObject co = new CollisionObject(
                        x * Main.MAP_SCALE,
                        y * Main.MAP_SCALE,
                        Main.MAP_SCALE, Main.MAP_SCALE
                );
                collisions[x][y] = co;
            }
        }
        return collisions;
    }

    public boolean matches(Area area) {
        return x == area.getX() && y == area.getY();
    }

    public boolean matches(int x, int y) {
        return this.x == x && this.y == y;
    }

    public boolean isOnTitleScreen() {
        return x == -Integer.MAX_VALUE && y == -Integer.MAX_VALUE;
    }

    @AllArgsConstructor
    enum TitleScreenOptions {
        NEW("New Game"),
        CONTINUE("Continue", () -> main.isCouldLoadData()),
        SETTINGS("Settings");

        TitleScreenOptions(String label) {
            this.label = label;
            fillCondition = () -> true;
        }

        private final String label;
        private final BooleanSupplier fillCondition;

        private static final int ARROW_CORD_BASE = 14, TEXT_CORD_BASE = 50;

        public int getArrowCords() {
            return ARROW_CORD_BASE + 35 * ordinal();
        }

        public int getTextCords() {
            return TEXT_CORD_BASE + 35 * ordinal();
        }

        public void displayText() {
            main.fill(0);
            main.text(label, 0, getTextCords()); //14 =
            if(fillCondition.getAsBoolean()) {
                main.fill(255);
                main.text(label, -4, getTextCords() - 4);
            }
        }

        public TitleScreenOptions get(boolean previous) {
            TitleScreenOptions option = next(previous);
            if(!option.fillCondition.getAsBoolean()) option = option.next(previous);
            return option;
        }

        private TitleScreenOptions next(boolean previous) {
            TitleScreenOptions[] values = TitleScreenOptions.values();
            int move = ordinal() + (previous ? -1 : 1);
            if(move < 0) move = values.length - 1;
            else if(move >= values.length) move = 0;
            return values[move];
        }
    }
}
