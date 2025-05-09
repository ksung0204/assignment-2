package citadels;

import java.util.List;

public class Magician extends CharacterCard {
    public Magician() {
        super(CharacterType.MAGICIAN, CharacterType.MAGICIAN.getRank(), CharacterType.MAGICIAN.getAbility());
    }

    @Override
    public void doAbility(Player self, Game game) {
        if (self instanceof HumanPlayer) {
            System.out.println("Magician: Skipping manual action (no access to CommandHandler).");
        } else {
            List<DistrictCard> hand = self.getHand();
            for (DistrictCard card : hand) {
                game.getDistrictDeck().addCard(card);
            }
            hand.clear();
            for (int i = 0; i < 2 && !game.getDistrictDeck().isEmpty(); i++) {
                self.addCardToHand(game.getDistrictDeck().drawCard());
            }
            System.out.println("AI Magician refreshed hand.");
        }
    }
}
