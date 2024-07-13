/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TAbstractRendered.java,v 1.6 2007/08/14 20:52:21 pbailey Exp $ 
 * 
 */

package teal.render;

import java.io.Serializable;

public interface TAbstractRendered extends HasBoundingArea, HasPosition,
    IsMoveable, IsPickable, TDrawable, HasColor, Serializable
{
    public final static int POSITION_CHANGE = 0x001;
	public final static int SCALE_CHANGE = 0x004;
    public final static int GEOMETRY_CHANGE = 0x008;
    public final static int COLOR_CHANGE = 0x010;
	public final static int VISIBILITY_CHANGE = 0x020;
    public void registerRenderFlag(int flag);

	public boolean isSelected();
	public void setSelected(boolean b);
	
	public boolean isSelectable();
	public void setSelectable(boolean b); 
	
}
	 
