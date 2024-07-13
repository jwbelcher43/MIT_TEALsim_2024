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
 * @author John Belcher
 * @version $Revision: 1.4 $
 */

/**
 * Interface for rendered objects that have width.
 */
public interface HasWidth {

    /**
     * Sets the width of this object.
     * 
     * @param length new width.
     */
    public void setWidth(double width);

    /**
     * Gets the width of this object.
     * 
     * @return width.
     */
    public double getWidth();
}