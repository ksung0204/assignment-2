package citadels.Cards.Characters;

import citadels.Cards.CharacterCard;
import citadels.Cards.DistrictCard;
import citadels.Game.Game;
import citadels.Player.Player;

public class Architect extends CharacterCard {
    public Architect() {
        super("Architect", 7, "Gains two extra district cards. Can build up to 3 districts per turn.",
                "The Architect automatically draws 2 extra district cards at the beginning of their turn.");
    }

    @Override
    public void performSpecialAbility(Game game, Player currentPlayer, String commandLine) {
        // Draw 2 extra cards
        for (int i = 0; i < 2; i++) {
            DistrictCard card = game.getDistrictDeck().drawCard();
            if (card != null) {
                currentPlayer.addDistrictCardToHand(card);
                System.out.println("Architect drew an extra card: " + card.getName());
            } else {
                System.out.println("The district deck is empty.");
                break;
            }
        }
        System.out.println("As the Architect, you can build up to 3 districts this turn.");
    }
}
