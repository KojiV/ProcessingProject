package koji.projects.character;

import koji.projects.GameObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

public abstract class Moveable extends GameObject {
    public abstract void loadSprites();
    @Getter @Setter protected int x, y;

    @Getter protected final boolean[] moving = new boolean[4];
    @Getter @Setter protected State state;

    @AllArgsConstructor
    public enum State {
        MOVE_DOWN(false),
        MOVE_LEFT(false),
        MOVE_RIGHT(false),
        MOVE_UP(false),
        ATTACK_DOWN(true, -32, 0, 0, 0),
        ATTACK_UP(true, 0, -32, -32, -32),
        ATTACK_RIGHT(true, 0, 0, 0, -32),
        ATTACK_LEFT(true, -32, -32, -32, 0);

        State(boolean attackingState) {
            this.attackingState = attackingState;
            this.firstOffsetX = 0;
            this.firstOffsetY = 0;
            this.secondOffsetX = 0;
            this.secondOffsetY = 0;
        }

        @Getter
        private final boolean attackingState;
        @Getter private final int firstOffsetX, firstOffsetY, secondOffsetX, secondOffsetY;
    }
}
