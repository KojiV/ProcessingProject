package koji.projects;

import koji.projects.area.Area;
import koji.projects.area.Arrow;
import koji.projects.character.NPC;
import koji.projects.character.Player;
import koji.projects.data.BottomBar;
import koji.projects.data.Textbox;
import lombok.Getter;
import lombok.SneakyThrows;
import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PImage;
import processing.event.KeyEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends PApplet {

    //TODO: Make one time text boxes persist through saves
    //TODO: NPC overlapping top layer (not always)

    @Getter private static Main main;
    @Getter private final static String prefix = "src/main/java/koji/projects/";

    public static void main(String[] args) {
        PApplet.main(Main.class, args);
    }

    @Getter private Player player;
    @Getter private Area area;
    @Getter private Arrow arrow;
    @Getter private BottomBar bar;
    @Getter private int mapScale;
    @Getter private PFont textFont, titleFont;
    @Getter private boolean couldLoadData = false;

    @Override public void settings() { size(1024, 696); }

    @Getter private HashMap<String, Textbox> textboxes;
    @Getter private List<NPC> npcs;

    @SneakyThrows @Override public void setup() {
        main = this;
        GameObject.setMain(this);

        area = new Area();
        mapScale = 32;

        couldLoadData = new File(prefix + "data/player.yml").exists();

        textFont = createFont(prefix + "textures/font.ttf", 16);
        titleFont = createFont(prefix + "textures/titlefont.ttf", 16);

        File textBoxesFile = new File(Main.getPrefix() + "data/textboxes.yml");
        FileConfiguration textBoxesConfig = YamlConfiguration.loadConfiguration(textBoxesFile);
        HashMap<String, Textbox> boxes = new HashMap<>();
        for(String key : getKeys(textBoxesConfig, "boxes.", false)) {
            //key = boxes.x
            int x = textBoxesConfig.getInt(key + ".x");
            int y = textBoxesConfig.getInt(key + ".y");
            int areaX = textBoxesConfig.getInt(key + ".areaX");
            int areaY = textBoxesConfig.getInt(key + ".areaY");
            Textbox box = new Textbox(
                    x * getMapScale(),
                    y * getMapScale(),
                    new Area(areaX, areaY),
                    textBoxesConfig.getStringList(key + ".text"),
                    textBoxesConfig.contains(key + ".objActivate") ?
                            textBoxesConfig.getInt(key + ".objActivate") :
                            -1
            );
            boxes.put(textBoxesConfig.getString(key + ".id"), box);
        }
        this.textboxes = boxes;

        File npcFile = new File(Main.getPrefix() + "character/npcs.yml");
        FileConfiguration npcFileConfig = YamlConfiguration.loadConfiguration(npcFile);
        ArrayList<NPC> npcs = new ArrayList<>();
        for (String key : getKeys(npcFileConfig, "npcs.", false)) {
            int x = npcFileConfig.getInt(key + ".x");
            int y = npcFileConfig.getInt(key + ".y");
            int areaX = npcFileConfig.getInt(key + ".areaX");
            int areaY = npcFileConfig.getInt(key + ".areaY");

            NPC npc = new NPC(
                    npcFileConfig.getInt(key + ".sprite"),
                    x, y, areaX, areaY,
                    this.textboxes.get(npcFileConfig.getString(key + ".textboxes.initial")),
                    this.textboxes.get(npcFileConfig.getString(key + ".textboxes.normal")),
                    npcFileConfig.contains(key + ".objActivate") ? npcFileConfig.getInt(key + ".objActivate") : -1,
                    npcFileConfig.getBoolean(key + ".overCounter"),
                    npcFileConfig, key
            );
            npcs.add(npc);
        }
        this.npcs = npcs;

        player = new Player();
        bar = new BottomBar();
        arrow = new Arrow(area, null, 14, 4);

        String[] methodNames = new String[] {"draw", "keyPressed", "keyReleased" };
        Class<?>[][] classes = new Class<?>[][] {
                new Class[0],
                new Class[] { KeyEvent.class },
                new Class[] { KeyEvent.class }
        };
        for(int i = 0; i < 3; i++) {
            int finalI = i;
            gameObjects[i] = GameObject.getGameObjects().stream().filter(r -> {
                try {
                    r.getClass().getDeclaredMethod(methodNames[finalI], classes[finalI]);
                    return true;
                } catch (NoSuchMethodException e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }
    }

    @SuppressWarnings("unchecked")
    List<GameObject>[] gameObjects = new List[3];

    private boolean skipDrawingPlayer = false;
    @Override public void draw() {
        int x1 = 1024, y1 = 696, x2 = 0, y2 = 0;
        boolean changed = false;

        for(GameObject r : gameObjects[0]) {
            for(Image img : r.getPreviousDrawImages()) {
                int x = img.getX();
                int y = img.getY();

                x1 = Math.min(x1, x);
                y1 = Math.min(y1, y);
                x2 = Math.max(x2, x + img.getWidth());
                y2 = Math.max(y2, y + img.getHeight());
                changed = true;
            }
        }
        boolean isNotEmpty = gameObjects[0].stream().anyMatch(go -> !go.getPreviousDrawImages().isEmpty());
        if(isNotEmpty && changed)
            image(area.getCurrentBackBackground().get(
                    x1, y1, x2 - x1, y2 - y1
            ), x1, y1);

        for(int i = 0; i < gameObjects[0].size(); i++) {
            GameObject r = gameObjects[0].get(i);

            if(r.getClass().equals(Arrow.class) && area.getCurrentFrontBackground() != null) {
                image(area.getCurrentFrontBackground().get(
                        x1, y1, x2 - x1, y2 - y1
                ), x1, y1);

                /*if(!gameObjects[0].get(i - 1).getPreviousDrawImages().isEmpty()) {
                    //Issue: once player is invisible once, they stay invisible
                    Image previous = gameObjects[0].get(i - 1).getPreviousDrawImages().get(0);
                    PImage img = main.get(previous.getX(), previous.getY(),
                            previous.getWidth(), previous.getHeight()
                    );

                    double amount = 0;
                    for (int loc = 0; loc < previous.getImage().pixels.length; loc++) {
                        if (main.alpha(previous.getImage().pixels[loc]) == 0) continue;
                        if (img.pixels[loc] == previous.getImage().pixels[loc]) amount++;
                    }
                    if (amount <= 104) skipDrawingPlayer = true;
                }*/
            }
            r.setPreviousDrawImages(r.draw());
        }
        if(intersects(x1, y1, x2 - x1, y2 - y1, 0, 574, 1028, 124)) {
            bar.drawBar();
        }
    }

    @Override public void keyPressed(KeyEvent event) {
        gameObjects[1].forEach(r -> r.keyPressed(event));
    }

    @Override public void keyReleased(KeyEvent event) {
        gameObjects[2].forEach(r -> r.keyReleased(event));
    }

    public static List<String> getKeys(FileConfiguration fc, String key, boolean deep) {
        List<String> list = new ArrayList<>();
        for (String keys : fc.getKeys(true)) {
            if (keys.startsWith(key)) {
                if (!deep) {
                    boolean skip = false;
                    for (String alreadyExisting : list) {
                        if (keys.startsWith(alreadyExisting + ".")) {
                            skip = true;
                            break;
                        }
                    }
                    if (skip) continue;
                }
                list.add(keys);
            }
        }
        return list;
    }

    public static boolean intersects(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
        int[] rec1 = { x1, y1, x1 + w1, y1 + h1 };
        int[] rec2 = { x2, y2, x2 + w2, y2 + h2 };

        boolean noOverlap = rec1[0] > rec2[2] ||
                rec2[0] > rec1[2] ||
                rec1[1] > rec2[3] ||
                rec2[1] > rec1[3];

        return !noOverlap;
    }

    public static double distance(int x1, int y1, int x2, int y2) {
        return Math.hypot(
                Math.abs(y1 - y2),
                Math.abs(x1 - x2)
        ) / main.getMapScale();
    }
}