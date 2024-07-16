/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: HasColor.java,v 1.7 2007/07/16 22:04:51 pbailey Exp $ 
 * 
 */

package teal.render;

import javax.vecmath.Color3f;

/**
 * Objects that have a modifiable color should implement this interface.
 */
public interface HasColor {
	/**
	 * This method should set the color of this object.
	 * 
	 * @param q new Color.
	 */
	public void setColor(Color3f q);
	/**
	 * This method should return the Color of this object.
	 * @return Color of this object.
	 */
	public Color3f getColor();
}
