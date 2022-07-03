package com.gabrielhd.practice.manager;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.lang.Lang;
import com.gabrielhd.practice.party.Party;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import com.gabrielhd.practice.utils.maps.TtlHashMap;
import com.gabrielhd.practice.utils.text.TextPlaceholders;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PartyManager {

    private final Practice plugin;

    private final Map<UUID, Party> parties;
    private final Map<UUID, UUID> partyLeaders;
    private final Map<UUID, List<UUID>> partyInvites;

    public PartyManager() {
        this.plugin = Practice.getInstance();

        this.parties = new HashMap<>();
        this.partyLeaders = new HashMap<>();
        this.partyInvites = new TtlHashMap<>(TimeUnit.SECONDS, 15L);
    }

    public boolean isLeader(UUID uuid) {
        return this.parties.containsKey(uuid);
    }

    public void removePartyInvites(UUID uuid) {
        this.partyInvites.remove(uuid);
    }

    public boolean hasPartyInvite(UUID player, UUID other) {
        return this.partyInvites.get(player) != null && this.partyInvites.get(player).contains(other);
    }

    public void createPartyInvite(UUID requester, UUID requested) {
        this.partyInvites.computeIfAbsent(requested, k -> new ArrayList<>()).add(requester);
    }

    public boolean isInParty(UUID player, Party party) {
        Party targetParty = this.getParty(player);
        return targetParty != null && targetParty.getLeader() == party.getLeader();
    }

    public Party getParty(UUID player) {
        if (this.parties.containsKey(player)) {
            return this.parties.get(player);
        }
        if (this.partyLeaders.containsKey(player)) {
            UUID leader = this.partyLeaders.get(player);
            return this.parties.get(leader);
        }
        return null;
    }

    public void createParty(Player player) {
        Party party = new Party(player.getUniqueId());

        this.parties.put(player.getUniqueId(), party);
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);

        player.sendMessage(Lang.PARTY_CREATED.get(player, new TextPlaceholders()));
    }

    private void disbandParty(Party party) {
        this.parties.remove(party.getLeader());

        party.broadcast(Lang.PARTY_DISBAND, new TextPlaceholders());

        party.members().forEach(member -> {
            PlayerData memberData = PlayerData.of(member);

            if (this.partyLeaders.get(memberData.getUuid()) != null) {
                this.partyLeaders.remove(memberData.getUuid());
            }

            if (memberData.getPlayerState() == PlayerState.SPAWN) {
                this.plugin.getPlayerManager().sendToSpawnAndReset(member);
            }
        });
    }

    public void leaveParty(Player player) {
        Party party = this.getParty(player.getUniqueId());
        if (party == null) {
            return;
        }
        PlayerData playerData = PlayerData.of(player);
        if (this.parties.containsKey(player.getUniqueId())) {
            this.disbandParty(party);
        } else {
            party.broadcast(Lang.PARTY_LEAVE, new TextPlaceholders().set("%player%", player.getName()));
            party.removeMember(player.getUniqueId());

            this.partyLeaders.remove(player.getUniqueId());
        }

        switch (playerData.getPlayerState()) {
            case FIGHTING: {
                this.plugin.getMatchManager().removeFighter(player, playerData, false);
                break;
            }
            case SPECTATING: {
                if (this.plugin.getEventManager().getSpectators().containsKey(player.getUniqueId())) {
                    this.plugin.getEventManager().removeSpectator(player);
                    break;
                }
                this.plugin.getMatchManager().removeSpectator(player);
                break;
            }
        }
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);
    }

    public void joinParty(UUID leader, Player player) {
        Party party = this.getParty(leader);
        party.addMember(player.getUniqueId());

        this.partyLeaders.put(player.getUniqueId(), leader);
        this.plugin.getPlayerManager().sendToSpawnAndReset(player);

        party.broadcast(Lang.PARTY_JOIN, new TextPlaceholders().set("%player%", player.getName()));
    }
}
