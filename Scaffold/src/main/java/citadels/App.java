package citadels;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.io.*;
import java.util.*;

public class App {
	
	private File cardsFile;

	public App() {
		try {
            cardsFile = new File(URLDecoder.decode(this.getClass().getResource("cards.tsv").getPath(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
	}

    public static void main(String[] args) {
        //App app = new App();
        Game game=new Game();
        game.start();
    }

}
