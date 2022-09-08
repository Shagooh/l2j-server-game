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

import java.util.Objects;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * ExVoteSystemInfo packet implementation.
 * @author Gnacik
 */
public class ExVoteSystemInfo extends L2GameServerPacket {
	private final L2PcInstance _player;
	
	public ExVoteSystemInfo(L2PcInstance player) {
		Objects.requireNonNull(player);
		_player = player;
	}
	
	@Override
	protected void writeImpl() {
		writeC(0xFE);
		writeH(0xC9);
		writeD(_player.getRecSystem().getLeft());
		writeD(_player.getRecSystem().getHave());
		writeD(_player.getRecSystem().getBonusTime());
		writeD(_player.getRecSystem().getBonus());
		writeD((!_player.getRecSystem().isBonusTaskActive() && _player.getRecSystem().getBonusTime() > 0) ? 0x01 : 0x00);
	}
}
