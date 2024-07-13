/* $Id: ChargeByInduction.java,v 1.10 2008/01/05 05:58:05 jbelcher Exp $ */

/**
 * A demonstration implementation of the TFramework.
 * 
 * @author Philip Bailey - Center for Educational Computing Initiatives / MIT
 * @version $Revision: 1.10 $
 */

package tealsim.physics.em;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.media.j3d.BoundingSphere;
import javax.swing.*;
import javax.vecmath.*;

import teal.config.Teal;
import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.math.RectangularPlane;
import teal.render.Rendered;
import teal.render.geometry.Pipe;
import teal.render.j3d.ShapeNode;
import teal.render.scene.TShapeNode;
import teal.render.viewer.*;
import teal.sim.collision.*;
import teal.sim.control.VisualizationControl;
import teal.sim.engine.TEngineControl;
import teal.sim.engine.SimEngine;
import teal.physics.em.SimEM;
import teal.physics.physical.PhysicalObject;
import teal.physics.em.PointCharge;
import teal.sim.spatial.*;
import teal.ui.control.ControlGroup;
import teal.ui.control.PropertyDouble;
import teal.visualization.dlic.DLIC;

import teal.util.TDebug;

public class FaradayIcePailShield extends SimEM implements SelectListener {

    private static final long serialVersionUID = 3257846575882646838L;

    JButton groundButton = null;
    JButton ungroundButton = null;
    JButton zeroCentralChargeButton = null;
    JButton posCentralChargeButton = null;
    JButton negCentralChargeButton = null;
    JButton resetButton = null;
    JButton changeSignCCButton = null;
    PropertyDouble slider1 = null;
    double radius1 = 3.;
    double radius2 = 4.75;
    double radius3 = 7;
    double radius4 = 8.;
    double height = 0.5;
    ControlGroup controls;
    ControlGroup controls1;
    VisualizationControl visGroup;
    
    protected FieldConvolution mDLIC = null;
	SpatialTextLabel lbl,lb2;
    final private int N = 20;
    private double signCentralCharge = -2.;
    private PointCharge[] pointCharges = new PointCharge[2 * N];
    private PointCharge centralCharge = null;
    private double pointChargeRadius = 0.2;
    //	ArrayList outerWalls, innerWalls;

    Rendered ring1, ring2, ring3, ring4;
    Rendered bottomcage, bottomshield, groundcageshield, groundshieldinfinity;
    PhysicalObject cylinder1, cylinder2, cylinder3, cylinder4;

    public FaradayIcePailShield() { 

        super();
        title = "Faraday Ice Pail and Shield ";
        TDebug.setGlobalLevel(0);

        // Building the world.
      
        theEngine.setBoundingArea(new BoundingSphere(new Point3d(), 8));
        theEngine.setDeltaTime(0.25);
        theEngine.setDamping(0.25);
        theEngine.setGravity(new Vector3d(0., 0., 0.));
        theEngine.setAnnihilating(false);
        
        //		theEngine.setShowTime(true);
       

        //mViewer.setNavigationMode(TViewer.ORBIT | TViewer.VP_ZOOM | TViewer.VP_TRANSLATE);
        mDLIC = new FieldConvolution();
        //mDLIC.setSize(new Dimension(1024,1024));
        RectangularPlane rec = new RectangularPlane(new Vector3d(-12., -12., 0.), new Vector3d(-12., 12., 0.),
            new Vector3d(12., 12., 0.));
        mDLIC.setComputePlane(rec);
   // 	mDLIC.setColorMode(Teal.ColorMode_MAGNITUDE);
     
        // Creating components.
        
        //  add labels
        
        Vector3d posFaradayCageLabel  = new Vector3d(-1.6,-2.,0);
		lbl = new SpatialTextLabel(" Ice Pail ", posFaradayCageLabel );
		lbl.setBaseScale(0.8);
		lbl.setPositionOffset(new Vector3d(0.05, 0.0, 0.));
		lbl.setRefDirectionOffset(0.5);
		lbl.setUseDirectionOffset(true);
		addElement(lbl);
		
        Vector3d posShieldLabel  = new Vector3d(-1.6,-6.2,0);
		lb2 = new SpatialTextLabel(" Shield ", posShieldLabel );
		lb2.setBaseScale(0.8);
		lb2.setPositionOffset(new Vector3d(0.05, 0.0, 0.));
		lb2.setRefDirectionOffset(0.5);
		lb2.setUseDirectionOffset(true);
		addElement(lb2);
		
		// add spatial structures
		
        // this is the inner wall of the inner wire mesh
		
        cylinder1 = new PhysicalObject();
        CylindricalWallCollisionController cwcc1 = new CylindricalWallCollisionController(cylinder1);
        cwcc1.setTolerance(0.1);
        cwcc1.setDirection(new Vector3d(0., 0., 1.));
        cwcc1.setRadius(radius1);
        cylinder1.setCollisionController(cwcc1);
        cylinder1.setColliding(true);
        addElement(cylinder1);
        double thickness1 = 0.05;
        ring1 = new Rendered();
        TShapeNode node1 = (TShapeNode) new ShapeNode();
        node1.setGeometry(Pipe.makeGeometry(50, radius1 + thickness1 / 2., thickness1, height));
        node1.setPickable(false);
        node1.setColor(new Color3f(Color.ORANGE));
        //		node1.setTransparency(0.1f);
        ring1.setDirection(new Vector3d(0., 0., 1.));
        ring1.setNode3D(node1);
        addElement(ring1);

        // this is the outer wall of the inner wire mesh
        cylinder2 = new PhysicalObject();
        CylindricalWallCollisionController cwcc2 = new CylindricalWallCollisionController(cylinder2);
        cwcc2.setTolerance(0.1);
        cwcc2.setDirection(new Vector3d(0., 0., 1.));
        cwcc2.setRadius(radius2);
        cylinder2.setCollisionController(cwcc2);
        cylinder2.setColliding(true);
        addElement(cylinder2);
        double thickness2 = 0.05;
        ring2 = new Rendered();
        TShapeNode node2 = (TShapeNode) new ShapeNode();
        node2.setGeometry(Pipe.makeGeometry(50, radius2 - thickness2 / 2., thickness2, height));
        node2.setPickable(false);
        ring2.setDirection(new Vector3d(0., 0., 1.));
        node2.setColor(new Color3f(Color.ORANGE));
        //		node2.setTransparency(0.1f);
        ring2.setNode3D(node2);
        addElement(ring2);

        // this is the inner wall of the shield grid (outer wire mesh)
        
        cylinder3 = new PhysicalObject();
        CylindricalWallCollisionController cwcc3 = new CylindricalWallCollisionController(cylinder3);
        cwcc3.setTolerance(0.1);
        cwcc3.setDirection(new Vector3d(0., 0., 1.));
        cwcc3.setRadius(radius3);
        cylinder3.setCollisionController(cwcc3);
        cylinder3.setColliding(false);
        addElement(cylinder3);
        double thickness3 = .05;
        ring3 = new Rendered();
        TShapeNode node3 = (TShapeNode) new ShapeNode();
        node3.setGeometry(Pipe.makeGeometry(50, radius3 - thickness3 / 2., thickness3, height));
        node3.setPickable(false);
        ring3.setDirection(new Vector3d(0., 0., 1.));
        node3.setColor(new Color3f(Color.ORANGE));
        //		node2.setTransparency(0.1f);
        ring3.setNode3D(node3);
        addElement(ring3);
        
   // this is the outer wall of the shield grid (outer wire mesh)
        
        cylinder4 = new PhysicalObject();
        CylindricalWallCollisionController cwcc4 = new CylindricalWallCollisionController(cylinder4);
        cwcc4.setTolerance(0.1);
        cwcc4.setDirection(new Vector3d(0., 0., 1.));
        cwcc4.setRadius(radius4);
        cylinder4.setCollisionController(cwcc4);
        cylinder4.setColliding(false);
        addElement(cylinder4);
        double thickness4 = 0.05;
        ring4 = new Rendered();
        TShapeNode node4 = (TShapeNode) new ShapeNode();
        node4.setGeometry(Pipe.makeGeometry(50, radius4 - thickness4 / 2., thickness4, height));
        node4.setPickable(false);
        ring4.setDirection(new Vector3d(0., 0., 1.));
        node4.setColor(new Color3f(Color.ORANGE));
      //  node2.setTransparency(0.9f);
        ring4.setNode3D(node4);
        addElement(ring4);
        
 // do bottom of the shield, and the cage, and the grounding conductor between shield and cage and shield and infinity
        
        bottomcage = new Rendered();
        TShapeNode nodebottomcage = (TShapeNode) new ShapeNode();
        nodebottomcage.setGeometry(Pipe.makeGeometry(50, (radius2+radius1)*.5, (radius2-radius1),.1));
        nodebottomcage.setPickable(false);
        nodebottomcage.setColor(new Color3f(Color.ORANGE));
        //nodebottomcage.setTransparency(0.4f);
        bottomcage.setDirection(new Vector3d(0., 0., 1.));
        bottomcage.setPosition(new Vector3d(0., 0., -height/2.));
        bottomcage.setNode3D(nodebottomcage);
        addElement(bottomcage);
        
        bottomshield = new Rendered();
        TShapeNode nodebottomshield = (TShapeNode) new ShapeNode();
        nodebottomshield.setGeometry(Pipe.makeGeometry(50, (radius3+radius4)*.5, (radius4-radius3), .1));
        nodebottomshield.setPickable(false);
        nodebottomshield.setColor(new Color3f(Color.ORANGE));
        bottomshield.setDirection(new Vector3d(0., 0., 1.));
        bottomshield.setPosition(new Vector3d(0., 0., -height/2.));
        bottomshield.setNode3D(nodebottomshield);
        addElement(bottomshield);
        
        groundcageshield = new Rendered();
        TShapeNode nodegroundcageshield = (TShapeNode) new ShapeNode();
        nodegroundcageshield.setGeometry(Pipe.makeGeometry(50, (radius4+radius1)*.5, (radius4-radius1), .1));
        nodegroundcageshield.setPickable(false);
        nodegroundcageshield.setColor(new Color3f(Color.ORANGE));
        nodegroundcageshield.setTransparency(0.4f);
        groundcageshield.setDirection(new Vector3d(0., 0., 1.));
        groundcageshield.setPosition(new Vector3d(0., 0., -height/2.));
        groundcageshield.setNode3D(nodegroundcageshield);
        groundcageshield.setDrawn(false);
        addElement(groundcageshield);
        
        groundshieldinfinity = new Rendered();
        TShapeNode nodegroundshieldinfinity = (TShapeNode) new ShapeNode();
        nodegroundshieldinfinity.setGeometry(Pipe.makeGeometry(50, radius4+4., 8.,  .1));
        nodegroundshieldinfinity.setPickable(false);
        nodegroundshieldinfinity.setColor(new Color3f(Color.ORANGE));
        nodegroundshieldinfinity.setTransparency(0.4f);
        groundshieldinfinity.setDirection(new Vector3d(0., 0., 1.));
        groundshieldinfinity.setPosition(new Vector3d(0., 0., height/2.));
        groundshieldinfinity.setNode3D(nodegroundshieldinfinity);
        groundshieldinfinity.setDrawn(true);
        addElement(groundshieldinfinity);
        
        
        // -> Point Charges
        // -> Point Charges
        int pos = N;
        int neg = N;
        double charge = 1.;
        for (int i = 0; i < 2 * N; i++) {
            pointCharges[i] = new PointCharge();
            pointCharges[i].setRadius(pointChargeRadius);
            pointCharges[i].setMass(1.0);
            charge = - charge;
    //        double charge = Math.random() > 0.5 ? 1. : -1.;
      //      if (charge > 0.) {
    //            if (pos == 0)
    //                charge = -1.;
   //             else pos--;
   //         }
   //         if (charge < 0.) {
  //              if (neg == 0)
  //                  charge = 1.;
  //              else neg--;
  //          }
            
            pointCharges[i].setCharge(charge);

            pointCharges[i].setGeneratingP(false);

            pointCharges[i].setID("pointCharge" + i);
            pointCharges[i].setPickable(false);
            pointCharges[i].setColliding(true);
            SphereCollisionController sccx = new SphereCollisionController(pointCharges[i]);
            sccx.setRadius(pointChargeRadius);
            sccx.setTolerance(0.5);
            //			sccx.setMode(SphereCollisionController.WALL_SPHERE );
            pointCharges[i].setCollisionController(sccx);
            addElement(pointCharges[i]);
        }
        resetPointCharges();

        centralCharge = new PointCharge();
        centralCharge.setPosition(new Vector3d(0.,0,0.));
        centralCharge.setRadius(.4);
        centralCharge.setPauliDistance(pointChargeRadius * 2.);
        centralCharge.setMass(1.0);
        centralCharge.setCharge(0.);
        centralCharge.setID("centralCharge");
        centralCharge.setPickable(false);
        centralCharge.setMoveable(false);
        centralCharge.setColliding(false);
        addElement(centralCharge);

        slider1 = new PropertyDouble();
        slider1.setPrecision(1);
        slider1.setMinimum(-200.);
        slider1.setMaximum(200.);
        //slider1.setBounds(40, 515, 415, 50);
        slider1.setPaintTicks(true);
        slider1.addRoute(centralCharge, "charge");
        double centralChargeCharge = 0.;  // signCentralCharge*N;
        slider1.setValue(centralChargeCharge);
        slider1.setText("Charge Producer");
        slider1.setBorder(null);

        controls = new ControlGroup();
        controls.setText("Charge Producer Panel");
  //      controls.add(slider1);
        

        changeSignCCButton = new JButton(new TealAction("Change Sign Charge Producer", "Change Sign Charge Producer", this));
        changeSignCCButton.setFont(changeSignCCButton.getFont().deriveFont(Font.BOLD));
        changeSignCCButton.setBounds(40, 600, 195, 24);
 //       controls.add(changeSignCCButton);
        
        
        zeroCentralChargeButton = new JButton(new TealAction("Zero Charge Producer", "Zero Charge Producer", this));
        zeroCentralChargeButton.setFont(zeroCentralChargeButton.getFont().deriveFont(Font.BOLD));
        zeroCentralChargeButton.setBounds(40, 600, 195, 24);
        controls.add(zeroCentralChargeButton);
        
        posCentralChargeButton = new JButton(new TealAction("Make Charge Producer Positive", "Make Charge Producer Positive", this));
        posCentralChargeButton.setFont(posCentralChargeButton.getFont().deriveFont(Font.BOLD));
        posCentralChargeButton.setBounds(40, 600, 195, 24);
        controls.add(posCentralChargeButton);
        
        negCentralChargeButton = new JButton(new TealAction("Make Charge Producer Negative", "Make Charge Producer Negative", this));
        negCentralChargeButton.setFont(negCentralChargeButton.getFont().deriveFont(Font.BOLD));
        negCentralChargeButton.setBounds(40, 600, 195, 24);
        controls.add(negCentralChargeButton);

        
        controls1 = new ControlGroup();
        controls1.setText("Ground/Ungroud Panel");

        groundButton = new JButton(new TealAction("Connect Cage To Shield", "Connect Cage To Shield", this));
        groundButton.setFont(groundButton.getFont().deriveFont(Font.BOLD));
        groundButton.setBounds(40, 570, 195, 24);
        controls1.add(groundButton);

        ungroundButton = new JButton(new TealAction("Disconnect Cage From Shield", "Disconnect Cage From Shield", this));
        ungroundButton.setFont(ungroundButton.getFont().deriveFont(Font.BOLD));
        ungroundButton.setBounds(40, 600, 195, 24);
        controls1.add(ungroundButton);


 
        resetButton = new JButton(new TealAction("Reset", "Reset", this));
        resetButton.setFont(resetButton.getFont().deriveFont(Font.BOLD));
        resetButton.setBounds(40, 600, 195, 24);
      //  controls.add(resetButton);
        
        visGroup = new VisualizationControl();
        visGroup.setFieldConvolution(mDLIC);
        visGroup.setConvolutionModes(DLIC.DLIC_FLAG_E | DLIC.DLIC_FLAG_EP);
        
        addElement(controls);
        addElement(controls1);
        addElement(visGroup);
     
        addSelectListener(this);
        mSEC.init();
        mSEC.start();
        mViewer.doStatus(0);
        resetCamera();
        reset();
        addActions();
    }

    
    void addActions() {
        TealAction tb = new TealAction("Faraday Ice Pail & Shield", this);
        addAction("Help", tb);
        TealAction ta = new TealAction("Execution & View", this);
        addAction("Help", ta);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareToIgnoreCase("Faraday Ice Pail & Shield") == 0) {
            if ((mFramework != null) && (mFramework instanceof TFramework)) {
                ((TFramework)mFramework).openBrowser("help/FaradayIcePailShield.html");
            }
        }
        if (e.getActionCommand().compareToIgnoreCase("Execution & View") == 0) {
            	if((mFramework != null) && (mFramework instanceof TFramework)) {
            		((TFramework)mFramework).openBrowser("help/executionView.html");
            	}
            }
        if (e.getActionCommand().compareToIgnoreCase("Connect Cage To Shield") == 0) {
            ground();}
        if (e.getActionCommand().compareToIgnoreCase("Disconnect Cage From Shield") == 0) {
            unground();}
        if (e.getActionCommand().compareToIgnoreCase("Zero Charge Producer") == 0) {
            zeroCentralCharge();}
        if (e.getActionCommand().compareToIgnoreCase("Change Sign Charge Producer") == 0) {
            changeSignCentralCharge();}
        if (e.getActionCommand().compareToIgnoreCase("Make Charge Producer Negative") == 0) {
            centralCharge.setCharge(-8.*N);}
        if (e.getActionCommand().compareToIgnoreCase("Make Charge Producer Positive") == 0) {
            centralCharge.setCharge(8.*N);}
        if (e.getActionCommand().compareToIgnoreCase("Reset") == 0) {
            reset();}
         else {
          super.actionPerformed(e);
        }
    }
        

    private void ground() {
 
    //    ring2.setDrawn(false);
        cylinder2.setColliding(false);
        
    //    ring3.setDrawn(false);
        cylinder3.setColliding(false);
        groundcageshield.setDrawn(true);
   //     cylinder4.setColliding(true);
    }

    private void unground() {

        int state = mSEC.getSimState();
        if (state == TEngineControl.RUNNING) {
            mSEC.stop();
        }

        ring2.setDrawn(true);
        cylinder2.setColliding(true);
        ring3.setDrawn(true);
        cylinder3.setColliding(true);
        
        groundcageshield.setDrawn(false);

        if (state == TEngineControl.RUNNING) {
            mSEC.start();
        }
        
    }
    
    private void zeroCentralCharge() {
  
  //      for (int i = 0; i < N; i++) {
  //      	int j = i+2*N;
  //          pointCharges[j].setCharge(0.);       
  //          pointCharges[j].setDrawn(false);      
  //      }
        
   
        slider1.setValue(0.);
        centralCharge.setCharge(0.);
    }


    public void propertyChange(PropertyChangeEvent pce) {
        super.propertyChange(pce);
    }

    public void changeSignCentralCharge() {
    	signCentralCharge = -signCentralCharge;
    //    resetPointCharges();
   //     mSEC.stop();
        //resetCamera();
  //      for (int i = 0; i < N; i++) {
 //       	int j = i+2*N;
  //          pointCharges[j].setCharge(-signCentralCharge);       
 //          pointCharges[j].setDrawn(true);      
  //      }
        centralCharge.setCharge(signCentralCharge*N);
  //      cylinder4.setColliding(false);
  //      unground();
    }
    
    public void reset() {
        resetPointCharges();
      //  mSEC.stop();
        //resetCamera();
        centralCharge.setCharge(0.);
        unground();
    }

    private void resetPointCharges() {
        Point3d[] positions = new Point3d[2 * N];
        Point3d position = null;
        double r1 = radius1 + pointChargeRadius * 1.9;
        double r2 = radius2  - pointChargeRadius * 1.9;
        for (int i = 0; i < 2 * N; i++) {
            double ang = 2. * Math.PI * (double) i / (2. * (double) N);
            boolean distinct = true;
            do {
                double rad = (r2 - r1) * Math.random() + r1;
                //				double ang = 2.*Math.PI*Math.random();
                position = new Point3d(rad * Math.cos(ang), rad * Math.sin(ang), 0.);
                if (i > 0) {
                    if (position.distance(positions[i - 1]) < pointChargeRadius * 1.1) {
                        System.out.println("i: " + i + ", Touching: " + position + " and " + positions[i - 1]);
                        distinct = false;
                    }
                }
            } while (!distinct);
            positions[i] = position;
            pointCharges[i].setPosition(new Vector3d(position), true);
            pointCharges[i].setVelocity(new Vector3d());
        }
    }


    public void resetCamera() {
        mViewer.setLookAt(new Point3d(0.0, 0.0, 1), new Point3d(), new Vector3d(0., 1., 0.));

    }

    public synchronized void dispose() {
        super.dispose();
    }

    public void processSelection(SelectEvent se) {
        TDebug.println(0, se.getSource() + " select state = " + se.getStatus());
    }

    

}
