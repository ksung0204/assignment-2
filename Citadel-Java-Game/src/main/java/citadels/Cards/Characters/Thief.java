package citadels.Cards.Characters;

import citadels.Cards.CharacterCard;
import citadels.Game.Game;
import citadels.Player.Player;

public class Thief extends CharacterCard {
    public Thief() {
        super("Thief", 2,
                "Select another character whom you wish to rob. When a player reveals that character to take his turn, you immediately take all of his gold. You cannot rob the Assassin or the killed character.",
                "To perform this action, type 'action steal <character rank>'");
    }

    @Override
    public void performSpecialAbility(Game game, Player currentPlayer, String commandLine) {
        if (commandLine.startsWith("action steal")) {
            try {
                int targetRank = Integer.parseInt(commandLine.substring("action steal".length()).trim());

                if (targetRank < 3 || targetRank > 8) {
                    System.out.println("Invalid character rank. Please choose a rank between 3 and 8.");
                    return;
                }

                // Cannot rob the Assassin (rank 1)
                if (targetRank == 1) {
                    System.out.println("You cannot rob the Assassin.");
                    return;
                }

                // Cannot rob the killed character
                if (targetRank == game.getKilledCharacterRank()) {
                    System.out.println("You cannot rob the killed character.");
                    return;
                }

                // Cannot rob yourself
                if (currentPlayer.getCurrentCharacterCard().getRank() == targetRank) {
                    System.out.println("You cannot rob yourself.");
                    return;
                }

                game.setRobbedCharacterRank(targetRank, currentPlayer);

                CharacterCard targetCharacter = game.getCharacterByRank(targetRank);
                if (targetCharacter != null) {
                    System.out.println("You chose to rob the " + targetCharacter.getName());
                } else {
                    System.out.println("You chose to rob character with rank " + targetRank);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between 3 and 8.");
            }
        } else {
            System.out.println("Invalid action for Thief. Use 'action steal <character_rank>'");
        }
    }
}