package koji.projects.enemies;

import koji.projects.area.Area;
import lombok.Getter;

public class Slime extends Enemy {
    @Getter private final int jumpSpeed;
    private int jumpTimer;

    public Slime(int spawnX, int spawnY, Area spawnArea, double viewRange, int jumpSpeed) {
        super(spawnX, spawnY, new Area(spawnArea), Type.SLIME, viewRange);
        this.jumpSpeed = jumpSpeed;
    }
}
