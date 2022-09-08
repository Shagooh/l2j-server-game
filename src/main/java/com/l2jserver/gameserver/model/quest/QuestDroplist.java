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
package com.l2jserver.gameserver.model.quest;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.drops.DropListScope;
import com.l2jserver.gameserver.model.drops.GeneralDropItem;
import com.l2jserver.gameserver.model.drops.GroupedGeneralDropItem;
import com.l2jserver.gameserver.model.drops.IDropItem;
import com.l2jserver.gameserver.model.holders.ItemChanceHolder;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.holders.QuestItemChanceHolder;
import com.l2jserver.gameserver.model.quest.QuestDroplist.QuestDropListBuilder.GroupedDropBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Noé Caratini aka Kita
 */
public class QuestDroplist {
    private final Map<Integer, List<QuestDropInfo>> dropsByNpcId = new HashMap<>();

    private QuestDroplist(QuestDropListBuilder builder) {
        dropsByNpcId.putAll(builder.dropList);
    }

    public QuestDropInfo get(int npcId) {
        if (!dropsByNpcId.containsKey(npcId)) {
            return null;
        }

        return dropsByNpcId.get(npcId).get(0);
    }

    public QuestDropInfo get(L2Npc npc) {
        return get(npc.getId());
    }

    public QuestDropInfo get(int npcId, int itemId) {
        if (!dropsByNpcId.containsKey(npcId)) {
            return null;
        }

        for (QuestDropInfo dropInfo : dropsByNpcId.get(npcId)) {
            if (dropInfo.item().getId() == itemId) {
                return dropInfo;
            }
        }

        return null;
    }

    public QuestDropInfo get(int npcId, ItemHolder item) {
        return get(npcId, item.getId());
    }

    public QuestDropInfo get(L2Npc npc, ItemHolder item) {
        return get(npc.getId(), item.getId());
    }

    public Set<Integer> getNpcIds() {
        return dropsByNpcId.keySet();
    }

    public Set<Integer> getItemIds() {
        return dropsByNpcId.values().stream().flatMap(List::stream)
                .map(QuestDropInfo::drop)
                .flatMap(dropItem -> {
                    if (dropItem instanceof GeneralDropItem gen) {
                        return Stream.of(gen.getItemId());
                    } else if (dropItem instanceof  GroupedGeneralDropItem grp) {
                        return grp.getItems().stream().map(GeneralDropItem::getItemId);
                    } else {
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toSet());
    }

    public static IDropItem singleDropItem(ItemChanceHolder itemHolder) {
        return singleDropItem(itemHolder.getId(), itemHolder.getCount(), itemHolder.getCount(), itemHolder.getChance());
    }

    public static IDropItem singleDropItem(ItemChanceHolder itemHolder, double chance) {
        return singleDropItem(itemHolder.getId(), itemHolder.getCount(), itemHolder.getCount(), chance);
    }

    public static IDropItem singleDropItem(ItemChanceHolder itemHolder, long amount) {
        return singleDropItem(itemHolder.getId(), amount, amount, itemHolder.getChance());
    }

    public static IDropItem singleDropItem(int itemId, long amount) {
        return singleDropItem(itemId, amount, amount, 100.0);
    }

    public static IDropItem singleDropItem(int itemId, double chance) {
        return singleDropItem(itemId, 1, 1, chance);
    }

    public static IDropItem singleDropItem(int itemId, long minAmount, long maxAmount, double chance) {
        return DropListScope.QUEST.newDropItem(itemId, minAmount, maxAmount, chance);
    }

    public static IDropItem groupedDropItem(double chance, ItemChanceHolder... itemHolders) {
        return groupedDropItem(chance, List.of(itemHolders));
    }

    private static IDropItem groupedDropItem(double chance, List<? extends ItemChanceHolder> itemHolders) {
        GroupedGeneralDropItem group = DropListScope.QUEST.newGroupedDropItem(chance);
        List<GeneralDropItem> dropItems = itemHolders.stream()
                .map(QuestDroplist::singleDropItem)
                .filter(GeneralDropItem.class::isInstance)
                .map(GeneralDropItem.class::cast)
                .toList();
        group.setItems(dropItems);
        return group;
    }

    public static QuestDropListBuilder builder() {
        return new QuestDropListBuilder();
    }

    public static class QuestDropListBuilder {
        private final Map<Integer, List<QuestDropInfo>> dropList = new HashMap<>();
        private Map.Entry<Integer, QuestDropInfo> lastAdded = null;

        public QuestDropListBuilder addSingleDrop(int npcId, QuestItemChanceHolder questItem, long minAmount, long maxAmount, double chance, int[] requiredItemIds) {
            List<QuestDropInfo> dropsForMob = dropList.computeIfAbsent(npcId, ArrayList::new);
            QuestDropInfo dropInfo = new QuestDropInfo(questItem, singleDropItem(questItem.getId(), minAmount, maxAmount, chance), requiredItemIds);
            dropsForMob.add(dropInfo);
            updateLastAdded(npcId, dropInfo);
            return this;
        }

        public QuestDropListBuilder addSingleDrop(int npcId, QuestItemChanceHolder questItem, long minAmount, long maxAmount, double chance) {
            return addSingleDrop(npcId, questItem, minAmount, maxAmount, chance, null);
        }

        public QuestDropListBuilder addSingleDrop(int npcId, QuestItemChanceHolder questItem) {
            return addSingleDrop(npcId, questItem, questItem.getCount(), questItem.getCount(), questItem.getChance());
        }

        public QuestDropListBuilder addSingleDrop(int npcId, QuestItemChanceHolder questItem, long amount, double chance) {
            return addSingleDrop(npcId,
                    new QuestItemChanceHolder(questItem.getId(), chance, amount, questItem.getLimit()));
        }

        public QuestDropListBuilder addSingleDrop(int npcId, QuestItemChanceHolder questItem, long amount) {
            return addSingleDrop(npcId,
                    new QuestItemChanceHolder(questItem.getId(), questItem.getChance(), amount, questItem.getLimit()));
        }

        public QuestDropListBuilder addSingleDrop(int npcId, QuestItemChanceHolder questItem, double chance) {
            return addSingleDrop(npcId,
                    new QuestItemChanceHolder(questItem.getId(), chance, questItem.getCount(), questItem.getLimit()));
        }

        public QuestDropListBuilder addSingleDrop(int npcId, int itemId) {
            return addSingleDrop(npcId,
                    new QuestItemChanceHolder(itemId, 100.0, 1, 0));
        }

        public QuestDropListBuilder addSingleDrop(int npcId, int itemId, long amount) {
            return addSingleDrop(npcId,
                    new QuestItemChanceHolder(itemId, 100.0, amount, 0));
        }

        public QuestDropListBuilder addSingleDrop(int npcId, int itemId, double chance) {
            return addSingleDrop(npcId,
                    new QuestItemChanceHolder(itemId, chance, 1, 0));
        }

        public QuestDropListBuilder addSingleDrop(int npcId, int itemId, long amount, double chance) {
            return addSingleDrop(npcId,
                    new QuestItemChanceHolder(itemId, chance, amount, 0));
        }

        public QuestDropListBuilder withRequiredItems(int... itemIds) {
            if (lastAdded == null) {
                throw new IllegalStateException("Cannot add required items without adding a drop first.");
            }

            int[] uniqueItemIds = Optional.ofNullable(itemIds)
                    .map(ids -> Arrays.stream(ids).distinct().toArray())
                    .orElse(null);

            QuestDropInfo oldDropInfo = lastAdded.getValue();
            QuestDropInfo newDropInfo = new QuestDropInfo(oldDropInfo.item(), oldDropInfo.drop(), uniqueItemIds);

            List<QuestDropInfo> dropsForNpc = dropList.get(lastAdded.getKey());
            dropsForNpc.remove(oldDropInfo);
            dropsForNpc.add(newDropInfo);
            updateLastAdded(lastAdded.getKey(), newDropInfo);
            return this;
        }

        public SingleDropBuilder bulkAddSingleDrop(QuestItemChanceHolder questItem) {
            return new SingleDropBuilder(this, questItem);
        }

        public SingleDropBuilder bulkAddSingleDrop(QuestItemChanceHolder questItem, double chance) {
            return bulkAddSingleDrop(new QuestItemChanceHolder(questItem.getId(), chance, questItem.getCount(), questItem.getLimit()));
        }

        public SingleDropBuilder bulkAddSingleDrop(int itemId, double chance) {
            return bulkAddSingleDrop(new QuestItemChanceHolder(itemId, chance, 1, 0));
        }

        private QuestDropListBuilder addGroupedDrop(int npcId, QuestDropInfo dropInfo) {
            List<QuestDropInfo> dropsForMob = dropList.computeIfAbsent(npcId, ArrayList::new);
            dropsForMob.add(dropInfo);
            updateLastAdded(npcId, dropInfo);
            return this;
        }

        public GroupedDropBuilder addGroupedDrop(int npcId, double chanceForGroup) {
            return new GroupedDropBuilder(this, npcId, chanceForGroup);
        }

        public GroupedDropForSingleItemBuilder addGroupedDropForSingleItem(int npcId, QuestItemChanceHolder questItem, double chanceForGroup) {
            return new GroupedDropForSingleItemBuilder(this, npcId, questItem, chanceForGroup);
        }

        private void updateLastAdded(int npcId, QuestDropInfo dropInfo) {
            lastAdded = Map.entry(npcId, dropInfo);
        }

        public QuestDroplist build() {
            return new QuestDroplist(this);
        }

        public static class SingleDropBuilder {
            private final QuestDropListBuilder parentBuilder;
            private final QuestItemChanceHolder item;

            private final Set<Integer> npcIds = new HashSet<>();
            private int[] requiredItems = null;

            public SingleDropBuilder(QuestDropListBuilder parentBuilder, QuestItemChanceHolder item) {
                this.parentBuilder = parentBuilder;
                this.item = item;
            }

            public SingleDropBuilder withNpcs(Set<Integer> npcIds) {
                this.npcIds.addAll(npcIds);
                return this;
            }

            public SingleDropBuilder withNpcs(int... npcIds) {
                return withNpcs(Arrays.stream(npcIds).boxed().collect(Collectors.toSet()));
            }

            public SingleDropBuilder withRequiredItems(int... itemIds) {
                requiredItems = itemIds;
                return this;
            }

            public QuestDropListBuilder build() {
                npcIds.forEach(npcId -> parentBuilder.addSingleDrop(npcId, item).withRequiredItems(requiredItems));
                return parentBuilder;
            }
        }

        public static class GroupedDropBuilder {
            private final QuestDropListBuilder parentBuilder;
            private final int npcId;

            private final double chance;
            protected final List<QuestItemChanceHolder> items = new ArrayList<>();

            private GroupedDropBuilder(QuestDropListBuilder parentBuilder, int npcId, double chanceForGroup) {
                this.parentBuilder = parentBuilder;
                this.npcId = npcId;
                this.chance = chanceForGroup;
            }

            public GroupedDropBuilder withDropItem(QuestItemChanceHolder questItem) {
                items.add(questItem);
                return this;
            }

            public GroupedDropBuilder withDropItem(QuestItemChanceHolder questItem, long amount) {
                return withDropItem(new QuestItemChanceHolder(questItem.getId(), questItem.getChance(), amount, questItem.getLimit()));
            }

            public GroupedDropBuilder withDropItem(QuestItemChanceHolder questItem, double chanceWithinGroup) {
                return withDropItem(new QuestItemChanceHolder(questItem.getId(), chanceWithinGroup, questItem.getCount(), questItem.getLimit()));
            }

            public GroupedDropBuilder withDropItem(QuestItemChanceHolder questItem, long amount, double chanceWithinGroup) {
                return withDropItem(new QuestItemChanceHolder(questItem.getId(), chanceWithinGroup, amount, questItem.getLimit()));
            }

            public GroupedDropBuilder withDropItem(int itemId, double chanceWithinGroup) {
                return withDropItem(new QuestItemChanceHolder(itemId, chanceWithinGroup, 1, 0));
            }

            public GroupedDropBuilder withDropItem(int itemId, long amount, double chanceWithinGroup) {
                return withDropItem(new QuestItemChanceHolder(itemId, chanceWithinGroup, amount, 0));
            }

            public QuestDropListBuilder build() {
                return parentBuilder.addGroupedDrop(npcId, new QuestDropInfo(items.get(0), groupedDropItem(chance, items)));
            }
        }
    }

    public static class GroupedDropForSingleItemBuilder extends GroupedDropBuilder {
        private final QuestItemChanceHolder questItem;

        private GroupedDropForSingleItemBuilder(QuestDropListBuilder parentBuilder, int npcId, QuestItemChanceHolder questItem, double chanceForGroup) {
            super(parentBuilder, npcId, chanceForGroup);
            this.questItem = questItem;
        }

        public GroupedDropForSingleItemBuilder withAmount(long amount, double chanceWithinGroup) {
            this.withDropItem(questItem, amount, chanceWithinGroup);
            return this;
        }

        public QuestDropListBuilder orElse(long amount) {
            double sumOfChances = items.stream()
                    .mapToDouble(ItemChanceHolder::getChance)
                    .sum();

            this.withDropItem(questItem, amount, 100.0 - sumOfChances);
            return super.build();
        }

        public QuestDropListBuilder build() {
            return super.build();
        }
    }

    public record QuestDropInfo(QuestItemChanceHolder item, IDropItem drop, int[] requiredItems) {

        public QuestDropInfo(QuestItemChanceHolder item, IDropItem drop) {
            this(item, drop, null);
        }

        public long getLimit() {
            return item.getLimit();
        }
    }
}
