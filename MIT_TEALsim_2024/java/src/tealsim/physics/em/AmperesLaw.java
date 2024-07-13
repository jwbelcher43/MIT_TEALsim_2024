 package tealsim.physics.em;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Iterator;
import javax.media.j3d.*;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.vecmath.*;
import teal.config.Teal;
import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.render.Rendered;
import teal.render.geometry.Cylinder;
import teal.render.j3d.*;
import teal.render.primitives.Line;
import teal.sim.control.VisualizationControl;
import teal.sim.engine.TEngineControl;
import teal.sim.spatial.FieldConvolution;
import teal.sim.spatial.FieldVector;
import teal.sim.spatial.SpatialTextLabel;
import teal.math.RectangularPlane;
import teal.physics.em.EMEngine;
import teal.physics.em.FiniteWire;
import teal.physics.em.SimEM;
import teal.physics.physical.PhysicalObject;
import teal.physics.physical.Wall;
import teal.ui.UIPanel;
import teal.ui.control.*;
import teal.util.TDebug;
import teal.visualization.dlic.DLIC;
// first random change//
/** An application to show the tangent vectors and magnetic fields on the contour surrounding an 
 * open surface to make clear the geometric concepts behind Ampere's Law.  We only allow lines of current
 * either into or out of the page, all of the same strength.
 *  
 * @author John Belcher
 * @version 1.0 
 * */

public class AmperesLaw extends SimEM  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4893701636905167425L;
	/** This is for serialized version, presently not used.  *\
 
//  USER INTERFACE CONTROLS  //
	/** ButtonGroup for the two choices of Amperean loops.  */
	ButtonGroup chooseAmpereanLoopOptionsGroup;
	/** ButtonGroup for the two ways to show the magnitude of B.  */
	ButtonGroup chooseHowToScaleBOptionsGroup;
	/** Radio button for choosing the Rectangular Amperean loop.  */
	JRadioButton showAmpereanRectangleButton;
	/** Radio button for choosing the Circular Amperean loop.  */
    JRadioButton showAmpereanCircleButton;       
    /** Button to reset original camera view. */
    JButton resetViewButton = null;
    /** Field line convolution of the magnetic field in the scene due to all the line currents.  */
    protected FieldConvolution mDLIC = null;
    /** Add line current to scene control group.  */
    ControlGroup addLineCurrentControlGroup;
    /** Button to add a positive (out of page) line current to scene.  */
    JButton addPosLineCurrentButton = null;
    /** Button to add a negative line current to scene.  */
    JButton addNegLineCurrentButton = null;
    /** Button to delete all line currents presently in scene.  */
    JButton deleteAllLineCurrentsButton = null;
    /** Control position of Amperean loop control group.  */
    ControlGroup PositionControlGroup;
    /** Reset view control group.  */
    ControlGroup resetViewControlGroup;
    /** Choose desired Amperean loop (circle or rectangle) control group.  */
    ControlGroup AmpereanLoopControlGroup;
    /** Control group for how to scale B field choices. */
    ControlGroup scaleByMagnitudeControlGroup;
	/** Radio button for choosing scale B vectors by magnitude.  */
    JRadioButton scaleBbyMagnitudeButton;    
	/** Radio button for choosing same length B vectors.  */
    JRadioButton normalizeBMagnitudeToUnityButton;    
    /** Visualization control for LIC visualization. LIC stands for line integral convolution.  */
    VisualizationControl LICvisualizationControl;
    /** Logical value for whether circle is visible. */
    boolean circleVisible = false;;
    /** Logical value for whether rectangle is visible. */
    boolean rectangleVisible = true;
    /** Logical value for whether there are any line currents in the scene. */
    boolean lineCurrentsPresent = true;
    /** Number of line currents in the scene, set to 2 for startup. */
    int numberLineCurrents = 2;
    
    //  Current Thru meter properties.  A cylinder in the lower left of the view whose height indicates the current through open surface
    /** Base for the current thru meter. */
    Wall baseCurrentThruMeter;
    /** Text label for the current thru meter.*/
 	SpatialTextLabel currentthrubaseLabel;
    /** The size of the square for the base of the current thru meter.  */
    double currentthruMeterSizeBase = 1.2;
    /** The height of the current thru meter rectangle for the current thru due to one unit of line current. */
    double oneUnitCurrentThru = 0.5;
     
    //  Display properties
    /** The approximate width of the canvas in the standard view. We use this to draw a box
     * around the LIC panel, which consists of four white lines, for contrast/appearance reasons.  */
    double screenWidth = 6.;
    /** The scale factor for the magnetic field vectors on our surfaces. Increasing this value makes
     * the field vectors larger.  */
    double arrowScaleBfield = .2;
    /** The scale factor for the surface normal vectors.  Increasing this value makes the normal vectors larger.  */
    double arrowScaleTangent = .3;
    
    // Field magnitude display properties 
    /** Boolean for whether we normalize B (false) or whether we allow the magnitude to show to some extent (true). 
     * The extent to which the magnitude is shown is determined by the variables normMagnitudeFactor and 
     * powerMagnitudeScale.  */
    boolean scaleByMagnitudeBoolean = false;
    /** If we scale by magnitude, the normalization factor we use. */
    double normMagnitudeFactor = 0.1;
    /** If we scale by magnitude, the power of the magnitude we let the scale vary by (less than or equal to 1). */
    double powerMagnitudeScale = 0.3;
    
    //  Amperean circle properties  
    /** X position of the center of the amperean loop. */
    double posX = 0.;
    /** Y position of the center of the amperean loop. */
    double posY = 0.;
    /** A TEALsim native object for the Amperean circle.  */
    Rendered AmpereanCircle = new Rendered();
    /** A ShapeNode for the Amperean circle.  */
    AmpereanCircleNode3D ShapeNodeACircle;
    /** A TEALsim native object for the current thru through the Amperean circle.  */
    Rendered AmpereanCircleCurrentThru = new Rendered();
    /** A ShapeNode for the current thru through the Amperean Circle.  */
    ShapeNode ShapeNodeACircleCurrentThru = new ShapeNode();
    /** Vector for the initial position of the Amperean circle. */
    Vector3d posACircle = null;
    /** The radius of the Amperean circle.  */
    double radiusACircle = 1.0;
    /** The height of the cylinder representing the current thru through the Amperean circle.  */
    double heightACircleCurrentThru = oneUnitCurrentThru;
    /** The radius of the cylinder representing the current thru through the Amperean circle.  */
    double radiusACircleCurrentThru = .5;
    /** Vector for the initial position of the cylinder representing current thru through the circle. */
    Vector3d posACircleCurrentThru = null;

    //  Amperean rectangle properties
    /** A TEALsim native object for the Amperean rectangle.  */
    Rendered AmpereanRectangle = new Rendered();
    /** A ShapeNode for the Amperean Rectangle.  */
    AmpereanRectangleNode3D ShapeNodeARectangle = null;
    /** A TEALsim native object for the current thru through the Amperean rectangle.  */
    Rendered AmpereanRectangleFlux = new Rendered();
    /** A ShapeNode for the current thru through the Amperean Rectangle.  */
    ShapeNode ShapeNodeARectangleCurrentThru = new ShapeNode();
    /** The height of the cylinder representing the current thru through the Amperean circle.  */
    double heightARectangleCurrentThru = 1.;
    /** The radius of the cylinder representing the current thru through the Amperean circle.  */
    double radiusARectangleCurrentThru = .5;
    /** The height of the Amperean rectangle.  */
    double heightARectangle = 3.;
    /** The width of the Amperean rectangle.  */
    double widthARectangle = 1.;
    
    // Controls for position of Amperean loop
    /** Slider for the y-position of the Amperean loop (rectangle or circle).  */
    PropertyDouble posSlider_y = new PropertyDouble();
    /** Slider for the x-position of the Amperean loop (rectangle or circle).  */
    PropertyDouble posSlider_x = new PropertyDouble();
    /** Vector for the initial position of the rectangle. */
    Vector3d posARectangle = null;
    /** Vector for the initial position of the rectangle representing current thru the rectangle. */
    Vector3d posARectangleCurrentThru = null;
    
    //  Field and normal vectors on the circle and their spatial distribution 
    /** The magnetic field vectors. */
    FieldVector[] theField;
    /** The scale factor for the magnetic field vectors on our contours. Increasing this value makes
     * the field vectors larger.  */
    double arrowScaleBField = .6;
   
    public AmperesLaw() {
        super();
        // Hide run controls       
        mSEC.rebuildPanel(0);
        // Set debug level
        TDebug.setGlobalLevel(0);
        title = "Ampere's Law";
        mViewer.setShowGizmos(false);
        mViewer.setCursorOnDrag(false);
        mDLIC = new FieldConvolution();
        mDLIC.setSize(new Dimension(512, 512));
        mDLIC.setComputePlane(new RectangularPlane(new BoundingSphere(new Point3d(), 3.)));


        
        // Create the Field Vectors to be used in the scene and add to the scene.  Actually number is (numvec - 1)
        int numvec = 9;
        theField = new FieldVector[numvec];
        for (int i = 1; i<numvec; i++) {
        	theField[i] = new FieldVector();
        	theField[i].setFieldType(2);
	    	theField[i].setPosition(new Vector3d(0,0,0));
	 		theField[i].setColor(Teal.DefaultBFieldColor);
	 		theField[i].setArrowScale(arrowScaleBField);
	 		theField[i].setDrawn(true);
	 		theField[i].setScaleByMagnitude(scaleByMagnitudeBoolean);
	 		theField[i].setNormFactor(normMagnitudeFactor);
	 		theField[i].setPowerScale(powerMagnitudeScale);
	 		addElement(theField[i]);
        }
       
        // Create the Amperean Circle using teal.render.geometry and add it to the scene
        posACircle = new Vector3d(0.,0.,0);
        ShapeNodeACircle = new AmpereanCircleNode3D(1.,.04,.04,.15);
        AmpereanCircle.setNode3D(ShapeNodeACircle);
        AmpereanCircle.setColor(new Color(0, 0, 170));
        AmpereanCircle.setPosition(posACircle);
        AmpereanCircle.setDirection(new Vector3d(0.,1.,0.));
        AmpereanCircle.setDrawn(false);
        addElement(AmpereanCircle);
    
        // Create the Cylinder representing the current thru through the circular amperean loop using teal.render.geometry and add it to the scene
        Vector3d posACircleCurrentThruBase;
        posACircleCurrentThruBase =  new Vector3d(2.,-2.5,0);
        posACircleCurrentThru=  new Vector3d(2.,-2.5,0);
        Vector3d posACircleCurrentThruLabel  = new Vector3d(1.,-2.8,0);
        ShapeNodeACircleCurrentThru.setGeometry(Cylinder.makeGeometry(32, radiusACircleCurrentThru, heightACircleCurrentThru));
        ShapeNodeACircleCurrentThru.setTransparency(0.3f);
        AmpereanCircleCurrentThru.setNode3D(ShapeNodeACircleCurrentThru);
        AmpereanCircleCurrentThru.setColor(new Color(255, 0, 0));
        AmpereanCircleCurrentThru.setPosition(posACircleCurrentThru);
        AmpereanCircleCurrentThru.setDirection(new Vector3d(0.,1.,0.));
        AmpereanCircleCurrentThru.setDrawn(false);
        addElement(AmpereanCircleCurrentThru);
        
        // Create the label and the base for the current thru meter and add to the scene
		currentthrubaseLabel = new SpatialTextLabel(" What is this? ", posACircleCurrentThruLabel);
		currentthrubaseLabel.setBaseScale(0.2);
		currentthrubaseLabel.setPositionOffset(new Vector3d(0.05, 0.0, 0.));
		currentthrubaseLabel.setRefDirectionOffset(0.5);
		currentthrubaseLabel.setUseDirectionOffset(true);
		addElement(currentthrubaseLabel);     
        baseCurrentThruMeter = new Wall(posACircleCurrentThruBase,new Vector3d(currentthruMeterSizeBase, 0, 0.), new Vector3d(0, 0., currentthruMeterSizeBase));
        addElement(baseCurrentThruMeter);
        
        // Create the Amperean Rectangle using teal.render.geometry and add it to the scene
        posARectangle = new Vector3d(0.,0.,0);
        ShapeNodeARectangle = new AmpereanRectangleNode3D(widthARectangle,heightARectangle,.02);
        AmpereanRectangle.setNode3D(ShapeNodeARectangle);
        AmpereanRectangle.setColor(new Color(0, 0, 170));
        AmpereanRectangle.setPosition(posARectangle);
        AmpereanRectangle.setDirection(new Vector3d(0.,1.,0.));
        AmpereanRectangle.setDrawn(true);
        addElement(AmpereanRectangle);
        
        // Create the Cylinder representing the current thru the rectangle using teal.render.geometry and add it to the scene
        ShapeNodeARectangleCurrentThru.setGeometry(Cylinder.makeGeometry(32, radiusARectangleCurrentThru, heightARectangleCurrentThru));
        ShapeNodeARectangleCurrentThru.setTransparency(0.3f);
        AmpereanRectangleFlux.setNode3D(ShapeNodeARectangleCurrentThru);
        AmpereanRectangleFlux.setColor(new Color(255, 0, 0));
        AmpereanRectangleFlux.setPosition(posACircleCurrentThru);
        AmpereanRectangleFlux.setDirection(new Vector3d(0.,1.,0.));
        AmpereanRectangleFlux.setDrawn(true);
        addElement(AmpereanRectangleFlux);
        
        // Put the B field vectors in their proper places.  
        PlaceBTVectors();
        
        // create reset view control group and add to scene    
	    resetViewControlGroup = new ControlGroup();
	    resetViewControlGroup.setText("Reset View");
	    resetViewButton = new JButton(new TealAction("Reset View", "resetview", this));
	    resetViewButton.setBounds(40, 730, 195, 24);
	    resetViewControlGroup.add(resetViewButton);
	    addElement(resetViewControlGroup);
	    
	    // Build interface panels
        UIPanel chooseAmpereanLoopUIPanel = new UIPanel();
        chooseAmpereanLoopUIPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        chooseAmpereanLoopUIPanel.setLayout(new GridLayout(2,1));
        chooseAmpereanLoopOptionsGroup = new ButtonGroup();
        UIPanel scaleEMagnitudeUIPanel = new UIPanel();
        scaleEMagnitudeUIPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        scaleEMagnitudeUIPanel.setLayout(new GridLayout(2,1));
        chooseHowToScaleBOptionsGroup = new ButtonGroup();
        showAmpereanRectangleButton = new JRadioButton("Show Amperean Cylinder");        
        showAmpereanCircleButton = new JRadioButton("Show Amperean Circle");    
        scaleBbyMagnitudeButton = new JRadioButton("Scale B Arrow Length by (Magnitude B)^0.3"); 
        normalizeBMagnitudeToUnityButton = new JRadioButton("Make All B Arrow Lengths the Same"); 
        showAmpereanRectangleButton.setSelected(true);
        normalizeBMagnitudeToUnityButton.setSelected(true);
        showAmpereanRectangleButton.addActionListener(this);
        showAmpereanCircleButton.addActionListener(this);
        scaleBbyMagnitudeButton.addActionListener(this);
        normalizeBMagnitudeToUnityButton.addActionListener(this);
		chooseAmpereanLoopOptionsGroup.add(showAmpereanRectangleButton);
		chooseAmpereanLoopOptionsGroup.add(showAmpereanCircleButton);
		chooseAmpereanLoopUIPanel.add(showAmpereanRectangleButton);
		chooseAmpereanLoopUIPanel.add(showAmpereanCircleButton);   
		chooseHowToScaleBOptionsGroup.add(normalizeBMagnitudeToUnityButton);
		chooseHowToScaleBOptionsGroup.add(scaleBbyMagnitudeButton);
		scaleEMagnitudeUIPanel.add(normalizeBMagnitudeToUnityButton);
		scaleEMagnitudeUIPanel.add(scaleBbyMagnitudeButton);   
	    scaleByMagnitudeControlGroup= new ControlGroup();
	    scaleByMagnitudeControlGroup.add(scaleEMagnitudeUIPanel);
        scaleByMagnitudeControlGroup.setText("Choose B Field Scaling");
        addElement(scaleByMagnitudeControlGroup);
        AmpereanLoopControlGroup= new ControlGroup();
        AmpereanLoopControlGroup.add(chooseAmpereanLoopUIPanel);
        AmpereanLoopControlGroup.setText("Choose Amperean Surface");
        addElement(AmpereanLoopControlGroup);

        // Create the two sliders for the Amperean rectangle position   
        posSlider_x.setText("X Position");
        posSlider_x.setMinimum(-3.);
        posSlider_x.setMaximum(3.0);
        posSlider_x.setPaintTicks(true);
        posSlider_x.addPropertyChangeListener("value", this);
        posSlider_x.setValue(0.);
        posSlider_x.setVisible(true);
        posSlider_y.setText("Y Position ");
        posSlider_y.setMinimum(-3.);
        posSlider_y.setMaximum(3.0);
        posSlider_y.setPaintTicks(true);
        posSlider_y.addPropertyChangeListener("value", this);
        posSlider_y.setValue(0.);
        posSlider_y.setVisible(true);
          
        // Add the sliders to the control group and add the control group to the scene       
        PositionControlGroup = new ControlGroup();
        PositionControlGroup.setText("Amperean Loop Position");
        PositionControlGroup.add(posSlider_y);
        PositionControlGroup.add(posSlider_x);
        addElement(PositionControlGroup);
        
        // add line current control group (Jbuttons associated with this group added in addAction()
        addLineCurrentControlGroup = new ControlGroup();
        addLineCurrentControlGroup.setText("Add Line Currents (Maximum Six)");
        addElement(addLineCurrentControlGroup);
        
        // add LIC visualization
        LICvisualizationControl = new VisualizationControl();
        LICvisualizationControl.setText("Field Visualization");
        LICvisualizationControl.setFieldConvolution(mDLIC);
        LICvisualizationControl.setConvolutionModes(DLIC.DLIC_FLAG_B);
        addElement(LICvisualizationControl);

        // Build the frame around the scene (four white lines) 
        Outline();
     
        // Change some features of the lighting, background color, etc., from the default values, if desired
        mViewer.setBackgroundColor(new Color(200,200,200));
        
        // Set parameters for mouseScale 
        Vector3d mouseScale = mViewer.getVpTranslateScale();
        mouseScale.x *= 0.05;
        mouseScale.y *= 0.05;
        mouseScale.z *= 0.5;
        mViewer.setVpTranslateScale(mouseScale);
       
        // Add Action for pull down menus          
        addActions();
        
        // Add one initial positive (out of page) and one negative line current to begin with
        addElement(randomLineCurrent(1.0, 2.0, 2.5, new Vector3d(0, 0, 0)));
        addElement(randomLineCurrent(-1.0, 2.0, 2.5, new Vector3d(0, 0, 0)));
        calculateCurrentThru();
        
        // Set initial state 
        mSEC.init();  
        theEngine.requestRefresh();
        mSEC.setVisible(true);
        reset();
        resetCamera();
  }


    /** Method to add actions to application.  The actions added are two entries on the pull down help menu,
     * and a number of actions to do with adding and removing line currents.  
     */
    void addActions() {
    	
    	// add two help menu actions
        TealAction ta = new TealAction("Ampere's Law", this);
        addAction("Help", ta);
        TealAction tb = new TealAction("Execution & View", this);
        addAction("Help", tb);
        
        // add actions to do with adding and deleting line currents
        deleteAllLineCurrentsButton = new JButton(new TealAction("Delete All Line Currents", "delete_all", this));
        deleteAllLineCurrentsButton.setBounds(250, 650, 195, 24);
        addPosLineCurrentButton = new JButton(new TealAction("Add Out of Page Line Current", "add_random_positive", this));
        addPosLineCurrentButton.setBounds(40, 690, 195, 24);
        addNegLineCurrentButton = new JButton(new TealAction("Add Into Page Line Current", "add_random_negative", this));
        addNegLineCurrentButton.setBounds(250, 690, 195, 24);
        addLineCurrentControlGroup.add(addPosLineCurrentButton);
        addLineCurrentControlGroup.add(addNegLineCurrentButton);
        addLineCurrentControlGroup.add(deleteAllLineCurrentsButton);
    }

  /** Method to handle the action performed events.  */  
    public void actionPerformed(ActionEvent e) {
        TDebug.println(1, " Action comamnd: " + e.getActionCommand());
        if (e.getActionCommand().compareToIgnoreCase("Ampere's Law") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).openBrowser("help/ampereslaw.html");
        	}
        }
        else if (e.getActionCommand().compareToIgnoreCase("Execution & View") == 0) 
        {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).openBrowser("help/executionView.html");
        	}
     
	    } else if (e.getActionCommand().compareToIgnoreCase("add_random_positive") == 0) {
	        if(numberLineCurrents < 6 ) addElement(randomLineCurrent(1.0, 2.0, 2.5, new Vector3d(0, 0, 0)));
	        numberLineCurrents = numberLineCurrents +1;
	        if(numberLineCurrents == 6) {
	        	addPosLineCurrentButton.setEnabled(false);
	        	addNegLineCurrentButton.setEnabled(false);
	        }
	        lineCurrentsPresent = true;
	        calculateCurrentThru();
	        MakeFieldVisible(true);
	        
	    } else if (e.getActionCommand().compareToIgnoreCase("add_random_negative") == 0) {
	        if(numberLineCurrents < 6 ) addElement(randomLineCurrent(-1.0, 2.0, 2.5, new Vector3d(0, 0, 0)));
	        numberLineCurrents = numberLineCurrents +1;
	        if(numberLineCurrents == 6) {
	        	addPosLineCurrentButton.setEnabled(false);
	        	addNegLineCurrentButton.setEnabled(false);
	        }
	        lineCurrentsPresent = true;
	        calculateCurrentThru();
	        MakeFieldVisible(true);
	        
	    } else if (e.getActionCommand().compareToIgnoreCase("delete_all") == 0) {
	        clearAllLineCurrents();
	        lineCurrentsPresent = false;
	        MakeFieldVisible(false);
	        addPosLineCurrentButton.setEnabled(true);
	        addNegLineCurrentButton.setEnabled(true);
	        numberLineCurrents = 0;
	       // this is a hack
	        heightACircleCurrentThru = 0.;
	        ShapeNodeACircleCurrentThru.setGeometry(Cylinder.makeGeometry(32, radiusACircleCurrentThru, heightACircleCurrentThru));
	        AmpereanCircleCurrentThru.setNode3D(ShapeNodeACircleCurrentThru);
	        AmpereanCircleCurrentThru.setPosition(new Vector3d(2.,-2.5+heightACircleCurrentThru/2.,0));

	        
	    } else if (e.getActionCommand().compareToIgnoreCase("resetview") == 0) {
	        resetCamera();
	        theEngine.requestSpatial();

	    } else if(e.getSource() == showAmpereanRectangleButton){
            AmpereanRectangle.setDrawn(true);	
            AmpereanRectangleFlux.setDrawn(true);
            rectangleVisible = true;
            AmpereanCircle.setDrawn(false);	
            AmpereanCircleCurrentThru.setDrawn(false);
            circleVisible = false;
            calculateCurrentThru();
            PlaceBTVectors();
            
	    } else if(e.getSource() == showAmpereanCircleButton){
            AmpereanCircle.setDrawn(true);	
            AmpereanCircleCurrentThru.setDrawn(true);
            circleVisible = true;
            AmpereanRectangleFlux.setDrawn(false);
            AmpereanRectangle.setDrawn(false);	
            rectangleVisible = false;
            PlaceBTVectors();
            calculateCurrentThru();
            
        } else if(e.getSource() == scaleBbyMagnitudeButton){
            	for (int i = 1; i <= 8; i++) {
             		theField[i].setScaleByMagnitude(true);
             	} 	
            theEngine.requestRefresh();
            theEngine.requestSpatial();
            
        } else if(e.getSource() == normalizeBMagnitudeToUnityButton){
        	for (int i = 1; i <= 8; i++) {
         		theField[i].setScaleByMagnitude(false);
         	} 	
              theEngine.requestRefresh();
              theEngine.requestSpatial();
              
        } else {
            super.actionPerformed(e);
        }
    }

    /** Method to place the magnetic field vectors on the rectangle and on the circle. */
	public void PlaceBTVectors() {
		// First get the transform3D group for the amperean loop as a whole, AmpereanLoop
	if ( rectangleVisible == true) {
	        AmpereanRectangle.setNode3D(ShapeNodeARectangle);
		    Transform3D AmpereanLoop = new Transform3D();
	        AmpereanLoop = ShapeNodeARectangle.getTransform3D();
	        
	    	 for (int j=1; j<=8; j++) {
	    		 // Get the transform3D group VectorITGTransform3D for the individual vector tangents 
	    		 // in the amperean loop, with respect to the center of the loop
	             TransformGroup VectorITG = new TransformGroup();
	             VectorITG = ShapeNodeARectangle.getTransformGroupVectorI(j);
	             Transform3D VectorITGTransform3D = new Transform3D();
	             VectorITG.getTransform(VectorITGTransform3D);
	             // Now that we have both of those transform groups, compute the world transform3D group for the individual vector tangents
	             // by multiplying the two transform3D groups together
	             Transform3D Transform3DJ = new Transform3D();
	             Transform3DJ.mul(AmpereanLoop, VectorITGTransform3D);
	             // we have the full transform3d group for the individual vector tangent vectors but we only want the displacement, not the rotation
	             // This locates the end of the vector tangent vectors, and that is where we want to place the field vectors
	             Vector3d WorldLocationTangentVectorJ = new Vector3d();
	             Transform3DJ.get(WorldLocationTangentVectorJ);
	             theField[j].setPosition(WorldLocationTangentVectorJ);
	             
	             //  Determine color of the tangent vector
	             Vector3d tangentLocal = new Vector3d();
	             tangentLocal = ShapeNodeARectangle.getTangentVectorI(j);
	             Vector3d tangentWorld = new Vector3d();
	             Transform3DJ.transform(tangentLocal,tangentWorld);
	             double dotproduct;
	             Vector3d theFieldDirection = new Vector3d();
	             theFieldDirection = theField[j].getDirection();
	             dotproduct = tangentWorld.dot(theFieldDirection);
	             if ( dotproduct < 0. ) ShapeNodeARectangle.setColorTangentI(j, new Color3f(Color.gray));
	             if ( dotproduct >= 0. ) ShapeNodeARectangle.setColorTangentI(j, new Color3f(Color.gray)); 
	    	 }
		        AmpereanRectangle.setNode3D(ShapeNodeARectangle);
	}
    	 else {
    		 
    		AmpereanCircle.setNode3D(ShapeNodeACircle);
  		    Transform3D AmpereanLoop = new Transform3D();
  	        AmpereanLoop = ShapeNodeACircle.getTransform3D();
  	        
  	    	 for (int j=1; j<=8; j++) {
  	    		 // Get the transform3D group VectorITGTransform3D for the individual vector tangents 
  	    		 // in the amperean loop, with respect to the center of the loop
  	             TransformGroup VectorITG = new TransformGroup();
  	             VectorITG = ShapeNodeACircle.getTransformGroupVectorI(j);
  	             Transform3D VectorITGTransform3D = new Transform3D();
  	             VectorITG.getTransform(VectorITGTransform3D);
  	             // Now that we have both of those transform groups, compute the world transform3D group for the individual vector tangents
  	             // by multiplying the two transform3D groups together
  	             Transform3D Transform3DJ = new Transform3D();
  	             Transform3DJ.mul(AmpereanLoop, VectorITGTransform3D);
  	             // we have the full transform3d group for the individual vector tangent vectors but we only want the displacement, not the rotation
  	             // This locates the end of the vector tangent vectors, and that is where we want to place the field vectors
  	             Vector3d WorldLocationTangentVectorJ = new Vector3d();
  	             Transform3DJ.get(WorldLocationTangentVectorJ);
  	             WorldLocationTangentVectorJ = ShapeNodeACircle.getTangentVectorI(j);
  	             Vector3d newPosition=new Vector3d();
  	             newPosition.x = WorldLocationTangentVectorJ.x+posX;
  	             newPosition.y = WorldLocationTangentVectorJ.y+posY;
  	             newPosition.z = 0.;
  	         //    VectorITGTransform3D.transform(WorldLocationTangentVectorJ);
  	             theField[j].setPosition(newPosition);
  	             
  	             //  Determine color of the tangent vector
  	             Vector3d tangentLocal = new Vector3d();
  	             tangentLocal = ShapeNodeACircle.getTangentVectorI(j);
  	             Vector3d tangentWorld = new Vector3d();
  	             Transform3DJ.transform(tangentLocal,tangentWorld);
  	             double dotproduct;
  	             Vector3d theFieldDirection = new Vector3d();
  	             theFieldDirection = theField[j].getDirection();
  	             dotproduct = tangentWorld.dot(theFieldDirection);
  	             if ( dotproduct < 0. ) ShapeNodeACircle.setColorTangentI(j, new Color3f(Color.gray));
  	             if ( dotproduct >= 0. ) ShapeNodeACircle.setColorTangentI(j, new Color3f(Color.gray));
  	             
  	    	 }
     		AmpereanCircle.setNode3D(ShapeNodeACircle);
    		 
    	 }
	}
	
	/**  Method to handle changes in position or orientation of the Amperean loops, or changes in the positions of the line currents. */
    public void propertyChange(PropertyChangeEvent pce) {
        Object source = pce.getSource();
        
        if (source == posSlider_x) {
            posX = ((Double) pce.getNewValue()).doubleValue();
            posARectangle.x=posX;
            AmpereanRectangle.setNode3D(ShapeNodeARectangle);
            AmpereanRectangle.setPosition(posARectangle);
            posACircle.x=posX;
            AmpereanCircle.setNode3D(ShapeNodeACircle);
            AmpereanCircle.setPosition(posACircle);
            // see if point line current inside or outside of the circle and rectangle after this move
            calculateCurrentThru();
            PlaceBTVectors();
            
        } else if (source == posSlider_y) {
            posY = ((Double) pce.getNewValue()).doubleValue();
            posARectangle.y = posY;
            AmpereanRectangle.setNode3D(ShapeNodeARectangle);
            AmpereanRectangle.setPosition(posARectangle);
            posACircle.y = posY;
            AmpereanCircle.setNode3D(ShapeNodeACircle);
            AmpereanCircle.setPosition(posACircle);
            calculateCurrentThru();
            PlaceBTVectors();
            
            
        } else if (source instanceof FiniteWire) {
        	
        	// Find the amount of line current in the rectangle in the new configuration of line currents
        	
            double currentThruRectangle = 0.;
            Collection elements = ((EMEngine)theEngine).getPhysicalObjs();
            TDebug.println(0, elements.size());
            Iterator myIterator = elements.iterator();
            while (myIterator.hasNext() == true) {			
                Object myObject = myIterator.next();
                if (myObject instanceof FiniteWire) {
                	Vector3d r = new Vector3d();
                	double myCurrent = 0.;
                	myCurrent = ((FiniteWire) myObject).getCurrent();
                    r.set(((PhysicalObject) myObject).getPosition());
                	boolean insiderectangle = insideRectangle(r);
                	if (insiderectangle)	currentThruRectangle = currentThruRectangle + myCurrent;
                }
            }
            // Adjust the height of the rectangle showing the current thru through the rectangle
            heightARectangleCurrentThru = oneUnitCurrentThru*(currentThruRectangle);
            ShapeNodeARectangleCurrentThru.setGeometry(Cylinder.makeGeometry(32, radiusARectangleCurrentThru, heightARectangleCurrentThru));
            AmpereanRectangleFlux.setNode3D(ShapeNodeARectangleCurrentThru);
            AmpereanRectangleFlux.setPosition(new Vector3d(2.,-2.5+heightARectangleCurrentThru/2.,0));
            
            //  Find the amount of line current in the circle in the new configuration of line currents
            
            double currentThruCircle = 0.;
            myIterator = elements.iterator();
            while (myIterator.hasNext() == true) {			
               Object myObject = myIterator.next();
               if (myObject instanceof FiniteWire) {
               	Vector3d r = new Vector3d();
               	double myCurrent = 0.;
               	myCurrent = ((FiniteWire) myObject).getCurrent();
                   r.set(((PhysicalObject) myObject).getPosition());
               	boolean insidecircle = insideCircle(r);
               	if (insidecircle)	currentThruCircle = currentThruCircle + myCurrent;
               }
            }
            // Adjust the height of the rectangle showing the current thru through the circle
            heightACircleCurrentThru = oneUnitCurrentThru*(currentThruCircle);
            ShapeNodeACircleCurrentThru.setGeometry(Cylinder.makeGeometry(32, radiusACircleCurrentThru, heightACircleCurrentThru));
            AmpereanCircleCurrentThru.setNode3D(ShapeNodeACircleCurrentThru);
            AmpereanCircleCurrentThru.setPosition(new Vector3d(2.,-2.5+heightACircleCurrentThru/2.,0));

        } else {
            super.propertyChange(pce);
        }
 
    }
    /** Method to determine whether a given position vector is inside the Amperean rectangle or not. */
    public boolean insideRectangle(Vector3d position){
    	boolean inside = false;
    	Vector3d relativePosition = new Vector3d();
    	Vector3d rectangleAxis = new Vector3d();
    	rectangleAxis = AmpereanRectangle.getDirection();
    	rectangleAxis.normalize();
    	relativePosition.sub(position,posARectangle);
    	// Compute the coordinates of position in a frame centered on the rectangle
    	double zcoordinate = relativePosition.dot(rectangleAxis);
    	rectangleAxis.scale(zcoordinate);
    	relativePosition.sub(rectangleAxis);
    	double rhocoordinate = relativePosition.length();
    	if (Math.abs(zcoordinate)<=heightARectangle/2. && rhocoordinate <= widthARectangle/2.) inside = true;
    	return inside;
    }
    
    /** Method to determine whether a given position vector is in side the Amperean circle or not. */
    public boolean insideCircle(Vector3d position){
    	boolean inside = false;
    	Vector3d relativePosition = new Vector3d();
    	relativePosition.sub(position,posACircle);
    	// compute the distance of the point line current from the center of the Amperean circle
    	double radialdistance = relativePosition.length();
    	if (radialdistance <= radiusACircle) inside = true;
    	return inside;
    }
    
    /** Method for general reset, currently does not do anything.  */
    public void reset() {       
    }
    
    /** Method to reset the camera view to the original view.  */
    public void resetCamera() {
        mViewer.setLookAt(new Point3d(0.0, 0.0, 0.4), 
        	new Point3d(0., 0.0, 0.), new Vector3d(0., 1., 0.)); 
    }
    
    /** Method to create a random line current, avoiding other line currents already present. */
    private FiniteWire randomLineCurrent(double current, double tolerance, double radius, Vector3d offset) {
        FiniteWire newLineCurrent = new FiniteWire();
        newLineCurrent.setCurrent(current);
        newLineCurrent.setDirection(new Vector3d(0.,0.,1.));
        newLineCurrent.setMass(1.);
        newLineCurrent.setRadius(0.2);
        newLineCurrent.setSelectable(true);
        newLineCurrent.setPickable(true);
        newLineCurrent.setColliding(true);
        newLineCurrent.addPropertyChangeListener(this);

        boolean positionOK = false;
        double rand;
        double signx;
        double signy;
        Vector3d testPos = new Vector3d();
        while (positionOK == false) {
            positionOK = true;

            rand = Math.random();
            signx = 1.;
            signy = 1.;
            if (rand > 0.5) signx = -1.0;
            rand = Math.random();
            if (rand > 0.5) signy = -1.0;
            testPos.set(new Vector3d(signx * Math.random() * radius, signy * Math.random() * radius, 0.));

            Collection elements = ((EMEngine)theEngine).getPhysicalObjs();
            Iterator myIterator = elements.iterator();
            int i = 0;
            while (myIterator.hasNext() == true) {
				if (i > 500)
				{
					//Give up
					TDebug.println(0,"addRandomCharge() : Could not find suitable position!");
					break;
				}
				
                Vector3d r = new Vector3d();
                Object myObject = myIterator.next();
                if (myObject instanceof FiniteWire) {
                    r.set(((PhysicalObject) myObject).getPosition());
                    r.sub(testPos);
                    double dist = r.length();
                    if (dist <= tolerance) {
                        positionOK = false;
                        break;
                    }
                }
                i++;
            }
        }
        testPos.add(offset);
        newLineCurrent.setPosition(testPos);
        return newLineCurrent;
    }

    /** Method to delete all line currents from the scene.  */
    private void clearAllLineCurrents() {
        int simstate = mSEC.getSimState();
        mSEC.stop();
        Collection elements = ((EMEngine)theEngine).getPhysicalObjs();
        Iterator it = elements.iterator();
        while (it.hasNext()) {
            Object em = it.next();
            if (em instanceof FiniteWire) {
                removeElement((FiniteWire) em);
            }
        }
        if (simstate == TEngineControl.RUNNING)
            mSEC.start();
        else theEngine.requestRefresh();
    }
    
    /** Method to make field for the circle or rectangle visible or not.    */
    private void MakeFieldVisible(boolean fieldvisible) {
    	for (int i = 1; i<=8; i++){
    		theField[i].setDrawn(fieldvisible);
    	}
    }
    
    /** Method to calculate the current thru through the Amperean circle or rectangle. */
    private void calculateCurrentThru(){
    	
    	// Interate over each point line current in the scene and see if it is inside the rectangle, if so increment currentThruRectangle
    	
	    double currentThruRectangle = 0.;
	    Collection elements = ((EMEngine)theEngine).getPhysicalObjs();
	    Iterator myIterator = elements.iterator();
	    while (myIterator.hasNext() == true) {			
	        Object myObject = myIterator.next();
	        if (myObject instanceof FiniteWire) {
	        	Vector3d r = new Vector3d();
	        	double myCurrent = 0.;
	        	myCurrent = ((FiniteWire) myObject).getCurrent();
	            r.set(((PhysicalObject) myObject).getPosition());
	        	boolean insiderectangle = insideRectangle(r);
	        	if (insiderectangle)	currentThruRectangle = currentThruRectangle + myCurrent;
	        }
	    }
	    heightARectangleCurrentThru = oneUnitCurrentThru*(currentThruRectangle);
	    ShapeNodeARectangleCurrentThru.setGeometry(Cylinder.makeGeometry(32, radiusARectangleCurrentThru, heightARectangleCurrentThru));
	    AmpereanRectangleFlux.setNode3D(ShapeNodeARectangleCurrentThru);
	    AmpereanRectangleFlux.setPosition(new Vector3d(2.,-2.5+heightARectangleCurrentThru/2.,0));
	    
    	// Interate over each point line current in the scene and see if it is inside the circle, if so increment currentThruCircle
	    
	    double currentThruCircle = 0.;
	    myIterator = elements.iterator();
	    while (myIterator.hasNext() == true) {			
	       Object myObject = myIterator.next();
	       if (myObject instanceof FiniteWire) {
	       	Vector3d r = new Vector3d();
	       	double myCurrentThru = 0.;
	       	myCurrentThru = ((FiniteWire) myObject).getCurrent();
	           r.set(((PhysicalObject) myObject).getPosition());
	       	boolean insidecircle = insideCircle(r);
	       	if (insidecircle)	currentThruCircle = currentThruCircle + myCurrentThru;
	       }
	   }
	   heightACircleCurrentThru = oneUnitCurrentThru*(currentThruCircle);
	   ShapeNodeACircleCurrentThru.setGeometry(Cylinder.makeGeometry(32, radiusACircleCurrentThru, heightACircleCurrentThru));
	   AmpereanCircleCurrentThru.setNode3D(ShapeNodeACircleCurrentThru);
	   AmpereanCircleCurrentThru.setPosition(new Vector3d(2.,-2.5+heightACircleCurrentThru/2.,0));
	   theEngine.requestSpatial();
    } 
    
    /** Method to create a frame around scene using while lines.  */
    public void Outline() {
        Line one = new Line(new Vector3d(-screenWidth/2.,-screenWidth/2., 0.), new Vector3d(-screenWidth/2.,screenWidth/2., 0.));
        one.setColor(Color.white);
        addElement(one);
        Line two = new Line(new Vector3d(-screenWidth/2.,-screenWidth/2., 0.), new Vector3d(screenWidth/2.,-screenWidth/2., 0.));
        two.setColor(Color.white);
        addElement(two);
        Line three = new Line(new Vector3d(screenWidth/2.,screenWidth/2., 0.), new Vector3d(-screenWidth/2.,screenWidth/2., 0.));
        three.setColor(Color.white);
        addElement(three);
        Line four = new Line(new Vector3d(screenWidth/2.,screenWidth/2., 0.), new Vector3d(screenWidth/2.,-screenWidth/2., 0.));
        four.setColor(Color.white);
        addElement(four);
    }
}

