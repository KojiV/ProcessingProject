package koji.projects;

import lombok.Getter;
import lombok.Setter;
import processing.core.PImage;

public class Image {
    @Getter private final PImage image;
    @Getter @Setter private float x, y, offsetX, offsetY, width, height;

    public Image(PImage image, float x, float y, int offsetX, int offsetY, float width, float height) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;

        if(image != null) Main.resize(image);
    }

    public Image(PImage image, float x, float y) {
        this(image, x, y, 0, 0, image.width, image.height);
    }

    public Image(PImage image, float x, float y, int[] offsets) {
        this(image, x, y, offsets[0], offsets[1], image.width, image.height);
    }

    public Image(PImage image) {
        this(image, 0, 0);
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public void draw() {
        draw(1);
    }

    public void draw(float multi) {
        Main.getMain().image(image, multi * (x + offsetX), multi * (y + offsetY));
    }
}
