package citadels;

import java.util.*;
import java.util.stream.Collectors;

public class ComputerPlayerLogic {
  private static final Random rand = new Random();

  public static CharacterCard chooseCharacter(Player player, List<CharacterCard> availableCharacters) {
    // Prioritize Architect or Magician if low on cards
    if (player.getHand().size() <= 2) {
      Optional<CharacterCard> preferred = availableCharacters.stream()
          .filter(c -> c.getType() == CharacterType.ARCHITECT || c.getType() == CharacterType.MAGICIAN)
          .findFirst();
      if (preferred.isPresent())
        return preferred.get();
    }
    // Otherwise, prioritize characters that gain gold based on city composition
    Map<Color, Integer> colorCounts = new HashMap<>();
    for (DistrictCard district : player.getCity()) {
      colorCounts.merge(district.getColor(), 1, Integer::sum);
    }
    List<CharacterType> preferredTypes = new ArrayList<>();
    if (colorCounts.getOrDefault(Color.YELLOW, 0) > 1)
      preferredTypes.add(CharacterType.KING);
    if (colorCounts.getOrDefault(Color.BLUE, 0) > 1)
      preferredTypes.add(CharacterType.BISHOP);
    if (colorCounts.getOrDefault(Color.GREEN, 0) > 1)
      preferredTypes.add(CharacterType.MERCHANT);
    if (colorCounts.getOrDefault(Color.RED, 0) > 1)
      preferredTypes.add(CharacterType.WARLORD);
    Optional<CharacterCard> strategic = availableCharacters.stream()
        .filter(c -> preferredTypes.contains(c.getType()))
        .findFirst();
    if (strategic.isPresent())
      return strategic.get();
    // Random choice if no strategic option
    return availableCharacters.get(rand.nextInt(availableCharacters.size()));
  }

  public static void collectResources(Player player, Deck<DistrictCard> districtDeck) {
    // Prefer cards if hand is low, otherwise prefer gold
    if (player.getHand().size() <= 2 && !districtDeck.isEmpty()) {
      List<DistrictCard> drawn = new ArrayList<>();
      for (int i = 0; i < 2 && !districtDeck.isEmpty(); i++) {
        drawn.add(districtDeck.drawCard());
      }
      if (!drawn.isEmpty()) {
        DistrictCard chosen = drawn.get(rand.nextInt(drawn.size())); // Simplified: random choice
        player.addCardToHand(chosen);
        drawn.remove(chosen);
        drawn.forEach(districtDeck::addCardToBottom);
        System.out.println("Player " + player.getId() + " drew a card.");
      }
    } else {
      player.addGold(2);
      System.out.println("Player " + player.getId() + " received 2 gold.");
    }
  }

  public static CharacterCard chooseAssassinTarget(List<Player> players) {
    // Target a player close to winning (7+ districts)
    List<Player> targets = players.stream()
        .filter(p -> p.getCity().size() >= 7)
        .collect(Collectors.toList());
    if (!targets.isEmpty()) {
      Player target = targets.get(rand.nextInt(targets.size()));
      return target.getCharacter() != null ? target.getCharacter() : randomCharacter(CharacterType.ASSASSIN);
    }
    // Otherwise, random character (excluding Assassin)
    return randomCharacter(CharacterType.ASSASSIN);
  }

  public static CharacterCard chooseThiefTarget(List<Player> players, CharacterCard killedCharacter) {
    // Target a player with gold
    List<Player> targets = players.stream()
        .filter(p -> p.getGold() > 2 && p.getCharacter() != null && p.getCharacter() != killedCharacter)
        .collect(Collectors.toList());
    if (!targets.isEmpty()) {
      return targets.get(rand.nextInt(targets.size())).getCharacter();
    }
    // Random character (excluding Assassin, Thief, killed)
    List<CharacterType> invalid = new ArrayList<>(Arrays.asList(CharacterType.ASSASSIN, CharacterType.THIEF));
    if (killedCharacter != null)
      invalid.add(killedCharacter.getType());
    return randomCharacter(invalid);
  }

  public static void performMagicianAction(Player player, List<Player> players, Deck<DistrictCard> districtDeck) {
    // Swap hands if another player has more cards
    List<Player> others = players.stream().filter(p -> p != player).collect(Collectors.toList());
    Optional<Player> swapTarget = others.stream()
        .filter(p -> p.getHand().size() > player.getHand().size())
        .findFirst();
    if (swapTarget.isPresent()) {
      Player target = swapTarget.get();
      List<DistrictCard> temp = new ArrayList<>(player.getHand());
      player.getHand().clear();
      player.getHand().addAll(target.getHand());
      target.getHand().clear();
      target.getHand().addAll(temp);
      System.out.println("Player " + player.getId() + " swapped hands with Player " + target.getId());
    } else if (!player.getHand().isEmpty() && !districtDeck.isEmpty()) {
      // Discard all cards and redraw
      List<DistrictCard> toDiscard = new ArrayList<>(player.getHand());
      player.getHand().clear();
      toDiscard.forEach(districtDeck::addCardToBottom);
      for (int i = 0; i < toDiscard.size() && !districtDeck.isEmpty(); i++) {
        player.addCardToHand(districtDeck.drawCard());
      }
      System.out.println("Player " + player.getId() + " discarded " + toDiscard.size() + " cards and drew new ones.");
    }
  }

  public static void performWarlordAction(Player player, List<Player> players) {
    // Destroy a district from a player close to winning
    List<Player> targets = players.stream()
        .filter(p -> p != player && p.getCity().size() >= 6 && p.getCity().size() < 8
            && p.getCharacter().getType() != CharacterType.BISHOP)
        .collect(Collectors.toList());
    if (targets.isEmpty())
      return;
    Player target = targets.get(rand.nextInt(targets.size()));
    List<DistrictCard> affordable = target.getCity().stream()
        .filter(d -> d.getCost() - 1 <= player.getGold())
        .collect(Collectors.toList());
    if (affordable.isEmpty())
      return;
    DistrictCard toDestroy = affordable.get(rand.nextInt(affordable.size()));
    player.removeGold(toDestroy.getCost() - 1);
    target.removeDistrictFromCity(toDestroy);
    System.out
        .println("Player " + player.getId() + " destroyed " + toDestroy.getName() + " from Player " + target.getId());
  }

  public static boolean buildDistrict(Player player) {
    // Build the most expensive district affordable
    List<DistrictCard> affordable = player.getHand().stream()
        .filter(d -> d.getCost() <= player.getGold()
            && !player.getCity().stream().anyMatch(c -> c.getName().equals(d.getName())))
        .sorted(Comparator.comparingInt(DistrictCard::getCost).reversed())
        .collect(Collectors.toList());
    if (affordable.isEmpty())
      return false;
    DistrictCard toBuild = affordable.get(0);
    player.removeGold(toBuild.getCost());
    player.removeCardFromHand(toBuild);
    player.addDistrictToCity(toBuild);
    System.out.println("Player " + player.getId() + " built " + toBuild);
    return true;
  }

  private static CharacterCard randomCharacter(CharacterType exclude) {
    List<CharacterType> valid = Arrays.stream(CharacterType.values())
        .filter(t -> t != exclude)
        .collect(Collectors.toList());
    return new CharacterCard(valid.get(rand.nextInt(valid.size())));
  }

  private static CharacterCard randomCharacter(List<CharacterType> exclude) {
    List<CharacterType> valid = Arrays.stream(CharacterType.values())
        .filter(t -> !exclude.contains(t))
        .collect(Collectors.toList());
    return new CharacterCard(valid.get(rand.nextInt(valid.size())));
  }
}