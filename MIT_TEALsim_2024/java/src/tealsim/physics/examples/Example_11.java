/* $Id: Example_04.java,v 1.2 2008/01/06 21:42:59 jbelcher Exp $ */
/**
 * @author John Belcher 
 * Revision: 1.0 $
 */

package tealsim.physics.examples;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.media.j3d.*;
import javax.vecmath.*;

import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.physics.em.CylindricalBarMagnet;
import teal.physics.em.SimEM;
import teal.ui.control.*;
import teal.util.TDebug;
import teal.sim.spatial.FieldConvolution;
import teal.visualization.dlic.DLIC;
import teal.math.RectangularPlane;
import java.awt.Dimension;

import teal.sim.collision.SphereCollisionController;
import teal.sim.control.VisualizationControl;


/** A simulation of a free magnet falling under gravity and also interacting magnetostatically with
 * a fixed magnet located underneath a wall.  We have a slider that can be used to vary the dipole moment
 * on the fixed magnet.  The friction in the world is set to a high value so that the falling magnet
 * will quickly settle down to its equilibrium position.
 *  
 * @author John Belcher
 * @version 1.0 
 * */

public class Example_11 extends SimEM {

    private static final long serialVersionUID = 3257008735204554035L;
    /** The fixed-in-space magnetic dipole moment slider. */
    PropertyDouble MagMomentSlider = new PropertyDouble();
    /** The radius of the cylinder representing the fixed-in-space magnet. */
    double fixedMagnetRad = 0.2;
    /** The length of the cylinder representing the fixed-in-space magnet. */
    double fixedMagnetLen = 0.1;
    /** The radius of the cylinder representing the floating magnet.  */
    double floatingMagnetRadius = 0.2;
    /** The length of the cylinder representing the floating magnet. */
    double floatngMagnetLen = 0.1;
    /** The friction in the world. */
    double friction = 0.1;
    /** The floating magnet.  */
    CylindricalBarMagnet floatingMagnet;
    /** The fixed magnet.  */
    CylindricalBarMagnet fixedMagnet;
    /** The initial vector position of the floating magnet.  */
    Vector3d floatingMagnetPos;
    /** The mass of both the floating and the fixed magnet. */
    double magnetMass = 0.035;
    /** The dipole moment of the fixed magnet. */
    double MuFixed = 1.;
    /** The dipole moment of the floating magnet. */
    double MuFloat = 1.;
 
    public Example_11() {
        super();

        TDebug.setGlobalLevel(0);

        title = "Example_11";
        
		///// Set properties on the SimEngine /////
		// Bounding area represents the characteristic size of the space.
		// setDeltaTime() sets the time step of the simulation.
		// setDamping() sets the damping on the system.
        
        BoundingSphere bs = new BoundingSphere(new Point3d(0, 1.6, 0), 03.5);
        theEngine.setBoundingArea(bs);
        theEngine.setDeltaTime(.01); 
        theEngine.setDamping(friction
        		);  
        theEngine.setGravity(new Vector3d(0.,0.,0.));
        mViewer.setBoundingArea(bs);
              
        fixedMagnet = new CylindricalBarMagnet();
        fixedMagnet.setMu(MuFixed);
        fixedMagnet.setPosition(new Vector3d(0., -0.8, 0.));
        fixedMagnet.setDirection(new Vector3d(0, -1, 0));
        fixedMagnet.setPickable(true);
        fixedMagnet.setRotable(true);
        fixedMagnet.setMoveable(true);
        fixedMagnet.setRadius(fixedMagnetRad);
        fixedMagnet.setLength(fixedMagnetLen);
        fixedMagnet.setMass(magnetMass);
        SphereCollisionController sccx = new SphereCollisionController(fixedMagnet);
        sccx.setRadius(0.2);
        sccx.setTolerance(0.1);
        fixedMagnet.setPauliDistance(1.2);
        //		sccx.setElasticity(0.);
        //		sccx.setMode(SphereCollisionController.WALL_SPHERE);
        fixedMagnet.setCollisionController(sccx);
        addElement(fixedMagnet);

        floatingMagnet = new CylindricalBarMagnet();
        floatingMagnet.setID("Magnet");
        floatingMagnet.setMu(MuFloat);
        floatingMagnet.setDirection(new Vector3d(0., 1., 0.));
        floatingMagnetPos = new Vector3d(0., 1.25, 0.);
        floatingMagnet.setPickable(true);
        floatingMagnet.setRotable(true);
        floatingMagnet.setMoveable(true);
        floatingMagnet.setRadius(floatingMagnetRadius);
        floatingMagnet.setLength(floatngMagnetLen);
        floatingMagnet.setMass(magnetMass);
        floatingMagnet.setPauliDistance(1.2);
        SphereCollisionController sccx1 = new SphereCollisionController(floatingMagnet);
        sccx1.setRadius(0.2);
        sccx1.setTolerance(0.1);
        addElement(floatingMagnet);
      
        
        // create the sliders to control the dipole moment
        
        MagMomentSlider.setText("Mufixed");
        MagMomentSlider.setMinimum(-10);
        MagMomentSlider.setMaximum(10);
        MagMomentSlider.setPaintTicks(true);
        MagMomentSlider.addPropertyChangeListener("value", this);
        MagMomentSlider.setValue(1.);
        MagMomentSlider.setVisible(true);

        // add the slider to a control group and add

        ControlGroup controls = new ControlGroup();
        controls.setText("Parameters");
        controls.add(MagMomentSlider);
        addElement(controls);
        
        // Add a FieldConvolution generator to the simulation.  
        // A FieldConvolution generates high-resolution 
		// images of a two-dimensional slice of the field.  
        // Below we create the generator and specify the size of the slice.
        RectangularPlane rec = new RectangularPlane(new Vector3d(-2.5, -2.5, 0.),
				new Vector3d(-2.5, 2.5, 0.), new Vector3d(2.5, 2.5, 0.));
		FieldConvolution mDLIC = new FieldConvolution();
		mDLIC.setSize(new Dimension(1024, 1024));
		mDLIC.setVisible(false);
		mDLIC.setComputePlane(rec);
        VisualizationControl vis = new VisualizationControl();
        vis.setFieldConvolution(mDLIC);
		vis.setConvolutionModes(DLIC.DLIC_FLAG_B | DLIC.DLIC_FLAG_BP);
        addElement(vis);
		
        // set paramters for mouseScale 
        
        Vector3d mouseScale = mViewer.getVpTranslateScale();
        mouseScale.x *= 0.05;
        mouseScale.y *= 0.05;
        mouseScale.z *= 0.5;
        mViewer.setVpTranslateScale(mouseScale);
        mSEC.init(); 
        resetCamera();
        // addAction for pulldown menus on TEALsim windows     
        addActions();
        reset();
        
    }

    
    void addActions() {
        TealAction ta = new TealAction("Execution & View", this);
        addAction("Help", ta);
        TealAction tb = new TealAction("Example_04", this);
        addAction("Help", tb);
    }

    public void actionPerformed(ActionEvent e) {
        TDebug.println(1, " Action comamnd: " + e.getActionCommand());
        if (e.getActionCommand().compareToIgnoreCase("Example_04") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).openBrowser("help/example_04.html");
        	}
        }  else {
            super.actionPerformed(e);
        }
        if (e.getActionCommand().compareToIgnoreCase("Execution & View") == 0) 
        {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).openBrowser("help/executionView.html");
        	}
        }  else {
            super.actionPerformed(e);
        }
    }

    public void reset() {
        floatingMagnet.setPosition(floatingMagnetPos);
        floatingMagnet.setVelocity(new Vector3d(0.,0.,0.));
        floatingMagnet.setDirection(new Vector3d(-.1, 0., 0.));
        theEngine.setDamping(friction);
        MagMomentSlider.setValue(1.);
		theEngine.requestRefresh();
    }

    public void resetCamera() {
        mViewer.setLookAt(new Point3d(0.0, 0.025, 0.4), 
        		new Point3d(0., 0.025, 0.), new Vector3d(0., 1., 0.));
    }

    public void propertyChange(PropertyChangeEvent pce) {
        Object source = pce.getSource();
        if (source == MagMomentSlider) {
            MuFixed = ((Double) pce.getNewValue()).doubleValue();
            fixedMagnet.setMu(MuFixed);   
        } else {
            super.propertyChange(pce);
        }
    }
    
}

