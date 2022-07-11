package com.gabrielhd.practice.events.tasks;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.utils.text.TextPlaceholders;

import java.util.Arrays;

public class SumoCountdownTask extends EventCountdownTask {

    public SumoCountdownTask(CustomEvent event) {
        super(event, 60);
    }
    
    @Override
    public boolean shouldAnnounce(int timeUntilStart) {
        return Arrays.asList(45, 30, 15, 10, 5).contains(timeUntilStart);
    }
    
    @Override
    public boolean canStart() {
        return this.getEvent().getPlayers().size() >= 4;
    }
    
    @Override
    public void onCancel() {
        this.getEvent().sendMessage(Lang.INSUFFICIENT_PLAYERS, new TextPlaceholders());
        this.getEvent().end();

        Practice.getInstance().getEventManager().setCooldown(0L);
    }
}
