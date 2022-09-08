/*
 * Copyright © 2004-2022 L2J Server
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
package com.l2jserver.gameserver.config;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.aeonbits.owner.Config.HotReloadType.ASYNC;
import static org.aeonbits.owner.Config.LoadType.MERGE;

import java.math.BigInteger;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;

import com.l2jserver.gameserver.config.converter.HexIdConverter;

/**
 * Hex Id Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
@Sources({
	"file:${L2J_HOME}/" + Configuration.CUSTOM_SUBPATH + HexIdConfiguration.FILENAME,
	"file:" + Configuration.DEFAULT_PATH + HexIdConfiguration.FILENAME,
	"classpath:" + Configuration.DEFAULT_PATH + HexIdConfiguration.FILENAME
})
@LoadPolicy(MERGE)
@HotReload(value = 20, unit = MINUTES, type = ASYNC)
public interface HexIdConfiguration extends Mutable, Reloadable, Accessible {
	public static final String FILENAME = "hexid.txt";
	
	public static final String SERVERID_KEY = "ServerID";
	public static final String HEXID_KEY = "HexID";
	
	@Key(SERVERID_KEY)
	Integer getServerID();
	
	@Key(HEXID_KEY)
	@ConverterClass(HexIdConverter.class)
	BigInteger getHexID();
}