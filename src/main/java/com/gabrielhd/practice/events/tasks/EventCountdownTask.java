package com.gabrielhd.practice.events.tasks;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.utils.text.Clickable;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

@Getter @Setter
public abstract class EventCountdownTask extends BukkitRunnable {

    private final CustomEvent event;
    private final int countdownTime;
    private int timeUntilStart;
    private boolean ended;
    
    public EventCountdownTask(CustomEvent event, int countdownTime) {
        this.event = event;

        this.countdownTime = countdownTime;
        this.timeUntilStart = countdownTime;
    }
    
    @Override
    public void run() {
        if (this.isEnded()) {
            return;
        }
        if (this.timeUntilStart <= 0) {
            if (this.canStart()) {
                Practice.getInstance().getServer().getScheduler().runTask(Practice.getInstance(), this.event::start);
            } else {
                Practice.getInstance().getServer().getScheduler().runTask(Practice.getInstance(), this::onCancel);
            }
            this.ended = true;
            return;
        }
        if (this.shouldAnnounce(this.timeUntilStart)) {
            Bukkit.getServer().getOnlinePlayers().forEach(player -> {
                String toSend = Lang.EVENT_BROADCAST.get(player, new TextPlaceholders().set("%player%", this.event.getHost().getName()).set("%timer%", this.event.getCountdownTask().getTimeUntilStart()));
                Clickable message = new Clickable(toSend, Lang.HOVER_BROADCAST_MESSAGE.get(player, new TextPlaceholders()), "/join " + this.event.getName());
            });
        }
        --this.timeUntilStart;
    }
    
    public abstract boolean shouldAnnounce(int p0);
    
    public abstract boolean canStart();
    
    public abstract void onCancel();
    
    private String getTime(int time) {
        StringBuilder timeStr = new StringBuilder();
        int minutes = 0;
        if (time % 60 == 0) {
            minutes = time / 60;
            time = 0;
        }
        else {
            while (time - 60 > 0) {
                ++minutes;
                time -= 60;
            }
        }
        if (minutes > 0) {
            timeStr.append(minutes).append("m");
        }
        if (time > 0) {
            timeStr.append((minutes > 0) ? " " : "").append(time).append("s");
        }
        return timeStr.toString();
    }
}
