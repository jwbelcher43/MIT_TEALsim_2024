/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: HasEngine.java,v 1.3 2007/07/16 22:05:01 pbailey Exp $ 
 * 
 */

package teal.sim.engine;


/**
* Provides for any object which is directly 
* controled through the SimEngine.
**/
public interface HasEngine
{
	/**
	 * Returns the SimEngine for this object.
	 * 
	 * @return the model.
	 */
	public TEngine getEngine();
	/**
	 * Sets the SimEngine for this object. 
	 * 
	 * @param model
	 */
	public void setEngine(TEngine model);
}
