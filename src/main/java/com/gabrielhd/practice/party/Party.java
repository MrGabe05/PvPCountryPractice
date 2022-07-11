package com.gabrielhd.practice.party;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.match.MatchTeam;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.utils.text.Color;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Stream;

@Getter @Setter
public class Party {

    private static final Map<UUID, Party> parties = new HashMap<>();
    private static final Map<UUID, UUID> partyLeaders = new HashMap<>();

    private UUID leader;
    private int limit;
    private boolean open;

    private final Set<UUID> members;
    private final Set<UUID> partyInvites;

    public Party(UUID leader) {
        this.leader = leader;
        this.limit = 50;

        this.members = new HashSet<>();
        this.members.add(leader);

        this.partyInvites = new HashSet<>();
    }

    public boolean isLeader(Player player) {
        return this.leader.equals(player.getUniqueId());
    }

    public boolean isInParty(Player player) {
        return this.members.contains(player.getUniqueId());
    }

    public void removePartyInvites(UUID uuid) {
        this.partyInvites.remove(uuid);
    }

    public boolean hasPartyInvite(UUID player) {
        return this.partyInvites.contains(player);
    }

    public void createPartyInvite(UUID requested) {
        this.partyInvites.add(requested);
    }

    public void joinPlayer(Player player) {
        if(this.members.add(player.getUniqueId())) {
            partyLeaders.put(player.getUniqueId(), leader);

            Practice.getInstance().getPlayerManager().sendToSpawnAndReset(player);

            broadcast(Lang.PARTY_JOIN, new TextPlaceholders().set("%player%", player.getName()));
        }
    }

    public void leaveParty(Player player) {
        if(this.members.remove(player.getUniqueId())) {
            PlayerData playerData = PlayerData.of(player);
            if (parties.containsKey(player.getUniqueId())) {
                this.disbandParty();
            } else {
                broadcast(Lang.PARTY_LEAVE, new TextPlaceholders().set("%player%", player.getName()));

                partyLeaders.remove(player.getUniqueId());
            }

            switch (playerData.getPlayerState()) {
                case FIGHTING: {
                    Practice.getInstance().getMatchManager().removeFighter(player, playerData, false);
                    break;
                }
                case SPECTATING: {
                    if (Practice.getInstance().getEventManager().getSpectators().containsKey(player.getUniqueId())) {
                        Practice.getInstance().getEventManager().removeSpectator(player);
                        break;
                    }
                    Practice.getInstance().getMatchManager().removeSpectator(player);
                    break;
                }
            }
            Practice.getInstance().getPlayerManager().sendToSpawnAndReset(player);
        }
    }

    public void broadcast(String message) {
        this.members().forEach(member -> member.sendMessage(Color.text(message)));
    }

    public void broadcast(Lang message, TextPlaceholders textPlaceholders) {
        this.members().forEach(member -> message.send(member, textPlaceholders));
    }

    private void disbandParty() {
        parties.remove(this.leader);

        broadcast(Lang.PARTY_DISBAND, new TextPlaceholders());

        this.members().forEach(member -> {
            PlayerData memberData = PlayerData.of(member);

            if (partyLeaders.get(memberData.getUuid()) != null) {
                partyLeaders.remove(memberData.getUuid());
            }

            if (memberData.getPlayerState() == PlayerState.SPAWN) {
                Practice.getInstance().getPlayerManager().sendToSpawnAndReset(member);
            }
        });
    }

    public MatchTeam[] split() {
        List<UUID> teamA = new ArrayList<>();
        List<UUID> teamB = new ArrayList<>();
        for (UUID member : this.members) {
            if (teamA.size() == teamB.size()) {
                teamA.add(member);
            } else {
                teamB.add(member);
            }
        }
        return new MatchTeam[] { new MatchTeam(teamA.get(0), teamA, 0), new MatchTeam(teamB.get(0), teamB, 1) };
    }

    public Stream<Player> members() {
        return this.members.stream().map(Bukkit::getPlayer).filter(Objects::nonNull);
    }

    public static Party of(UUID player) {
        if (parties.containsKey(player)) {
            return parties.get(player);
        }
        if (partyLeaders.containsKey(player)) {
            UUID leader = partyLeaders.get(player);
            return parties.get(leader);
        }
        return null;
    }

    public static Party create(Player player) {
        Party party = new Party(player.getUniqueId());

        parties.put(player.getUniqueId(), party);

        Practice.getInstance().getPlayerManager().sendToSpawnAndReset(player);

        Lang.PARTY_CREATED.send(player, new TextPlaceholders());

        return parties.get(player.getUniqueId());
    }
}
