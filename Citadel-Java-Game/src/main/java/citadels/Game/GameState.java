package citadels.Game;

import java.util.List;
import citadels.Cards.CharacterCard;
import citadels.Cards.DistrictCard;
import citadels.Player.Player;
import citadels.Utils.Deck;

public class GameState {
    private List<Player> players;
    private Deck<DistrictCard> districtDeck;
    private List<CharacterCard> allCharacterCards;
    private CharacterCard mysteryCardFaceDown;
    private int crownedPlayer;
    private int killCharacterRank;
    private int robbedCharacterRank;
    private int thiefPlayer;
    private boolean gameEnded;
    private int firstPlayerToCompleteCity;

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public Deck<DistrictCard> getDistrictDeck() {
        return districtDeck;
    }

    public void setDistrictDeck(Deck<DistrictCard> districtDeck) {
        this.districtDeck = districtDeck;
    }

    public List<CharacterCard> getAllCharacterCards() {
        return allCharacterCards;
    }

    public void setAllCharacterCards(List<CharacterCard> allCharacterCards) {
        this.allCharacterCards = allCharacterCards;
    }

    public CharacterCard getMysteryCardFaceDown() {
        return mysteryCardFaceDown;
    }

    public void setMysteryCardFaceDown(CharacterCard mysteryCardFaceDown) {
        this.mysteryCardFaceDown = mysteryCardFaceDown;
    }

    public int getCrownedPlayer() {
        return crownedPlayer;
    }

    public void setCrownedPlayer(int crownedPlayer) {
        this.crownedPlayer = crownedPlayer;
    }

    public int getKillCharacterRank() {
        return killCharacterRank;
    }

    public void setKillCharacterRank(int killCharacterRank) {
        this.killCharacterRank = killCharacterRank;
    }

    public int getRobbedCharacterRank() {
        return robbedCharacterRank;
    }

    public void setRobbedCharacterRank(int robbedCharacterRank) {
        this.robbedCharacterRank = robbedCharacterRank;
    }

    public int getThiefPlayer() {
        return thiefPlayer;
    }

    public void setThiefPlayer(int thiefPlayer) {
        this.thiefPlayer = thiefPlayer;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public void setGameEnded(boolean gameEnded) {
        this.gameEnded = gameEnded;
    }

    public int getFirstPlayerToCompleteCity() {
        return firstPlayerToCompleteCity;
    }

    public void setFirstPlayerToCompleteCity(int firstPlayerToCompleteCity) {
        this.firstPlayerToCompleteCity = firstPlayerToCompleteCity;
    }
}
