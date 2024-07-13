/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TShapeNode.java,v 1.5 2007/07/16 22:04:57 pbailey Exp $ 
 * 
 */

package teal.render.scene;

import java.util.Enumeration;

import teal.render.HasColor;
import com.sun.j3d.utils.geometry.GeometryInfo;

/** Interface for the
 */

public interface TShapeNode extends TAbstractShapeNode, TNode3D, HasColor{

	public void setGeometry(GeometryInfo geo);
	public void setGeometry(GeometryInfo geo, int idx);
	public void addGeometry(GeometryInfo geo);
	public GeometryInfo getGeometry();
	public Enumeration getAllGeometries();
	public void removeAllGeometry();

    public void setShininess(float shine);
    public void setTransparency(float x);

}