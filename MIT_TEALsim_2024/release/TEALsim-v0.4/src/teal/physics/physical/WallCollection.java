/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: WallCollection.java,v 1.9 2007/07/17 15:46:57 pbailey Exp $ 
 * 
 */

package teal.physics.physical;

import java.util.Collection;

import teal.sim.TSimElement;

public interface WallCollection extends TSimElement{
	public Collection getWalls();
}
