/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: ArcConstraint.java,v 1.13 2007/07/16 22:05:00 pbailey Exp $ 
 * 
 */

package teal.sim.constraint;

import javax.vecmath.Vector3d;

/**
 * @author mesrob
 *
 */
public class SphericalArcConstraint implements Constraint {

	/* We will represent an arc by its center, radius and a vector which
	 * is the normal to the plane of the arc.
	 */

	protected Vector3d center = null, normal = null;
	protected double radius = 1.;
	protected Vector3d lastReaction = new Vector3d();

	public SphericalArcConstraint( Vector3d ccenter, Vector3d nnormal, double rradius ) {
		setCoefficients( ccenter, nnormal, rradius );
	}

	public void setCoefficients( Vector3d ccenter, Vector3d nnormal, double rradius ) {
		center = new Vector3d(ccenter);
		normal = new Vector3d(nnormal);
		normal.normalize();
		radius = rradius;
	}	

	public void setPoint( Vector3d ccenter ) {
		center = new Vector3d(ccenter);
	}	
	public Vector3d getCenter() {
		return new Vector3d(center);
	}	

	public void setNormal( Vector3d nnormal ) {
		normal = new Vector3d(nnormal);
		normal.normalize();
	}	
	public Vector3d getNormal() {
		return new Vector3d(normal);
	}	

	public void setRadius( double rradius ) {
		radius = rradius;
	}	
	public double getRadius() {
		return radius;
	}	

/*	public Vector3d getReaction(Vector3d position, Vector3d velocity, Vector3d action) {
		Vector3d bar = new Vector3d(position); bar.sub(center);
		Vector3d tangent = new Vector3d();
		tangent.cross(bar, normal); tangent.normalize();
		double effective = action.dot(tangent);
		Vector3d reaction = new Vector3d(tangent);
		reaction.scaleAdd(-effective, action);
		reaction.negate();

		Vector3d centripetal = new Vector3d(bar);
		centripetal.scale(-1./bar.length());
		centripetal.scale(velocity.length()*velocity.length()/radius);
		reaction.add(centripetal);

		return reaction;
	}
*/
	public Vector3d getReaction(Vector3d position, Vector3d velocity, Vector3d action) {
		return getReaction(position, velocity, action, 0.);
	}
	
	public Vector3d getReaction(Vector3d position, Vector3d velocity, Vector3d action, double mass) {
		
		// we are given the total force on the charge, action
		System.out.println("   " );
		System.out.println("action " + action.x + "   " + action.y + "   " + action.z );
		Vector3d radial = new Vector3d(position);
// we calculate the unit radial vector from the center of the arc
		radial.normalize();
		// we set up a new direction that is perpendicular to the radial direction and the normal to the plane.  why is this not normalized?
		Vector3d tangential = new Vector3d();
		tangential.cross(radial, normal);
		// we find the component of the force along the tangential direction
		double effective = action.dot(tangential);
		Vector3d reaction = new Vector3d(tangential);  // we set the reaction to the negative of the force tangential 
		reaction.scaleAdd(-effective, action);     // we then add in the action, which will kill the tangential force LEAVING the radial and normal force
		reaction.negate();  // we then construct a vector which is missin the tangenial force and has the negative of the radial and normal forces

		Vector3d centripetal = new Vector3d(radial);
		centripetal.scale(-mass*velocity.lengthSquared()/radius);
		
		reaction.add(centripetal);  // now we add into this the centripetal force, so we now have a total force whiich has zero tangenaial force, the negative of the orginal radial and normal force, and the centipetal force
		System.out.println("reaction " + reaction.x + "   " + reaction.y + "   " + reaction.z );
		lastReaction.set(reaction);
		// we return reaction, which is above us is added to the original the action.  This will cancel out the original radial and normal forces, leaving the original tangential force and the centripetal forces.  
		// but since we are using mass =0, so we are just left with the original tangential force, and only that force.  
		return reaction;
	}

	public void set_default() {
		setCoefficients(	new Vector3d(0., 0., 0.),
							new Vector3d(1., 0., 0.),
							1.0 );
	}

	public void set( Constraint c ) {
		if( c instanceof SphericalArcConstraint ) {
			setCoefficients(	((SphericalArcConstraint) c).getCenter(),
								((SphericalArcConstraint) c).getNormal(),
								((SphericalArcConstraint) c).getRadius() );
		}	
	}
	
	public Vector3d getLastReaction() {
		return this.lastReaction;
	}

}
