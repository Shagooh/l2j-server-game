/*
 * Copyright © 2004-2022 L2J Server
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
package com.l2jserver.gameserver.model.entity;

import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.dao.factory.impl.DAOFactory;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.tasks.player.RecoBonusTask;
import com.l2jserver.gameserver.model.actor.tasks.player.RecoGiveTask;
import com.l2jserver.gameserver.network.serverpackets.ExVoteSystemInfo;

/**
 * @author HorridoJoho
 * @since 2.6.3.0
 */
public class RecommendationSystem {
	private final L2PcInstance _player;
	
	/** Recommendation task time */
	private int _recoBonusTime;
	/** number of recommendation obtained by other players */
	private int _recomHave;
	/** number of recommendations the player can give to other players. */
	private int _recomLeft;
	/** Recommendation task */
	private ScheduledFuture<?> _recoGiveTask;
	/** Recommendation Two Hours bonus **/
	private boolean _recoTwoHoursGiven = false;
	/** recommendation bonus time end task */
	private ScheduledFuture<?> _recoBonusTask;
	/** recommendation bonus time paused by peace zone entrance */
	private boolean _recoBonusPeacePause = true;
	/** count of recommendation bonus time pauses by other mechanisms than peace zone entrance */
	private AtomicInteger _recoBonusOtherPause = new AtomicInteger(0);
	
	public RecommendationSystem(L2PcInstance player) {
		Objects.requireNonNull(player);
		_player = player;
	}
	
	/** Update L2PcInstance Recommendations data. */
	public void store() {
		DAOFactory.getInstance().getRecommendationBonusDAO().insert(_player, getBonusTime());
	}
	
	/** Start timer to give recommendations left */
	public void startGiveTask() {
		if (_recoGiveTask == null) {
			_recoGiveTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RecoGiveTask(_player), 7200000, 3600000);
		}
	}
	
	/** Stop timer to give recommendations left */
	public void stopGiveTask() {
		if (_recoGiveTask != null) {
			_recoGiveTask.cancel(false);
			_recoGiveTask = null;
		}
	}
	
	/**
	 * Method to start the recommentation bonus task. Actions which usually resume the recommendation bonus time just have to call this method, specifying if it is from xpSpGain or other mechanisms.<br>
	 * <br>
	 * <b>All checks for the recommendation bonus timer are inside this method, no outside checks allowed!</b>
	 * @param xpSpGain Whether the start is coming from xpSpGain or other mechanisms.
	 */
	public void startBonusTask(boolean xpSpGain) {
		if (xpSpGain) {
			_player.debugFeature("RecBonus", "Set peace pause flag to false.");
			_recoBonusPeacePause = false;
		} else {
			int newCount = _recoBonusOtherPause.updateAndGet(i -> i > 0 ? i - 1 : 0);
			_player.debugFeature("RecBonus", "Decrement count of other pauses to {}", newCount);
		}
		
		if (getBonusTime() <= 0) {
			_player.debugFeature("RecBonus", "Not scheduling task because bonus time is {}.", getBonusTime());
			return;
		}
		
		if (_recoBonusPeacePause) {
			_player.debugFeature("RecBonus", "Not scheduling task because it was paused by peace.");
			return;
		}
		
		if (_recoBonusOtherPause.get() > 0) {
			_player.debugFeature("RecBonus", "Not scheduling task because it was paused by other mechanisms than peace.");
			return;
		}
		
		if (isBonusTaskActive()) {
			_player.debugFeature("RecBonus", "Not scheduling task because it was already scheduled.");
			return;
		}
		
		scheduleBonusTask(getBonusTime());
		
		_player.debugFeature("RecBonus", "Starting task.");
		_player.sendPacket(new ExVoteSystemInfo(_player));
	}
	
	/**
	 * Method to stop the recommentation bonus task. Actions which usually suspenmd the recommendation bonus time just have to call this method, specifying if it is from entering a pace zone or other mechanisms.<br>
	 * <br>
	 * <b>All checks for the recommendation bonus timer are inside this method, no outside checks allowed!</b>
	 * @param peaceZone Whether the stop is coming from entering a peace zone or other mechanisms.
	 */
	public void stopBonusTask(boolean peaceZone) {
		if (peaceZone) {
			_player.debugFeature("RecBonus", "Set peace pause flag to true.");
			_recoBonusPeacePause = true;
		} else {
			int newCount = _recoBonusOtherPause.incrementAndGet();
			_player.debugFeature("RecBonus", "Increment count of other pauses to {}", newCount);
		}
		
		if (!isBonusTaskActive()) {
			_player.debugFeature("RecBonus", "Not stopping task because it is not started.");
			return;
		}

		int remainingTime = getBonusTime();
		cancelBonusTask();
		setBonusTime(remainingTime);

		_player.debugFeature("RecBonus", "Stopping task.");
		_player.sendPacket(new ExVoteSystemInfo(_player));
	}
	
	/**
	 * Method to be called by task which gets fired when the recommendation bonus time is up.
	 */
	public void finishBonusTask()
	{
		cancelBonusTask();
		setBonusTime(0);
		
		_player.debugFeature("RecBonus", "Finishing task.");
		_player.sendPacket(new ExVoteSystemInfo(_player));
	}
	
	/**
	 * Give a recommendation to another player.
	 * @param target the target to recommend
	 */
	public void give(L2PcInstance target) {
		target.getRecSystem().incHave();
		decLeft();
	}
	
	/** Increment the number of recommendation received by other player */
	protected void incHave() {
		if (_recomHave < 255) {
			_recomHave++;
		}
	}
	
	/** Decrement the number of recommendation that the player can give. */
	protected void decLeft() {
		if (_recomLeft > 0) {
			_recomLeft--;
		}
	}
	
	/**
	 * Set the number of recommendations received by other players
	 * @param value recommendations received by other players
	 */
	public void setHave(int value) {
		_recomHave = Math.min(Math.max(value, 0), 255);
	}
	
	/**
	 * Set the number of recommendations the player can give to other players
	 * @param value recommendations the player can give to other players
	 */
	public void setLeft(int value) {
		_recomLeft = Math.min(Math.max(value, 0), 255);
	}
	
	public void setTwoHoursGiven(boolean val) {
		_recoTwoHoursGiven = val;
	}
	
	public void setBonusTime(int time) {
		if (isBonusTaskActive()) {
			cancelBonusTask();
			scheduleBonusTask(time);
		}

		_recoBonusTime = time;
	}
	
	/** @return remaining recommendation bonus time */
	public int getBonusTime() {
		if (isBonusTaskActive()) {
			return (int) Math.max(0, _recoBonusTask.getDelay(TimeUnit.SECONDS));
		}

		return _recoBonusTime;
	}
	
	/** @return recommendation bonus percentage */
	public int getBonus() {
		return (getBonusTime() > 0) || _player.hasAbnormalTypeVote() ? RecoBonus.getRecoBonus(_player) : 0;
	}
	
	/** @return recommendations received by other players */
	public int getHave() {
		return _recomHave;
	}
	
	/** @return recommendations the player can give to other players */
	public int getLeft() {
		return _recomLeft;
	}
	
	/** @return Whether the recommendation bonus end timer was scheduled */
	public boolean isBonusTaskActive() {
		return _recoBonusTask != null && !_recoBonusTask.isCancelled() && !_recoBonusTask.isDone();
	}
	
	public boolean isTwoHoursGiven() {
		return _recoTwoHoursGiven;
	}
	
	private void scheduleBonusTask(int delay) {
		_recoBonusTask = ThreadPoolManager.getInstance().scheduleGeneral(new RecoBonusTask(_player), delay * 1000);
	}
	
	private void cancelBonusTask() {
		_recoBonusTask.cancel(true);
		_recoBonusTask = null;
	}
}
