package com.gabrielhd.practice.utils.timer;

import com.gabrielhd.practice.Practice;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

@Getter @Setter
public class TimerCooldown {

    private final Timer timer;
    private final UUID owner;

    private long expiryMillis;
    private long pauseMillis;

    private BukkitTask eventNotificationTask;
    
    protected TimerCooldown(Timer timer, long duration) {
        this.owner = null;
        this.timer = timer;

        this.setRemaining(duration);
    }
    
    protected TimerCooldown(Timer timer, UUID playerUUID, long duration) {
        this.timer = timer;
        this.owner = playerUUID;

        this.setRemaining(duration);
    }
    
    public long getRemaining() {
        return this.getRemaining(false);
    }
    
    protected void setRemaining(long milliseconds) throws IllegalStateException {
        if (milliseconds <= 0L) {
            this.cancel();
            return;
        }
        long expiry = System.currentTimeMillis() + milliseconds;
        if (expiry != this.expiryMillis) {
            this.expiryMillis = expiry;
            if (this.eventNotificationTask != null) {
                this.eventNotificationTask.cancel();
            }
            long ticks = milliseconds / 50L;
            this.eventNotificationTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (TimerCooldown.this.timer instanceof PlayerTimer && TimerCooldown.this.owner != null) {
                        ((PlayerTimer)TimerCooldown.this.timer).handleExpiry(Practice.getInstance().getServer().getPlayer(TimerCooldown.this.owner), TimerCooldown.this.owner);
                    }
                }
            }.runTaskLaterAsynchronously(JavaPlugin.getProvidingPlugin(this.getClass()), ticks);
        }
    }
    
    protected long getRemaining(boolean ignorePaused) {
        if (!ignorePaused && this.pauseMillis != 0L) {
            return this.pauseMillis;
        }
        return this.expiryMillis - System.currentTimeMillis();
    }
    
    protected boolean isPaused() {
        return this.pauseMillis != 0L;
    }
    
    public void setPaused(boolean paused) {
        if (paused != this.isPaused()) {
            if (paused) {
                this.pauseMillis = this.getRemaining(true);
                this.cancel();
            }
            else {
                this.setRemaining(this.pauseMillis);
                this.pauseMillis = 0L;
            }
        }
    }
    
    protected void cancel() throws IllegalStateException {
        if (this.eventNotificationTask != null) {
            this.eventNotificationTask.cancel();
            this.eventNotificationTask = null;
        }
    }
}
