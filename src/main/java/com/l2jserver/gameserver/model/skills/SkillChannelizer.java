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
package com.l2jserver.gameserver.model.skills;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jserver.gameserver.GeoData;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.enums.ShotType;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.targets.AffectScope;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jserver.gameserver.util.Util;

/**
 * Skill Channelizer implementation.
 * @author UnAfraid
 */
public class SkillChannelizer implements Runnable {
	private static final Logger _log = Logger.getLogger(SkillChannelizer.class.getName());
	
	private final L2Character _channelizer;
	private List<L2Character> _channelized;
	private L2Character _initialChannelized;
	
	private Skill _skill;
	private volatile ScheduledFuture<?> _task = null;
	
	public SkillChannelizer(L2Character channelizer) {
		_channelizer = channelizer;
	}
	
	public L2Character getChannelizer() {
		return _channelizer;
	}
	
	public List<L2Character> getChannelized() {
		return _channelized;
	}
	
	public boolean hasChannelized() {
		return _channelized != null;
	}
	
	public void startChanneling(Skill skill) {
		// Verify for same status.
		if (isChanneling()) {
			_log.warning("Character: " + _channelizer + " is attempting to channel skill but he already does!");
			return;
		}
		
		// If affect scope is SINGLE, save target to avoid target changing during channeling.
		if (skill.getAffectScope() == AffectScope.SINGLE) {
			final var creatures = skill.getTargets(_channelizer);
			if (!creatures.isEmpty()) {
				final var creature = creatures.get(0); // Get the first target to lock it on
				_initialChannelized = ((L2Character) creature);
				_initialChannelized.getSkillChannelized().addChannelizer(skill.getChannelingSkillId(), getChannelizer());
			}
		}
		
		// Start channeling.
		_skill = skill;
		_task = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, skill.getChannelingTickInitialDelay(), skill.getChannelingTickInterval());
	}
	
	public void stopChanneling() {
		// Verify for same status.
		if (!isChanneling()) {
			_log.warning("Character: " + _channelizer + " is attempting to stop channel skill but he does not!");
			return;
		}
		
		// Cancel the task and unset it.
		_task.cancel(true);
		_task = null;
		
		// Recalculate channeling state, cancel target channelization and unset it.
		if (_channelized != null) {
			for (L2Character chars : _channelized) {
				cleanupChannelization(_skill, chars);
			}
		}
		
		// unset channelized
		_channelized = null;
		_initialChannelized = null;
		
		// unset skill.
		_skill = null;
	}
	
	public Skill getSkill() {
		return _skill;
	}
	
	public boolean isChanneling() {
		return _task != null;
	}
	
	@Override
	public void run() {
		if (!isChanneling()) {
			return;
		}
		if (_skill == null) {
			return;
		}
		try {
			if (_skill.getMpPerChanneling() > 0) {
				// Validate mana per tick.
				if (_channelizer.getCurrentMp() < _skill.getMpPerChanneling()) {
					if (_channelizer.isPlayer()) {
						_channelizer.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
					}
					_channelizer.abortCast();
					return;
				}
				
				// Reduce mana per tick
				_channelizer.reduceCurrentMp(_skill.getMpPerChanneling());
			}
			
			// Apply channeling skills on the targets.
			if (_skill.getChannelingSkillId() > 0) {
				final Skill baseSkill = SkillData.getInstance().getSkill(_skill.getChannelingSkillId(), 1);
				if (baseSkill == null) {
					_log.warning(getClass().getSimpleName() + ": skill " + _skill + " couldn't find effect id skill: " + _skill.getChannelingSkillId() + " !");
					_channelizer.abortCast();
					return;
				}
				
				final var targets = new LinkedList<L2Character>();
				
				// If _singleChannelized exists, keep effects only on that target, else affect the new target.
				if (_initialChannelized != null) {
					targets.add(_initialChannelized);
					_initialChannelized.getSkillChannelized().addChannelizer(_skill.getChannelingSkillId(), getChannelizer());
				} else {
					for (var object : _skill.getTargets(_channelizer)) {
						if (object.isCharacter()) {
							final var creature = (L2Character) object;
							targets.add(creature);
							creature.getSkillChannelized().addChannelizer(_skill.getChannelingSkillId(), getChannelizer());
						}
					}
				}
				
				if (targets.isEmpty()) {
					return;
				}
				_channelized = targets;
				
				for (L2Character character : _channelized) {
					if (!Util.checkIfInRange(_skill.getEffectRange(), _channelizer, character, true)) {
						if (_initialChannelized != null) {
							_channelizer.abortCast();
							_channelizer.sendPacket(SystemMessageId.TARGET_TOO_FAR);
						}
						continue;
					} else if (!GeoData.getInstance().canSeeTarget(_channelizer, character)) {
						if (_initialChannelized != null) {
							_channelizer.abortCast();
							_channelizer.sendPacket(SystemMessageId.CANT_SEE_TARGET);
						}
						continue;
					} else {
						final int maxSkillLevel = SkillData.getInstance().getMaxLevel(_skill.getChannelingSkillId());
						final int skillLevel = Math.min(character.getSkillChannelized().getChannerlizersSize(_skill.getChannelingSkillId()), maxSkillLevel);
						final BuffInfo info = character.getEffectList().getBuffInfoBySkillId(_skill.getChannelingSkillId());
						
						if ((info == null) || (info.getSkill().getLevel() < skillLevel)) {
							final Skill skill = SkillData.getInstance().getSkill(_skill.getChannelingSkillId(), skillLevel);
							if (skill == null) {
								_log.warning(getClass().getSimpleName() + ": Non existent channeling skill requested: " + _skill);
								_channelizer.abortCast();
								return;
							}
							
							// Update PvP status
							if (character.isPlayable() && getChannelizer().isPlayer() && skill.isBad()) {
								((L2PcInstance) getChannelizer()).updatePvPStatus(character);
							}
							
							skill.applyEffects(getChannelizer(), character);
							
							// Reduce shots.
							if (_skill.useSpiritShot()) {
								_channelizer.setChargedShot(_channelizer.isChargedShot(ShotType.BLESSED_SPIRITSHOTS) ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
							} else {
								_channelizer.setChargedShot(ShotType.SOULSHOTS, false);
							}
							
							// Shots are re-charged every cast.
							_channelizer.rechargeShots(_skill.useSoulShot(), _skill.useSpiritShot());
						}
						_channelizer.broadcastPacket(new MagicSkillLaunched(_channelizer, _skill.getId(), _skill.getLevel(), character));
					}
				}
			}
		} catch (Exception e) {
			_log.warning("Error while channelizing skill: " + _skill + " channelizer: " + _channelizer + " channelized: " + _channelized + "; " + e.getMessage());
		}
	}
	
	private void cleanupChannelization(Skill skill, L2Character character) {
		final BuffInfo info = character.getEffectList().getBuffInfoBySkillId(skill.getChannelingSkillId());
		if (info != null) {
			final int channerlizersSize = character.getSkillChannelized().getChannerlizersSize(skill.getChannelingSkillId());
			// If this is the last channelizer for the target, remove attached effects, else decrease affecting skill level by 1.
			if (channerlizersSize == 1) {
				character.getEffectList().remove(false, info);
			} else if (channerlizersSize > 1) {
				final int maxSkillLevel = SkillData.getInstance().getMaxLevel(skill.getChannelingSkillId());
				final int skillLevel = Math.min(character.getSkillChannelized().getChannerlizersSize(skill.getChannelingSkillId()), maxSkillLevel);
				final Skill currentSkill = SkillData.getInstance().getSkill(skill.getChannelingSkillId(), skillLevel);
				character.getEffectList().stopSkillEffects(true, currentSkill);
				final Skill nextSkill = SkillData.getInstance().getSkill(skill.getChannelingSkillId(), skillLevel - 1);
				nextSkill.applyEffects(getChannelizer(), character);
			}
		}
		character.getSkillChannelized().removeChannelizer(skill.getChannelingSkillId(), getChannelizer());
	}
}
