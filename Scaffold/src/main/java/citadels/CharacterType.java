package citadels;


public enum CharacterType {
    ASSASSIN(1, "Select another character to kill. The killed character loses their turn."),
    THIEF(2,
        "Select another character to rob. When they reveal, you take all their gold. Cannot rob Assassin or killed character."),
    MAGICIAN(3, "Either exchange your hand with another player's, or discard any number of cards and draw that many."),
    KING(4, "Gain 1 gold per yellow district. Receive the crown token."),
    BISHOP(5, "Gain 1 gold per blue district. Your buildings cannot be destroyed by Warlord unless killed."),
    MERCHANT(6, "Gain 1 gold per green district. Gain 1 extra gold."),
    ARCHITECT(7, "Gain 2 extra district cards. Can build up to 3 districts per turn."),
    WARLORD(8, "Gain 1 gold per red district. Can destroy one district by paying one less than its cost.");

    private final int rank;
    private final String ability;
    CharacterType(int rank, String ability) {
    this.rank = rank;
    this.ability = ability;
    }

    public int getRank() {
        return rank;
    }

    public String getAbility() {
        return ability;
    }
}
