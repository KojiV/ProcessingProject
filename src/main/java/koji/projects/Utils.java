package koji.projects;

import org.simpleyaml.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static List<String> getKeys(FileConfiguration fc, String key, boolean deep) {
        List<String> list = new ArrayList<>();
        for (String keys : fc.getKeys(true)) {
            if (keys.startsWith(key)) {
                if (!deep) {
                    boolean skip = false;
                    for (String alreadyExisting : list) {
                        if (keys.startsWith(alreadyExisting + ".")) {
                            skip = true;
                            break;
                        }
                    }
                    if (skip) continue;
                }
                list.add(keys);
            }
        }
        return list;
    }

    public static <T> T getOrDefault(List<T> list, int index, T def) {
        if(index < 0 || index >= list.size()) return def;
        return list.get(index);
    }
}
