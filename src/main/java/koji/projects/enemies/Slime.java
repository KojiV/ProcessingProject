package koji.projects.enemies;

import koji.projects.Image;
import koji.projects.area.Area;
import lombok.Getter;

import java.util.List;

public class Slime extends Enemy {
    @Getter private final int jumpDistance;
    private double jumpDistanceX, jumpDistanceY;
    private boolean jumping;

    public Slime(int spawnX, int spawnY, Area spawnArea, double viewRange, int jumpDistance) {
        super(spawnX, spawnY, new Area(spawnArea), Type.SLIME, viewRange);
        this.jumpDistance = jumpDistance;

        this.speed = 1;
    }

    @Override
    public List<Image> draw() {
        if(isVisible()) {
            if (animationTimers[1] == 1) {
                jumping = false;
                if (sprite.getOffsetX() == -2) sprite.setOffsetX(2);
                else sprite.setOffsetX(-2);
            } else if (!jumping) {
                if(canSeePlayer()) {
                    double t = getType().getAnimationDelays()[0][1]; // in frames
                    double v0 = jumpDistance / t;
                    double theta = Math.atan(slope); // degrees
                    double v0x = Math.abs(v0 * Math.cos(theta));
                    double v0y = Math.abs(v0 * Math.sin(theta));
                    if(x > main.getPlayer().getX()) v0x = -v0x;
                    if(y > main.getPlayer().getY()) v0y = -v0y;
                    jumpDistanceX = v0x;
                    jumpDistanceY = v0y;
                } else {
                    jumpDistanceX = 0;
                    jumpDistanceY = 0;
                }
                jumping = true;
                sprite.setOffsetX(0);
            } else {
                x += jumpDistanceX;
                y += jumpDistanceY;
            }
        }
        return super.draw();
    }
}
