package me.zetra.onboarding.commands;

import java.util.List;
import me.zetra.onboarding.ZetraOnboarding;
import me.zetra.onboarding.models.OnboardingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SkipCommand
implements CommandExecutor {
    private final ZetraOnboarding plugin;

    public SkipCommand(ZetraOnboarding plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00a7cApenas jogadores podem usar este comando.");
            return true;
        }
        Player player = (Player)sender;
        OnboardingPlayer op = this.plugin.getMissionManager().getOnboardingPlayer(player);
        if (op.isFinished()) {
            player.sendMessage(this.plugin.getConfig().getString("messages.already-finished").replace("&", "\u00a7"));
            return true;
        }
        if (this.plugin.getConfig().getBoolean("settings.skip-rewards", true)) {
            this.giveFinalRewards(player);
        }
        this.plugin.getMissionManager().finishOnboarding(player);
        player.sendMessage(this.plugin.getConfig().getString("messages.skip-success").replace("&", "\u00a7"));
        return true;
    }

    private void giveFinalRewards(Player player) {
        List<String> commands = this.plugin.getConfig().getStringList("missions.m4.reward-commands");
        for (String cmd : commands) {
            String processed = cmd.replace("%player%", player.getName());
            Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)processed);
        }
    }
}
