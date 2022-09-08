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

import static com.l2jserver.gameserver.config.Configuration.hunting;

import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.AbnormalVisualEffect;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExNevitAdventEffect;
import com.l2jserver.gameserver.network.serverpackets.ExNevitAdventPointInfoPacket;
import com.l2jserver.gameserver.network.serverpackets.ExNevitAdventTimeChange;

/**
 * @author Maneco2
 * @since 2.6.3.0
 */
public class HuntingSystem {
	
	private static final int ADDITIONAL_NEVIT_POINTS = 2;
	private static final int HUNTING_BONUS_REFRESH_RATE = 1;
	
	private final L2PcInstance _activeChar;
	private ScheduledFuture<?> _huntingBonusTask;
	private ScheduledFuture<?> _nevitBlessingTimeTask;
	private boolean _message25;
	private boolean _message50;
	private boolean _message75;
	
	public HuntingSystem(L2PcInstance player) {
		Objects.requireNonNull(player);
		_activeChar = player;
	}
	
	public void onPlayerLogin() {
		final Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 6);
		cal.set(Calendar.MINUTE, 30);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		// Reset Hunting System
		if ((getActiveChar().getLastAccess() < (cal.getTimeInMillis() / 1000L)) && (System.currentTimeMillis() > cal.getTimeInMillis())) {
			setHuntingBonusTime(0);
		}
		
		// Send Hunting Bonus UI Packets
		getActiveChar().sendPacket(new ExNevitAdventPointInfoPacket(getNevitBlessingPoints()));
		getActiveChar().sendPacket(new ExNevitAdventTimeChange(getHuntingBonusTime(), true));
		
		checkNevitBlessingEffect(getNevitBlessingTime());
		
		checkSystemMessageSend();
	}
	
	public void onPlayerLogout() {
		stopNevitBlessingEffectTask(true);
		stopHuntingBonusTask(false);
	}
	
	public void addPoints(int val) {
		setNevitBlessingPoints(getNevitBlessingPoints() + val);
		
		if (getNevitBlessingPoints() > hunting().getNevitBlessingMaxPoints()) {
			setNevitBlessingPoints(0);
			checkNevitBlessingEffect(hunting().getNevitBlessingEffetcTime());
		}
		
		checkSystemMessageSend();
	}
	
	public void startHuntingSystemTask() {
		if ((_huntingBonusTask == null) && ((getHuntingBonusTime() < hunting().getHuntingBonusMaxTime() || !hunting().getHuntingBonusLimit()))) {
			_huntingBonusTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new HuntingBonusTask(), 1000, 1000);
			if (hunting().getHuntingBonusLimit()) {
				getActiveChar().sendPacket(new ExNevitAdventTimeChange(getHuntingBonusTime(), false));
			}
		}
	}
	
	public class HuntingBonusTask implements Runnable {
		@Override
		public void run() {
			setHuntingBonusTime(getHuntingBonusTime() + HUNTING_BONUS_REFRESH_RATE);
			if (getHuntingBonusTime() >= hunting().getHuntingBonusMaxTime() && hunting().getHuntingBonusLimit()) {
				setHuntingBonusTime(hunting().getHuntingBonusMaxTime());
				stopHuntingBonusTask(true);
				return;
			}
			
			if (hunting().getHuntingBonusLimit()) {
				getActiveChar().sendPacket(new ExNevitAdventTimeChange(getHuntingBonusTime(), false));
			}
			
			addPoints(ADDITIONAL_NEVIT_POINTS);
			if (getNevitBlessingTime() > 0) {
				addPoints(hunting().getNevitRegularPoints());
			} else {
				addPoints(hunting().getNevitRegularPoints2());
			}
		}
	}
	
	public class NevitEffectEnd implements Runnable {
		@Override
		public void run() {
			setNevitBlessingTime(0);
			getActiveChar().sendPacket(new ExNevitAdventEffect(0));
			getActiveChar().sendPacket(new ExNevitAdventPointInfoPacket(getNevitBlessingPoints()));
			getActiveChar().sendPacket(SystemMessageId.NEVITS_ADVENT_BLESSING_HAS_ENDED);
			getActiveChar().stopAbnormalVisualEffect(true, AbnormalVisualEffect.NEVIT_ADVENT);
			stopNevitBlessingEffectTask(false);
		}
	}
	
	public void checkNevitBlessingEffect(int value) {
		if (getNevitBlessingTime() > 0) {
			stopNevitBlessingEffectTask(false);
			value = getNevitBlessingTime();
		}
		
		if (value > 0) {
			final int percent = calcPercent(getNevitBlessingPoints());
			if (percent < 25) {
				_message25 = false;
				_message50 = false;
				_message75 = false;
			}
			setNevitBlessingTime(value);
			getActiveChar().sendPacket(new ExNevitAdventEffect(value));
			getActiveChar().sendPacket(SystemMessageId.THE_ANGEL_NEVIT_HAS_BLESSED_YOU_FROM_ABOVE);
			getActiveChar().startAbnormalVisualEffect(true, AbnormalVisualEffect.NEVIT_ADVENT);
			_nevitBlessingTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new NevitEffectEnd(), value * 1000L);
		}
	}
	
	public void stopHuntingBonusTask(boolean sendPacket) {
		if (_huntingBonusTask != null) {
			_huntingBonusTask.cancel(true);
			_huntingBonusTask = null;
		}
		
		if (sendPacket) {
			getActiveChar().sendPacket(new ExNevitAdventTimeChange(getHuntingBonusTime(), true));
		}
	}
	
	public void stopNevitBlessingEffectTask(boolean value) {
		if (_nevitBlessingTimeTask != null) {
			if (value) {
				int time = (int) _nevitBlessingTimeTask.getDelay(TimeUnit.SECONDS);
				if (time > 0) {
					setNevitBlessingTime(time);
				} else {
					setNevitBlessingTime(0);
				}
			}
			_nevitBlessingTimeTask.cancel(true);
			_nevitBlessingTimeTask = null;
		}
	}
	
	public void checkSystemMessageSend() {
		final int percent = calcPercent(getNevitBlessingPoints());
		if (percent >= 75) {
			if (!_message75) {
				_message75 = true;
				getActiveChar().sendPacket(SystemMessageId.NEVITS_ADVENT_BLESSING_SHINES_STRONGLY_FROM_ABOVE);
			}
		} else if (percent >= 50) {
			if (!_message50) {
				_message50 = true;
				getActiveChar().sendPacket(SystemMessageId.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT);
			}
		} else if (percent >= 25) {
			if (!_message25) {
				_message25 = true;
				getActiveChar().sendPacket(SystemMessageId.YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_ADVENT_BLESSING);
			}
		}
	}
	
	public boolean isNevitBlessingActive() {
		return ((_nevitBlessingTimeTask != null) && (_nevitBlessingTimeTask.getDelay(TimeUnit.SECONDS) > 0));
	}
	
	public boolean isHuntingBonusTaskActive() {
		return (_nevitBlessingTimeTask != null);
	}
	
	public static int calcPercent(int points) {
		return (int) ((100.0 / hunting().getNevitBlessingMaxPoints()) * points);
	}
	
	public L2PcInstance getActiveChar() {
		return _activeChar;
	}
	
	public int getNevitBlessingPoints() {
		return getActiveChar().getStat().getNevitBlessingPoints();
	}
	
	public void setNevitBlessingPoints(int points) {
		getActiveChar().getStat().setNevitBlessingPoints(points);
	}
	
	public int getHuntingBonusTime() {
		return getActiveChar().getStat().getHuntingBonusTime();
	}
	
	public void setHuntingBonusTime(int time) {
		getActiveChar().getStat().setHuntingBonusTime(time);
	}
	
	public int getNevitBlessingTime() {
		return getActiveChar().getStat().getNevitBlessingTime();
	}
	
	public void setNevitBlessingTime(int time) {
		getActiveChar().getStat().setNevitBlessingTime(time);
	}
	
	public double getNevitHourglassMultiplier() {
		return (getActiveChar().getRecSystem().getBonusTime() > 0) || getActiveChar().hasAbnormalTypeVote() ? RecoBonus.getRecoMultiplier(getActiveChar()) : 0;
	}
}