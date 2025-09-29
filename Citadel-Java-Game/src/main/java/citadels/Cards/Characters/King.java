package citadels.Cards.Characters;

import citadels.Cards.CharacterCard;
import citadels.Cards.DistrictCard;
import citadels.Game.Game;
import citadels.Player.Player;
import citadels.Utils.DistrictColor;

public class King extends CharacterCard {
    public King() {
        super("King", 4,
                "Gains one gold for each yellow (noble) district in their city. They receive the crown token and will be the first to choose characters on the next round.",
                "");
    }

    @Override
    public void performSpecialAbility(Game game, Player currentPlayer, String commandLine) {
        // King's special ability is handled automatically
    }

    @Override
    public void collectDistrictIncome(Game game, Player currentPlayer) {
        int yellowCount = 0;

        // Count yellow districts
        for (DistrictCard card : currentPlayer.getCity()) {
            if (card.getColor() == DistrictColor.YELLOW) {
                yellowCount++;
            }
        }

        // Check for School of Magic (counts as any color for income)
        boolean hasSchoolOfMagic = currentPlayer.getCity().stream()
                .anyMatch(card -> card.getName().equals("School Of Magic"));

        if (hasSchoolOfMagic) {
            yellowCount++;
            System.out.println("School Of Magic counts as a yellow district for income.");
        }

        if (yellowCount > 0) {
            currentPlayer.addGold(yellowCount);
            System.out.println(
                    currentPlayer.getName() + " received " + yellowCount + " gold from yellow districts.");
        }
    }
}