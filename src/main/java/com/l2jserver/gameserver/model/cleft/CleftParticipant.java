/*
 * Copyright © 2004-2021 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model.cleft;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author HorridoJoho
 * @since 2.6.3.0
 */
public final class CleftParticipant {
	private volatile L2PcInstance _player;
	private volatile CleftTeam _team;
	private AtomicInteger _kills;
	private AtomicInteger _deaths;
	private AtomicInteger _towers;
	
	public CleftParticipant(L2PcInstance player, CleftTeam team) {
		Objects.requireNonNull(player);
		Objects.requireNonNull(team);
		
		_player = player;
		_team = team;
		_kills = new AtomicInteger(0);
		_deaths = new AtomicInteger(0);
		_towers = new AtomicInteger(0);
	}
	
	public CleftParticipant(L2PcInstance player, CleftTeam team, int kills, int deaths, int towers) {
		this(player, team);
		_kills.set(kills);
		_deaths.set(deaths);
		_towers.set(towers);
	}
	
	public void setPlayer(L2PcInstance player) {
		Objects.requireNonNull(player);
		_player = player;
	}
	
	public void setTeam(CleftTeam team) {
		Objects.requireNonNull(team);
		_team = team;
	}
	
	public int addKill() {
		return _kills.incrementAndGet();
	}
	
	public int addDeath() {
		return _deaths.incrementAndGet();
	}
	
	public int addTower() {
		return _towers.incrementAndGet();
	}
	
	public L2PcInstance getPlayer() {
		return _player;
	}
	
	public CleftTeam getTeam() {
		return _team;
	}
	
	public int getKills() {
		return _kills.get();
	}
	
	public int getDeaths() {
		return _deaths.get();
	}
	
	public int getTowers() {
		return _towers.get();
	}
}