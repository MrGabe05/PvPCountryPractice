package com.gabrielhd.practice.events;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.event.EventStartEvent;
import com.gabrielhd.practice.events.tasks.EventCountdownTask;
import com.gabrielhd.practice.events.types.Sumo;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.utils.others.PlayerUtil;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter @Setter
public abstract class CustomEvent<K extends EventPlayer> {

    private final String name;

    private Player host;

    private int minPlayers;
    private int maxPlayers;

    private EventState state;
    private EventType eventType;

    private Location max;
    private Location min;

    public CustomEvent(String name, EventType eventType) {
        this.name = name;
        this.eventType = eventType;
        this.minPlayers = 4;
        this.maxPlayers = 50;
    }

    public void startCountdown() {
        if (this.getCountdownTask().isEnded()) {
            this.getCountdownTask().setTimeUntilStart(this.getCountdownTask().getCountdownTime());
            this.getCountdownTask().setEnded(false);
        } else {
            this.getCountdownTask().runTaskTimerAsynchronously(Practice.getInstance(), 20L, 20L);
        }
    }

    public boolean joinEvent(Player player) {
        if(this.getSpawn() == null) {
            Lang.SUMO_SPAWN_NULL.send(player);
            return false;
        }
        if (this.getPlayers().size() >= this.maxPlayers) {
            Lang.SUMO_LIMIT.send(player);
            return false;
        }

        PlayerData playerData = PlayerData.of(player);
        playerData.setPlayerState(PlayerState.EVENT);

        PlayerUtil.clearPlayer(player);

        player.teleport(this.getSpawn());

        for (Player other : this.getBukkitPlayers()) {
            other.showPlayer(player);
            player.showPlayer(other);
        }

        this.sendMessage(Lang.JOINED_EVENT, new TextPlaceholders().set("%player%", player.getName()).set("%players%", this.getPlayers().size()));
        return true;
    }

    public void kill(Player player) {
        if (this.onDeath() != null) {
            this.onDeath().accept(player);
        }
    }

    public void leave(Player player) {
        if (this.onDeath() != null) {
            this.onDeath().accept(player);
        }
        this.getPlayers().remove(player.getUniqueId());
        Practice.getInstance().getPlayerManager().sendToSpawnAndReset(player);
    }

    public void start() {
        new EventStartEvent(this).call();

        this.setState(EventState.STARTED);
        this.onStart();

        Practice.getInstance().getEventManager().setCooldown(0L);
    }

    public void end() {
        Practice.getInstance().getEventManager().setCooldown(System.currentTimeMillis() + 300000L);

        if (this.eventType == EventType.SUMO) {
            Sumo sumoEvent = (Sumo) this;

            for (Player sPlayer : Bukkit.getOnlinePlayers()) {
                boolean inEvent = Practice.getInstance().getEventManager().getEventPlaying(sPlayer) != null;
                if (inEvent) {
                    Practice.getInstance().getPlayerManager().sendToSpawnEventFinish(sPlayer.getPlayer());
                }
            }
            if (sumoEvent.getWaterCheckTask() != null) {
                sumoEvent.getWaterCheckTask().cancel();
            }
        }

        this.getPlayers().clear();

        this.setState(EventState.UNANNOUNCED);

        Iterator<UUID> iterator = Practice.getInstance().getEventManager().getSpectators().keySet().iterator();
        while (iterator.hasNext()) {
            UUID spectatorUUID = iterator.next();
            Player spectator = Bukkit.getPlayer(spectatorUUID);
            if (spectator != null) {
                Practice.getInstance().getServer().getScheduler().runTask(Practice.getInstance(), () -> Practice.getInstance().getPlayerManager().sendToSpawnAndReset(spectator));
                iterator.remove();
            }
        }
        Practice.getInstance().getEventManager().getSpectators().clear();
        this.getCountdownTask().setEnded(true);
    }

    public Set<Player> getBukkitPlayers() {
        return this.getPlayers().keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public void sendMessage(Lang message, TextPlaceholders textPlaceholders) {
        this.getBukkitPlayers().forEach(player -> message.send(player, textPlaceholders));
    }

    public EventPlayer getPlayer(UUID uuid) {
        return this.getPlayers().get(uuid);
    }

    public abstract Map<UUID, K> getPlayers();

    public abstract EventCountdownTask getCountdownTask();

    public abstract Location getSpawn();

    public abstract void onStart();

    public abstract Consumer<Player> onJoin();

    public abstract Consumer<Player> onDeath();
}
