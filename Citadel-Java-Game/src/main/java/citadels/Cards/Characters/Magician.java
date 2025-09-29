package citadels.Cards.Characters;

import citadels.Cards.CharacterCard;
import citadels.Cards.DistrictCard;
import citadels.Game.Game;
import citadels.Player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Magician extends CharacterCard {
    private boolean abilityUsedThisTurn;

    public Magician() {
        super("Magician", 3,
                "Can either exchange their hand with another player's, or discard any number of district cards face down to the bottom of the deck and draw an equal number of cards from the district deck (can only do this once per turn).",
                "To perform this action, type 'action swap <player number>' to swap hands with that player or type 'action redraw <id1,id2,...>' to discard the given number of district cards from your hand and redraw them from the deck, where id1, id2 etc are the positions in your hand when doing the hand command.");
        this.abilityUsedThisTurn = false;
    }

    @Override
    public void performSpecialAbility(Game game, Player currentPlayer, String commandLine) {
        if (abilityUsedThisTurn) {
            System.out.println("You have already used your special ability this turn.");
            return;
        }

        if (commandLine.startsWith("action swap")) {
            try {
                int targetPlayerIndex = Integer.parseInt(commandLine.substring("action swap".length()).trim()) - 1;
                List<Player> players = game.getPlayers();

                if (targetPlayerIndex < 0 || targetPlayerIndex >= players.size()) {
                    System.out
                            .println("Invalid player number. Please choose a valid player (1-" + players.size() + ").");
                    return;
                }

                Player targetPlayer = players.get(targetPlayerIndex);
                if (targetPlayer == currentPlayer) {
                    System.out.println("You cannot swap with yourself.");
                    return;
                }

                // Swap hands
                List<DistrictCard> currentPlayerHand = new ArrayList<>(currentPlayer.getHand());
                List<DistrictCard> targetPlayerHand = new ArrayList<>(targetPlayer.getHand());

                currentPlayer.setHand(targetPlayerHand);
                targetPlayer.setHand(currentPlayerHand);

                System.out.println("You swapped hands with " + targetPlayer.getName() + ".");
                abilityUsedThisTurn = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid player number. Please enter a valid number.");
            }
        } else if (commandLine.startsWith("action redraw")) {
            try {
                String[] cardIndicesStr = commandLine.substring("action redraw".length()).trim().split(",");
                List<Integer> cardIndices = Arrays.stream(cardIndicesStr)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(Integer::parseInt)
                        .map(i -> i - 1) // Convert to 0-indexed
                        .sorted((a, b) -> b - a) // Sort in descending order to remove from end first
                        .collect(Collectors.toList());

                if (cardIndices.isEmpty()) {
                    System.out.println("No cards selected for redraw.");
                    return;
                }

                List<DistrictCard> hand = currentPlayer.getHand();

                // Check if all indices are valid
                for (int index : cardIndices) {
                    if (index < 0 || index >= hand.size()) {
                        System.out.println(
                                "Invalid card index: " + (index + 1) + ". Please choose valid card positions.");
                        return;
                    }
                }

                // Remove cards from hand and add to bottom of deck
                for (int index : cardIndices) {
                    DistrictCard card = hand.get(index);
                    game.getDistrictDeck().addCardToBottom(card);
                    currentPlayer.removeDistrictCardInHand(index);
                }

                // Draw new cards
                for (int i = 0; i < cardIndices.size(); i++) {
                    DistrictCard newCard = game.getDistrictDeck().drawCard();
                    if (newCard != null) {
                        currentPlayer.addDistrictCardToHand(newCard);
                    }
                }

                System.out.println(
                        "You discarded " + cardIndices.size() + " cards and drew " + cardIndices.size() + " new ones.");
                abilityUsedThisTurn = true;
            } catch (NumberFormatException e) {
                System.out.println("Invalid card indices. Please enter valid numbers separated by commas.");
            }
        } else {
            System.out.println(
                    "Invalid action for Magician. Use 'action swap <player number>' or 'action redraw <id1,id2,...>'");
        }
    }

    @Override
    public void resetTurnState() {
        abilityUsedThisTurn = false;
    }
}