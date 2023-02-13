package koji.projects.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Stats {
    HEALTH(10),
    DEFENSE(0),
    ATTACK_SPEED(5),
    SPEED(3),
    DAMAGE(1);

    @Getter private final int baseAmount;
}
