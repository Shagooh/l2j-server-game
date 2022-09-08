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
package com.l2jserver.gameserver.network.serverpackets;

import java.util.Map;
import java.util.Objects;

import com.l2jserver.gameserver.model.cleft.CleftMatch;
import com.l2jserver.gameserver.model.cleft.CleftParticipant;
import com.l2jserver.gameserver.model.cleft.CleftTeam;
import com.l2jserver.gameserver.model.cleft.CleftTowerType;

/**
 * @author HorridoJoho
 * @since 2.6.3.0
 */
public abstract class ExCleftState extends AbstractExCleftPacket {
	private enum CleftStateUpdate {
		TOTAL,
		TOWER_DESTROY,
		COMBAT_AERIAL_TARGET_UPDATE,
		RESULT,
		PVP_KILL
	}
	
	public static final class Total extends ExCleftState {
		private final CleftMatch _match;
		
		public Total(CleftMatch match) {
			super(CleftStateUpdate.TOTAL);
			
			Objects.requireNonNull(match);
			
			_match = match;
		}
		
		private void writeTeam(CleftTeam team) {
			Map<Integer, CleftParticipant> participants = team.getParticipants();
			writeD(participants.size());
			for (Map.Entry<Integer, CleftParticipant> participant : participants.entrySet()) {
				writeParticipantPerformance(participant.getValue());
			}
		}
		
		@Override
		protected void writeImpl() {
			super.writeImpl();
			
			writeRemainingAndTeamPoints(_match);

			writeD(_match.getBlueTeam().getCombatAerialTarget().getId());
			writeD(_match.getRedTeam().getCombatAerialTarget().getId());
			writeS(_match.getBlueTeam().getCombatAerialTarget().getName());
			writeS(_match.getRedTeam().getCombatAerialTarget().getName());
			
			writeTeam(_match.getBlueTeam());
			writeTeam(_match.getRedTeam());
		}
	}
	
	public static final class TowerDestroy extends ExCleftState {
		private final CleftMatch _match;
		private final CleftParticipant _destroyer;
		private final CleftTowerType _towerType;
		
		public TowerDestroy(CleftMatch match, CleftParticipant destroyer, CleftTowerType towerType) {
			super(CleftStateUpdate.TOWER_DESTROY);
			
			Objects.requireNonNull(match);
			Objects.requireNonNull(destroyer);
			Objects.requireNonNull(towerType);
			
			_match = match;
			_destroyer = destroyer;
			_towerType = towerType;
		}
		
		@Override
		protected void writeImpl() {
			super.writeImpl();
			
			writeRemainingAndTeamPoints(_match);
			writeTeamId(_destroyer.getTeam());
			writeD(_towerType.ordinal());
			writeParticipantPerformance(_destroyer);
		}
	}
	
	public static final class CombatAerialTargetUpdate extends ExCleftState {
		private final CleftMatch _match;
		private final CleftTeam _team;
		
		public CombatAerialTargetUpdate(CleftMatch match, CleftTeam team) {
			super(CleftStateUpdate.COMBAT_AERIAL_TARGET_UPDATE);
			
			Objects.requireNonNull(match);
			Objects.requireNonNull(team);
			
			_match = match;
			_team = team;
		}
		
		@Override
		protected void writeImpl() {
			super.writeImpl();
			
			writeRemaining(_match);
			writeTeamId(_team);
			writeD(_team.getCombatAerialTarget().getId());
			writeS(_team.getCombatAerialTarget().getName());
		}
	}
	
	public static final class Result extends ExCleftState {
		private final CleftTeam _winTeam;
		private final CleftTeam _loseTeam;
		
		public Result(CleftTeam winTeam, CleftTeam loseTeam) {
			super(CleftStateUpdate.RESULT);
			
			Objects.requireNonNull(winTeam);
			Objects.requireNonNull(loseTeam);
			
			_winTeam = winTeam;
			_loseTeam = loseTeam;
		}
		
		@Override
		protected void writeImpl() {
			super.writeImpl();
			
			writeTeamId(_winTeam);
			writeTeamId(_loseTeam);
		}
	}
	
	public static final class PvpKill extends ExCleftState {
		private final CleftMatch _match;
		private final CleftParticipant _killer;
		private final CleftParticipant _killed;
		
		public PvpKill(CleftMatch match, CleftParticipant killer, CleftParticipant killed) {
			super(CleftStateUpdate.RESULT);
			
			Objects.requireNonNull(match);
			Objects.requireNonNull(killer);
			Objects.requireNonNull(killed);
			
			_match = match;
			_killer = killer;
			_killed = killed;
		}
		
		@Override
		protected void writeImpl() {
			super.writeImpl();
			
			writeRemainingAndTeamPoints(_match);
			
			writeTeamId(_killer.getTeam());
			writeParticipantPerformance(_killer);
			
			writeTeamId(_killed.getTeam());
			writeParticipantPerformance(_killed);
		}
	}
	
	private final CleftStateUpdate _state;
	
	private ExCleftState(CleftStateUpdate state) {
		Objects.requireNonNull(state);

		_state = state;
	}
	
	protected void writeRemaining(CleftMatch match) {
		writeD(match.getRemainingSeconds());
	}
	
	protected void writeTeamPoints(CleftMatch match) {
		writeD(match.getBlueTeam().getPoints());
		writeD(match.getRedTeam().getPoints());
	}
	
	protected void writeRemainingAndTeamPoints(CleftMatch match) {
		writeRemaining(match);
		writeTeamPoints(match);
	}
	
	protected void writeParticipantPerformance(CleftParticipant participant) {
		Objects.requireNonNull(participant);

		writeD(participant.getPlayer().getObjectId());
		writeD(participant.getKills());
		writeD(participant.getDeaths());
		writeD(participant.getTowers());
	}
	
	@Override
	protected void writeImpl() {
		writeC(0xfe);
		writeH(0x95);
		
		writeD(_state.ordinal());
	}
}