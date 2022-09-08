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

import com.l2jserver.gameserver.enums.Team;

/**
 * @author HorridoJoho
 * @since 2.6.3.0
 */
public final class CleftMatch {
	private final int _minTeamMembers;
	private final boolean _isBalanced;
	private final CleftTeam _blueTeam;
	private final CleftTeam _redTeam;
	
	public CleftMatch(int minTeamMembers, boolean isBalanced) {
		_minTeamMembers = minTeamMembers;
		_isBalanced = isBalanced;
		_blueTeam = new CleftTeam(Team.BLUE);
		_redTeam = new CleftTeam(Team.RED);
	}
	
	public int getMinTeamMembers() {
		return _minTeamMembers;
	}
	
	public boolean isBalanced() {
		return _isBalanced;
	}
	
	public CleftTeam getBlueTeam() {
		return _blueTeam;
	}
	
	public CleftTeam getRedTeam() {
		return _redTeam;
	}
	
	public int getRemainingSeconds() {
		// TODO:
		return 0;
	}
}