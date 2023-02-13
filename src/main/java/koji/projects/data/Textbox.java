package koji.projects.data;

import koji.projects.area.Area;
import koji.projects.GameObject;
import koji.projects.character.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class Textbox extends GameObject {
    @Getter protected int x, y;
    @Getter protected Area area;
    @Getter protected List<String> text;
    @Getter protected int objActivate;

    public boolean intersects(Player player) {
        int[] rec1 = {
                player.getX(),
                player.getY(),
                player.getX() + player.getWidth(),
                player.getY() + player.getHeight()
        };
        int[] rec2 = {
                x - player.getInteractRange(),
                y - player.getInteractRange(),
                x + getMapScale() + player.getInteractRange(),
                y + getMapScale() + player.getInteractRange()
        };

        boolean noOverlap = rec1[0] > rec2[2] ||
                rec2[0] > rec1[2] ||
                rec1[1] > rec2[3] ||
                rec2[1] > rec1[3];

        return !noOverlap;
    }

    public void onComplete() {}
}
