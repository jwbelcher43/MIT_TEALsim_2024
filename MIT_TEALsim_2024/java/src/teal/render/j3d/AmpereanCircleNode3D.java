/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: WireNode.java,v 1.15 2007/07/16 22:04:56 pbailey Exp $ 
 * 
 */

package teal.render.j3d;

import java.awt.Color;

import javax.media.j3d.Appearance;
import javax.media.j3d.Geometry;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.geometry.Cone;


import teal.render.scene.TIntegralNode3D;

import teal.render.geometry.Pipe;
import teal.sim.spatial.FieldVector;




/**
 * This is the Node3D class for the Amperean Circle line integral
 */

public class AmpereanCircleNode3D extends Node3D implements TIntegralNode3D{
	
    /** The field tangent vectors on the contour. */
    TransformGroup[] Tangents;
    Shape3D[] cones;
    Shape3D[] lines;
    Vector3f[] locationTangents;
    Vector3f[] locationTangents1;
    double[] angleTangents;
    Appearance[] appTangents;
	float transparency = 0.0f;
	
	TransformGroup CircleWithTangents = new TransformGroup();
//	TransformGroup arrow = new TransformGroup();
	
	public AmpereanCircleNode3D(double radius, double thickness, double height, double conedim) {

		super();
        float fheight2;
        fheight2 = (float) height;
        float fradius;
        fradius = (float) radius;
        fheight2 = fheight2/2.f;
		// Make the ring
		Shape3D ring = new Shape3D();
		initShape(ring);
		ring.setGeometry(Pipe.makeGeometry(24,radius,thickness,height).getIndexedGeometryArray());
		Appearance appTop = Node3D.makeAppearance(new Color3f(new Color(255,255,255)),.8f,0.5f,false);
		TransparencyAttributes taTop = new TransparencyAttributes(TransparencyAttributes.NICEST,0.5f); //app.getTransparencyAttributes();
		appTop.setTransparencyAttributes(taTop);
		ring.setAppearance(appTop);
		TransformGroup TopTG=new TransformGroup();
		Transform3D tranTop = new Transform3D();
		tranTop.set( new Vector3f(fheight2,0.f,0.f));
		tranTop.setRotation(new AxisAngle4d(0.,0.,1.,0.));
		TopTG.addChild(ring);
		TopTG.setTransform(tranTop);
		CircleWithTangents.addChild(TopTG);
		
	
		
		float conedimf = 0.f;
		conedimf = (float) conedim;
		Cone fatcone = new Cone(1.f*conedimf,1.f*conedimf);
		Geometry stem  = teal.render.geometry.Cylinder.makeGeometry(20, conedim/4., 2.*conedim, 0.0).getIndexedGeometryArray(true);
		appTangents = new Appearance[10];
		Color3f colorTangents = new Color3f(Color.gray);
		Tangents = new TransformGroup[10];
		lines = new Shape3D[10];
		cones = new Shape3D[10];
		locationTangents=new Vector3f[10];
		angleTangents = new double[10];
		locationTangents1=new Vector3f[10];



		for (int i = 1; i <= 8; i++) {
			appTangents[i] = Node3D.makeAppearance(colorTangents,.8f,transparency,false);
			lines[i] = new Shape3D();
			initShape(lines[i]);
			lines[i].setGeometry(stem);
			lines[i].setAppearance(appTangents[i]);
			cones[i] = new Shape3D();
			initShape(cones[i]);
			cones[i].setGeometry(fatcone.getShape(Cone.BODY).getGeometry());
			cones[i].addGeometry(fatcone.getShape(Cone.CAP).getGeometry());
			cones[i].setAppearance(appTangents[i]);
			TransformGroup translated_cone=new TransformGroup();
			Transform3D tran = new Transform3D(); 
			float cone_displacement = 0.f;
			cone_displacement = (float)(conedim);
			tran.set( new Vector3f(0.f,cone_displacement,0.f));
			translated_cone.addChild(cones[i]);
			translated_cone.setTransform(tran);
			Tangents[i] = new TransformGroup();
			Tangents[i].addChild(lines[i]);
			Tangents[i].addChild(translated_cone);
			Transform3D tranTangents = new Transform3D(); 
			Transform3D tranTangents1 = new Transform3D(); 
			float xcomp = (float)(radius*Math.cos((i-1)*Math.PI/4.));
			float zcomp = (float)(radius*Math.sin((i-1)*Math.PI/4.));
			locationTangents[i]= new Vector3f(xcomp,0.f,zcomp);
			locationTangents1[i]= new Vector3f();
			angleTangents[i]= -(i-1)*Math.PI/4 - .5*Math.PI;;
			tranTangents.set( locationTangents[i]);
			tranTangents.setRotation(new AxisAngle4d(0.,0.,1.,0.5*Math.PI));
			tranTangents1.setRotation(new AxisAngle4d(1.,0.,0.,angleTangents[i]));
			tranTangents.mul(tranTangents1);
		//	tranTangents.setRotation(new AxisAngle4d(0.,1.,0.,angleTangents[i]));
			Tangents[i].setTransform(tranTangents);
			CircleWithTangents.addChild(Tangents[i]);
		}
		
		CircleWithTangents.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		CircleWithTangents.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 

		Transform3D CircleTangentTG = new Transform3D(); 
		//CircleTangentTG.set(new Vector3f(0.f,-0.0f,0.f));
		CircleTangentTG.setRotation(new AxisAngle4d(1.,0.,0.,0.5*Math.PI));
//		CircleTangentTG.setScale(1.);
		CircleWithTangents.setTransform(CircleTangentTG);
		for (int i = 1; i <= 8; i++) {
		//	System.out.println("before " + locationTangents[i]+ " angle " + angleTangents[i]);
			CircleTangentTG.transform(locationTangents[i],locationTangents1[i]);
		//	System.out.println("after " + locationTangents1[i]);
		}
		mContents.addChild(CircleWithTangents);	
	}
	
	public TransformGroup getTransformGroupVectorI(int i) {
		TransformGroup t = new TransformGroup();
		t = Tangents[i];
		return t;
	}
	
	public void setColorTangentI(int i, Color3f c ) {
		appTangents[i] = Node3D.makeAppearance(c,.8f,transparency,false);
		lines[i].setAppearance(appTangents[i]);
		cones[i].setAppearance(appTangents[i]);
		
	}
	
	public Vector3d getTangentVectorI(int i ) {
		Vector3f t = new Vector3f();
		Vector3d g = new Vector3d();
		t = locationTangents1[i];
		g.x = t.x;
		g.y = t.y;
		g.z = t.z;
		return g;
	}
	/**
	 * This is called from InfiniteWire when the current changes.  Scales the arrow indicator to reflect the
	 * change in current.
	 * 
	 * @param current
	 */
	public void setArrowDirection(double current) {
		Transform3D t = new Transform3D();
		t.setScale(current);
		
		CircleWithTangents.setTransform(t);
		//txt.setString("Current: " + current + "\n linebreak?");
		
	}
	public static void initShape(Shape3D shape){
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_READ);
		shape.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		
	}
}
