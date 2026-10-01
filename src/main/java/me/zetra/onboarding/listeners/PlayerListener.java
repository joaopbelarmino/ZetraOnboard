package me.zetra.onboarding.listeners;

import java.util.List;
import me.zetra.onboarding.ZetraOnboarding;
import me.zetra.onboarding.models.OnboardingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class PlayerListener
implements Listener {
    private final ZetraOnboarding plugin;

    public PlayerListener(ZetraOnboarding plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!this.plugin.getConfig().getBoolean("settings.anti-fake.enabled", true)) {
            return;
        }
        String ip = event.getAddress().getHostAddress();
        String name = event.getName();
        int maxPerIp = this.plugin.getConfig().getInt("settings.anti-fake.max-per-ip", 1);
        List whitelist = this.plugin.getConfig().getStringList("settings.anti-fake.whitelist");
        if (whitelist.contains(name)) {
            return;
        }
        int count = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.isOp() || whitelist.contains(online.getName()) || online.getAddress() == null || !online.getAddress().getAddress().getHostAddress().equals(ip)) continue;
            ++count;
        }
        if (count >= maxPerIp) {
            String kickMsg = this.plugin.getConfig().getString("settings.anti-fake.kick-message", "&cLimite de IP atingido!").replace("%limit%", String.valueOf(maxPerIp)).replace("&", "\u00a7");
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, kickMsg);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        OnboardingPlayer op = this.plugin.getMissionManager().getOnboardingPlayer(player);
        if (!op.isFinished()) {
            this.plugin.getMissionManager().updateBossBar(player);
            if (op.getCurrentMission() == 1) {
                player.sendTitle("\u00a7fSeja bem-vindo ao", "\u00a7d\u00a7lZetraMC", 20, 100, 20);
            }
            if (op.getCurrentMission() == 2) {
                this.plugin.getMissionManager().checkInventoryMission(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.plugin.getMissionManager().unloadPlayer(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack cursor;
        HumanEntity humanEntity = event.getWhoClicked();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player)humanEntity;
        OnboardingPlayer op = this.plugin.getMissionManager().getOnboardingPlayer(player);
        if (op.isFinished() || op.getCurrentMission() != 2) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item != null && item.getType() == Material.IRON_CHESTPLATE) {
            this.plugin.getMissionManager().nextMission(player);
        }
        if ((cursor = event.getCursor()) != null && cursor.getType() == Material.IRON_CHESTPLATE) {
            this.plugin.getMissionManager().nextMission(player);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player)livingEntity;
        OnboardingPlayer op = this.plugin.getMissionManager().getOnboardingPlayer(player);
        if (op.isFinished() || op.getCurrentMission() != 2) {
            return;
        }
        if (event.getItem().getItemStack().getType() == Material.IRON_CHESTPLATE) {
            this.plugin.getMissionManager().nextMission(player);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        OnboardingPlayer op = this.plugin.getMissionManager().getOnboardingPlayer(player);
        if (op.isFinished()) {
            return;
        }
        String message = event.getMessage().toLowerCase();
        if (op.getCurrentMission() == 3 && (message.startsWith("/g ") || message.equals("/g"))) {
            this.plugin.getMissionManager().nextMission(player);
        }
        if (op.getCurrentMission() == 4 && (message.startsWith("/clan ") || message.startsWith("/clans "))) {
            this.plugin.getServer().getScheduler().runTaskLater((Plugin)this.plugin, () -> this.plugin.getMissionManager().checkClanMission(player), 20L);
        }
    }
}
