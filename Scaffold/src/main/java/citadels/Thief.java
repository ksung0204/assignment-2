package citadels;

public class Thief extends CharacterCard {
    public Thief() {
        super(CharacterType.THIEF);
    }

    @Override
    public void doAbility(Player self, Game game) {
        CharacterType target = game.getCommandHandler().promptThiefTarget();
        if (target == CharacterType.ASSASSIN || game.getKilledCharacter().getType() == target) {
            System.out.println("Invalid steal target.");
            return;
        }

        Player victim = game.getPlayerByCharacterType(target);
        if (victim != null && victim != self) {
            int stolen = victim.getGold();
            victim.removeGold(stolen);
            self.addGold(stolen);
            System.out.println("Player " + self.getId() + " (Thief) stole " + stolen + " gold.");
        }
    }
}