package me.zetra.onboarding;

import me.zetra.onboarding.commands.OnboardingCommand;
import me.zetra.onboarding.commands.SkipCommand;
import me.zetra.onboarding.listeners.PlayerListener;
import me.zetra.onboarding.managers.MissionManager;
import me.zetra.onboarding.models.OnboardingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ZetraOnboarding
extends JavaPlugin {
    private MissionManager missionManager;

    public void onEnable() {
        this.saveDefaultConfig();
        this.missionManager = new MissionManager(this);
        this.getCommand("zetraonboarding").setExecutor((CommandExecutor)new OnboardingCommand(this));
        this.getCommand("skip").setExecutor((CommandExecutor)new SkipCommand(this));
        this.getServer().getPluginManager().registerEvents((Listener)new PlayerListener(this), (Plugin)this);
        this.startReminderTask();
        this.startClanCheckTask();
        this.getLogger().info("ZetraOnboarding habilitado com sucesso!");
    }

    public void onDisable() {
        this.missionManager.saveData();
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.missionManager.unloadPlayer(player);
        }
    }

    public MissionManager getMissionManager() {
        return this.missionManager;
    }

    private void startReminderTask() {
        int delay = this.getConfig().getInt("settings.announcement-delay", 60) * 20;
        new BukkitRunnable(){

            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    String warn;
                    OnboardingPlayer op = ZetraOnboarding.this.missionManager.getOnboardingPlayer(player);
                    if (op == null || op.isFinished() || (warn = ZetraOnboarding.this.getConfig().getString("missions.m" + op.getCurrentMission() + ".chat-warn")) == null || warn.isEmpty()) continue;
                    player.sendMessage(warn.replace("&", "\u00a7"));
                }
            }
        }.runTaskTimer((Plugin)this, (long)delay, (long)delay);
    }

    private void startClanCheckTask() {
        new BukkitRunnable(){

            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ZetraOnboarding.this.missionManager.checkClanMission(player);
                }
            }
        }.runTaskTimer((Plugin)this, 100L, 100L);
    }
}
