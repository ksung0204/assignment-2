package citadels.Cards.Characters;

import citadels.Cards.CharacterCard;
import citadels.Cards.DistrictCard;
import citadels.Game.Game;
import citadels.Player.Player;
import citadels.Utils.DistrictColor;

import java.util.List;

public class Warlord extends CharacterCard {
    public Warlord() {
        super("Warlord", 8,
                "Gains one gold for each red (military) district in their city. You can destroy one district of your choice by paying one fewer gold than its building cost. You cannot destroy a district in a city with 8 or more districts.",
                "To perform this action, type 'action destroy <player number> <district index>'");
    }

    @Override
    public void performSpecialAbility(Game game, Player currentPlayer, String commandLine) {
        if (commandLine.startsWith("action destroy")) {
            try {
                String[] parts = commandLine.substring("action destroy".length()).trim().split("\\s+");
                if (parts.length != 2) {
                    System.out.println("Invalid format. Use 'action destroy <player number> <district index>'");
                    return;
                }

                int targetPlayerIndex = Integer.parseInt(parts[0]) - 1;
                int districtIndex = Integer.parseInt(parts[1]) - 1;

                List<Player> players = game.getPlayers();
                if (targetPlayerIndex < 0 || targetPlayerIndex >= players.size()) {
                    System.out
                            .println("Invalid player number. Please choose a valid player (1-" + players.size() + ").");
                    return;
                }

                Player targetPlayer = players.get(targetPlayerIndex);
                if (targetPlayer == currentPlayer) {
                    System.out.println("You cannot destroy your own districts.");
                    return;
                }

                // Check if target player has 8 or more districts
                if (targetPlayer.getCity().size() >= 8) {
                    System.out.println("Cannot destroy districts in a city with 8 or more districts.");
                    return;
                }

                // Check if target player has the Bishop character
                if (targetPlayer.getCurrentCharacterCard() instanceof Bishop) {
                    System.out.println("Cannot destroy districts of the Bishop.");
                    return;
                }

                if (districtIndex < 0 || districtIndex >= targetPlayer.getCity().size()) {
                    System.out.println("Invalid district index. Please choose a valid district (1-"
                            + targetPlayer.getCity().size() + ").");
                    return;
                }

                DistrictCard targetDistrict = targetPlayer.getCity().get(districtIndex);
                int cost = targetDistrict.getCost();
                int destroyCost = cost - 1;

                if (currentPlayer.getGold() < destroyCost) {
                    System.out.println("You don't have enough gold to destroy this district. Cost: " + destroyCost);
                    return;
                }

                // Remove the district and deduct gold
                targetPlayer.removeDistrictCardInCity(targetDistrict);
                currentPlayer.removeGold(destroyCost);
                System.out.println("You destroyed " + targetDistrict.getName() + " for " + destroyCost + " gold.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter valid numbers for player and district indices.");
            }
        } else {
            System.out.println("Invalid action for Warlord. Use 'action destroy <player number> <district index>'");
        }
    }

    @Override
    public void collectDistrictIncome(Game game, Player currentPlayer) {
        int redCount = 0;

        // Count red districts
        for (DistrictCard card : currentPlayer.getCity()) {
            if (card.getColor() == DistrictColor.RED) {
                redCount++;
            }
        }

        // Check for School of Magic (counts as any color for income)
        boolean hasSchoolOfMagic = currentPlayer.getCity().stream()
                .anyMatch(card -> card.getName().equals("School Of Magic"));

        if (hasSchoolOfMagic) {
            redCount++;
            System.out.println("School Of Magic counts as a red district for income.");
        }

        if (redCount > 0) {
            currentPlayer.addGold(redCount);
            System.out.println(
                    currentPlayer.getName() + " received " + redCount + " gold from red districts.");
        }
    }
}