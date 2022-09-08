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

import static com.l2jserver.gameserver.config.Configuration.npc;
import static com.l2jserver.gameserver.model.actor.instance.L2StaticObjectInstance.FLAGPOLE;
import static com.l2jserver.gameserver.model.zone.ZoneId.PEACE;
import static com.l2jserver.gameserver.model.zone.ZoneId.PVP;
import static com.l2jserver.gameserver.network.SystemMessageId.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE;
import static com.l2jserver.gameserver.network.SystemMessageId.CANNOT_USE_ON_YOURSELF;
import static com.l2jserver.gameserver.network.SystemMessageId.CANT_SEE_TARGET;
import static com.l2jserver.gameserver.network.SystemMessageId.INCORRECT_TARGET;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.gameserver.GeoData;
import com.l2jserver.gameserver.instancemanager.DuelManager;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jserver.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.holders.SkillUseHolder;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Target type enumerated.
 * @author Zoey76
 * @version 2.6.3.0
 */
public enum TargetType {
	/** Advance Head Quarters (Outposts). */
	ADVANCE_BASE {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if ((target == null) || !target.isNpc() || (target.getId() != OUTPOST) || ((L2Npc) target).isDead()) {
				caster.sendPacket(INCORRECT_TARGET);
				return null;
			}
			return target;
		}
	},
	/** Enemies in high terrain or protected by castle walls and doors. */
	ARTILLERY {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if ((target == null) || !target.isDoor() || ((L2DoorInstance) target).isDead() || !target.isAutoAttackable(caster)) {
				caster.sendPacket(INCORRECT_TARGET);
				return null;
			}
			return target;
		}
	},
	/** Doors or treasure chests. */
	DOOR_TREASURE {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if ((target == null) || (!target.isDoor() && !(target instanceof L2ChestInstance))) {
				caster.sendPacket(INCORRECT_TARGET);
				return null;
			}
			return target;
		}
	},
	/** Any enemies (included allies). */
	ENEMY {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if ((target == null) || ((L2Character) target).isDead() || (caster.getObjectId() == target.getObjectId()) || !target.isCharacter()) {
				return null;
			}
			
			if (target.isNpc()) {
				if (target.isAttackable()) {
					return target;
				}
				caster.sendPacket(INCORRECT_TARGET);
				return null;
			}
			
			final var player = caster.getActingPlayer();
			if (player != null) {
				if (player.doesSkillNeedCtrl((L2Character) target, skill) && !getCurrentPlayableSkill(caster).isCtrlPressed()) {
					caster.sendPacket(INCORRECT_TARGET);
					return null;
				}
			}
			return target;
		}
	},
	/** Friendly. */
	ENEMY_NOT {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if ((target == null) || !target.isCharacter() || ((L2Character) target).isDead() || target.isAutoAttackable(caster)) {
				caster.sendPacket(INCORRECT_TARGET);
				return null;
			}
			return target;
		}
	},
	/** Only enemies (not included allies). */
	ENEMY_ONLY {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if ((target == null) || !target.isCharacter() || (caster.getObjectId() == target.getObjectId()) || ((L2Character) target).isDead() || !target.isAutoAttackable(caster)) {
				caster.sendPacket(INCORRECT_TARGET);
				return null;
			}
			
			if (target.isNpc()) {
				if (target.isAttackable()) {
					return target;
				}
				caster.sendPacket(INCORRECT_TARGET);
				return null;
			}
			
			final var player = caster.getActingPlayer();
			if (player == null) {
				return null;
			}
			
			// In Olympiad, different sides.
			if (player.isInOlympiadMode()) {
				final var targetPlayer = target.getActingPlayer();
				if ((targetPlayer != null) && (player.getOlympiadSide() != targetPlayer.getOlympiadSide())) {
					return target;
				}
				player.sendPacket(INCORRECT_TARGET);
				return null;
			}
			
			final var targetCreature = (L2Character) target;
			// In Duel, different sides.
			if (player.isInDuelWith(targetCreature)) {
				final var targetPlayer = target.getActingPlayer();
				final var duel = DuelManager.getInstance().getDuel(player.getDuelId());
				final var teamA = duel.getTeamA();
				final var teamB = duel.getTeamB();
				if (teamA.contains(player) && teamB.contains(targetPlayer) || //
				teamB.contains(player) && teamA.contains(targetPlayer)) {
					return target;
				}
				player.sendPacket(INCORRECT_TARGET);
				return null;
			}
			
			// Not in same party.
			if (player.isInPartyWith(targetCreature)) {
				player.sendPacket(INCORRECT_TARGET);
				return null;
			}
			
			// In PVP Zone.
			if (player.isInsideZone(PVP)) {
				return target;
			}
			
			// Not in same clan.
			if (player.isInClanWith(targetCreature)) {
				player.sendPacket(INCORRECT_TARGET);
				return null;
			}
			
			// TODO(Zoey76): Validate.
			// Not in same alliance.
			if (player.isInAllyWith(targetCreature)) {
				player.sendPacket(INCORRECT_TARGET);
				return null;
			}
			
			// TODO(Zoey76): Validate.
			// Not in same command channel.
			if (player.isInCommandChannelWith(targetCreature)) {
				player.sendPacket(INCORRECT_TARGET);
				return null;
			}
			
			// Not on same Siege Side.
			if (player.isOnSameSiegeSideWith(targetCreature)) {
				player.sendPacket(INCORRECT_TARGET);
				return null;
			}
			
			// At Clan War.
			if (player.isAtWarWith(targetCreature)) {
				return target;
			}
			
			// Cannot PvP.
			if (!player.checkIfPvP(targetCreature) && (target.isPlayable() && target.getActingPlayer().getKarma() == 0)) {
				player.sendPacket(INCORRECT_TARGET);
				return null;
			}
			
			return target;
		}
	},
	/** Fortress's Flagpole. */
	FORTRESS_FLAGPOLE {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if (!(target instanceof L2StaticObjectInstance) || (((L2StaticObjectInstance) target).getType() != FLAGPOLE)) {
				return null;
			}
			return target;
		}
	},
	/** Ground. */
	GROUND {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			final var player = caster.getActingPlayer();
			if (player == null) {
				return null;
			}
			
			final var worldPosition = player.getCurrentSkillWorldPosition();
			if ((worldPosition == null) || !GeoData.getInstance().canSeeTarget(player, worldPosition)) {
				caster.sendPacket(CANT_SEE_TARGET);
				return null;
			}
			
			if (skill.isBad() && caster.isInsideZone(PEACE)) {
				caster.sendPacket(A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE);
				return null;
			}
			return caster;
		}
	},
	/** Holy Artifacts from sieges. */
	HOLYTHING {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if (!(target instanceof L2ArtefactInstance)) {
				return null;
			}
			return target;
		}
	},
	/** Items. */
	ITEM {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if (!(target instanceof L2ItemInstance)) {
				return null;
			}
			return caster;
		}
	},
	/** Nothing. */
	NONE {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			return caster;
		}
	},
	/** NPC corpses. */
	NPC_BODY {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if ((target == null) || !target.isNpc() || !((L2Character) target).isDead()) {
				caster.sendPacket(INCORRECT_TARGET);
				return null;
			}
			
			// TODO(Zoey76): Review this validation.
			if (skill.hasEffectType(L2EffectType.SUMMON) && target.isServitor() && (target.getActingPlayer() != null) && (target.getActingPlayer().getObjectId() == caster.getObjectId())) {
				return null;
			}
			
			// TODO(Zoey76): This is validated with condition.
			if (skill.hasEffectType(L2EffectType.HP_DRAIN) && ((L2Attackable) target).isOldCorpse(caster.getActingPlayer(), npc().getCorpseConsumeSkillAllowedTimeBeforeDecay(), true)) {
				return null;
			}
			return target;
		}
	},
	/** Others, except caster. */
	OTHERS {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if ((target == null) || target == caster) {
				caster.sendPacket(CANNOT_USE_ON_YOURSELF);
				return null;
			}
			return target;
		}
	},
	/** Player Controlled corpses. */
	PC_BODY {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if ((target == null) || !(target.isPlayer() || target.isPet()) || !((L2Character) target).isDead()) {
				caster.sendPacket(INCORRECT_TARGET);
				return null;
			}
			return target;
		}
	},
	/** Self. */
	SELF {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			return caster;
		}
	},
	/** Servitor, not pet. */
	SUMMON {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if (!caster.hasServitor()) {
				return null;
			}

			return caster.getSummon();
		}
	},
	/** Anything targetable. */
	TARGET {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			if (target == null) {
				return null;
			}
			
			final var player = caster.getActingPlayer();
			if (player != null) {
				if (target.isAutoAttackable(caster)) {
					final var currentSkill = getCurrentPlayableSkill(caster);
					if ((currentSkill != null) && !currentSkill.isCtrlPressed()) {
						caster.sendPacket(INCORRECT_TARGET);
						return null;
					}
				}
			}
			return target;
		}
	},
	/** Wyverns. */
	WYVERN_TARGET {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			// TODO(Zoey76): Implement.
			if (caster.isPlayable()) {
				LOG.warn("Target type {} not implmented for skill {}!", skill.getTargetType(), skill);
			}
			return target;
		}
	},
	@Deprecated
	ONE {
		@Override
		public L2Object getTarget(Skill skill, L2Character caster, L2Object target) {
			// TODO(Zoey76): Remove custom target type.
			if (caster.isPlayable()) {
				LOG.warn("Outdated {} with target type {} used on {} by {}!", skill, skill.getTargetType(), target, caster);
			}
			skill.updateTargetSystem(TARGET, AffectScope.SINGLE, AffectObjectStaticImpl.ALL);
			return TARGET.getTarget(skill, caster, target);
		}
	};
	
	public static final L2Object[] EMPTY_TARGET_LIST = new L2Object[0];
	
	private static final Logger LOG = LoggerFactory.getLogger(TargetType.class);
	
	private static final int OUTPOST = 36590;
	
	public abstract L2Object getTarget(Skill skill, L2Character caster, L2Object target);
	
	public final List<L2Object> getTargets(Skill skill, L2Character caster, L2Object target) {
		final var actualTarget = getTarget(skill, caster, target);
		if (actualTarget == null) {
			return List.of();
		}
		return skill.getAffectScope().affectTargets(caster, actualTarget, skill);
	}

	private static final SkillUseHolder getCurrentPlayableSkill(L2Character caster) {
		if (caster.isSummon()) {
			return caster.getActingPlayer().getCurrentPetSkill();
		}
		return caster.getActingPlayer().getCurrentSkill();
	}
}
