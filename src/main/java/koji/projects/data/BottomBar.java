package koji.projects.data;

import koji.projects.GameObject;
import koji.projects.Main;
import koji.projects.Utils;
import koji.projects.character.NPC;
import lombok.Getter;
import lombok.Setter;
import processing.event.KeyEvent;

public class BottomBar extends GameObject {
    @Getter private BarState barState;
    @Setter private Textbox textbox;

    private int currentTextboxPage = -1;

    public void drawBar() {
        main.strokeWeight(5);
        main.stroke(255);
        main.fill(0);
        main.rect(3, 577, 1018, 114);
        switch (barState) {
            case STANDARD -> {
                main.textAlign(main.LEFT);
                main.textFont(main.getTextFont());
                main.fill(255);

                main.textSize(Main.getGameScale() * 24);
                main.text("Health: " + (int) main.getPlayer().getHealth(), 20, 613);
                main.text("Speed: " + (int) main.getPlayer().getSpeed(), 20, 647);
                main.text("Defense: " + (int) main.getPlayer().getDefense(), 20, 681);
                main.text("Damage: " + (int) main.getPlayer().getDamage(), 210, 612);
                main.text("Atk Spd: " + (int) main.getPlayer().getAttackSpeed(), 210, 647);

                main.textSize(Main.getGameScale() * 18);
                main.text("Objective: ", 648, 616);
                main.text(getArrow().getObjective().getText(), 796, 616);
            }
            case TEXT -> {
               display();
            }
        }
    }

    private void display() {
        main.textAlign(main.CENTER);
        main.textFont(main.getTextFont());
        main.fill(255);
        main.textSize(Main.getGameScale() * 32);

        String text = Utils.getOrDefault(textbox.getText(), currentTextboxPage, "");
        StringBuilder sb = new StringBuilder(text);
        int i = 0;
        while ((i = sb.indexOf(" ", i + 30)) != -1) {
            sb.replace(i, i + 1, "\n");
        }
        main.text(sb.toString(), 512, 642);
        main.fill(0);
    }

    @Override public void keyPressed(KeyEvent event) {
        if(barState == BarState.TEXT && event.getKey() == ' ') {
            if(currentTextboxPage + 1 >= textbox.getText().size()) {
                if(textbox.getObjActivate() != -1) {
                    textbox.onComplete();
                    getArrow().setObjective(Objective.objFromId(
                            textbox.getObjActivate()
                    ));
                    getArrow().onAreaUpdate();
                    textbox.setObjActivate(-1);
                    if(textbox instanceof NPC) ((NPC) textbox).setTalked(true);
                }

                currentTextboxPage = -1;
                setTextbox(null);
                setBarState(BarState.STANDARD);
                return;
            }
            currentTextboxPage++;

            main.strokeWeight(5);
            main.stroke(255);
            main.fill(0);
            main.rect(3, 579, 1018, 114);

            display();
        }
    }

    public void setBarState(BarState barState) {
        this.barState = barState;
        drawBar();
    }

    public BottomBar() {
        super();
        barState = BarState.STANDARD;
        textbox = null;
    }

    public enum BarState {
        STANDARD,
        TEXT;
    }
}
