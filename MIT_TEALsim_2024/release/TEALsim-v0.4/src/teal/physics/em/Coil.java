/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: Coil.java,v 1.15 2007/08/16 22:09:42 jbelcher Exp $ 
 * 
 */

package teal.physics.em;

import teal.render.geometry.Pipe;
import teal.render.j3d.*;
import teal.render.scene.*;

/**
 * This class extends RingOfCurrent, by providing a new graphical node. All
 * other functionalities are inherited from RingOfCurrent.
 * 
 * @see teal.physics.em.RingOfCurrent
 */

public class Coil extends RingOfCurrent {

	// *************************************************************************
	// Constructor and Standard Methods
	// *************************************************************************

    private static final long serialVersionUID = 3978702878875858488L;

    public Coil() {
		super();
	}

	public String toString() {
		return "Coil: " + id;
	}

	// *************************************************************************
	// Render and Graphics Methods
	// *************************************************************************

	protected TNode3D makeNode() {
		TShapeNode node = (TShapeNode) new ShapeNode();
		node.setElement(this);
		node.setGeometry(Pipe.makeGeometry(20, radius, torusRadius, torusRadius));
		//node.setAppearance(Node3D.makeAppearance(mColor,0.7f,0.f,false));
		node.setColor(mColor);
		node.setShininess(0.7f);
		node.setPickable(isPickable);
		renderFlags ^= GEOMETRY_CHANGE;
		return node;
	}

	public void render() {
		if (mNode != null) {
			if ((renderFlags & GEOMETRY_CHANGE) == GEOMETRY_CHANGE) {

				if (mNode instanceof TShapeNode) {

					((TShapeNode) mNode).setGeometry(Pipe.makeGeometry(20,
							radius, torusRadius, torusRadius));
				}
				renderFlags ^= GEOMETRY_CHANGE;
			}
			super.render();
		}
	}

}
