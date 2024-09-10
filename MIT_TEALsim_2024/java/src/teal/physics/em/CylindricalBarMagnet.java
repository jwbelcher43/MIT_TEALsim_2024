/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: MagneticDipole.java,v 1.36 2008/01/10 05:54:48 jbelcher Exp $ 
 * 
 */

package teal.physics.em;

import javax.swing.ImageIcon;
import javax.vecmath.*;

import teal.config.Teal;
import teal.core.TUpdatable;
import teal.math.SpecialFunctions;
import teal.render.HasRotation;
import teal.render.j3d.MagDipoleNode3D;
import teal.render.scene.TNode3D;
import teal.util.*;

/**
 * Represents a magnetic point-dipole.  See Section 3.1 of the 
 * <a href="C:\Development\Projects\generalDoc\TEAL_Physics_Math.pdf"> 
 * TEAL Physics and Mathematics</a> documentation.  
 */
public class CylindricalBarMagnet extends Dipole {

    private static final long serialVersionUID = 3257290227361068337L;
    protected double CEIaccuracy = 0.0002;  // default accuracy for elliptic Integral computation
    protected transient double radius_d = Teal.CylindricalBarMagnetDefaultRadius;
    protected double mu;
	protected transient double mu_d;
    protected boolean feelsBField = true;
    protected boolean avoid_singularity = false;
    protected double avoid_singularity_scale = 1;
	protected double pauliDistance = -1.;
    public CylindricalBarMagnet() {
        super();
        generatingBField = true;
        radius = Teal.MagnetRadius;
        length = Teal.MagnetLength;

        mu = Teal.MagnetDefaultMu;
        mu_d = mu;
        setMass(Teal.MagnetMass);
        setColor(Teal.MagnetColor);

    }

    /** 
     * Returns dipole moment calculated as a function of direction and mu.
     */
    public Vector3d getDipoleMoment() {
        Matrix3d mat = new Matrix3d();
        mat.set(orientation_d);
        Vector3d direction = new Vector3d(initialDirection);
        mat.transform(direction);
        Vector3d dipoleMoment = direction; // getDirection(); // Very very bad!
        dipoleMoment.scale(mu);
        return dipoleMoment;
    }

    /**
     * Returns the magnitude of the dipole moment (mu).
     * 
     * @return the magnitude of the dipole moment.
     */
    public double getMu() {
        return mu;
    }

    /**
     * Sets the magnitude of the dipole moment to this value.
     * 
     * @param ch the new magnitude of the dipole moment.
     */
    public void setMu(double ch) {
        double c = (ch);
        TDebug.println(1, id + ": setting mu to: " + c);
        Double old = new Double(mu);
        mu = c;
        if (theEngine != null) theEngine.requestSpatial();
        firePropertyChange("mu", old, new Double(mu));

        renderFlags |= COLOR_CHANGE;

    }

    public void render() {
        if ((renderFlags & COLOR_CHANGE) == COLOR_CHANGE) {
            if (mNode != null && mNode instanceof MagDipoleNode3D) ((MagDipoleNode3D) mNode).fixColor(mu);
            renderFlags ^= COLOR_CHANGE;
        }
        super.render();
    }

    /** 
     * Sets whether this object responds to external magnetic fields.
     * 
     * @param x well does it??
     */
    public void setFeelsBField(boolean x) {
        feelsBField = x;
        if (theEngine != null) theEngine.requestSpatial();
    }

    public Vector3d getExternalForces() {
        Vector3d externalForces = super.getExternalForces();
        if (Double.isNaN(externalForces.length())) {
            TDebug.println(2,"NaN(1) in teal.sim.physical.MagneticDipole.geteExternalForces().");
        }
        try {
            if (feelsBField) {
                Matrix3d gradB = ((EMEngine)theEngine).getBField().getGradient(position_d, this);
                Vector3d bForces = new Vector3d();
                Vector3d dBdx = new Vector3d();
                Vector3d dBdy = new Vector3d();
                Vector3d dBdz = new Vector3d();
                gradB.getColumn(0, dBdx);
                gradB.getColumn(1, dBdy);
                gradB.getColumn(2, dBdz);
                Vector3d dipoleMoment = getDipoleMoment();
                bForces.x = dipoleMoment.dot(dBdx);
                bForces.y = dipoleMoment.dot(dBdy);
                bForces.z = dipoleMoment.dot(dBdz);
                externalForces.add(bForces);
            }
        } catch (ArithmeticException ae) {
            TDebug.printThrown(0, ae);
        }

        if (Double.isNaN(externalForces.length())) {
            TDebug.println(2,"NaN(2) in teal.sim.physical.MagneticDipole.geteExternalForces().");
        }

        return externalForces;
    }

    protected Vector3d getTorque() {
        //get torsional damping from PhysicalObject
        Vector3d T = super.getTorque();
        Vector3d m = new Vector3d(getDipoleMoment());
        Vector3d B = ((EMEngine)theEngine).getBField().get(position_d, this);
        Vector3d retour = new Vector3d();
        retour.cross(m, B);
        T.add(retour);
        return T;
    }

    /**
     * Evaluates the magnetic field at the given position and time.
     * 
     * @see #getB(Vector3d)
     * @param pos Position to evaluate the magnetic field at.
     * @param t Time to evaluate the magnetic field at.
     * @return Magnetic field value at the given position and time.
     */
    public Vector3d getB(Vector3d pos, double t) {
        return getB(pos);
    }

    /**
     * Evaluates the magnetic field at the given position. If the point of
     * evaluation is on-axis, a closed form expression is used. Otherwise, the
     * <code>ellipticalIntegral</code> method is used for the evaluation.   
     * See Section 3.2 of the 
     * <a href="C:\Development\Projects\generalDoc\TEAL_Physics_Math.pdf"> 
     * TEAL Physics and Mathematics</a> documentation. 
     * 
     * @see teal.math.SpecialFunctions#ellipticIntegral(double, double, double, double, double)
     * @param point Position to evaluate the magnetic field at.
     * @return Magnetic field value at the given position.
     */
    public Vector3d getB(Vector3d point) {

        // If the dipole moment of the ring is zero, we return a zero vector for B.
        Vector3d dipoleMoment = new Vector3d(getDipoleMoment());
        if (dipoleMoment.length() <= Teal.DoubleZero) {
            return new Vector3d();
        }

        // Variable declarations.
        // *********************************************************************
        Vector3d value = null;
        Vector3d temp = new Vector3d();
        Vector3d zprime = new Vector3d();
        Vector3d xprime = new Vector3d();
        Vector3d R = new Vector3d();
        double f, Zprime, Xprime, Zprime_norm, Xprime_norm, Zprime_norm2, BR, BZ;
        double r12, ks, kc, k, h, L1, L2, G0, G1;

        // Computations.
        // *********************************************************************

        // Construct dipole centered coordinate system x'y'z' with z' axis along
        // the ring normal dipole axis M and the x'-axis perpendicular to that
        // vector and in the plane of X and M. The B-field of the ring will have
        // only x' and z' components. Zprime and Rprime are the coordinates of
        // the observation point as seen in this prime coordinate system, as
        // seen from the center of the ring.

        R.sub(position_d, point);
        zprime.set(dipoleMoment);
        zprime.normalize();
        Zprime = zprime.dot(R);
        xprime.set(R);
        temp.set(zprime);
        temp.scale(Zprime);
        xprime.sub(temp);

        // On-axis computation.
        // *********************************************************************
        if (xprime.length() < Teal.DoubleZero) {
            double dK = (R.length() * R.length()) + (radius_d * radius_d);
            dK = Math.pow(dK, 1.5);
            dK = 1. / dK;
            dipoleMoment.scale(2.*dK *Teal.PermitivityVacuumOver4Pi);
            value = dipoleMoment;
            return value;
        }

        // Off-axis computation by elliptic integration.
        // *********************************************************************
        xprime.normalize();
        Xprime = xprime.dot(R);

        //  Normalize lengths to radius of ring
        Zprime_norm = Zprime / radius_d;
        Xprime_norm = Xprime / radius_d;
        Zprime_norm2 = Zprime_norm * Zprime_norm;

        r12 = Zprime_norm2 + (Xprime_norm + 1.) * (Xprime_norm + 1.);
        ks = (Zprime_norm2 + (Xprime_norm - 1.) * (Xprime_norm - 1.)) / r12;
        kc = Math.sqrt(ks);
        k = Math.sqrt(1. - ks);
        h = 1. + ks - (1. - ks) * Xprime_norm;
        G0 = SpecialFunctions.ellipticIntegral(kc, 1., -1., 1.,CEIaccuracy);
        G1 = .5 * SpecialFunctions.ellipticIntegral(kc, ks, -1., 1.,CEIaccuracy);
        L1 = (G0 + h * G1) * k / Math.pow(Xprime_norm, 1.5);
        L2 = Zprime_norm * G1 * k * k * k / Math.pow(Xprime_norm, 1.5);
        f = dipoleMoment.length() / (Math.PI * Math.pow(radius_d, 3.));
        BR = f * L2;
        BZ = f * L1;

        //  We have found the vector field components in our dipole centered
        //  coordinate system. Now we reconstruct the total vector field in xyz.
        
        value = new Vector3d();
        Vector3d repBR = new Vector3d(xprime);
        Vector3d repBZ = new Vector3d(zprime);
        repBR.scale(BR);
        repBZ.scale(BZ);
        value.add(repBR);
        value.add(repBZ);
        value.scale(Teal.PermitivityVacuumOver4Pi);

        return value;
    }

    /**
     * Setting "avoid singularity" to true is equivalent to approximating the B field
     * by a uniform value within a disk around the singular point of the magnet. 
     */
    public void setAvoidSingularity(boolean x) {
        avoid_singularity = x;
    }

    public boolean getAvoidSingularity() {
        return avoid_singularity;
    }

    /**
     * The dimension of the disk within which the B field is approximated.  The approximation is a uniform 
     * value is of radius scale*1e-1 and thickness 2.*scale*1e-2. 
     */
    public void setAvoidSingularityScale(double x) {
        avoid_singularity_scale = x;
    }

    public double getAvoidSingularityScale() {
        return avoid_singularity_scale;
    }

    /**
     * Returns the "flux" value of this dipole at the given position.  This uses the flux function as given 
     * by equation (3.1.3.1) of the <a href="C:\Development\Projects\generalDoc\TEAL_Physics_Math.pdf"> 
     * TEAL Physics and Mathematics</a>  documentation.  Note that we are taking mu naught to be 
     * 1 in this expression and multiplying by a factor of 100 simply to make the flux values in a reasonable 
     * range.  Any change in this factor of 100 should be accompanied by similar changes in anything else 
     * used in magnetic flux calculations, e.g. the ring of current.  
     */
    public double getBFlux(Vector3d pos) {
        Vector3d zprime = new Vector3d(getDipoleMoment());
        if (zprime.length() == 0.) {
            return 0.;
        } else {
            //zprime.normalize();
            Vector3d r = new Vector3d();
            r.sub(pos, position_d);
            double angle = r.angle(zprime);
            double flux = 100. * (0.5 * getDipoleMoment().length() * Math.pow(Math.sin(angle), 2)) * (1. / r.length());
            return flux;
        }

    }

    public Vector3d getE(Vector3d pos) {
        // non-relativistic calculation of E field of moving
        // magnetic dipole as -VxB, assuming V in m/sec
        Vector3d bVector = getB(pos);
        Vector3d eVector = new Vector3d();
        eVector.cross(velocity, bVector);
        eVector.scale(-1);
        return eVector;
    }

    // Placeholder. Must return the electric potential.
    public double getEPotential(Vector3d pos) {
        return 0.;
    }

    public Vector3d getE(Vector3d pos, double t) {
        // non-relativistic calculation of E field of moving
        // magnetic dipole as -VxB, assuming V in m/sec
        Vector3d bVector = getB(pos);
        Vector3d eVector = new Vector3d();

        eVector.cross(velocity, bVector);
        eVector.scale(-1);
        return eVector;
    }
    
    /*  start jwb add 9/9/2024
    
	public Vector3d getP(Vector3d position, double t) {
		return getP(position);
	}

	/**
	 * <code>getP</code> returns the Pauli field generated by this
	 * Magnetic Dipole at a point.
	 *
	 * @param pos Distance at which P Field is calculated
	 * @return P Field
	 */
	public Vector3d getP(Vector3d pos) {
		Vector3d R = new Vector3d();
		R.sub(pos, this.position_d);
		double r_2 = R.lengthSquared();
		double r = R.length();

		// k = Pauli power.	
		double k = 12.0;
		// r0 = Pauli distance.	
		double r0 = (pauliDistance < 0.)?(2.*radius):pauliDistance;

		R.normalize();

		// A scale of 1 exactly counterbalances the electric field.
		// Ad-hoc scale.
//		double scale = (r>r0)?0.:(10.*(r0-r)/r0+1.);
		// Pauli scale.
		double scale = Math.pow(r0/r, (k-2.));
		R.scale(Teal.PermitivityVacuumOver4Pi* Math.abs(this.mu_d) * scale / ( r_2));

		return R;
	}

	public void setGeneratingP(boolean b) {
		generatingPField= b;
		if(theEngine != null)
		theEngine.requestSpatial();
	}

	public boolean isGeneratingP() {
		return generatingPField;
	}
	
	public double getPauliDistance()
	{
		return this.pauliDistance;
	}
	
	public void setPauliDistance(double distance)
	{
		this.pauliDistance = distance;
		if(theEngine != null)
			theEngine.requestSpatial();
	}
	

	
	//   end jwb add 9/9/2024
    public Matrix3d getGradientBField(Vector3d pos) {
        Matrix3d m = new Matrix3d();
        Vector3d bFieldTest;
        Vector3d bField = getB(pos);

        bFieldTest = getB(new Vector3d(pos.x + epsilon, pos.y, pos.z));
        m.m00 = m.m10 = m.m20 = (bFieldTest.x - bField.x) / epsilon;

        bFieldTest = getB(new Vector3d(pos.x, pos.y + epsilon, pos.z));
        m.m01 = m.m11 = m.m21 = (bFieldTest.y - bField.y) / epsilon;

        bFieldTest = getB(new Vector3d(pos.x, pos.y, pos.z + epsilon));
        m.m02 = m.m12 = m.m22 = (bFieldTest.z - bField.z) / epsilon;

        return m;
    }

    /**
     * this returns a newly constructed Matrix with all values set to zero.
     */
    public Matrix3d getGradientEField(Vector3d pos) {
        return new Matrix3d();
    }

    public ImageIcon getIcon() {
        return (ImageIcon) IconCreator.getIcon("Magnet.gif");
    }

    protected TNode3D makeNode() {
        MagDipoleNode3D node = new MagDipoleNode3D();
        node.setElement(this);
        node.updateGeometry(length,radius);
        node.fixColor(mu);
        node.setRotation(orientation);
        return node;
    }
    protected void updateNodeColor(){
    	if (mNode instanceof MagDipoleNode3D) ((MagDipoleNode3D)mNode).fixColor(mu);
    }
    protected void updateNodeGeometry(){
    	if (mNode instanceof MagDipoleNode3D) ((MagDipoleNode3D)mNode).updateGeometry(length,radius);
    }
    protected void updateNode() {
        if (mNode != null) {
            if (mNode instanceof MagDipoleNode3D) ((TUpdatable) mNode).update();
            ((HasRotation) mNode).setRotation(orientation);
        }
    }

}
