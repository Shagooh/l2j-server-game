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
package com.l2jserver.gameserver.instancemanager;

import static com.l2jserver.gameserver.config.Configuration.graciaSeeds;

import java.util.Calendar;
import java.util.logging.Logger;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.instancemanager.tasks.UpdateSoDStateTask;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.quest.Quest;

public final class GraciaSeedsManager {
	
	private static final Logger _log = Logger.getLogger(GraciaSeedsManager.class.getName());
	
	public static String ENERGY_SEEDS = "EnergySeeds";
	public static String SOD_DEFENCE = "Defence";
	
	private static final byte SOITYPE = 2;
	
	private static final byte SOATYPE = 3;
	
	// Seed of Destruction
	private static final int EDRIC = 32527;
	private static final Location EDRIC_SPAWN_LOCATION = new Location(-248525, 250048, 4307, 24576);
	private static final byte SODTYPE = 1;
	private L2Npc edricSpawn = null;
	private int _SoDTiatKilled = 0;
	private int _SoDState = 1;
	private final Calendar _SoDLastStateChangeDate;
	
	protected GraciaSeedsManager() {
		_SoDLastStateChangeDate = Calendar.getInstance();
		loadData();
		handleSodStages();
	}
	
	public void saveData(byte seedType) {
		switch (seedType) {
			case SODTYPE:
				// Seed of Destruction
				GlobalVariablesManager.getInstance().set("SoDState", _SoDState);
				GlobalVariablesManager.getInstance().set("SoDTiatKilled", _SoDTiatKilled);
				GlobalVariablesManager.getInstance().set("SoDLSCDate", _SoDLastStateChangeDate.getTimeInMillis());
				break;
			case SOITYPE:
				// Seed of Infinity
				break;
			case SOATYPE:
				// Seed of Annihilation
				break;
			default:
				_log.warning(getClass().getSimpleName() + ": Unknown SeedType in SaveData: " + seedType);
				break;
		}
	}
	
	public void loadData() {
		// Seed of Destruction variables
		if (GlobalVariablesManager.getInstance().hasVariable("SoDState")) {
			_SoDState = GlobalVariablesManager.getInstance().getInt("SoDState");
			_SoDTiatKilled = GlobalVariablesManager.getInstance().getInt("SoDTiatKilled", _SoDTiatKilled);
			_SoDLastStateChangeDate.setTimeInMillis(GlobalVariablesManager.getInstance().getLong("SoDLSCDate"));
		} else {
			// save Initial values
			saveData(SODTYPE);
		}
	}
	
	private void handleSodStages() {
		switch (_SoDState) {
			case 1:
				// Despawn Edric(Remnant Manager) and do nothing else, players should kill Tiat a few times
				despawnSoDRemnantManager();
				break;
			case 2:
				// Conquest Complete state, if too much time is passed than change to defense state
				long timePast = System.currentTimeMillis() - _SoDLastStateChangeDate.getTimeInMillis();
				if (timePast >= graciaSeeds().getStage2Length()) {
					// change to Defend state
					setSoDState(5, true, false);
				} else {
					// Schedule change to Defend state
					ThreadPoolManager.getInstance().scheduleEffect(new UpdateSoDStateTask(), graciaSeeds().getStage2Length() - timePast);
				}
				// Spawn Edric(Remnant Manager) for solo instances;
				spawnSoDRemnantManager();
				break;
			case 3, 4, 5:
				// Spawn Edric(Remnant Manager) else is handled by datapack
				spawnSoDRemnantManager();
				break;
			default:
				_log.warning(getClass().getSimpleName() + ": Unknown Seed of Destruction state(" + _SoDState + ")! ");
		}
	}
	
	public void updateSoDDefence(int state) {
		if (state >= 3 && state <= 5) {
			final Quest quest = QuestManager.getInstance().getQuest(SOD_DEFENCE);
			if (quest == null) {
				_log.warning(getClass().getSimpleName() + ": missing Defence Quest!");
			} else {
				quest.notifyEvent("start", null, null);
			}
		} else {
			_log.warning("Invalid Seed of Destruction defence state(" + state + "), should be 3, 4 or 5");
		}
	}
	
	public void stopSoDInvasion() {
		final Quest defQuest = QuestManager.getInstance().getQuest(SOD_DEFENCE);
		if (defQuest == null) {
			_log.warning(getClass().getSimpleName() + ": missing Defence Quest!");
		} else {
			defQuest.notifyEvent("stop", null, null);
		}
	}
	
	public void updateSodState() {
		final Quest esQuest = QuestManager.getInstance().getQuest(ENERGY_SEEDS);
		if (esQuest == null) {
			_log.warning(getClass().getSimpleName() + ": missing EnergySeeds Quest!");
		} else {
			esQuest.notifyEvent("StopSoDAi", null, null);
			stopSoDInvasion();
		}
	}
	
	public void increaseSoDTiatKilled() {
		if (_SoDState == 1) {
			_SoDTiatKilled++;
			if (_SoDTiatKilled >= graciaSeeds().getTiatKillCountForNextState()) {
				setSoDState(2, false, true);
			}
			saveData(SODTYPE);
		}
	}
	
	public void setSoDOpenState() {
		Quest esQuest = QuestManager.getInstance().getQuest(ENERGY_SEEDS);
		if (esQuest == null) {
			_log.warning(getClass().getSimpleName() + ": missing EnergySeeds Quest!");
		} else {
			esQuest.notifyEvent("StartSoDAi", null, null);
			stopSoDInvasion();
		}
	}
	
	public void spawnSoDRemnantManager() {
		try {
			if (edricSpawn == null || edricSpawn.isDecayed()) {
				final L2Spawn spawn = new L2Spawn(EDRIC);
				spawn.setInstanceId(0);
				spawn.setLocation(EDRIC_SPAWN_LOCATION);
				spawn.stopRespawn();
				final L2Npc npc = spawn.spawnOne(false);
				edricSpawn = npc;
			}
		} catch (Exception e) {
			_log.warning("Could not spawn NPC Edric #" + EDRIC + "; error: " + e.getMessage());
		}
	}
	
	public void despawnSoDRemnantManager() {
		if (edricSpawn != null) {
			edricSpawn.deleteMe();
		}
	}
	
	public int getSoDTiatKilled() {
		return _SoDTiatKilled;
	}
	
	public void setSoDState(int value, boolean doSave, boolean updateDate) {
		_log.info(getClass().getSimpleName() + ": New Seed of Destruction state -> " + value + ".");
		if (updateDate) {
			_SoDLastStateChangeDate.setTimeInMillis(System.currentTimeMillis());
		}
		_SoDState = value;
		
		if (_SoDState == 1) {
			// reset number of Tiat kills
			_SoDTiatKilled = 0;
			updateSodState();
		} else if (_SoDState == 2) {
			setSoDOpenState();
		} else if (_SoDState > 2) {
			updateSoDDefence(_SoDState);
		}
		
		handleSodStages();
		
		if (doSave) {
			saveData(SODTYPE);
		}
	}
	
	public long getSoDTimeForNextStateChange() {
		// this should not happen!
		return switch (_SoDState) {
			case 2 -> ((_SoDLastStateChangeDate.getTimeInMillis() + graciaSeeds().getStage2Length()) - System.currentTimeMillis());
			default -> -1;
		};
	}
	
	public Calendar getSoDLastStateChangeDate() {
		return _SoDLastStateChangeDate;
	}
	
	public int getSoDState() {
		return _SoDState;
	}
	
	/**
	 * Gets the single instance of {@code GraciaSeedsManager}.
	 * @return single instance of {@code GraciaSeedsManager}
	 */
	public static GraciaSeedsManager getInstance() {
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder {
		protected static final GraciaSeedsManager _instance = new GraciaSeedsManager();
	}
}