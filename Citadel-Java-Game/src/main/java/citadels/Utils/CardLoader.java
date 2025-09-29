package citadels.Utils;

import citadels.Cards.DistrictCard;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;

public class CardLoader {
    public static List<DistrictCard> loadDistrictCards() {
        String fileName = "/citadels/cards.tsv"; // Đường dẫn trong resources
        List<DistrictCard> districtCards = new ArrayList<>();
        try (InputStream is = CardLoader.class.getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            String line;
            reader.readLine(); // Bỏ qua dòng header nếu có (Name	Color	Cost	Quantity	Ability)

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 4) { // Cần ít nhất Name, Quantity, Color, Cost, Text
                    String name = parts[0].trim();
                    int quantity = Integer.parseInt(parts[1].trim());
                    DistrictColor color = DistrictColor.fromString(parts[2].trim());
                    int cost = Integer.parseInt(parts[3].trim());
                    String ability = (parts.length > 4 && color == DistrictColor.PURPLE) ? parts[4].trim() : null;

                    if (color == null) {
                        System.err.println("Warning: Unknown color '" + parts[1].trim() + "' for card '" + name + "'. Skipping card.");
                        continue;
                    }

                    for (int i = 0; i < quantity; i++) {
                        districtCards.add(new DistrictCard(name, color, cost, ability));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading district cards from " + fileName + ": " + e.getMessage());
            e.printStackTrace();
        }
        
        if (districtCards.isEmpty()) {
            System.err.println("Cannot load any district cards! Check the cards.tsv file and the path.");
        } else {
            System.out.println("Successfully loaded " + districtCards.size() + " district cards.");
        }
        return districtCards;
    }
}
