/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TNameResolver.java,v 1.3 2007/07/16 22:04:45 pbailey Exp $
 * 
 */

package teal.core;

/**
 * 
 * @author Andrew McKinney
 * @author Phil Bailey
 * @author Michael Danziger
 * @version $Revision: 1.3 $
 */

public interface TNameResolver {

    /**
     * Maps the objects name to the TealElement
     */
    public TElement fetchTElement(String name);
}