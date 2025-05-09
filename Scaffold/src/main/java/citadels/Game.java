package citadels;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Game {
  private List<Player> players;
  private Deck<DistrictCard> districtDeck;
  private Deck<CharacterCard> characterDeck;
  private Player crownedPlayer;
  private boolean gameEnded;
  private CommandHandler commandHandler;
  private boolean debugMode;
  private CharacterCard killedCharacter;
  private Map<Player, CharacterCard> stolenCharacters;
  private static final String CARDS_FILE = "cards.tsv";

  public Game() {
    players = new ArrayList<>();
    districtDeck = new Deck<>();
    characterDeck = new Deck<>();
    gameEnded = false;
    debugMode = false;
    commandHandler = new CommandHandler(this);
    stolenCharacters = new HashMap<>();
    initializeCharacterDeck();
  }

  public void start() {
    initializeGame();
    gameLoop();
  }

  private void initializeGame() {
    int numPlayers = commandHandler.promptNumPlayers();
    initializePlayers(numPlayers);
    initializeDistrictDeck();
    assignCrown();
    dealInitialCardsAndGold();
    System.out.println("Starting Citadels with " + numPlayers + " players...");
    System.out.println("You are player 1");
  }

  private void initializePlayers(int numPlayers) {
    players.add(new HumanPlayer(1, commandHandler));
    for (int i = 2; i <= numPlayers; i++) {
      players.add(new AIPlayer(i));
    }
  }

  private void initializeDistrictDeck() {
    try (BufferedReader br = new BufferedReader(
        new FileReader(URLDecoder.decode(this.getClass().getResource("cards.tsv").getPath(),
            StandardCharsets.UTF_8.name())))) {
      String line = br.readLine(); // skip the header
      while ((line = br.readLine()) != null) {
        String[] parts = line.split("\t");
        if (parts.length < 4)
          continue;

        String name = parts[0];
        int quantity = Integer.parseInt(parts[1]);
        Color color = Color.valueOf(parts[2].toUpperCase());
        int cost = Integer.parseInt(parts[3]);
        String specialAbility = parts.length > 4 ? parts[4] : "";
        for (int i = 0; i < quantity; i++) {
          districtDeck.addCard(new DistrictCard(name, color, cost, specialAbility));
        }
      }
    } catch (IOException e) {
      System.err.println("Error reading cards.tsv: " + e.getMessage());
      System.exit(1);
    }
    districtDeck.shuffle();
  }

  private void initializeCharacterDeck() {
    for (CharacterType type : CharacterType.values()) {
      characterDeck.addCard(new CharacterCard(type));
    }
  }

  private void assignCrown() {
    Random rand = new Random();
    crownedPlayer = players.get(rand.nextInt(players.size()));
  }

  private void dealInitialCardsAndGold() {
    for (Player player : players) {
      player.addGold(2);
      for (int i = 0; i < 4; i++) {
        player.addCardToHand(districtDeck.drawCard());
      }
    }
  }

  private void gameLoop() {
    while (!gameEnded) {
      playRound();
      checkGameEnd();
    }
    calculateScoresAndDeclareWinner();
  }

  private void playRound() {
    characterSelectionPhase();
    turnPhase();
  }

  private void characterSelectionPhase() {
    System.out.println("================================\nSELECTION PHASE\n================================");
    characterDeck.shuffle();
    List<CharacterCard> availableCharacters = new ArrayList<>(characterDeck.getCards());
    List<CharacterCard> discarded = new ArrayList<>();
    // Discard one face-down
    discarded.add(availableCharacters.remove(new Random().nextInt(availableCharacters.size())));
    System.out.println("A mystery character was removed.");
    // Discard face-up based on player count
    int faceUpDiscards = getFaceUpDiscards(players.size());
    for (int i = 0; i < faceUpDiscards; i++) {
      CharacterCard card = null;
      do {
        if (availableCharacters.isEmpty())
          break;
        card = availableCharacters.remove(new Random().nextInt(availableCharacters.size()));
        if (card.getType() == CharacterType.KING) {
          System.out.println("King was removed.");
          System.out.println("The King cannot be visibly removed, trying again..");
          availableCharacters.add(card);
        }
      } while (card != null && card.getType() == CharacterType.KING);
      if (card != null) {
        discarded.add(card);
        System.out.println(card.getType().name() + " was removed.");
      }
    }
    // Players choose characters
    List<Player> selectionOrder = new ArrayList<>();
    int crownedIndex = players.indexOf(crownedPlayer);
    for (int i = 0; i < players.size(); i++) {
      selectionOrder.add(players.get((crownedIndex + i) % players.size()));
    }
    for (Player player : selectionOrder) {
      player.chooseCharacter(availableCharacters);
      availableCharacters.remove(player.getCharacter());
    }
  }

  private int getFaceUpDiscards(int numPlayers) {
    switch (numPlayers) {
      case 4:
        return 2;
      case 5:
        return 1;
      default:
        return 0;
    }
  }

  private void turnPhase() {
    System.out.println("Character choosing is over, action round will now begin.");
    System.out.println("================================\nTURN PHASE\n================================");
    killedCharacter = null;
    stolenCharacters.clear();
    for (int rank = 1; rank <= 8; rank++) {
      CharacterType currentType = getCharacterTypeByRank(rank);
      Player currentPlayer = findPlayerWithCharacter(currentType);
      if (currentPlayer == null) {
        System.out.println(rank + ": " + currentType.name() + "\nNo one is the " + currentType.name());
        commandHandler.promptContinue();
        continue;
      }
      if (currentPlayer.getCharacter() == killedCharacter) {
        System.out.println(rank + ": " + currentType.name() + "\nPlayer " + currentPlayer.getId()
            + " was killed and skips their turn.");
        commandHandler.promptContinue();
        continue;
      }
      System.out.println(rank + ": " + currentType.name());
      System.out.println("Player " + currentPlayer.getId() + " is the " + currentType.name());
      if (currentPlayer instanceof HumanPlayer) {
        System.out.println("Your turn.");
      }
      if (debugMode && currentPlayer instanceof AIPlayer) {
        System.out.println("Debug: Player " + currentPlayer.getId() + " hand: " + currentPlayer.getHand());
      }
      // Handle Thief's stolen gold
      if (stolenCharacters.containsValue(currentPlayer.getCharacter())) {
        Player thief = stolenCharacters.entrySet().stream()
            .filter(e -> e.getValue() == currentPlayer.getCharacter())
            .map(Map.Entry::getKey)
            .findFirst().orElse(null);
        if (thief != null) {
          int gold = currentPlayer.getGold();
          currentPlayer.removeGold(gold);
          thief.addGold(gold);
          System.out
              .println("Player " + thief.getId() + " stole " + gold + " gold from Player " + currentPlayer.getId());
        }
      }
      // Collect gold for district types
      switch (currentType) {
        case KING:
          int nobleGold = currentPlayer.getCity().stream()
              .filter(d -> d.getColor() == Color.YELLOW)
              .mapToInt(DistrictCard::getCost)
              .sum();
          currentPlayer.addGold(nobleGold);
          System.out.println("King gains " + nobleGold + " gold for noble districts.");
          crownedPlayer = currentPlayer;
          break;
        case BISHOP:
          int religiousGold = currentPlayer.getCity().stream()
              .filter(d -> d.getColor() == Color.BLUE)
              .mapToInt(DistrictCard::getCost)
              .sum();
          currentPlayer.addGold(religiousGold);
          System.out.println("Bishop gains " + religiousGold + " gold for religious districts.");
          break;
        case MERCHANT:
          int tradeGold = currentPlayer.getCity().stream()
              .filter(d -> d.getColor() == Color.GREEN)
              .mapToInt(DistrictCard::getCost)
              .sum();
          currentPlayer.addGold(tradeGold + 1);
          System.out.println("Merchant gains " + tradeGold + " gold for trade districts and 1 extra gold.");
          break;
        case WARLORD:
          int militaryGold = currentPlayer.getCity().stream()
              .filter(d -> d.getColor() == Color.RED)
              .mapToInt(DistrictCard::getCost)
              .sum();
          currentPlayer.addGold(militaryGold);
          System.out.println("Warlord gains " + militaryGold + " gold for military districts.");
          break;
      }
      // Collect resources
      currentPlayer.collectResources(districtDeck);
      // Perform special ability
      currentPlayer.performSpecialAbility(this);
      // Build districts
      int maxBuilds = currentType == CharacterType.ARCHITECT ? 3 : 1;
      for (int i = 0; i < maxBuilds; i++) {
        currentPlayer.buildDistrict(this);
      }
      if (currentPlayer instanceof HumanPlayer) {
        commandHandler.promptEndTurn();
      } else {
        System.out.println("Player " + currentPlayer.getId() + " ended their turn.");
        commandHandler.promptContinue();
      }
    }
  }

  private CharacterType getCharacterTypeByRank(int rank) {
    for (CharacterType type : CharacterType.values()) {
      if (type.getRank() == rank)
        return type;
    }
    return null;
  }

  private Player findPlayerWithCharacter(CharacterType type) {
    return players.stream()
        .filter(p -> p.getCharacter() != null && p.getCharacter().getType() == type)
        .findFirst()
        .orElse(null);
  }

  private void checkGameEnd() {
    gameEnded = players.stream().anyMatch(p -> p.getCity().size() >= 8);
  }

  private void calculateScoresAndDeclareWinner() {
    Map<Player, Integer> scores = new HashMap<>();
    Player firstCompleted = null;
    for (Player player : players) {
      int score = 0;
      // Points for building costs
      score += player.getCity().stream().mapToInt(DistrictCard::getCost).sum();
      // Points for all colors
      Set<Color> colors = new HashSet<>();
      player.getCity().forEach(d -> colors.add(d.getColor()));
      if (colors.size() == Color.values().length) {
        score += 3;
        System.out.println("Player " + player.getId() + " gains 3 points for having all district types.");
      }
      // Points for completed city
      if (player.getCity().size() >= 8) {
        if (firstCompleted == null) {
          firstCompleted = player;
          score += 4;
          System.out.println("Player " + player.getId() + " gains 4 points for first completed city.");
        } else {
          score += 2;
          System.out.println("Player " + player.getId() + " gains 2 points for completed city.");
        }
      }
      // Points from purple districts (simplified)
      for (DistrictCard district : player.getCity()) {
        if (district.getColor() == Color.PURPLE && !district.getSpecialAbility().isEmpty()) {
          score += 1; // Placeholder for special ability points
          System.out
              .println("Player " + player.getId() + " gains 1 point from " + district.getName() + " special ability.");
        }
      }
      scores.put(player, score);
      System.out.println("Player " + player.getId() + " score: " + score);
    }
    // Determine winner
    int maxScore = Collections.max(scores.values());
    List<Player> winners = scores.entrySet().stream()
        .filter(e -> e.getValue() == maxScore)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    Player winner;
    if (winners.size() > 1) {
      winner = winners.stream()
          .max(Comparator.comparing(p -> p.getCharacter() != null ? p.getCharacter().getType().getRank() : 0))
          .orElse(winners.get(0));
      System.out.println("Tie resolved by highest character rank.");
    } else {
      winner = winners.get(0);
    }
    System.out.println("Congratulations! Player " + winner.getId() + " wins with " + maxScore + " points!");
  }

  // Methods for CommandHandler and AI logic
  @SuppressWarnings("unchecked")
  public void saveGame(String filename) {
    GameState state = new GameState(players, districtDeck, characterDeck, crownedPlayer, gameEnded);
    try (FileWriter writer = new FileWriter(filename)) {
      writer.write(state.toJSON().toJSONString());
      System.out.println("Game saved to " + filename);
    } catch (IOException e) {
      System.out.println("Error saving game: " + e.getMessage());
    }
  }

  public void loadGame(String filename) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
      StringBuilder jsonStr = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        jsonStr.append(line);
      }

      JSONParser parser = new JSONParser();
      JSONObject json = (JSONObject) parser.parse(jsonStr.toString());

      GameState state = GameState.fromJSON(json, commandHandler);
      this.players = state.getPlayers();
      this.districtDeck = state.getDistrictDeck();
      this.characterDeck = state.getCharacterDeck();
      this.crownedPlayer = state.getCrownedPlayer();
      this.gameEnded = state.isGameEnded();
      System.out.println("Game loaded from " + filename);
    } catch (IOException | ParseException e) {
      System.out.println("Error loading game: " + e.getMessage());
    }
  }

  public void setKilledCharacter(CharacterCard character) {
    this.killedCharacter = character;
  }

  public void addStolenCharacter(Player thief, CharacterCard target) {
    stolenCharacters.put(thief, target);
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

  public CharacterCard getKilledCharacter() {
    return killedCharacter;
  }

  public Player getCrownedPlayer() {
    return crownedPlayer;
  }

  public boolean isDebugMode() {
    return debugMode;
  }

  public void toggleDebugMode() {
    debugMode = !debugMode;
    System.out.println("Debug mode " + (debugMode ? "enabled" : "disabled"));
  }
}