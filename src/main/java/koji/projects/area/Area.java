package koji.projects.area;

import koji.projects.GameObject;
import koji.projects.Image;
import koji.projects.Main;
import koji.projects.character.CollisionObject;
import koji.projects.data.Objective;
import lombok.Getter;
import lombok.SneakyThrows;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.event.KeyEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Area extends GameObject {
    @Getter
    private PImage currentBackBackground;
    @Getter private PImage currentFrontBackground = null;

    public Area() {
        super();
        x = -Integer.MAX_VALUE;
        y = -Integer.MAX_VALUE;

        for(int i = 0; i < titleScreenImages.length; i++) {
            PImage image = main.loadImage(
                    Main.getPrefix() + "textures/titlescreen/background/frame_" + i + "_delay-0.05s.png"
            );
            image.resize(1024, 576);
            titleScreenImages[i] = image;
        }
        currentBackBackground = titleScreenImages[0];
    }

    public Area(int x, int y) {
        super();
        this.x = x;
        this.y = y;
    }

    @Getter private int x, y;
    @Getter private List<CollisionObject> collisions = new ArrayList<>();

    public void changeArea(int x, int y) {
        this.x = x;
        this.y = y;

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
                int scaledX = x1 * getMapScale();
                int scaledY = y1 * getMapScale();

                int corner1 = getMapScale() - 1;
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
                    for(int offsetX = 0; offsetX < getMapScale(); offsetX++) {
                        for(int offsetY = 0; offsetY < getMapScale(); offsetY++) {
                            int loc = scaledX + offsetX + (scaledY + offsetY) * currentFrontBackground.width;
                            if(main.alpha(currentFrontBackground.pixels[loc]) != 0) {
                                list.add(loc);
                            }
                        }
                    }
                    topLayerAreas.put(
                            new int[] { x1 * getMapScale(), y1 * getMapScale() },
                            list
                    );
                }
            }
        }

        collisions = loadCollisions();

        main.translate(0, 0);
        main.rotate(0);
        main.image(currentBackBackground, 0, 0);
        main.image(currentFrontBackground, 0, 0);
        main.translate(0, 0);
        main.rotate(0);

        main.getBar().drawBar();

        try {
            getPlayer().updateStats();
        } catch (IOException e) {
            Main.println("oooo fuck...");
        }
    }

    public void changeArea(Arrow.Direction direction) {
        switch (direction) {
            case UP -> changeArea(x, y - 1);
            case DOWN -> changeArea(x, y + 1);
            case LEFT -> changeArea(x - 1, y);
            case RIGHT -> changeArea(x + 1, y);
        }
    }

    private final PImage[] titleScreenImages = new PImage[28];

    //Index 0 is the timer for moving the gif,
    //Index 1 is what gif it's on
    //Index 2 is the holding for the title rotation
    //Index 3 is the degrees the title is rotated
    private final int[] titleVars = new int[4];
    private boolean titleMovingRight;
    private final int[] titleScreenCords = new int[] { 68, 14 };
    private boolean newGameSelected = true;

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
                main.translate(512, 200);
                main.rotate(Main.radians(titleVars[3]));
                main.textFont(main.getTitleFont());
                main.textSize(128);
                main.fill(0);
                main.textAlign(main.CENTER);
                main.text("Test", 0, 0);
                main.fill(255);
                main.text("Test", -6, -6);
                main.circle(0, 0, 5);
                main.textSize(32);
                main.fill(0);
                main.text("New Game", 0, 50); //14 =
                main.fill(255);
                main.text("New Game", -4, 46);
                main.fill(0);
                main.text("Continue", 0, 85); //
                if (main.isCouldLoadData()) {
                    main.fill(255);
                    main.text("Continue", -4, 81);
                }
                main.image(getArrow().updateImage(Arrow.Direction.LEFT), titleScreenCords[0], titleScreenCords[1]);

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
            return new ArrayList<>();
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
        return new ArrayList<>();
    }

    public void keyPressed(KeyEvent event) {
        if(isOnTitleScreen()) {
            switch (event.getKeyCode()) {
                case 87: case 83: case 65: case 68: case Main.UP: case Main.DOWN: case Main.LEFT: case Main.RIGHT:
                    if(main.isCouldLoadData()) {
                        newGameSelected = !newGameSelected;
                        titleScreenCords[1] = newGameSelected ? 14 : 49;
                    }
                    break;
                case Main.ENTER:
                    main.translate(0, 0);
                    main.rotate(0);
                    if(newGameSelected || !getPlayer().readStats()) {
                        getArrow().setObjective(new Objective());
                        changeArea(0, 0);
                    }
                    break;
            }
        }
        if(event.getKey() == 't') System.out.println(main.frameRate);
    }

    @SneakyThrows public List<CollisionObject> loadCollisions() {
        File f = new File(Main.getPrefix() + "collisions/area" + x + "_" + y + ".txt");
        if(!f.exists()) {
            Main.println("LOADING AREA COLLISIONS, WAIT");

            File directory = new File(Main.getPrefix() + "collisions");
            boolean boo = true;
            if(!directory.exists()) boo = directory.mkdirs();

            boolean success = boo && f.createNewFile();
            if(!success) return new ArrayList<>();

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
                            int getX = main.getMapScale() * x1;
                            int getY = main.getMapScale() * y1;
                            int wh = main.getMapScale();

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

    @SneakyThrows public List<CollisionObject> loadCollisions(int x, int y) {
        ArrayList<CollisionObject> collisions = new ArrayList<>();
        File f = new File(Main.getPrefix() + "collisions/area" + x + "_" + y + ".txt");
        if(f.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = Main.split(line, " ");
                CollisionObject co = new CollisionObject(
                        Integer.parseInt(split[0]) * main.getMapScale(),
                        Integer.parseInt(split[1]) * main.getMapScale(),
                        main.getMapScale(), main.getMapScale()
                );
                collisions.add(co);
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
}
