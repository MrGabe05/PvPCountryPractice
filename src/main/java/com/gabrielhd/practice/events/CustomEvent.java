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
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter @Setter
public abstract class CustomEvent {

    private final String name;

    private Player host;

    private int minPlayers;
    private int maxPlayers;

    private Location spawn;

    private EventState state;
    private EventType eventType;

    private final Map<UUID, EventPlayer> participants;

    public CustomEvent(String name, Location spawn, EventType eventType) {
        this.name = name;
        this.spawn = spawn;
        this.eventType = eventType;
        this.minPlayers = 4;
        this.maxPlayers = 50;

        this.participants = new HashMap<>();
    }

    public void startCountdown() {
        if (this.getCountdownTask().isEnded()) {
            this.getCountdownTask().setTimeUntilStart(this.getCountdownTask().getCountdownTime());
            this.getCountdownTask().setEnded(false);
        }
        else {
            this.getCountdownTask().runTaskTimerAsynchronously(Practice.getInstance(), 20L, 20L);
        }
    }

    public boolean joinEvent(Player player) {
        if (this.participants.size() >= this.maxPlayers) {
            return false;
        }

        PlayerData playerData = PlayerData.of(player);
        playerData.setPlayerState(PlayerState.EVENT);

        PlayerUtil.clearPlayer(player);

        if (this.getSpawnLocations().size() == 1) {
            player.teleport(this.getSpawnLocations().get(0));
        } else {
            List<Location> spawnLocations = new ArrayList<>(this.getSpawnLocations());

            player.teleport(spawnLocations.remove(ThreadLocalRandom.current().nextInt(spawnLocations.size())));
        }

        for (Player other : this.getBukkitPlayers()) {
            other.showPlayer(player);
            player.showPlayer(other);
        }

        this.sendMessage(Lang.JOINED_EVENT, new TextPlaceholders().set("%player%", player.getName()).set("%players%", this.participants.size()));
        return true;
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
            Sumo sumoEvent = (Sumo)this;

            for(Player sPlayer : Bukkit.getOnlinePlayers()) {
                boolean inEvent = Practice.getInstance().getEventManager().getEventPlaying(sPlayer) != null;
                if(inEvent) {
                    Practice.getInstance().getPlayerManager().sendToSpawnEventFinish(sPlayer.getPlayer());
                }
            }
            if (sumoEvent.getWaterCheckTask() != null) {
                sumoEvent.getWaterCheckTask().cancel();
            }
        }

        this.getParticipants().clear();

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
        //this.getCountdownTask().setEnded(true);
    }

    public Set<Player> getBukkitPlayers() {
        return this.participants.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public void sendMessage(Lang message, TextPlaceholders textPlaceholders) {
        this.getBukkitPlayers().forEach(player -> player.sendMessage(message.get(player, textPlaceholders)));
    }

    public EventPlayer getPlayer(UUID uuid) {
        return this.participants.get(uuid);
    }

    public abstract EventCountdownTask getCountdownTask();

    public abstract List<Location> getSpawnLocations();

    public abstract void onStart();

    public abstract Consumer<Player> onDeath();
}
