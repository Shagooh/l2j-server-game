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

import static com.l2jserver.gameserver.config.Configuration.npc;
import static com.l2jserver.gameserver.network.SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED;

import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Checks Sweeper conditions:
 * <ul>
 * <li>Minimum checks, player not null, skill not null.</li>
 * <li>Checks if the target isn't null, is dead and spoiled.</li>
 * <li>Checks if the sweeper player is the target spoiler, or is in the spoiler party.</li>
 * <li>Checks if the corpse is too old.</li>
 * <li>Checks inventory limit and weight max load won't be exceed after sweep.</li>
 * </ul>
 * If two or more conditions aren't meet at the same time, one message per condition will be shown.
 * @author Zoey76
 */
public class ConditionPlayerCanSweep extends Condition {
	private final boolean _val;
	
	public ConditionPlayerCanSweep(boolean val) {
		_val = val;
	}
	
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item) {
		boolean canSweep = false;
		final var sweeper = effector.getActingPlayer();
		if (sweeper != null) {
			if (skill != null) {
				final var targets = skill.getTargets(sweeper);
				for (var object : targets) {
					if (!object.isAttackable()) {
						continue;
					}
					
					final var target = (L2Attackable) object;
					if (!target.isDead()) {
						continue;
					}
					
					if (target.isSpoiled()) {
						canSweep = target.checkSpoilOwner(sweeper, true);
						canSweep &= !target.isOldCorpse(sweeper, npc().getCorpseConsumeSkillAllowedTimeBeforeDecay(), true);
						canSweep &= sweeper.getInventory().checkInventorySlotsAndWeight(target.getSpoilLootItems(), true, true);
					} else {
						sweeper.sendPacket(SWEEPER_FAILED_TARGET_NOT_SPOILED);
					}
				}
			}
		}
		return (_val == canSweep);
	}
}
