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

import static com.l2jserver.gameserver.model.skills.targets.AffectScope.BALAKAS_SCOPE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.DEAD_PLEDGE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.DEAD_UNION;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.FAN;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.NONE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.PARTY;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.PARTY_PLEDGE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.PLEDGE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.POINT_BLANK;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.RANGE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.RANGE_SORT_BY_HP;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.RING_RANGE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.SINGLE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.SQUARE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.SQUARE_PB;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.STATIC_OBJECT_SCOPE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.WYVERN_SCOPE;
import static com.l2jserver.gameserver.model.zone.ZoneId.SIEGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.l2jserver.gameserver.GeoData;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2ClanMember;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2ServitorInstance;
import com.l2jserver.gameserver.model.actor.knownlist.NpcKnownList;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.util.Util;

/**
 * Affect Scope test.
 * @author Zoey76
 * @version 2.6.3.0
 */
@ExtendWith(MockitoExtension.class)
class AffectScopeTest {
	
	private static final int AFFECT_LIMIT = 5;
	
	private static final int AFFECT_RANGE = 1000;
	
	private static final int[] FAN_RANGE = {
		0,
		0,
		80,
		150
	};
	
	@Mock
	private L2Character caster;
	@Mock
	private L2Character target;
	@Mock
	private Skill skill;
	@Mock
	private L2World world;
	@Mock
	private L2Object object1;
	@Mock
	private L2PcInstance player1;
	@Mock
	private L2PcInstance player2;
	@Mock
	private L2PcInstance player3;
	@Mock
	private L2PcInstance player4;
	@Mock
	private L2PcInstance player5;
	@Mock
	private L2PcInstance player6;
	@Mock
	private L2PcInstance player7;
	@Mock
	private L2PcInstance player8;
	@Mock
	private L2Summon summon;
	@Mock
	private L2ServitorInstance servitor;
	@Mock
	private AffectObject affectObject;
	@Mock
	private L2Party party1;
	@Mock
	private L2Party party2;
	@Mock
	private L2Clan clan;
	@Mock
	private L2ClanMember clanMember1;
	@Mock
	private L2ClanMember clanMember2;
	@Mock
	private L2ClanMember clanMember3;
	@Mock
	private L2ClanMember clanMember4;
	@Mock
	private L2Npc npc1;
	@Mock
	private L2Npc npc2;
	@Mock
	private L2Npc npc3;
	@Mock
	private L2NpcTemplate npcTemplate;
	@Mock
	private NpcKnownList npcKnownList;
	@Mock
	private GeoData geoData;
	@Mock
	private L2Object otherObject;
	
	private static MockedStatic<L2World> mockedStaticWorld;
	private static MockedStatic<Util> mockedStaticUtil;
	private static MockedStatic<GeoData> mockedStaticGeoData;
	
	@BeforeAll
	static void init() {
		mockedStaticWorld = mockStatic(L2World.class);
		mockedStaticUtil = mockStatic(Util.class);
		mockedStaticGeoData = mockStatic(GeoData.class);
	}
	
	@AfterAll
	static void after() {
		mockedStaticWorld.close();
		mockedStaticUtil.close();
		mockedStaticGeoData.close();
	}
	
	@Test
	@DisplayName("Test BALAKAS_SCOPE.")
	void testBalakasScope() {
		assertEquals(List.of(), BALAKAS_SCOPE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test DEAD_PLEDGE scope, when caster is not playable.")
	void testDeadPledgeScopeCasterIsNotPlayable() {
		when(target.isPlayable()).thenReturn(false);
		
		assertEquals(List.of(), DEAD_PLEDGE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test DEAD_PLEDGE scope, when caster is not in clan.")
	void testDeadPledgeScopePlayerIsNotInClan() {
		when(target.isPlayable()).thenReturn(true);
		when(target.getActingPlayer()).thenReturn(player1);
		when(player1.getClanId()).thenReturn(0);
		
		assertEquals(List.of(), DEAD_PLEDGE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test DEAD_PLEDGE scope.")
	void testDeadPledgeScopePlayer() {
		when(target.isPlayable()).thenReturn(true);
		when(target.getActingPlayer()).thenReturn(player1);
		when(player1.getClanId()).thenReturn(1);
		when(player1.isInDuel()).thenReturn(false);
		when(skill.getAffectLimit()).thenReturn(AFFECT_LIMIT);
		when(skill.getAffectRange()).thenReturn(AFFECT_RANGE);
		when(skill.getAffectObject()).thenReturn(affectObject);
		when(L2World.getInstance()).thenReturn(world);
		when(world.getVisibleObjects(target, AFFECT_RANGE)).thenReturn(List.of(object1, servitor, player2, player3, player4, player5, player6, player7, player8));
		when(object1.isPlayable()).thenReturn(false);
		when(servitor.isPlayable()).thenReturn(true);
		when(servitor.getActingPlayer()).thenReturn(null);
		when(player2.isPlayable()).thenReturn(true);
		when(player2.getActingPlayer()).thenReturn(player2);
		when(player2.getClanId()).thenReturn(0);
		when(player3.isPlayable()).thenReturn(true);
		when(player3.getActingPlayer()).thenReturn(player3);
		when(player3.getClanId()).thenReturn(1);
		when(player1.checkPvpSkill(player3, skill)).thenReturn(true);
		when(player1.isInOlympiadMode()).thenReturn(false);
		when(player3.isInsideZone(SIEGE)).thenReturn(false);
		when(affectObject.affectObject(player1, player3)).thenReturn(false);
		
		when(player4.isPlayable()).thenReturn(true);
		when(player4.getActingPlayer()).thenReturn(player4);
		when(player4.getClanId()).thenReturn(1);
		when(player1.checkPvpSkill(player4, skill)).thenReturn(true);
		when(player1.isInOlympiadMode()).thenReturn(false);
		when(player4.isInsideZone(SIEGE)).thenReturn(false);
		when(affectObject.affectObject(player1, player4)).thenReturn(true);
		
		when(player5.isPlayable()).thenReturn(true);
		when(player5.getActingPlayer()).thenReturn(player5);
		when(player5.getClanId()).thenReturn(1);
		when(player1.checkPvpSkill(player5, skill)).thenReturn(true);
		when(player1.isInOlympiadMode()).thenReturn(false);
		when(player5.isInsideZone(SIEGE)).thenReturn(false);
		when(affectObject.affectObject(player1, player5)).thenReturn(true);
		
		when(player6.isPlayable()).thenReturn(true);
		when(player6.getActingPlayer()).thenReturn(player6);
		when(player6.getClanId()).thenReturn(1);
		when(player1.checkPvpSkill(player6, skill)).thenReturn(true);
		when(player1.isInOlympiadMode()).thenReturn(false);
		when(player6.isInsideZone(SIEGE)).thenReturn(false);
		when(affectObject.affectObject(player1, player6)).thenReturn(true);
		
		when(player7.isPlayable()).thenReturn(true);
		when(player7.getActingPlayer()).thenReturn(player7);
		when(player7.getClanId()).thenReturn(1);
		when(player1.checkPvpSkill(player7, skill)).thenReturn(true);
		when(player1.isInOlympiadMode()).thenReturn(false);
		when(player7.isInsideZone(SIEGE)).thenReturn(false);
		when(affectObject.affectObject(player1, player7)).thenReturn(true);
		
		when(player8.isPlayable()).thenReturn(true);
		when(player8.getActingPlayer()).thenReturn(player8);
		when(player8.getClanId()).thenReturn(1);
		when(player1.checkPvpSkill(player8, skill)).thenReturn(true);
		when(player1.isInOlympiadMode()).thenReturn(false);
		when(player8.isInsideZone(SIEGE)).thenReturn(false);
		when(affectObject.affectObject(player1, player8)).thenReturn(true);
		
		assertEquals(List.of(player4, player5, player6, player7, player8), DEAD_PLEDGE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test DEAD_UNION scope, when target is not playable.")
	void testDeadUnionScopeTargetIsNotPlayable() {
		assertEquals(List.of(), DEAD_UNION.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test DEAD_UNION scope.")
	void testDeadUnionScope() {
		when(target.isPlayable()).thenReturn(true);
		when(target.getActingPlayer()).thenReturn(player1);
		when(skill.getAffectLimit()).thenReturn(AFFECT_LIMIT);
		when(skill.getAffectRange()).thenReturn(AFFECT_RANGE);
		when(skill.getAffectObject()).thenReturn(affectObject);
		when(L2World.getInstance()).thenReturn(world);
		when(world.getVisibleObjects(target, AFFECT_RANGE)).thenReturn(List.of(object1, servitor, player2, player3, player4, player5));
		
		when(object1.isCharacter()).thenReturn(false);
		
		when(servitor.isCharacter()).thenReturn(true);
		when(player1.isInCommandChannelWith(servitor)).thenReturn(true);
		when(servitor.isDead()).thenReturn(true);
		when(affectObject.affectObject(player1, servitor)).thenReturn(true);
		
		when(player2.isCharacter()).thenReturn(true);
		when(player1.isInCommandChannelWith(player2)).thenReturn(false);
		
		when(player3.isCharacter()).thenReturn(true);
		when(player1.isInCommandChannelWith(player3)).thenReturn(true);
		when(player3.isDead()).thenReturn(false);
		
		when(player4.isCharacter()).thenReturn(true);
		when(player1.isInCommandChannelWith(player4)).thenReturn(true);
		when(player4.isDead()).thenReturn(true);
		when(affectObject.affectObject(player1, player4)).thenReturn(false);
		
		when(player5.isCharacter()).thenReturn(true);
		when(player1.isInCommandChannelWith(player5)).thenReturn(true);
		when(player5.isDead()).thenReturn(true);
		when(affectObject.affectObject(player1, player5)).thenReturn(true);
		
		assertEquals(List.of(servitor, player5), DEAD_UNION.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test FAN scope.")
	void testFanScope() {
		when(Util.calculateAngleFrom(caster, target)).thenReturn(323.53);
		when(skill.getAffectLimit()).thenReturn(AFFECT_LIMIT);
		when(skill.getFanRange()).thenReturn(FAN_RANGE);
		when(skill.getAffectObject()).thenReturn(affectObject);
		
		when(L2World.getInstance()).thenReturn(world);
		when(world.getVisibleObjects(caster, FAN_RANGE[2])).thenReturn(List.of(target, player1, player2, npc1, npc2, npc3, otherObject));
		
		when(target.isCharacter()).thenReturn(true);
		when(target.isDead()).thenReturn(false);
		when(Util.calculateAngleFrom(caster, target)).thenReturn(323.53);
		when(affectObject.affectObject(caster, target)).thenReturn(true);
		when(GeoData.getInstance()).thenReturn(geoData);
		when(geoData.canSeeTarget(caster, target)).thenReturn(true);
		
		when(player1.isCharacter()).thenReturn(true);
		when(player1.isDead()).thenReturn(false);
		when(Util.calculateAngleFrom(caster, player1)).thenReturn(100.33);
		
		when(player2.isCharacter()).thenReturn(true);
		when(player2.isDead()).thenReturn(false);
		when(Util.calculateAngleFrom(caster, player2)).thenReturn(323.53);
		when(affectObject.affectObject(caster, player2)).thenReturn(true);
		when(GeoData.getInstance()).thenReturn(geoData);
		when(geoData.canSeeTarget(caster, player2)).thenReturn(false);

		when(npc1.isCharacter()).thenReturn(true);
		when(npc1.isDead()).thenReturn(true);

		when(npc2.isCharacter()).thenReturn(true);
		when(npc2.isDead()).thenReturn(false);
		when(Util.calculateAngleFrom(caster, npc2)).thenReturn(323.53);
		when(affectObject.affectObject(caster, npc2)).thenReturn(true);
		when(GeoData.getInstance()).thenReturn(geoData);
		when(geoData.canSeeTarget(caster, npc2)).thenReturn(true);

		when(npc3.isCharacter()).thenReturn(true);
		when(npc3.isDead()).thenReturn(false);
		when(Util.calculateAngleFrom(caster, npc3)).thenReturn(323.53);
		when(affectObject.affectObject(caster, npc3)).thenReturn(false);
		
		when(otherObject.isCharacter()).thenReturn(false);
		
		assertEquals(List.of(target, npc2), FAN.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test NONE scope.")
	void testNoneScope() {
		assertEquals(List.of(), NONE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test PARTY scope, when target is in party.")
	void testPartyScopeTargetIsInParty() {
		when(skill.getAffectRange()).thenReturn(AFFECT_RANGE);
		when(target.isCharacter()).thenReturn(true);
		when(target.isInParty()).thenReturn(true);
		when(target.getParty()).thenReturn(party1);
		when(party1.getMembers()).thenReturn(List.of(player1, player2, player3, player4));
		
		// Party member with summon, both close enough.
		when(Util.checkIfInRange(AFFECT_RANGE, target, player1, true)).thenReturn(true);
		when(player1.hasSummon()).thenReturn(true);
		when(player1.getSummon()).thenReturn(summon);
		when(Util.checkIfInRange(AFFECT_RANGE, target, summon, true)).thenReturn(true);
		
		// Party member close enough without summon.
		when(Util.checkIfInRange(AFFECT_RANGE, target, player2, true)).thenReturn(true);
		when(player2.hasSummon()).thenReturn(false);
		
		// Party member's summon not close enough to target.
		when(Util.checkIfInRange(AFFECT_RANGE, target, player3, true)).thenReturn(true);
		when(player3.hasSummon()).thenReturn(true);
		when(player3.getSummon()).thenReturn(servitor);
		when(Util.checkIfInRange(AFFECT_RANGE, target, servitor, true)).thenReturn(false);
		
		// Party member not close enough to target.
		when(Util.checkIfInRange(AFFECT_RANGE, target, player4, true)).thenReturn(false);
		
		assertEquals(List.of(player1, summon, player2, player3), PARTY.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test PARTY scope, when target without summon is not in party.")
	void testPartyScopeTargetWithoutSummonIsNotInParty() {
		when(target.isCharacter()).thenReturn(true);
		when(skill.getAffectRange()).thenReturn(AFFECT_RANGE);
		when(target.isInParty()).thenReturn(false);
		when(target.getActingPlayer()).thenReturn(player1);
		when(player1.hasSummon()).thenReturn(false);
		
		assertEquals(List.of(player1), PARTY.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test PARTY scope, when target with summon is not in party.")
	void testPartyScopeTargetWithSummonIsNotInParty() {
		when(target.isCharacter()).thenReturn(true);
		when(skill.getAffectRange()).thenReturn(AFFECT_RANGE);
		when(target.isInParty()).thenReturn(false);
		when(target.getActingPlayer()).thenReturn(player1);
		when(player1.hasSummon()).thenReturn(true);
		when(player1.getSummon()).thenReturn(summon);
		when(Util.checkIfInRange(AFFECT_RANGE, target, summon, true)).thenReturn(true);
		
		assertEquals(List.of(player1, summon), PARTY.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test PARTY scope, when target with summon too far is not in party.")
	void testPartyScopeTargetWithSummonTooFarIsNotInParty() {
		when(target.isCharacter()).thenReturn(true);
		when(skill.getAffectRange()).thenReturn(AFFECT_RANGE);
		when(target.isInParty()).thenReturn(false);
		when(target.getActingPlayer()).thenReturn(player1);
		when(player1.hasSummon()).thenReturn(true);
		when(player1.getSummon()).thenReturn(summon);
		when(Util.checkIfInRange(AFFECT_RANGE, target, summon, true)).thenReturn(false);
		
		assertEquals(List.of(player1), PARTY.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Implement: Test PARTY_PLEDGE scope.")
	void testPartyPledgeScope() {
		// TODO(Zoey76): Implement.
		assertEquals(List.of(), PARTY_PLEDGE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test PLEDGE scope, when caster is player in clan.")
	void testPledgeScopeCasterIsPlayerInClan() {
		when(skill.getAffectRange()).thenReturn(AFFECT_RANGE);
		when(skill.getAffectLimit()).thenReturn(AFFECT_LIMIT);
		when(target.isPlayer()).thenReturn(true);
		when(target.getActingPlayer()).thenReturn(player1);
		when(player1.getClan()).thenReturn(clan);
		when(clan.getMembers()).thenReturn(new L2ClanMember[] {
			clanMember1, //
			clanMember2, //
			clanMember3, //
			clanMember4
		});
		
		when(player1.isInDuel()).thenReturn(false, true, false);
		
		// Player, not in duel nor Olympiad, with summon.
		when(clanMember1.getPlayerInstance()).thenReturn(player1);
		when(player1.checkPvpSkill(player1, skill)).thenReturn(true);
		when(player1.isInOlympiadMode()).thenReturn(false);
		when(Util.checkIfInRange(AFFECT_RANGE, player1, player1, true)).thenReturn(true);
		when(player1.hasSummon()).thenReturn(true);
		when(player1.getSummon()).thenReturn(summon);
		when(Util.checkIfInRange(AFFECT_RANGE, player1, summon, true)).thenReturn(true);
		
		// Player in duel party, but different than target.
		when(clanMember2.getPlayerInstance()).thenReturn(player2);
		when(player1.getDuelId()).thenReturn(1);
		when(player2.getDuelId()).thenReturn(1);
		when(player1.isInParty()).thenReturn(true);
		when(player2.isInParty()).thenReturn(true);
		when(player1.getParty()).thenReturn(party1);
		when(party1.getLeaderObjectId()).thenReturn(1000);
		when(player2.getParty()).thenReturn(party2);
		when(party2.getLeaderObjectId()).thenReturn(2000);
		
		when(clanMember3.getPlayerInstance()).thenReturn(null);
		
		when(clanMember4.getPlayerInstance()).thenReturn(player4);
		when(player1.checkPvpSkill(player4, skill)).thenReturn(true);
		when(player1.isInOlympiadMode()).thenReturn(true);
		when(player1.getOlympiadGameId()).thenReturn(1);
		when(player4.getOlympiadGameId()).thenReturn(1);
		when(player1.getOlympiadSide()).thenReturn(1);
		when(player4.getOlympiadSide()).thenReturn(2);
		
		assertEquals(List.of(player1, summon), PLEDGE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test PLEDGE scope, when caster is player not in clan.")
	void testPledgeScopeCasterIsPlayerNotInClan() {
		when(skill.getAffectRange()).thenReturn(AFFECT_RANGE);
		when(skill.getAffectLimit()).thenReturn(AFFECT_LIMIT);
		when(target.isPlayer()).thenReturn(true);
		when(target.getActingPlayer()).thenReturn(player1);
		when(player1.getClan()).thenReturn(null);
		
		when(Util.checkIfInRange(AFFECT_RANGE, player1, player1, true)).thenReturn(true);
		when(player1.hasSummon()).thenReturn(true);
		when(player1.getSummon()).thenReturn(summon);
		when(Util.checkIfInRange(AFFECT_RANGE, player1, summon, true)).thenReturn(true);
		
		assertEquals(List.of(player1, summon), PLEDGE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test PLEDGE scope, when caster is NPC in clan.")
	void testPledgeScopeCasterIsNpcInClan() {
		when(skill.getAffectRange()).thenReturn(AFFECT_RANGE);
		when(skill.getAffectLimit()).thenReturn(AFFECT_LIMIT);
		when(npc1.isPlayer()).thenReturn(false);
		when(npc1.isNpc()).thenReturn(true);
		when(npc1.getTemplate()).thenReturn(npcTemplate);
		when(npcTemplate.getClans()).thenReturn(Set.of(1, 2));
		when(npc1.getKnownList()).thenReturn(npcKnownList);
		when(npcKnownList.getKnownCharactersInRadius(AFFECT_RANGE)).thenReturn(List.of(player2, npc2, npc3, summon));
		
		when(player2.isNpc()).thenReturn(false);
		
		when(npc2.isNpc()).thenReturn(true);
		when(npc1.isInMyClan(npc2)).thenReturn(true);
		
		when(npc3.isNpc()).thenReturn(true);
		when(npc1.isInMyClan(npc3)).thenReturn(false);
		
		when(summon.isNpc()).thenReturn(false);
		
		assertEquals(List.of(npc1, npc2), PLEDGE.affectTargets(npc1, npc1, skill));
	}
	
	@Test
	@DisplayName("Test POINT_BLANK scope.")
	void testPointBlankScope() {
		when(caster.isCharacter()).thenReturn(true);
		when(skill.getAffectLimit()).thenReturn(AFFECT_LIMIT);
		when(skill.getAffectObject()).thenReturn(affectObject);
		when(skill.getAffectRange()).thenReturn(AFFECT_RANGE);
		
		when(L2World.getInstance()).thenReturn(world);
		when(world.getVisibleObjects(caster, AFFECT_RANGE)) //
			.thenReturn(List.of(caster, npc2, npc3, summon));
		
		when(affectObject.affectObject(caster, caster)).thenReturn(true);
		when(affectObject.affectObject(caster, npc2)).thenReturn(false);
		when(affectObject.affectObject(caster, npc3)).thenReturn(false);
		when(affectObject.affectObject(caster, summon)).thenReturn(true);
		
		assertEquals(List.of(caster, summon), POINT_BLANK.affectTargets(caster, caster, skill));
	}
	
	@Test
	@DisplayName("Test RANGE scope.")
	void testRangeScope() {
		when(skill.getAffectLimit()).thenReturn(AFFECT_LIMIT);
		when(skill.getAffectRange()).thenReturn(AFFECT_RANGE);
		when(skill.getAffectObject()).thenReturn(affectObject);
		
		when(L2World.getInstance()).thenReturn(world);
		when(world.getVisibleObjects(caster, target, AFFECT_RANGE)).thenReturn(List.of(object1, servitor, player2, player3, player4, player5, player6, player7, player8));
		
		when(object1.isCharacter()).thenReturn(false);
		
		when(servitor.isCharacter()).thenReturn(true);
		when(servitor.isDead()).thenReturn(true);
		
		when(player2.isCharacter()).thenReturn(true);
		when(player2.isDead()).thenReturn(false);
		when(affectObject.affectObject(caster, player2)).thenReturn(true);
		
		when(player3.isCharacter()).thenReturn(true);
		when(player3.isDead()).thenReturn(false);
		when(affectObject.affectObject(caster, player3)).thenReturn(true);
		
		when(player4.isCharacter()).thenReturn(true);
		when(player4.isDead()).thenReturn(false);
		when(affectObject.affectObject(caster, player4)).thenReturn(true);
		
		when(player5.isCharacter()).thenReturn(true);
		when(player5.isDead()).thenReturn(false);
		when(affectObject.affectObject(caster, player5)).thenReturn(true);
		
		when(player6.isCharacter()).thenReturn(true);
		when(player6.isDead()).thenReturn(false);
		when(affectObject.affectObject(caster, player6)).thenReturn(false);
		
		assertEquals(List.of(player2, player3, player4, player5), RANGE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test RANGE_SORT_BY_HP scope.")
	void testRangeSortByHpScope() {
		when(skill.getAffectLimit()).thenReturn(AFFECT_LIMIT);
		when(skill.getAffectRange()).thenReturn(AFFECT_RANGE);
		when(L2World.getInstance()).thenReturn(world);
		when(world.getVisibleObjects(caster, target, AFFECT_RANGE)).thenReturn(List.of(target, object1, servitor, player2, player3));
		
		when(object1.isCharacter()).thenReturn(false);
		
		when(servitor.isCharacter()).thenReturn(true);
		when(servitor.isDead()).thenReturn(true);
		
		when(player2.isCharacter()).thenReturn(true);
		when(player2.isDead()).thenReturn(false);
		when(player2.getCurrentHp()).thenReturn(1000.0);
		when(player2.getMaxHp()).thenReturn(1000);
		
		when(player3.isCharacter()).thenReturn(true);
		when(player3.isDead()).thenReturn(false);
		when(player3.getCurrentHp()).thenReturn(1900.0);
		when(player3.getMaxHp()).thenReturn(2000);
		
		when(target.isCharacter()).thenReturn(true);
		when(target.isDead()).thenReturn(false);
		when(target.getCurrentHp()).thenReturn(500.0);
		when(target.getMaxHp()).thenReturn(1000);
		
		assertEquals(List.of(target, player3, player2), RANGE_SORT_BY_HP.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Implement: Test RING_RANGE scope.")
	void testRingRangeScope() {
		// TODO(Zoey76): Implement.
		assertEquals(List.of(), RING_RANGE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test SINGLE scope, when object is not affected.")
	void testSingleScopeObjectIsNotAffected() {
		when(skill.getAffectObject()).thenReturn(affectObject);
		when(affectObject.affectObject(caster, target)).thenReturn(false);
		
		assertEquals(List.of(), SINGLE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Test SINGLE scope.")
	void testSingleScope() {
		when(skill.getAffectObject()).thenReturn(affectObject);
		when(affectObject.affectObject(caster, target)).thenReturn(true);
		
		assertEquals(List.of(target), SINGLE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Implement: Test SQUARE scope.")
	void testSquareScope() {
		// TODO(Zoey76): Implement.
		assertEquals(List.of(), SQUARE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Implement: Test SQUARE_PB scope.")
	void testSquarePBScope() {
		// TODO(Zoey76): Implement.
		assertEquals(List.of(), SQUARE_PB.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Implement: Test STATIC_OBJECT_SCOPE scope.")
	void testStaticObjectScope() {
		// TODO(Zoey76): Implement.
		assertEquals(List.of(), STATIC_OBJECT_SCOPE.affectTargets(caster, target, skill));
	}
	
	@Test
	@DisplayName("Implement: Test WYVERN_SCOPE scope.")
	void testWyvernScope() {
		// TODO(Zoey76): Implement.
		assertEquals(List.of(), WYVERN_SCOPE.affectTargets(caster, target, skill));
	}
}
