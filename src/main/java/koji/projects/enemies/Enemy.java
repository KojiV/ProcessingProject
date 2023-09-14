package koji.projects.enemies;

import koji.projects.GameObject;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class Enemy extends Moveable {
    protected Area spawnArea;
    protected PImage spriteSheet;
    protected double viewRange;
    @Getter boolean lockedOn = false;
    @Getter private Type type;

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

        isVisible = false;
        animationTimers = new int[2];
        state = State.MOVE_DOWN;
    }

    private boolean isVisible;
    private int[] animationTimers;

    @Override public void areaUpdate() {
        isVisible = main.getArea().matches(spawnArea);
    }

    @Override
    public List<Image> draw() {
        if(isVisible) {
            sprite.draw();

            if(animationTimers[0] > type.getAnimationDelays()[0][animationTimers[1]]) {
                animationTimers[0] = 0;
                animationTimers[1] = animationTimers[1] >= 3 ? 0 : animationTimers[1] + 1;
            } else animationTimers[0]++;

            sprite = animations.get(state).get(animationTimers[1]);
            sprite.setX(x + sprite.getOffsetX());
            sprite.setY(y + sprite.getOffsetY());

            return List.of(sprite);
        }
        return new ArrayList<>();
    }

    public boolean canSeePlayer() {
        double startX = x * 1.0 / getMapScale();
        double startY = y * 1.0 / getMapScale();
        double endX = main.getPlayer().getX() * 1.0 / getMapScale();
        double endY = main.getPlayer().getY() * 1.0 / getMapScale();

        double difX = endX - startX;
        double difY = endY - startY;
        double distance = Math.abs(difX) + Math.abs(difY);

        double dX = difX / distance;
        double dY = difY / distance;
        for (int i = 0; i <= Math.ceil(distance); i++) {
            int x = (int) Math.floor(startX + dX * i);
            int y = (int) Math.floor(startY + dY * i);
            if(main.getArea().getCollisions().get(x * 32 + y) != null) return false;
        }
        return true;
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
                image.image(spriteSheet.get(j * 48, findI * 48 + 96, 48, 48), 0, 0);

                if(state.toString().contains("LEFT")) {
                    image.pushMatrix();
                    image.scale(-1.0f, 1.0f);
                    image.image(spriteSheet.get(j * 48, findI * 48 + 96, 48, 48), -48, 0);
                    image.popMatrix();
                }
                PImage pImage = image.get();
                pImage.resize(64, 64);
                list.add(new Image(pImage, x, y));
            }
            animations.put(state, list);
        }
    }

    @AllArgsConstructor
    enum Type {
        SLIME("slime", new int[][] {
                { 38, 10, 10, 38 }
        });

        @Getter private final String spriteTag;
        @Getter private final int[][] animationDelays;
    }
}
