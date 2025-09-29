package citadels.Utils;

public enum DistrictColor {
    YELLOW,
    BLUE,
    GREEN,
    RED,
    PURPLE;

    public static DistrictColor fromString(String color) {
        switch (color) {
            case "yellow": return YELLOW;
            case "blue": return BLUE;
            case "green": return GREEN;
            case "red": return RED;
            case "purple": return PURPLE;
            default: throw new IllegalArgumentException("Invalid district color: " + color);
        }
    }
}
