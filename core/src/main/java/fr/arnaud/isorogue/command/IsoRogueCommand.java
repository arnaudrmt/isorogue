package fr.arnaud.isorogue.command;

import fr.arnaud.isorogue.IsoRogue;
import fr.arnaud.isorogue.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IsoRogueCommand implements CommandExecutor {

    private final GameManager gameManager;

    public IsoRogueCommand(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only a player can execute this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "WRONG SYNTAXE! " + ChatColor.YELLOW +
                    "/rogue <start|stop>");
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {

            if (gameManager.getGameByPlayer(player) != null) {
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR! " + ChatColor.YELLOW +
                        "You are already in a game!");
                return true;
            }

            IsoRogue.getInstance().getGameManager().startNewGame(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {

            if (gameManager.getGameByPlayer(player) == null) {
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "ERROR! " + ChatColor.YELLOW +
                        "You are not currently in a game!");
                return true;
            }

            IsoRogue.getInstance().getGameManager().stopGame(player);
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "YOU LEFT! " + ChatColor.YELLOW +
                    "You have left your dungeon!");
            return true;
        }

        player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "WRONG SYNTAXE! " + ChatColor.YELLOW +
                "/rogue <start|stop>");

        return true;
    }
}
