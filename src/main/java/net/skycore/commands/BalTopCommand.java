package net.skycore.commands;

import net.skycore.SkyCore;
import org.powernukkitx.OfflinePlayer;
import org.powernukkitx.command.Command;
import org.powernukkitx.command.CommandExecutor;
import org.powernukkitx.command.CommandSender;

import java.util.*;

public class BalTopCommand implements CommandExecutor {

    private final SkyCore plugin;

    public BalTopCommand(SkyCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String symbol = plugin.getEconomyManager().getCurrencySymbol();
        List<Map.Entry<UUID, Double>> entries = new ArrayList<>(plugin.getEconomyManager().getAllBalances().entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        sender.sendMessage("\u00A7b--- En Zengin 10 Oyuncu ---");
        int rank = 1;
        for (Map.Entry<UUID, Double> e : entries) {
            if (rank > 10) break;
            OfflinePlayer op = plugin.getServer().getOfflinePlayer(e.getKey());
            String name = op != null ? op.getName() : e.getKey().toString();
            sender.sendMessage("\u00A7f#" + rank + " \u00A7e" + name + " \u00A77- \u00A7a" + symbol + String.format("%.2f", e.getValue()));
            rank++;
        }
        return true;
    }
}
