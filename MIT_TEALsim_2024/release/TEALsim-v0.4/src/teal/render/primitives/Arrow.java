/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: Arrow.java,v 1.5 2007/07/16 22:04:56 pbailey Exp $ 
 * 
 */

package teal.render.primitives;

import javax.media.j3d.IndexedGeometryArray;
import javax.vecmath.*;

import teal.render.geometry.HasFromTo;
import teal.render.j3d.*;
import teal.render.scene.*;

/**
 * This class generates a Rendered arrow, which can be added to the world directly.
 * 
 */
public class Arrow extends Line {

    private static final long serialVersionUID = 3257850995487815993L;
    
    private boolean solid = false;
    private IndexedGeometryArray geo;

    public Arrow() {
        super();
    }

    /**
     * Creates an arrow that points from and to the given points.
     * 
     * @param from position of the base of the arrow.
     * @param to position of the head of the arrow.
     */
    public Arrow(Vector3d from, Vector3d to) {
        super(from, to);
    }
    
    public Arrow(Vector3d from, Vector3d to, IndexedGeometryArray geo) {
    	this(from,to);
    	solid = true;
    	
    }

    protected TNode3D makeNode() {
    	TShapeNode node;
    	if (solid) {
    		node = (TShapeNode) new SolidArrowNode();
    		((SolidArrowNode) node).setGeometry(geo);
    	} else {
    		node = (TShapeNode) new ArrowNode();
    	}
        ((HasFromTo) node).setFromTo(position, drawTo);
        node.setColor(mColor);
        return node;
    }
}
