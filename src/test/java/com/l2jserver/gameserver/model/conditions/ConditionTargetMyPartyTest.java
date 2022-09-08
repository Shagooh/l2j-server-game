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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Condition Target My Party test.
 * @author Zoey76
 * @version 2.6.3.0
 */
@ExtendWith(MockitoExtension.class)
public class ConditionTargetMyPartyTest {
	
	@Mock
	private Skill skill;
	@Mock
	private L2Character effector;
	@Mock
	private L2Character effected;
	@Mock
	private L2PcInstance player;
	@Mock
	private L2PcInstance otherPlayer;
	
	private static final ConditionTargetMyParty CONDITION_INCLUDE_ME = new ConditionTargetMyParty("INCLUDE_ME");
	
	private static final ConditionTargetMyParty CONDITION_EXCEPT_ME = new ConditionTargetMyParty("EXCEPT_ME");
	
	@Test
	public void test_null_player() {
		assertFalse(CONDITION_INCLUDE_ME.testImpl(effector, effected, skill, null));
	}
	
	@Test
	public void test_self_target_exclude_me() {
		when(effector.getActingPlayer()).thenReturn(player);
		
		assertFalse(CONDITION_EXCEPT_ME.testImpl(effector, player, skill, null));
	}
	
	@Test
	public void test_player_in_party_target_not_in_party() {
		when(effector.getActingPlayer()).thenReturn(player);
		when(player.isInParty()).thenReturn(true);
		when(player.isInPartyWith(effected)).thenReturn(false);
		
		assertFalse(CONDITION_INCLUDE_ME.testImpl(effector, effected, skill, null));
	}
	
	@Test
	public void test_player_in_party_with_target() {
		when(effector.getActingPlayer()).thenReturn(player);
		when(player.isInParty()).thenReturn(true);
		when(player.isInPartyWith(effected)).thenReturn(true);
		
		assertTrue(CONDITION_INCLUDE_ME.testImpl(effector, effected, skill, null));
	}
	
	@Test
	public void test_player_not_in_party_target_not_player_or_player_summon() {
		when(effector.getActingPlayer()).thenReturn(player);
		when(player.isInParty()).thenReturn(false);
		when(effected.getActingPlayer()).thenReturn(otherPlayer);
		
		assertFalse(CONDITION_INCLUDE_ME.testImpl(effector, effected, skill, null));
	}
	
	@Test
	public void test_player_in_party_target_player_or_player_summon() {
		when(effector.getActingPlayer()).thenReturn(player);
		when(player.isInParty()).thenReturn(false);
		when(effected.getActingPlayer()).thenReturn(player);
		
		assertTrue(CONDITION_INCLUDE_ME.testImpl(effector, effected, skill, null));
	}
}
