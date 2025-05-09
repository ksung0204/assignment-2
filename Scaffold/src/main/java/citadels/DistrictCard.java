package citadels;

public class DistrictCard extends Card {
    private String name;
    private Color color;
    private int cost;
    private String text;
    private String specialAbility;
    public DistrictCard(String name, Color color, int cost, String text){
        this.name=name;
        this.color=color;
        this.cost=cost;
        this.text=text;
        this.specialAbility=specialAbility;
    }
    public String getName(){
        return name;
    }
    public Color getColor(){
        return color;
    }
    public int getCost(){
        return cost;
    }
    public String getSpecialAbility(){
        return specialAbility;
    }
    public String toString(){
        return name+color.name()+cost;
    }
}
