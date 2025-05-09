package citadels;

public class Bishop extends CharacterCard {
    public Bishop() {
        super(CharacterType.BISHOP, CharacterType.BISHOP.getRank(), CharacterType.BISHOP.getAbility());
    }

    @Override
    public void doAbility(Player self, Game game) {
        long count = self.getCity().stream()
            .filter(d -> d.getColor() == Color.BLUE)
            .count();
        self.addGold((int) count);
        System.out.println("Bishop collected " + count + " gold.");
    }
}