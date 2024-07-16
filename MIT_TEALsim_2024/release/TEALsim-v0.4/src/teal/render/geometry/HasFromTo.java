/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: HasFromTo.java,v 1.3 2007/07/16 22:04:52 pbailey Exp $
 * 
 */

package teal.render.geometry;

import javax.vecmath.*;

/**
 * 
 * @author Andrew McKinney
 * @author Phil Bailey
 * @author Michael Danziger
 * @version $Revision: 1.3 $
 */

/**
 * Interface for rendered objects that have a start point and end point (such as Line).
 */
public interface HasFromTo {

    /**
     * Sets the start (from) and end (to) points of this object.
     * 
     * @param from new start point.
     * @param to new end point.
     */
    public void setFromTo(Vector3d from, Vector3d to);
}