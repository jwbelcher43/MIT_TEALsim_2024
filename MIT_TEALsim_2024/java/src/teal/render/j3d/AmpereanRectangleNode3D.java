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

import teal.render.geometry.Cylinder;
import teal.sim.spatial.FieldVector;




/**
 * This is the Node3D class for the Amperean Rectangle line integral
 */

public class AmpereanRectangleNode3D extends Node3D implements TIntegralNode3D{
	
    /** The field tangent vectors on the contour. */
    TransformGroup[] Tangents;
    Shape3D[] cones;
    Shape3D[] lines;
    Vector3f[] locationTangents;
    double[] angleTangents;
    Appearance[] appTangents;
	float transparency = 0.0f;
	
	TransformGroup RectangleWithTangents = new TransformGroup();
//	TransformGroup arrow = new TransformGroup();
	
	public AmpereanRectangleNode3D(double height, double width, double radiusline) {

		super();
        float fheight2;
        fheight2 = (float) height;
        fheight2 = fheight2/2.f;
        float fwidth2;
        fwidth2 = (float) width;
        fwidth2 = fwidth2/2.f;
		// Make the top segment of the rectangle 
		Shape3D topSegment = new Shape3D();
		initShape(topSegment);
		topSegment.setGeometry(Cylinder.makeGeometry(24,radiusline,width).getIndexedGeometryArray());
		Appearance appTop = Node3D.makeAppearance(new Color3f(new Color(255,255,255)),.8f,0.5f,false);
		TransparencyAttributes taTop = new TransparencyAttributes(TransparencyAttributes.NICEST,0.5f); //app.getTransparencyAttributes();
		appTop.setTransparencyAttributes(taTop);
		topSegment.setAppearance(appTop);
		TransformGroup TopTG=new TransformGroup();
		Transform3D tranTop = new Transform3D();
		tranTop.set( new Vector3f(fheight2,0.f,0.f));
		tranTop.setRotation(new AxisAngle4d(0.,0.,1.,0.));
		TopTG.addChild(topSegment);
		TopTG.setTransform(tranTop);
		RectangleWithTangents.addChild(TopTG);
		
		// Make the bottom segment of the rectangle 
		Shape3D bottomSegment = new Shape3D();
		initShape(bottomSegment);
		bottomSegment.setGeometry(Cylinder.makeGeometry(24,radiusline,width).getIndexedGeometryArray());
		Appearance appBottom = Node3D.makeAppearance(new Color3f(new Color(255,255,255)),.8f,0.5f,false);
		TransparencyAttributes taBottom = new TransparencyAttributes(TransparencyAttributes.NICEST,0.5f); //app.getTransparencyAttributes();
		appBottom.setTransparencyAttributes(taBottom);
		bottomSegment.setAppearance(appBottom);
		TransformGroup BottomTG = new TransformGroup();
		Transform3D tranBottom = new Transform3D();
		tranBottom.set( new Vector3f(-fheight2,0.f,0.f));
		tranBottom.setRotation(new AxisAngle4d(0.,0.,1.,0.));
		BottomTG.addChild(bottomSegment);
		BottomTG.setTransform(tranBottom);
		RectangleWithTangents.addChild(BottomTG);
		
		// Make the right segment of the rectangle 
		Shape3D rightSegment = new Shape3D();
		initShape(rightSegment);
		rightSegment.setGeometry(Cylinder.makeGeometry(24,radiusline,height).getIndexedGeometryArray());
		Appearance appRight = Node3D.makeAppearance(new Color3f(new Color(255,255,255)),.8f,0.5f,false);
		TransparencyAttributes taRight = new TransparencyAttributes(TransparencyAttributes.NICEST,0.5f); //app.getTransparencyAttributes();
		appRight.setTransparencyAttributes(taRight);
		rightSegment.setAppearance(appRight);
		TransformGroup RightTG=new TransformGroup();
		Transform3D tranRight = new Transform3D();
		tranRight.set( new Vector3f(0.f,fwidth2,0.f));
		tranRight.setRotation(new AxisAngle4d(0.,0.,1.,.5*Math.PI));
		RightTG.addChild(rightSegment);
		RightTG.setTransform(tranRight);
		RectangleWithTangents.addChild(RightTG);
		
		// Make the right segment of the rectangle 
		Shape3D leftSegment = new Shape3D();
		initShape(leftSegment);
		leftSegment.setGeometry(Cylinder.makeGeometry(24,radiusline,height).getIndexedGeometryArray());
		Appearance appLeft = Node3D.makeAppearance(new Color3f(new Color(255,255,255)),.8f,0.5f,false);
		TransparencyAttributes taLeft = new TransparencyAttributes(TransparencyAttributes.NICEST,0.5f); //app.getTransparencyAttributes();
		appLeft.setTransparencyAttributes(taLeft);
		leftSegment.setAppearance(appLeft);
		TransformGroup LeftTG=new TransformGroup();
		Transform3D tranLeft = new Transform3D();
		tranLeft.set( new Vector3f(0.f,-fwidth2,0.f));
		tranLeft.setRotation(new AxisAngle4d(0.,0.,1.,.5*Math.PI));
		LeftTG.addChild(leftSegment);
		LeftTG.setTransform(tranLeft);
		RectangleWithTangents.addChild(LeftTG);
		
		float conedim = 0.f;
		conedim = (float) radiusline;
		Cone fatcone = new Cone(7.f*conedim,7.f*conedim);
		Geometry stem  = teal.render.geometry.Cylinder.makeGeometry(20, radiusline*1.3, height/3., 0.0).getIndexedGeometryArray(true);
		appTangents = new Appearance[10];
		Color3f colorTangents = new Color3f(Color.gray);
		Tangents = new TransformGroup[10];
		lines = new Shape3D[10];
		cones = new Shape3D[10];

		angleTangents = new double[10];
		locationTangents = new Vector3f[10];
		locationTangents[1]= new Vector3f(0.f,fwidth2,0.f);
		appTangents[1] = Node3D.makeAppearance(colorTangents,.8f,transparency,false);
		angleTangents[1]=Math.PI/2.;
		locationTangents[2] = new Vector3f(0.f,-fwidth2,0.f);
		appTangents[2] = Node3D.makeAppearance(colorTangents,.8f,transparency,false);
		angleTangents[2]=1.5*Math.PI;
		locationTangents[3]=new Vector3f(fheight2,-fwidth2*0.666f,0.f);
		appTangents[3] = Node3D.makeAppearance(colorTangents,.8f,transparency,false);
		angleTangents[3]=0.;
		locationTangents[4]=new Vector3f(fheight2,0.f,0.f);
		appTangents[4] = Node3D.makeAppearance(colorTangents,.8f,transparency,false);
		angleTangents[4]=0.;
		locationTangents[5]=new Vector3f(fheight2,fwidth2*0.666f,0.f);
		appTangents[5] = Node3D.makeAppearance(colorTangents,.8f,transparency,false);
		angleTangents[5]=0.;
		locationTangents[6]=new Vector3f(-fheight2,-fwidth2*0.666f,0.f);
		appTangents[6] = Node3D.makeAppearance(colorTangents,.8f,transparency,false);
		angleTangents[6]=Math.PI;
		locationTangents[7]=new Vector3f(-fheight2,0.f,0.f);
		appTangents[7] = Node3D.makeAppearance(colorTangents,.8f,transparency,false);
		angleTangents[7]=Math.PI;
		locationTangents[8]=new Vector3f(-fheight2,fwidth2*0.666f,0.f);
		appTangents[8] = Node3D.makeAppearance(colorTangents,.8f,transparency,false);
		angleTangents[8]=Math.PI;
		for (int i = 1; i <= 8; i++) {
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
			cone_displacement = (float)(height/6.);
			tran.set( new Vector3f(0.f,cone_displacement,0.f));
			translated_cone.addChild(cones[i]);
			translated_cone.setTransform(tran);
			Tangents[i] = new TransformGroup();
			Tangents[i].addChild(lines[i]);
			Tangents[i].addChild(translated_cone);
			Transform3D tranTangents = new Transform3D(); 
			tranTangents.set( locationTangents[i]);
			tranTangents.setRotation(new AxisAngle4d(0.,0.,1.,angleTangents[i]));
			Tangents[i].setTransform(tranTangents);
			RectangleWithTangents.addChild(Tangents[i]);
		}
		
		RectangleWithTangents.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		RectangleWithTangents.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 

		Transform3D RectangleTangentsTG = new Transform3D(); 
		//RectangleTangentsTG.set(new Vector3f(0.f,-0.0f,0.f));
		RectangleTangentsTG.setScale(1.);
		RectangleWithTangents.setTransform(RectangleTangentsTG);
		mContents.addChild(RectangleWithTangents);	
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
		t = locationTangents[i];
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
		
		RectangleWithTangents.setTransform(t);
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
