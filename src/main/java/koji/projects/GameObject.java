package koji.projects;

import koji.projects.area.Area;
import koji.projects.area.Arrow;
import koji.projects.data.Objective;
import koji.projects.character.Player;
import koji.projects.data.Textbox;
import lombok.Getter;
import lombok.Setter;
import processing.event.KeyEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameObject {
    @Setter protected static Main main;
    @Getter private static final List<GameObject> gameObjects = new ArrayList<>();

    public GameObject() {
        gameObjects.add(this);
    }

    public void keyPressed(KeyEvent event) {}
    @Getter @Setter protected List<Image> previousDrawImages = new ArrayList<>();

    public List<Image> draw() {
        return new ArrayList<>();
    }
    public void keyReleased(KeyEvent event) {}

    public static Arrow getArrow() {
        return main.getArrow();
    }

    public static Player getPlayer() {
        return main.getPlayer();
    }

    public Area getArea() {
        return main.getArea();
    }
    public Objective getObjective() {
        return getArrow().getObjective();
    }

    public static int getMapScale() {
        return main.getMapScale();
    }

    private HashMap<String, Textbox> cachedBoxes = new HashMap<>();
    private int cachedX = -Integer.MAX_VALUE;
    private int cachedY = -Integer.MAX_VALUE;

    public List<Textbox> getBoxesForArea() {
        if(cachedX != getArea().getX() || cachedY != getArea().getY()) {
            cachedX = getArea().getX();
            cachedY = getArea().getY();
            cachedBoxes = main.getTextboxes();
            new ArrayList<>(main.getTextboxes().values()).stream().filter(
                    t -> t.getArea().getX() != cachedX || t.getArea().getY() != cachedY
            ).forEach(box -> cachedBoxes.remove(
                    main.getTextboxes().keySet().stream().filter(
                            b -> cachedBoxes.get(b).equals(box)
                    ).findFirst().get())
            );
        }
        return cachedBoxes.values().stream().toList();
    }
}
