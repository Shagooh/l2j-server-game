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
package com.l2jserver.gameserver.model.stats;

import static com.l2jserver.gameserver.config.Configuration.server;
import static com.l2jserver.gameserver.enums.ShotType.BLESSED_SPIRITSHOTS;
import static com.l2jserver.gameserver.enums.ShotType.SPIRITSHOTS;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Formulas test.
 * @author Zoey76
 * @version 2.6.3.0
 */
@ExtendWith(MockitoExtension.class)
public class FormulasTest {
	
	private static final int HP_REGENERATE_PERIOD_CHARACTER = 3000;
	
	private static final int HP_REGENERATE_PERIOD_DOOR = 300000;
	
	@Mock
	private L2Character character;
	@Mock
	private Skill skill;
	
	@BeforeAll
	private static void init() {
		server().setProperty("DatapackRoot", "src/test/resources");
	}
	
	@Test
	public void test_get_regenerate_period() {
		when(character.isDoor()).thenReturn(false);
		
		assertEquals(HP_REGENERATE_PERIOD_CHARACTER, Formulas.getRegeneratePeriod(character));
	}
	
	@Test
	public void test_get_regenerate_period_door() {
		when(character.isDoor()).thenReturn(true);
		
		assertEquals(HP_REGENERATE_PERIOD_DOOR, Formulas.getRegeneratePeriod(character));
	}

	@ParameterizedTest
    @MethodSource("provide")
	public void test_calculate_cast_time(int hitTime, boolean isChanneling, int channelingSkillId, boolean isStatic, boolean isMagic, //
		int mAtkSpeed, double pAtkSpeed, boolean isChargedSpiritshots, boolean isChargedBlessedSpiritShots, double expected) {
		lenient().when(character.getMAtkSpd()).thenReturn(mAtkSpeed);
		lenient().when(character.getPAtkSpd()).thenReturn(pAtkSpeed);
		lenient().when(character.isChargedShot(SPIRITSHOTS)).thenReturn(isChargedSpiritshots);
		lenient().when(character.isChargedShot(BLESSED_SPIRITSHOTS)).thenReturn(isChargedBlessedSpiritShots);
		when(skill.getHitTime()).thenReturn(hitTime);
		when(skill.isChanneling()).thenReturn(isChanneling);
		lenient().when(skill.getChannelingSkillId()).thenReturn(channelingSkillId);
		lenient().when(skill.isStatic()).thenReturn(isStatic);
		lenient().when(skill.isMagic()).thenReturn(isMagic);
		
		assertEquals(expected, Formulas.calcCastTime(character, skill));
	}
	
	private static Iterator<Object[]> provide() {
		final List<Object[]> result = new LinkedList<>();
		// @formatter:off
		// TODO(Zoey76): Take care of the "bad" values.
		result.add(new Object[]{ 0, true, 1, false, false, 0, 0.0, false, false, 0.0 });
		result.add(new Object[]{ 0, true, 0, false, false, 0, 0.0, false, false, NaN });
		result.add(new Object[]{ 0, false, 1, false, false, 0, 0.0, false, false, NaN });
		result.add(new Object[]{ 0, false, 0, false, true, 500, 0.0, false, false, 0.0 });
		result.add(new Object[]{ 600, false, 0, false, true, 500, 0.0, false, false, 500.0 });
		result.add(new Object[]{ 3000, false, 0, false, true, 600, 0.0, false, false, 1665.0 });
		result.add(new Object[]{ 0, false, 0, false, false, 0, 500.0, false, false, 0.0 });
		result.add(new Object[]{ 600, false, 0, false, false, 0, 500.0, false, false, 500. });
		result.add(new Object[]{ 3000, false, 0, false, false, 0, 600.0, false, false, 1665.0 });
		result.add(new Object[]{ 1400, false, 0, false, true, 0, 0.0, true, false, POSITIVE_INFINITY });
		result.add(new Object[]{ 1400, false, 0, false, true, 0, 0.0, false, true, POSITIVE_INFINITY });
		result.add(new Object[]{ 1400, false, 0, true, true, 0, 0.0, true, false, 840.0 });
		result.add(new Object[]{ 1400, false, 0, true, true, 0, 0.0, false, true, 840.0 });
		// @formatter:on
		return result.iterator();
	}
}
