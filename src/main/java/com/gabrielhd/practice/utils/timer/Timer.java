package com.gabrielhd.practice.utils.timer;

import lombok.Getter;

@Getter
public abstract class Timer {

    protected final String name;
    protected final long defaultCooldown;
    
    public Timer(final String name, final long defaultCooldown) {
        this.name = name;
        this.defaultCooldown = defaultCooldown;
    }
}
