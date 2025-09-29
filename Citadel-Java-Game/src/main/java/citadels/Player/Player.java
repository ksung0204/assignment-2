package citadels.Player;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import citadels.Cards.DistrictCard;
import citadels.Cards.CharacterCard;
import citadels.Utils.DistrictColor;

public abstract class Player {
    protected String name;
    protected int gold;
    protected List<DistrictCard> hand;
    protected List<DistrictCard> city;
    protected CharacterCard currentCharacterCard;
    protected int score;
    protected boolean firstToCompleteCity;

    public Player(String name) {
        this.name = name;
        this.gold = 2;
        this.hand = new ArrayList<>();
        this.city = new ArrayList<>();
        this.currentCharacterCard = null;
        this.score = 0;
        this.firstToCompleteCity = false;
    }

    public void addGold(int amount) {
        this.gold += amount;
    }

    public int getGold() {
        return this.gold;
    }

    public boolean removeGold(int amount) {
        if (this.gold >= amount) {
            this.gold -= amount;
            return true;
        }
        return false;
    }

    public void addDistrictCardToHand(DistrictCard districtCard) {
        this.hand.add(districtCard);
    }

    public void setHand(List<DistrictCard> hand) {
        this.hand = hand;
    }

    public void removeDistrictCardInHand(int index) {
        this.hand.remove(index);
    }

    public List<DistrictCard> getHand() {
        return this.hand;
    }

    public List<DistrictCard> getCity() {
        return this.city;
    }

    public boolean canBuildDistrict(DistrictCard card) {
        // Check if already has this district
        for (DistrictCard builtDistrict : city) {
            if (builtDistrict.getName().equals(card.getName())) {
                // Check for Quarry special ability - can build duplicate districts
                boolean hasQuarry = city.stream()
                        .anyMatch(d -> d.getName().equals("Quarry"));

                // Check if already has a duplicate
                boolean alreadyHasDuplicate = city.stream()
                        .filter(d -> d.getName().equals(card.getName()))
                        .count() > 1;

                if (!hasQuarry || alreadyHasDuplicate) {
                    return false;
                }
            }
        }

        // Calculate effective cost
        int effectiveCost = card.getCost();

        // Check for Factory special ability - reduces cost of purple districts by 1
        if (card.getColor() == DistrictColor.PURPLE) {
            boolean hasFactory = city.stream()
                    .anyMatch(d -> d.getName().equals("Factory"));

            if (hasFactory) {
                effectiveCost = Math.max(0, effectiveCost - 1);
            }
        }

        return gold >= effectiveCost;
    }

    public void buildDistrict(DistrictCard card) {
        if (canBuildDistrict(card)) {
            // Calculate effective cost
            int effectiveCost = card.getCost();

            // Check for Factory special ability - reduces cost of purple districts by 1
            if (card.getColor() == DistrictColor.PURPLE) {
                boolean hasFactory = city.stream()
                        .anyMatch(d -> d.getName().equals("Factory"));

                if (hasFactory) {
                    effectiveCost = Math.max(0, effectiveCost - 1);
                    System.out.println("Factory reduces the cost by 1 gold.");
                }
            }

            removeGold(effectiveCost);
            city.add(card);

            // Remove the card from the player's hand
            for (int i = 0; i < hand.size(); i++) {
                if (hand.get(i) == card) {
                    hand.remove(i);
                    break;
                }
            }

            System.out.println(name + " built " + card.getDisplayString());
        }
    }

    public CharacterCard getCurrentCharacterCard() {
        return this.currentCharacterCard;
    }

    public void setCurrentCharacterCard(CharacterCard card) {
        this.currentCharacterCard = card;
    }

    public void removeDistrictCardInCity(DistrictCard card) {
        this.city.remove(card);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public String getName() {
        return name;
    }

    public String getCityToString() {
        // city=Docks [green3], Market [green2], Cathedral [purple5]
        String cityInfo = city.isEmpty() ? ""
                : String.join(", ", city.stream().map(DistrictCard::getDisplayString).collect(Collectors.toList()));
        return "city=" + cityInfo;
    }

    public int getHandSize() {
        return hand.size();
    }

    public boolean isFirstToCompleteCity() {
        return firstToCompleteCity;
    }

    public void setFirstToCompleteCity(boolean firstToCompleteCity) {
        this.firstToCompleteCity = firstToCompleteCity;
    }
}
