package citadels.Player;

import citadels.Cards.DistrictCard;
import citadels.Cards.Characters.Architect;
import citadels.Cards.Characters.Magician;
import citadels.Cards.Characters.Merchant;
import citadels.Game.Game;
import citadels.Cards.CharacterCard;
import java.util.List;

public class ComputerPlayer extends Player {
    public ComputerPlayer(String name) {
        super(name);
    }


    // AI LOGIC
    public DistrictCard getBestDistrictCardToBuild() {
        if (hand.isEmpty()) return null;
        DistrictCard bestCard = null;
        for (DistrictCard card : hand) {
            if (canBuildDistrict(card)) {
                if (bestCard == null || card.getCost() > bestCard.getCost()) {
                    bestCard = card;
                }
            }
        }
        return bestCard;
    }

    public String chooseActionAI() { 
        if (hand.size() < 2 && gold > 5) return "cards"; // Ít bài, nhiều tiền -> lấy bài
        if (gold < 3) return "gold"; // Ít tiền -> lấy tiền
        return Math.random() < 0.5 ? "gold" : "cards"; // Ngẫu nhiên
    }

    public CharacterCard chooseCharacterAI(List<CharacterCard> availableCharacters, Game game) {
        // Logic AI cơ bản:
        // 1. Nếu có thể thắng vòng này (xây đủ 8 quận), chọn Architect nếu có thể xây nhiều.
        // 2. Nếu ít bài, ưu tiên Architect hoặc Magician.
        // 3. Nếu ít vàng, ưu tiên Merchant, King, Bishop (nếu có quận tương ứng).
        // 4. Nếu có người chơi gần thắng, cân nhắc Assassin hoặc Warlord.
        // 5. Ngược lại, chọn ngẫu nhiên hoặc một nhân vật có lợi ích chung.

        // Ví dụ rất đơn giản:
        if (hand.size() < 2) {
            for (CharacterCard cc : availableCharacters) {
                if (cc instanceof Architect || cc instanceof Magician) return cc;
            }
        }
        if (gold < 3) {
             for (CharacterCard cc : availableCharacters) {
                if (cc instanceof Merchant) return cc;
            }
        }
        // Chọn ngẫu nhiên nếu không có ưu tiên rõ ràng
        if (!availableCharacters.isEmpty()) {
            return availableCharacters.get((int) (Math.random() * availableCharacters.size()));
        }
        return null;
    }
}
