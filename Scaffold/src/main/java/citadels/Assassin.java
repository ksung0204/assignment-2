package citadels;
public class Assassin extends CharacterCard{
    public Assassin() {
        super(CharacterType.ASSASSIN);
    }

    @Override
    public void doAbility(Player self, Game game) {
        CharacterType target = game.getCommandHandler().promptAssassinTarget();
        CharacterCard killed = game.findCharacterCard(target);
        game.setKilledCharacter(killed);
        System.out.println("Player " + self.getId() + " (Assassin) killed " + target.name());
    }
}
