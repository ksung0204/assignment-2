package citadels.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import citadels.Cards.Card;

public class Deck<T extends Card> {
    private ArrayList<T> cards;

    public Deck() {
        this.cards = new ArrayList<>();
    }

    public Deck(List<T> cards) {
        this.cards = new ArrayList<>(cards);
    }

    public void addCard(T card) {
        cards.add(card);
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public T drawCard() {
        if (isEmpty()) {
            return null;
        }
        return cards.remove(0);
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public List<T> getCards() {
        return cards;
    }

    public void addCardToBottom(T card) {
        cards.add(card);
    }
}