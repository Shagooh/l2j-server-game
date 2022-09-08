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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;

/**
 * Exist NPC condition test.
 * @author Zoey76
 * @version 2.6.3.0
 */
@ExtendWith(MockitoExtension.class)
class ConditionOpExistNpcTest {
	
	private static final Set<Integer> NPC_IDS = Set.of(1, 2);
	
	private static final int RADIUS = 200;
	
	@Mock
	private L2Character effector;
	@Mock
	private L2Character effected;
	@Mock
	private L2World world;
	@Mock
	private L2Npc npc1;
	@Mock
	private L2Npc npc2;
	@Mock
	private L2Npc npc3;
	
	private static MockedStatic<L2World> mockedStaticWorld;
	
	@BeforeAll
	static void before() {
		mockedStaticWorld = mockStatic(L2World.class);
	}
	
	@AfterAll
	static void after() {
		mockedStaticWorld.close();
	}
	
	@Test
	void testExistNpcPresent() {
		final var condition = new ConditionOpExistNpc(NPC_IDS, RADIUS, true);
		when(L2World.getInstance()).thenReturn(world);
		when(world.getVisibleObjects(effector, RADIUS)).thenReturn(List.of(effected, npc1, npc2, npc3));
		when(effected.isNpc()).thenReturn(false);
		when(npc1.isNpc()).thenReturn(true);
		when(npc1.getId()).thenReturn(3);
		when(npc2.isNpc()).thenReturn(true);
		when(npc2.getId()).thenReturn(2);
		assertTrue(condition.testImpl(effector, effected, null, null));
	}
	
	@Test
	void testExistNpcNotPresent() {
		final var condition = new ConditionOpExistNpc(NPC_IDS, RADIUS, false);
		when(L2World.getInstance()).thenReturn(world);
		when(world.getVisibleObjects(effector, RADIUS)).thenReturn(List.of(effected, npc1, npc2, npc3));
		when(effected.isNpc()).thenReturn(false);
		when(npc1.isNpc()).thenReturn(true);
		when(npc1.getId()).thenReturn(4);
		when(npc2.isNpc()).thenReturn(true);
		when(npc2.getId()).thenReturn(5);
		when(npc3.isNpc()).thenReturn(true);
		when(npc3.getId()).thenReturn(6);
		assertTrue(condition.testImpl(effector, effected, null, null));
	}
}
