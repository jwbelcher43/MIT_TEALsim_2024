/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: EMEngine.java,v 1.8 2008/12/23 19:31:24 jbelcher Exp $ 
 * 
 */

package teal.physics.em;

import java.util.*;
import javax.vecmath.*;
import teal.config.*;
import teal.core.*;
import teal.field.*;
import teal.physics.*;
import teal.physics.physical.*;
import teal.sim.*;
import teal.sim.engine.*;


/**
 * <code>EMEngine</code> is the Physics simulation engine.
 *
 */

public class EMEngine extends SimEngine {

    private static final long serialVersionUID = 3257007661496152120L;

    /** 
     * List of all objects of type <code>PhysicalObject</code> that were added to the engine.
     * 
     * @see #addSimElement(TSimElement)
     * @see #addSimElements(Collection)
     * @see #removeSimElement(TSimElement)
     * @see #removeSimElements(Collection)
     */

    protected List physObjs;

    /**
     * Damping coefficient of the world. This is used mainly by all
     * <code>PhysicalObject</code>s, through subjecting them to an
     * additional force proportional to their velocity, with the
     * proportionality constant being <code>-damping</code>.
     * 
     * @see #getDamping()
     * @see #setDamping(double)
     */
    protected double damping = Teal.DefaultWorldDumping;

    /**
     * Gravitational acceleration vector. This is made a direct parameter of
     * the engine, instead of, for example, a gravitational field generating
     * object, for reasons of performance and simplicity.
     */
    protected Vector3d gravity = new Vector3d(0., -9.81, 0.);

    /**
     * Composite gravitational field, which groups all gravitional field generating
     * objects within a wrapper class that simplifies field querying. Queries are
     * either total, or exclude a single object, which is useful when computing the
     * value of the field experienced by that object, due to all others.
     * 
     * @see teal.physics.GField
     */
    protected GField gField;
    /**
     * Composite magnetic field, which groups all magnetic field generating
     * objects within a wrapper class that simplifies field querying. Queries are
     * either total, or exclude a single object, which is useful when computing the
     * value of the field experienced by that object, due to all others.
     * 
     * @see teal.physics.em.BField
     */
    protected BField bField;
    /**
     * Composite electric field, which groups all electric field generating
     * objects within a wrapper class that simplifies field querying. Queries are
     * either total, or exclude a single object, which is useful when computing the
     * value of the field experienced by that object, due to all others.
     * 
     * @see teal.physics.em.EField
     */
    protected EField eField;
    
    /**
     * Composite Pauli field, which groups all Pauli field generating
     * objects within a wrapper class that simplifies field querying. Queries are
     * either total, or exclude a single object, which is useful when computing the
     * value of the field experienced by that object, due to all others.
     * 
     * @see teal.physics.em.PField
     */
    protected PField pField;

    public EMEngine() {
        super();
        physObjs = new ArrayList();
        gField = new GField();
        bField = new BField();
        eField = new EField();
        pField = new PField();
    }

    /**
     * Calls the default constructor, but overrides the default
     * <code>AbstractElement</code> ID by the specified one.
     * 
     * @param idStr 
     *
     */
    public EMEngine(String idStr) {
        this();
        id = idStr;
    }

    synchronized public void synchAddSimElement(TSimElement obj) {
        /*
         * The status save and restore scheme used below insures, if this
         * method is called at any point other than the top of the run loop, (this
         * is possible, if addSimElement is called say, when the engine thread has
         * not yet started), that we do not concurrently proceed with a regular run.
         * 
         * It might be wiser to rely on a more robust way to do this.
         */

        boolean status = waiting;
        
        if (!status) waiting = true;
        
        super.synchAddSimElement(obj);

        if (obj instanceof PhysicalObject) {
            physObjs.add(obj);
        }

        if (obj instanceof GeneratesG) {
            gField.add(obj);
            requestSpatial();
        }

        if (obj instanceof GeneratesB) {
            bField.add(obj);
            requestSpatial();
        }

        if (obj instanceof GeneratesE) {
            eField.add(obj);
            requestSpatial();
        }

        if (obj instanceof GeneratesP) {
            pField.add(obj);
            requestSpatial();
        }
        waiting = status;
    }

    public synchronized void synchRemoveSimElement(TSimElement obj) {
        /*
         * The status save and restore scheme used below insures, if this
         * method is called at any point other than the top of the run loop, (this
         * is possible, if removeSimElement is called say, when the engine thread has
         * not yet started), that we do not concurrently proceed with a regular run.
         * 
         * It might be wiser to rely on a more robust way to do this.
         */
        boolean status = waiting;
        if (!status) waiting = true;

        if (obj instanceof GeneratesG) {
            gField.remove(obj);
            requestSpatial();
        }

        if (obj instanceof GeneratesB) {
            bField.remove(obj);
            requestSpatial();
        }

        if (obj instanceof GeneratesE) {
            eField.remove(obj);
            requestSpatial();
        }

        if (obj instanceof GeneratesP) {
            pField.remove(obj);
            requestSpatial();
        }

        if (obj instanceof PhysicalObject) {
            //((PhysicalObject) obj).removeReferents();
            physObjs.remove(obj);
        }

        super.synchRemoveSimElement(obj);
        waiting = status;
    }

    /**
     * Returns a cloned copy of the model's current <code>PhysicalObject</code>s,
     * changes to this list will not effect the engine. Objects must be removed
     * from the engine via the <code>removeElement</code> methods. 
     */
    public Collection getPhysicalObjs() {
        return new ArrayList(physObjs);
    }
    
    /**
     * Gets the gravitational acceleration vector.
     * 
     * @return Current gravitational acceleration vector.
     * @see #gravity
     */
    public Vector3d getGravity() {
        return new Vector3d(gravity);
    }

    /**
     * Sets the gravitational acceleration vector.
     * 
     * @param gravity Desired gravitational acceleration vector.
     * @see #gravity
     */
    public void setGravity(Vector3d gravity) {
        this.gravity = new Vector3d(gravity);
    }
    
    public double getDamping() {
        return damping;
    }

    public void setDamping(double val) {
        damping = val;
    }

    /**
     * Gets the composite gravitational field.
     * 
     * @see #gField
     * @return Current composite gravitational field.
     */
    public CompositeField getGField() {
        return gField;
    }

    /**
     * Gets the composite magnetic field.
     * 
     * @see #bField
     * @return Current composite magnetic field.
     */
    public CompositeField getBField() {
        return bField;
    }

    /**
     * Gets the composite electric field.
     * 
     * @see #eField
     * @return Current composite electric field.
     */
    public CompositeField getEField() {
        return eField;
    }

    /**
     * Gets the composite Pauli field.
     * 
     * @see #pField
     * @return Current composite Pauli field.
     */
    public CompositeField getPField() {
        return pField;
    }

    /**
     * Uses the composite gravitional field object to obtain the field
     * value at the given position.
     * 
     * @param position Position to evaluate the gravitional field at.
     * @see #gField
     * @return Gravitional field value at the given position.
     */
    public Vector3d getG(Vector3d position) {
        return gField.get(position);
    }

    /**
     * Uses the composite gravitional field object to obtain the field
     * value at the given position and time. 
     * 
     * @param position Position to evaluate the gravitional field at.
     * @param time Time to evaluate the gravitional field at.
     * @see #gField
     * @return Gravitational field value at the given position and time.
     */
    public Vector3d getG(Vector3d position, double time) {
        return gField.get(position, time);
    }

    /**
     * Uses the composite gravitional field object to obtain the field
     * value at the given position, excluding a single object, as specified.
     * 
     * @param position Position to evaluate the gravitional field at.
     * @param excluded Object excluded from the evaluation of the field.
     * @see #gField
     * @return Gravitational field value at the given position,
     *         excluding the specified object.
     */
    public Vector3d getG(Vector3d position, TElement excluded) {
        return gField.get(position, excluded);
    }

    /**
     * Uses the composite gravitional field object to obtain the field
     * value at the given position and time, excluding a single object,
     * as specified.
     * 
     * @param position Position to evaluate the gravitional field at.
     * @param excluded Object excluded from the evaluation of the field.
     * @param time Time to evaluate the gravitional field at.
     * @see #gField
     * @return Gravitational field value at the given position and time,
     *         excluding the specified object.
     */
    public Vector3d getG(Vector3d position, TElement excluded, double time) {
        return gField.get(position, excluded, time);
    }

    /**
     * Uses the composite magnetic field object to obtain the field
     * value at the given position.
     * 
     * @param position Position to evaluate the magnetic field at.
     * @see #bField
     * @return Magntetic field value at the given position.
     */
    public Vector3d getB(Vector3d position) {
        return bField.get(position);
    }

    /**
     * Uses the composite magnetic field object to obtain the field
     * value at the given position and time. 
     * 
     * @param position Position to evaluate the magnetic field at.
     * @param time Time to evaluate the magnetic field at.
     * @see #bField
     * @return Magnetic field value at the given position and time.
     */
    public Vector3d getB(Vector3d position, double time) {
        return bField.get(position, time);
    }

    /**
     * Uses the composite magnetic field object to obtain the field
     * value at the given position, excluding a single object, as specified.
     * 
     * @param position Position to evaluate the magnetic field at.
     * @param excluded Object excluded from the evaluation of the field.
     * @see #bField
     * @return Magnetic field value at the given position,
     *         excluding the specified object.
     */
    public Vector3d getB(Vector3d position, TElement excluded) {
        return bField.get(position, excluded);
    }

    /**
     * Uses the composite magnetic field object to obtain the field
     * value at the given position and time, excluding a single object,
     * as specified.
     * 
     * @param position Position to evaluate the magnetic field at.
     * @param excluded Object excluded from the evaluation of the field.
     * @param time Time to evaluate the magnetic field at.
     * @see #bField
     * @return Magnetic field value at the given position and time,
     *         excluding the specified object.
     */
    public Vector3d getB(Vector3d position, TElement excluded, double time) {
        return bField.get(position, excluded, time);
    }

    /**
     * Uses the composite electric field object to obtain the field
     * value at the given position.
     * 
     * @param position Position to evaluate the electric field at.
     * @see #eField
     * @return Electric field value at the given position.
     */
    public Vector3d getE(Vector3d position) {
        return eField.get(position);
    }

    /**
     * Uses the composite electric field object to obtain the field
     * value at the given position and time. 
     * 
     * @param position Position to evaluate the electric field at.
     * @param time Time to evaluate the electric field at.
     * @see #eField
     * @return Electric field value at the given position and time.
     */
    public Vector3d getE(Vector3d position, double time) {
        return eField.get(position, time);
    }

    /**
     * Uses the composite electric field object to obtain the field
     * value at the given position, excluding a single object, as specified.
     * 
     * @param position Position to evaluate the electric field at.
     * @param excluded Object excluded from the evaluation of the field.
     * @see #eField
     * @return Electric field value at the given position,
     *         excluding the specified object.
     */
    public Vector3d getE(Vector3d position, TElement excluded) {
        return eField.get(position, excluded);
    }

    /**
     * Uses the composite electric field object to obtain the field
     * value at the given position and time, excluding a single object,
     * as specified.
     * 
     * @param position Position to evaluate the electric field at.
     * @param excluded Object excluded from the evaluation of the field.
     * @param time Time to evaluate the electric field at.
     * @see #eField
     * @return Electric field value at the given position and time,
     *         excluding the specified object.
     */
    public Vector3d getE(Vector3d position, TElement excluded, double time) {
        return eField.get(position, excluded, time);
    }

    /**
     * Uses the composite Pauli field object to obtain the field
     * value at the given position.
     * 
     * @param position Position to evaluate the Pauli field at.
     * @see #pField
     * @return Pauli field value at the given position.
     */
    public Vector3d getP(Vector3d position) {
        return pField.get(position);
    }

    /**
     * Uses the composite Pauli field object to obtain the field
     * value at the given position and time. 
     * 
     * @param position Position to evaluate the Pauli field at.
     * @param time Time to evaluate the Pauli field at.
     * @see #pField
     * @return Pauli field value at the given position and time.
     */
    public Vector3d getP(Vector3d position, double time) {
        return pField.get(position, time);
    }

    /**
     * Uses the composite Pauli field object to obtain the field
     * value at the given position, excluding a single object, as specified.
     * 
     * @param position Position to evaluate the Pauli field at.
     * @param excluded Object excluded from the evaluation of the field.
     * @see #pField
     * @return Pauli field value at the given position,
     *         excluding the specified object.
     */
    public Vector3d getP(Vector3d position, TElement excluded) {
        return pField.get(position, excluded);
    }

    /**
     * Uses the composite Pauli field object to obtain the field
     * value at the given position and time, excluding a single object,
     * as specified.
     * 
     * @param position Position to evaluate the Pauli field at.
     * @param excluded Object excluded from the evaluation of the field.
     * @param time Time to evaluate the electric field at.
     * @see #pField
     * @return Pauli field value at the given position and time,
     *         excluding the specified object.
     */
    public Vector3d getP(Vector3d position, TElement excluded, double time) {
        return pField.get(position, excluded, time);
    }

    /**
     * Uses the composite electric field object to obtain the electric
     * potential value at the given position.
     * 
     * @param position Position to evaluate the electric potential at.
     * @see #eField
     * @return Electric potential value at the given position.
     */
    public double getEPotential(Vector3d position) {
        return eField.getPotential(position);
    }   
    
    public boolean IsAnnihilatingElement(TSimElement elm){
    	boolean annihil = false;
    	if (elm instanceof PointCharge ) annihil = true;
    	if (elm instanceof InfiniteLineCharge ) annihil = true;
    	return annihil;
    }

}
