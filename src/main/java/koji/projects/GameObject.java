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
    public GameObject() {
        Class<?> clazz = getClass();

        do clazz = clazz.getSuperclass();
        while (clazz != GameObject.class && clazz != null);
        if(clazz == null) return;

        for (int i = 0; i < 5; i++) {
            try {
                clazz.getDeclaredMethod(Main.GOMETHODS[i], Main.GOCLASSES[i]);
                Main.getMain().getGameObjects()[i].add(this);
            } catch (NoSuchMethodException ignored) {}
        }
    }

    public void keyPressed(KeyEvent event) {}
    @Getter @Setter protected List<Image> previousDrawImages = new ArrayList<>();

    public List<Image> draw() {
        return new ArrayList<>();
    }
    public void keyReleased(KeyEvent event) {}

    public void playerUpdated() {}

    public void areaUpdate() {}

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
