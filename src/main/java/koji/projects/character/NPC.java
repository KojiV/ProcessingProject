package koji.projects.character;

import koji.projects.Image;
import koji.projects.Main;
import koji.projects.area.Area;
import koji.projects.data.Textbox;
import lombok.Getter;
import lombok.Setter;
import org.simpleyaml.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class NPC extends Textbox {
    private final Image sprite;
    private final Textbox normalText;
    private final boolean overCounter;
    private final FileConfiguration fc;
    @Getter private final String key;
    @Getter @Setter private boolean talked = false;
    @Getter private final boolean talkable;

    public NPC(int spriteId, int x, int y,
               int areaX, int areaY,
               Textbox initialText, Textbox normalText,
               int objActivate, boolean overCounter, FileConfiguration fc, String key,
               boolean talkable
    ) {
        super(x * getMapScale(), y * getMapScale(),
                new Area(areaX, areaY),
                initialText == null ? normalText.getText() : initialText.getText(),
                objActivate
        );
        sprite = new Image(
                main.loadImage(Main.getPrefix() + "textures/npcs/" + spriteId + ".png"),
                this.x, this.y
        );

        this.normalText = normalText == null ? initialText : normalText;
        this.overCounter = overCounter;
        this.fc = fc;
        this.key = key;
        this.talkable = talkable;
    }

    @Override public void onComplete() {
        if(!talked) {
            talked = true;
            text = normalText.getText();
        }
        for(String key : Main.getKeys(fc, key + ".extraTags.", false)) {
            boolean value = fc.getBoolean(key);
            String name = key.split("\\.")[key.split("\\.").length - 1];

            try {
                Field f = Player.class.getDeclaredField(name);
                f.setAccessible(true);
                f.set(getPlayer(), value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    @Override public List<Image> draw() {
        if(main.getArea().matches(area)) {
            main.image(sprite.getImage(), x, y);
            return List.of(sprite);
        }
        return new ArrayList<>();
    }

    @Override public boolean intersects(Player player) {
        if(!overCounter) return super.intersects(player);
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
                y + getMapScale() * 2 + player.getInteractRange()
        };

        boolean noOverlap = rec1[0] > rec2[2] ||
                rec2[0] > rec1[2] ||
                rec1[1] > rec2[3] ||
                rec2[1] > rec1[3];

        return !noOverlap;
    }
}
