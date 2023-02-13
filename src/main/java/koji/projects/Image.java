package koji.projects;

import lombok.Getter;
import processing.core.PImage;

public class Image {
    @Getter private final PImage image;
    @Getter private final int x, y, offsetX, offsetY;

    public Image(PImage image, int x, int y, int offsetX, int offsetY) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public Image(PImage image, int x, int y) {
        this(image, x, y, 0, 0);
    }

    public Image(PImage image, int x, int y, int[] offsets) {
        this(image, x, y, offsets[0], offsets[1]);
    }

    public int getWidth() {
        return image.width;
    }

    public int getHeight() {
        return image.height;
    }
}
