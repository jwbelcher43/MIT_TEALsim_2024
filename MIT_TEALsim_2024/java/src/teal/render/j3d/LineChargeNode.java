/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: LineChargeNode.java,v 1.1 2008/12/23 18:56:07 jbelcher Exp $ 
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
import javax.vecmath.Color3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Quat4d;
import javax.vecmath.AxisAngle4d;

import teal.render.geometry.Cylinder;
import teal.render.geometry.Pipe;

import com.sun.j3d.utils.geometry.Cone;
import com.sun.j3d.utils.geometry.Text2D;

/**
 * This is the node for the Infinite Line of Charge class.  
 */
public class LineChargeNode extends Node3D {
	TransformGroup arrow = new TransformGroup();
	Text2D txt;
	
	public LineChargeNode(double len, double rad) {
//		super(2);
//		
//		setGeometry(0,Cylinder.makeGeometry(24,rad,len).getIndexedGeometryArray(),0);
		super();
		Shape3D wire = new Shape3D();
		Shape3D pipe = new Shape3D();
		initShape(pipe);
		initShape(wire);
		wire.setGeometry(Cylinder.makeGeometry(24,1.5,len).getIndexedGeometryArray());
	//	pipe.setGeometry(Pipe.makeGeometry(50, 1., .5, 2.));
		Appearance app = Node3D.makeAppearance(new Color3f(new Color(154,105,0)),.8f,0.5f,false);
		TransparencyAttributes ta = new TransparencyAttributes(TransparencyAttributes.NICEST,0.5f); //app.getTransparencyAttributes();
		ta.setTransparency(0.8f);
		app.setTransparencyAttributes(ta);
		wire.setAppearance(app);
		Quat4d rotater = new Quat4d(0.,1.0,1.,0.);
		TransformGroup rotated =new TransformGroup();
		Transform3D tran = new Transform3D(); 
		//tran.set( new Vector3f(0.f,0.95f,0.f));
		tran.set( rotater);
		rotated.addChild(wire);
		rotated.setTransform(tran);
		
		mContents.addChild(rotated);
		

		Shape3D line=new Shape3D();
		initShape(line);
		//line.setGeometry(sLine);
		Geometry stem  = teal.render.geometry.Cylinder.makeGeometry(20, 0.05, 1, 0.5).getIndexedGeometryArray(true);
		line.setGeometry(stem);
		
		Cone fatcone = new Cone(0.2f,0.25f);
		Shape3D cone = new Shape3D();
		initShape(cone);
		cone.setGeometry(fatcone.getShape(Cone.BODY).getGeometry());
		cone.addGeometry(fatcone.getShape(Cone.CAP).getGeometry());
		


		arrow.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		arrow.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		//arrow.addChild(line);
		//arrow.addChild(translated_cone);
		Appearance arrowAppearance = Node3D.makeAppearance(new Color3f(new Color(154,105,0)),0.f,0.f,false);
		line.setAppearance(arrowAppearance);
		cone.setAppearance(arrowAppearance);
		arrow.addChild(line);
	//	arrow.addChild(rotated);

		Transform3D tran2 = new Transform3D(); 
		//tran2.set(new Vector3f(0.f,-0.5f,0.f));
		tran2.setScale(4.);
		arrow.setTransform(tran2);
	//	mContents.addChild(arrow);
		
		
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
		
		arrow.setTransform(t);
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
