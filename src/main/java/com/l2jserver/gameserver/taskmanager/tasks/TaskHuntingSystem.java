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
package com.l2jserver.gameserver.taskmanager.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.ExNevitAdventTimeChange;
import com.l2jserver.gameserver.taskmanager.Task;
import com.l2jserver.gameserver.taskmanager.TaskManager;
import com.l2jserver.gameserver.taskmanager.TaskManager.ExecutedTask;
import com.l2jserver.gameserver.taskmanager.TaskTypes;

/**
 * @author Maneco2
 * @since 2.6.3.0
 */
public class TaskHuntingSystem extends Task {
	protected static final Logger LOG = LoggerFactory.getLogger(TaskHuntingSystem.class);
	
	private static final String NAME = "hunting_system";
	
	private static final int RESET_HUNTING_BONUS = 0;
	private static final String UPDATE_CHARACTERS_HUNTING = "UPDATE characters SET hunting_bonus=?";
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task) {
		for (L2PcInstance player : L2World.getInstance().getPlayers()) {
			player.getHuntingSystem().setHuntingBonusTime(RESET_HUNTING_BONUS);
			if (player.isOnline()) {
				if (player.getHuntingSystem().isHuntingBonusTaskActive()) {
					player.sendPacket(new ExNevitAdventTimeChange(player.getHuntingSystem().getHuntingBonusTime(), false));
				}
			}
		}
		
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(UPDATE_CHARACTERS_HUNTING)) {
			ps.setInt(1, RESET_HUNTING_BONUS);
			ps.executeUpdate();
		} catch (Exception e) {
			LOG.warn("{}: Failed to execute SQL-Query for the reset of the Hunting-System!", getClass().getSimpleName(), e);
		}
		
		LOG.info("Hunting System reseted.");
	}
	
	@Override
	public void initializate() {
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
	}
}