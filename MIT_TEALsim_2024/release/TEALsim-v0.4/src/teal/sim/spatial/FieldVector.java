/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: FieldVector.java,v 1.17 2008/11/10 18:59:56 jbelcher Exp $ 
 * 
 */

package teal.sim.spatial;

import javax.vecmath.Vector3d;

import teal.config.Teal;
import teal.render.j3d.*;
import teal.render.scene.*;
import teal.physics.em.EMEngine;

public class FieldVector extends SpatialVector {

    private static final long serialVersionUID = 3979265858943857712L;
    protected int fieldType = 0;
    public static int E_FIELD = 0;
    public static int B_FIELD = 1;
    protected boolean scaleByMagnitude = false;
    protected double scaleFactor = 1.;
    protected double normFactor = 1.;
    protected double powerScale = 1.;

    public FieldVector() {
    }

    public FieldVector(Vector3d pos, int fieldType, boolean scale) {
        position = pos;
        this.fieldType = fieldType;
        this.scaleByMagnitude = scale;

        if (fieldType == E_FIELD) {
            this.setColor(Teal.DefaultEFieldColor);
        } else if (fieldType == B_FIELD) {
            this.setColor(Teal.DefaultBFieldColor);
        }
    }

    public void nextSpatial() {
        if (fieldType == E_FIELD) {
            value.set(((EMEngine)theEngine).getEField().get(position));
            value.scale(scaleFactor);
        } else {
            value.set(((EMEngine)theEngine).getBField().get(position));
            value.scale(scaleFactor);
        }
        registerRenderFlag(GEOMETRY_CHANGE);
    }

    protected TNode3D makeNode() {
        //SolidArrowNode node = new SolidArrowNode( );
        TShapeNode node = (TShapeNode) new SolidArrowNode( );
        node.setPickable(false);
        node.setVisible(true);
        node.setColor(getColor());
        updateNode3D(node);
        return node;
    }

    public void updateNode3D(LineNode node) {
        if ((node == null) || (theEngine == null)) {
            return;
        }
        Vector3d vector = new Vector3d(value);
        if (scaleByMagnitude) {
        	double scalepower = Math.pow(value.length()/normFactor,powerScale);
            node.setScale(scalepower*arrowScale);
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
        	double scalepower = Math.pow(value.length()/normFactor,powerScale);
            node.setScale(scalepower*arrowScale);
        } else {
            node.setScale(arrowScale);
        }

        node.setPosition(position);
        if (vector.length() > 0) {
            node.setDirection(new Vector3d(vector));
        }

    }

    /**
     * @return Returns the fieldType.
     */
    public double getFieldType() {
        return fieldType;
    }

    /**
     * @param fieldType The scaleFactor to set.
     */
    public void setFieldType(int fieldType) {
        this.fieldType = fieldType;
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
    
    /**
     * @return Returns scale, whether we scale by magnitude or not.
     */
    public boolean getScaleByMagnitude() {
        return this.scaleByMagnitude;
    }

    /**
     * @param scale for whether we scale by magnitude or not.
     */
    public void setScaleByMagnitude(boolean scale) {
        this.scaleByMagnitude = scale;
    }
    /**
     * @return Returns factor used in normalization when scaling by magnitude
     */
    public double getNormFactor() {
        return this.normFactor;
    }

    /**
     * @param normFactor sets factor used in normalization when scaling by magnitude
     */
    public void setNormFactor(double normFactor) {
        this.normFactor = normFactor;
    }
    
    /**
     * @return Returns power used when scaling by magnitude
     */
    public double getPowerScale() {
        return this.powerScale;
    }

    /**
     * @param powerScale sets power used when scaling by magnitude
     */
    public void setPowerScale(double powerScale) {
        this.powerScale= powerScale;
    }
}
