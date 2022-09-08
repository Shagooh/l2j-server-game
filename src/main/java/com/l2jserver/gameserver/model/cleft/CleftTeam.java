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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author HorridoJoho
 * @since 2.6.3.0
 */
public final class CleftTeam {
	private final Team _team;
	private final Map<Integer, CleftParticipant> _participants;
	private AtomicInteger _points;
	private CleftCombatAerialTarget _combatAerialTarget;
	
	public CleftTeam(Team team) {
		Objects.requireNonNull(team);
		if (team == Team.NONE) {
			throw new IllegalArgumentException("Team.NONE not allowed!");
		}
		
		_team = team;
		_participants = Collections.synchronizedMap(new LinkedHashMap<>());
		_points = new AtomicInteger(0);
	}
	
	public CleftTeam(Team team, Map<Integer, CleftParticipant> participants, int points) {
		this(team);
		_participants.putAll(participants);
		_points.set(points);
	}
	
	public void addPoints(int points) {
		_points.addAndGet(points);
	}
	
	public void addParticipant(L2PcInstance player) {
		_participants.put(player.getObjectId(), new CleftParticipant(player, this));
	}
	
	public CleftParticipant removeParticipant(L2PcInstance player) {
		return _participants.remove(player.getObjectId());
	}
	
	public void setCombarAerialTarget(int id, String name) {
		_combatAerialTarget = new CleftCombatAerialTarget(id, name);
	}
	
	public Team getTeam() {
		return _team;
	}
	
	public Map<Integer, CleftParticipant> getParticipants() {
		return Collections.unmodifiableMap(_participants);
	}
	
	public int getPoints() {
		return _points.get();
	}
	
	public CleftCombatAerialTarget getCombatAerialTarget() {
		return _combatAerialTarget;
	}
}