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
package com.l2jserver.gameserver.model.actor.stat;

import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Noé Caratini aka Kita
 * @version 2.6.3.0
 */
@ExtendWith(MockitoExtension.class)
public class SummonStatTest {

    @Mock
    private L2Summon summon;
    @Mock
    private L2PcInstance player;
    @Mock
    private PcStat pcStat;

    private SummonStat summonStat;

    @BeforeEach
    public void setUp() {
        summonStat = new SummonStat(summon);

        when(summon.getOwner()).thenReturn(player);
        when(player.getStat()).thenReturn(pcStat);
    }

    @Test
    public void shouldReturnOwnersMaxBuffCount() {
        int maxBuffs = 24;

        when(pcStat.getMaxBuffCount()).thenReturn(maxBuffs);

        int result = summonStat.getMaxBuffCount();

        assertThat(result).isEqualTo(maxBuffs);
    }
}
