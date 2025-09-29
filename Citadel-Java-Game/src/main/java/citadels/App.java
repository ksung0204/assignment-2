package citadels;

import java.util.Scanner;
import citadels.Game.Game;

public class App {
    public static void main(String[] args) {
        Game game = new Game(new Scanner(System.in));
        game.play();
    }
}
