package koji.projects.area;

import koji.projects.GameObject;
import koji.projects.Image;
import koji.projects.Main;
import koji.projects.data.Objective;
import lombok.Getter;
import lombok.Setter;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;

public class Arrow extends GameObject {
    private final List<PImage> images = new ArrayList<>();

    @Getter @Setter private Area area;
    @Setter @Getter private Objective objective;
    private int x, y;
    @Getter private Direction direction;
    @Getter @Setter private PImage image;

    public void setX(int x, int offset) {
        this.x = x * main.getMapScale() + offset;
    }
    public void setY(int y) {
        this.y = y * main.getMapScale();
    }

    public Arrow(Area area, Objective objective, int x, int y) {
        this.area = area;
        this.objective = objective;
        this.x = x * main.getMapScale() + 5;
        this.y = y * main.getMapScale();
    }

    int[] movementFlags = new int[2];
    // First boolean is whether it's vertical or not
    // Second boolean is whether it's currently going up
    boolean[] movementBoos = new boolean[2];

    @Override public List<Image> draw() {
        if(objective != null) {
            if(movementFlags[0] == 3) {
                movementFlags[0] = 0;

                if(movementFlags[1] == 7) {
                    movementFlags[1] = 0;
                    movementBoos[1] = !movementBoos[1];
                } else {
                    int direction = movementBoos[1] ? 1 : -1;

                    if(movementBoos[0]) y += direction;
                    else x += direction;

                    movementFlags[1]++;
                }

            } else movementFlags[0]++;

            main.image(image, x, y);
            return List.of(new Image(image, x, y));
        }
        return new ArrayList<>();
    }

    public void onAreaUpdate() {
        if(area.getY() < objective.getArea().getY()) {
            direction = Direction.UP;
            setX(15, 10);
            setY(1);
            movementBoos[0] = true;
        } else if (area.getX() < objective.getArea().getX()) {
            direction = Direction.RIGHT;
            setX(30, 0);
            setY(9);
            movementBoos[0] = false;
        } else if (area.getX() > objective.getArea().getX()) {
            direction = Direction.LEFT;
            setX(1, 0);
            setY(9);
            movementBoos[0] = false;
        } else if (area.getY() > objective.getArea().getY()) {
            direction = Direction.DOWN;
            setX(15, 10);
            setY(16);
            movementBoos[0] = true;
        } else {
            direction = Direction.SAME;
            setX(objective.getX(), 5);
            setY(objective.getY());
            movementBoos[0] = true;
        }
        image = updateImage(direction);
    }

    public PImage updateImage(Direction dir) {
        if(images.isEmpty()) {
            images.add(main.loadImage(Main.getPrefix() + "textures/upgo.png"));
            images.add(main.loadImage(Main.getPrefix() + "textures/downgo.png"));
            images.add(main.loadImage(Main.getPrefix() + "textures/rightgo.png"));
            images.add(main.loadImage(Main.getPrefix() + "textures/leftgo.png"));
            images.add(main.loadImage(Main.getPrefix() + "textures/arrow.png"));
        }
        return switch (dir) {
            case UP -> images.get(0);
            case DOWN -> images.get(1);
            case RIGHT -> images.get(2);
            case LEFT -> images.get(3);
            case SAME -> images.get(4);
        };
    }

    public enum Direction {
        UP,
        DOWN,
        RIGHT,
        LEFT,
        SAME;

        public static Direction[] getCoreDirections() {
            return new Direction[]{ UP, DOWN, RIGHT, LEFT };
        }
    }
}
