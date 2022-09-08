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

import static com.l2jserver.gameserver.model.zone.ZoneId.SIEGE;
import static java.util.Comparator.comparingDouble;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.l2jserver.gameserver.GeoData;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.entity.TvTEvent;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.util.Util;

/**
 * Affect Scope.
 * @author Zoey76
 * @version 2.6.2.0
 */
public enum AffectScope {
	/** Affects Valakas. */
	BALAKAS_SCOPE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			// TODO(Zoey76): Implement.
			return List.of();
		}
	},
	/** Affects dead clan mates of the target. */
	DEAD_PLEDGE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			// TODO(Zoey76): Should work for NPC clans?
			if (!target.isPlayable()) {
				return List.of();
			}
			
			final var player = target.getActingPlayer();
			final var clanId = player.getClanId();
			if (clanId == 0) {
				// TODO(Zoey76): Should work without clan and over pet?
				return List.of();
			}
			
			final var affectLimit = skill.getAffectLimit();
			final var affectObject = skill.getAffectObject();
			final var targets = new ArrayList<L2Object>();
			final var inDuel = player.isInDuel();
			for (var object : L2World.getInstance().getVisibleObjects(target, skill.getAffectRange())) {
				if ((affectLimit > 0) && (targets.size() >= affectLimit)) {
					break;
				}
				
				if (!object.isPlayable()) {
					continue;
				}
				
				final var targetPlayer = object.getActingPlayer();
				if (targetPlayer == null) {
					continue;
				}
				
				if (clanId != targetPlayer.getClanId()) {
					continue;
				}
				
				if (inDuel) {
					if (player.getDuelId() != targetPlayer.getDuelId()) {
						continue;
					}
					if (player.isInParty() && targetPlayer.isInParty() && (player.getParty().getLeaderObjectId() != targetPlayer.getParty().getLeaderObjectId())) {
						continue;
					}
				}
				
				if (!player.checkPvpSkill(targetPlayer, skill)) {
					continue;
				}
				
				if (!TvTEvent.checkForTvTSkill(player, targetPlayer, skill)) {
					continue;
				}
				
				if (player.isInOlympiadMode()) {
					if (player.getOlympiadGameId() != targetPlayer.getOlympiadGameId()) {
						continue;
					}
					
					if (player.getOlympiadSide() != targetPlayer.getOlympiadGameId()) {
						continue;
					}
				}
				
				if (targetPlayer.isInsideZone(SIEGE) && !targetPlayer.isInSiege()) {
					continue;
				}
				
				if (!affectObject.affectObject(player, targetPlayer)) {
					continue;
				}
				
				targets.add(targetPlayer);
			}
			return targets;
		}
	},
	/** Affects dead command channel mates of the target. */
	DEAD_UNION {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			if (!target.isPlayable()) {
				return List.of();
			}
			
			final var player = target.getActingPlayer();
			final var affectLimit = skill.getAffectLimit();
			final var affectObject = skill.getAffectObject();
			final var targets = new ArrayList<L2Object>();
			for (L2Object object : L2World.getInstance().getVisibleObjects(target, skill.getAffectRange())) {
				if ((affectLimit > 0) && (targets.size() >= affectLimit)) {
					break;
				}
				
				if (!object.isCharacter()) {
					continue;
				}
				
				final var creature = (L2Character) object;
				if (!player.isInCommandChannelWith(creature)) {
					continue;
				}
				
				if (!creature.isDead()) {
					continue;
				}
				
				if (!affectObject.affectObject(player, creature)) {
					continue;
				}
				
				targets.add(creature);
			}
			return targets;
		}
	},
	/** Affects fan area. */
	FAN {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			final var headingAngle = Util.calculateAngleFrom(caster, target);
			final var affectLimit = skill.getAffectLimit();
			final var fanRange = skill.getFanRange();
			final var fanStartingAngle = fanRange[1];
			final var fanRadius = fanRange[2];
			final var fanAngle = fanRange[3];
			final var affectObject = skill.getAffectObject();
			final var targets = new ArrayList<L2Object>();
			for (var object : L2World.getInstance().getVisibleObjects(caster, fanRadius)) {
				if ((affectLimit > 0) && (targets.size() >= affectLimit)) {
					break;
				}
				
				if (!object.isCharacter()) {
					continue;
				}
				
				final var creature = (L2Character) object;
				if (creature.isDead()) {
					continue;
				}
				
				if (Math.abs(Util.calculateAngleFrom(caster, creature) - (headingAngle + fanStartingAngle)) > fanAngle / 2) {
					continue;
				}
				
				if (!affectObject.affectObject(caster, creature)) {
					continue;
				}
				
				if (!GeoData.getInstance().canSeeTarget(caster, creature)) {
					continue;
				}
				
				targets.add(creature);
			}
			return targets;
		}
	},
	/** Affects nothing. */
	NONE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			return List.of();
		}
	},
	/** Affects party members of the target. */
	PARTY {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			if (!target.isCharacter()) {
				return List.of();
			}
			
			final var affectLimit = skill.getAffectLimit();
			final var affectRange = skill.getAffectRange();
			final var targets = new ArrayList<L2Object>();
			final var creature = (L2Character) target;
			if (creature.isInParty()) {
				for (var partyMember : creature.getParty().getMembers()) {
					if ((affectLimit > 0) && (targets.size() >= affectLimit)) {
						break;
					}
					
					if (!Util.checkIfInRange(affectRange, creature, partyMember, true)) {
						continue;
					}
					
					// TODO(Zoey76): Check affect object?
					
					targets.add(partyMember);
					
					if (partyMember.hasSummon()) {
						final var summon = partyMember.getSummon();
						if (Util.checkIfInRange(affectRange, creature, summon, true)) {
							targets.add(summon);
						}
					}
				}
			} else {
				final var player = target.getActingPlayer();
				targets.add(player);
				if (player.hasSummon()) {
					final var summon = player.getSummon();
					if (Util.checkIfInRange(affectRange, creature, summon, true)) {
						targets.add(summon);
					}
				}
			}
			return targets;
		}
	},
	/** Affects party and clan mates of the target. */
	PARTY_PLEDGE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			final var targets = new HashSet<L2Object>();
			targets.addAll(PARTY.affectTargets(caster, target, skill));
			targets.addAll(PLEDGE.affectTargets(caster, target, skill));
			return new LinkedList<>(targets);
		}
	},
	/** Affects clan mates of the target. */
	PLEDGE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			final var affectRange = skill.getAffectRange();
			final var affectLimit = skill.getAffectLimit();
			final var targets = new ArrayList<L2Object>();
			if (target.isPlayer()) {
				final var targetPlayer = target.getActingPlayer();
				final var clan = targetPlayer.getClan();
				if (clan != null) {
					for (var clanMember : clan.getMembers()) {
						if ((affectLimit > 0) && (targets.size() >= affectLimit)) {
							break;
						}
						
						final var clanMemberPlayer = clanMember.getPlayerInstance();
						if (clanMemberPlayer == null) {
							continue;
						}
						
						if (targetPlayer.isInDuel()) {
							if (targetPlayer.getDuelId() != clanMemberPlayer.getDuelId()) {
								continue;
							}
							if (targetPlayer.isInParty() && clanMemberPlayer.isInParty() && //
							(targetPlayer.getParty().getLeaderObjectId() != clanMemberPlayer.getParty().getLeaderObjectId())) {
								continue;
							}
						}
						
						if (!targetPlayer.checkPvpSkill(clanMemberPlayer, skill)) {
							continue;
						}
						
						if (!TvTEvent.checkForTvTSkill(targetPlayer, clanMemberPlayer, skill)) {
							continue;
						}
						
						if (targetPlayer.isInOlympiadMode()) {
							if (targetPlayer.getOlympiadGameId() != clanMemberPlayer.getOlympiadGameId()) {
								continue;
							}
							
							if (targetPlayer.getOlympiadSide() != clanMemberPlayer.getOlympiadSide()) {
								continue;
							}
						}
						
						if (Util.checkIfInRange(affectRange, targetPlayer, clanMemberPlayer, true)) {
							targets.add(clanMemberPlayer);
						}
						
						if (clanMemberPlayer.hasSummon()) {
							final var summon = clanMemberPlayer.getSummon();
							if (Util.checkIfInRange(affectRange, targetPlayer, summon, true)) {
								targets.add(summon);
							}
						}
					}
				} else {
					if (Util.checkIfInRange(affectRange, targetPlayer, targetPlayer, true)) {
						targets.add(targetPlayer);
					}
					
					if (targetPlayer.hasSummon()) {
						final var summon = targetPlayer.getSummon();
						if (Util.checkIfInRange(affectRange, targetPlayer, summon, true)) {
							targets.add(summon);
						}
					}
				}
			} else if (target.isNpc()) {
				final var npc = (L2Npc) target;
				targets.add(target);
				
				final var clans = npc.getTemplate().getClans();
				if ((clans == null) || clans.isEmpty()) {
					return targets;
				}
				
				for (var creature : npc.getKnownList().getKnownCharactersInRadius(affectRange)) {
					if ((affectLimit > 0) && (targets.size() >= affectLimit)) {
						break;
					}
					
					if (!creature.isNpc()) {
						continue;
					}
					
					if (!npc.isInMyClan((L2Npc) creature)) {
						continue;
					}
					
					targets.add(creature);
				}
			}
			return targets;
		}
	},
	/** Affects point blank targets, using caster as point of origin. */
	POINT_BLANK {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			if (!target.isCharacter()) {
				return List.of();
			}
			
			final var affectLimit = skill.getAffectLimit();
			final var affectObject = skill.getAffectObject();
			final var creature = (L2Character) target;
			return L2World.getInstance().getVisibleObjects(target, skill.getAffectRange()) //
				.stream() //
				.filter(c -> affectObject.affectObject(creature, c)) //
				.limit(affectLimit > 0 ? affectLimit : Integer.MAX_VALUE) //
				.collect(Collectors.toList());
		}
	},
	/** Affects ranged targets, using selected target as point of origin. */
	RANGE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			final var affectLimit = skill.getAffectLimit();
			return L2World.getInstance().getVisibleObjects(caster, target, skill.getAffectRange()) //
				.stream() //
				.filter(L2Object::isCharacter) //
				.map(o -> (L2Character) o) //
				.filter(c -> !c.isDead()) //
				.filter(c -> skill.getAffectObject().affectObject(caster, c)) //
				.limit(affectLimit > 0 ? affectLimit : Integer.MAX_VALUE) //
				.collect(Collectors.toList());
		}
	},
	/** Affects ranged targets sorted by HP, using selected target as point of origin. */
	RANGE_SORT_BY_HP {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			final var affectLimit = skill.getAffectLimit();
			return L2World.getInstance().getVisibleObjects(caster, target, skill.getAffectRange()) //
				.stream() //
				.filter(L2Object::isCharacter) //
				.map(o -> (L2Character) o) //
				.filter(c -> !c.isDead()) //
				.sorted(comparingDouble(c -> c.getCurrentHp() / c.getMaxHp())) //
				.limit(affectLimit > 0 ? affectLimit : Integer.MAX_VALUE) //
				.collect(Collectors.toList());
		}
	},
	/** Affects ranged targets, using selected target as point of origin. */
	RING_RANGE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			// TODO(Zoey76): Implement.
			return List.of();
		}
	},
	/** Affects a single target. */
	SINGLE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			if (!skill.getAffectObject().affectObject(caster, target)) {
				// TODO(Zoey76): Add message?
				return List.of();
			}
			return List.of(target);
		}
	},
	/** Affects targets inside an square area, using selected target as point of origin. */
	SQUARE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			// TODO(Zoey76): Implement.
			return List.of();
		}
	},
	/** Affects targets inside an square area, using caster as point of origin. */
	SQUARE_PB {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			// TODO(Zoey76): Implement.
			return List.of();
		}
	},
	/** Affects static object targets. */
	STATIC_OBJECT_SCOPE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			// TODO(Zoey76): Implement.
			return List.of();
		}
	},
	/** Affects wyvern. */
	WYVERN_SCOPE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill) {
			// TODO(Zoey76): Implement.
			return List.of();
		}
	};
	
	public abstract List<L2Object> affectTargets(L2Character caster, L2Object target, Skill skill);
}
