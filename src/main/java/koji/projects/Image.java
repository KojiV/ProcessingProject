package koji.projects;

import lombok.Getter;
import lombok.Setter;
import processing.core.PImage;

public class Image {
    @Getter private final PImage image;
    @Getter @Setter private float x, y, offsetX, offsetY;

    public Image(PImage image, float x, float y, int offsetX, int offsetY) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.offsetX = offsetX;
        this.offsetY = offsetY;

        Main.resize(image);
    }

    public Image(PImage image, float x, float y) {
        this(image, x, y, 0, 0);
    }

    public Image(PImage image, float x, float y, int[] offsets) {
        this(image, x, y, offsets[0], offsets[1]);
    }

    public Image(PImage image) {
        this(image, 0, 0);
    }

    public int getWidth() {
        return image.width;
    }

    public int getHeight() {
        return image.height;
    }

    public void draw() {
        draw(1);
    }

    public void draw(float multi) {
        Main.getMain().image(image, multi * (x + offsetX), multi * (y + offsetY));
    }
}
