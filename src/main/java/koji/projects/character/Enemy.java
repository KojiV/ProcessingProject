package koji.projects.character;

import koji.projects.GameObject;
import koji.projects.Image;
import koji.projects.Main;
import koji.projects.area.Area;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

public class Enemy extends GameObject {
    protected int spawnX;
    protected int spawnY;
    protected Area spawnArea;
    protected Image sprite;
    protected double viewRange;


    public Enemy(int spawnX, int spawnY, Area spawnArea, Type type, double viewRange) {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnArea = spawnArea;
        this.viewRange = viewRange;

        this.sprite = new Image(
                main.loadImage(
                        Main.getPrefix() + "textures/enemies/" + type.getSpriteTag() + ".png"
                ), spawnX, spawnY
        );
    }

    @Override public List<Image> draw() {
        return super.draw();
    }

    /*public boolean canSeePlayer() {
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
    }*/

    @AllArgsConstructor
    enum Type {
        SLIME("slime");

        @Getter private final String spriteTag;
    }
}
