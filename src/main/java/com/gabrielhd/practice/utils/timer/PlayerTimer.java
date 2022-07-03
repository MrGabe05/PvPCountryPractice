package com.gabrielhd.practice.utils.timer;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public abstract class PlayerTimer extends Timer {
    
    @Getter protected Map<UUID, TimerCooldown> cooldowns;
    
    public PlayerTimer(String name, long defaultCooldown) {
        super(name, defaultCooldown);
        this.cooldowns = new ConcurrentHashMap<>();
    }
    
    protected void handleExpiry(Player player, UUID playerUUID) {
        this.cooldowns.remove(playerUUID);
    }
    
    public TimerCooldown clearCooldown(UUID uuid) {
        return this.clearCooldown(null, uuid);
    }
    
    public TimerCooldown clearCooldown(Player player) {
        Objects.requireNonNull(player);
        return this.clearCooldown(player, player.getUniqueId());
    }
    
    public TimerCooldown clearCooldown(Player player, UUID playerUUID) {
        TimerCooldown runnable = this.cooldowns.remove(playerUUID);
        if (runnable != null) {
            runnable.cancel();
        }
        return runnable;
    }
    
    public boolean isPaused(Player player) {
        return this.isPaused(player.getUniqueId());
    }
    
    public boolean isPaused(UUID playerUUID) {
        TimerCooldown runnable = this.cooldowns.get(playerUUID);
        return runnable != null && runnable.isPaused();
    }
    
    public void setPaused(UUID playerUUID, boolean paused) {
        TimerCooldown runnable = this.cooldowns.get(playerUUID);
        if (runnable != null && runnable.isPaused() != paused) {
            runnable.setPaused(paused);
        }
    }
    
    public long getRemaining(Player player) {
        return this.getRemaining(player.getUniqueId());
    }
    
    public long getRemaining(UUID playerUUID) {
        TimerCooldown runnable = this.cooldowns.get(playerUUID);
        return (runnable == null) ? 0L : runnable.getRemaining();
    }
    
    public boolean setCooldown(Player player, UUID playerUUID) {
        return this.setCooldown(player, playerUUID, this.defaultCooldown, false);
    }
    
    public boolean setCooldown(Player player, UUID playerUUID, long duration, boolean overwrite) {
        return this.setCooldown(player, playerUUID, duration, overwrite, null);
    }
    
    public boolean setCooldown(Player player, UUID playerUUID, long duration, boolean overwrite, Predicate<Long> currentCooldownPredicate) {
        TimerCooldown runnable = (duration > 0L) ? this.cooldowns.get(playerUUID) : this.clearCooldown(player, playerUUID);
        if (runnable == null) {
            runnable = new TimerCooldown(this, playerUUID, duration);
            this.cooldowns.put(playerUUID, runnable);
            return true;
        }
        long remaining = runnable.getRemaining();
        if (!overwrite && remaining > 0L && duration <= remaining) {
            return false;
        }
        boolean flag = true;
        if (currentCooldownPredicate != null) {
            flag = currentCooldownPredicate.test(remaining);
        }
        if (flag) {
            runnable.setRemaining(duration);
        }
        return flag;
    }
}
