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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Pattern Converter test.
 * @author Zoey76
 * @version 2.6.3.0
 */
public class PatternConverterTest {
	
	private static final PatternConverter CONVERTER = new PatternConverter();
	
	@ParameterizedTest
	@MethodSource("providePatterns")
	public void convertTest(String pattern, String text, boolean expected) {
		assertEquals(CONVERTER.convert(null, pattern).matcher(text).matches(), expected);
	}
	
	public static Object[][] providePatterns() {
		return new Object[][] {
			{
				"[A-Z][a-z]{3,3}[A-Za-z0-9]*",
				"OmfgWTF1",
				true
			},
			{
				"[A-Z][a-z]{3,3}[A-Za-z0-9]*",
				"",
				false
			},
			{
				"[A-Z][a-z]{3,3}[A-Za-z0-9]+",
				"",
				false
			},
			{
				"[a-zA-Z0-9]*",
				"Zoey76",
				true
			}
		};
	}
}
