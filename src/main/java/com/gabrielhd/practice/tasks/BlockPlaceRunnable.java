package com.gabrielhd.practice.tasks;

import com.boydti.fawe.util.EditSessionBuilder;
import com.boydti.fawe.util.TaskManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class BlockPlaceRunnable extends BukkitRunnable
{
    private final World world;
    private final ConcurrentMap<Location, Block> blocks;
    private final int totalBlocks;
    private final Iterator<Location> iterator;
    private final int blockIndex;
    private final int blocksPlaced;
    private boolean completed;
    
    public BlockPlaceRunnable(final World world, final Map<Location, Block> blocks) {
        this.blockIndex = 0;
        this.blocksPlaced = 0;
        this.completed = false;
        this.world = world;
        (this.blocks = new ConcurrentHashMap<>()).putAll(blocks);
        this.totalBlocks = blocks.keySet().size();
        this.iterator = blocks.keySet().iterator();
    }
    
    @Override
    public void run() {
        if (this.blocks.isEmpty() || !this.iterator.hasNext()) {
            this.finish();
            this.completed = true;
            this.cancel();
            return;
        }
        TaskManager.IMP.async(() -> {
            EditSession editSession = new EditSessionBuilder(this.world.getName()).fastmode(true).allowedRegionsEverywhere().autoQueue(false).limitUnlimited().build();
            Iterator<Map.Entry<Location, Block>> itera = this.blocks.entrySet().iterator();
            Map.Entry<Location, Block> entry;
            while (itera.hasNext()) {
                entry = itera.next();
                try {
                    editSession.setBlock(new Vector(entry.getKey().getBlockX(), entry.getKey().getBlockY(), entry.getKey().getZ()), new BaseBlock(entry.getValue().getTypeId(), entry.getValue().getData()));
                }
                catch (MaxChangedBlocksException ignored) {}
            }
            editSession.flushQueue();
            TaskManager.IMP.task(this.blocks::clear);
        });
    }
    
    public abstract void finish();
    
    public World getWorld() {
        return this.world;
    }
    
    public ConcurrentMap<Location, Block> getBlocks() {
        return this.blocks;
    }
    
    public int getTotalBlocks() {
        return this.totalBlocks;
    }
    
    public Iterator<Location> getIterator() {
        return this.iterator;
    }
    
    public int getBlockIndex() {
        return this.blockIndex;
    }
    
    public int getBlocksPlaced() {
        return this.blocksPlaced;
    }
    
    public boolean isCompleted() {
        return this.completed;
    }
}
