import com.sun.tools.javac.util.Pair;

public class Prison extends Place {
    @Override
    public Pair<Player.Status, Message> actionTo(Player player) {
        return new Pair<>(Player.Status.END_TURN, Message.COME_TO_PRISON);
    }
}
