package com.gabrielhd.practice.scoreboard;

import com.bizarrealex.aether.scoreboard.Board;
import com.bizarrealex.aether.scoreboard.BoardAdapter;
import com.bizarrealex.aether.scoreboard.cooldown.BoardCooldown;
import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.cache.StatusCache;
import com.gabrielhd.practice.config.YamlConfig;
import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.events.EventPlayer;
import com.gabrielhd.practice.events.EventState;
import com.gabrielhd.practice.events.types.Sumo;
import com.gabrielhd.practice.match.Match;
import com.gabrielhd.practice.party.Party;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.queue.QueueEntry;
import com.gabrielhd.practice.queue.QueueType;
import com.gabrielhd.practice.settings.item.ProfileOptionsItemState;
import com.gabrielhd.practice.utils.others.PlayerUtil;
import com.gabrielhd.practice.utils.text.Color;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class PracticeBoard implements BoardAdapter {

    private final Practice plugin;
    private final YamlConfig boardConfig;
    
    public PracticeBoard() {
        this.plugin = Practice.getInstance();

        this.boardConfig = new YamlConfig(this.plugin, "Scoreboard");
    }
    
    @Override
    public String getTitle(Player player) {
        PlayerData playerData = PlayerData.of(player);

        if(playerData == null || playerData.getOptions().getScoreboard() == ProfileOptionsItemState.DISABLED) return "";

        switch (playerData.getPlayerState()) {
            case SPECTATING:
            case FIGHTING: {
                return boardConfig.getString("Scoreboard.Game.Title");
            }
            case QUEUE: {
                return boardConfig.getString("Scoreboard.Queue.Title");
            }
            case EVENT: {
                return boardConfig.getString("Scoreboard.Event.Title");
            }
            case FFA: {
                return boardConfig.getString("Scoreboard.FFA.Title");
            }
            default: {
                return boardConfig.getString("Scoreboard.Lobby.Title");
            }
        }
    }
    
    @Override
    public List<String> getScoreboard(Player player, Board board, Set<BoardCooldown> cooldowns) {
        PlayerData playerData = PlayerData.of(player);
        if (playerData == null || playerData.getOptions().getScoreboard() == ProfileOptionsItemState.DISABLED) return null;

        switch (playerData.getPlayerState()) {
            case SPECTATING:
            case FIGHTING: {
                return this.getGameBoard(player);
            }
            case QUEUE: {
                return this.getQueueBoard(player);
            }
            case EVENT: {
                return this.getEventBoard(player);
            }
            case FFA: {
                return this.getFFABoard(player);
            }
            default: {
                if(Party.of(player.getUniqueId()) != null) {
                    return this.getPartyBoard(player);
                }

                return this.getLobbyBoard(player);
            }
        }
    }
    
    private List<String> getLobbyBoard(Player player) {
        List<String> lines = new ArrayList<>();

        TextPlaceholders placeholders = new TextPlaceholders();
        placeholders.set("%player%", player.getName());
        placeholders.set("%online%", Bukkit.getOnlinePlayers().size());
        placeholders.set("%ingame%", StatusCache.getInstance().getFighting());
        placeholders.set("%waiting%", StatusCache.getInstance().getQueueing());

        for(String line : boardConfig.getStringList("Scoreboard.Lobby.Lines")) {
            if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                line = PlaceholderAPI.setPlaceholders(player, line);
            }

            line = placeholders.parse(line);

            lines.add(Color.text(line));
        }

        return lines;
    }

    private List<String> getQueueBoard(Player player) {
        List<String> lines = new ArrayList<>();

        PlayerData playerData = PlayerData.of(player);

        TextPlaceholders placeholders = new TextPlaceholders();
        placeholders.set("%player%", player.getName());
        placeholders.set("%online%", Bukkit.getOnlinePlayers().size());
        placeholders.set("%ingame%", StatusCache.getInstance().getFighting());
        placeholders.set("%waiting%", StatusCache.getInstance().getQueueing());

        Party party = Party.of(player.getUniqueId());

        QueueEntry queueEntry = this.plugin.getQueueManager().getQueueEntry(player.getUniqueId());
        if(queueEntry == null) return this.getLobbyBoard(player);

        placeholders.set("%kit%", queueEntry.getKitName());
        placeholders.set("%type%", queueEntry.getQueueType());

        String eloRangeString = "";

        if(queueEntry.getQueueType().isRanked()) {
            long queueTime = System.currentTimeMillis() - ((party == null) ? this.plugin.getQueueManager().getPlayerQueueTime(player.getUniqueId()) : this.plugin.getQueueManager().getPlayerQueueTime(party.getLeader()));
            int eloRange = playerData.getEloRange();
            int seconds = Math.round(queueTime / 1000L);
            if (seconds > 5 && eloRange != -1) {
                eloRange += seconds * 50;
                if (eloRange >= 3000) {
                    eloRange = 3000;
                }
            }
            int elo = 1000;
            if (queueEntry.getQueueType() == QueueType.RANKED) {
                elo = playerData.getElo(queueEntry.getKitName());
            }
            eloRangeString = "[" + Math.max(elo - eloRange / 2, 0) + " -> " + Math.max(elo + eloRange / 2, 0) + "]";
        }
        placeholders.set("%elo-range%", eloRangeString);

        if(party == null) {
            for (String line : boardConfig.getStringList("Scoreboard.Queue.Normal")) {
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    line = PlaceholderAPI.setPlaceholders(player, line);
                }

                line = placeholders.parse(line);

                lines.add(Color.text(line));
            }
        } else {
            placeholders.set("%leader%", Bukkit.getPlayer(party.getLeader()).getName());
            placeholders.set("%amount%", party.getMembers().size());

            for (String line : boardConfig.getStringList("Scoreboard.Queue.Party")) {
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    line = PlaceholderAPI.setPlaceholders(player, line);
                }

                line = placeholders.parse(line);

                lines.add(Color.text(line));
            }
        }

        return lines;
    }

    private List<String> getEventBoard(Player player) {
        List<String> lines = new ArrayList<>();

        TextPlaceholders placeholders = new TextPlaceholders();
        placeholders.set("%player%", player.getName());
        placeholders.set("%online%", Bukkit.getOnlinePlayers().size());

        CustomEvent event = this.plugin.getEventManager().getEventPlaying(player);
        if (this.plugin.getEventManager().getSpectators().containsKey(player.getUniqueId())) {
            event = this.plugin.getEventManager().getSpectators().get(player.getUniqueId());
        }
        if(event == null) return this.getLobbyBoard(player);

        Sumo sumo = (Sumo) event;

        placeholders.set("%event%", event.getName());
        placeholders.set("%host%", event.getHost());
        placeholders.set("%limit%", sumo.getMaxPlayers());
        placeholders.set("%playing%", sumo.getByState(EventPlayer.PlayerState.WAITING).size());

        int countdown = sumo.getCountdownTask().getTimeUntilStart();
        if(countdown > 0 && event.getState() == EventState.WAITING) {
            placeholders.set("%countdown%", countdown);
        }

        placeholders.set("%status%", StringUtils.capitalize(event.getPlayer(player.getUniqueId()).getState().name().toLowerCase(Locale.ROOT)));

        if(sumo.getFighting().size() > 0) {
            int i = 1;
            for(Player fighters : sumo.getFighting().stream().map(Bukkit::getPlayer).collect(Collectors.toList())) {
                placeholders.set("%fighter-" + i + "%", fighters.getName());
                placeholders.set("%fighter-" + i + "-ping%", PlayerUtil.getPing(fighters));

                i++;
            }
        }

        if(event.getState() == EventState.WAITING) {
            for(String line : boardConfig.getStringList("Scoreboard.Event.Waiting")) {
                if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    line = PlaceholderAPI.setPlaceholders(player, line);
                }

                line = placeholders.parse(line);

                lines.add(Color.text(line));
            }
        } else if(event.getState() == EventState.STARTED) {
            for(String line : boardConfig.getStringList("Scoreboard.Event.Ingame")) {
                if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    line = PlaceholderAPI.setPlaceholders(player, line);
                }

                line = placeholders.parse(line);

                lines.add(Color.text(line));
            }
        }

        return lines;
    }

    private List<String> getFFABoard(Player player) {
        List<String> lines = new ArrayList<>();

        PlayerData playerData = PlayerData.of(player);
        if(playerData == null || this.plugin.getMatchManager().getMatch(playerData) == null) return this.getLobbyBoard(player);

        Match match = this.plugin.getMatchManager().getMatch(playerData);

        TextPlaceholders placeholders = new TextPlaceholders();
        placeholders.set("%player%", player.getName());
        placeholders.set("%kit%", match.getKit().getName());
        placeholders.set("%online%", Bukkit.getOnlinePlayers().size());
        placeholders.set("%ping%", (playerData.getOptions().getScoreboard() == ProfileOptionsItemState.SHOW_PING ? PlayerUtil.getPing(player) : ""));

        Party party = Party.of(player.getUniqueId());

        if(match.isPartyMatch()) {
            placeholders.set("%leader%", Bukkit.getPlayer(party.getLeader()).getName());
            placeholders.set("%amount%", party.getMembers().size());

            for (String line : boardConfig.getStringList("Scoreboard.FFA.Party")) {
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    line = PlaceholderAPI.setPlaceholders(player, line);
                }

                line = placeholders.parse(line);

                lines.add(Color.text(line));
            }
        } else {
            for (String line : boardConfig.getStringList("Scoreboard.FFA.Normal")) {
                if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    line = PlaceholderAPI.setPlaceholders(player, line);
                }

                line = placeholders.parse(line);

                lines.add(Color.text(line));
            }
        }

        return lines;
    }

    private List<String> getPartyBoard(Player player) {
        List<String> lines = new ArrayList<>();

        Party party = Party.of(player.getUniqueId());

        TextPlaceholders placeholders = new TextPlaceholders();
        placeholders.set("%player%", player.getName());
        placeholders.set("%online%", Bukkit.getOnlinePlayers().size());
        placeholders.set("%ingame%", StatusCache.getInstance().getFighting());
        placeholders.set("%waiting%", StatusCache.getInstance().getQueueing());
        placeholders.set("%leader%", Bukkit.getPlayer(party.getLeader()).getName());
        placeholders.set("%amount%", party.getMembers().size());

        for(String line : boardConfig.getStringList("Scoreboard.Party.Lines")) {
            if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                line = PlaceholderAPI.setPlaceholders(player, line);
            }

            line = placeholders.parse(line);

            lines.add(Color.text(line));
        }

        return lines;
    }
    
    private List<String> getGameBoard(Player player) {
        List<String> lines = new ArrayList<>();

        PlayerData playerData = PlayerData.of(player);
        if(playerData == null || this.plugin.getMatchManager().getMatch(playerData) == null) return this.getLobbyBoard(player);

        Match match = this.plugin.getMatchManager().getMatch(playerData);

        Player opponent = ((match.getTeams().get(0).getPlayers().get(0) == player.getUniqueId()) ? Bukkit.getPlayer(match.getTeams().get(1).getPlayers().get(0)) : Bukkit.getPlayer(match.getTeams().get(0).getPlayers().get(0)));

        TextPlaceholders placeholders = new TextPlaceholders();
        placeholders.set("%player%", player.getName());
        placeholders.set("%kit%", match.getKit().getName());
        placeholders.set("%opponent%", opponent.getName());
        placeholders.set("%online%", Bukkit.getOnlinePlayers().size());
        placeholders.set("%ping%", (playerData.getOptions().getScoreboard() == ProfileOptionsItemState.SHOW_PING ? PlayerUtil.getPing(player) : ""));
        placeholders.set("%ping-opponent%", (playerData.getOptions().getScoreboard() == ProfileOptionsItemState.SHOW_PING ? PlayerUtil.getPing(opponent) : ""));

        for(String line : boardConfig.getStringList("Scoreboard.Game.Lines")) {
            if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                line = PlaceholderAPI.setPlaceholders(player, line);
            }

            line = placeholders.parse(line);

            lines.add(Color.text(line));
        }

        return lines;
    }
}
