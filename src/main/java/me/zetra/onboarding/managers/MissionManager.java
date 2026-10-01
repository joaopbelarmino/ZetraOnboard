package me.zetra.onboarding.managers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import me.zetra.onboarding.ZetraOnboarding;
import me.zetra.onboarding.models.OnboardingPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;

public class MissionManager {
    private final ZetraOnboarding plugin;
    private final Map<UUID, OnboardingPlayer> players = new HashMap<UUID, OnboardingPlayer>();
    private final File dataFile;
    private FileConfiguration dataConfig;

    public MissionManager(ZetraOnboarding plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        this.loadData();
    }

    private void loadData() {
        if (!this.dataFile.exists()) {
            try {
                this.dataFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.dataConfig = YamlConfiguration.loadConfiguration((File)this.dataFile);
    }

    public void saveData() {
        try {
            this.dataConfig.save(this.dataFile);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public OnboardingPlayer getOnboardingPlayer(Player player) {
        if (this.players.containsKey(player.getUniqueId())) {
            return this.players.get(player.getUniqueId());
        }
        String path = "players." + String.valueOf(player.getUniqueId());
        if (this.dataConfig.contains(path)) {
            int mission = this.dataConfig.getInt(path + ".mission");
            boolean finished = this.dataConfig.getBoolean(path + ".finished");
            OnboardingPlayer op = new OnboardingPlayer(player.getUniqueId(), mission, finished);
            this.players.put(player.getUniqueId(), op);
            return op;
        }
        boolean isTrulyNew = !player.hasPlayedBefore();
        OnboardingPlayer op = new OnboardingPlayer(player.getUniqueId(), 1, !isTrulyNew);
        this.players.put(player.getUniqueId(), op);
        this.savePlayer(op);
        return op;
    }

    public void savePlayer(OnboardingPlayer op) {
        String path = "players." + String.valueOf(op.getUuid());
        this.dataConfig.set(path + ".mission", (Object)op.getCurrentMission());
        this.dataConfig.set(path + ".finished", (Object)op.isFinished());
        this.saveData();
    }

    public void nextMission(Player player) {
        OnboardingPlayer op = this.getOnboardingPlayer(player);
        if (op.isFinished()) {
            return;
        }
        int current = op.getCurrentMission();
        this.giveRewards(player, current);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        player.sendTitle("\u00a7a\u00a7lMISS\u00c3O CONCLU\u00cdDA", "\u00a7fVoc\u00ea avan\u00e7ou no tutorial!", 10, 40, 10);
        String msg = this.plugin.getConfig().getString("messages.mission-complete", "\u00a7a\u2714 Miss\u00e3o conclu\u00edda!");
        player.sendMessage(msg.replace("&", "\u00a7"));
        if (current >= 4) {
            this.finishOnboarding(player);
        } else {
            op.setCurrentMission(current + 1);
            this.updateBossBar(player);
            this.savePlayer(op);
            if (op.getCurrentMission() == 2) {
                Bukkit.getScheduler().runTaskLater((Plugin)this.plugin, () -> this.checkInventoryMission(player), 5L);
            }
        }
    }

    public void finishOnboarding(Player player) {
        OnboardingPlayer op = this.getOnboardingPlayer(player);
        op.setFinished(true);
        op.removeBossBar();
        this.savePlayer(op);
        player.sendTitle("\u00a76\u00a7lPARAB\u00c9NS!", "\u00a7eTutorial conclu\u00eddo com sucesso!", 10, 70, 20);
        String finishMsg = this.plugin.getConfig().getString("messages.onboarding-finished", "\u00a76\u2728 Parab\u00e9ns! Voc\u00ea concluiu todo o tutorial!");
        player.sendMessage(finishMsg.replace("&", "\u00a7"));
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        this.spawnFirework(player);
    }

    public void updateBossBar(Player player) {
        OnboardingPlayer op = this.getOnboardingPlayer(player);
        if (op.isFinished()) {
            op.removeBossBar();
            return;
        }
        String missionKey = "m" + op.getCurrentMission();
        String title = this.plugin.getConfig().getString("missions." + missionKey + ".bossbar", "Miss\u00e3o " + op.getCurrentMission());
        op.createBossBar(title.replace("&", "\u00a7"));
    }

    private void giveRewards(Player player, int missionNumber) {
        List<String> commands = this.plugin.getConfig().getStringList("missions.m" + missionNumber + ".reward-commands");
        for (String cmd : commands) {
            String processed = cmd.replace("%player%", player.getName());
            Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)processed);
        }
    }

    public void checkClanMission(Player player) {
        String clan;
        OnboardingPlayer op = this.getOnboardingPlayer(player);
        if (!(op.isFinished() || op.getCurrentMission() != 4 || (clan = PlaceholderAPI.setPlaceholders((Player)player, (String)"%simpleclans_clan_name%")) == null || clan.isEmpty() || clan.equalsIgnoreCase("none") || clan.equalsIgnoreCase(""))) {
            this.nextMission(player);
        }
    }

    public void checkInventoryMission(Player player) {
        OnboardingPlayer op = this.getOnboardingPlayer(player);
        if (!op.isFinished() && op.getCurrentMission() == 2) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item == null || item.getType() != Material.IRON_CHESTPLATE) continue;
                this.nextMission(player);
                return;
            }
            ItemStack chest = player.getInventory().getChestplate();
            if (chest != null && chest.getType() == Material.IRON_CHESTPLATE) {
                this.nextMission(player);
            }
        }
    }

    private void spawnFirework(Player player) {
        Firework fw = (Firework)player.getWorld().spawn(player.getLocation(), Firework.class);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.addEffect(FireworkEffect.builder().withColor(new Color[]{Color.ORANGE, Color.YELLOW, Color.fromRGB((int)255, (int)215, (int)0)}).with(FireworkEffect.Type.BALL_LARGE).flicker(true).build());
        fwm.setPower(1);
        fw.setFireworkMeta(fwm);
    }

    public Map<UUID, OnboardingPlayer> getActivePlayers() {
        return this.players;
    }

    public void unloadPlayer(Player player) {
        OnboardingPlayer op = this.players.remove(player.getUniqueId());
        if (op != null) {
            op.removeBossBar();
        }
    }
}
