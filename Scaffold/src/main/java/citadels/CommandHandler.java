package citadels;

import java.util.*;
import java.util.stream.Collectors;

public class CommandHandler {
  private Game game;

  public CommandHandler(Game game) {
    this.game = game;
    this.scanner = new Scanner(System.in);
  }
  private Scanner scanner = new Scanner(System.in);

  public int numberplayer() {
      System.out.println("Enter how many players [4-7]: ");
      int number = scanner.nextInt();
      scanner.nextLine(); // Consume newline
      return number;
  }

  public CharacterType promptThiefTarget() {
      System.out.println("Choose a character to steal from:");
      for (CharacterType type : CharacterType.values()) {
          System.out.println("- " + type.name());
      }
      while (true) {
          System.out.print("Enter character name: ");
          String input = scanner.nextLine().toUpperCase();
          try {
              return CharacterType.valueOf(input);
          } catch (IllegalArgumentException e) {
              System.out.println("Invalid character. Try again.");
          }
      }
  }
  public int promptNumPlayers() {
    while (true) {
      System.out.println("Enter how many players [4-7]:");
      try {
        int num = Integer.parseInt(scanner.nextLine());
        if (num >= 4 && num <= 7)
          return num;
        System.out.println("Please enter a number between 4 and 7.");
      } catch (NumberFormatException e) {
        System.out.println("Invalid input. Please enter a number.");
      }
    }
  }

  public void displayCharacters(List<CharacterCard> characters) {
    for (int i = 0; i < characters.size(); i++) {
      System.out.println((i + 1) + ": " + characters.get(i).getType().name());
    }
  }

  public CharacterCard promptCharacterChoice(List<CharacterCard> characters) {
    while (true) {
      System.out.print("> ");
      try {
        int choice = Integer.parseInt(scanner.nextLine()) - 1;
        if (choice >= 0 && choice < characters.size()) {
          return characters.get(choice);
        }
        System.out.println("Invalid choice. Please select a number between 1 and " + characters.size() + ".");
      } catch (NumberFormatException e) {
        System.out.println("Invalid input. Please enter a number.");
      }
    }
  }

  public String promptGoldOrCards() {
    while (true) {
      System.out.println("Collect 2 gold or draw two cards and pick one [gold/cards]:");
      String choice = scanner.nextLine().trim().toLowerCase();
      if (choice.equals("gold") || choice.equals("cards"))
        return choice;
      System.out.println("Please enter 'gold' or 'cards'.");
    }
  }

  public DistrictCard promptCardChoice(List<DistrictCard> cards) {
    System.out.println("Choose a card:");
    for (int i = 0; i < cards.size(); i++) {
      System.out.println((i + 1) + ": " + cards.get(i));
    }
    while (true) {
      System.out.print("> ");
      try {
        int choice = Integer.parseInt(scanner.nextLine()) - 1;
        if (choice >= 0 && choice < cards.size()) {
          return cards.get(choice);
        }
        System.out.println("Invalid choice. Please select a number between 1 and " + cards.size() + ".");
      } catch (NumberFormatException e) {
        System.out.println("Invalid input. Please enter a number.");
      }
    }
  }

  public void promptContinue() {
    System.out.println("Press t to process turns");
    while (!scanner.nextLine().trim().equalsIgnoreCase("t")) {
      System.out.println("It is not your turn. Press t to continue with other player turns.");
    }
  }

  public void promptEndTurn() {
    System.out.println("Enter 'end' to end your turn");
    while (!scanner.nextLine().trim().equalsIgnoreCase("end")) {
      handleCommand(scanner.nextLine().trim());
    }
    System.out.println("You ended your turn.");
  }

  public CharacterType promptAssassinTarget() {
    List<CharacterType> validTargets = Arrays.stream(CharacterType.values())
        .filter(t -> t != CharacterType.ASSASSIN)
        .collect(Collectors.toList());
    System.out.println("Who do you want to kill? Choose a character from 2-8:");
    return promptCharacterType(validTargets);
  }

  public CharacterType promptThiefTarget(CharacterType killedCharacter) {
    List<CharacterType> validTargets = Arrays.stream(CharacterType.values())
        .filter(t -> t != CharacterType.ASSASSIN && t != CharacterType.THIEF && t != killedCharacter)
        .collect(Collectors.toList());
    System.out.println("Who do you want to steal from? Choose a character from 3-8:");
    return promptCharacterType(validTargets);
  }

  private CharacterType promptCharacterType(List<CharacterType> validTargets) {
    for (int i = 0; i < validTargets.size(); i++) {
      System.out.println((i + 1) + ": " + validTargets.get(i).name());
    }
    while (true) {
      System.out.print("> ");
      try {
        int choice = Integer.parseInt(scanner.nextLine()) - 1;
        if (choice >= 0 && choice < validTargets.size()) {
          return validTargets.get(choice);
        }
        System.out.println("Invalid choice. Please select a number between 1 and " + validTargets.size() + ".");
      } catch (NumberFormatException e) {
        System.out.println("Invalid input. Please enter a number.");
      }
    }
  }

  public void handleMagicianAction(Player player, List<Player> players, Deck<DistrictCard> districtDeck) {
    System.out.println("Magician's action: [swap/redraw]");
    String action = scanner.nextLine().trim().toLowerCase();
    if (action.equals("swap")) {
      System.out.println("Choose a player to swap hands with:");
      List<Player> others = players.stream().filter(p -> p != player).collect(Collectors.toList());
      for (int i = 0; i < others.size(); i++) {
        System.out.println((i + 1) + ": Player " + others.get(i).getId());
      }
      while (true) {
        try {
          int choice = Integer.parseInt(scanner.nextLine()) - 1;
          if (choice >= 0 && choice < others.size()) {
            Player target = others.get(choice);
            List<DistrictCard> temp = new ArrayList<>(player.getHand());
            player.getHand().clear();
            player.getHand().addAll(target.getHand());
            target.getHand().clear();
            target.getHand().addAll(temp);
            System.out.println("Swapped hands with Player " + target.getId());
            break;
          }
          System.out.println("Invalid choice.");
        } catch (NumberFormatException e) {
          System.out.println("Invalid input. Please enter a number.");
        }
      }
    } else if (action.equals("redraw")) {
      System.out.println("Enter card positions to discard (e.g., 1,2,3):");
      displayHand(player);
      String[] indices = scanner.nextLine().split(",");
      List<Integer> toDiscard = new ArrayList<>();
      try {
        for (String idx : indices) {
          int i = Integer.parseInt(idx.trim()) - 1;
          if (i >= 0 && i < player.getHand().size()) {
            toDiscard.add(i);
          }
        }
        List<DistrictCard> discarded = new ArrayList<>();
        toDiscard.stream().sorted(Comparator.reverseOrder()).forEach(i -> {
          DistrictCard card = player.getHand().get(i);
          player.removeCardFromHand(card);
          districtDeck.addCardToBottom(card);
          discarded.add(card);
        });
        for (int i = 0; i < discarded.size() && !districtDeck.isEmpty(); i++) {
          player.addCardToHand(districtDeck.drawCard());
        }
        System.out.println("Discarded " + discarded.size() + " cards and drew new ones.");
      } catch (NumberFormatException e) {
        System.out.println("Invalid input. Cards not discarded.");
      }
    } else {
      System.out.println("Invalid action. Use 'swap' or 'redraw'.");
    }
  }

  public void handleWarlordAction(Player player, List<Player> players) {
    System.out.println("Warlord's action: Destroy a district? [yes/no]");
    if (!scanner.nextLine().trim().toLowerCase().equals("yes"))
      return;
    List<Player> validTargets = players.stream()
        .filter(p -> p.getCity().size() < 8 && p.getCharacter().getType() != CharacterType.BISHOP)
        .collect(Collectors.toList());
    if (validTargets.isEmpty()) {
      System.out.println("No valid targets to destroy.");
      return;
    }
    System.out.println("Choose a player to destroy a district from:");
    for (int i = 0; i < validTargets.size(); i++) {
      System.out.println((i + 1) + ": Player " + validTargets.get(i).getId());
    }
    Player target;
    while (true) {
      try {
        int choice = Integer.parseInt(scanner.nextLine()) - 1;
        if (choice >= 0 && choice < validTargets.size()) {
          target = validTargets.get(choice);
          break;
        }
        System.out.println("Invalid choice.");
      } catch (NumberFormatException e) {
        System.out.println("Invalid input. Please enter a number.");
      }
    }
    System.out.println("Choose a district to destroy:");
    List<DistrictCard> targetCity = target.getCity();
    for (int i = 0; i < targetCity.size(); i++) {
      DistrictCard district = targetCity.get(i);
      System.out.println((i + 1) + ": " + district + " (cost to destroy: " + (district.getCost() - 1) + ")");
    }
    while (true) {
      try {
        int choice = Integer.parseInt(scanner.nextLine()) - 1;
        if (choice >= 0 && choice < targetCity.size()) {
          DistrictCard toDestroy = targetCity.get(choice);
          int cost = toDestroy.getCost() - 1;
          if (player.getGold() >= cost) {
            player.removeGold(cost);
            target.removeDistrictFromCity(toDestroy);
            System.out.println("Destroyed " + toDestroy.getName() + " from Player " + target.getId());
          } else {
            System.out.println("Not enough gold to destroy " + toDestroy.getName());
          }
          break;
        }
        System.out.println("Invalid choice.");
      } catch (NumberFormatException e) {
        System.out.println("Invalid input. Please enter a number.");
      }
    }
  }

  public boolean promptBuild(Player player) {
    System.out.println("Build a district? [yes/no]");
    if (!scanner.nextLine().trim().toLowerCase().equals("yes"))
      return false;
    displayHand(player);
    System.out.println("Enter the position of the card to build:");
    try {
      int choice = Integer.parseInt(scanner.nextLine()) - 1;
      if (choice >= 0 && choice < player.getHand().size()) {
        DistrictCard card = player.getHand().get(choice);
        if (player.getCity().stream().anyMatch(d -> d.getName().equals(card.getName()))) {
          System.out.println("Cannot build duplicate district: " + card.getName());
          return false;
        }
        if (player.getGold() >= card.getCost()) {
          player.removeGold(card.getCost());
          player.removeCardFromHand(card);
          player.addDistrictToCity(card);
          System.out.println("Built " + card);
          return true;
        } else {
          System.out.println("Not enough gold to build " + card.getName());
          return false;
        }
      } else {
        System.out.println("Invalid position.");
        return false;
      }
    } catch (NumberFormatException e) {
      System.out.println("Invalid input. Please enter a number.");
      return false;
    }
  }

  public void handleCommand(String input) {
    String[] parts = input.trim().split("\\s+");
    String command = parts[0].toLowerCase();
    Player human = game.getPlayers().stream().filter(p -> p instanceof HumanPlayer).findFirst().orElse(null);
    if (human == null)
      return;

    switch (command) {
      case "t":
        break;
      case "hand":
        displayHand(human);
        break;
      case "gold": {
        int playerId = parts.length > 1 ? parseInt(parts[1], human.getId()) : human.getId();
        Player target = findPlayer(playerId);
        if (target != null) {
          System.out.println("Player " + playerId + " has " + target.getGold() + " gold.");
        }
        break;
      }
      case "build":
        if (parts.length > 1) {
          try {
            int position = Integer.parseInt(parts[1]) - 1;
            if (position >= 0 && position < human.getHand().size()) {
              DistrictCard card = human.getHand().get(position);
              if (human.getCity().stream().anyMatch(d -> d.getName().equals(card.getName()))) {
                System.out.println("Cannot build duplicate district: " + card.getName());
              } else if (human.getGold() >= card.getCost()) {
                human.removeGold(card.getCost());
                human.removeCardFromHand(card);
                human.addDistrictToCity(card);
                System.out.println("Built " + card);
              } else {
                System.out.println("Not enough gold to build " + card.getName());
              }
            } else {
              System.out.println("Invalid position.");
            }
          } catch (NumberFormatException e) {
            System.out.println("Invalid position. Please enter a number.");
          }
        } else {
          System.out.println("Usage: build <position>");
        }
        break;
      case "citadel":
      case "list":
      case "city": {
        int playerId = parts.length > 1 ? parseInt(parts[1], human.getId()) : human.getId();
        Player target = findPlayer(playerId);
        if (target != null) {
          System.out.println("Player " + playerId + " has built:");
          if (target.getCity().isEmpty()) {
            System.out.println("(empty)");
          } else {
            target.getCity().forEach(d -> System.out.println(d));
          }
        }
        break;
      }
      case "action":
        if (human.getCharacter() != null) {
          System.out.println("Your character's ability: " + human.getCharacter().getType().getAbility());
          switch (human.getCharacter().getType()) {
            case MAGICIAN:
              System.out.println("Use: action swap <player> or action redraw <id1,id2,...>");
              break;
            case WARLORD:
              System.out.println("Use: action to destroy a district");
              break;
            case ASSASSIN:
            case THIEF:
              System.out.println("Already performed at start of turn.");
              break;
            default:
              System.out.println("This ability is performed automatically.");
          }
        } else {
          System.out.println("No character selected.");
        }
        break;
      case "info":
        if (parts.length > 1) {
          try {
            int position = Integer.parseInt(parts[1]) - 1;
            if (position >= 0 && position < human.getHand().size()) {
              DistrictCard card = human.getHand().get(position);
              if (card.getColor() == Color.PURPLE && !card.getSpecialAbility().isEmpty()) {
                System.out.println(card.getName() + ": " + card.getSpecialAbility());
              } else {
                System.out.println("No special ability for " + card.getName());
              }
            } else {
              try {
                CharacterType type = CharacterType.valueOf(parts[1].toUpperCase());
                System.out.println(type.name() + ": " + type.getAbility());
              } catch (IllegalArgumentException e) {
                System.out.println("Invalid card position or character name.");
              }
            }
          } catch (NumberFormatException e) {
            try {
              CharacterType type = CharacterType.valueOf(parts[1].toUpperCase());
              System.out.println(type.name() + ": " + type.getAbility());
            } catch (IllegalArgumentException ex) {
              System.out.println("Invalid input. Use 'info <position>' or 'info <character>'.");
            }
          }
        } else {
          System.out.println("Usage: info <position> or info <character>");
        }
        break;
      case "end":
        // Handled by promptEndTurn
        break;
      case "all":
        for (Player p : game.getPlayers()) {
          StringBuilder sb = new StringBuilder();
          sb.append("Player ").append(p.getId());
          if (p == human)
            sb.append(" (you)");
          sb.append(": cards=").append(p.getHand().size())
              .append(" gold=").append(p.getGold())
              .append(" city=");
          if (p.getCity().isEmpty()) {
            sb.append("(empty)");
          } else {
            p.getCity().forEach(d -> sb.append(d).append(" "));
          }
          System.out.println(sb);
        }
        break;
      case "save":
        if (parts.length > 1) {
          game.saveGame(parts[1]);
        } else {
          System.out.println("Usage: save <filename>");
        }
        break;
      case "load":
        if (parts.length > 1) {
          game.loadGame(parts[1]);
        } else {
          System.out.println("Usage: load <filename>");
        }
        break;
      case "debug":
        game.toggleDebugMode();
        break;
      case "":
        break;
      default:
        displayHelp();
    }
  }

  private void displayHand(Player player) {
    System.out.println("You have " + player.getGold() + " gold. Cards in hand:");
    if (player.getHand().isEmpty()) {
      System.out.println("(empty)");
    } else {
      for (int i = 0; i < player.getHand().size(); i++) {
        DistrictCard card = player.getHand().get(i);
        System.out.println((i + 1) + ". " + card.getName() + " (" + card.getColor().name().toLowerCase() + "), cost: "
            + card.getCost());
      }
    }
  }

  private Player findPlayer(int id) {
    return game.getPlayers().stream()
        .filter(p -> p.getId() == id)
        .findFirst()
        .orElseGet(() -> {
          System.out.println("Player " + id + " not found.");
          return null;
        });
  }

  private int parseInt(String str, int defaultValue) {
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private void displayHelp() {
    System.out.println("Available commands:");
    System.out.println("t : processes turns");
    System.out.println("hand : shows cards in hand");
    System.out.println("gold [p] : shows gold of a player");
    System.out.println("build <place in hand> : builds a building into your city");
    System.out.println("citadel/list/city [p] : shows districts built by a player");
    System.out.println("action : gives info about your special action and how to perform it");
    System.out.println("info <position or character> : shows information about a character or building");
    System.out.println("end : ends your turn");
    System.out.println("all : shows all current game info");
    System.out.println("save <file> : saves the game state");
    System.out.println("load <file> : loads a game state");
    System.out.println("debug : toggles debug mode");
  }

  public CharacterCard getKilledCharacter() {
    return game.getKilledCharacter();
  }
}