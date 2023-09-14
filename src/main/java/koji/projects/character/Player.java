package koji.projects.character;

import koji.projects.Image;
import koji.projects.Main;
import koji.projects.Utils;
import koji.projects.area.Arrow;
import koji.projects.data.BottomBar;
import koji.projects.data.Objective;
import koji.projects.data.Stats;
import koji.projects.data.Textbox;
import lombok.Getter;
import lombok.Setter;
import org.simpleyaml.configuration.file.FileConfiguration;
import org.simpleyaml.configuration.file.YamlConfiguration;
import processing.core.PImage;
import processing.event.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Player extends Moveable {
    @Getter @Setter private int interactRange,
            width = 25,
            height = 25;
    @Getter private final File playerData = new File(Main.getPrefix() + "data/player/player.yml");

    public Player() {
        super();
        this.x = 504;
        this.y = 128;
        this.state = State.MOVE_DOWN;
        this.interactRange = 20;

        this.speed = Stats.SPEED.getBaseAmount();
        this.health = Stats.HEALTH.getBaseAmount();
        this.defense = Stats.DEFENSE.getBaseAmount();
        this.damage = Stats.DAMAGE.getBaseAmount();
        this.attackSpeed = Stats.ATTACK_SPEED.getBaseAmount();

        loadSprites();
    }

    @Override public void loadSprites() {
        PImage spriteSheet = main.loadImage(Main.getPrefix() + "textures/player/spritesheet.png");

        for (int state = 0; state < 2; state++) {
            for (int i = 0; i < 4; i++) {
                State s = State.values()[state * 4 + i];
                List<Image> images = new ArrayList<>();
                for (int i1 = 0; i1 < (state == 1 ? 4 : 7); i1++) {
                    int amount = (state + 1) * 32 ;
                    int extra = state == 1 ? 128 : 0;
                    PImage img = spriteSheet.get(i1 * amount, i * amount + extra, amount, amount);
                    images.add(new Image(img, x, y, getOffsets(s, i1)));
                }
                animations.put(s, images);
            }
        }
        sprite = animations.get(State.MOVE_DOWN).get(0);
    }

    public int[] getOffsets(State state, int index) {
        if(!state.isAttackingState()) return new int[2];
        if(index <= 1) return new int[] { state.getFirstOffsetX(), state.getFirstOffsetY() };
        return new int[] { state.getSecondOffsetX(), state.getSecondOffsetY() };
    }

    public List<CollisionObject> getNearbyCollisionBoxes() {
        return getNearbyCollisionBoxes(getArea().getCollisions(), x, y);
    }

    public List<CollisionObject> getNearbyCollisionBoxes(List<CollisionObject> boxes, int x, int y) {
        int tileX = x / main.getMapScale();
        int tileY = y / main.getMapScale();
        int index = tileX * 18 + tileY;

        return new ArrayList<>(Stream.of(
                Utils.getOrDefault(boxes, index, null),
                Utils.getOrDefault(boxes, index + 1, null),
                Utils.getOrDefault(boxes, index - 1, null),
                Utils.getOrDefault(boxes, index - 19, null),
                Utils.getOrDefault(boxes, index - 18, null),
                Utils.getOrDefault(boxes, index - 17, null),
                Utils.getOrDefault(boxes, index + 19, null),
                Utils.getOrDefault(boxes, index + 18, null),
                Utils.getOrDefault(boxes, index + 17, null)
        ).filter(Objects::nonNull).toList());
    }

    private final static HashMap<State, List<Image>> animations = new HashMap<>();
    private int moveOverrideSprite = 1;

    public void attackFunction() {
        if(moveTimer1 == 11 - attackSpeed) {
            moveTimer1 = 0;
            if(moveTimer2 + 1 > 3) {
                moveTimer2 = 0;
                switch (state) {
                    case ATTACK_UP -> {
                        state = State.MOVE_UP;
                        moveOverrideSprite = 0;
                    }
                    case ATTACK_DOWN -> {
                        state = State.MOVE_DOWN;
                        moveOverrideSprite = 1;
                    }
                    case ATTACK_LEFT -> {
                        state = State.MOVE_LEFT;
                        moveOverrideSprite = 2;
                    }
                    case ATTACK_RIGHT -> {
                        state = State.MOVE_RIGHT;
                        moveOverrideSprite = 3;
                    }
                }
                sprite = animations.get(state).get(0);
                sprite.setX(x + sprite.getOffsetX());
                sprite.setY(y + sprite.getOffsetY());

                sprite.draw();
            } else moveTimer2++;

        } else moveTimer1++;

        useLegs();
    }

    public void walkFunction() {
        if(moveTimer1 == 4) {
            moveTimer1 = 0;

            if(moveTimer2 + 1 > 6) moveTimer2 = 0;
            else moveTimer2++;
        } else moveTimer1++;

        useLegs();
    }

    public void useLegs() {
        for(int theSpeed = 0; theSpeed < speed; theSpeed++) {
            int moveX = x + parseAddedX();
            int moveY = y + parseAddedY();

            if (parseAddedX() != 0 || parseAddedY() != 0) {
                List<CollisionObject> nearbyObjects = getNearbyCollisionBoxes();

                boolean canMoveHorizontal = nearbyObjects.stream().noneMatch(o ->
                        o.intersects(this, moveX, y)
                ) && moveX > -1 && moveX < 1024 - width;
                boolean canMoveVertical = nearbyObjects.stream().noneMatch(o ->
                        o.intersects(this, x, moveY)
                ) && moveY > -1 && moveY < 567 - height;
                //Main.println(canMoveHorizontal, canMoveVertical);

                if ((canMoveHorizontal || canMoveVertical) && Arrays.stream(
                        Arrow.Direction.getCoreDirections()).noneMatch(
                        d -> isOffscreen(d) && canMove(d)
                )) {
                    //Main.println("asparagus", x, y, moveX, moveY);
                    x = canMoveHorizontal ? moveX : x;
                    y = canMoveVertical ? moveY : y;
                } else if (Arrays.stream(Arrow.Direction.getCoreDirections()).anyMatch(
                        d -> isOffscreen(d) && canMove(d)
                )) {
                    int[] changedCords = new int[]{ x, y };

                    Arrow.Direction dir = Arrays.stream(Arrow.Direction.getCoreDirections()).filter(
                            d -> isOffscreen(d) && canMove(d)
                    ).findAny().get();

                    switch (dir) {
                        case UP -> changedCords[1] = 2;
                        case DOWN -> changedCords[1] = 565 - height;
                        case LEFT -> changedCords[0] = 2;
                        case RIGHT -> changedCords[0] = 1022 - width;
                    }
                    if(main.getArea().changeArea(dir)) {
                        x = changedCords[0];
                        y = changedCords[1];
                    }
                }
            }
        }
    }

    public boolean canMove(Arrow.Direction dir) {
        int[] nextArea = new int[]{getArea().getX(), getArea().getY()};
        switch (dir) {
            case UP -> nextArea[1]--;
            case DOWN -> nextArea[1]++;
            case LEFT -> nextArea[0]--;
            case RIGHT -> nextArea[0]++;
        }

        if (getObjective().isAreaLock() && getObjective().getArea().matches(
                getArea().getX(), getArea().getY()
        )) return false;

        int[] changedCords = new int[]{x, y};

        switch (dir) {
            case UP -> changedCords[1] = 2;
            case DOWN -> changedCords[1] = 565 - height;
            case LEFT -> changedCords[0] = 2;
            case RIGHT -> changedCords[0] = 1022 - width;
        }

        List<CollisionObject> objects = getArea().loadCollisions(nextArea[0], nextArea[1]);

        if(objects.isEmpty()) return false;
        return getNearbyCollisionBoxes(
                objects,
                changedCords[0], changedCords[1]
        ).stream().noneMatch(s -> s.intersects(this, changedCords[0], changedCords[1]));
    }

    public boolean isOffscreen(Arrow.Direction direction) {
        return switch (direction) {
            case DOWN -> y - 1 < 0;
            case UP -> y + 1 > 566 - height;
            case LEFT -> x - 1 < 0;
            case RIGHT -> x + 1 > 1023 - width;
            case SAME -> false; //This should never hit LMAO
            // why the lmao past me???
        };
    }

    public int parseAddedX() {
        int added = 0;
        if(moving[2]) added--;
        if(moving[3]) added++;
        return added;
    }

    public int parseAddedY() {
        int added = 0;
        if(moving[0]) added--;
        if(moving[1]) added++;
        return added;
    }

    public List<Image> draw() {
        if(!getArea().isOnTitleScreen()) {
            if ((parseAddedX() != 0 || parseAddedY() != 0) || state.isAttackingState()) {
                State correctState;
                switch (moveOverrideSprite) {
                    default -> correctState = !state.isAttackingState() ? State.MOVE_UP : State.ATTACK_UP;
                    case 1 -> correctState = !state.isAttackingState() ? State.MOVE_DOWN : State.ATTACK_DOWN;
                    case 2 -> correctState = !state.isAttackingState() ? State.MOVE_LEFT : State.ATTACK_LEFT;
                    case 3 -> correctState = !state.isAttackingState() ? State.MOVE_RIGHT : State.ATTACK_RIGHT;
                }

                if(!state.isAttackingState()) walkFunction();
                else attackFunction();
                sprite = animations.get(correctState).get(moveTimer2);
                sprite.setX(x);
                sprite.setY(y);

                main.playerUpdated();
            }
            sprite.draw();
            return List.of(sprite);
        }
        return new ArrayList<>();
    }

    private int moveTimer1 = 0;
    private int moveTimer2 = 0;

    public void keyPressed(KeyEvent event) {
        if(main.getBar().getBarState() != BottomBar.BarState.TEXT) {
            int key = event.getKeyCode();
            if(key == Main.UP || key == main.getUpKey()) {
                moving[0] = true;
                if(!state.isAttackingState()) {
                    moveOverrideSprite = 0;
                    state = State.MOVE_UP;
                }
            } else if (key == Main.DOWN || key == main.getDownKey()) {
                moving[1] = true;
                if(!state.isAttackingState()) {
                    moveOverrideSprite = 1;
                    state = State.MOVE_DOWN;
                }
            } else if (key == Main.LEFT || key == main.getLeftKey()) {
                moving[2] = true;
                if(!state.isAttackingState()) {
                    moveOverrideSprite = 2;
                    state = State.MOVE_LEFT;
                }
            } else if (key == Main.RIGHT || key == main.getRightKey()) {
                moving[3] = true;
                if(!state.isAttackingState()) {
                    moveOverrideSprite = 3;
                    state = State.MOVE_RIGHT;
                }
            } else if (key == 32) {
                List<Textbox> boxes = new ArrayList<>(getBoxesForArea());
                boxes.addAll(main.getNpcs());

                if (boxes.stream().anyMatch(b -> b.intersects(getPlayer()) &&
                        (!(b instanceof NPC) || ((NPC) b).isTalkable()))
                ) {
                    Textbox box = boxes.stream().filter(
                            b -> b.intersects(getPlayer())
                    ).findFirst().get();
                    main.getBar().setTextbox(box);
                    main.getBar().setBarState(BottomBar.BarState.TEXT);
                }
            } else if (key == main.getEKey()) {
                if(hasSword && !state.isAttackingState()) {
                    moveTimer1 = 0;
                    moveTimer2 = 0;
                    switch (state) {
                        case MOVE_UP -> {
                            state = State.ATTACK_UP;
                            moveOverrideSprite = 0;
                        }
                        case MOVE_DOWN -> {
                            state = State.ATTACK_DOWN;
                            moveOverrideSprite = 1;
                        }
                        case MOVE_LEFT -> {
                            state = State.ATTACK_LEFT;
                            moveOverrideSprite = 2;
                        }
                        case MOVE_RIGHT -> {
                            state = State.ATTACK_RIGHT;
                            moveOverrideSprite = 3;
                        }
                    }

                }
                //Attacking (if it has sword)
            }
        }
        if(event.getKey() == 'c')
            Main.println("Cords:", x, y, x / main.getMapScale(), y / main.getMapScale());
    }

    public void keyReleased(KeyEvent event) {
        boolean did = true;
        int key = event.getKeyCode();
        if(key == main.getUpKey() || key == Main.UP) {
            moving[0] = false;
        } else if (key == main.getDownKey() || key == Main.DOWN) {
            moving[1] = false;
        } else if (key == main.getLeftKey() || key == Main.LEFT) {
            moving[2] = false;
        } else if (key == main.getRightKey() || key == Main.RIGHT) {
            moving[3] = false;
        } else {
            did = false;
        }
        if (did && !state.isAttackingState()) {
            moveTimer1 = 0;
            moveTimer2 = 0;
            Stream<Boolean> stream = IntStream.range(0, moving.length)
                    .mapToObj(idx -> moving[idx]);

            if (stream.filter(obj -> obj).count() == 1) {
                for (int i = 0; i < 4; i++) {
                    if (moving[i]) {
                        moveOverrideSprite = i;
                        switch (i) {
                            default -> state = State.MOVE_UP;
                            case 1 -> state = State.MOVE_DOWN;
                            case 2 -> state = State.MOVE_LEFT;
                            case 3 -> state = State.MOVE_RIGHT;
                        }
                    }
                }
            }
        }
    }

    private static final HashMap<Stats, Integer> stats = new HashMap<>();

    public boolean readStats() {
        try {
            FileConfiguration fC = YamlConfiguration.loadConfiguration(playerData);
            for(Stats stat : Stats.values()) {
                stats.put(stat, fC.getInt("stats." + stat.name().toLowerCase()));
            }
            x = fC.getInt("x");
            y = fC.getInt("y");
            getArrow().setObjective(Objective.objFromId(fC.getInt("obj")));
            getArea().changeArea(fC.getInt("areaX"), fC.getInt("areaY"));
            for(String boxes : fC.getStringList("read-boxes"))
                if(main.getTextboxes().containsKey(boxes))
                    main.getTextboxes().get(boxes).setObjActivate(-1);

            for(String npcs : fC.getStringList("talked-npcs"))
                for (NPC npc : main.getNpcs())
                    if(npc.getKey().equals(npcs))
                        npc.onComplete();


            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            Main.println("PLAYER DATA FAILED TO LOAD, STARTING NEW GAME");
            return false;
        }
    }

    public void updateStats() throws IOException {
        if (!playerData.exists()) {
            playerData.createNewFile();
        }

        FileConfiguration fC = YamlConfiguration.loadConfiguration(playerData);
        try {
            for(Stats stat : Stats.values()) {
                fC.set("stats." + stat.name().toLowerCase(), stats.getOrDefault(stat, stat.getBaseAmount()));
            }
            fC.set("x", x);
            fC.set("y", y);
            fC.set("areaX", getArea().getX());
            fC.set("areaY", getArea().getY());
            fC.set("obj", getArrow().getObjective().getId());
            List<String> list = new ArrayList<>();
            for(String key : main.getTextboxes().keySet()) {
                Textbox box = main.getTextboxes().get(key);
                if(box.getObjActivate() == -1) list.add(key);
            }
            if(!list.isEmpty()) fC.set("read-boxes", list);
            list = new ArrayList<>();
            for(NPC npc : main.getNpcs()) {
                if(npc.isTalked()) list.add(npc.getKey());
            }
            if(!list.isEmpty()) fC.set("talked-npcs", list);

            fC.save(playerData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Getter @Setter private boolean hasSword = false;
}
