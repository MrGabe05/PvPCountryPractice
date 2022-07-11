package com.gabrielhd.practice.tasks;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.utils.timer.impl.EnderpearlTimer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ExpBarRunnable implements Runnable
{
    private final Practice plugin;
    
    @Override
    public void run() {
        final EnderpearlTimer timer = Practice.getInstance().getTimerManager().getTimer(EnderpearlTimer.class);
        for (final UUID uuid : timer.getCooldowns().keySet()) {
            final Player player = this.plugin.getServer().getPlayer(uuid);
            if (player != null) {
                final long time = timer.getRemaining(player);
                final int seconds = (int)Math.round(time / 1000.0);
                player.setLevel(seconds);
                player.setExp(time / 15000.0f);
            }
        }
    }
    
    public ExpBarRunnable() {
        this.plugin = Practice.getInstance();
    }
}
