package citadels.Cards.Characters;

import citadels.Cards.CharacterCard;
import citadels.Cards.DistrictCard;
import citadels.Game.Game;
import citadels.Player.Player;
import citadels.Utils.DistrictColor;

public class Merchant extends CharacterCard {
    public Merchant() {
        super("Merchant", 6, "Gains one gold for each green (trade) district in their city. Gains one extra gold.", "");
    }

    @Override
    public void performSpecialAbility(Game game, Player currentPlayer, String commandLine) {
        // Merchant's special ability is handled automatically
    }

    @Override
    public void collectDistrictIncome(Game game, Player currentPlayer) {
        int greenCount = 0;

        // Count green districts
        for (DistrictCard card : currentPlayer.getCity()) {
            if (card.getColor() == DistrictColor.GREEN) {
                greenCount++;
            }
        }

        // Check for School of Magic (counts as any color for income)
        boolean hasSchoolOfMagic = currentPlayer.getCity().stream()
                .anyMatch(card -> card.getName().equals("School Of Magic"));

        if (hasSchoolOfMagic) {
            greenCount++;
            System.out.println("School Of Magic counts as a green district for income.");
        }

        if (greenCount > 0) {
            currentPlayer.addGold(greenCount);
            System.out.println(
                    currentPlayer.getName() + " received " + greenCount + " gold from green districts.");
        }
    }
}