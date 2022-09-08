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

import static com.l2jserver.gameserver.model.zone.ZoneId.SIEGE;
import static com.l2jserver.gameserver.network.SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE;
import static com.l2jserver.gameserver.network.SystemMessageId.REJECT_RESURRECTION;
import static com.l2jserver.gameserver.network.SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Operator Resurrection condition.
 * @author Zoey76
 * @version 2.6.3.0
 */
public class ConditionOpResurrection extends Condition {
	
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item) {
		if (effected == null || !(effected.isPlayer() || effected.isPet()) || !effected.isDead()) {
			return false;
		}
		
		if (effected.isResurrectionBlocked()) {
			effector.sendPacket(REJECT_RESURRECTION);
			return false;
		}
		
		final L2PcInstance player = effected.getActingPlayer();
		if (effected.isPlayer() && player.isReviveRequested()) {
			effector.sendPacket(RES_HAS_ALREADY_BEEN_PROPOSED);
			return false;
		}
		
		if (effected.isPet() && player.isRevivingPet()) {
			effector.sendPacket(RES_HAS_ALREADY_BEEN_PROPOSED);
			return false;
		}
		
		if (player.isInsideZone(SIEGE) && !player.isInSiege()) {
			effector.sendPacket(CANNOT_BE_RESURRECTED_DURING_SIEGE);
			return false;
		}
		
		if (player.isFestivalParticipant()) {
			effector.sendMessage("You may not resurrect participants in a festival.");
			return false;
		}
		return true;
	}
}