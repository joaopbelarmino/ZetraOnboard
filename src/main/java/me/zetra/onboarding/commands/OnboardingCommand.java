package me.zetra.onboarding.commands;

import me.zetra.onboarding.ZetraOnboarding;
import me.zetra.onboarding.models.OnboardingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OnboardingCommand
implements CommandExecutor {
    private final ZetraOnboarding plugin;

    public OnboardingCommand(ZetraOnboarding plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("zetraonboarding.admin")) {
            sender.sendMessage("\u00a7cSem permiss\u00e3o.");
            return true;
        }
        if (args.length >= 2 && args[0].equalsIgnoreCase("next")) {
            Player target = Bukkit.getPlayer((String)args[1]);
            if (target != null) {
                OnboardingPlayer op = this.plugin.getMissionManager().getOnboardingPlayer(target);
                if (op.getCurrentMission() == 1 && !op.isFinished()) {
                    this.plugin.getMissionManager().nextMission(target);
                    sender.sendMessage("\u00a7aAvan\u00e7ando miss\u00e3o de " + target.getName());
                } else {
                    sender.sendMessage("\u00a7cO comando next s\u00f3 pode ser usado na Miss\u00e3o 1.");
                }
            } else {
                sender.sendMessage("\u00a7cJogador offline.");
            }
            return true;
        }
        sender.sendMessage("\u00a7eUso: /zetraonboarding next <player>");
        return true;
    }
}
