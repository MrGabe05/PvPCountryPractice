package com.bizarrealex.aether.scoreboard;

import com.bizarrealex.aether.scoreboard.cooldown.BoardCooldown;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public interface BoardAdapter
{
    String getTitle(final Player p0);
    
    List<String> getScoreboard(final Player p0, final Board p1, final Set<BoardCooldown> p2);
}
