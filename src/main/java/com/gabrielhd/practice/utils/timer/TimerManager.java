package com.gabrielhd.practice.utils.timer;

import com.gabrielhd.practice.Practice;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.util.LinkedHashSet;
import java.util.Set;

public class TimerManager implements Listener {

    @Getter private final Set<Timer> timers;
    
    public TimerManager() {
        this.timers = new LinkedHashSet<>();
    }
    
    public void registerTimer(Timer timer) {
        this.timers.add(timer);

        if (timer instanceof Listener) {
            Bukkit.getServer().getPluginManager().registerEvents((Listener)timer, Practice.getInstance());
        }
    }
    
    public void unregisterTimer(Timer timer) {
        this.timers.remove(timer);
    }
    
    public <T extends Timer> T getTimer(Class<T> timerClass) {
        for (Timer timer : this.timers) {
            if (timer.getClass().equals(timerClass)) {
                return (T)timer;
            }
        }
        return null;
    }
}
