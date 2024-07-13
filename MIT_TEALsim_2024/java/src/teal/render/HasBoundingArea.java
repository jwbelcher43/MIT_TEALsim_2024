/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: HasBoundingArea.java,v 1.6 2007/07/16 22:04:50 pbailey Exp $ 
 * 
 */

package teal.render;

import javax.media.j3d.Bounds;

/**
 * Objects that have a bounding area should implement this interface.
 *
 */
public interface HasBoundingArea {
	/**
	 * Returns the bounding area of this object.
	 * 
	 * @return Bounding area
	 */
	public Bounds getBoundingArea();
}
