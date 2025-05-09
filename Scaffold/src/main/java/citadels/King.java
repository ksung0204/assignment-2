package citadels;

public class King extends CharacterCard {
    public King() {
        super(CharacterType.KING);
    }

    @Override
    public void doAbility(Player self, Game game) {
        long count = self.getCity().stream()
                .filter(d -> d.getColor() == Color.YELLOW)
                .count();
        self.addGold((int) count);
        game.setCrownedPlayer(self);
        System.out.println("King collected " + count + " gold and becomes crowned player.");
    }
}
