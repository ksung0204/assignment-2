package citadels.Cards;

import citadels.Utils.DistrictColor;

public class DistrictCard extends Card  {
    private DistrictColor color;
    private int cost;
    private String specialAbility;

    public DistrictCard(String name, DistrictColor color, int cost, String specialAbility) {
        this.name = name;
        this.color = color;
        this.cost = cost;
        this.specialAbility = specialAbility;
    }

    public DistrictColor getColor() {
        return color;
    }

    public int getCost() {
        return cost;
    }

    public String getSpecialAbility() {
        return specialAbility;
    }


    @Override
    public String toString() {
        return String.format("%s (%s), cost: %d", name, color.name().toLowerCase(), cost);
    }

    public String getDisplayString() {
        return String.format("%s [%s%d]", name, color.name().toLowerCase(), cost);
    }
}
