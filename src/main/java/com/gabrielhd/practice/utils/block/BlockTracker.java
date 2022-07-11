package com.gabrielhd.practice.utils.block;

import org.bukkit.Location;
import org.bukkit.block.BlockState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

public class BlockTracker {

    private final Set<Location> playerPlacedBlocks;
    private final LinkedList<BlockState> changeTrackers;

    public BlockTracker() {
        this.changeTrackers = new LinkedList<>();
        this.playerPlacedBlocks = new HashSet<>();
    }

    public synchronized void rollback() {
        BlockState blockState;
        while ((blockState = this.changeTrackers.pollLast()) != null) {
            blockState.update(true, false);
        }
        this.playerPlacedBlocks.clear();
    }

    public void add(BlockState blockState) {
        this.changeTrackers.add(blockState);
    }
}
