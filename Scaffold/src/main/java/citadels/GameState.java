package citadels;

import org.json.simple.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameState {
  private List<Player> players;
  private Deck<DistrictCard> districtDeck;
  private Deck<CharacterCard> characterDeck;
  private Player crownedPlayer;
  private boolean gameEnded;

  public GameState(List<Player> players, Deck<DistrictCard> districtDeck, Deck<CharacterCard> characterDeck,
      Player crownedPlayer, boolean gameEnded) {
    this.players = players;
    this.districtDeck = districtDeck;
    this.characterDeck = characterDeck;
    this.crownedPlayer = crownedPlayer;
    this.gameEnded = gameEnded;
  }

  @SuppressWarnings("unchecked")
  public JSONObject toJSON() {
    JSONObject json = new JSONObject();
    JSONArray playersArray = new JSONArray();
    for (Player player : players) {
      JSONObject playerJson = new JSONObject();
      playerJson.put("type", player instanceof HumanPlayer ? "HumanPlayer" : "AIPlayer");
      playerJson.put("id", player.getId());
      playerJson.put("gold", player.getGold());

      JSONArray handArray = new JSONArray();
      for (DistrictCard card : player.getHand()) {
        handArray.add(card.getName());
      }
      playerJson.put("hand", handArray);

      JSONArray cityArray = new JSONArray();
      for (DistrictCard card : player.getCity()) {
        cityArray.add(card.getName());
      }
      playerJson.put("city", cityArray);
      playerJson.put("character",
          player.getCharacter() != null ? player.getCharacter().getType().name() : null);
      playersArray.add(playerJson);
    }
    json.put("players", playersArray);

    JSONArray districtDeckArray = new JSONArray();
    for (DistrictCard card : districtDeck.getCards()) {
      districtDeckArray.add(card.getName());
    }
    json.put("districtDeck", districtDeckArray);

    JSONArray characterDeckArray = new JSONArray();
    for (CharacterCard card : characterDeck.getCards()) {
      characterDeckArray.add(card.getType().name());
    }
    json.put("characterDeck", characterDeckArray);
    json.put("crownedPlayerId", crownedPlayer.getId());
    json.put("gameEnded", gameEnded);
    return json;
  }

  public static GameState fromJSON(JSONObject json, CommandHandler commandHandler) {
    List<Player> players = new ArrayList<>();
    JSONArray playersArray = (JSONArray) json.get("players");
    // Temporary storage for district cards to reconstruct deck
    List<DistrictCard> allDistrictCards = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader("cards.tsv"))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] parts = line.split("\t");
        if (parts.length < 4)
          continue;
        String name = parts[0];
        Color color = Color.valueOf(parts[1].toUpperCase());
        int cost = Integer.parseInt(parts[2]);
        int quantity = Integer.parseInt(parts[3]);
        String specialAbility = parts.length > 4 ? parts[4] : "";
        for (int i = 0; i < quantity; i++) {
          allDistrictCards.add(new DistrictCard(name, color, cost, specialAbility));
        }
      }
    } catch (IOException e) {
      System.err.println("Error reading cards.tsv: " + e.getMessage());
    }

    for (int i = 0; i < playersArray.size(); i++) {
      JSONObject playerJson = (JSONObject) playersArray.get(i);
      int id = ((Long) playerJson.get("id")).intValue();
      Player player;

      if (((String) playerJson.get("type")).equals("HumanPlayer")) {
        player = new HumanPlayer(id, commandHandler);
      } else {
        player = new AIPlayer(id);
      }

      player.addGold(((Long) playerJson.get("gold")).intValue());

      JSONArray handArray = (JSONArray) playerJson.get("hand");
      for (int j = 0; j < handArray.size(); j++) {
        String cardName = (String) handArray.get(j);
        DistrictCard card = allDistrictCards.stream()
            .filter(c -> c.getName().equals(cardName))
            .findFirst()
            .orElse(null);
        if (card != null) {
          player.addCardToHand(card);
          allDistrictCards.remove(card);
        }
      }

      JSONArray cityArray = (JSONArray) playerJson.get("city");
      for (int j = 0; j < cityArray.size(); j++) {
        String cardName = (String) cityArray.get(j);
        DistrictCard card = allDistrictCards.stream()
            .filter(c -> c.getName().equals(cardName))
            .findFirst()
            .orElse(null);
        if (card != null) {
          player.addDistrictToCity(card);
          allDistrictCards.remove(card);
        }
      }

      Object characterObj = playerJson.get("character");
      String character = (characterObj == null) ? null : (String) characterObj;
      if (character != null) {
        CharacterType type = CharacterType.valueOf(character);
        switch (type) {
            case ASSASSIN: player.setCharacter(new Assassin()); break;
            case THIEF: player.setCharacter(new Thief()); break;
            case MAGICIAN: player.setCharacter(new Magician()); break;
            case KING: player.setCharacter(new King()); break;
            case BISHOP: player.setCharacter(new Bishop()); break;
            case MERCHANT: player.setCharacter(new Merchant()); break;
            case ARCHITECT: player.setCharacter(new Architect()); break;
            case WARLORD: player.setCharacter(new Warlord()); break;
          }
      }

      players.add(player);
    }

    Deck<DistrictCard> districtDeck = new Deck<>();
    JSONArray districtDeckArray = (JSONArray) json.get("districtDeck");
    for (int i = 0; i < districtDeckArray.size(); i++) {
      String cardName = (String) districtDeckArray.get(i);
      DistrictCard card = allDistrictCards.stream()
          .filter(c -> c.getName().equals(cardName))
          .findFirst()
          .orElse(null);
      if (card != null) {
        districtDeck.addCard(card);
        allDistrictCards.remove(card);
      }
    }

    Deck<CharacterCard> characterDeck = new Deck<>();
    JSONArray characterDeckArray = (JSONArray) json.get("characterDeck");
    for (int i = 0; i < characterDeckArray.size(); i++) {
      CharacterType type = CharacterType.valueOf((String) characterDeckArray.get(i));
      switch (type) {
        case ASSASSIN: characterDeck.addCard(new Assassin()); break;
        case THIEF: characterDeck.addCard(new Thief()); break;
        case MAGICIAN: characterDeck.addCard(new Magician()); break;
        case KING: characterDeck.addCard(new King()); break;
        case BISHOP: characterDeck.addCard(new Bishop()); break;
        case MERCHANT: characterDeck.addCard(new Merchant()); break;
        case ARCHITECT: characterDeck.addCard(new Architect()); break;
        case WARLORD: characterDeck.addCard(new Warlord()); break;
      }
    }

    Long crownedPlayerIdLong = (Long) json.get("crownedPlayerId");
    int crownedPlayerId = crownedPlayerIdLong.intValue();
    Player crownedPlayer = players.stream()
        .filter(p -> p.getId() == crownedPlayerId)
        .findFirst()
        .orElse(players.get(0));

    boolean gameEnded = (Boolean) json.get("gameEnded");

    return new GameState(players, districtDeck, characterDeck, crownedPlayer, gameEnded);
  }

  // Getters
  public List<Player> getPlayers() {
    return players;
  }

  public Deck<DistrictCard> getDistrictDeck() {
    return districtDeck;
  }

  public Deck<CharacterCard> getCharacterDeck() {
    return characterDeck;
  }

  public Player getCrownedPlayer() {
    return crownedPlayer;
  }

  public boolean isGameEnded() {
    return gameEnded;
  }
}