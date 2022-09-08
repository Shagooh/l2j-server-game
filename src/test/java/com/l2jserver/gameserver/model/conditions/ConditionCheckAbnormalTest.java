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
package com.l2jserver.gameserver.model.conditions;

import static com.l2jserver.gameserver.model.skills.AbnormalType.MULTI_DEBUFF_WIND;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.l2jserver.gameserver.model.CharEffectList;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Check abnormal type and level test.
 * @author Zoey76
 * @version 2.6.3.0
 */
@ExtendWith(MockitoExtension.class)
class ConditionCheckAbnormalTest {
	
	@Mock
	private L2Character effected;
	@Mock
	private CharEffectList charEffectList;
	@Mock
	private BuffInfo buffInfo;
	@Mock
	private Skill skill;
	
	@Test
	void testEffectedWithoutAbnormal() {
		final var condition = new ConditionCheckAbnormal(MULTI_DEBUFF_WIND, 1, true);
		when(effected.getEffectList()).thenReturn(charEffectList);
		when(charEffectList.getBuffInfoByAbnormalType(MULTI_DEBUFF_WIND)).thenReturn(null);
		assertFalse(condition.testImpl(null, effected, null, null));
	}
	
	@Test
	void testEffectedWithLowerLevelAbnormal() {
		final var condition = new ConditionCheckAbnormal(MULTI_DEBUFF_WIND, 3, true);
		when(effected.getEffectList()).thenReturn(charEffectList);
		when(charEffectList.getBuffInfoByAbnormalType(MULTI_DEBUFF_WIND)).thenReturn(buffInfo);
		when(buffInfo.getSkill()).thenReturn(skill);
		when(skill.getAbnormalLvl()).thenReturn(1);
		assertFalse(condition.testImpl(null, effected, null, null));
	}
	
	@Test
	void testEffectedWithAbnormal() {
		final var condition = new ConditionCheckAbnormal(MULTI_DEBUFF_WIND, 3, true);
		when(effected.getEffectList()).thenReturn(charEffectList);
		when(charEffectList.getBuffInfoByAbnormalType(MULTI_DEBUFF_WIND)).thenReturn(buffInfo);
		when(buffInfo.getSkill()).thenReturn(skill);
		when(skill.getAbnormalLvl()).thenReturn(3);
		assertTrue(condition.testImpl(null, effected, null, null));
	}
}
