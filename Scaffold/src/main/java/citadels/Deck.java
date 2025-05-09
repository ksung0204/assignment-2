
package citadels;
import java.util.ArrayList;
import java.util.Collections;
//dùng generics để có thể dùng district hoặc charactercardcharactercard
public class Deck <T>{
    private ArrayList <T> cards;
    public Deck(){
        cards= new ArrayList<>();
    }
    public void addCard(T card){
        cards.add(card);
    }
    public T drawCard(){
        if (cards.isEmpty()){
            return null;
        }
        return cards.remove(0);

    }
    public void addCardToBottom(T card){
        cards.add(card);
    }
    public void shuffle(){
        Collections.shuffle(cards);
    }
    public ArrayList<T> getCards(){
        return new ArrayList<>(cards);
    }
    public boolean isEmpty(){
        return cards.isEmpty();   
    }
}
