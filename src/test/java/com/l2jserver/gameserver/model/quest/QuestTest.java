package com.l2jserver.gameserver.model.quest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class QuestTest {

    private Quest quest;

    @BeforeEach
    void setUp() {
        quest = new Quest(1, "Test quest", "A test quest");
    }

    @Test
    public void shouldRegisterQuestItems() {
        quest.registerQuestItems(1, 2);

        assertThat(quest.getRegisteredItemIds()).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    public void shouldRegisterQuestItemsWithSet() {
        quest.registerQuestItems(Set.of(1, 2));

        assertThat(quest.getRegisteredItemIds()).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    public void shouldAddToRegisteredQuestItemsIfCalledMultipleTimes() {
        quest.registerQuestItems(1, 2);
        quest.registerQuestItems(3, 4);
        quest.registerQuestItems(Set.of(5, 6));

        assertThat(quest.getRegisteredItemIds()).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6);
    }
}