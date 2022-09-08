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

/**
 * @author HorridoJoho
 * @since 2.6.3.0
 */
public abstract class ExCleftList extends AbstractExCleftPacket {
	private enum ListAction {
		CLOSE,
		TOTAL,
		ADD,
		REMOVE,
		CHANGE_TEAM
	}
	
	public static final class Close extends ExCleftList {
		public Close() {
			super(ListAction.CLOSE);
		}
		
		@Override
		protected void writeImpl() {
			super.writeImpl();
		}
	}
	
	public static final class Total extends ExCleftList {
		private final CleftMatch _match;
		
		public Total(CleftMatch match) {
			super(ListAction.TOTAL);
			
			Objects.requireNonNull(match);
			
			_match = match;
		}
		
		private void writeTeamParticipants(CleftTeam team) {
			Map<Integer, CleftParticipant> participants = team.getParticipants();
			writeD(participants.size());
			for (CleftParticipant participant : participants.values()) {
				writeParticipant(participant, true);
			}
		}
		
		@Override
		protected void writeImpl() {
			super.writeImpl();
			
			writeD(_match.getMinTeamMembers());
			writeD(_match.isBalanced() ? 1 : 0);
			
			writeTeamParticipants(_match.getBlueTeam());
			writeTeamParticipants(_match.getRedTeam());
		}
	}
	
	public static final class Add extends ExCleftList {
		private final CleftTeam _team;
		private final CleftParticipant _player;
		
		public Add(CleftTeam team, CleftParticipant player) {
			super(ListAction.ADD);
			
			Objects.requireNonNull(team);
			Objects.requireNonNull(player);
			
			_team = team;
			_player = player;
		}
		
		@Override
		protected void writeImpl() {
			super.writeImpl();
			
			writeTeamId(_team);
			writeParticipant(_player, true);
		}
	}
	
	public static final class Remove extends ExCleftList {
		private final CleftTeam _team;
		private final CleftParticipant _player;
		
		public Remove(CleftTeam team, CleftParticipant player) {
			super(ListAction.REMOVE);
			
			Objects.requireNonNull(team);
			Objects.requireNonNull(player);
			
			_team = team;
			_player = player;
		}
		
		@Override
		protected void writeImpl() {
			super.writeImpl();
			
			writeTeamId(_team);
			writeParticipant(_player, false);
		}
	}
	
	public static final class ChangeTeam extends ExCleftList {
		private final CleftTeam _oldTeam;
		private final CleftTeam _newTeam;
		private final CleftParticipant _player;
		
		public ChangeTeam(CleftTeam oldTeam, CleftTeam newTeam, CleftParticipant player) {
			super(ListAction.CHANGE_TEAM);
			
			Objects.requireNonNull(oldTeam);
			Objects.requireNonNull(newTeam);
			Objects.requireNonNull(player);
			
			_oldTeam = oldTeam;
			_newTeam = newTeam;
			_player = player;
		}
		
		@Override
		protected void writeImpl() {
			super.writeImpl();
			
			writeParticipant(_player, false);
			writeTeamId(_oldTeam);
			writeTeamId(_newTeam);
		}
	}
	
	private final ListAction _action;
	
	protected ExCleftList(ListAction action) {
		Objects.requireNonNull(action);
		
		_action = action;
	}
	
	protected void writeParticipant(CleftParticipant participant, boolean writeName) {
		writeD(participant.getPlayer().getObjectId());
		if (writeName) {
			writeS(participant.getPlayer().getName());
		}
	}
	
	@Override
	protected void writeImpl() {
		writeC(0xfe);
		writeH(0x94);
		
		writeD(_action.ordinal() - 1);
	}
}