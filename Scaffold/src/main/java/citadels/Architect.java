package citadels;

public class Architect extends CharacterCard {
    public Architect() {
        super(CharacterType.ARCHITECT);
    }

    @Override
    public void doAbility(Player self, Game game) {
        for (int i = 0; i < 2 && !game.getDistrictDeck().isEmpty(); i++) {
            self.addCardToHand(game.getDistrictDeck().drawCard());
        }
        System.out.println("Architect drew 2 extra cards.");
    }
}