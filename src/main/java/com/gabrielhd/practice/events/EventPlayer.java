package com.gabrielhd.practice.events;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class EventPlayer {

    private final UUID uuid;

    private PlayerState state;

    public EventPlayer(UUID uuid) {
        this.uuid = uuid;

        this.state = PlayerState.WAITING;
    }

    public enum PlayerState {
        WAITING,
        PREPARING,
        FIGHTING,
        ELIMINATED;
    }
}
