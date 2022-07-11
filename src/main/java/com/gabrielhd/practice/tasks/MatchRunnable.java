package com.gabrielhd.practice.tasks;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.match.Match;
import com.gabrielhd.practice.match.MatchState;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchRunnable extends BukkitRunnable {

    private final Practice plugin;
    private final Match match;
    
    @Override
    public void run() {
        switch (this.match.getMatchState()) {
            case STARTING: {
                if (this.match.decrementCountdown() != 0) {
                    //this.match.broadcast("§eLa pelea empieza en §f"+this.match.getCountdown()+"...");
                    break;
                }
                this.match.setMatchState(MatchState.FIGHTING);
                //this.match.broadcast("§6¡Pelea iniciada, Buena suerte!");
                break;
            }
            case SWITCHING: {
                if (this.match.decrementCountdown() == 0) {
                    this.match.getEntitiesToRemove().forEach(Entity::remove);
                    this.match.clearEntitiesToRemove();
                    this.match.setMatchState(MatchState.FIGHTING);
                    this.plugin.getMatchManager().pickPlayer(this.match);
                    break;
                }
                break;
            }
            case ENDING: {
                if (this.match.decrementCountdown() == 0) {
                    this.match.getRunnables().forEach(id -> this.plugin.getServer().getScheduler().cancelTask(id));
                    this.match.getEntitiesToRemove().forEach(Entity::remove);
                    this.match.getTeams().forEach(team -> team.alivePlayers().forEach(this.plugin.getPlayerManager()::sendToSpawnAndReset));
                    this.match.spectatorPlayers().forEach(this.plugin.getMatchManager()::removeSpectator);
                    this.plugin.getMatchManager().removeMatch(this.match);
                    if (this.match.getKit().isBuild()) {
                        new MatchResetRunnable(this.match).runTaskTimer(this.plugin, 20L, 20L);
                    }
                    this.cancel();
                    break;
                }
                break;
            }
        }
    }
    
    public MatchRunnable(final Match match) {
        this.plugin = Practice.getInstance();
        this.match = match;
    }
}
