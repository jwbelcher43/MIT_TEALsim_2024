/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: InfiniteWire.java,v 1.28 2008/01/05 05:54:53 jbelcher Exp $ 
 * 
 */

package teal.physics.em;

import javax.vecmath.*;

import teal.config.*;
import teal.render.geometry.HasRadius;
import teal.render.geometry.HasWidth;
import teal.render.geometry.HasHeight;
import teal.render.j3d.*;
import teal.render.scene.*;

/**
 * Represents a infinite slab of current 
 */
public class HollowCurrentShell extends EMObject implements HasHeight, HasRadius, HasWidth, HasCurrent, HasInductance, GeneratesE,
    GeneratesB {

    private static final long serialVersionUID = 3257005470995724084L;

    double inductance;
    double current;

    protected boolean generatingBField = true;
    protected boolean generatingEField = true;
    protected boolean generatingEPotential = false;
    protected boolean generatingPField = false;
    protected double radius;
    protected double height;
    protected double width;

    //Vector3d orientation;

    public HollowCurrentShell() {
        super();
        isMoveable = false;
        setCurrent(Teal.InfiniteWireDefaultCurrent);
        setMass(Teal.InfiniteWireMass);
        this.radius = 1.;   
    }

    public HollowCurrentShell(double height, double width, double radius) {
        super();
        isMoveable = false;
        setCurrent(Teal.InfiniteWireDefaultCurrent);
        setMass(Teal.InfiniteWireMass);
        this.radius = radius;
        this.height = height;
        this.width = width;
    }
    
    /**
     Stub must be replaced 
     */
    public double getEFlux(Vector3d pos) {
        return 0.;
    }

    public void setGeneratingB(boolean b) {
        generatingBField = b;
        if (theEngine != null) theEngine.requestSpatial();
    }

    public boolean isGeneratingB() {
        return generatingBField;
    }

    public void setGeneratingE(boolean b) {
        generatingEField = b;
        if (theEngine != null) theEngine.requestSpatial();
    }

    public boolean isGeneratingE() {
        return generatingEField;
    }

    public void setGeneratingP(boolean b) {
        generatingPField = b;
        if (theEngine != null) theEngine.requestSpatial();
    }

    public boolean isGeneratingP() {
        return generatingPField;
    }

    public Vector3d getB(Vector3d pos) {
        Vector3d B = new Vector3d();
        double rad = 0.;
        rad = Math.sqrt(pos.x*pos.x+pos.y*pos.y);
        B = new Vector3d(-pos.y/rad,pos.x/rad,0.);
   //     if (pos.y >= length/2.) B = new Vector3d(-1.,0.,0.);
  //      if (pos.y <= -length/2.) B = new Vector3d(1.,0.,0.);
  //      if (Math.abs(pos.y)< length/2.) {
   //     	double ratio = -(2.*pos.y)/length;
   //     	B = new Vector3d(ratio,0.,0.);
    //    }
        
            return B;

    }

    public Vector3d getB(Vector3d x, double t) {
        return getB(x);
    }

    // placeholder
    public double getBFlux(Vector3d pos) {
        Vector3d r = new Vector3d();
        Vector3d rperp = new Vector3d();
        Vector3d rpar = new Vector3d();
        Vector3d v = new Vector3d();
        double bflux;

        // This should get the distance to the closest point on the wire
        r.sub(pos, position_d);
        v = getDirection();

        rpar.scale(r.dot(v) / (v.length() * v.length()), v);
        rperp.sub(r, rpar);

        bflux = -Math.log(rperp.length()) * (current / (2. * Math.PI));
        return bflux;
    }

    public Vector3d getE(Vector3d x, double t) {
        return getE(x);
    }

    public Vector3d getE(Vector3d x) {
        return new Vector3d();
    }

    // Placeholder. Must return the electric potential.
    public double getEPotential(Vector3d pos) {
        return 0.;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double i) {
        Double old = new Double(current);
        current = i;
        firePropertyChange("current", old, new Double(current));

        // This needs to be set now that there is an arrow on the wire that indicates the direction of the current
        renderFlags |= GEOMETRY_CHANGE;

        if (theEngine != null)
        //synchronized (theEngine){
            theEngine.requestSpatial();
        //}
    }

    public double getInductance() {
        return inductance;
    }

    public void setInductance(double i) {
        Double old = new Double(inductance);
        inductance = i;
        firePropertyChange("inductance", old, new Double(inductance));

        if (theEngine != null) theEngine.requestSpatial();
    }

    public void setRadius(double radius) {
        this.radius = radius;
        renderFlags |= GEOMETRY_CHANGE;
        if (theEngine != null) theEngine.requestSpatial();
    }
    
    public double getRadius() {
        return radius;
    }

    public void setHeight(double height) {
        this.height  = height;
        renderFlags |= GEOMETRY_CHANGE;
        if (theEngine != null) theEngine.requestSpatial();
    }

    public double getHeight() {
        return this.height;
    }

    public void setWidth(double height) {
        this.width  = width;
        renderFlags |= GEOMETRY_CHANGE;
        if (theEngine != null) theEngine.requestSpatial();
    }

    public double getWidth() {
        return this.width;
    }
    



    public void render() {
        if (mNode != null) {
            if ((renderFlags & GEOMETRY_CHANGE) == GEOMETRY_CHANGE) {

                if (mNode != null) {
                    
                    ((CurrentSlabNode3D) mNode).setArrowDirection(current);
                }
                renderFlags ^= GEOMETRY_CHANGE;
                

            }
            super.render();
        }
    }

    protected TNode3D makeNode() {
      double radius = Teal.InfiniteWireDefaultRadius;
      double length = .5;
  	  TNode3D node = (TNode3D) new CurrentSlabNode3D(radius,length);
      // Scale
      Vector3d scaling = new Vector3d(height, width, 0.);
      node.setScale(scaling);

      // Position
      node.setPosition(new Vector3d(0.,1.,0.));
        node.setElement(this);
		node.setRotable(false);
        node.setPickable(isPickable);
        node.setSelectable(true);
        return node;
    }

}
