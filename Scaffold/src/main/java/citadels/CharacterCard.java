package citadels;

public abstract class CharacterCard extends Card {
    protected final int rank;
    protected final String abilityDescription;
    protected final CharacterType type;

    public CharacterCard(CharacterType type, int rank, String abilityDescription) {
        this.type = type;
        this.rank = rank;
        this.abilityDescription = abilityDescription;
    }

    public CharacterType getType() {
        return type;
    }

    public int getRank() {
        return rank;
    }

    public String getAbilityDescription() {
        return abilityDescription;
    }

    @Override
    public String getName() {
        return type.name();
    }

    public abstract void doAbility(Player self, Game game);
}
