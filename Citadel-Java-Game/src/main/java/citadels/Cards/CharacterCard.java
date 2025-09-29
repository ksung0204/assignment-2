package citadels.Cards;

import citadels.Game.Game;
import citadels.Player.Player;

import java.util.Scanner;

public abstract class CharacterCard extends Card {
    protected int rank;
    protected String specialAbility;
    protected String performSpecialAbilityDescription;

    public CharacterCard(String name, int rank, String specialAbility, String performSpecialAbilityDescription) {
        this.name = name;
        this.rank = rank;
        this.specialAbility = specialAbility;
        this.performSpecialAbilityDescription = performSpecialAbilityDescription;
    }

    public int getRank() {
        return rank;
    }

    public String getSpecialAbility() {
        return specialAbility;
    }

    public abstract void performSpecialAbility(Game game, Player currentPlayer, String commandLine);

    public void collectDistrictIncome(Game game, Player currentPlayer) {
        // Default implementation does nothing
        // Subclasses like King, Bishop, etc. will override this
    }

    public String getPerformSpecialAbilityDescription() {
        return String.format("%s %s", specialAbility, performSpecialAbilityDescription);
    }

    public void resetTurnState() {
        // Default implementation does nothing
        // Subclasses that need to track turn state will override this
    }
}
