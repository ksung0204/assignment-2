package citadels.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import citadels.Cards.CharacterCard;
import citadels.Cards.DistrictCard;
import citadels.Cards.Characters.Architect;
import citadels.Cards.Characters.Assassin;
import citadels.Cards.Characters.Bishop;
import citadels.Cards.Characters.King;
import citadels.Cards.Characters.Magician;
import citadels.Cards.Characters.Merchant;
import citadels.Cards.Characters.Thief;
import citadels.Cards.Characters.Warlord;
import citadels.Player.ComputerPlayer;
import citadels.Player.HumanPlayer;
import citadels.Player.Player;
import citadels.Utils.CardLoader;
import citadels.Utils.Deck;
import citadels.Utils.DistrictColor;

public class Game {
    private List<Player> players;
    private Deck<DistrictCard> districtDeck;
    private List<CharacterCard> allCharacterCards;
    private CharacterCard mysteryCardFaceDown;
    private Player crownedPlayer;
    private int currentPlayerIndex;
    private Scanner scanner;
    private int killCharacterRank = -1;
    private int robbedCharacterRank = -1;
    private Player thiefPlayer = null;
    private boolean gameEnded = false;
    private Player firstPlayerToCompleteCity = null;
    private boolean debugMode = false;
    private Map<String, Boolean> purpleAbilitiesUsedThisTurn;

    public Game(Scanner scanner) {
        this.scanner = scanner;
        this.players = new ArrayList<>();
        this.districtDeck = new Deck<DistrictCard>(CardLoader.loadDistrictCards());
        this.purpleAbilitiesUsedThisTurn = new HashMap<>();
    }

    private List<CharacterCard> initializeCharacterCards() {
        List<CharacterCard> characters = new ArrayList<>();
        characters.add(new Assassin());
        characters.add(new Thief());
        characters.add(new Magician());
        characters.add(new King());
        characters.add(new Bishop());
        characters.add(new Merchant());
        characters.add(new Architect());
        characters.add(new Warlord());
        return characters;
    }

    public void setUpGame() {
        System.out.print("Enter how many players [4-7]: ");
        int numPlayers = 0;
        while (numPlayers < 4 || numPlayers > 7) {
            try {
                numPlayers = Integer.parseInt(scanner.nextLine());
                if (numPlayers < 4 || numPlayers > 7) {
                    System.out.print("Invalid number of players. Please enter a number between 4 and 7: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }

        players.add(new HumanPlayer("Player 1"));
        for (int i = 1; i < numPlayers; i++) {
            players.add(new ComputerPlayer("Player " + (i + 1)));
        }

        System.out.println("Shuffling deck...");
        districtDeck.shuffle();
        System.out.println("Adding characters...");
        System.out.println("Dealing cards...");
        for (Player player : players) {
            for (int i = 0; i < 4; i++) {
                player.addDistrictCardToHand(districtDeck.drawCard());
            }
            // the 2 golds are already added in the Player constructor
        }
        crownedPlayer = players.get((int) (Math.random() * players.size()));
        System.out.println("Starting Citadels with " + numPlayers + " players...");
        System.out.println("You are Player 1");
    }

    public void play() {
        setUpGame();
        while (!gameEnded) {
            resetRoundState();

            System.out.println("================");
            System.out.println("NEW ROUND");
            System.out.println("================");

            characterSelectionPhase();
            if (gameEnded)
                break;
            turnPhase();
            checkGameEndCondition();
        }
        calculateAndAnnounceWinner();
    }

    // Resets round-specific state
    private void resetRoundState() {
        allCharacterCards = new ArrayList<>(initializeCharacterCards());
        killCharacterRank = -1;
        robbedCharacterRank = -1;
        thiefPlayer = null;
        purpleAbilitiesUsedThisTurn.clear();
    }

    private List<Player> getSelectionOrder() {
        List<Player> selectionOrder = new ArrayList<>();
        int crownedPlayerIdx = players.indexOf(crownedPlayer);
        if (crownedPlayerIdx == -1) {
            crownedPlayer = players.get(0);
            crownedPlayerIdx = 0;
            System.err.println("Error: Crowned player not found, defaulting to Player 1.");
        }
        for (int i = 0; i < players.size(); i++) {
            selectionOrder.add(players.get((crownedPlayerIdx + i) % players.size()));
        }
        return selectionOrder;
    }

    private List<CharacterCard> performInitialCardDiscardsAndGetSelectionPool() {
        List<CharacterCard> selectionPool;

        while (true) {
            selectionPool = new ArrayList<>(allCharacterCards);
            Collections.shuffle(selectionPool);

            if (selectionPool.isEmpty()) {
                System.err.println("Error: No character cards to select from after shuffle.");
                return null;
            }
            this.mysteryCardFaceDown = selectionPool.remove(0);

            int numPlayers = players.size();
            int faceUpToReveal = 0;
            if (numPlayers == 4)
                faceUpToReveal = 2;
            else if (numPlayers == 5)
                faceUpToReveal = 1;

            List<CharacterCard> revealedFaceUpCards = new ArrayList<>();
            boolean kingRevealed = false;

            List<CharacterCard> tempPoolForReveal = new ArrayList<>(selectionPool);

            for (int i = 0; i < faceUpToReveal; i++) {
                if (tempPoolForReveal.isEmpty())
                    break;
                CharacterCard cardToReveal = tempPoolForReveal.get(i);

                if (cardToReveal instanceof King) {
                    System.out.println("A mystery character was removed.");
                    System.out.println(cardToReveal.getName() + " was removed.");
                    System.out.println("The King cannot be visibly removed, trying again...");
                    kingRevealed = true;
                    break;
                }
                revealedFaceUpCards.add(cardToReveal);
            }

            if (!kingRevealed) {
                System.out.println("A mystery character was removed (face down).");
                for (CharacterCard revealedCard : revealedFaceUpCards) {
                    System.out.println(revealedCard.getName() + " was removed (face up).");
                    selectionPool.remove(revealedCard);
                }
                return selectionPool;
            }
        }
    }

    // Handles character selection for all players
    private void characterSelectionPhase() {
        System.out.println(crownedPlayer.getName() + " has the crown and will choose first.");
        System.out.println("Press 't' to process turn");
        if (!waitForTOrUniversalCommand("Press 't' to process turn",
                "It is not your turn. Press t to continue with other player turns.")) {
            return;
        }
        System.out.println("================");
        System.out.println("SELECTION PHASE");
        System.out.println("================");
        List<CharacterCard> availableForSelection = performInitialCardDiscardsAndGetSelectionPool();
        if (availableForSelection == null) {
            System.err.println("Failed to prepare characters for selection. Ending game.");
            return;
        }

        List<Player> selectionOrder = getSelectionOrder();

        for (int i = 0; i < selectionOrder.size(); i++) {
            Player currentPlayerSelecting = selectionOrder.get(i);
            boolean isLastPlayer = (i == selectionOrder.size() - 1);
            boolean is7PlayerGameAndLastChooser = (players.size() == 7 && isLastPlayer);

            if (availableForSelection.isEmpty() && !is7PlayerGameAndLastChooser) {
                System.out.println("No more characters to choose from for " + currentPlayerSelecting.getName()
                        + ". This shouldn't happen before the last player in a 7-player game.");
                break;
            }

            System.out.println("\nIt's " + currentPlayerSelecting.getName() + "'s turn to choose a character.");

            if (currentPlayerSelecting instanceof HumanPlayer) {
                promptHumanCharacterChoice(currentPlayerSelecting, availableForSelection,
                        is7PlayerGameAndLastChooser ? mysteryCardFaceDown : null);
            } else {
                aiChooseCharacter((ComputerPlayer) currentPlayerSelecting, availableForSelection,
                        is7PlayerGameAndLastChooser ? mysteryCardFaceDown : null);
                System.out.println(currentPlayerSelecting.getName() + " chose a character.");

                if (!isLastPlayer) {
                    System.out.println("Press 't' to continue with other player turns.");
                    if (!waitForTOrUniversalCommand("Press 't' to continue with other player turns.",
                            "It is not your turn. Press 't' to continue with other player turns."))
                        return;
                }
            }
        }
        System.out.println("Character choosing is over, action round will now begin.");
        return;
    }

    private void promptHumanCharacterChoice(Player humanPlayer, List<CharacterCard> availableChars,
            CharacterCard mysteryCardOptionFor7P) {
        List<CharacterCard> choices = new ArrayList<>(availableChars);
        if (mysteryCardOptionFor7P != null) {
            System.out.println("You also consider the mystery card: " + mysteryCardOptionFor7P.getName());
            choices.add(mysteryCardOptionFor7P);
        }

        if (choices.isEmpty()) {
            System.out.println("No characters available for you to choose.");
            if (mysteryCardOptionFor7P != null) {
                System.out.println("You automatically take the mystery card: " + mysteryCardOptionFor7P.getName());
                humanPlayer.setCurrentCharacterCard(mysteryCardOptionFor7P);
                if (availableChars.size() == 1 && mysteryCardOptionFor7P != null) {
                }
                return;
            }
            return;
        }

        System.out.println("Available characters to choose:");
        for (int j = 0; j < choices.size(); j++) {
            System.out.println((j + 1) + ". " + choices.get(j).getName());
        }
        System.out.print("Choose your character (enter number): ");
        int choiceNum = -1;
        CharacterCard chosenCard = null;

        while (chosenCard == null) {
            try {
                String line = scanner.nextLine();
                if (line.isEmpty())
                    continue;
                choiceNum = Integer.parseInt(line) - 1;
                if (choiceNum >= 0 && choiceNum < choices.size()) {
                    chosenCard = choices.get(choiceNum);
                } else {
                    System.out.print("Invalid choice. Please enter a valid number: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a number: ");
            }
        }

        humanPlayer.setCurrentCharacterCard(chosenCard);
        System.out.println(humanPlayer.getName() + " chose " + chosenCard.getName() + ".");

        if (mysteryCardOptionFor7P != null) {
            if (chosenCard == mysteryCardOptionFor7P) {
                if (!availableChars.isEmpty()) {
                    System.out.println("You discarded " + availableChars.get(0).getName() + " (face down).");
                }
            } else {
                System.out.println(
                        "You discarded the mystery card (" + mysteryCardOptionFor7P.getName() + ") (face down).");
                availableChars.remove(chosenCard);
            }
        } else {
            availableChars.remove(chosenCard);
        }
    }

    private boolean is7PlayerGameAndAiIsLastChooser(Player player, boolean mysteryCardAvailable) {
        if (players.size() != 7 || player instanceof HumanPlayer || !mysteryCardAvailable)
            return false;
        List<Player> selOrder = getSelectionOrder();
        return !selOrder.isEmpty() && selOrder.get(selOrder.size() - 1) == player;
    }

    private void aiChooseCharacter(ComputerPlayer aiPlayer, List<CharacterCard> availableChars,
            CharacterCard mysteryCardOptionFor7P) {
        CharacterCard chosenCard;
        if (is7PlayerGameAndAiIsLastChooser(aiPlayer, mysteryCardOptionFor7P != null)) {
            CharacterCard lastAvailable = availableChars.isEmpty() ? null : availableChars.get(0);
            if (lastAvailable != null && mysteryCardOptionFor7P != null) {
                if (Math.random() < 0.5) {
                    chosenCard = lastAvailable;
                    System.out.println(aiPlayer.getName() + " discarded the mystery card ("
                            + mysteryCardOptionFor7P.getName() + ") face down.");
                } else {
                    chosenCard = mysteryCardOptionFor7P;
                    System.out.println(aiPlayer.getName() + " discarded "
                            + (lastAvailable != null ? lastAvailable.getName() : "the other card") + " face down.");
                }
            } else if (lastAvailable != null) {
                chosenCard = lastAvailable;
            } else {
                chosenCard = mysteryCardOptionFor7P;
            }
        } else {
            chosenCard = aiPlayer.chooseCharacterAI(new ArrayList<>(availableChars), this);
        }

        if (chosenCard != null) {
            aiPlayer.setCurrentCharacterCard(chosenCard);
            if (availableChars.contains(chosenCard)) {
                availableChars.remove(chosenCard);
            }
        } else if (!availableChars.isEmpty()) {
            chosenCard = availableChars.remove(0);
            aiPlayer.setCurrentCharacterCard(chosenCard);
        } else if (mysteryCardOptionFor7P != null && is7PlayerGameAndAiIsLastChooser(aiPlayer, true)) {
            aiPlayer.setCurrentCharacterCard(mysteryCardOptionFor7P);
        } else {
            System.err.println("Error: AI " + aiPlayer.getName()
                    + " could not choose a character. Pool empty and not 7th player last choice with mystery.");
        }
    }

    private List<Player> getTurnOrder() {
        return players.stream()
                .filter(p -> p.getCurrentCharacterCard() != null)
                .sorted(Comparator.comparingInt(p -> p.getCurrentCharacterCard().getRank()))
                .collect(Collectors.toList());
    }

    private void performAITurn(ComputerPlayer aiPlayer, CharacterCard character) {
        if (debugMode) {
            System.out.print("Debug: ");
            displayHand(aiPlayer);
        }
        takeGoldOrCardAction(aiPlayer);

        int maxBuildsAI = (character instanceof Architect) ? 3 : 1;
        int districtsBuiltThisTurnAI = 0;
        for (int i = 0; i < maxBuildsAI; i++) {
            DistrictCard chosenCardToBuildAI = aiPlayer.getBestDistrictCardToBuild();
            if (chosenCardToBuildAI != null) {
                DistrictCard cardInHandInstance = null;
                for (DistrictCard handCard : aiPlayer.getHand()) {
                    if (handCard.getName().equals(chosenCardToBuildAI.getName()) &&
                            handCard.getCost() == chosenCardToBuildAI.getCost() &&
                            aiPlayer.canBuildDistrict(handCard)) {
                        cardInHandInstance = handCard;
                        break;
                    }
                }

                if (cardInHandInstance != null) {
                    aiPlayer.buildDistrict(cardInHandInstance);
                    districtsBuiltThisTurnAI++;
                    if (aiPlayer.getCity().size() >= 8 && firstPlayerToCompleteCity == null) {
                        firstPlayerToCompleteCity = aiPlayer;
                        aiPlayer.setFirstToCompleteCity(true);
                        System.out.println(aiPlayer.getName() + " is the first to build 8 districts!");
                    }
                    if (checkGameEndConditionDuringTurn(aiPlayer))
                        break;
                } else {
                    break;
                }
            } else
                break;
        }

        if (character instanceof Assassin && getKilledCharacterRank() == -1) {
            List<CharacterCard> targets = getAllCharacterCards().stream()
                    .filter(c -> c.getRank() >= 2 && c.getRank() <= 8).collect(Collectors.toList());
            if (!targets.isEmpty()) {
                CharacterCard target = targets.get((int) (Math.random() * targets.size()));
                setKilledCharacterRank(target.getRank());
                System.out.println(aiPlayer.getName() + " killed " + target.getName());
            }
        } else if (character instanceof Thief && getRobbedCharacterRank() == -1) {
            List<CharacterCard> targets = getAllCharacterCards().stream()
                    .filter(c -> c.getRank() >= 3 && c.getRank() <= 8 && c.getRank() != getKilledCharacterRank())
                    .collect(Collectors.toList());
            if (!targets.isEmpty()) {
                CharacterCard target = targets.get((int) (Math.random() * targets.size()));
                setRobbedCharacterRank(target.getRank(), aiPlayer);
                System.out.println(aiPlayer.getName() + " robbed " + target.getName());
            }
        }
    }

    // Handles the turn phase for all characters in order
    private void turnPhase() {
        System.out.println("================");
        System.out.println("TURN PHASE");
        System.out.println("================");
        List<Player> turnOrder = getTurnOrder();

        for (int rankToCall = 1; rankToCall <= 8; rankToCall++) {
            final int currentRank = rankToCall;
            Player playerForTurn = turnOrder.stream()
                    .filter(p -> p.getCurrentCharacterCard() != null
                            && p.getCurrentCharacterCard().getRank() == currentRank)
                    .findFirst().orElse(null);

            CharacterCard charInfo = allCharacterCards.get(currentRank - 1);
            String charNameForDisplay = (charInfo != null) ? charInfo.getName() : "Character of Rank " + currentRank;

            System.out.println("\n" + currentRank + ": " + charNameForDisplay);

            if (playerForTurn == null) {
                System.out.println("No one is the " + charNameForDisplay + ".");
            } else {
                CharacterCard currentCharacter = playerForTurn.getCurrentCharacterCard();
                System.out.println(playerForTurn.getName() + " is the " + currentCharacter.getName() + ".");

                if (currentCharacter.getRank() == this.killCharacterRank) {
                    System.out.println(playerForTurn.getName() + " (" + currentCharacter.getName()
                            + ") was killed by the Assassin and loses their turn.");
                    if (currentCharacter instanceof King) {
                        crownedPlayer = playerForTurn;
                        System.out.println(playerForTurn.getName() + " still receives the crown token.");
                    }
                } else {
                    performAutomaticTurnStartActions(playerForTurn, currentCharacter);

                    if (playerForTurn instanceof HumanPlayer) {
                        System.out.println("Your turn.");
                        takeGoldOrCardAction(playerForTurn);
                        if (!processHumanPlayerCommands(playerForTurn, currentCharacter))
                            return;
                    } else {
                        System.out.println(playerForTurn.getName() + " is taking its turn...");
                        performAITurn((ComputerPlayer) playerForTurn, currentCharacter);
                        System.out.println(playerForTurn.getName() + " ended its turn.");
                    }
                    if (playerForTurn.getCity().size() >= 8 && firstPlayerToCompleteCity == null) {
                        firstPlayerToCompleteCity = playerForTurn;
                        playerForTurn.setFirstToCompleteCity(true);
                        System.out.println(playerForTurn.getName() + " is the first to build 8 districts!");
                    }
                    if (checkGameEndConditionDuringTurn(playerForTurn)) {
                    }
                }
            }

            // This block is now correctly placed AFTER a player's turn (human or AI) is
            // fully processed.
            if (rankToCall < 8) {
                System.out.println("Press 't' to call the next character.");
                if (!waitForTOrUniversalCommand("Press 't' to call the next character.",
                        "Press 't' to call the next character."))
                    return;
            }
            if (gameEnded)
                break;
        }
    }

    private void takeGoldOrCardAction(Player player) {
        if (player instanceof HumanPlayer) {
            System.out.println("Collect 2 gold or draw two cards and pick one [gold/cards]:");
            String choice = scanner.nextLine().trim().toLowerCase();
            if (choice.equals("gold")) {
                player.addGold(2);
                System.out.println(player.getName() + " received 2 gold.");
            } else if (choice.equals("cards")) {
                // Check if player has Observatory
                boolean hasObservatory = player.getCity().stream()
                        .anyMatch(card -> card.getName().equals("Observatory"));

                // Check if player has Library
                boolean hasLibrary = player.getCity().stream()
                        .anyMatch(card -> card.getName().equals("Library"));

                if (hasObservatory) {
                    // Draw 3 cards, keep 1
                    System.out.println("You have the Observatory. You can draw 3 cards and keep 1.");
                    DistrictCard card1 = districtDeck.drawCard();
                    DistrictCard card2 = districtDeck.drawCard();
                    DistrictCard card3 = districtDeck.drawCard();

                    if (card1 == null || card2 == null || card3 == null) {
                        System.out.println("Not enough cards in the deck.");
                        // Add any non-null cards to player's hand
                        if (card1 != null)
                            player.addDistrictCardToHand(card1);
                        if (card2 != null)
                            player.addDistrictCardToHand(card2);
                        if (card3 != null)
                            player.addDistrictCardToHand(card3);
                        return;
                    }

                    System.out.println("Choose a card to keep (1-3):");
                    System.out.println("1. " + card1.toString());
                    System.out.println("2. " + card2.toString());
                    System.out.println("3. " + card3.toString());

                    try {
                        int choice2 = Integer.parseInt(scanner.nextLine().trim());
                        if (choice2 == 1) {
                            player.addDistrictCardToHand(card1);
                            districtDeck.addCardToBottom(card2);
                            districtDeck.addCardToBottom(card3);
                            System.out.println("You kept " + card1.getDisplayString() + ". Others returned to deck.");
                        } else if (choice2 == 2) {
                            player.addDistrictCardToHand(card2);
                            districtDeck.addCardToBottom(card1);
                            districtDeck.addCardToBottom(card3);
                            System.out.println("You kept " + card2.getDisplayString() + ". Others returned to deck.");
                        } else if (choice2 == 3) {
                            player.addDistrictCardToHand(card3);
                            districtDeck.addCardToBottom(card1);
                            districtDeck.addCardToBottom(card2);
                            System.out.println("You kept " + card3.getDisplayString() + ". Others returned to deck.");
                        } else {
                            System.out.println("Invalid choice. Keeping the first card by default.");
                            player.addDistrictCardToHand(card1);
                            districtDeck.addCardToBottom(card2);
                            districtDeck.addCardToBottom(card3);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Keeping the first card by default.");
                        player.addDistrictCardToHand(card1);
                        districtDeck.addCardToBottom(card2);
                        districtDeck.addCardToBottom(card3);
                    }
                } else if (hasLibrary) {
                    // Keep both cards
                    System.out.println("You have the Library. You can keep both cards you draw.");
                    DistrictCard card1 = districtDeck.drawCard();
                    DistrictCard card2 = districtDeck.drawCard();

                    if (card1 != null) {
                        player.addDistrictCardToHand(card1);
                        System.out.println("You drew: " + card1.getDisplayString());
                    }

                    if (card2 != null) {
                        player.addDistrictCardToHand(card2);
                        System.out.println("You drew: " + card2.getDisplayString());
                    }

                    if (card1 == null && card2 == null) {
                        System.out.println("The district deck is empty.");
                    }
                } else {
                    // Regular draw 2, keep 1
                    DistrictCard card1 = districtDeck.drawCard();
                    DistrictCard card2 = districtDeck.drawCard();

                    if (card1 == null || card2 == null) {
                        System.out.println("Not enough cards in the deck.");
                        if (card1 != null)
                            player.addDistrictCardToHand(card1);
                        if (card2 != null)
                            player.addDistrictCardToHand(card2);
                        return;
                    }

                    System.out.println("Choose a card to keep (1-2):");
                    System.out.println("1. " + card1.toString());
                    System.out.println("2. " + card2.toString());

                    try {
                        int choice2 = Integer.parseInt(scanner.nextLine().trim());
                        if (choice2 == 1) {
                            player.addDistrictCardToHand(card1);
                            districtDeck.addCardToBottom(card2);
                            System.out.println("You kept " + card1.getDisplayString() + ". " + card2.getName()
                                    + " returned to deck.");
                        } else if (choice2 == 2) {
                            player.addDistrictCardToHand(card2);
                            districtDeck.addCardToBottom(card1);
                            System.out.println("You kept " + card2.getDisplayString() + ". " + card1.getName()
                                    + " returned to deck.");
                        } else {
                            System.out.println("Invalid choice. Keeping the first card by default.");
                            player.addDistrictCardToHand(card1);
                            districtDeck.addCardToBottom(card2);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Keeping the first card by default.");
                        player.addDistrictCardToHand(card1);
                        districtDeck.addCardToBottom(card2);
                    }
                }
            } else {
                System.out.println("Invalid choice. Please type 'gold' or 'cards'.");
                takeGoldOrCardAction(player);
            }
        } else {
            // AI logic for choosing gold or cards
            ComputerPlayer ai = (ComputerPlayer) player;
            String choice = ai.chooseActionAI();

            if (choice.equals("gold")) {
                player.addGold(2);
                System.out.println(player.getName() + " chose to take 2 gold.");
            } else {
                // Check if AI has Observatory or Library
                boolean hasObservatory = player.getCity().stream()
                        .anyMatch(card -> card.getName().equals("Observatory"));

                boolean hasLibrary = player.getCity().stream()
                        .anyMatch(card -> card.getName().equals("Library"));

                if (hasObservatory) {
                    // Draw 3 cards, keep the most expensive one
                    DistrictCard card1 = districtDeck.drawCard();
                    DistrictCard card2 = districtDeck.drawCard();
                    DistrictCard card3 = districtDeck.drawCard();

                    if (card1 == null || card2 == null || card3 == null) {
                        // Add any non-null cards to player's hand
                        if (card1 != null)
                            player.addDistrictCardToHand(card1);
                        if (card2 != null)
                            player.addDistrictCardToHand(card2);
                        if (card3 != null)
                            player.addDistrictCardToHand(card3);
                        System.out.println(player.getName() + " drew cards (deck running low).");
                        return;
                    }

                    // Find the most expensive card
                    DistrictCard mostExpensive = card1;
                    if (card2.getCost() > mostExpensive.getCost())
                        mostExpensive = card2;
                    if (card3.getCost() > mostExpensive.getCost())
                        mostExpensive = card3;

                    player.addDistrictCardToHand(mostExpensive);

                    // Return the other cards to the bottom of the deck
                    if (mostExpensive != card1)
                        districtDeck.addCardToBottom(card1);
                    if (mostExpensive != card2)
                        districtDeck.addCardToBottom(card2);
                    if (mostExpensive != card3)
                        districtDeck.addCardToBottom(card3);

                    System.out.println(player.getName() + " used Observatory to draw 3 cards and kept 1.");
                } else if (hasLibrary) {
                    // Keep both cards
                    DistrictCard card1 = districtDeck.drawCard();
                    DistrictCard card2 = districtDeck.drawCard();

                    if (card1 != null)
                        player.addDistrictCardToHand(card1);
                    if (card2 != null)
                        player.addDistrictCardToHand(card2);

                    System.out.println(player.getName() + " used Library to draw and keep 2 cards.");
                } else {
                    // Regular draw 2, keep 1
                    DistrictCard card1 = districtDeck.drawCard();
                    DistrictCard card2 = districtDeck.drawCard();

                    if (card1 == null || card2 == null) {
                        // Add any non-null cards to player's hand
                        if (card1 != null)
                            player.addDistrictCardToHand(card1);
                        if (card2 != null)
                            player.addDistrictCardToHand(card2);
                        System.out.println(player.getName() + " drew cards (deck running low).");
                        return;
                    }

                    // AI chooses the more expensive card
                    if (card1.getCost() >= card2.getCost()) {
                        player.addDistrictCardToHand(card1);
                        districtDeck.addCardToBottom(card2);
                    } else {
                        player.addDistrictCardToHand(card2);
                        districtDeck.addCardToBottom(card1);
                    }

                    System.out.println(player.getName() + " drew 2 cards and kept 1.");
                }
            }
        }
    }

    private void performAutomaticTurnStartActions(Player player, CharacterCard character) {
        // Reset character's turn state
        character.resetTurnState();

        if (character.getRank() == robbedCharacterRank && thiefPlayer != null && thiefPlayer != player) {
            int goldStolen = player.getGold();
            if (goldStolen > 0) {
                thiefPlayer.addGold(goldStolen);
                player.removeGold(goldStolen);
                System.out.println(thiefPlayer.getName() + " (Thief) steals " + goldStolen + " gold from "
                        + player.getName() + " (" + character.getName() + ").");
            } else {
                System.out.println(
                        player.getName() + " (" + character.getName() + ") has no gold for the Thief to steal.");
            }
        }
        if (character instanceof King) {
            crownedPlayer = player;
            System.out.println(player.getName() + " receives the crown token.");
        }
        if (character instanceof Merchant) {
            player.addGold(1);
            System.out.println(player.getName() + " received 1 extra gold.");
        }
        character.collectDistrictIncome(this, player);
        if (character instanceof Architect) {
            System.out.println(player.getName() + " draws 2 extra district cards.");
            DistrictCard extra1 = districtDeck.drawCard();
            DistrictCard extra2 = districtDeck.drawCard();
            if (extra1 != null)
                player.addDistrictCardToHand(extra1);
            else
                System.out.println("Deck empty, couldn't draw first extra card.");
            if (extra2 != null)
                player.addDistrictCardToHand(extra2);
            else
                System.out.println("Deck empty, couldn't draw second extra card.");
        }
    }

    private void displayHand(Player player) {
        System.out.println(player.getName() + " has " + player.getGold() + " gold. Cards in hand:");
        if (player.getHand().isEmpty()) {
        } else {
            for (int i = 0; i < player.getHand().size(); i++) {
                DistrictCard card = player.getHand().get(i);
                System.out.println("  " + (i + 1) + ". " + card.toString() +
                        (card.getColor() == DistrictColor.PURPLE && card.getSpecialAbility() != null
                                ? " - Ability: " + card.getSpecialAbility()
                                : ""));
            }
        }
    }

    private void displayCity(Player player) {
        System.out.println(player.getName() + " has built :");
        if (player.getCity().isEmpty()) {

        } else {
            for (DistrictCard card : player.getCity()) {
                System.out.println("  " + card.getName() + "(" + card.getColor().toString().toLowerCase() + "), "
                        + "points: " + card.getCost());
            }
        }
    }

    private void displayAllPlayersInfo() {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p instanceof HumanPlayer) {
                System.out.print("Player 1 (you): " + "cards=" + p.getHandSize() + " gold=" + p.getGold()
                        + " " + p.getCityToString());
            } else {
                System.out.print("Player " + (i + 1) + ": " + "cards=" + p.getHandSize() + " gold=" + p.getGold()
                        + " " + p.getCityToString());
            }
            System.out.println();
        }
    }

    private boolean checkGameEndConditionDuringTurn(Player currentPlayer) {
        if (currentPlayer.getCity().size() >= 8) {
            System.out.println(
                    currentPlayer.getName() + " has completed their city! The game will end after this round.");
            return true;
        }
        return false;
    }

    private boolean processHumanPlayerCommands(Player humanPlayer, CharacterCard character) {
        boolean turnEndedByPlayer = false;
        int districtsBuiltThisTurn = 0;
        int maxBuilds = (character instanceof Architect) ? 3 : 1;

        while (!turnEndedByPlayer) {

            System.out.print("> ");
            if (!scanner.hasNextLine())
                return false;
            String commandLine = scanner.nextLine().trim();
            if (commandLine.isEmpty())
                continue;
            String[] parts = commandLine.split("\\s+");
            String command = parts[0].toLowerCase();

            switch (command) {
                case "help":
                    displayHelp();
                    break;
                case "hand":
                    displayHand(humanPlayer);
                    break;
                case "gold":
                    System.out.println("You have " + humanPlayer.getGold() + " gold.");
                    break;
                case "citadel":
                case "list":
                case "city":
                    Player target = humanPlayer;
                    if (parts.length > 1) {
                        try {
                            int pNum = Integer.parseInt(parts[1]);
                            if (pNum > 0 && pNum <= players.size())
                                target = players.get(pNum - 1);
                            else
                                System.out.println("Invalid player number.");
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid player number format.");
                        }
                    }
                    displayCity(target);
                    break;
                case "build":
                    if (districtsBuiltThisTurn < maxBuilds) {
                        if (parts.length > 1) {
                            try {
                                int cardIdx = Integer.parseInt(parts[1]) - 1;
                                if (cardIdx >= 0 && cardIdx < humanPlayer.getHand().size()) {
                                    DistrictCard cardToBuild = humanPlayer.getHand().get(cardIdx);
                                    if (humanPlayer.canBuildDistrict(cardToBuild)) {
                                        humanPlayer.buildDistrict(cardToBuild);
                                        districtsBuiltThisTurn++;
                                        if (humanPlayer.getCity().size() >= 8 && firstPlayerToCompleteCity == null) {
                                            firstPlayerToCompleteCity = humanPlayer;
                                            humanPlayer.setFirstToCompleteCity(true);
                                            System.out.println(
                                                    humanPlayer.getName() + " is the first to build 8 districts!");
                                        }
                                        if (checkGameEndConditionDuringTurn(humanPlayer)) {
                                        }

                                    } else {
                                        if (humanPlayer.getGold() < cardToBuild.getCost())
                                            System.out.println(
                                                    "You cannot afford to build this. Cost: " + cardToBuild.getCost());
                                        else
                                            System.out.println(
                                                    "You cannot build this district (already in city or other reason).");
                                    }
                                } else
                                    System.out.println("Invalid card number in hand.");
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid format. Use 'build <number>'.");
                            }
                        } else
                            System.out.println("Usage: build <card_number_in_hand>");
                    } else
                        System.out.println("Max builds reached for this turn.");
                    break;
                case "action":
                    if (character != null) {
                        boolean alreadyUsed = false;
                        if (character instanceof Assassin && getKilledCharacterRank() != -1
                                && humanPlayer.getCurrentCharacterCard() == character) {
                            System.out.println("Assassin's target already chosen this round.");
                            alreadyUsed = true;
                        } else if (character instanceof Thief && getRobbedCharacterRank() != -1
                                && humanPlayer.getCurrentCharacterCard() == character) {
                            System.out.println("Thief's target already chosen this round.");
                            alreadyUsed = true;
                        }
                        if (!alreadyUsed) {
                            if (parts.length == 1) {
                                // Just "action" was typed - show the special ability guideline
                                System.out.println(character.getPerformSpecialAbilityDescription());
                            } else if (parts.length > 1 && parts[1].equalsIgnoreCase("purple")) {
                                handlePurpleDistrictAction(humanPlayer,
                                        commandLine.substring("action purple".length()).trim());
                            } else {
                                character.performSpecialAbility(this, humanPlayer, commandLine);
                            }
                        }
                    } else
                        System.out.println("No character to perform action.");
                    break;
                case "info":
                    if (parts.length > 1)
                        handleInfoCommand(humanPlayer, commandLine.substring(command.length()).trim());
                    else
                        System.out.println("Usage: info <card_H_in_hand | character_name>");
                    break;
                case "all":
                    displayAllPlayersInfo();
                    break;
                case "save":
                    if (parts.length > 1) {
                        String filename = parts[1];
                        saveGame(filename);
                    } else {
                        System.out.println("Usage: save <filename>");
                    }
                    break;
                case "load":
                    if (parts.length > 1) {
                        String filename = parts[1];
                        loadGame(filename);
                    } else {
                        System.out.println("Usage: load <filename>");
                    }
                    break;
                case "debug":
                    debugMode = !debugMode;
                    System.out.println("Debug mode " + (debugMode ? "enabled" : "disabled"));
                    break;
                case "end":
                    turnEndedByPlayer = true;
                    System.out.println("You ended your turn.");

                    // Check for end-of-turn purple district abilities
                    checkEndOfTurnPurpleDistrictAbilities(humanPlayer);
                    break;
                case "t": // 't' is not used to end the human's active turn actions.
                    System.out.println(
                            "Your turn is active. Use 'end' to finish your turn. Type 'help' for other commands.");
                    break;
                default:
                    displayHelp();
                    break;
            }
            if (checkGameEndConditionDuringTurn(humanPlayer) && !turnEndedByPlayer) {
                // If game ends due to building 8th district, but player hasn't typed 'end',
                // their turn effectively ends here for action purposes.
                // However, the loop continues until 'end' is typed to allow viewing info, etc.
                // Or, we could force turnEndedByPlayer = true;
                // For now, let them type 'end'.
            }
        }
        return true;
    }

    private void checkGameEndCondition() {
    Player currentPlayer = players.get(currentPlayerIndex);
    if (currentPlayer.getCity().size() >= 8) {
        gameEnded = true;
        firstPlayerToCompleteCity = currentPlayer;
        System.out.println(currentPlayer.getName() + " is the winnerwinner!");
        calculateAndAnnounceWinner();
    }
}

    private void calculateAndAnnounceWinner() {
        System.out.println("=====================================");
        System.out.println("GAME OVER - CALCULATING FINAL SCORES");
        System.out.println("=====================================");

        // Calculate scores for each player
        for (Player player : players) {
            int score = 0;

            // Points from district costs
            for (DistrictCard district : player.getCity()) {
                score += district.getCost();
            }
            System.out.println(player.getName() + " - District points: " + score);

            // Bonus for having all district colors
            boolean hasYellow = false;
            boolean hasBlue = false;
            boolean hasGreen = false;
            boolean hasRed = false;
            boolean hasPurple = false;

            // Check for Haunted City (counts as any color for scoring)
            boolean hasHauntedCity = player.getCity().stream()
                    .anyMatch(card -> card.getName().equals("Haunted City"));

            for (DistrictCard district : player.getCity()) {
                switch (district.getColor()) {
                    case YELLOW:
                        hasYellow = true;
                        break;
                    case BLUE:
                        hasBlue = true;
                        break;
                    case GREEN:
                        hasGreen = true;
                        break;
                    case RED:
                        hasRed = true;
                        break;
                    case PURPLE:
                        hasPurple = true;
                        break;
                }
            }

            // If player has Haunted City, let them choose the color they need most
            if (hasHauntedCity) {
                if (!hasYellow)
                    hasYellow = true;
                else if (!hasBlue)
                    hasBlue = true;
                else if (!hasGreen)
                    hasGreen = true;
                else if (!hasRed)
                    hasRed = true;

                System.out.println(player.getName() + " - Haunted City counts as missing color for all-color bonus.");
            }

            if (hasYellow && hasBlue && hasGreen && hasRed) {
                score += 3;
                System.out.println(player.getName() + " - Bonus for having all colors: +3");
            }

            // Bonus for completing city
            if (player.getCity().size() >= 8) {
                if (player.isFirstToCompleteCity()) {
                    score += 4;
                    System.out.println(player.getName() + " - First to complete city: +4");
                } else {
                    score += 2;
                    System.out.println(player.getName() + " - Completed city: +2");
                }
            }

            // Bonus from purple districts
            for (DistrictCard district : player.getCity()) {
                if (district.getColor() == DistrictColor.PURPLE) {
                    int purpleBonus = 0;

                    switch (district.getName()) {
                        case "Dragon Gate":
                        case "University":
                            purpleBonus = 2; // Worth 8 points instead of 6
                            break;
                        case "Imperial Treasury":
                            purpleBonus = player.getGold();
                            break;
                        case "Map Room":
                            purpleBonus = player.getHand().size();
                            break;
                        case "Wishing Well":
                            // Count other purple districts
                            int otherPurpleCount = 0;
                            for (DistrictCard d : player.getCity()) {
                                if (d.getColor() == DistrictColor.PURPLE && !d.getName().equals("Wishing Well")) {
                                    otherPurpleCount++;
                                }
                            }
                            purpleBonus = otherPurpleCount;
                            break;
                    }

                    if (purpleBonus > 0) {
                        score += purpleBonus;
                        System.out.println(
                                player.getName() + " - Bonus from " + district.getName() + ": +" + purpleBonus);
                    }
                }
            }

            player.setScore(score);
            System.out.println(player.getName() + " - TOTAL SCORE: " + score);
            System.out.println("-------------------------------------");
        }

        // Find the winner
        Player winner = players.stream()
                .max(Comparator.comparingInt(Player::getScore))
                .orElse(null);

        // Check for ties
        List<Player> tiedPlayers = players.stream()
                .filter(p -> p.getScore() == winner.getScore())
                .collect(Collectors.toList());

        if (tiedPlayers.size() > 1) {
            // Resolve tie by highest character rank in last round
            Player tieWinner = tiedPlayers.stream()
                    .filter(p -> p.getCurrentCharacterCard() != null)
                    .max(Comparator.comparingInt(p -> p.getCurrentCharacterCard().getRank()))
                    .orElse(tiedPlayers.get(0));

            System.out.println("There was a tie! " + tieWinner.getName()
                    + " wins with the highest character rank in the final round.");
            System.out.println(tieWinner.getName() + " is the winner with " + tieWinner.getScore() + " points!");
        } else {
            System.out.println(winner.getName() + " is the winner with " + winner.getScore() + " points!");
        }
    }

    private boolean waitForTOrUniversalCommand(String promptForT, String nonTurnPrompt) {
        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                System.err.println("End of input detected. Exiting.");
                gameEnded = true;
                return false;
            }
            String inputLine = scanner.nextLine().trim();
            if (inputLine.isEmpty())
                continue;

            String command = inputLine.split("\\s+")[0].toLowerCase();

            if (command.equals("t"))
                return true;

            if (isUniversalCommand(command)) {
                handleUniversalCommand(inputLine);
                // After handling a universal command, re-iterate the need for 't' or the
                // original prompt.
                // The calling method should re-print its specific prompt if needed.
                // For now, let's assume the calling method will handle re-prompting if it was a
                // specific wait.
                // Or, we can just print a generic "> " again.
                // System.out.println(promptForT); // Re-prompt might be too verbose here.
                continue; // Loop again to wait for 't' or another universal command
            }

            System.out.println(nonTurnPrompt);
        }
    }

    private boolean isUniversalCommand(String command) {
        return command.equals("help") || command.equals("all") || command.equals("debug") ||
                command.equals("save") || command.equals("load") || command.equals("gold") ||
                command.equals("hand") || command.equals("city") || command.equals("citadel") || command.equals("list");
    }

    private void handleUniversalCommand(String commandLine) {
        String[] parts = commandLine.split("\\s+", 2);
        String command = parts[0].toLowerCase();

        switch (command) {
            case "help":
                displayHelp();
                break;
            case "all":
                displayAllPlayersInfo();
                break;
            case "debug":
                debugMode = !debugMode;
                System.out.println("Debug mode " + (debugMode ? "enabled" : "disabled"));
                break;
            case "save":
                if (parts.length > 1) {
                    String filename = parts[1];
                    saveGame(filename);
                } else {
                    System.out.println("Usage: save <filename>");
                }
                break;
            case "load":
                if (parts.length > 1) {
                    String filename = parts[1];
                    loadGame(filename);
                } else {
                    System.out.println("Usage: load <filename>");
                }
                break;
            default:
                break;
        }
    }

    private void displayHelp() {
        System.out.println("Available commands:");
        System.out.println("info : show information about a character or building");
        System.out.println("t : processes turns");
        System.out.println("all : shows all current game info");
        System.out.println("citadel/list/city : shows districts built by a player");
        System.out.println("hand : shows cards in hand");
        System.out.println("gold [p] : shows gold of a player");
        System.out.println("build <place in hand> : Builds a building into your city");
        System.out.println("action : Shows your character's special ability and how to use it");
        System.out.println("  - Assassin: 'action kill <character_rank>' to kill a character");
        System.out.println("  - Thief: 'action steal <character_rank>' to rob a character");
        System.out.println(
                "  - Magician: 'action swap <player_number>' to swap hands or 'action redraw <id1,id2,...>' to discard and redraw");
        System.out.println("  - Warlord: 'action destroy <player_number> <district_index>' to destroy a district");
        System.out.println("  - Purple districts: 'action purple <district_name>' to use a purple district's ability");
        System.out.println("end : Ends your turn");
        System.out.println("save <file> : Saves the current game state");
        System.out.println("load <file> : Loads the game state");
        System.out.println("help : Shows this help message");
        System.out.println("debug : Toggles debug mode");
    }

    private void handleInfoCommand(Player player, String query) {
        // Check if it's a request for information about a purple district
        if (query.toLowerCase().startsWith("purple")) {
            String districtName = query.substring("purple".length()).trim();
            Optional<DistrictCard> purpleDistrict = player.getCity().stream()
                    .filter(d -> d.getColor() == DistrictColor.PURPLE && d.getName().equalsIgnoreCase(districtName))
                    .findFirst();

            if (purpleDistrict.isPresent()) {
                DistrictCard district = purpleDistrict.get();
                System.out.println(district.getName() + " - " + district.getSpecialAbility());

                // Provide additional usage information
                switch (district.getName()) {
                    case "Laboratory":
                        System.out.println("To use: 'action purple Laboratory' - Discard a card for 1 gold");
                        break;
                    case "Smithy":
                        System.out.println("To use: 'action purple Smithy' - Pay 2 gold to draw 3 cards");
                        break;
                    case "Armory":
                        System.out.println(
                                "To use: 'action purple Armory' - Destroy the Armory to destroy another district");
                        break;
                    default:
                        System.out.println("This district has a passive ability that works automatically.");
                        break;
                }
                return;
            }
        }

        try {
            int cardIndex = Integer.parseInt(query) - 1;
            if (cardIndex >= 0 && cardIndex < player.getHand().size()) {
                DistrictCard card = player.getHand().get(cardIndex);
                if (card.getColor() == DistrictColor.PURPLE && card.getSpecialAbility() != null) {
                    System.out.println("Info for " + card.getName() + ": " + card.getSpecialAbility());
                } else if (card.getColor() == DistrictColor.PURPLE) {
                    System.out
                            .println(card.getName() + " is a purple district but has no special ability text defined.");
                } else {
                    System.out.println(card.getName() + " is not a purple district with a special ability text.");
                }
                return;
            }
        } catch (NumberFormatException e) {
        }

        for (CharacterCard character : allCharacterCards) {
            if (character.getName().equalsIgnoreCase(query)) {
                System.out
                        .println("Info for Character " + character.getName() + " (Rank " + character.getRank() + "):");
                System.out.println("  " + character.getSpecialAbility());
                return;
            }
        }
        System.out.println(
                "Could not find info for '" + query
                        + "'. Use 'info <card_#_in_hand>', 'info purple <district_name>', or 'info <character_name>'.");
    }

    public int getKilledCharacterRank() {
        return this.killCharacterRank;
    }

    public void setKilledCharacterRank(int rank) {
        this.killCharacterRank = rank;
    }

    public int getRobbedCharacterRank() {
        return robbedCharacterRank;
    }

    public void setRobbedCharacterRank(int rank, Player thief) {
        this.robbedCharacterRank = rank;
        this.thiefPlayer = thief;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Deck<DistrictCard> getDistrictDeck() {
        return districtDeck;
    }

    public List<CharacterCard> getAllCharacterCards() {
        return allCharacterCards;
    }

    public CharacterCard getCharacterByRank(int rank) {
        return allCharacterCards.stream().filter(cc -> cc.getRank() == rank).findFirst().orElse(null);
    }

    private void handlePurpleDistrictAction(Player player, String action) {
        String[] parts = action.split("\\s+");
        if (parts.length < 1) {
            System.out.println("Invalid purple district action. Use 'action purple <district_name>'");
            return;
        }

        String districtName = parts[0];
        Optional<DistrictCard> purpleDistrict = player.getCity().stream()
                .filter(d -> d.getColor() == DistrictColor.PURPLE && d.getName().equalsIgnoreCase(districtName))
                .findFirst();

        if (!purpleDistrict.isPresent()) {
            System.out.println("You don't have a purple district named " + districtName + " in your city.");
            return;
        }

        DistrictCard district = purpleDistrict.get();
        String key = player.getName() + "-" + district.getName();

        if (purpleAbilitiesUsedThisTurn.containsKey(key) && purpleAbilitiesUsedThisTurn.get(key)) {
            System.out.println("You've already used " + district.getName() + "'s ability this turn.");
            return;
        }

        // Handle each purple district's special ability
        switch (district.getName()) {
            case "Laboratory":
                if (player.getHand().isEmpty()) {
                    System.out.println("You have no cards to discard.");
                    return;
                }

                System.out.println("Choose a card to discard for 1 gold (1-" + player.getHand().size() + "):");
                try {
                    int cardIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    if (cardIndex >= 0 && cardIndex < player.getHand().size()) {
                        DistrictCard discarded = player.getHand().get(cardIndex);
                        player.removeDistrictCardInHand(cardIndex);
                        player.addGold(1);
                        System.out.println("You discarded " + discarded.getName() + " and received 1 gold.");
                        purpleAbilitiesUsedThisTurn.put(key, true);
                    } else {
                        System.out.println("Invalid card index.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                }
                break;

            case "Smithy":
                if (player.getGold() < 2) {
                    System.out
                            .println("You need 2 gold to use the Smithy. You only have " + player.getGold() + " gold.");
                    return;
                }

                player.removeGold(2);
                for (int i = 0; i < 3; i++) {
                    DistrictCard card = districtDeck.drawCard();
                    if (card != null) {
                        player.addDistrictCardToHand(card);
                    }
                }
                System.out.println("You paid 2 gold and drew 3 district cards.");
                purpleAbilitiesUsedThisTurn.put(key, true);
                break;

            case "Armory":
                System.out.println("Do you want to destroy the Armory to destroy another district? (yes/no)");
                String response = scanner.nextLine().trim().toLowerCase();
                if (response.equals("yes")) {
                    System.out.println("Choose a player (1-" + players.size() + "):");
                    try {
                        int playerIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (playerIndex >= 0 && playerIndex < players.size()) {
                            Player targetPlayer = players.get(playerIndex);
                            if (targetPlayer.getCity().isEmpty()) {
                                System.out.println(targetPlayer.getName() + " has no districts to destroy.");
                                return;
                            }

                            System.out
                                    .println("Choose a district to destroy (1-" + targetPlayer.getCity().size() + "):");
                            int districtIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
                            if (districtIndex >= 0 && districtIndex < targetPlayer.getCity().size()) {
                                DistrictCard targetDistrict = targetPlayer.getCity().get(districtIndex);

                                // Cannot destroy Keep
                                if (targetDistrict.getName().equals("Keep")) {
                                    System.out.println("The Keep cannot be destroyed.");
                                    return;
                                }

                                // Cannot destroy districts of Bishop unless killed
                                if (targetPlayer.getCurrentCharacterCard() != null &&
                                        targetPlayer.getCurrentCharacterCard().getRank() == 5 &&
                                        killCharacterRank != 5) {
                                    System.out.println(
                                            "Cannot destroy a district belonging to the Bishop unless they are killed.");
                                    return;
                                }

                                // Destroy both Armory and target district
                                player.removeDistrictCardInCity(district);
                                targetPlayer.removeDistrictCardInCity(targetDistrict);
                                System.out.println("You destroyed your Armory to destroy " + targetDistrict.getName() +
                                        " in " + targetPlayer.getName() + "'s city.");

                                // Check for Graveyard effect
                                for (Player p : players) {
                                    if (p != player && p != targetPlayer) {
                                        boolean hasGraveyard = p.getCity().stream()
                                                .anyMatch(card -> card.getName().equals("Graveyard"));

                                        if (hasGraveyard && p.getGold() >= 1) {
                                            if (p == players.get(0)) { // Human player
                                                System.out.println(
                                                        "You have a Graveyard. Do you want to pay 1 gold to take the destroyed district? (yes/no)");
                                                String graveyardResponse = scanner.nextLine().trim().toLowerCase();
                                                if (graveyardResponse.equals("yes")) {
                                                    p.removeGold(1);
                                                    p.addDistrictCardToHand(targetDistrict);
                                                    System.out.println("You paid 1 gold and added "
                                                            + targetDistrict.getName() + " to your hand.");
                                                }
                                            } else { // AI player
                                                p.removeGold(1);
                                                p.addDistrictCardToHand(targetDistrict);
                                                System.out.println(p.getName()
                                                        + " used their Graveyard to take the destroyed district.");
                                            }
                                        }
                                    }
                                }
                            } else {
                                System.out.println("Invalid district index.");
                            }
                        } else {
                            System.out.println("Invalid player index.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                    }
                }
                break;

            case "Lighthouse":
                System.out.println("You can look through the District Deck and choose one card to add to your hand.");
                System.out.println("Available cards in the deck:");

                List<DistrictCard> deckCards = districtDeck.getCards();
                if (deckCards.isEmpty()) {
                    System.out.println("The district deck is empty.");
                    return;
                }

                for (int i = 0; i < deckCards.size(); i++) {
                    System.out.println((i + 1) + ". " + deckCards.get(i).toString());
                }

                System.out.println("Choose a card number to add to your hand:");
                try {
                    int cardIndex = Integer.parseInt(scanner.nextLine().trim()) - 1;
                    if (cardIndex >= 0 && cardIndex < deckCards.size()) {
                        DistrictCard selectedCard = deckCards.get(cardIndex);
                        deckCards.remove(cardIndex);
                        player.addDistrictCardToHand(selectedCard);
                        System.out.println("You added " + selectedCard.getName() + " to your hand.");

                        // Shuffle the deck
                        districtDeck.shuffle();
                        System.out.println("The district deck has been shuffled.");

                        purpleAbilitiesUsedThisTurn.put(key, true);
                    } else {
                        System.out.println("Invalid card index.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                }
                break;

            case "Bell Tower":
                System.out.println(
                        "Do you want to announce that the game will end when a player builds 7 districts instead of 8? (yes/no)");
                String bellTowerResponse = scanner.nextLine().trim().toLowerCase();
                if (bellTowerResponse.equals("yes")) {
                    System.out.println(
                            "You announced that the game will end when a player builds 7 districts instead of 8!");
                    // This would need to be implemented by modifying the checkGameEndCondition
                    // methods
                    // For now, we'll just set a flag and print a message
                    purpleAbilitiesUsedThisTurn.put(key, true);
                }
                break;

            default:
                System.out.println(
                        "The " + district.getName() + " ability is passive or not implemented for direct use.");
                break;
        }
    }

    private void checkEndOfTurnPurpleDistrictAbilities(Player player) {
        // Check for Poor House
        boolean hasPoorHouse = player.getCity().stream()
                .anyMatch(card -> card.getName().equals("Poor House"));

        if (hasPoorHouse && player.getGold() == 0) {
            player.addGold(1);
            System.out.println(player.getName()
                    + " has no gold at the end of their turn and receives 1 gold from the Poor House.");
        }

        // Check for Park
        boolean hasPark = player.getCity().stream()
                .anyMatch(card -> card.getName().equals("Park"));

        if (hasPark && player.getHand().isEmpty()) {
            for (int i = 0; i < 2; i++) {
                DistrictCard card = districtDeck.drawCard();
                if (card != null) {
                    player.addDistrictCardToHand(card);
                }
            }
            System.out.println(
                    player.getName() + " has no cards at the end of their turn and draws 2 cards from the Park.");
        }
    }

    private void saveGame(String filename) {
        try {
            GameState gameState = new GameState();
            gameState.setPlayers(players);
            gameState.setDistrictDeck(districtDeck);
            gameState.setAllCharacterCards(allCharacterCards);
            gameState.setMysteryCardFaceDown(mysteryCardFaceDown);
            gameState.setCrownedPlayer(players.indexOf(crownedPlayer));
            gameState.setKillCharacterRank(killCharacterRank);
            gameState.setRobbedCharacterRank(robbedCharacterRank);
            gameState.setThiefPlayer(players.indexOf(thiefPlayer));
            gameState.setGameEnded(gameEnded);
            gameState.setFirstPlayerToCompleteCity(players.indexOf(firstPlayerToCompleteCity));

            // Convert to JSON using simple-json
            org.json.simple.JSONObject json = new org.json.simple.JSONObject();

            // Save player data
            org.json.simple.JSONArray playersJson = new org.json.simple.JSONArray();
            for (Player player : players) {
                org.json.simple.JSONObject playerJson = new org.json.simple.JSONObject();
                playerJson.put("name", player.getName());
                playerJson.put("gold", player.getGold());
                playerJson.put("score", player.getScore());
                playerJson.put("firstToCompleteCity", player.isFirstToCompleteCity());
                playerJson.put("isHuman", player instanceof HumanPlayer);

                // Save player's hand
                org.json.simple.JSONArray handJson = new org.json.simple.JSONArray();
                for (DistrictCard card : player.getHand()) {
                    org.json.simple.JSONObject cardJson = new org.json.simple.JSONObject();
                    cardJson.put("name", card.getName());
                    cardJson.put("color", card.getColor().name());
                    cardJson.put("cost", card.getCost());
                    if (card.getSpecialAbility() != null) {
                        cardJson.put("specialAbility", card.getSpecialAbility());
                    }
                    handJson.add(cardJson);
                }
                playerJson.put("hand", handJson);

                // Save player's city
                org.json.simple.JSONArray cityJson = new org.json.simple.JSONArray();
                for (DistrictCard card : player.getCity()) {
                    org.json.simple.JSONObject cardJson = new org.json.simple.JSONObject();
                    cardJson.put("name", card.getName());
                    cardJson.put("color", card.getColor().name());
                    cardJson.put("cost", card.getCost());
                    if (card.getSpecialAbility() != null) {
                        cardJson.put("specialAbility", card.getSpecialAbility());
                    }
                    cityJson.add(cardJson);
                }
                playerJson.put("city", cityJson);

                // Save player's character
                if (player.getCurrentCharacterCard() != null) {
                    playerJson.put("characterRank", player.getCurrentCharacterCard().getRank());
                }

                playersJson.add(playerJson);
            }
            json.put("players", playersJson);

            // Save district deck
            org.json.simple.JSONArray deckJson = new org.json.simple.JSONArray();
            for (DistrictCard card : districtDeck.getCards()) {
                org.json.simple.JSONObject cardJson = new org.json.simple.JSONObject();
                cardJson.put("name", card.getName());
                cardJson.put("color", card.getColor().name());
                cardJson.put("cost", card.getCost());
                if (card.getSpecialAbility() != null) {
                    cardJson.put("specialAbility", card.getSpecialAbility());
                }
                deckJson.add(cardJson);
            }
            json.put("districtDeck", deckJson);

            // Save game state
            json.put("crownedPlayerIndex", players.indexOf(crownedPlayer));
            json.put("killCharacterRank", killCharacterRank);
            json.put("robbedCharacterRank", robbedCharacterRank);
            json.put("thiefPlayerIndex", thiefPlayer != null ? players.indexOf(thiefPlayer) : -1);
            json.put("gameEnded", gameEnded);
            json.put("firstPlayerToCompleteCityIndex",
                    firstPlayerToCompleteCity != null ? players.indexOf(firstPlayerToCompleteCity) : -1);

            // Write to file
            try (java.io.FileWriter file = new java.io.FileWriter(filename)) {
                file.write(json.toJSONString());
                System.out.println("Game saved to " + filename);
            }
        } catch (Exception e) {
            System.out.println("Error saving game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadGame(String filename) {
        try {
            org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
            org.json.simple.JSONObject json = (org.json.simple.JSONObject) parser
                    .parse(new java.io.FileReader(filename));

            // Clear current game state
            players.clear();
            districtDeck = new Deck<>();

            // Load players
            org.json.simple.JSONArray playersJson = (org.json.simple.JSONArray) json.get("players");
            for (Object obj : playersJson) {
                org.json.simple.JSONObject playerJson = (org.json.simple.JSONObject) obj;
                String name = (String) playerJson.get("name");
                long gold = (Long) playerJson.get("gold");
                long score = (Long) playerJson.get("score");
                boolean firstToCompleteCity = (Boolean) playerJson.get("firstToCompleteCity");
                boolean isHuman = (Boolean) playerJson.get("isHuman");

                Player player;
                if (isHuman) {
                    player = new HumanPlayer(name);
                } else {
                    player = new ComputerPlayer(name);
                }
                player.setScore((int) score);
                player.addGold((int) gold - 2); // Adjust for the 2 gold added in constructor
                player.setFirstToCompleteCity(firstToCompleteCity);

                // Load player's hand
                org.json.simple.JSONArray handJson = (org.json.simple.JSONArray) playerJson.get("hand");
                for (Object cardObj : handJson) {
                    org.json.simple.JSONObject cardJson = (org.json.simple.JSONObject) cardObj;
                    String cardName = (String) cardJson.get("name");
                    String colorStr = (String) cardJson.get("color");
                    long cost = (Long) cardJson.get("cost");
                    String specialAbility = (String) cardJson.get("specialAbility");

                    DistrictColor color = DistrictColor.valueOf(colorStr);
                    DistrictCard card = new DistrictCard(cardName, color, (int) cost, specialAbility);
                    player.addDistrictCardToHand(card);
                }

                // Load player's city
                org.json.simple.JSONArray cityJson = (org.json.simple.JSONArray) playerJson.get("city");
                for (Object cardObj : cityJson) {
                    org.json.simple.JSONObject cardJson = (org.json.simple.JSONObject) cardObj;
                    String cardName = (String) cardJson.get("name");
                    String colorStr = (String) cardJson.get("color");
                    long cost = (Long) cardJson.get("cost");
                    String specialAbility = (String) cardJson.get("specialAbility");

                    DistrictColor color = DistrictColor.valueOf(colorStr);
                    DistrictCard card = new DistrictCard(cardName, color, (int) cost, specialAbility);
                    player.getCity().add(card); // Add directly to city to avoid gold cost
                }

                players.add(player);
            }

            // Load district deck
            org.json.simple.JSONArray deckJson = (org.json.simple.JSONArray) json.get("districtDeck");
            for (Object obj : deckJson) {
                org.json.simple.JSONObject cardJson = (org.json.simple.JSONObject) obj;
                String cardName = (String) cardJson.get("name");
                String colorStr = (String) cardJson.get("color");
                long cost = (Long) cardJson.get("cost");
                String specialAbility = (String) cardJson.get("specialAbility");

                DistrictColor color = DistrictColor.valueOf(colorStr);
                DistrictCard card = new DistrictCard(cardName, color, (int) cost, specialAbility);
                districtDeck.addCard(card);
            }

            // Load game state
            long crownedPlayerIndex = (Long) json.get("crownedPlayerIndex");
            if (crownedPlayerIndex >= 0 && crownedPlayerIndex < players.size()) {
                crownedPlayer = players.get((int) crownedPlayerIndex);
            }

            killCharacterRank = ((Long) json.get("killCharacterRank")).intValue();
            robbedCharacterRank = ((Long) json.get("robbedCharacterRank")).intValue();

            long thiefPlayerIndex = (Long) json.get("thiefPlayerIndex");
            if (thiefPlayerIndex >= 0 && thiefPlayerIndex < players.size()) {
                thiefPlayer = players.get((int) thiefPlayerIndex);
            }

            gameEnded = (Boolean) json.get("gameEnded");

            long firstPlayerToCompleteCityIndex = (Long) json.get("firstPlayerToCompleteCityIndex");
            if (firstPlayerToCompleteCityIndex >= 0 && firstPlayerToCompleteCityIndex < players.size()) {
                firstPlayerToCompleteCity = players.get((int) firstPlayerToCompleteCityIndex);
            }

            // Initialize character cards
            allCharacterCards = initializeCharacterCards();

            System.out.println("Game loaded from " + filename);
            System.out.println("Continuing game with " + players.size() + " players...");

        } catch (Exception e) {
            System.out.println("Error loading game: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
