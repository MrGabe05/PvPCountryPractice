package com.gabrielhd.practice.events.types;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.events.EventPlayer;
import com.gabrielhd.practice.events.EventType;
import com.gabrielhd.practice.events.tasks.EventCountdownTask;
import com.gabrielhd.practice.events.tasks.SumoCountdownTask;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.utils.others.PlayerUtil;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
public class Sumo extends CustomEvent {

    private final Location sumoFirst;
    private final Location sumoSecond;

    private final Set<UUID> fighting;

    private WaterCheckTask waterCheckTask;
    private final SumoCountdownTask countdownTask;

    public Sumo(Location spawn, Location sumoFirst, Location sumoSecond) {
        super("Sumo", spawn, EventType.SUMO);

        this.sumoFirst = sumoFirst;
        this.sumoSecond = sumoSecond;

        this.fighting = new HashSet<>();

        this.countdownTask = new SumoCountdownTask(this);
    }

    @Override
    public List<Location> getSpawnLocations() {
        return Collections.singletonList(this.getSpawn());
    }

    @Override
    public EventCountdownTask getCountdownTask() {
        return this.countdownTask;
    }

    @Override
    public void onStart() {
        (this.waterCheckTask = new WaterCheckTask()).runTaskTimer(Practice.getInstance(), 0L, 10L);
        this.selectPlayers();
    }

    @Override
    public Consumer<Player> onDeath() {
        return player -> {
            EventPlayer data = this.getPlayer(player.getUniqueId());
            if (data != null && !this.fighting.isEmpty()) {
                if (data.getState() == EventPlayer.PlayerState.FIGHTING || data.getState() == EventPlayer.PlayerState.PREPARING) {
                    UUID uuidKiller = getOponent(player);
                    Player killer = Bukkit.getPlayer(uuidKiller);
                    EventPlayer killerData = this.getPlayer(uuidKiller);

                    PlayerData playerData = PlayerData.of(player);
                    if (playerData != null) {
                        playerData.getEventsLosses().put(EventType.SUMO, playerData.getEventsLosses().getOrDefault(EventType.SUMO, 0) + 1);
                    }

                    data.setState(EventPlayer.PlayerState.ELIMINATED);
                    killerData.setState(EventPlayer.PlayerState.WAITING);

                    PlayerUtil.clearPlayer(player);
                    Practice.getInstance().getPlayerManager().giveLobbyItems(player);
                    PlayerUtil.clearPlayer(killer);
                    Practice.getInstance().getPlayerManager().giveLobbyItems(killer);

                    if (this.getSpawnLocations().size() == 1) {
                        player.teleport(this.getSpawnLocations().get(0));
                        killer.teleport(this.getSpawnLocations().get(0));
                    }

                    this.sendMessage(Lang.EVENT_PLAYER_ELIMINATED, new TextPlaceholders().set("%player%", player.getName()).set("%killer%", killer.getName()));

                    Bukkit.getScheduler().runTaskLater(Practice.getInstance(), this::selectPlayers, 60L);
                }
            }
        };
    }

    public UUID getOponent(Player player) {
        for(UUID uuid : this.fighting) {
            if(!uuid.equals(player.getUniqueId())) {
                return uuid;
            }
        }
        return null;
    }

    private void selectPlayers() {
        List<UUID> players = this.getByState(EventPlayer.PlayerState.WAITING);

        if (players.size() == 1) {
            Player winner = Bukkit.getPlayer(players.get(0));
            if(winner == null) return;

            PlayerData winnerData = PlayerData.of(winner);
            if(winnerData == null) return;

            winnerData.getEventsWins().put(EventType.SUMO, winnerData.getEventsWins().getOrDefault(EventType.SUMO, 0) + 1);

            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(Lang.WINNING_EVENT.get(player, new TextPlaceholders().set("%winner%", winner.getName()))));

            this.fighting.clear();
            this.end();
            return;
        }

        Player picked1 = this.getRandomPlayer();
        Player picked2 = this.getRandomPlayer();
        if (picked1 == null || picked2 == null) {
            this.selectPlayers();
            return;
        }

        this.sendMessage(Lang.SELECTING_EVENT_PLAYERS, new TextPlaceholders());

        this.fighting.clear();

        this.fighting.add(picked1.getUniqueId());
        this.fighting.add(picked2.getUniqueId());

        PlayerUtil.clearPlayer(picked1);
        PlayerUtil.clearPlayer(picked2);

        picked1.showPlayer(picked2);
        picked2.showPlayer(picked1);
        picked1.teleport(this.sumoFirst);
        picked2.teleport(this.sumoSecond);

        for (Player other : this.getBukkitPlayers()) {
            if (other != null) {
                other.showPlayer(picked1);
                other.showPlayer(picked2);
            }
        }

        for (UUID spectatorUUID : Practice.getInstance().getEventManager().getSpectators().keySet()) {
            Player spectator = Bukkit.getPlayer(spectatorUUID);
            if (spectatorUUID != null) {
                spectator.showPlayer(picked1);
                spectator.showPlayer(picked2);
            }
        }

        this.sendMessage(Lang.STARTING_EVENT_FIGHT, new TextPlaceholders().set("%player_1%", picked1.getName()).set("%player_2%", picked2.getName()));
    }

    private Player getRandomPlayer() {
        List<UUID> waiting = this.getByState(EventPlayer.PlayerState.WAITING);
        if (waiting.isEmpty()) {
            return null;
        }

        Collections.shuffle(waiting);
        UUID uuid = waiting.get(ThreadLocalRandom.current().nextInt(waiting.size()));

        this.getPlayer(uuid).setState(EventPlayer.PlayerState.PREPARING);

        return Bukkit.getPlayer(uuid);
    }

    public List<UUID> getByState(EventPlayer.PlayerState state) {
        return this.getParticipants().values().stream().filter(player -> player.getState() == state).map(EventPlayer::getUuid).collect(Collectors.toList());
    }

    public class WaterCheckTask extends BukkitRunnable {

        @Override
        public void run() {
            if (Sumo.this.getParticipants().size() <= 1) {
                return;
            }

            Sumo.this.getBukkitPlayers().forEach(player -> {
                if (Sumo.this.getPlayer(player.getUniqueId()) == null || Sumo.this.getPlayer(player.getUniqueId()).getState() == EventPlayer.PlayerState.FIGHTING) {
                    Block legs = player.getLocation().getBlock();
                    Block head = legs.getRelative(BlockFace.UP);
                    if (legs.getType() == Material.WATER || legs.getType() == Material.STATIONARY_WATER || head.getType() == Material.WATER || head.getType() == Material.STATIONARY_WATER) {
                        Sumo.this.onDeath().accept(player);
                    }
                }
            });
        }
    }
}
