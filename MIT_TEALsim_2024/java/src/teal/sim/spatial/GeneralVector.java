package teal.sim.spatial;

/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: GeneralVector.java,v 1.1 2008/08/21 17:49:45 jbelcher Exp $ 
 * 
 */

import javax.vecmath.Vector3d;

import teal.config.Teal;
import teal.render.j3d.*;
import teal.render.scene.*;

public class GeneralVector extends SpatialVector {

    private static final long serialVersionUID = 3979265858943857712L;
    protected boolean scaleByMagnitude = false;
    protected double scaleFactor = 1.;

    public GeneralVector() {
    }

    public GeneralVector(Vector3d pos, boolean scale) {
        position = pos;
        this.scaleByMagnitude = scale;

        
        this.setColor(Teal.DefaultEFieldColor);
       
    }

    public void nextSpatial() {
       
        registerRenderFlag(GEOMETRY_CHANGE);
    }

    protected TNode3D makeNode() {
     //   SolidArrowNode node = new SolidArrowNode( );
        TShapeNode node = (TShapeNode) new SolidArrowNode( );
        node.setPickable(false);
        node.setVisible(true);
        node.setColor(getColor());
        node.setTransparency(.2f);
        updateNode3D(node);
        return node;
    }

    public void updateNode3D(LineNode node) {
        if ((node == null) || (theEngine == null)) {
            return;
        }
        Vector3d vector = new Vector3d(value);
        if (scaleByMagnitude) {
            vector.scale(value.length());
        } else {
            vector.scale(arrowScale);
        }

        Vector3d from = new Vector3d(position);
        Vector3d to = new Vector3d(from);
        to.add(vector);
        node.setFromTo(from, to);

    }

    public void updateNode3D(TShapeNode node) {
        if ((node == null) || (theEngine == null)) {
            return;
        }
        Vector3d vector = new Vector3d(value);
        if (scaleByMagnitude) {
            node.setScale(value.length());
        } else {
            node.setScale(arrowScale);
        }

        node.setPosition(position);
        if (vector.length() > 0) {
            node.setDirection(new Vector3d(vector));
        }

    }

    /**
     * @return Returns the scaleFactor.
     */
    public double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * @param scaleFactor The scaleFactor to set.
     */
    public void setScaleFactor(double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public Vector3d getValue() {
        return this.value;
    }
}
