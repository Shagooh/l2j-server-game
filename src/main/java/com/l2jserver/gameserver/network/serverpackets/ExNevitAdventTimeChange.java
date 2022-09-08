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

import static com.l2jserver.gameserver.config.Configuration.hunting;

/**
 * @author mochitto
 */
public class ExNevitAdventTimeChange extends L2GameServerPacket {
	private final boolean _paused;
	private final int _time;
	
	public ExNevitAdventTimeChange(int time, boolean paused) {
		_time = Math.min(time, hunting().getHuntingBonusMaxTime());
		_paused = paused;
	}
	
	@Override
	protected void writeImpl() {
		writeC(0xFE);
		writeH(0xE1);
		if (_paused && _time == hunting().getHuntingBonusMaxTime()) {
			writeD(0x02); // Quit
		} else {
			writeC(_paused ? 0x00 : 0x01); // 0=Paused - 1=Started
		}
		writeD(_time); // if state get max time is automatically changed to quit
	}
}
