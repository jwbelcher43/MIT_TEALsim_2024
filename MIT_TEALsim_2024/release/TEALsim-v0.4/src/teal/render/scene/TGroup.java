/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TGroup.java,v 1.4 2007/07/16 22:04:57 pbailey Exp $ 
 * 
 */

package teal.render.scene;

import java.util.Collection;

/** Interface for the
 */

public interface TGroup {

 
	public TNode getChild(int i); 
	public void addChild(TNode child);
    public void removeChild(TNode child);
    /** The Collection returned is a copy. */
    public Collection getChildren();
    public void removeChildren();
    public int getNumberOfChildren();

}
