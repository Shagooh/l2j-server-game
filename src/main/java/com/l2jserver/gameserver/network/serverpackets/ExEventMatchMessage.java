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

/**
 * @author janiii
 * @author HorridoJoho
 * @since 2.6.3.0
 */
public class ExEventMatchMessage extends L2GameServerPacket {
	enum MessageType {
		STRING,
		STATIC_FINISH,
		STATIC_START,
		STATIC_GAME_OVER,
		STATIC_1,
		STATIC_2,
		STATIC_3,
		STATIC_4,
		STATIC_5
	}
	
	public static final ExEventMatchMessage STATIC_FINISH_PACKET = new ExEventMatchMessage(MessageType.STATIC_FINISH);
	public static final ExEventMatchMessage STATIC_START_PACKET = new ExEventMatchMessage(MessageType.STATIC_START);
	public static final ExEventMatchMessage STATIC_GAME_OVER_PACKET = new ExEventMatchMessage(MessageType.STATIC_GAME_OVER);
	public static final ExEventMatchMessage STATIC_1_PACKET = new ExEventMatchMessage(MessageType.STATIC_1);
	public static final ExEventMatchMessage STATIC_2_PACKET = new ExEventMatchMessage(MessageType.STATIC_2);
	public static final ExEventMatchMessage STATIC_3_PACKET = new ExEventMatchMessage(MessageType.STATIC_3);
	public static final ExEventMatchMessage STATIC_4_PACKET = new ExEventMatchMessage(MessageType.STATIC_4);
	public static final ExEventMatchMessage STATIC_5_PACKET = new ExEventMatchMessage(MessageType.STATIC_5);

	private final MessageType _type;
	private final String _message;
	
	/**
	 * @param message Text to display.
	 */
	public ExEventMatchMessage(String message)
	{
		_type = MessageType.STRING;
		_message = message;
	}
	
	private ExEventMatchMessage(MessageType type) {
		_type = type;
		_message = null;
	}
	
	@Override
	protected void writeImpl() {
		writeC(0xFE);
		writeH(0x0F);

		writeC(_type.ordinal());
		if (_type == MessageType.STRING) {
			writeS(_message);
		}
	}
}
