/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TNode3D.java,v 1.9 2007/07/16 22:04:57 pbailey Exp $ 
 * 
 */

package teal.render.scene;

import javax.media.j3d.Bounds;
import javax.media.j3d.Transform3D;
import javax.vecmath.Vector3d;
import teal.render.TAbstractRendered;

/** 
* provides SceneGraphNode for the management of a single Element.
*
* @author Phil Bailey
* @version $Revision: 1.9 $
*
**/
public interface TIntegralNode3D 
{

    public TAbstractRendered getElement();
    public void setElement(TAbstractRendered element);
    public Bounds getBoundingArea();
    public boolean isSelected();
    public void setSelected(boolean b);
    public boolean isSelectable();
    public void setSelectable(boolean b);
    public boolean isVisible();
    public void setVisible(boolean b);
    public void detach();
    
    public void setDirection(Vector3d newDirection);
    public void setScale(double s);
    public void setScale(Vector3d s);
    public Vector3d getScale();
    
    public void setModelOffsetTransform(Transform3D t);
    public Transform3D getModelOffsetTransform();
    public void setModelOffsetPosition(Vector3d offset);
    public Vector3d getModelOffsetPosition();

}	

