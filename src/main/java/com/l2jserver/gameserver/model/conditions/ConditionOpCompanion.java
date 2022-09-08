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

import static com.l2jserver.gameserver.model.conditions.ConditionOpCompanion.CompanionType.PET;
import static com.l2jserver.gameserver.network.SystemMessageId.S1_CANNOT_BE_USED;
import static com.l2jserver.gameserver.network.serverpackets.SystemMessage.getSystemMessage;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Operator Companion condition.
 * @author Zoey76
 * @version 2.6.3.0
 */
public class ConditionOpCompanion extends Condition {
	private final CompanionType type;
	
	public ConditionOpCompanion(String type) {
		this.type = CompanionType.valueOf(type);
	}
	
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item) {
		if (type == PET) {
			if (!effected.isPet()) {
				effector.sendPacket(getSystemMessage(S1_CANNOT_BE_USED).addSkillName(skill));
				return false;
			}
			return true;
		}
		return false;
	}
	
	public enum CompanionType {
		PET
	}
}
