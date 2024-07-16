/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: HasLength.java,v 1.4 2007/08/16 22:09:43 jbelcher Exp $
 * 
 */

package teal.render.geometry;

/**
 * 
 * @author Andrew McKinney
 * @author Phil Bailey
 * @author Michael Danziger
 * @version $Revision: 1.4 $
 */

/**
 * Interface for rendered objects that have length.
 */
public interface HasLength {

    /**
     * Sets the length of this object.
     * 
     * @param length new length.
     */
    public void setLength(double length);

    /**
     * Gets the length of this object.
     * 
     * @return length.
     */
    public double getLength();
}