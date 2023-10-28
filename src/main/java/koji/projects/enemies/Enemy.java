package koji.projects.enemies;

import koji.projects.Image;
import koji.projects.Main;
import koji.projects.area.Area;
import koji.projects.character.Moveable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import processing.core.PGraphics;
import processing.core.PImage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class Enemy extends Moveable {
    protected Area spawnArea;
    protected PImage spriteSheet;
    protected double viewRange;
    @Getter boolean lockedOn = false;
    @Getter private final Type type;

    public Enemy(int spawnX, int spawnY, Area spawnArea, Type type, double viewRange) {
        this.x = spawnX * getMapScale();
        this.y = spawnY * getMapScale();
        this.spawnArea = spawnArea;
        this.type = type;
        this.viewRange = viewRange;

        if(!new File(Main.getPrefix() + "textures/enemies/" + type.getSpriteTag() + ".png").exists())
            throw new RuntimeException("Enemy Spritesheet must exist!");

        this.spriteSheet = main.loadImage(
                Main.getPrefix() + "textures/enemies/" + type.getSpriteTag() + ".png"
        );

        loadSprites();

        this.sprite = animations.get(State.MOVE_DOWN).get(0);

        speed = 2;
        health = 5;
        defense = 0;
        damage = 2;
        attackSpeed = 3;

        visible = false;
        animationTimers = new int[2];
        state = State.MOVE_DOWN;
    }

    @Getter private boolean visible;
    protected final int[] animationTimers;
    protected double slope;

    @Override public void areaUpdate() {
        visible = main.getArea().matches(spawnArea);
    }

    @Override
    public List<Image> draw() {
        if(visible) {
            sprite.draw();

            if(animationTimers[0] > type.getAnimationDelays()[0][animationTimers[1]]) {
                animationTimers[0] = 0;
                animationTimers[1] = animationTimers[1] >= 3 ? 0 : animationTimers[1] + 1;
            } else animationTimers[0]++;

            sprite = animations.get(state).get(animationTimers[1]);
            sprite.setX(x);
            sprite.setY(y);

            return List.of(sprite);
        }
        return new ArrayList<>();
    }

    public boolean canSeePlayer() {
        double x1 = getEffectiveX() / getMapScale(),
                y1 = getEffectiveY() / getMapScale(),
                x2 = main.getPlayer().getEffectiveX() / getMapScale(),
                y2 = main.getPlayer().getEffectiveY() / getMapScale();

        for(int x = 0; x < 32; x++) {
            for(int y = 0; y < 18; y++) {
                main.getHighlight()[x][y] = false;
            }
        }

        slope = (y2 - y1) / (x2 - x1);
        slope = slope == Double.NEGATIVE_INFINITY ? -90 : slope == Double.POSITIVE_INFINITY ? 90 : slope;

        // Setting line variable
        int x = (int) Math.round(x1), y = (int) Math.round(y1);
        // Getting distances
        double deltaX = Math.abs(x2 - x1), deltaY = Math.abs(y2 - y1);
        // Sign function returns -1 if below 0, 0 if 0, and 1 is above 0
        int signX = sign(x2 - x1), signY = sign(y2 - y1);
        int interchange = 0;
        if(deltaY > deltaX) interchange = 1;
        // Offset variables
        double a = 2 * deltaY, e = a - deltaX, b = a - 2 * deltaX;
        for(int i = 0; i < deltaX; i++) {
            if(i != 0) {
                if (e < 0) {
                    if (interchange == 1) y = y + signY;
                    else x = x + signX;
                    e = e + a;
                } else {
                    y = y + signY;
                    x = x + signX;
                    e = e + b;
                }
            }
            //Main.println(main.getArea().getCollisions()[x][y]);
            if(y >= 0 && y < 32 && x >= 0 && x < 18 &&
                    main.getArea().getCollisions()[x][y] != null
            ) return false;
            main.getHighlight()[x][y] = true;
        }
        return true;
    }

    private int sign(double i) {
        return i == 0 ? 0 : i < 0 ? -1 : 1;
    }

    protected final HashMap<State, List<Image>> animations = new HashMap<>();

    @Override public void loadSprites() {
        for(int i = 0; i < State.values().length; i++) {
            State state = State.values()[i];

            int findI = i;
            if(state.toString().contains("LEFT")) findI--;
            List<Image> list = new ArrayList<>();

            for(int j = 0; j < 4; j++) {
                PGraphics image = main.createGraphics(48, 48);
                image.beginDraw();

                if(state.toString().contains("LEFT")) {
                    image.pushMatrix();
                    image.scale(-1.0f, 1.0f);
                    image.image(spriteSheet.get(j * 48, findI * 48 + 96, 48, 48), -48, 0);
                    image.popMatrix();
                } else image.image(spriteSheet.get(j * 48, findI * 48 + 96, 48, 48), 0, 0);
                PImage pImage = image.get();
                pImage.resize(64, 64);
                list.add(new Image(pImage, x, y));
            }
            animations.put(state, list);
        }
    }

    public void destroy() {
        if(thisClass == null) return;
        for (int i = 0; i < 5; i++) {
            try {
                thisClass.getDeclaredMethod(Main.GO_METHODS[i], Main.GO_CLASSES[i]);
                Main.getMain().getGameObjects()[i].remove(this);
            } catch (NoSuchMethodException ignored) {}
        }
    }

    @AllArgsConstructor
    enum Type {
        SLIME("slime", new int[][] {
                { 8, 40, 8, 8 }
        });

        @Getter private final String spriteTag;
        @Getter private final int[][] animationDelays;
    }
}
