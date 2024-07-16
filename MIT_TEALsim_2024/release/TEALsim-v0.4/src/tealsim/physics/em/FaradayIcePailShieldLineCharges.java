/* $Id: FaradayIcePailShieldLineCharges.java,v 1.1 2008/12/23 20:02:28 jbelcher Exp $ */

/**
 * A demonstration implementation of the TFramework.
 * 
 * @author Philip Bailey - Center for Educational Computing Initiatives / MIT
 * @version $Revision: 1.1 $
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
import teal.physics.em.InfiniteLineCharge;
import teal.sim.spatial.*;
import teal.ui.UIPanel;
import teal.ui.control.ControlGroup;
import teal.ui.control.PropertyDouble;
import teal.visualization.dlic.DLIC;

import teal.util.TDebug;

public class FaradayIcePailShieldLineCharges extends SimEM implements SelectListener {

    private static final long serialVersionUID = 3257846575882646838L;
    boolean setCentralCharge= false;
    boolean setGroundState = false;
	UIPanel GaussianControls;
	ControlGroup gaussianControl;
	ControlGroup groundShieldControl;
	ButtonGroup optionsGroup;
	ButtonGroup optionsGroup1;
	/** Radio button for zero charge */
	JRadioButton rad1;
	/** Radio button for positive charge*/
    JRadioButton rad2;       
	/** Radio button for negative charge*/
    JRadioButton rad3;    
	/** Radio button for ground outer shield */
	JRadioButton rad4;
	/** Radio button for not grounding outer shield*/
    JRadioButton rad5;       
    JButton runButton = null;
    JButton pauseButton = null;
    JButton groundButton = null;
    JButton ungroundButton = null;
    JButton zeroCentralChargeButton = null;
    JButton resetButton = null;
    JButton changeSignCCButton = null;
    PropertyDouble slider1 = null;
    double radius1 = 1.5;
    double radius2 = 3.0;
    double radius3 = 4.;
    double radius4 = 5.5;
    double radius5 = 7.5;
    double height = 1.;
    JButton but6 = null;
    ControlGroup params1;
    ControlGroup controls;
    VisualizationControl visGroup;
	JTextArea messages;
    

	SpatialTextLabel lbl,lb2,lb3;
    final private int N = 8;
    private double signCentralCharge = 0.;
    private InfiniteLineCharge[] pointCharges = new InfiniteLineCharge[8 * N];
    Point3d[] positions = new Point3d[8 * N];
    private InfiniteLineCharge centralCharge = null;
    private double pointChargeRadius = 0.2;
    //	ArrayList outerWalls, innerWalls;

    Rendered ring1, ring2, ring3, ring4, ring5;
    Rendered bottomcage, bottomshield, groundcageshield, groundshieldinfinity;
    PhysicalObject cylinder1, cylinder2, cylinder3, cylinder4, cylinder5;

    public FaradayIcePailShieldLineCharges() { 

        super();
        title = "Faraday Ice Pail and Shield ";
        TDebug.setGlobalLevel(0);
        mSEC.rebuildPanel(0);

        // Building the world.
      
        theEngine.setBoundingArea(new BoundingSphere(new Point3d(), 8));
        theEngine.setDeltaTime(0.03);
        theEngine.setDamping(0.8);
        theEngine.setGravity(new Vector3d(0., 0., 0.));
        theEngine.setAnnihilating(true);
  
        // Creating components.
        
        //  add labels
		double zoffset = 3.;
        Vector3d posFaradayCageLabel  = new Vector3d(-2.,-1.8,zoffset);
		lbl = new SpatialTextLabel(" ICE PAIL ", posFaradayCageLabel );
		lbl.setBaseScale(1.);
		addElement(lbl);
		
        Vector3d posShieldLabel  = new Vector3d(-2.,-4.,zoffset);
		lb2 = new SpatialTextLabel(" SHIELD ", posShieldLabel );
		lb2.setBaseScale(1.);
		addElement(lb2);
		
		Vector3d posInfinity = new Vector3d(-1.,5.5,zoffset);
	    lb3= new SpatialTextLabel(" INFINITY ", posInfinity );
		lb3.setBaseScale(0.7);
        lb3.setDrawn(false);
		addElement(lb3);
		
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
        cylinder3.setColliding(true);
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
        cylinder4.setColliding(true);
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
        
   // this is the wall at infinity
        
        cylinder5 = new PhysicalObject();
        CylindricalWallCollisionController cwcc5 = new CylindricalWallCollisionController(cylinder5);
        cwcc5.setTolerance(0.1);
        cwcc5.setDirection(new Vector3d(0., 0., 1.));
        cwcc5.setRadius(radius5);
        cylinder5.setCollisionController(cwcc5);
        cylinder5.setColliding(true);
        addElement(cylinder5);
        double thickness5 = .05;
        ring5 = new Rendered();
        TShapeNode node5 = (TShapeNode) new ShapeNode();
        node5.setGeometry(Pipe.makeGeometry(50, radius5 + thickness5 / 2., thickness5, height));
        node5.setPickable(false);

        ring5.setDirection(new Vector3d(0., 0., 1.));
        node5.setColor(new Color3f(Color.black));
      //  node2.setTransparency(0.9f);
        ring5.setNode3D(node5);
        ring5.setDrawn(false);
        addElement(ring5);
        
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
        nodegroundshieldinfinity.setGeometry(Pipe.makeGeometry(50, (radius4+radius5)*.5, radius5-radius4,  .1));
        nodegroundshieldinfinity.setPickable(false);
        nodegroundshieldinfinity.setColor(new Color3f(Color.ORANGE));
        nodegroundshieldinfinity.setTransparency(0.5f);
        groundshieldinfinity.setDirection(new Vector3d(0., 0., 1.));
        groundshieldinfinity.setPosition(new Vector3d(0., 0., height/2.));
        groundshieldinfinity.setNode3D(nodegroundshieldinfinity);
        groundshieldinfinity.setDrawn(true);
        groundshieldinfinity.setDrawn(false);
        addElement(groundshieldinfinity);
        
        
  
        resetPointCharges();

        centralCharge = new InfiniteLineCharge();
        centralCharge.setPosition(new Vector3d(0.,0,0.));
        centralCharge.setRadius(.4);
        centralCharge.setMass(1.0);
        centralCharge.setCharge(0.);
        centralCharge.setID("centralCharge");
        centralCharge.setPickable(false);
        centralCharge.setMoveable(false);
        centralCharge.setColliding(false);
        centralCharge.setDrawn(true);
        addElement(centralCharge);
        
        GridBagLayout gbl =new GridBagLayout();
        GridBagConstraints con = new GridBagConstraints();
        con.gridwidth = GridBagConstraints.REMAINDER; //end row
        
       GaussianControls = new UIPanel();
        GaussianControls.setLayout(gbl);
        // set radio buttons for choice of gaussian surface     
        UIPanel options = new UIPanel();
        options.setBorder(BorderFactory.createLineBorder(Color.black));
        options.setLayout(new GridLayout(2,1));
        optionsGroup = new ButtonGroup();
        // set radio buttons for scale of E     
        UIPanel options1 = new UIPanel();
        options1.setBorder(BorderFactory.createLineBorder(Color.black));
        options1.setLayout(new GridLayout(2,1));
        optionsGroup1 = new ButtonGroup();
        rad1 = new JRadioButton("Central charge zero");        
        rad2 = new JRadioButton("Central charge positive");    
        rad3 = new JRadioButton("Central charge negative"); 
        rad4 = new JRadioButton("Ground shield to infinity");    
        rad5 = new JRadioButton("Do not ground shield to infinity"); 

        //rad1.setSelected(true);

        rad1.addActionListener(this);
        rad2.addActionListener(this);
        rad3.addActionListener(this);
        rad4.addActionListener(this);
        rad5.addActionListener(this);

	//	optionsGroup.add(rad1);
		optionsGroup.add(rad2);
		optionsGroup.add(rad3);
	//	options.add(rad1);
		options.add(rad2);   
		options.add(rad3);
		
		optionsGroup1.add(rad4);
		optionsGroup1.add(rad5);
		options1.add(rad4);
		options1.add(rad5);   

        gaussianControl= new ControlGroup();
        GaussianControls.add(options);
        gaussianControl.add(options);
        gaussianControl.setText("Before Running Set Central Charge");
        
        groundShieldControl= new ControlGroup();
        groundShieldControl.add(options1);
        groundShieldControl.setText("Before Running Set Shield Ground");
        addElement(gaussianControl);
        addElement(groundShieldControl);
     

	//	options1.add(rad3);   
	 //   GaussianControls.add(options);
        controls = new ControlGroup();
        controls.setText("Execution Controls");
  //      controls.add(slider1);
  //      addElement(GaussianControls);
        
        runButton = new JButton(new TealAction("Start", "Start", this));
        runButton.setFont(runButton.getFont().deriveFont(Font.BOLD));
        runButton.setBounds(40, 570, 195, 24);
        controls.add(runButton);
        
        pauseButton = new JButton(new TealAction("Pause", "Pause", this));
        pauseButton.setFont(pauseButton.getFont().deriveFont(Font.BOLD));
        pauseButton.setBounds(40, 570, 195, 24);
   //     controls.add(pauseButton);
        
        groundButton = new JButton(new TealAction("Connect Pail To Shield", "Connect Pail To Shield", this));
        groundButton.setFont(groundButton.getFont().deriveFont(Font.BOLD));
        groundButton.setBounds(40, 570, 195, 24);
        controls.add(groundButton);

        ungroundButton = new JButton(new TealAction("Disconnect Pail From Shield", "Disconnect Pail From Shield", this));
        ungroundButton.setFont(ungroundButton.getFont().deriveFont(Font.BOLD));
        ungroundButton.setBounds(40, 600, 195, 24);
        controls.add(ungroundButton);
        

        zeroCentralChargeButton = new JButton(new TealAction("Zero Central Charge", "Zero Central Charge", this));
        zeroCentralChargeButton.setFont(zeroCentralChargeButton.getFont().deriveFont(Font.BOLD));
        zeroCentralChargeButton.setBounds(40, 600, 195, 24);
        controls.add(zeroCentralChargeButton);

        messages = new JTextArea();
        messages.setColumns(32);
        messages.setRows(4);
        messages.setLineWrap(true);
        messages.setWrapStyleWord(true);
        messages.setVisible(true);
        messages.setText("");
        controls.add(messages);
        addElement(controls);
        but6 = new JButton(new TealAction("RESET", "reset", this));
        but6.setBounds(40, 730, 195, 24);
        params1 = new ControlGroup();
        params1.setText("RESET");
        params1.add(but6);
        addElement(params1);
     
        addSelectListener(this);
        mSEC.init();
        mViewer.doStatus(0);
        resetCamera();
    //  reset();
        addActions();
    }

    
    void addActions() {
        TealAction ta = new TealAction("Execution & View", this);
        addAction("Help", ta);
        TealAction tb = new TealAction("Faraday Ice Pail & Shield", this);
        addAction("Help", tb);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareToIgnoreCase("Faraday Ice Pail & Shield") == 0) {
            if ((mFramework != null) && (mFramework instanceof TFramework)) {
                ((TFramework)mFramework).openBrowser("help/FaradayIcePailShield.html");
            }
        }
        if (e.getActionCommand().compareToIgnoreCase("Execution & View") == 0) 
            {
            	if((mFramework != null) && (mFramework instanceof TFramework)) {
            		((TFramework)mFramework).openBrowser("help/executionView.html");
            	}
            }
        if (e.getActionCommand().compareToIgnoreCase("Start") == 0) {
           if(setCentralCharge && setGroundState) mSEC.start();
           else executionMessage("Must set charge and ground state\nbefore you can start execution");
            }
        if (e.getActionCommand().compareToIgnoreCase("Pause") == 0) {
        //      int state = mSEC.getSimState();
        //     if (state == TEngineControl.RUNNING) {
                 mSEC.stop();}
       //      }
        if (e.getActionCommand().compareToIgnoreCase("Connect Pail To Shield") == 0) {
            ground();}
        if (e.getActionCommand().compareToIgnoreCase("Disconnect Pail From Shield") == 0) {
            unground();}
        if (e.getActionCommand().compareToIgnoreCase("Zero Central Charge") == 0) {
        	cylinder4.setColliding(false);
            zeroCentralCharge();}
        if (e.getActionCommand().compareToIgnoreCase("Reset") == 0) {
            reset();}
      
        else if(e.getSource() == rad2){
        	signCentralCharge = 1;
            double centralChargeCharge = signCentralCharge*N;
            centralCharge.setCharge(centralChargeCharge);
            rad1.setVisible(false);
            rad2.setVisible(false);
            rad3.setVisible(false);
            setCentralCharge= true;
      }
        else if(e.getSource() == rad3){
        	signCentralCharge = -1.;
            double centralChargeCharge = signCentralCharge*N;
            centralCharge.setCharge(centralChargeCharge);
            rad1.setVisible(false);
            rad2.setVisible(false);
            rad3.setVisible(false);
            setCentralCharge= true;
      } 
        else if(e.getSource() == rad4){
            lb3.setDrawn(true);
            groundshieldinfinity.setDrawn(true);
            cylinder4.setColliding(false);
            ring5.setDrawn(true);
            rad4.setVisible(false);
            rad5.setVisible(false);         
            setGroundState = true;
      }
        else if(e.getSource() == rad5){
            rad4.setVisible(false);
            rad5.setVisible(false);      
            cylinder4.setColliding(true);
            setGroundState = true;
      }
     else if (e.getActionCommand().compareToIgnoreCase("reset") == 0) {
        resetPointCharges();

    }
         else {
          super.actionPerformed(e);
        }
    }
        

    private void ground() {
 
        ring2.setDrawn(false);
        cylinder2.setColliding(false);
        
        ring3.setDrawn(false);
        cylinder3.setColliding(false);
        groundcageshield.setDrawn(true);
        cylinder4.setColliding(true);
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
         
        centralCharge.setCharge(0.);
    }


    public void propertyChange(PropertyChangeEvent pce) {
        super.propertyChange(pce);
    }

    public void changeSignCentralCharge() {
    	signCentralCharge = -signCentralCharge;
        resetPointCharges();
        mSEC.stop();
        //resetCamera();
        for (int i = 0; i < N; i++) {
        	int j = i+2*N;
            pointCharges[j].setCharge(-signCentralCharge);       
            pointCharges[j].setDrawn(true);      
        }
        centralCharge.setCharge(signCentralCharge*N);
        cylinder4.setColliding(false);
        unground();
    }
    
    public void reset() {
         //   int state = mSEC.getSimState();
        //  if (state == TEngineControl.RUNNING) {
                 mSEC.stop();
        //resetCamera();
      //  if(signCentralCharge!=0){
	//        for ( int j = 0; j<4; j++)  {
	//	        for (int i = 0; i < 2 * N; i++) {
	//	        	removeElement(pointCharges[i+j*2*N]);
	//	        }
	 //       }
     //   }
        signCentralCharge = 0;
        centralCharge.setCharge(signCentralCharge*N);
        groundshieldinfinity.setDrawn(false);
        setCentralCharge= false;
        setGroundState = false;
        rad1.setSelected(false);
        rad2.setSelected(false);
        rad3.setSelected(false);
        rad4.setSelected(false);
        rad5.setSelected(false);
        rad1.setVisible(true);
        rad2.setVisible(true);
        rad3.setVisible(true);
        rad4.setVisible(true);
        rad5.setVisible(true);

        resetPointCharges();
        theEngine.requestSpatial();
    }

    private void resetPointCharges() {
        // -> Point Charges
        // these are the charges on the inner conductor, N positive and N negative alternating, 
        // and N on the outer conductor (in from infinity), opposite sign of central charge
        for (int i = 0; i < 8 * N; i++) {
            pointCharges[i] = new InfiniteLineCharge();
            pointCharges[i].setRadius(pointChargeRadius);
            pointCharges[i].setMass(1.0);
            pointCharges[i].setGeneratingP(true);
            pointCharges[i].setID("pointCharge" + i);
            pointCharges[i].setPickable(false);
            pointCharges[i].setColliding(true);
            pointCharges[i].setMoveable(true);
            SphereCollisionController sccx = new SphereCollisionController(pointCharges[i]);
            sccx.setRadius(pointChargeRadius);
            sccx.setTolerance(0.5);
            pointCharges[i].setCollisionController(sccx);
            addElement(pointCharges[i]);
        }
        Point3d position = null;
        double charge=1.;
        double mass=1.;
        double ang;
        double rad=1.;
        // we now create four rings, two in the inner cylinder and two in the outer cylinder
        // j is the index for the four rings 0,1,2,3
        for ( int j = 0; j<4; j++)  {
            if (j == 0 ) rad = radius1+2*pointChargeRadius;
            if ( j==1) rad = radius2-2*pointChargeRadius;
            if (j == 2 ) rad = radius3+2*pointChargeRadius;
            if ( j==3) rad = radius4-2*pointChargeRadius;
            charge = Math.pow(-1,j);
            // now we create the 2N charges in each ring, N positive and N negative, alternating
	        for (int i = 0; i < 2 * N; i++) {
	        // set charge signs
	        	charge = -1.*charge;
	            pointCharges[i+j*2*N].setCharge(charge);
	    //        if (charge < 0.) mass = 1.; else mass = 100.;
	  //          if (charge < 0. ) radius = pointChargeRadius; else radius = pointChargeRadius;
	            pointCharges[i+j*2*N].setMass(mass);
	   //         pointCharges[i].setRadius(radius);
	               
	         // set position of charges
            	ang = 2. * Math.PI * (double) i / (2. * (double) N);
	            position = new Point3d(rad * Math.cos(ang), rad * Math.sin(ang), 0.);
	            pointCharges[i+j*2*N].setPosition(new Vector3d(position), true);
	            pointCharges[i+j*2*N].setVelocity(new Vector3d());
	        }
        }
    }
    void executionMessage(String str){
    	StringBuffer buf = new StringBuffer(str);
    	//buf.append("\nYou have a total of " + numMoves + " points!");
    	//buf.append("\nCharge A: = " + pcA.getCharge() + "\nCharge B: = " + pcB.getCharge());
    	TDebug.println(1,buf.toString());
    	messages.setText(buf.toString()); 	
   // 	clearText = true;
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
