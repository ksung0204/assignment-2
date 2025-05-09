package citadels;

public class Warlord extends CharacterCard {
    public Warlord() {
        super(CharacterType.WARLORD, CharacterType.WARLORD.getRank(), CharacterType.WARLORD.getAbility());
    }

    @Override
    public void doAbility(Player self, Game game) {
        long count = self.getCity().stream()
            .filter(d -> d.getColor() == Color.RED)
            .count();
        self.addGold((int) count);

        if (self instanceof HumanPlayer) {
            game.getCommandHandler().handleWarlordAbility(self, game);
        } else {
            ComputerPlayerLogic.performWarlordAction(self, game.getPlayers());
        }
    }
}
