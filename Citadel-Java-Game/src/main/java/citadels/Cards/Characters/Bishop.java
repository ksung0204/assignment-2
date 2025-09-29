package citadels.Cards.Characters;

import citadels.Cards.CharacterCard;
import citadels.Cards.DistrictCard;
import citadels.Game.Game;
import citadels.Player.Player;
import citadels.Utils.DistrictColor;

public class Bishop extends CharacterCard {
    public Bishop() {
        super("Bishop", 5,
                "Gains one gold for each blue (religious) district in their city. Their buildings cannot be destroyed by the Warlord, unless they are killed by the Assassin",
                "");
    }

    @Override
    public void performSpecialAbility(Game game, Player currentPlayer, String commandLine) {
        // Bishop's special ability is handled automatically
    }

    @Override
    public void collectDistrictIncome(Game game, Player currentPlayer) {
        int blueCount = 0;

        // Count blue districts
        for (DistrictCard card : currentPlayer.getCity()) {
            if (card.getColor() == DistrictColor.BLUE) {
                blueCount++;
            }
        }

        // Check for School of Magic (counts as any color for income)
        boolean hasSchoolOfMagic = currentPlayer.getCity().stream()
                .anyMatch(card -> card.getName().equals("School Of Magic"));

        if (hasSchoolOfMagic) {
            blueCount++;
            System.out.println("School Of Magic counts as a blue district for income.");
        }

        if (blueCount > 0) {
            currentPlayer.addGold(blueCount);
            System.out.println(
                    currentPlayer.getName() + " received " + blueCount + " gold from blue districts.");
        }
    }
}