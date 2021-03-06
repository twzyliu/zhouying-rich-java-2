package rich.command;

import com.sun.tools.javac.util.Pair;
import rich.game.Message;
import rich.game.Player;

public interface Command {
    Pair<Player.Status, Message> execute(Player player);

    Pair<Player.Status, Message> respondWith(Player player, Response response);
}
