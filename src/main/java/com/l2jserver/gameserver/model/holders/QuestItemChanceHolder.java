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
package com.l2jserver.gameserver.model.holders;

/**
 * A DTO for quest items; contains limit info.<br>
 * @author xban1x
 * @author Noé Caratini aka Kita
 */
public class QuestItemChanceHolder extends ItemChanceHolder {
	private final long limit;

	public QuestItemChanceHolder(int id) {
		this(id, 100, 1, 0);
	}

	public QuestItemChanceHolder(int id, long limit) {
		this(id, 100, 1, limit);
	}

	public QuestItemChanceHolder(int id, long count, long limit) {
		this(id, 100, count, limit);
	}
	
	public QuestItemChanceHolder(int id, double chance) {
		this(id, chance, 1, 0);
	}

	public QuestItemChanceHolder(int id, double chance, long limit) {
		this(id, chance, 1, limit);
	}
	
	public QuestItemChanceHolder(int id, double chance, long count, long limit) {
		super(id, chance, count);
		this.limit = limit;
	}

	public long getLimit() {
		return limit;
	}

	@Override
	public String toString() {
		return "[" + getClass().getSimpleName() + "] ID: " + getId() + ", count: " + getCount() + ", chance: " + getChance() + ", limit: " + getLimit();
	}
}
