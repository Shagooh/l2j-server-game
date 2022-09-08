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
package com.l2jserver.gameserver.datatables;

import com.l2jserver.gameserver.model.L2Spawn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Noé Caratini aka Kita
 */
@ExtendWith(MockitoExtension.class)
public class NpcPersonalAIDataTest {
    private static final String SPAWN1_NAME = "spawn1";
    private static final String PARAM_1 = "param1";
    private static final String PARAM_2 = "param2";
    private static final Map<String, Integer> SPAWN1_DATA = Map.of(
            PARAM_1, 10,
            PARAM_2, 20
    );

    @Mock
    private L2Spawn spawn;

    private NpcPersonalAIData aiData;

    @BeforeEach
    void setUp() {
        when(spawn.getName()).thenReturn(SPAWN1_NAME);

        aiData = new NpcPersonalAIData();

        aiData.storeData(spawn, SPAWN1_DATA);
    }

    @Test
    public void shouldReturnNegativeAIValueForInvalidSpawnName() {
        final int aiValue = aiData.getAIValue("invalid", "invalid");

        assertThat(aiValue).isNegative();
    }

    @Test
    public void shouldReturnNegativeAIValueForInvalidParamName() {
        final int aiValue = aiData.getAIValue(SPAWN1_NAME, "invalid");

        assertThat(aiValue).isNegative();
    }

    @Test
    public void shouldReturnAIValue() {
        final int aiValue1 = aiData.getAIValue(SPAWN1_NAME, PARAM_1);
        final int aiValue2 = aiData.getAIValue(SPAWN1_NAME, PARAM_2);

        assertThat(aiValue1).isEqualTo(10);
        assertThat(aiValue2).isEqualTo(20);
    }
}