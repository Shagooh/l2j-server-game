package com.l2jserver.gameserver.model.quest;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.drops.GeneralDropItem;
import com.l2jserver.gameserver.model.drops.GroupedGeneralDropItem;
import com.l2jserver.gameserver.model.drops.IDropItem;
import com.l2jserver.gameserver.model.holders.QuestItemChanceHolder;
import com.l2jserver.gameserver.model.quest.QuestDroplist.QuestDropInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class QuestDroplistTest {
    private static final QuestItemChanceHolder QUEST_ITEM_1 = new QuestItemChanceHolder(1, 25.0, 2L, 70L);
    private static final QuestItemChanceHolder QUEST_ITEM_2 = new QuestItemChanceHolder(2, 50.0);
    private static final QuestItemChanceHolder QUEST_ITEM_3 = new QuestItemChanceHolder(3, 50.0, 2L, 0L);

    @Mock
    private L2Npc npc;

    @Test
    public void shouldBuildDroplistAndRetrieveInfo() {
        when(npc.getId()).thenReturn(1);

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .build();

        QuestDropInfo dropInfo = dropList.get(1);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item()).isEqualTo(QUEST_ITEM_1);
        assertThat(dropInfo.getLimit()).isEqualTo(QUEST_ITEM_1.getLimit());

        QuestDropInfo dropInfo2 = dropList.get(npc);
        assertThat(dropInfo2).isNotNull();
        assertThat(dropInfo2.item()).isEqualTo(QUEST_ITEM_1);
        assertThat(dropInfo2.getLimit()).isEqualTo(QUEST_ITEM_1.getLimit());

        assertThat(dropList.getNpcIds()).containsExactly(1);
    }

    @Test
    public void shouldAddSingleDropWithAmount() {
        long amount = 5;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addSingleDrop(2, QUEST_ITEM_1, amount)
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item()).isNotEqualTo(QUEST_ITEM_1);
        assertThat(dropInfo.item().getId()).isEqualTo(QUEST_ITEM_1.getId());
        assertThat(dropInfo.item().getCount()).isEqualTo(amount);
        assertThat(dropInfo.item().getChance()).isEqualTo(QUEST_ITEM_1.getChance());
        assertThat(dropInfo.item().getLimit()).isEqualTo(QUEST_ITEM_1.getLimit());
    }

    @Test
    public void shouldAddSingleDropWithChance() {
        double chance = 75.0;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addSingleDrop(2, QUEST_ITEM_1, chance)
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item()).isNotEqualTo(QUEST_ITEM_1);
        assertThat(dropInfo.item().getId()).isEqualTo(QUEST_ITEM_1.getId());
        assertThat(dropInfo.item().getCount()).isEqualTo(QUEST_ITEM_1.getCount());
        assertThat(dropInfo.item().getChance()).isEqualTo(chance);
        assertThat(dropInfo.item().getLimit()).isEqualTo(QUEST_ITEM_1.getLimit());
    }

    @Test
    public void shouldAddSingleDropWithAmountAndChance() {
        long amount = 5;
        double chance = 75.0;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addSingleDrop(2, QUEST_ITEM_1, amount, chance)
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item()).isNotEqualTo(QUEST_ITEM_1);
        assertThat(dropInfo.item().getId()).isEqualTo(QUEST_ITEM_1.getId());
        assertThat(dropInfo.item().getCount()).isEqualTo(amount);
        assertThat(dropInfo.item().getChance()).isEqualTo(chance);
        assertThat(dropInfo.item().getLimit()).isEqualTo(QUEST_ITEM_1.getLimit());
    }

    @Test
    public void shouldAddSingleDropWithItemId() {
        int itemId = 2;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addSingleDrop(2, itemId)
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item().getId()).isEqualTo(itemId);
        assertThat(dropInfo.item().getCount()).isEqualTo(1);
        assertThat(dropInfo.item().getChance()).isEqualTo(100.0);
        assertThat(dropInfo.item().getLimit()).isEqualTo(0);
    }

    @Test
    public void shouldAddSingleDropWithItemIdAndAmount() {
        int itemId = 2;
        long amount = 5;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addSingleDrop(2, itemId, amount)
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item().getId()).isEqualTo(itemId);
        assertThat(dropInfo.item().getCount()).isEqualTo(amount);
        assertThat(dropInfo.item().getChance()).isEqualTo(100.0);
        assertThat(dropInfo.item().getLimit()).isEqualTo(0);
    }

    @Test
    public void shouldAddSingleDropWithItemIdAndChance() {
        int itemId = 2;
        double chance = 50.0;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addSingleDrop(2, itemId, chance)
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item().getId()).isEqualTo(itemId);
        assertThat(dropInfo.item().getCount()).isEqualTo(1);
        assertThat(dropInfo.item().getChance()).isEqualTo(chance);
        assertThat(dropInfo.item().getLimit()).isEqualTo(0);
    }

    @Test
    public void shouldAddSingleDropWithItemIdAndAmountAndChance() {
        int itemId = 2;
        long amount = 5;
        double chance = 50.0;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addSingleDrop(2, itemId, amount, chance)
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item().getId()).isEqualTo(itemId);
        assertThat(dropInfo.item().getCount()).isEqualTo(amount);
        assertThat(dropInfo.item().getChance()).isEqualTo(chance);
        assertThat(dropInfo.item().getLimit()).isEqualTo(0);
    }

    @Test
    public void shouldAddSingleDropWithRequiredItems() {
        int[] requiredItemIds = {10, 11};

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1).withRequiredItems(requiredItemIds)
                .build();

        QuestDropInfo dropInfo = dropList.get(1);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item()).isEqualTo(QUEST_ITEM_1);
        assertThat(dropInfo.getLimit()).isEqualTo(QUEST_ITEM_1.getLimit());
        assertThat(dropInfo.requiredItems()).isEqualTo(requiredItemIds);
    }

    @Test
    public void shouldAddRequiredItemsWithoutDuplicates() {
        int[] requiredItemIds = {10, 11};

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1).withRequiredItems(10, 11, 11)
                .build();

        QuestDropInfo dropInfo = dropList.get(1);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item()).isEqualTo(QUEST_ITEM_1);
        assertThat(dropInfo.getLimit()).isEqualTo(QUEST_ITEM_1.getLimit());
        assertThat(dropInfo.requiredItems()).isEqualTo(requiredItemIds);
    }

    @Test
    public void shouldThrowExceptionWhenAddingRequiredItemsWithoutDrop() {
        assertThatIllegalStateException().isThrownBy(() ->
                QuestDroplist.builder()
                        .withRequiredItems(1, 2)
                        .build());
    }

    @Test
    public void shouldOverwriteRequiredItemsIfCalledTwice() {
        int[] requiredItemIds = {2, 3};

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1).withRequiredItems(1, 2).withRequiredItems(requiredItemIds)
                .build();

        QuestDropInfo dropInfo = dropList.get(1);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item()).isEqualTo(QUEST_ITEM_1);
        assertThat(dropInfo.getLimit()).isEqualTo(QUEST_ITEM_1.getLimit());
        assertThat(dropInfo.requiredItems()).isEqualTo(requiredItemIds);
    }

    @Test
    public void shouldBulkAddSingleDrop() {
        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .bulkAddSingleDrop(QUEST_ITEM_1)
                    .withNpcs(Set.of(2, 3, 4))
                    .build()
                .bulkAddSingleDrop(QUEST_ITEM_1)
                    .withNpcs(5, 6, 7)
                    .build()
                .build();

        IntStream.range(2, 8).forEach(npcId -> {
            QuestDropInfo dropInfo = dropList.get(npcId);
            assertThat(dropInfo).isNotNull();
            assertThat(dropInfo.item()).isEqualTo(QUEST_ITEM_1);
        });
    }

    @Test
    public void shouldBulkAddSingleDropWithChance() {
        double chance = 10.0;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .bulkAddSingleDrop(QUEST_ITEM_1, chance)
                    .withNpcs(Set.of(2, 3, 4))
                    .build()
                .bulkAddSingleDrop(QUEST_ITEM_1, chance)
                    .withNpcs(5, 6, 7)
                    .build()
                .build();

        IntStream.range(2, 8).forEach(npcId -> {
            QuestDropInfo dropInfo = dropList.get(npcId);
            assertThat(dropInfo).isNotNull();
            assertThat(dropInfo.item()).isNotEqualTo(QUEST_ITEM_1);
            assertThat(dropInfo.item().getId()).isEqualTo(QUEST_ITEM_1.getId());
            assertThat(dropInfo.item().getCount()).isEqualTo(QUEST_ITEM_1.getCount());
            assertThat(dropInfo.item().getChance()).isEqualTo(chance);
            assertThat(dropInfo.item().getLimit()).isEqualTo(QUEST_ITEM_1.getLimit());
        });
    }

    @Test
    public void shouldBulkAddSingleDropWithItemIdAndChance() {
        double chance = 10.0;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .bulkAddSingleDrop(QUEST_ITEM_1.getId(), chance)
                    .withNpcs(Set.of(2, 3, 4))
                    .build()
                .bulkAddSingleDrop(QUEST_ITEM_1.getId(), chance)
                    .withNpcs(5, 6, 7)
                    .build()
                .build();

        IntStream.range(2, 8).forEach(npcId -> {
            QuestDropInfo dropInfo = dropList.get(npcId);
            assertThat(dropInfo).isNotNull();
            assertThat(dropInfo.item()).isNotEqualTo(QUEST_ITEM_1);
            assertThat(dropInfo.item().getId()).isEqualTo(QUEST_ITEM_1.getId());
            assertThat(dropInfo.item().getCount()).isEqualTo(1);
            assertThat(dropInfo.item().getChance()).isEqualTo(chance);
            assertThat(dropInfo.item().getLimit()).isEqualTo(0);
        });
    }

    @Test
    public void shouldBulkAddSingleDropWithRequiredItems() {
        int[] requiredItemIds = {10, 11, 12};

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .bulkAddSingleDrop(QUEST_ITEM_1).withNpcs(2, 3, 4).withRequiredItems(requiredItemIds).build()
                .build();

        IntStream.range(2, 5).forEach(npcId -> {
            QuestDropInfo dropInfo = dropList.get(npcId);
            assertThat(dropInfo).isNotNull();
            assertThat(dropInfo.item()).isEqualTo(QUEST_ITEM_1);
            assertThat(dropInfo.requiredItems()).isEqualTo(requiredItemIds);
        });
    }

    @Test
    public void shouldAddGroupedDrop() {
        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addGroupedDrop(2, 100.0)
                    .withDropItem(QUEST_ITEM_2)
                    .withDropItem(QUEST_ITEM_3)
                    .build()
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item()).isEqualTo(QUEST_ITEM_2);

        assertThat(dropInfo.drop()).isInstanceOf(GroupedGeneralDropItem.class);
        GroupedGeneralDropItem group = (GroupedGeneralDropItem) dropInfo.drop();
        assertThat(group.getItems()).hasSize(2);
        assertThat(group.getItems()).anySatisfy(dropItem -> {
            assertThat(dropItem.getItemId()).isEqualTo(QUEST_ITEM_2.getId());
            assertThat(dropItem.getChance()).isEqualTo(QUEST_ITEM_2.getChance());
            assertThat(dropItem.getMin()).isEqualTo(QUEST_ITEM_2.getCount());
            assertThat(dropItem.getMax()).isEqualTo(QUEST_ITEM_2.getCount());
        });
        assertThat(group.getItems()).anySatisfy(dropItem -> {
            assertThat(dropItem.getItemId()).isEqualTo(QUEST_ITEM_3.getId());
            assertThat(dropItem.getChance()).isEqualTo(QUEST_ITEM_3.getChance());
            assertThat(dropItem.getMin()).isEqualTo(QUEST_ITEM_3.getCount());
            assertThat(dropItem.getMax()).isEqualTo(QUEST_ITEM_3.getCount());
        });
    }

    @Test
    public void shouldAddGroupedDropWithAmount() {
        long amount = 5;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addGroupedDrop(2, 100.0)
                    .withDropItem(QUEST_ITEM_2, amount)
                    .build()
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.getLimit()).isEqualTo(QUEST_ITEM_2.getLimit());

        assertThat(dropInfo.drop()).isInstanceOf(GroupedGeneralDropItem.class);
        GroupedGeneralDropItem group = (GroupedGeneralDropItem) dropInfo.drop();
        assertThat(group.getItems()).hasSize(1);
        assertThat(group.getItems()).satisfiesExactly(dropItem -> {
            assertThat(dropItem.getItemId()).isEqualTo(QUEST_ITEM_2.getId());
            assertThat(dropItem.getChance()).isEqualTo(QUEST_ITEM_2.getChance());
            assertThat(dropItem.getMin()).isEqualTo(amount);
            assertThat(dropItem.getMax()).isEqualTo(amount);
        });
    }

    @Test
    public void shouldAddGroupedDropWithChance() {
        double chance = 80.0;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addGroupedDrop(2, 100.0)
                    .withDropItem(QUEST_ITEM_2, chance)
                    .build()
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.getLimit()).isEqualTo(QUEST_ITEM_2.getLimit());

        assertThat(dropInfo.drop()).isInstanceOf(GroupedGeneralDropItem.class);
        GroupedGeneralDropItem group = (GroupedGeneralDropItem) dropInfo.drop();
        assertThat(group.getItems()).hasSize(1);
        assertThat(group.getItems()).satisfiesExactly(dropItem -> {
            assertThat(dropItem.getItemId()).isEqualTo(QUEST_ITEM_2.getId());
            assertThat(dropItem.getChance()).isEqualTo(chance);
            assertThat(dropItem.getMin()).isEqualTo(QUEST_ITEM_2.getCount());
            assertThat(dropItem.getMax()).isEqualTo(QUEST_ITEM_2.getCount());
        });
    }

    @Test
    public void shouldAddGroupedDropWithItemIdAndChance() {
        double chance = 80.0;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addGroupedDrop(2, 100.0)
                    .withDropItem(QUEST_ITEM_2.getId(), chance)
                    .build()
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.getLimit()).isEqualTo(0);

        assertThat(dropInfo.drop()).isInstanceOf(GroupedGeneralDropItem.class);
        GroupedGeneralDropItem group = (GroupedGeneralDropItem) dropInfo.drop();
        assertThat(group.getItems()).hasSize(1);
        assertThat(group.getItems()).satisfiesExactly(dropItem -> {
            assertThat(dropItem.getItemId()).isEqualTo(QUEST_ITEM_2.getId());
            assertThat(dropItem.getChance()).isEqualTo(chance);
            assertThat(dropItem.getMin()).isEqualTo(1);
            assertThat(dropItem.getMax()).isEqualTo(1);
        });
    }

    @Test
    public void shouldAddGroupedDropWithItemIdAmountAndChance() {
        long amount = 5;
        double chance = 80.0;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addGroupedDrop(2, 100.0)
                    .withDropItem(QUEST_ITEM_2.getId(), amount, chance)
                    .build()
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.getLimit()).isEqualTo(0);

        assertThat(dropInfo.drop()).isInstanceOf(GroupedGeneralDropItem.class);
        GroupedGeneralDropItem group = (GroupedGeneralDropItem) dropInfo.drop();
        assertThat(group.getItems()).hasSize(1);
        assertThat(group.getItems()).satisfiesExactly(dropItem -> {
            assertThat(dropItem.getItemId()).isEqualTo(QUEST_ITEM_2.getId());
            assertThat(dropItem.getChance()).isEqualTo(chance);
            assertThat(dropItem.getMin()).isEqualTo(amount);
            assertThat(dropItem.getMax()).isEqualTo(amount);
        });
    }

    @Test
    public void shouldAddGroupedDropWithAmountAndChance() {
        long amount = 5;
        double chance = 80.0;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addGroupedDrop(2, 100.0)
                    .withDropItem(QUEST_ITEM_2, amount, chance)
                    .build()
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item().getId()).isEqualTo(QUEST_ITEM_2.getId());

        assertThat(dropInfo.drop()).isInstanceOf(GroupedGeneralDropItem.class);
        GroupedGeneralDropItem group = (GroupedGeneralDropItem) dropInfo.drop();
        assertThat(group.getItems()).hasSize(1);
        assertThat(group.getItems()).satisfiesExactly(dropItem -> {
            assertThat(dropItem.getItemId()).isEqualTo(QUEST_ITEM_2.getId());
            assertThat(dropItem.getChance()).isEqualTo(chance);
            assertThat(dropItem.getMin()).isEqualTo(amount);
            assertThat(dropItem.getMax()).isEqualTo(amount);
        });
    }

    @Test
    public void shouldAddGroupedDropWithRequiredItems() {
        int[] requiredItemIds = {10, 11, 12};

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addGroupedDrop(2, 100.0).withDropItem(QUEST_ITEM_2).withDropItem(QUEST_ITEM_3)
                .build().withRequiredItems(requiredItemIds)
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.requiredItems()).isEqualTo(requiredItemIds);
    }

    @Test
    public void shouldAddGroupedDropForSingleItem() {
        long amount1 = 3;
        long amount2 = 5;
        double chance1 = 20.0;
        double chance2 = 80.0;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addGroupedDropForSingleItem(2, QUEST_ITEM_2, 100.0)
                    .withAmount(amount1, chance1)
                    .withAmount(amount2, chance2).build()
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.drop()).isInstanceOf(GroupedGeneralDropItem.class);

        GroupedGeneralDropItem group = (GroupedGeneralDropItem) dropInfo.drop();
        assertThat(group.getItems()).hasSize(2);
        assertThat(group.getItems()).anySatisfy(dropItem -> {
            assertThat(dropItem.getItemId()).isEqualTo(QUEST_ITEM_2.getId());
            assertThat(dropItem.getChance()).isEqualTo(chance1);
            assertThat(dropItem.getMin()).isEqualTo(amount1);
            assertThat(dropItem.getMax()).isEqualTo(amount1);
        });
        assertThat(group.getItems()).anySatisfy(dropItem -> {
            assertThat(dropItem.getItemId()).isEqualTo(QUEST_ITEM_2.getId());
            assertThat(dropItem.getChance()).isEqualTo(chance2);
            assertThat(dropItem.getMin()).isEqualTo(amount2);
            assertThat(dropItem.getMax()).isEqualTo(amount2);
        });
    }

    @Test
    public void shouldAddGroupedDropForSingleItemUsingOrElse() {
        long amount1 = 3;
        long amount2 = 5;
        double chance1 = 20.0;
        double chance2 = 80.0;

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addGroupedDropForSingleItem(2, QUEST_ITEM_2, 100.0)
                    .withAmount(amount1, chance1)
                    .orElse(amount2)
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.drop()).isInstanceOf(GroupedGeneralDropItem.class);

        GroupedGeneralDropItem group = (GroupedGeneralDropItem) dropInfo.drop();
        assertThat(group.getItems()).hasSize(2);
        assertThat(group.getItems()).anySatisfy(dropItem -> {
            assertThat(dropItem.getItemId()).isEqualTo(QUEST_ITEM_2.getId());
            assertThat(dropItem.getChance()).isEqualTo(chance1);
            assertThat(dropItem.getMin()).isEqualTo(amount1);
            assertThat(dropItem.getMax()).isEqualTo(amount1);
        });
        assertThat(group.getItems()).anySatisfy(dropItem -> {
            assertThat(dropItem.getItemId()).isEqualTo(QUEST_ITEM_2.getId());
            assertThat(dropItem.getChance()).isEqualTo(chance2);
            assertThat(dropItem.getMin()).isEqualTo(amount2);
            assertThat(dropItem.getMax()).isEqualTo(amount2);
        });
    }

    @Test
    public void shouldGenerateSingleDropItem() {
        IDropItem dropItem = QuestDroplist.singleDropItem(QUEST_ITEM_1);

        assertThat(dropItem).isInstanceOf(GeneralDropItem.class);

        GeneralDropItem generalDropItem = (GeneralDropItem) dropItem;
        assertThat(generalDropItem.getItemId()).isEqualTo(QUEST_ITEM_1.getId());
        assertThat(generalDropItem.getChance()).isEqualTo(QUEST_ITEM_1.getChance());
        assertThat(generalDropItem.getMin()).isEqualTo(QUEST_ITEM_1.getCount());
        assertThat(generalDropItem.getMax()).isEqualTo(QUEST_ITEM_1.getCount());
    }

    @Test
    public void shouldGenerateSingleDropItemWithChance() {
        double chance = 75.0;

        IDropItem dropItem = QuestDroplist.singleDropItem(QUEST_ITEM_1, chance);

        assertThat(dropItem).isInstanceOf(GeneralDropItem.class);

        GeneralDropItem generalDropItem = (GeneralDropItem) dropItem;
        assertThat(generalDropItem.getItemId()).isEqualTo(QUEST_ITEM_1.getId());
        assertThat(generalDropItem.getChance()).isEqualTo(chance);
        assertThat(generalDropItem.getMin()).isEqualTo(QUEST_ITEM_1.getCount());
        assertThat(generalDropItem.getMax()).isEqualTo(QUEST_ITEM_1.getCount());
    }

    @Test
    public void shouldGenerateSingleDropItemWithAmount() {
        long amount = 10;

        IDropItem dropItem = QuestDroplist.singleDropItem(QUEST_ITEM_1, amount);

        assertThat(dropItem).isInstanceOf(GeneralDropItem.class);

        GeneralDropItem generalDropItem = (GeneralDropItem) dropItem;
        assertThat(generalDropItem.getItemId()).isEqualTo(QUEST_ITEM_1.getId());
        assertThat(generalDropItem.getChance()).isEqualTo(QUEST_ITEM_1.getChance());
        assertThat(generalDropItem.getMin()).isEqualTo(amount);
        assertThat(generalDropItem.getMax()).isEqualTo(amount);
    }

    @Test
    public void shouldGenerateSingleDropItemWithItemIdAndAmount() {
        long amount = 10;

        IDropItem dropItem = QuestDroplist.singleDropItem(QUEST_ITEM_1.getId(), amount);

        assertThat(dropItem).isInstanceOf(GeneralDropItem.class);

        GeneralDropItem generalDropItem = (GeneralDropItem) dropItem;
        assertThat(generalDropItem.getItemId()).isEqualTo(QUEST_ITEM_1.getId());
        assertThat(generalDropItem.getChance()).isEqualTo(100.0);
        assertThat(generalDropItem.getMin()).isEqualTo(amount);
        assertThat(generalDropItem.getMax()).isEqualTo(amount);
    }

    @Test
    public void shouldGenerateSingleDropItemWithItemIdAndChance() {
        double chance = 75.0;

        IDropItem dropItem = QuestDroplist.singleDropItem(QUEST_ITEM_1.getId(), chance);

        assertThat(dropItem).isInstanceOf(GeneralDropItem.class);

        GeneralDropItem generalDropItem = (GeneralDropItem) dropItem;
        assertThat(generalDropItem.getItemId()).isEqualTo(QUEST_ITEM_1.getId());
        assertThat(generalDropItem.getChance()).isEqualTo(chance);
        assertThat(generalDropItem.getMin()).isEqualTo(1);
        assertThat(generalDropItem.getMax()).isEqualTo(1);
    }

    @Test
    public void shouldGenerateSingleDropItemWithItemIdMinMaxAndChance() {
        int itemId = 2;
        long min = 1;
        long max = 5;
        double chance = 75.0;

        IDropItem dropItem = QuestDroplist.singleDropItem(itemId, min, max, chance);

        assertThat(dropItem).isInstanceOf(GeneralDropItem.class);

        GeneralDropItem generalDropItem = (GeneralDropItem) dropItem;
        assertThat(generalDropItem.getItemId()).isEqualTo(itemId);
        assertThat(generalDropItem.getChance()).isEqualTo(chance);
        assertThat(generalDropItem.getMin()).isEqualTo(min);
        assertThat(generalDropItem.getMax()).isEqualTo(max);
    }

    @Test
    public void shouldGenerateGroupedDropItem() {
        double chance = 75.0;
        IDropItem group = QuestDroplist.groupedDropItem(chance, QUEST_ITEM_2, QUEST_ITEM_3);

        assertThat(group).isInstanceOf(GroupedGeneralDropItem.class);

        GroupedGeneralDropItem groupDropItem = (GroupedGeneralDropItem) group;
        assertThat(groupDropItem.getChance()).isEqualTo(chance);
        assertThat(groupDropItem.getItems()).anySatisfy(item -> {
            assertThat(item.getItemId()).isEqualTo(QUEST_ITEM_2.getId());
            assertThat(item.getChance()).isEqualTo(QUEST_ITEM_2.getChance());
            assertThat(item.getMin()).isEqualTo(QUEST_ITEM_2.getCount());
            assertThat(item.getMax()).isEqualTo(QUEST_ITEM_2.getCount());
        });
        assertThat(groupDropItem.getItems()).anySatisfy(item -> {
            assertThat(item.getItemId()).isEqualTo(QUEST_ITEM_3.getId());
            assertThat(item.getChance()).isEqualTo(QUEST_ITEM_3.getChance());
            assertThat(item.getMin()).isEqualTo(QUEST_ITEM_3.getCount());
            assertThat(item.getMax()).isEqualTo(QUEST_ITEM_3.getCount());
        });
    }

    @Test
    public void shouldAddDropToExistingNpcAndRetrieveInfo() {
        when(npc.getId()).thenReturn(1);

        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addSingleDrop(1, QUEST_ITEM_2)
                .build();

        QuestDropInfo dropInfo = dropList.get(1, QUEST_ITEM_1.getId());
        assertThat(dropInfo).isNotNull();
        assertThat(dropInfo.item()).isEqualTo(QUEST_ITEM_1);

        QuestDropInfo dropInfo2 = dropList.get(1, QUEST_ITEM_2);
        assertThat(dropInfo2).isNotNull();
        assertThat(dropInfo2.item()).isEqualTo(QUEST_ITEM_2);

        QuestDropInfo dropInfo3 = dropList.get(npc, QUEST_ITEM_2);
        assertThat(dropInfo3).isNotNull();
        assertThat(dropInfo3.item()).isEqualTo(QUEST_ITEM_2);
    }

    @Test
    public void shouldReturnNullForInvalidKeys() {
        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addSingleDrop(1, QUEST_ITEM_2)
                .build();

        QuestDropInfo dropInfo = dropList.get(2);
        assertThat(dropInfo).isNull();

        QuestDropInfo dropInfo2 = dropList.get(2, QUEST_ITEM_3.getId());
        assertThat(dropInfo2).isNull();

        QuestDropInfo dropInfo3 = dropList.get(1, QUEST_ITEM_3.getId());
        assertThat(dropInfo3).isNull();
    }

    @Test
    public void shouldGetAllItemIds() {
        QuestDroplist dropList = QuestDroplist.builder()
                .addSingleDrop(1, QUEST_ITEM_1)
                .addGroupedDrop(2, 100.0)
                .withDropItem(QUEST_ITEM_2)
                .withDropItem(QUEST_ITEM_3)
                .build()
                .addSingleDrop(3, QUEST_ITEM_1)
                .build();

        Set<Integer> itemIds = dropList.getItemIds();

        assertThat(itemIds).containsExactlyInAnyOrder(QUEST_ITEM_1.getId(), QUEST_ITEM_2.getId(), QUEST_ITEM_3.getId());
    }
}
