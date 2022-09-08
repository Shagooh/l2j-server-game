package com.l2jserver.gameserver.model.skills.targets;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;

/**
 * Affect Object.
 * @author Zoey76
 * @version 2.6.3.0
 */
public interface AffectObject {
	
	boolean affectObject(L2Character caster, L2Object object);
}