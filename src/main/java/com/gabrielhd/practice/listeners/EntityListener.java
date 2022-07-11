package com.gabrielhd.practice.listeners;

import com.gabrielhd.practice.Practice;
import com.gabrielhd.practice.events.CustomEvent;
import com.gabrielhd.practice.events.EventPlayer;
import com.gabrielhd.practice.events.types.Sumo;
import com.gabrielhd.practice.match.Match;
import com.gabrielhd.practice.match.MatchState;
import com.gabrielhd.practice.player.PlayerData;
import com.gabrielhd.practice.player.PlayerState;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EntityListener implements Listener {

    private final Practice plugin;
    
    public EntityListener() {
        this.plugin = Practice.getInstance();
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player = (Player)e.getEntity();
            PlayerData playerData = PlayerData.of(player);
            switch (playerData.getPlayerState()) {
                case FIGHTING: {
                    Match match = this.plugin.getMatchManager().getMatch(playerData);
                    if (match.getMatchState() != MatchState.FIGHTING) {
                        e.setCancelled(true);
                    }
                    if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                        this.plugin.getMatchManager().removeFighter(player, playerData, true);
                    }
                    if (match.getKit().isParkour()) {
                        e.setCancelled(true);
                        break;
                    }
                    break;
                }
                case EVENT: {
                    CustomEvent event = this.plugin.getEventManager().getEventPlaying(player);
                    if (event == null) {
                        break;
                    }
                    if (event instanceof Sumo) {
                        Sumo sumoEvent = (Sumo)event;
                        EventPlayer sumoPlayer = sumoEvent.getPlayer(player.getUniqueId());
                        if (sumoPlayer != null) {
                            e.setCancelled(sumoPlayer.getState() != EventPlayer.PlayerState.FIGHTING);
                            break;
                        }
                        break;
                    }
                }
                default: {
                    if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                        if(!Practice.getInstance().getSpawnsLocation().isEmpty()) {
                            if (Practice.getInstance().getSpawnsLocation().size() == 1) {
                                player.teleport(Practice.getInstance().getSpawnsLocation().get(0));
                            }
                            else {
                                List<Location> spawnLocations = new ArrayList<>(Practice.getInstance().getSpawnsLocation());
                                player.teleport(spawnLocations.remove(ThreadLocalRandom.current().nextInt(spawnLocations.size())));
                            }
                        }
                    }
                    e.setCancelled(true);
                    break;
                }
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            e.setCancelled(true);
            return;
        }
        Player entity = (Player)e.getEntity();
        Player damager;
        if (e.getDamager() instanceof Player) {
            damager = (Player)e.getDamager();
        } else {
            if (!(e.getDamager() instanceof Projectile)) {
                return;
            }
            damager = (Player)((Projectile)e.getDamager()).getShooter();
        }

        PlayerData entityData = PlayerData.of(entity);
        PlayerData damagerData = PlayerData.of(damager);
        if (entityData == null || damagerData == null) {
            e.setCancelled(true);
            return;
        }
        boolean isEventEntity = this.plugin.getEventManager().getEventPlaying(entity) != null;
        boolean isEventDamager = this.plugin.getEventManager().getEventPlaying(damager) != null;
        CustomEvent eventDamager = this.plugin.getEventManager().getEventPlaying(damager);
        CustomEvent eventEntity = this.plugin.getEventManager().getEventPlaying(entity);
        if (damagerData.getPlayerState() == PlayerState.SPECTATING || this.plugin.getEventManager().getSpectators().containsKey(damager.getUniqueId())) {
            e.setCancelled(true);
            return;
        }
        if ((!entity.canSee(damager) && damager.canSee(entity)) || damager.getGameMode() == GameMode.SPECTATOR) {
            e.setCancelled(true);
            return;
        }
        if ((isEventDamager && eventDamager instanceof Sumo && eventDamager.getPlayer(damager.getUniqueId()).getState() != EventPlayer.PlayerState.FIGHTING) || (isEventEntity && eventDamager instanceof Sumo && eventEntity.getPlayer(entity.getUniqueId()).getState() != EventPlayer.PlayerState.FIGHTING) || (!isEventDamager && damagerData.getPlayerState() != PlayerState.FIGHTING) || (!isEventEntity && entityData.getPlayerState() != PlayerState.FIGHTING)) {
            e.setCancelled(true);
            return;
        }
        if ((entityData.getPlayerState() == PlayerState.EVENT && eventEntity instanceof Sumo) || (damagerData.getPlayerState() == PlayerState.EVENT && eventDamager instanceof Sumo)) {
            e.setDamage(0.0);
            return;
        }
        if(entityData.getPlayerState() == PlayerState.FIGHTING && entityData != null && !entity.getWorld().getName().equalsIgnoreCase(Practice.getInstance().getSpawnsLocation().stream().map(location -> location.getWorld().getName()).findFirst().orElse("lobby"))) {
            if (this.plugin.getMatchManager().getMatch(entityData) != null) {
                Match match = this.plugin.getMatchManager().getMatch(entityData);
                if (match == null) {
                    e.setDamage(0.0);
                    return;
                }
                if (damagerData.getTeamID() == entityData.getTeamID() && !match.isFFA()) {
                    e.setCancelled(true);
                    return;
                }
                if (match.getKit().isParkour()) {
                    e.setCancelled(true);
                    return;
                }
                if (match.getKit().isSpleef() || match.getKit().isSumo()) {
                    e.setDamage(0.0);
                }
                if (e.getDamager() instanceof Player) {
                    damagerData.setCombo(damagerData.getCombo() + 1);
                    damagerData.setHits(damagerData.getHits() + 1);
                    if (damagerData.getCombo() > damagerData.getLongestCombo()) {
                        damagerData.setLongestCombo(damagerData.getCombo());
                    }
                    entityData.setCombo(0);
                    if (match.getKit().isSpleef()) {
                        e.setCancelled(true);
                    }
                } else if (e.getDamager() instanceof Arrow) {
                    Arrow arrow2 = (Arrow) e.getDamager();
                    if (arrow2.getShooter() instanceof Player) {
                        Player shooter = (Player) arrow2.getShooter();
                        if (!entity.getName().equals(shooter.getName())) {
                            double health = Math.ceil(entity.getHealth() - e.getFinalDamage()) / 2.0;
                            if (health > 0.0) {
                                shooter.sendMessage(ChatColor.WHITE + entity.getName() + ChatColor.YELLOW + " ha recibido un disparo." + ChatColor.GRAY + " (" + ChatColor.RED + health + ChatColor.GRAY + ")");
                            }
                        }
                    }
                }
            }
        }
    }
    
    @EventHandler
    public void onPotionSplash(PotionSplashEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player)) {
            return;
        }
        for (PotionEffect effect : e.getEntity().getEffects()) {
            if (effect.getType().equals(PotionEffectType.HEAL)) {
                Player shooter = (Player)e.getEntity().getShooter();
                if (e.getIntensity(shooter) > 0.5) {
                    break;
                }
                PlayerData shooterData = PlayerData.of(shooter);
                if (shooterData != null) {
                    shooterData.setMissedPots(shooterData.getMissedPots() + 1);
                    break;
                }
                break;
            }
        }
    }
}
