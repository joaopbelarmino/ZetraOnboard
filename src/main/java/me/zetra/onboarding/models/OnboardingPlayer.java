package me.zetra.onboarding.models;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class OnboardingPlayer {
    private final UUID uuid;
    private int currentMission;
    private boolean finished;
    private BossBar bossBar;

    public OnboardingPlayer(UUID uuid, int currentMission, boolean finished) {
        this.uuid = uuid;
        this.currentMission = currentMission;
        this.finished = finished;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public int getCurrentMission() {
        return this.currentMission;
    }

    public void setCurrentMission(int currentMission) {
        this.currentMission = currentMission;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public BossBar getBossBar() {
        return this.bossBar;
    }

    public void createBossBar(String title) {
        if (this.bossBar != null) {
            this.bossBar.removeAll();
        }
        this.bossBar = Bukkit.createBossBar((String)title, (BarColor)BarColor.YELLOW, (BarStyle)BarStyle.SOLID, (BarFlag[])new BarFlag[0]);
        Player player = Bukkit.getPlayer((UUID)this.uuid);
        if (player != null) {
            this.bossBar.addPlayer(player);
            this.bossBar.setVisible(true);
        }
    }

    public void removeBossBar() {
        if (this.bossBar != null) {
            this.bossBar.removeAll();
            this.bossBar = null;
        }
    }
}
