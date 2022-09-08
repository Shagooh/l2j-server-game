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
package com.l2jserver.gameserver.model.skills.targets;

import static com.l2jserver.gameserver.model.skills.targets.AffectObjectStaticImpl.ALL;
import static com.l2jserver.gameserver.model.skills.targets.AffectObjectStaticImpl.CLAN;
import static com.l2jserver.gameserver.model.skills.targets.AffectObjectStaticImpl.FRIEND;
import static com.l2jserver.gameserver.model.skills.targets.AffectObjectStaticImpl.HIDDEN_PLACE;
import static com.l2jserver.gameserver.model.skills.targets.AffectObjectStaticImpl.INVISIBLE;
import static com.l2jserver.gameserver.model.skills.targets.AffectObjectStaticImpl.NONE;
import static com.l2jserver.gameserver.model.skills.targets.AffectObjectStaticImpl.NOT_FRIEND;
import static com.l2jserver.gameserver.model.skills.targets.AffectObjectStaticImpl.OBJECT_DEAD_NPC_BODY;
import static com.l2jserver.gameserver.model.skills.targets.AffectObjectStaticImpl.UNDEAD_REAL_ENEMY;
import static com.l2jserver.gameserver.model.skills.targets.AffectObjectStaticImpl.WYVERN_OBJECT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;

/**
 * Affect Object test.
 * @author Zoey76
 * @version 2.6.3.0
 */
@ExtendWith(MockitoExtension.class)
class AffectObjectTest {
	
	@Mock
	private L2Character caster;
	@Mock
	private L2Object object;
	@Mock
	private L2Character creature;
	@Mock
	private L2Npc npc;
	
	@Test
	@DisplayName("Test affect object ALL.")
	void testAffectObjectAll() {
		assertTrue(ALL.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Test affect object CLAN, when player is not in clan.")
	void testAffectObjectClanPlayerIsNotInClan() {
		when(caster.isPlayable()).thenReturn(true);
		when(caster.getClanId()).thenReturn(0);
		
		assertFalse(CLAN.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Test affect object CLAN, when player is in clan, but object is not playable.")
	void testAffectObjectClanPlayerIsInClanObjectNotPlayable() {
		when(caster.isPlayable()).thenReturn(true);
		when(caster.getClanId()).thenReturn(1);
		when(object.isPlayable()).thenReturn(false);
		
		assertFalse(CLAN.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Test affect object CLAN, when player is in clan, but object is in another clan.")
	void testAffectObjectClanPlayerIsInClanObjectIsInAnotherClan() {
		when(caster.isPlayable()).thenReturn(true);
		when(caster.getClanId()).thenReturn(1);
		when(creature.isPlayable()).thenReturn(true);
		when(creature.getClanId()).thenReturn(2);
		
		assertFalse(CLAN.affectObject(caster, creature));
	}
	
	@Test
	@DisplayName("Test affect object CLAN, when player is in clan with object.")
	void testAffectObjectClanPlayerIsInClanWithObject() {
		when(caster.isPlayable()).thenReturn(true);
		when(caster.getClanId()).thenReturn(1);
		when(creature.isPlayable()).thenReturn(true);
		when(creature.getClanId()).thenReturn(1);
		
		assertTrue(CLAN.affectObject(caster, creature));
	}
	
	@Test
	@DisplayName("Test affect object FRIEND, when object is autoattackable.")
	void testAffectObjectFriendTargetIsAutoattackable() {
		when(object.isAutoAttackable(caster)).thenReturn(true);
		
		assertFalse(FRIEND.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Test affect object FRIEND, when object is not autoattackable.")
	void testAffectObjectFriendTargetIsNotAutoattackable() {
		when(object.isAutoAttackable(caster)).thenReturn(false);
		
		assertTrue(FRIEND.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Implement: Test affect object HIDDEN_PLACE.")
	void testAffectObjectHiddenPlace() {
		// TODO(Zoey76): Implement.
		assertFalse(HIDDEN_PLACE.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Test affect object INVISIBLE, when object is visible.")
	void testAffectObjectInvisibleVisibleObject() {
		when(object.isInvisible()).thenReturn(false);
		
		assertFalse(INVISIBLE.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Test affect object INVISIBLE, when object is invisible.")
	void testAffectObjectInvisibleInvisibleObject() {
		when(object.isInvisible()).thenReturn(true);
		
		assertTrue(INVISIBLE.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Test affect object NONE.")
	void testAffectObjectNone() {
		assertFalse(NONE.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Test affect object NOT_FRIEND, when target is not autoattackable.")
	void testAffectObjectNotFriendTargetIsNotAutoattackable() {
		when(object.isAutoAttackable(caster)).thenReturn(false);
		
		assertFalse(NOT_FRIEND.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Test affect object NOT_FRIEND, when target is autoattackable.")
	void testAffectObjectNotFriendTargetIsAutoattackable() {
		when(object.isAutoAttackable(caster)).thenReturn(true);
		
		assertTrue(NOT_FRIEND.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Test affect object NOT_FRIEND, when target is not dead.")
	void testAffectObjectNotFriendTargetIsNotDead() {
		when(creature.isDead()).thenReturn(false);
		when(creature.isAutoAttackable(caster)).thenReturn(true);
		
		assertTrue(NOT_FRIEND.affectObject(caster, creature));
	}
	
	@Test
	@DisplayName("Test affect object NOT_FRIEND, when target is dead.")
	void testAffectObjectNotFriendTargetIsDead() {
		when(creature.isDead()).thenReturn(true);
		
		assertFalse(NOT_FRIEND.affectObject(caster, creature));
	}
	
	@Test
	@DisplayName("Test affect object OBJECT_DEAD_NPC_BODY, when target is not NPC.")
	void testAffectObjectObjectDeadNpcBodyNotNpc() {
		when(object.isNpc()).thenReturn(false);
		
		assertFalse(OBJECT_DEAD_NPC_BODY.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Test affect object OBJECT_DEAD_NPC_BODY, when target is not NPC.")
	void test_affect_object_object_dead_npc_body_not_dead() {
		when(npc.isNpc()).thenReturn(true);
		when(npc.isDead()).thenReturn(false);
		
		assertFalse(OBJECT_DEAD_NPC_BODY.affectObject(caster, npc));
	}
	
	@Test
	@DisplayName("Test affect object OBJECT_DEAD_NPC_BODY, when target is a dead NPC.")
	void testAffectObjectObjectDeadNpcBodyDeadNpc() {
		when(npc.isNpc()).thenReturn(true);
		when(npc.isDead()).thenReturn(true);
		
		assertTrue(OBJECT_DEAD_NPC_BODY.affectObject(caster, npc));
	}
	
	@Test
	@DisplayName("Test affect object UNDEAD_REAL_ENEMY, when target is not NPC.")
	void testAffectObjectUndeadRealEnemyIsNotNpc() {
		when(object.isNpc()).thenReturn(false);
		
		assertFalse(UNDEAD_REAL_ENEMY.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Test affect object UNDEAD_REAL_ENEMY, when NPC target is not undead.")
	void testAffectObjectUndeadRealEnemyIsNotUndead() {
		when(npc.isNpc()).thenReturn(true);
		when(npc.isUndead()).thenReturn(false);
		
		assertFalse(UNDEAD_REAL_ENEMY.affectObject(caster, npc));
	}
	
	@Test
	@DisplayName("Test affect object UNDEAD_REAL_ENEMY, when NPC target is undead.")
	void testAffectObjectUndeadRealEnemyIsUndead() {
		when(npc.isNpc()).thenReturn(true);
		when(npc.isUndead()).thenReturn(true);
		
		assertTrue(UNDEAD_REAL_ENEMY.affectObject(caster, npc));
	}
	
	@Test
	@DisplayName("Test affect object WYVERN_OBJECT, when target is not NPC.")
	void testAffectObjectWyvernObjectIsNotNpc() {
		when(object.isNpc()).thenReturn(false);
		
		assertFalse(WYVERN_OBJECT.affectObject(caster, object));
	}
	
	@Test
	@DisplayName("Test affect object WYVERN_OBJECT, when target is not Wyvern.")
	void testAffectObjectWyvernObjectIsNotWyvern() {
		when(npc.isNpc()).thenReturn(true);
		when(npc.getId()).thenReturn(1);
		
		assertFalse(WYVERN_OBJECT.affectObject(caster, npc));
	}
	
	@Test
	@DisplayName("Test affect object WYVERN_OBJECT, when target is Wyvern.")
	void testAffectObjectWyvernObjectIsWyvern() {
		when(npc.isNpc()).thenReturn(true);
		when(npc.getId()).thenReturn(12621);
		
		assertTrue(WYVERN_OBJECT.affectObject(caster, npc));
	}
}
