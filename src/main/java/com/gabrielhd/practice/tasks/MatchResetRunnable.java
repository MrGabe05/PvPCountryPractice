package com.gabrielhd.practice.tasks;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.match.Match;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchResetRunnable extends BukkitRunnable
{
    private final Practice plugin;
    private final Match match;
    
    @Override
    public void run() {
        this.match.getBlockTracker().rollback();
        this.match.getPlacedBlockLocations().clear();
        this.match.getArena().addAvailableArena(this.match.getStandArena());
        this.plugin.getArenaManager().removeArenaMatchUUID(this.match.getStandArena());
        this.cancel();
    }
    
    public MatchResetRunnable(Match match) {
        this.plugin = Practice.getInstance();
        this.match = match;
    }
}
