/* $Id: GaussLawFlux.java,v 1.4 2008/12/23 20:05:38 jbelcher Exp $ */
/**
 * @author John Belcher - Department of Physics / MIT
 * @version $Revision: 1.4 $
 */

package tealsim.physics.em;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.media.j3d.*;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.vecmath.*;

import teal.config.Teal;
import teal.core.AbstractElement;
import teal.core.HasReference;
import teal.core.TElement;
import teal.field.Field;
import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.render.Rendered;
import teal.render.geometry.Cylinder;
import teal.render.geometry.Pipe;
import teal.render.geometry.Sphere;
import teal.render.j3d.*;
import teal.render.j3d.loaders.Loader3DS;
import teal.render.primitives.Line;
import teal.render.scene.TShapeNode;
import teal.render.viewer.SelectEvent;
import teal.render.viewer.SelectListener;
import teal.sim.TSimElement;
import teal.sim.collision.SphereCollisionController;
import teal.sim.control.VisualizationControl;
import teal.sim.engine.TEngineControl;
import teal.sim.spatial.FieldConvolution;
import teal.sim.spatial.FieldDirectionGrid;
import teal.sim.spatial.FieldVector;
import teal.sim.spatial.GeneralVector;
import teal.sim.spatial.RelativeFLine;
import teal.sim.spatial.SpatialTextLabel;
import teal.math.RectangularPlane;
import teal.physics.em.EMEngine;
import teal.physics.em.PointCharge;
import teal.physics.em.InfiniteLineCharge;
import teal.physics.em.SimEM;
import teal.physics.physical.PhysicalObject;
import teal.physics.physical.Wall;
import teal.ui.UIPanel;
import teal.ui.control.*;
import teal.util.TDebug;
import teal.visualization.dlic.DLIC;


/** An application to show the normals and electric fields on the surface of a 
 * closed surface to make clear the geometric concepts behind Gauss's Law.  
 *  
 * @author John Belcher
 * @version 1.0 
 * */

public class filledCylinderShell extends SimEM  {
	/** This is for serialized version, presently not used.  *\
    private static final long serialVersionUID = 3257008735204554035L;
    

    
    //  USER INTERFACE CONTROLS  //
    
	/** ButtonGroup for the two choices of Gaussian surfaces.  */
	ButtonGroup chooseGaussianSurfaceOptionsGroup;
	/** ButtonGroup for the two ways to show the magnitude of E.  */
	ButtonGroup chooseHowToScaleEOptionsGroup;
	/** Radio button for choosing no Gaussian surface.  */
	JRadioButton showNoGaussianButton;
	/** Radio button for choosing the Cylindrical Gaussian surface.  */
	JRadioButton showGaussianCylinderButton;
	/** Radio button for choosing the Spherical Gaussian surface.  */
    JRadioButton showGaussianSphereButton;       
    /** Button to reset original camera view. */
    JButton resetViewButton = null;
    /** Button to reset Gaussian shapes to zero. */
    JButton resetGaussButton = null;
    
    // pipe for outer cylinder 
    Rendered ring,ring1,ring2;

    /** Array list to do with objects selected in scene. */
    private ArrayList selectList = new ArrayList(); 
    /** Field line convolution of the electric field in the scene due to all the charges.  */
    protected FieldConvolution mDLIC = null;
    /** Add charge to scene control group.  */
    ControlGroup addChargeControlGroup;
    /** Button to add a positive charge to scene.  */
    JButton addPosChargeButton = null;
    /** Button to add a negative charge to scene.  */
    JButton addNegChargeButton = null;
    /** Button to delete all charges presently in scene.  */
    JButton deleteAllChargesButton = null;
    /** Control position and orientation of Gaussian surface control group.  */
    ControlGroup PositionOrientationControlGroup;
    /** Reset view control group.  */
    ControlGroup resetViewControlGroup;
    /** Choose desired Gaussian surface (sphere or cylinder) control group.  */
    ControlGroup gaussianSurfaceControlGroup;
    /** Control group for how to scale E field choices. */
    ControlGroup scaleByMagnitudeControlGroup;
	/** Radio button for choosing scale E vectors by magnitude.  */
    JRadioButton scaleEbyMagnitudeButton;    
	/** Radio button for choosing same length E vectors.  */
    JRadioButton normalizeEMagnitudeToUnityButton;    
    
    /** Visualization control for LIC visualization. LIC stands for line integral convolution.  */
    VisualizationControl LICvisualizationControl;
    /** Logical value for whether sphere is visible. */
    boolean sphereVisible = false;;
    /** Logical value for whether cylinder is visible. */
    boolean cylinderVisible = false;
    /** Logical value for whether there are any point charges in the scene. */
    boolean chargesPresent = true;
    /** Number of charges in the scene, set to 2 for startup. */
    int numberCharges = 2;
    
    //  Flux meter properties 
     
     /** Base for the flux meter. */
     Wall baseFluxMeter;
     /** Text label for the flux meter.*/
 	 SpatialTextLabel fluxmeterbaseLabel;
     /** The size of the square for the base of the flux meter.  */
     double fluxMeterSizeBase = 1.2;
     /** The height of the flux meter cylinder for the flux due to one unit of charge. */
     double oneUnitFlux = 0.5;
    
    /** The approximate width of the canvas in the standard view. We use this to draw a box
     * around the LIC panel, which consists of four white lines, for contrast/appearance reasons.  */
    double screenWidth = 6.;
    /** The scale factor for the electric field vectors on our surfaces. Increasing this value makes
     * the field vectors larger.  */
    double arrowScaleEfield = .6;
    /** The scale factor for the surface normal vectors.  Increasing this value makes the normal vectors larger.  */
    double arrowScaleNormal = .3;
    
    // Field magnitude display properties 
    
    /** Boolean for whether we normalize E (false) or whether we allow the magnitude to show to some extent (true). 
     * The extent to which the magnitude is shown is determined by the variables normMagnitudeFactor and 
     * powerMagnitudeScale.  */
    boolean scaleByMagnitudeBoolean = true;
    /** If we scale by magnitude, the normalization factor we use. */
    double normMagnitudeFactor = .5;
    /** If we scale by magnitude, the power of the magnitude we let the scale vary by (less than or equal to 1). */
    double powerMagnitudeScale = 1.;
    
    //  Gaussian sphere properties  
    
    /** A TEALsim native object for the Gaussian sphere.  */
    Rendered GaussianSphere = new Rendered();
    /** A ShapeNode for the Gaussian sphere.  */
    ShapeNode ShapeNodeGSphere = new ShapeNode();
    /** A TEALsim native object for the flux through the Gaussian sphere.  */
    Rendered GaussianSphereFlux = new Rendered();
    /** A ShapeNode for the flux through the Gaussian Sphere.  */
    ShapeNode ShapeNodeGSphereFlux = new ShapeNode();
    /** Vector for the initial position of the Gaussian sphere. */
    Vector3d posGSphere = null;
    /** The radius of the Gaussian sphere.  */
    double radiusGSphere = 1.0;
    /** The height of the cylinder representing the flux through the Gaussian sphere.  */
    double heightGSphereFlux = oneUnitFlux;
    /** The radius of the cylinder representing the flux through the Gaussian sphere.  */
    double radiusGSphereFlux = .5;
    /** Vector for the initial position of the cylinder representing flux through the sphere. */
    Vector3d posGSphereFlux = null;

    //  Gaussian cylinder properties
    
    /** A TEALsim native object for the Gaussian cylinder.  */
    Rendered GaussianCylinder = new Rendered();
    /** A ShapeNode for the Gaussian Cylinder.  */
    ShapeNode ShapeNodeGCylinder = new ShapeNode();
    /** A TEALsim native object for the flux through the Gaussian cylinder.  */
    Rendered GaussianCylinderFlux = new Rendered();
    /** A ShapeNode for the flux through the Gaussian Cylinder.  */
    ShapeNode ShapeNodeGCylinderFlux = new ShapeNode();
    /** The height of the cylinder representing the flux through the Gaussian sphere.  */
    double heightGCylinderFlux = 1.;
    /** The radius of the cylinder representing the flux through the Gaussian sphere.  */
    double radiusGCylinderFlux = .5;
    /** The radius of the Gaussian cylinder.  */
    double radiusGCylinder = .6;
    /** The height of the Gaussian cylinder.  */
    double heightGCylinder = 3.;
    /** The angle from the x axis in the xy plane of the Gaussian cylinder. */
    double angleGCylinder = 0.;
    
    // Controls for position and orientation of Gaussian surface
    
    /** Slider for the y-position of the Gaussian surface (cylinder or sphere).  */
    PropertyDouble posSlider_y = new PropertyDouble();
    /** Slider for the x-position of the Gaussian surface (cylinder or sphere).  */
    PropertyDouble posSlider_x = new PropertyDouble();
    /** Slider for the rotation angle of the Gaussian surface (cylinder or sphere).  */
    PropertyDouble radiusGaussianSurface = new PropertyDouble();
    /** Vector for the initial position of the cylinder. */
    Vector3d posGCylinder = null;
    /** Vector for the initial position of the cylinder representing flux through the cylinder. */
    Vector3d posGCylinderFlux = null;
    
    //  Field and normal vectors on the sphere and their spatial distribution 
    
    /** The electric field vectors on the sphere. */
    FieldVector[][] theFieldSphere;
    /** The normal vectors on the sphere. */
    GeneralVector[][] theNormalSphere;
    /** The number of azimuth angle nodes on the sphere. */
    int numAziSphere = 8;
    /** The number of polar angle nodes on the sphere. */
    int numThetaSphere = 6;
   
    //   Field and normal vectors on the cylinder and their spatial distribution 
    
    /** The electric field vectors on the top of the cylinder. */
    FieldVector[][] theFieldCylinderTop;
    /** The electric field vectors on the bottom of the cylinder. */
    FieldVector[][] theFieldCylinderBottom;
    /** The electric field vectors on the sides of the cylinder. */
    FieldVector[][] theFieldCylinderSides;
    /** The electric field vectors on the top of the cylinder. */
    GeneralVector[][] theNormalCylinderTop;
    /** The electric field vectors on the bottom of the cylinder. */
    GeneralVector[][] theNormalCylinderBottom;
    /** The electric field vectors on the sides of the cylinder. */
    GeneralVector[][] theNormalCylinderSides;
    /** The number of radial nodes for the electric field vectors on the top and bottom of the cylinder. */
    int numRadTopBottomCylinder = 1;
    /** The number of azimuthal nodes for the electric field vectors on the top and bottom of the cylinder.  */
    int numAziTopBottomCylinder = 4;
    /** The number of azimuthal nodes for the electric field vectors on the sides of the cylinder. */
    int numAziSidesCylinder = 8;
    /** The number of z nodes for the electric field vectors on the sides the cylinder.  */
    int numZSidesCylinder = 4;

    public filledCylinderShell() {
        super();
        // Hide run controls       
        mSEC.rebuildPanel(0);
        // Set debug level
        TDebug.setGlobalLevel(0);
        title = "Filled Cylindrical Shell of Charge";
        mViewer.setShowGizmos(false);
        mDLIC = new FieldConvolution();
        mDLIC.setSize(new Dimension(512, 512));
        mDLIC.setComputePlane(new RectangularPlane(new BoundingSphere(new Point3d(), 3.)));

        addChargeControlGroup = new ControlGroup();
        addChargeControlGroup.setText("Add Charges (Maximum Six Charges)");
        
        resetViewControlGroup = new ControlGroup();
        resetViewControlGroup.setText("Reset Position of Gaussian Surfaces and View");

        mViewer.setCursorOnDrag(false);
       
        // Create the Gaussian Sphere using teal.render.geometry and add it to the scene
        
        posGSphere = new Vector3d(0.,0.,0);
        ShapeNodeGSphere.setGeometry(Sphere.makeGeometry(32, radiusGSphere));
        ShapeNodeGSphere.setTransparency(0.5f);
        GaussianSphere.setNode3D(ShapeNodeGSphere);
        GaussianSphere.setColor(new Color(0, 0, 170));
        GaussianSphere.setPosition(posGSphere);
        GaussianSphere.setDirection(new Vector3d(0.,1.,0.));
        GaussianSphere.setDrawn(false);
        addElement(GaussianSphere);
        
        // do outer cylinder 
        double thickness2 = 0.05;
        double radius2 = 2.5;
        ring = new Rendered();
        TShapeNode node = (TShapeNode) new ShapeNode();
        node.setGeometry(Pipe.makeGeometry(50, radius2 - thickness2 / 2., thickness2, 1.));
        node.setPickable(false);
        ring.setDirection(new Vector3d(0., 0., 1.));
        node.setColor(new Color3f(new Color(154,105,0)));
        node.setTransparency(0.8f);
        ring.setNode3D(node);
        addElement(ring);
        ring2 = new Rendered();
        TShapeNode node2 = (TShapeNode) new ShapeNode();
        node2.setGeometry(Pipe.makeGeometry(50, radius2 - thickness2 / 2., thickness2, 100.));
        node2.setPickable(false);
        ring2.setDirection(new Vector3d(0., 0., 1.));
        node2.setColor(new Color3f(new Color(154,105,0)));
        node2.setTransparency(0.95f);
        ring2.setNode3D(node2);
        addElement(ring2);
        double radius1= 1.5;
        ring1 = new Rendered();
        TShapeNode node1 = (TShapeNode) new ShapeNode();
        node1.setGeometry(Pipe.makeGeometry(50, radius1 - thickness2 / 2., thickness2, 100.));
        node1.setPickable(false);
        ring1.setDirection(new Vector3d(0., 0., 1.));
        node1.setColor(new Color3f(new Color(154,105,0)));
        node1.setTransparency(0.95f);
        ring1.setNode3D(node1);
        addElement(ring1);

    
        // Create the electric field and normal vectors on the sphere 
        
        theFieldSphere = new FieldVector[numAziSphere][numThetaSphere];
        theNormalSphere = new GeneralVector[numAziSphere][numThetaSphere];
        for (int j = 0; j < numThetaSphere; j++) {
        	for (int i = 0; i < numAziSphere; i++) {
	     		theFieldSphere[i][j] = new FieldVector();
	     		theFieldSphere[i][j].setPosition(new Vector3d(0,0,0));
	     		theFieldSphere[i][j].setColor(Teal.DefaultEFieldColor);
	     		theFieldSphere[i][j].setArrowScale(arrowScaleEfield);
	     		theFieldSphere[i][j].setDrawn(false);
	     		theFieldSphere[i][j].setScaleByMagnitude(scaleByMagnitudeBoolean);
	     		theFieldSphere[i][j].setDrawn(false);
	     		theFieldSphere[i][j].setScaleByMagnitude(scaleByMagnitudeBoolean);
	     		theFieldSphere[i][j].setNormFactor(normMagnitudeFactor);
	     		theFieldSphere[i][j].setPowerScale(powerMagnitudeScale);
	     		addElement(theFieldSphere[i][j]);
	     		theNormalSphere[i][j] = new GeneralVector();
	     		theNormalSphere[i][j].setPosition(new Vector3d(0,0,0));
	     		theNormalSphere[i][j].setColor(new Color(0, 0, 170));
	     		theNormalSphere[i][j].setArrowScale(arrowScaleNormal);
	     		theNormalSphere[i][j].setColor(new Color(0, 0, 170));
	     		theNormalSphere[i][j].setDrawn(false);
	     		addElement(theNormalSphere[i][j]);
        	} 	
        }
        
        // Create the Cylinder representing the flux through the sphere using teal.render.geometry and add it to the scene
        
        Vector3d posGSphereFluxBase;
        posGSphereFluxBase =  new Vector3d(2.,-2.5,0);

        posGSphereFlux=  new Vector3d(2.,-2.5,0);
        Vector3d posGSphereFluxLabel  = new Vector3d(1.,-2.8,0);
        ShapeNodeGSphereFlux.setGeometry(Cylinder.makeGeometry(32, radiusGSphereFlux, heightGSphereFlux));
        ShapeNodeGSphereFlux.setTransparency(0.3f);
        GaussianSphereFlux.setNode3D(ShapeNodeGSphereFlux);
        GaussianSphereFlux.setColor(new Color(255, 0, 0));
        GaussianSphereFlux.setPosition(posGSphereFlux);
        GaussianSphereFlux.setDirection(new Vector3d(0.,1.,0.));
        GaussianSphereFlux.setDrawn(false);
        addElement(GaussianSphereFlux);
        
		fluxmeterbaseLabel = new SpatialTextLabel(" What is this? ", posGSphereFluxLabel);
		fluxmeterbaseLabel.setBaseScale(0.2);
		fluxmeterbaseLabel.setPositionOffset(new Vector3d(0.05, 0.0, 0.));
		fluxmeterbaseLabel.setRefDirectionOffset(0.5);
		fluxmeterbaseLabel.setUseDirectionOffset(true);
		addElement(fluxmeterbaseLabel);
        
		// Create the base for the flux meter       
		
        baseFluxMeter = new Wall(posGSphereFluxBase,new Vector3d(fluxMeterSizeBase, 0, 0.), new Vector3d(0, 0., fluxMeterSizeBase));
        addElement(baseFluxMeter);
        
        // Create the Gaussian Cylinder using teal.render.geometry and add them to the scene
        
        posGCylinder = new Vector3d(0.,0.,0);
        ShapeNodeGCylinder.setGeometry(Cylinder.makeGeometry(32, radiusGCylinder, heightGCylinder));
        ShapeNodeGCylinder.setTransparency(0.5f);
        GaussianCylinder.setNode3D(ShapeNodeGCylinder);
        GaussianCylinder.setColor(new Color(0, 0, 170));
        GaussianCylinder.setPosition(posGCylinder);
        GaussianCylinder.setDirection(new Vector3d(0.,0.,1.));
        GaussianCylinder.setDrawn(false);
        addElement(GaussianCylinder);
        
        //  Create the electric field and normal vectors on the top and bottom of the cylinder  
        
        theFieldCylinderTop = new FieldVector[numAziTopBottomCylinder][numRadTopBottomCylinder];
        theFieldCylinderBottom = new FieldVector[numAziTopBottomCylinder][numRadTopBottomCylinder];
        theNormalCylinderTop = new GeneralVector[numAziTopBottomCylinder][numRadTopBottomCylinder];
        theNormalCylinderBottom = new GeneralVector[numAziTopBottomCylinder][numRadTopBottomCylinder];
        for (int j = 0; j < numRadTopBottomCylinder; j++) {
        	for (int i = 0; i < numAziTopBottomCylinder; i++) {
	     		theFieldCylinderTop[i][j] = new FieldVector();
	     		theFieldCylinderTop[i][j].setPosition(new Vector3d(0,0,0));
	     		theFieldCylinderTop[i][j].setColor(Teal.DefaultEFieldColor);
	     		theFieldCylinderTop[i][j].setArrowScale(arrowScaleEfield);
	     		theFieldCylinderTop[i][j].setDrawn(false);
	     		theFieldCylinderTop[i][j].setScaleByMagnitude(scaleByMagnitudeBoolean);
	     		theFieldCylinderTop[i][j].setNormFactor(normMagnitudeFactor);
	     		theFieldCylinderTop[i][j].setPowerScale(powerMagnitudeScale);
	     		addElement(theFieldCylinderTop[i][j]);
	     		theFieldCylinderBottom[i][j] = new FieldVector();
	     		theFieldCylinderBottom[i][j].setPosition(new Vector3d(0,0,0));
	     		theFieldCylinderBottom[i][j].setColor(Teal.DefaultEFieldColor);
	     		theFieldCylinderBottom[i][j].setArrowScale(arrowScaleEfield);
	     		theFieldCylinderBottom[i][j].setDrawn(false);
	     		theFieldCylinderBottom[i][j].setScaleByMagnitude(scaleByMagnitudeBoolean);
	     		theFieldCylinderBottom[i][j].setNormFactor(normMagnitudeFactor);
	     		theFieldCylinderBottom[i][j].setPowerScale(powerMagnitudeScale);
	            addElement(theFieldCylinderBottom[i][j]);
	     		theNormalCylinderTop[i][j] = new GeneralVector();
	     		theNormalCylinderTop[i][j].setPosition(new Vector3d(0,0,0));
	     		theNormalCylinderTop[i][j].setColor(new Color(0, 0, 170));
	     		theNormalCylinderTop[i][j].setArrowScale(arrowScaleNormal);
	     		theNormalCylinderTop[i][j].setDrawn(false);
	     		addElement(theNormalCylinderTop[i][j]);
	     		theNormalCylinderBottom[i][j] = new GeneralVector();
	     		theNormalCylinderBottom[i][j].setPosition(new Vector3d(0,0,0));
	     		theNormalCylinderBottom[i][j].setColor(new Color(0, 0, 170));
	     		theNormalCylinderBottom[i][j].setArrowScale(arrowScaleNormal);
	     		theNormalCylinderBottom[i][j].setDrawn(false);
	            addElement(theNormalCylinderBottom[i][j]);
        	} 	
        }
        
        //  Create the electric field vectors and normal vectors on the sides of the cylinder  
        
        theFieldCylinderSides = new FieldVector[numAziSidesCylinder][numZSidesCylinder];
        theNormalCylinderSides = new GeneralVector[numAziSidesCylinder][numZSidesCylinder];
        for (int j = 0; j < numZSidesCylinder; j++) {
        	for (int i = 0; i < numAziSidesCylinder; i++) {
	     		theFieldCylinderSides[i][j] = new FieldVector();
	     		theFieldCylinderSides[i][j].setPosition(new Vector3d(0,0,0));
	     		theFieldCylinderSides[i][j].setColor(Teal.DefaultEFieldColor);
	     		theFieldCylinderSides[i][j].setArrowScale(arrowScaleEfield);
	     		theFieldCylinderSides[i][j].setDrawn(false);
	     		theFieldCylinderSides[i][j].setScaleByMagnitude(scaleByMagnitudeBoolean);
	     		theFieldCylinderSides[i][j].setNormFactor(normMagnitudeFactor);
	     		theFieldCylinderSides[i][j].setPowerScale(powerMagnitudeScale);
	        	Transform3D offsetTrans = new Transform3D();
	    		theFieldCylinderSides[i][j].setModelOffsetTransform(offsetTrans);
	     		addElement(theFieldCylinderSides[i][j]);
	     		theNormalCylinderSides[i][j] = new GeneralVector();
	     		theNormalCylinderSides[i][j].setPosition(new Vector3d(0,0,0));
	     		theNormalCylinderSides[i][j].setArrowScale(arrowScaleNormal);
	     		theNormalCylinderSides[i][j].setDrawn(false);
	     		theNormalCylinderSides[i][j].setColor(new Color(0, 0, 170));
	     		addElement(theNormalCylinderSides[i][j]);
        	} 	
        }
        
        
        // Create the Cylinder representing the flux thorugh the cylinder using teal.render.geometry and add it to the scene

        ShapeNodeGCylinderFlux.setGeometry(Cylinder.makeGeometry(32, radiusGCylinderFlux, heightGCylinderFlux));
        ShapeNodeGCylinderFlux.setTransparency(0.3f);
        GaussianCylinderFlux.setNode3D(ShapeNodeGCylinderFlux);
        GaussianCylinderFlux.setColor(new Color(255, 0, 0));
        GaussianCylinderFlux.setPosition(posGSphereFlux);
        GaussianCylinderFlux.setDirection(new Vector3d(0.,1.,0.));
        GaussianCylinderFlux.setDrawn(false);
        addElement(GaussianCylinderFlux);
        
        // Put the E field vectors and the normals in their proper places.  
        
        PlaceENVectors();
        
        // Build interface panels
        
        UIPanel chooseGaussianSurfaceUIPanel = new UIPanel();
        chooseGaussianSurfaceUIPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        chooseGaussianSurfaceUIPanel.setLayout(new GridLayout(2,1));
        chooseGaussianSurfaceOptionsGroup = new ButtonGroup();
        UIPanel scaleEMagnitudeUIPanel = new UIPanel();
        scaleEMagnitudeUIPanel.setBorder(BorderFactory.createLineBorder(Color.black));
        scaleEMagnitudeUIPanel.setLayout(new GridLayout(2,1));
        chooseHowToScaleEOptionsGroup = new ButtonGroup();
        showNoGaussianButton = new JRadioButton("Show No Gaussian Surface");  
        showGaussianCylinderButton = new JRadioButton("Show Gaussian Cylinder");        
        showGaussianSphereButton = new JRadioButton("Show Gaussian Sphere");    
        scaleEbyMagnitudeButton = new JRadioButton("Scale E Arrow Length by (Magnitude E)^0.3"); 
        normalizeEMagnitudeToUnityButton = new JRadioButton("Make All E Arrow Lengths the Same"); 
        showNoGaussianButton.setSelected(true);
        showGaussianCylinderButton.setSelected(false);
        normalizeEMagnitudeToUnityButton.setSelected(true);
        showGaussianCylinderButton.addActionListener(this);
        showNoGaussianButton.addActionListener(this);
        showGaussianSphereButton.addActionListener(this);
        scaleEbyMagnitudeButton.addActionListener(this);
        normalizeEMagnitudeToUnityButton.addActionListener(this);
		chooseGaussianSurfaceOptionsGroup.add(showNoGaussianButton);
		chooseGaussianSurfaceOptionsGroup.add(showGaussianSphereButton);
		chooseGaussianSurfaceOptionsGroup.add(showGaussianCylinderButton);
		chooseGaussianSurfaceUIPanel.add(showNoGaussianButton);
		chooseGaussianSurfaceUIPanel.add(showGaussianSphereButton);  
		chooseGaussianSurfaceUIPanel.add(showGaussianCylinderButton);
		chooseHowToScaleEOptionsGroup.add(normalizeEMagnitudeToUnityButton);
		chooseHowToScaleEOptionsGroup.add(scaleEbyMagnitudeButton);

		scaleEMagnitudeUIPanel.add(normalizeEMagnitudeToUnityButton);
		scaleEMagnitudeUIPanel.add(scaleEbyMagnitudeButton);   

	    scaleByMagnitudeControlGroup= new ControlGroup();
	    scaleByMagnitudeControlGroup.add(scaleEMagnitudeUIPanel);
        scaleByMagnitudeControlGroup.setText("Choose E Field Scaling");
        gaussianSurfaceControlGroup= new ControlGroup();
        gaussianSurfaceControlGroup.add(chooseGaussianSurfaceUIPanel);
        gaussianSurfaceControlGroup.setText("Choose Gaussian Surface");

        mViewer.setCursorOnDrag(false);
        
        // Create the two sliders for the Gaussian cylinder position  
        
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
        
        // Create the angle orientation slider for the cylinder, where angle is the angle from the x axis    
        
        radiusGaussianSurface.setText("Radius");
        radiusGaussianSurface.setMinimum(.1);
        radiusGaussianSurface.setMaximum(3.);
        radiusGaussianSurface.setPaintTicks(true);
        radiusGaussianSurface.addPropertyChangeListener("value", this);
        radiusGaussianSurface.setValue(2.);
        radiusGaussianSurface.setVisible(true);
       
        // Add the sliders to the control group and add the control group to the scene
        
        PositionOrientationControlGroup = new ControlGroup();
        PositionOrientationControlGroup.setText("Gaussian Surface Radius and Position");
        PositionOrientationControlGroup.add(radiusGaussianSurface);
        PositionOrientationControlGroup.add(posSlider_x);
        PositionOrientationControlGroup.add(posSlider_y);
        
        addElement(resetViewControlGroup);
        addElement(gaussianSurfaceControlGroup);
        addElement(PositionOrientationControlGroup);
  //     addElement(scaleByMagnitudeControlGroup);
  //      addElement(addChargeControlGroup);

        LICvisualizationControl = new VisualizationControl();
        LICvisualizationControl.setText("Field Visualization");
        LICvisualizationControl.setFieldConvolution(mDLIC);
        LICvisualizationControl.setConvolutionModes(DLIC.DLIC_FLAG_E );
  //      addElement(LICvisualizationControl);
        
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
        
        // Add one initial positive and one negative charge to begin with
        
     //   addElement(randomCharge(1.0, 2.0, 2.5, new Vector3d(0, 0, 0)));
     //   addElement(randomCharge(-1.0, 2.0, 2.5, new Vector3d(0, 0, 0)));
        
        InfiniteLineCharge newLineCharge = new InfiniteLineCharge();
        newLineCharge.setCharge(1.);
        newLineCharge.setMass(1.);
        newLineCharge.setRadius(0.2);
        newLineCharge.setSelectable(true);
        newLineCharge.setPickable(false);
        newLineCharge.setColliding(true);
        newLineCharge.addPropertyChangeListener(this);
        addElement(newLineCharge);
        calculateFlux();
        
 // Set initial state
        
        mSEC.init();  
        theEngine.requestRefresh();
        mSEC.setVisible(true);
        reset();
        resetCamera();
        GaussianCylinderFlux.setDrawn(false);
  }


    /** Method to add actions to application.  The actions added are two entries on the pull down help menu and the
     * buttons for adding a positive or negative charge and for deleting all charges. 
     */
    void addActions() {
        
        TealAction tb = new TealAction("Filled Shell", this);
   //     addAction("Help", tb);
        
        TealAction ta = new TealAction("Execution & View", this);
        addAction("Help", ta);

        deleteAllChargesButton = new JButton(new TealAction("Delete All Charges", "delete_all", this));
        deleteAllChargesButton.setBounds(250, 650, 195, 24);

        addPosChargeButton = new JButton(new TealAction("Add Random Positive (orange)", "add_random_positive", this));
        addPosChargeButton.setBounds(40, 690, 195, 24);

        addNegChargeButton = new JButton(new TealAction("Add Random Negative (blue)", "add_random_negative", this));
        addNegChargeButton.setBounds(250, 690, 195, 24);
        
        resetViewButton = new JButton(new TealAction("Reset View", "resetview", this));
        resetViewButton.setBounds(40, 730, 195, 24);
        
        resetGaussButton = new JButton(new TealAction("Reset Gaussian Shape Position", "resetgauss", this));
        resetGaussButton.setBounds(40, 730, 195, 24);
       
        addChargeControlGroup.add(addPosChargeButton);
        addChargeControlGroup.add(addNegChargeButton);
        addChargeControlGroup.add(deleteAllChargesButton);
        resetViewControlGroup.add(resetGaussButton);
        resetViewControlGroup.add(resetViewButton);

        
    }


  /** Method to handle the action performed events.  */  
    public void actionPerformed(ActionEvent e) {
        TDebug.println(1, " Action comamnd: " + e.getActionCommand());
        if (e.getActionCommand().compareToIgnoreCase("Filled Shell") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).openBrowser("help/filledcylindricalshell.html");
        	}
        }
        else if (e.getActionCommand().compareToIgnoreCase("Execution & View") == 0) 
        {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).openBrowser("help/executionView.html");
        	}
     
	    } else if (e.getActionCommand().compareToIgnoreCase("add_random_positive") == 0) {
	        if(numberCharges < 6 ) addElement(randomCharge(1.0, 2.0, 2.5, new Vector3d(0, 0, 0)));
	        numberCharges = numberCharges +1;
	        if(numberCharges == 6) {
	        	addPosChargeButton.setEnabled(false);
	        	addNegChargeButton.setEnabled(false);
	        }
	        chargesPresent = true;
	        calculateFlux();
	        MakeFieldVisible(true);
	        
	    } else if (e.getActionCommand().compareToIgnoreCase("add_random_negative") == 0) {
	        if(numberCharges < 6 ) addElement(randomCharge(-1.0, 2.0, 2.5, new Vector3d(0, 0, 0)));
	        numberCharges = numberCharges +1;
	        if(numberCharges == 6) {
	        	addPosChargeButton.setEnabled(false);
	        	addNegChargeButton.setEnabled(false);
	        }
	        chargesPresent = true;
	        calculateFlux();
	        MakeFieldVisible(true);
	        
	    } else if (e.getActionCommand().compareToIgnoreCase("delete_all") == 0) {
	        clearAllCharges();
	        chargesPresent = false;
	        MakeFieldVisible(false);
	        addPosChargeButton.setEnabled(true);
	        addNegChargeButton.setEnabled(true);
	        numberCharges = 0;
	       // this is a hack
	        heightGSphereFlux = 0.;
	        ShapeNodeGSphereFlux.setGeometry(Cylinder.makeGeometry(32, radiusGSphereFlux, heightGSphereFlux));
	        GaussianSphereFlux.setNode3D(ShapeNodeGSphereFlux);
	        GaussianSphereFlux.setPosition(new Vector3d(2.,-2.5+heightGSphereFlux/2.,0));
	        theEngine.requestSpatial();
	        theEngine.requestRefresh();
	        
	    } else if (e.getActionCommand().compareToIgnoreCase("resetview") == 0) {
	        resetCamera();
	        theEngine.requestSpatial();
	        
	    } else if (e.getActionCommand().compareToIgnoreCase("resetgauss") == 0) {
            posGCylinder=new Vector3d(0.,0.,0.);
            GaussianCylinder.setNode3D(ShapeNodeGCylinder);
            GaussianCylinder.setPosition(posGCylinder);
            GaussianCylinderFlux.setDrawn(true);
            GaussianCylinderFlux.setNode3D(ShapeNodeGCylinderFlux);

            posGSphere=new Vector3d(0.,0.,0.);
            GaussianSphere.setNode3D(ShapeNodeGSphere);
            GaussianSphere.setPosition(posGSphere);
            posSlider_x.setValue(0.);
            posSlider_y.setValue(0.);
	        theEngine.requestSpatial();
	        theEngine.requestRefresh();

	    } else if(e.getSource() == showNoGaussianButton){
            GaussianSphere.setDrawn(false);	
            GaussianSphereFlux.setDrawn(false);
            sphereVisible = false;
            for (int j = 0; j < numThetaSphere; j++) {
            	for (int i = 0; i < numAziSphere; i++) {
            		if(chargesPresent) theFieldSphere[i][j].setDrawn(false); else theFieldSphere[i][j].setDrawn(false);
    	     		theNormalSphere[i][j].setDrawn(false);
            	} 	
            }
            GaussianCylinderFlux.setDrawn(false);
            GaussianCylinder.setDrawn(false);	
            cylinderVisible = false;
            for (int j = 0; j < numRadTopBottomCylinder; j++) {
            	for (int i = 0; i < numAziTopBottomCylinder; i++) {
    	     		theFieldCylinderTop[i][j].setDrawn(false);
    	     		theFieldCylinderBottom[i][j].setDrawn(false);
    	     		theNormalCylinderTop[i][j].setDrawn(false);
    	     		theNormalCylinderBottom[i][j].setDrawn(false);
            	} 	
            }
            for (int j = 0; j < numZSidesCylinder; j++) {
            	for (int i = 0; i < numAziSidesCylinder; i++) {
    	     		theFieldCylinderSides[i][j].setDrawn(false);
    	     		theNormalCylinderSides[i][j].setDrawn(false);
            	} 	
            }
            calculateFlux();
            
	    } else if(e.getSource() == showGaussianCylinderButton){
            GaussianCylinder.setDrawn(true);	
            posGCylinder.x=0;
            posGCylinder.y =0.;
            posGCylinder.z =0.;
       //     if((posGCylinder.x ==0.) && (posGCylinder.y==0.) ) GaussianCylinderFlux.setDrawn(true);
        //    GaussianCylinderFlux.setDrawn(true);
            cylinderVisible = true;
            for (int j = 0; j < numRadTopBottomCylinder; j++) {
            	for (int i = 0; i < numAziTopBottomCylinder; i++) {
    	     		if(chargesPresent) theFieldCylinderTop[i][j].setDrawn(true); else theFieldCylinderTop[i][j].setDrawn(false);
    	     		if(chargesPresent) theFieldCylinderBottom[i][j].setDrawn(true); else theFieldCylinderBottom[i][j].setDrawn(false);
    	     		theNormalCylinderTop[i][j].setDrawn(true);
    	     		theNormalCylinderBottom[i][j].setDrawn(true);
            	} 	
            }
            for (int j = 0; j < numZSidesCylinder; j++) {
            	for (int i = 0; i < numAziSidesCylinder; i++) {
            		if(chargesPresent) theFieldCylinderSides[i][j].setDrawn(true); else theFieldCylinderSides[i][j].setDrawn(false);
    	     		theNormalCylinderSides[i][j].setDrawn(true);
            	} 	
            }
            GaussianSphereFlux.setDrawn(false);
            GaussianSphere.setDrawn(false);	
            sphereVisible = false;
            for (int j = 0; j < numThetaSphere; j++) {
            	for (int i = 0; i < numAziSphere; i++) {
    	     		theFieldSphere[i][j].setDrawn(false);
    	     		theNormalSphere[i][j].setDrawn(false);
            	} 	
            }
            calculateFlux();
            
	    } else if(e.getSource() == showGaussianSphereButton){
            GaussianSphere.setDrawn(true);	
            GaussianSphereFlux.setDrawn(false);
            sphereVisible = true;
            for (int j = 0; j < numThetaSphere; j++) {
            	for (int i = 0; i < numAziSphere; i++) {
            		if(chargesPresent) theFieldSphere[i][j].setDrawn(true); else theFieldSphere[i][j].setDrawn(false);
    	     		theNormalSphere[i][j].setDrawn(true);
            	} 	
            }
            GaussianCylinderFlux.setDrawn(false);
            GaussianCylinder.setDrawn(false);	
            cylinderVisible = false;
            for (int j = 0; j < numRadTopBottomCylinder; j++) {
            	for (int i = 0; i < numAziTopBottomCylinder; i++) {
    	     		theFieldCylinderTop[i][j].setDrawn(false);
    	     		theFieldCylinderBottom[i][j].setDrawn(false);
    	     		theNormalCylinderTop[i][j].setDrawn(false);
    	     		theNormalCylinderBottom[i][j].setDrawn(false);
            	} 	
            }
            for (int j = 0; j < numZSidesCylinder; j++) {
            	for (int i = 0; i < numAziSidesCylinder; i++) {
    	     		theFieldCylinderSides[i][j].setDrawn(false);
    	     		theNormalCylinderSides[i][j].setDrawn(false);
            	} 	
            }
            calculateFlux();
            
        } else if(e.getSource() == scaleEbyMagnitudeButton){
            for (int j = 0; j < numThetaSphere; j++) {
            	for (int i = 0; i < numAziSphere; i++) {
            		theFieldSphere[i][j].setScaleByMagnitude(true);
            	} 	
            }
            for (int j = 0; j < numRadTopBottomCylinder; j++) {
            	for (int i = 0; i < numAziTopBottomCylinder; i++) {
    	     		theFieldCylinderTop[i][j].setScaleByMagnitude(true);
    	     		theFieldCylinderBottom[i][j].setScaleByMagnitude(true);
            	} 	
            }
            for (int j = 0; j < numZSidesCylinder; j++) {
            	for (int i = 0; i < numAziSidesCylinder; i++) {
    	     		theFieldCylinderSides[i][j].setScaleByMagnitude(true);
            	} 	
            }
            theEngine.requestRefresh();
            theEngine.requestSpatial();
            
        } else if(e.getSource() == normalizeEMagnitudeToUnityButton){
        	  for (int j = 0; j < numThetaSphere; j++) {
              	for (int i = 0; i < numAziSphere; i++) {
              		theFieldSphere[i][j].setScaleByMagnitude(false);
              	} 	
              }
              for (int j = 0; j < numRadTopBottomCylinder; j++) {
              	for (int i = 0; i < numAziTopBottomCylinder; i++) {
      	     		theFieldCylinderTop[i][j].setScaleByMagnitude(false);
      	     		theFieldCylinderBottom[i][j].setScaleByMagnitude(false);
              	} 	
              }
              for (int j = 0; j < numZSidesCylinder; j++) {
              	for (int i = 0; i < numAziSidesCylinder; i++) {
      	     		theFieldCylinderSides[i][j].setScaleByMagnitude(false);
              	} 	
              }
              theEngine.requestRefresh();
              theEngine.requestSpatial();
              
        } else {
            super.actionPerformed(e);
        }
    }

    /** Method to place the electric field vectors and normals on the cylinder and on the sphere. */
	public void PlaceENVectors() {
        double compx = Math.cos(angleGCylinder*Math.PI/180.);
        double compy = Math.sin(angleGCylinder*Math.PI/180.);
        
		// first place the vectors on the sphere
	    for (int j = 0; j < numThetaSphere; j++) {
	    	double cosvalue = (j+1)*2./(numThetaSphere*1.+1.)-1.;
	    	double acosangle = Math.acos(cosvalue);
	    	acosangle =  j*Math.PI/(numThetaSphere*1.-1.);
        	for (int i = 0; i < numAziSphere; i++) {
	    		double aziangle = i*2.*Math.PI/(numAziSphere*1.);  	
	    		Vector3d azidir = new Vector3d(Math.cos(acosangle),Math.cos(aziangle)*Math.sin(acosangle),Math.sin(aziangle)*Math.sin(acosangle));
	    		Vector3d azipos = new Vector3d(azidir);
	    		azipos.scale(radiusGSphere);
	    		Vector3d aziposTrans = new Vector3d(0,0,0);
	    		Vector3d azidirTrans = new Vector3d(0,0,0);
	    		aziposTrans.x = azipos.x*compx - azipos.y*compy;
	    		aziposTrans.y = azipos.x*compy + azipos.y*compx;
	    		aziposTrans.z = azipos.z;
	    		azidirTrans.x = azidir.x*compx - azidir.y*compy;
	    		azidirTrans.y = azidir.x*compy + azidir.y*compx;
	    		azidirTrans.z = azidir.z;
	    		aziposTrans.add(posGCylinder);
	     		theFieldSphere[i][j].setPosition(aziposTrans);
	     		theNormalSphere[i][j].setPosition(aziposTrans);
	     		theNormalSphere[i][j].setValue(azidirTrans);
	     		// here we make the field vector tip be at the location of the arrow if the arrow points inward at the local normal
	   //     	Transform3D offsetTrans = new Transform3D();
	   //  		double dot = theFieldSphere[i][j].getValue().dot(azidirTrans);
	        //	if ( dot > 0. ) offsetTrans.setTranslation(new Vector3d(0., 0., 0.));
	       // 	else offsetTrans.setTranslation(new Vector3d(0., -1.1, 0.));
     	//	theFieldCylinderSides[i][j].setModelOffsetTransform(offsetTrans);
        	} 	   	
        }	
		
	    // Now place the vectors on the top of the cylinder
	    
		Vector3d normalTop = null;
		Vector3d centerTop = new Vector3d(0,0,0);

        normalTop = new Vector3d(0., 0.,1.);
        normalTop.scale(heightGCylinder/2.);
		centerTop.add(normalTop);
		centerTop.add(posGCylinder);

        for (int j = 0; j < numRadTopBottomCylinder; j++) {
        	double rad = (j+1)*radiusGCylinder/(numRadTopBottomCylinder+1);
        	for (int i = 0; i < numAziTopBottomCylinder; i++) {
        		double aziangle = i*2.*Math.PI/(numAziTopBottomCylinder*1.);
        		Vector3d azipos = new Vector3d(Math.cos(aziangle),Math.sin(aziangle),0.);
        		Vector3d azidir = new Vector3d(0.,0.,1.);
        		azipos.scale(rad);
        		azipos.add(centerTop);
	     		theFieldCylinderTop[i][j].setPosition(azipos);
	     		theNormalCylinderTop[i][j].setPosition(azipos);
	     		theNormalCylinderTop[i][j].setValue(azidir);
//	     		theFieldCylinderTop[i][j].setDrawn(false);
//	     		theNormalCylinderTop[i][j].setDrawn(false);
	     		// here we make the field vector tip be at the location of the arrow if the arrow points inward at the local normal
	   //     	Transform3D offsetTrans = new Transform3D();
	  //   		double dot = theFieldCylinderTop[i][j].getValue().dot(azidirTrans);
	    //    	if ( dot > 0. ) offsetTrans.setTranslation(new Vector3d(0., 0., 0.));
	    //    	else offsetTrans.setTranslation(new Vector3d(0., -1.1, 0.));
	    // 		theFieldCylinderTop[i][j].setModelOffsetTransform(offsetTrans);
        	} 	
        }

        	
        // Now place the vectors on the bottom of the cylinder
        
		Vector3d normalBottom = null;
		Vector3d centerBottom = new Vector3d(0,0,0);
        normalBottom = new Vector3d(0., 0.,1.);
        normalBottom.scale(-heightGCylinder/2.);
		centerBottom.add(normalBottom);
		centerBottom.add(posGCylinder);
		
        for (int j = 0; j < numRadTopBottomCylinder; j++) {
        	double rad = (j+1)*radiusGCylinder/(numRadTopBottomCylinder+1);
        	for (int i = 0; i < numAziTopBottomCylinder; i++) {
        		double aziangle = i*2.*Math.PI/(numAziTopBottomCylinder*1.);
        		Vector3d azipos = new Vector3d(Math.cos(aziangle),Math.sin(aziangle),0.);
        		Vector3d azidir = new Vector3d(-0.,0.,-1.);
        		azipos.scale(rad);
        		azipos.add(centerBottom);
	     		theFieldCylinderBottom[i][j].setPosition(azipos);
	     		theNormalCylinderBottom[i][j].setPosition(azipos);
	     		theNormalCylinderBottom[i][j].setValue(azidir);
//	     		theNormalCylinderBottom[i][j].setDrawn(false);
//	     		theFieldCylinderBottom[i][j].setDrawn(false);
	     		// here we make the field vector tip be at the location of the arrow if the arrow points inward at the local normal
	 //       	Transform3D offsetTrans = new Transform3D();
	 //    		double dot = theFieldCylinderBottom[i][j].getValue().dot(azidirTrans);
	        //	if ( dot > 0. ) offsetTrans.setTranslation(new Vector3d(0., 0., 0.));
	        //	else offsetTrans.setTranslation(new Vector3d(0., -1.1, 0.));
	     	//	theFieldCylinderBottom[i][j].setModelOffsetTransform(offsetTrans);
        	} 	   	
        }		
		
        // Now place the vectors on the sides of the cylinder
        
		Vector3d normalSides = null;
		Vector3d centerSides = new Vector3d(0,0,0);
        normalSides = new Vector3d(compx, compy,0.);
		centerSides.add(normalSides);
		centerSides.add(posGCylinder);

	    for (int j = 0; j < numZSidesCylinder; j++) {
	    	double zvalue = (j+1)*heightGCylinder/(numZSidesCylinder+1)-heightGCylinder/2.;
        	for (int i = 0; i < numAziSidesCylinder; i++) {
    		double aziangle = i*2.*Math.PI/(numAziSidesCylinder*1.);
    		Vector3d azipos = new Vector3d(radiusGCylinder*Math.cos(aziangle),radiusGCylinder*Math.sin(aziangle),zvalue);
    		Vector3d azidir = new Vector3d(Math.cos(aziangle),Math.sin(aziangle),0.);
    		azipos.add(posGCylinder);
     		theFieldCylinderSides[i][j].setPosition(azipos);
     		theNormalCylinderSides[i][j].setPosition(azipos);
     		theNormalCylinderSides[i][j].setValue(azidir);
   //  		theNormalCylinderSides[i][j].setDrawn(true);
   //  		theFieldCylinderSides[i][j].setDrawn(true)	;
     		// here we make the field vector tip be at the location of the arrow if the arrow points inward at the local normal
     //   	Transform3D offsetTrans = new Transform3D();
     //		double dot = theFieldCylinderSides[i][j].getValue().dot(azidirTrans);
        //	if ( dot > 0. ) offsetTrans.setTranslation(new Vector3d(0., 0., 0.));
       // 	else offsetTrans.setTranslation(new Vector3d(0., -1.1, 0.));
     	//	theFieldCylinderSides[i][j].setModelOffsetTransform(offsetTrans);
        	} 	   	
        }	
	    
	}  
	
	/**  Method to handle changes in position or orientation of the Gaussian surfaces, or changes in the positions of the charges. */
    public void propertyChange(PropertyChangeEvent pce) {
        Object source = pce.getSource();
        
        if (source == posSlider_x) {
            double posX = ((Double) pce.getNewValue()).doubleValue();
            posGCylinder.x=posX;
            ShapeNodeGCylinder.setGeometry(Cylinder.makeGeometry(32, radiusGCylinder, heightGCylinder));
            GaussianCylinder.setNode3D(ShapeNodeGCylinder);
            GaussianCylinder.setPosition(posGCylinder);
            posGSphere.x=posX;
            GaussianSphere.setNode3D(ShapeNodeGSphere);
            GaussianSphere.setPosition(posGSphere);
            // see if point charge inside or outside of the sphere and cylinder after this move
            GaussianCylinderFlux.setDrawn(false);
            calculateFlux();
            PlaceENVectors();
            
        } else if (source == posSlider_y) {
            double posY = ((Double) pce.getNewValue()).doubleValue();
            posGCylinder.y=posY;
            GaussianCylinder.setNode3D(ShapeNodeGCylinder);
            GaussianCylinder.setPosition(posGCylinder);
            posGSphere.y=posY;
            GaussianSphere.setNode3D(ShapeNodeGSphere);
            GaussianSphere.setPosition(posGSphere);
            GaussianCylinderFlux.setDrawn(false);
            calculateFlux();
            PlaceENVectors();
            
        } else if (source == radiusGaussianSurface) {
        	radiusGCylinder = ((Double) pce.getNewValue()).doubleValue();
            ShapeNodeGCylinder.setGeometry(Cylinder.makeGeometry(32, radiusGCylinder, heightGCylinder));
            GaussianCylinder.setNode3D(ShapeNodeGCylinder);
            radiusGSphere = radiusGCylinder;
            ShapeNodeGSphere.setGeometry(Sphere.makeGeometry(32, radiusGSphere));
            GaussianSphere.setNode3D(ShapeNodeGSphere);
       //     GaussianCylinderFlux.setDrawn(true);
       //     GaussianCylinderFlux.setNode3D(ShapeNodeGCylinderFlux);
            if(GaussianCylinder.isDrawn()) GaussianCylinderFlux.setDrawn(true);
            calculateFlux();
            PlaceENVectors();
            
        } else if (source instanceof PointCharge) {
        	
        	// Find the amount of charge in the cylinder in the new configuration of charges
        	
            double chargeInCylinder = 0.;
            Collection elements = ((EMEngine)theEngine).getPhysicalObjs();
            TDebug.println(0, elements.size());
            Iterator myIterator = elements.iterator();
            while (myIterator.hasNext() == true) {			
                Object myObject = myIterator.next();
                if (myObject instanceof PointCharge) {
                	Vector3d r = new Vector3d();
                	double myCharge = 0.;
                	myCharge = ((PointCharge) myObject).getCharge();
                    r.set(((PhysicalObject) myObject).getPosition());
                	boolean insidecylinder = insideCylinder(r);
                	if (insidecylinder)	chargeInCylinder = chargeInCylinder + myCharge;
                }
            }
            // Adjust the height of the cylinder showing the flux through the cylinder
            heightGCylinderFlux = oneUnitFlux*(chargeInCylinder);
            ShapeNodeGCylinderFlux.setGeometry(Cylinder.makeGeometry(32, radiusGCylinderFlux, heightGCylinderFlux));
            GaussianCylinderFlux.setNode3D(ShapeNodeGCylinderFlux);
            GaussianCylinderFlux.setPosition(new Vector3d(2.,-2.5+heightGCylinderFlux/2.,0));
            
            //  Find the amount of charge in the sphere in the new configuration of charges
            
            double chargeInSphere = 0.;
            myIterator = elements.iterator();
            while (myIterator.hasNext() == true) {			
               Object myObject = myIterator.next();
               if (myObject instanceof PointCharge) {
               	Vector3d r = new Vector3d();
               	double myCharge = 0.;
               	myCharge = ((PointCharge) myObject).getCharge();
                   r.set(((PhysicalObject) myObject).getPosition());
               	boolean insidesphere = insideSphere(r);
               	if (insidesphere)	chargeInSphere = chargeInSphere + myCharge;
               }
            }
            // Adjust the height of the cylinder showing the flux through the sphere
            heightGSphereFlux = oneUnitFlux*(chargeInSphere);
            ShapeNodeGSphereFlux.setGeometry(Cylinder.makeGeometry(32, radiusGSphereFlux, heightGSphereFlux));
            GaussianSphereFlux.setNode3D(ShapeNodeGSphereFlux);
            GaussianSphereFlux.setPosition(new Vector3d(2.,-2.5+heightGSphereFlux/2.,0));

        } else {
            super.propertyChange(pce);
        }
 
    }
    /** Method to determine whether a given position vector is inside the Gaussian cylinder or not. */
    public boolean insideCylinder(Vector3d position){
    	boolean inside = false;
    	Vector3d relativePosition = new Vector3d();
    	Vector3d cylinderAxis = new Vector3d();
    	cylinderAxis = GaussianCylinder.getDirection();
    	cylinderAxis.normalize();
    	relativePosition.sub(position,posGCylinder);
    	// Compute the coordinates of position in a frame centered on the cylinder
    	double zcoordinate = relativePosition.dot(cylinderAxis);
    	cylinderAxis.scale(zcoordinate);
    	relativePosition.sub(cylinderAxis);
    	double rhocoordinate = relativePosition.length();
    	if (Math.abs(zcoordinate)<=heightGCylinder/2. && rhocoordinate <= radiusGCylinder) inside = true;
    	return inside;
    }
    
    /** Method to determine whether a given position vector is in side the Gaussian sphere or not. */
    public boolean insideSphere(Vector3d position){
    	boolean inside = false;
    	Vector3d relativePosition = new Vector3d();
    	relativePosition.sub(position,posGSphere);
    	// compute the distance of the point charge from the center of the Gaussian sphere
    	double radialdistance = relativePosition.length();
    	if (radialdistance <= radiusGSphere) inside = true;
    	return inside;
    }
    
    /** Method for general reset, currently does not do anything.  */
    public void reset() {       
    }
    
    /** Method to reset the camera view to the original view.  */
    public void resetCamera() {
        mViewer.setLookAt(new Point3d(0.4, 0., 0.4), 
        	new Point3d(0., 0.0, 0.), new Vector3d(0., 1., 0.)); 
     //   mViewer.setLookAt(new Point3d(0.0, 0.0, 0.4), 
    //        	new Point3d(0., 0.0, 0.), new Vector3d(0., 1., 0.)); 
    }
    
    /** Method to create a random point charge with a particular charge, avoiding other charges already present. */
    private PointCharge randomCharge(double charge, double tolerance, double radius, Vector3d offset) {
        PointCharge newCharge = new PointCharge();
        newCharge.setCharge(charge);
        newCharge.setMass(1.);
        newCharge.setRadius(0.2);
        newCharge.setSelectable(true);
        newCharge.setPickable(true);
        newCharge.setColliding(true);
        newCharge.addPropertyChangeListener(this);

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
                if (myObject instanceof PointCharge) {
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
        newCharge.setPosition(testPos);
        return newCharge;
    }

    /** Method to delete all charges from the scene.  */
    private void clearAllCharges() {
        int simstate = mSEC.getSimState();
        mSEC.stop();
        Collection elements = ((EMEngine)theEngine).getPhysicalObjs();
        Iterator it = elements.iterator();
        while (it.hasNext()) {
            Object em = it.next();
            if (em instanceof PointCharge) {
                removeElement((PointCharge) em);
            }
        }
        if (simstate == TEngineControl.RUNNING)
            mSEC.start();
        else theEngine.requestRefresh();
    }
    
    /** Method to make field for the sphere or cylinder visible, depending on which of those is visible.    */
    private void MakeFieldVisible(boolean fieldvisible) {
    	
	    if(sphereVisible){
		    for (int j = 0; j < numThetaSphere; j++) {
		    	for (int i = 0; i < numAziSphere; i++) {
		     		theFieldSphere[i][j].setDrawn(fieldvisible);
		    	} 	
		    }
	    }
	    
	    if(cylinderVisible){
		    for (int j = 0; j < numRadTopBottomCylinder; j++) {
		    	for (int i = 0; i < numAziTopBottomCylinder; i++) {
		     		theFieldCylinderTop[i][j].setDrawn(fieldvisible);
		     		theFieldCylinderBottom[i][j].setDrawn(fieldvisible);
		    	} 	
		    }
		    for (int j = 0; j < numZSidesCylinder; j++) {
		    	for (int i = 0; i < numAziSidesCylinder; i++) {
		     		theFieldCylinderSides[i][j].setDrawn(fieldvisible);
		    	} 	
		    }
	    }
    }
    
    /** Method to calculate the flux through the Gaussian sphere or cylinder. */
    private void calculateFlux(){
    	
    	// Interate over each point charge in the scene and see if it is inside the cylinder, if so increment chargeInCylinder
    	
	    double chargeInCylinder = 0.;
	    Collection elements = ((EMEngine)theEngine).getPhysicalObjs();
	    Iterator myIterator = elements.iterator();
	    while (myIterator.hasNext() == true) {			
	        Object myObject = myIterator.next();
	        if (myObject instanceof PointCharge) {
	        	Vector3d r = new Vector3d();
	        	double myCharge = 0.;
	        	myCharge = ((PointCharge) myObject).getCharge();
	            r.set(((PhysicalObject) myObject).getPosition());
	        	boolean insidecylinder = insideCylinder(r);
	        	if (insidecylinder)	chargeInCylinder = chargeInCylinder + myCharge;
	        }
	    }
	    chargeInCylinder = 0.;
	    if(radiusGCylinder <1.5) chargeInCylinder=(radiusGCylinder*radiusGCylinder)/1.5;
	    if(radiusGCylinder >1.5) chargeInCylinder=1.5;
	    if(radiusGCylinder >2.5) chargeInCylinder=0.;
	    heightGCylinderFlux = oneUnitFlux*(chargeInCylinder);
	    ShapeNodeGCylinderFlux.setGeometry(Cylinder.makeGeometry(32, radiusGCylinderFlux, heightGCylinderFlux));
	    GaussianCylinderFlux.setNode3D(ShapeNodeGCylinderFlux);
	    GaussianCylinderFlux.setPosition(new Vector3d(2.,-2.5+heightGCylinderFlux/2.,0));
	    
    	// Interate over each point charge in the scene and see if it is inside the sphere, if so increment chargeInSphere
	    
	    double chargeInSphere = 0.;
	    myIterator = elements.iterator();
	    while (myIterator.hasNext() == true) {			
	       Object myObject = myIterator.next();
	       if (myObject instanceof PointCharge) {
	       	Vector3d r = new Vector3d();
	       	double myCharge = 0.;
	       	myCharge = ((PointCharge) myObject).getCharge();
	           r.set(((PhysicalObject) myObject).getPosition());
	       	boolean insidesphere = insideSphere(r);
	       	if (insidesphere)	chargeInSphere = chargeInSphere + myCharge;
	       }
	   }
	   heightGSphereFlux = oneUnitFlux*(chargeInSphere);
	   ShapeNodeGSphereFlux.setGeometry(Cylinder.makeGeometry(32, radiusGSphereFlux, heightGSphereFlux));
	   GaussianSphereFlux.setNode3D(ShapeNodeGSphereFlux);
	   GaussianSphereFlux.setPosition(new Vector3d(2.,-2.5+heightGSphereFlux/2.,0));
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
