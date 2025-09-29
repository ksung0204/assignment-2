package citadels.Cards.Characters;

import citadels.Cards.CharacterCard;
import citadels.Player.Player;
import citadels.Game.Game;

public class Assassin extends CharacterCard {
    public Assassin() {
        super("Assassin", 1, "Select another character whom you wish to kill. The killed character loses their turn.",
                "To perform this action, type 'action kill <character_rank>'");
    }

    @Override
    public void performSpecialAbility(Game game, Player currentPlayer, String commandLine) {
        if (commandLine.startsWith("action kill")) {
            try {
                int targetRank = Integer.parseInt(commandLine.substring("action kill".length()).trim());

                if (targetRank < 2 || targetRank > 8) {
                    System.out.println("Invalid character rank. Please choose a rank between 2 and 8.");
                    return;
                }

                // Cannot assassinate yourself
                if (currentPlayer.getCurrentCharacterCard().getRank() == targetRank) {
                    System.out.println("You cannot assassinate yourself.");
                    return;
                }

                game.setKilledCharacterRank(targetRank);

                CharacterCard targetCharacter = game.getCharacterByRank(targetRank);
                if (targetCharacter != null) {
                    System.out.println("You chose to assassinate the " + targetCharacter.getName());
                } else {
                    System.out.println("You chose to assassinate character with rank " + targetRank);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 2 and 8.");
            }
        } else {
            System.out.println("Invalid action for Assassin. Use 'action kill <character_rank>'");
        }
    }
}
