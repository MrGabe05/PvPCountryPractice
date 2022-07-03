package com.gabrielhd.practice.event;

import com.gabrielhd.practice.events.CustomEvent;

public class EventStartEvent extends BaseEvent {

    private final CustomEvent event;
    
    public CustomEvent getEvent() {
        return this.event;
    }
    
    public EventStartEvent(CustomEvent event) {
        this.event = event;
    }
}
