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
package com.l2jserver.gameserver.config.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Hex Id Converter test.
 * @author Zoey76
 * @version 2.6.3.0
 */
public class HexIdConverterTest {
	
	private static final HexIdConverter CONVERTER = new HexIdConverter();
	
	@ParameterizedTest
    @MethodSource("provideKeyValues")
	public void convertTest(String hexId, BigInteger expected) {
		assertEquals(CONVERTER.convert(null, hexId), expected);
	}
	
	public static Object[][] provideKeyValues() {
		return new Object[][] {
			{
				"-1eeb34fce0c64b610338d1269d8cfea4",
				new BigInteger("-1eeb34fce0c64b610338d1269d8cfea4", 16)
			}
		};
	}
}
