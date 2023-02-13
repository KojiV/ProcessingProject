package koji.projects.data;

import koji.projects.GameObject;
import koji.projects.Main;
import koji.projects.area.Area;
import lombok.Getter;
import lombok.Setter;
import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Objective extends GameObject {

    @Getter @Setter private int x, y, id;
    @Getter @Setter private Area area;
    @Getter @Setter private boolean areaLock;

    public Objective() {
        super();
        area = getArea();

        id = 0;
        x = 14;
        y = 4;
        area = new Area(0, 0);
        areaLock = true;
    }

    public static Objective objFromId(int id) {
        Objective obj = new Objective();
        obj.setId(id);
        obj.setX(obj.getXFromFile());
        obj.setY(obj.getYFromFile());
        obj.setArea(obj.getAreaFromFile());
        obj.setAreaLock(obj.getAreaLockFromFile());
        return obj;
    }

    private static final File objFile = new File(
            Main.getPrefix() + "data/objectives.yml"
    );

    public String getText() {
        try {
            return YamlConfiguration.loadConfiguration(objFile).getString("objectives." + id + ".text");
        } catch (IOException e) {
            return "";
        }
    }

    public int getXFromFile() {
        try {
            return YamlConfiguration.loadConfiguration(objFile).getInt(
                    "objectives." + id + ".arrow.x"
            );
        } catch (IOException e) {
            return 0;
        }
    }

    public int getYFromFile() {
        try {
            return YamlConfiguration.loadConfiguration(objFile).getInt(
                    "objectives." + id + ".arrow.y"
            );
        } catch (IOException e) {
            return 0;
        }
    }

    public Area getAreaFromFile() {
        try {
            FileConfiguration fc = YamlConfiguration.loadConfiguration(objFile);
            return new Area(
                    fc.getInt("objectives." + id + ".arrow.areaX"),
                    fc.getInt("objectives." + id + ".arrow.areaY")
            );
        } catch (IOException ex) {
            return getArea();
        }
    }

    public boolean getAreaLockFromFile() {
        try {
            return YamlConfiguration.loadConfiguration(objFile)
                    .getBoolean("objectives." + id + ".areaLock");
        } catch (IOException ex) {
            return false;
        }
    }
}
