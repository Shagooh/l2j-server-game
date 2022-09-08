package com.l2jserver.gameserver.model.skills.targets;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TargetTypeTest {

    @Mock
    private Skill skill;
    @Mock
    private L2Character caster;
    @Mock
    private L2Object target;

    @Test
    public void doorTreasureShouldReturnNullIfTargetIsNull() {
        L2Object result = TargetType.DOOR_TREASURE.getTarget(skill, caster, null);

        assertThat(result).isNull();
    }

    @Test
    public void doorTreasureShouldReturnNullIfTargetIsNotADoorOrChest() {
        when(target.isDoor()).thenReturn(false);

        L2Object result = TargetType.DOOR_TREASURE.getTarget(skill, caster, target);

        assertThat(result).isNull();
    }

    @Test
    public void doorTreasureShouldReturnTargetIfDoor() {
        when(target.isDoor()).thenReturn(true);

        L2Object result = TargetType.DOOR_TREASURE.getTarget(skill, caster, target);

        assertThat(result).isSameAs(target);
    }

    @Test
    public void doorTreasureShouldReturnTargetIfChest() {
        target = mock(L2ChestInstance.class);

        L2Object result = TargetType.DOOR_TREASURE.getTarget(skill, caster, target);

        assertThat(result).isSameAs(target);
    }
}