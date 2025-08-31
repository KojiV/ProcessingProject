package koji.projects.character;

import koji.projects.GameObject;
import koji.projects.Main;
import lombok.Getter;

public class CollisionObject extends GameObject {
    @Getter private final int x, y, width, height;

    public CollisionObject(int x, int y, int width, int height, boolean textBox) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.textBox = textBox;
    }

    public CollisionObject(int x, int y, int width, int height) {
        this(x, y, width, height, false);
    }

    @Getter private final boolean textBox;

    //!(x1 > x2 + width2 || x1 + width1 < x2 || y1 > y2 + height2 || y1 + height1 < y2);
    public boolean intersects(Player player, float x2, float y2) {
       float[] rec1 = { x2, y2, x2 + player.getWidth(), y2 + player.getHeight() };
       int[] rec2 = { x, y, x + width, y + height };

       boolean noOverlap = rec1[0] > rec2[2] ||
               rec2[0] > rec1[2] ||
               rec1[1] > rec2[3] ||
               rec2[1] > rec1[3];

       return !noOverlap;
    }

    public double distance(int x, int y) {
        return Math.hypot(
                Math.abs(y - this.y),
                Math.abs(x - this.x)
        ) / Main.MAP_SCALE;
    }
}
