/* $Id: Example_01.java,v 1.4 2008/12/23 20:16:49 jbelcher Exp $ */
/**
 * @author John Belcher - Department of Physics / MIT
 * @version $Revision: 1.4 $
 */

package tealsim.physics.examples;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.media.j3d.*;
import javax.vecmath.*;
import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.render.Rendered;
import teal.render.geometry.Cylinder;
import teal.render.geometry.Sphere;
import teal.render.j3d.*;
import teal.render.j3d.loaders.Loader3DS;
import teal.physics.em.SimEM;
import teal.ui.control.*;
import teal.util.TDebug;

/** Create two native 3D objects and import two.  The native objects are a green sphere and a flat red cylinder (a disk) and
 * the imported two are .3DS objects (a tapered cone and an orange hemisphere).  
 * The vertical position of the red disk is controlled by a slider, as is the orientation of the cone.  
 *  
 * @author John Belcher
 * @version 1.0 
 * */

public class Example_01 extends SimEM {

    private static final long serialVersionUID = 3257008735204554035L;
    /** A TEALsim native object (a red disk).  */
    Rendered nativeObject01 = new Rendered();
    /** A ShapeNode for the red disk.  */
    ShapeNode ShapeNodeNative01 = new ShapeNode();
    /** A TEALsim native object (a green sphere).  */
    Rendered nativeObject02 = new Rendered();
    /** A ShapeNode for the green sphere.  */
    ShapeNode ShapeNodeNative02 = new ShapeNode();
    /** An imported 3DS object (a hemisphere).  */
    Rendered importedObject01 = new Rendered();
    /** A 3D node for the hemisphere. */
    Node3D node01 = new Node3D();
    /** An imported 3DS object (a cone).  */
    Rendered importedObject02 = new Rendered();
    /** A 3D node for the cone. */
    Node3D node02 = new Node3D();
    /** Slider for the position of the red disk.  */
    PropertyDouble posSlider01 = new PropertyDouble();
    /** Slider for the rotation angle of the cone.  */
    PropertyDouble angSlider01 = new PropertyDouble();
    /** Slider for the position of the cone.  */
    PropertyDouble posSlider02 = new PropertyDouble();
    
    public Example_01() {
        super();
//        System.out.println("here");
        TDebug.setGlobalLevel(0);
        title = "Example_01";
        
 // create two objects using teal.render.geometry 
 // and add them to the scene
        
        ShapeNodeNative01.setGeometry(Cylinder.makeGeometry(32, 2., 0.05));
        nativeObject01.setNode3D(ShapeNodeNative01);
        nativeObject01.setColor(new Color(0, 0, 0));
        nativeObject01.setPosition(new Vector3d(0,0.,0.));
        nativeObject01.setDirection(new Vector3d(0.,1.,0.));
        addElement(nativeObject01);
        
        ShapeNodeNative02.setGeometry(Sphere.makeGeometry(16,.5));
        nativeObject02.setNode3D(ShapeNodeNative02);
        nativeObject02.setColor(new Color(0, 255, 0));  // makes the sphere green
        nativeObject02.setPosition(new Vector3d(0, 2, 0));
        nativeObject02.setSelectable(true);
        nativeObject02.setPickable(true);
        nativeObject02.setMoveable(true);
        nativeObject02.addPropertyChangeListener(this);
        addElement(nativeObject02);           

        //  print out a line
        
        System.out.println( "Hello From Example_01!" );
 // import two .3DS files objects using Loader3DS
 // The conversion between max units and Java3D units 
 // is 1 Java3D unit = 1 Max inch
        
        double scale3DS = 0.01; // this is an overall scale factor for these .3DS objects
 
        Loader3DS max = new Loader3DS();
    	
        BranchGroup bg01 = 
         max.getBranchGroup("models/geoSphere.3DS",
         "models/");
        node01.setScale(scale3DS);
        node01.addContents(bg01);
        
        importedObject01.setNode3D(node01);
        importedObject01.setPosition(new Vector3d(0., -1., 0.));
        addElement(importedObject01);

        BranchGroup bg02 = 
         max.getBranchGroup("models/cone.3DS", "models/");
        node02.setScale(scale3DS);
        node02.addContents(bg02);
        
        importedObject02.setNode3D(node02);
        importedObject02.setPosition(new Vector3d(0., 0., 0.));
        importedObject02.setDirection(new Vector3d(0.,1.,0.));
        addElement(importedObject02);
        
 // create the slider for the disk 
        
        posSlider01.setText("Disk Position ");
        posSlider01.setMinimum(-1.);
        posSlider01.setMaximum(3.0);
        posSlider01.setPaintTicks(true);
        posSlider01.addPropertyChangeListener("value", this);
        posSlider01.setValue(-1.);
        posSlider01.setVisible(true);
        
// create the two sliders for the cone 
        
        angSlider01.setText("Cone Rotation Angle");
        angSlider01.setMinimum(-180.);
        angSlider01.setMaximum(180.0);
        angSlider01.setPaintTicks(true);
        angSlider01.addPropertyChangeListener("value", this);
        angSlider01.setValue(0.);
        angSlider01.setVisible(true);
        
        posSlider02.setText("Cone Position");
        posSlider02.setMinimum(-2.);
        posSlider02.setMaximum(3.0);
        posSlider02.setPaintTicks(true);
        posSlider02.addPropertyChangeListener("value", this);
        posSlider02.setValue(0.);
        posSlider02.setVisible(true);
        
 // add the sliders to control groups and add those to the scene

        ControlGroup controls01 = new ControlGroup();
        controls01.setText("Red Disk");
        controls01.add(posSlider01);
        addElement(controls01);
        
        ControlGroup controls02 = new ControlGroup();
        controls02.setText("Cone");
        controls02.add(angSlider01);
        controls02.add(posSlider02);
        addElement(controls02);
        
// change some features of the lighting, background color, etc., from the default values, if desired
        
        mViewer.setBackgroundColor(new Color(240,240,255));
        
 // set paramters for mouseScale 
        
        Vector3d mouseScale = mViewer.getVpTranslateScale();
        mouseScale.x *= 0.05;
        mouseScale.y *= 0.05;
        mouseScale.z *= 0.5;
        mViewer.setVpTranslateScale(mouseScale);
        
// set initial state

        mSEC.init();  
        theEngine.requestRefresh();
        mSEC.setVisible(true);
        // the following statement removes the "run" controls since there is nothing to run here
        mSEC.rebuildPanel(0);
        reset();
        resetCamera();
        // addAction for pulldown menus on TEALsim windows     
        addActions();

    }

// add two items to the help menu, one to explain the simulation and the other to explain the 
// veiw and execution controls
  
    void addActions() {
    //    TealAction ta = new TealAction("Execution & View", this);
    //    addAction("Help", ta);
        TealAction tb = new TealAction("Example_01", this);
        addAction("Help", tb);
    }

    
    public void actionPerformed(ActionEvent e) {
        TDebug.println(1, " Action comamnd: " + e.getActionCommand());
        if (e.getActionCommand().compareToIgnoreCase("Example_01") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).openBrowser("help/example_01.html");
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
    }

    public void resetCamera() {
        mViewer.setLookAt(new Point3d(0.0, 0.025, 0.4), 
        	new Point3d(0., 0.025, 0.), new Vector3d(0., 1., 0.));
     
    }

    public void propertyChange(PropertyChangeEvent pce) {
        Object source = pce.getSource();
        if (source == posSlider01) {
            double posV01 = ((Double) pce.getNewValue()).doubleValue();
            nativeObject01.setNode3D(ShapeNodeNative01);
            nativeObject01.setPosition(new Vector3d(0., posV01, 0.));
        } else 
        if (source == angSlider01) {
            double angV02 = ((Double) pce.getNewValue()).doubleValue();
            double angV02rad = angV02*Math.PI/180.;
            double compx = Math.sin(angV02rad);
            double compy = Math.cos(angV02rad);
            importedObject02.setNode3D(node02);
            importedObject02.setDirection(new Vector3d(compx, compy, 0.));
        } else {
            super.propertyChange(pce);
        }
        if (source == posSlider02) {
            double posV02 = ((Double) pce.getNewValue()).doubleValue();
            importedObject02.setNode3D(node02);
            importedObject02.setPosition(new Vector3d(0, posV02, 0.));
        } else {
            super.propertyChange(pce);
        }
    }
    
}
