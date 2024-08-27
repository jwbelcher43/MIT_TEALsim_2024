/*
 * Created on Oct 6, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

package tealsim.physics.em;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import teal.field.Field;
import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.math.RectangularPlane;
import teal.render.Rendered;
import teal.render.j3d.loaders.Loader3DS;
import teal.sim.collision.SphereCollisionController;
import teal.sim.constraint.ArcConstraint;
import teal.sim.constraint.SphericalArcConstraint;
import teal.sim.control.VisualizationControl;
import teal.sim.engine.EngineObj;
import teal.sim.engine.TEngine;
import teal.physics.em.SimEM;
import teal.physics.em.EMEngine;
import teal.physics.physical.Wall;
import teal.physics.em.PointCharge;
import teal.sim.properties.IsSpatial;
import teal.sim.simulation.SimWorld;
import teal.sim.spatial.FieldConvolution;
import teal.sim.spatial.FieldLineManager;
import teal.sim.spatial.RelativeFLine;
import teal.ui.control.ControlGroup;
import teal.ui.control.PropertyDouble;
import teal.ui.swing.JTaskPaneGroup;
import teal.util.TDebug;
import teal.visualization.dlic.DLIC;

// from Example_01

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

/**
 * @author danziger
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SphericalElectrostaticPendulum extends SimEM {

    private static final long serialVersionUID = 3256443586278208051L;
    
    /** An imported 3DS object (a hemisphere).  */
    Rendered importedObject01 = new Rendered();
    Node3D node01 = new Node3D();
    /** An imported 3DS object (a cone).  */
    Rendered importedObject02 = new Rendered();
    /** A 3D node for the cone. */
    Node3D node02 = new Node3D();

    JButton but = null;
    JButton but1 = null;
    JTaskPaneGroup vis;
    JLabel label;
    JLabel score;
    double minScore = 100000000.;
    PointCharge playerCharge;
    Watcher watch;
    double wallscale = 2.0;
    double wheight = 3.0;
    double wallElasticity = 1.0;
    Vector3d wallheight = new Vector3d(0., 0., wheight);
    Appearance myAppearance;
    
    protected FieldConvolution mDLIC = null;
    FieldLineManager fmanager = null;

    public SphericalElectrostaticPendulum() {

        super();
        title = "Electrostatic Pendulum";
        
       
        TDebug.setGlobalLevel(0);

        // Building the world.
        theEngine.setDamping(0.0);
        theEngine.setGravity(new Vector3d(0., -.3,0.));
        
        // import two .3DS files objects using Loader3DS
        // The conversion between max units and Java3D units 
        // is 1 Java3D unit = 1 Max inch
        
        /** A TEALsim native object (a red disk).  */
        Rendered nativeObject01 = new Rendered();
        /** A ShapeNode for the red disk.  */
        
        
        ShapeNode ShapeNodeNative01 = new ShapeNode();
        /** A TEALsim native object (a green sphere).  */

        double lengthPendulum=23;  // maximum of 23
        double heightSupport = 25.;
        ShapeNodeNative01.setGeometry(Cylinder.makeGeometry(32, .2, lengthPendulum));
        nativeObject01.setNode3D(ShapeNodeNative01);
        nativeObject01.setColor(new Color(0, 0, 0));
        nativeObject01.setPosition(new Vector3d(0,heightSupport,0.));
        nativeObject01.setModelOffsetPosition(new Vector3d(0,-lengthPendulum/2,0.));
        nativeObject01.setDirection(new Vector3d(1.,0,0.));
        addElement(nativeObject01);
        
        
        double scale3DS = 3.; // this is an overall scale factor for these .3DS objects
        // Creating components.
       Loader3DS max = new Loader3DS();
    	
        BranchGroup bg01 = 
         max.getBranchGroup("models/Arm_Base.3DS",
         "models/");
        node01.setScale(scale3DS);
      node01.addContents(bg01);
        
        importedObject01.setNode3D(node01);
        importedObject01.setPosition(new Vector3d(0., 0., 0.));
        addElement(importedObject01);
        
// change some features of the lighting, background color, etc., from the default values, if desired
        
        mViewer.setBackgroundColor(new Color(240,240,255));
        
        // -> Rectangular Walls
        myAppearance = Node3D.makeAppearance(new Color3f(Color.GRAY), 0.5f, 0.5f, false);
        myAppearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.5f));

        // Set charges
        double pointChargeRadius = 0.9;

        PointCharge chargeNW = new PointCharge();
        chargeNW.setRadius(pointChargeRadius);
        //chargeNW.setPauliDistance(4.*pointChargeRadius);
        chargeNW.setMass(1.0);
        chargeNW.setCharge(100.0);
        chargeNW.setID("chargeNW");
        chargeNW.setPickable(false);
        chargeNW.setColliding(false);
        chargeNW.setGeneratingP(true);
        chargeNW.setPosition(new Vector3d(3., 0., 0.));
        chargeNW.setMoveable(false);
        SphereCollisionController sccx = new SphereCollisionController(chargeNW);
        sccx.setRadius(pointChargeRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        chargeNW.setCollisionController(sccx);
        addElement(chargeNW);

        PointCharge chargeNE = new PointCharge();
        chargeNE.setRadius(pointChargeRadius);
        //chargeNE.setPauliDistance(4.*pointChargeRadius);
        chargeNE.setMass(1.0);
        chargeNE.setCharge(-100.0);
        chargeNE.setID("chargeNW");
        chargeNE.setPickable(false);
        chargeNE.setColliding(false);
        chargeNE.setGeneratingP(true);
        chargeNE.setPosition(new Vector3d(-3, 0., 0.));
        chargeNE.setMoveable(false);
        sccx = new SphereCollisionController(chargeNE);
        sccx.setRadius(pointChargeRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        chargeNE.setCollisionController(sccx);
       addElement(chargeNE);

        playerCharge = new PointCharge();
        playerCharge.setRadius(pointChargeRadius);
        //playerCharge.setPauliDistance(4.*pointChargeRadius);
        playerCharge.setMass(1.0);
        playerCharge.setCharge(1.0);
        playerCharge.setID("playerCharge");
        playerCharge.setPickable(false);
        playerCharge.setColliding(true);
        playerCharge.setGeneratingP(true);
        playerCharge.setPosition(new Vector3d(0.,0., 0.));
        playerCharge.setMoveable(true);
        playerCharge.setConstrained(true);
        sccx = new SphereCollisionController(playerCharge);
        sccx.setRadius(pointChargeRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        playerCharge.setCollisionController(sccx);
        //playerCharge.addPropertyChangeListener("charge",this );
         addElement(playerCharge);
         
 		SphericalArcConstraint arc = new SphericalArcConstraint(new Vector3d(.0,25.,0.), new Vector3d(0.,0.,1.), lengthPendulum);
		playerCharge.addConstraint(arc);
 		
        int maxStep = 200;
        int numberFL = 12;
        fmanager = new FieldLineManager();
        fmanager.setElementManager(this);
        for (int j = 0; j < 48; j++) {
            RelativeFLine fl = new RelativeFLine(chargeNW, ((j + 1) / 48.) * Math.PI * 2.);
            fl.setType(Field.E_FIELD);
            fl.setKMax(maxStep);
            fmanager.addFieldLine(fl);

            fl = new RelativeFLine(chargeNE, ((j + 1) / 48.) * Math.PI * 2.);
            fl.setType(Field.E_FIELD);
            fl.setKMax(maxStep);
           fmanager.addFieldLine(fl);

            fl = new RelativeFLine(playerCharge, ((j + 1) / 48.) * Math.PI * 2.);
            fl.setType(Field.E_FIELD);
            fl.setKMax(maxStep);
            fmanager.addFieldLine(fl);
        }
        fmanager.setSymmetryCount(2);
        theEngine.setBoundingArea(new BoundingSphere(new Point3d(), 12));

        // Building the GUI.
        PropertyDouble chargeSlider = new PropertyDouble();
        chargeSlider.setText("Player Charge:");
        chargeSlider.setMinimum(-10.);
        chargeSlider.setMaximum(10.);
        chargeSlider.setBounds(40, 535, 415, 50);
        chargeSlider.setPaintTicks(true);
        chargeSlider.addRoute(playerCharge, "charge");
        chargeSlider.setValue(-5);
        //addElement(chargeSlider);
        chargeSlider.setVisible(true);
        label = new JLabel("Current Time:");
        score = new JLabel();
        label.setBounds(40, 595, 140, 50);
        score.setBounds(220, 595, 40, 50);
        label.setVisible(true);
        score.setVisible(true);
        //addElement(label);
        //addElement(score);
        watch = new Watcher();
        addElement(watch);

        //JTaskPane tp = new JTaskPane();
        ControlGroup params = new ControlGroup();
        params.setText("Parameters");
        params.add(chargeSlider);
        params.add(label);
        params.add(score);
        addElement(params);
        //tp.add(params);
        VisualizationControl vis = new VisualizationControl();
        vis.setText("Field Visualization");
        mDLIC = new FieldConvolution();
        mDLIC.setComputePlane(new RectangularPlane(theEngine.getBoundingArea()));
        vis.setFieldConvolution(mDLIC);
        vis.setConvolutionModes(DLIC.DLIC_FLAG_E | DLIC.DLIC_FLAG_EP);
        vis.setSymmetryCount(1);
        vis.setColorPerVertex(true);
        vis.setFieldLineManager(fmanager);
        vis.setActionFlags(0);
        vis.setColorPerVertex(false);
        
        addElement(vis);
        //tp.add(vis);
        //addElement(tp);

        addActions();
        watch.setActionEnabled(true);
        
        theEngine.setDeltaTime(0.25);
        mSEC.init();

        resetCamera();
        reset(heightSupport);
    }

    private void addWall(Vector3d pos, Vector3d length, Vector3d height) {
        Wall myWall = new Wall(pos, length, height);
        myWall.setElasticity(wallElasticity);
        myWall.setColor(Color.GREEN);
        myWall.setPickable(false);
        WallNode myNode = (WallNode) myWall.getNode3D();
        myNode.setFillAppearance(myAppearance);
        addElement(myWall);
    }

    void addActions() {

        TealAction ta = new TealAction("EM Video Game", this);
        addAction("Help", ta);

        ta = new TealAction("Level Complete", "Level Complete", this);
        watch.setAction(ta);


        
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareToIgnoreCase("EM Video Game") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/emvideogame.html");
        	}
        } else if (e.getActionCommand().compareToIgnoreCase("Level complete") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/emvideogame.html");
        	}
        } else {
            super.actionPerformed(e);
        }
    }

    public void propertyChange(PropertyChangeEvent pce) {
        super.propertyChange(pce);
    }

    public void reset(double heightSupport) {
        mSEC.stop();
        mSEC.reset();
        resetPointCharges(heightSupport);
        //theEngine.requestRefresh();
        watch.setActionEnabled(true);
    }

    private void resetPointCharges(double heightSupport) {

        playerCharge.setPosition(new Vector3d(-heightSupport, heightSupport, 0));
    }

    public void resetCamera() {
    	mViewer.setLookAt(new Point3d(0.,.8,4.), new Point3d(0,0,0), new Vector3d(0,1,0));

    }

    public class Watcher extends EngineObj implements IsSpatial {

        private static final long serialVersionUID = 3761692286114804280L;
        //Bounds testBounds = new BoundingSphere(new Point3d(11.4,11.4,0.),2.);
        Bounds testBounds = new BoundingBox(new Point3d(8., -16., -1.5), new Point3d(12., -12., 1.5));
        TealAction theAction = null;
        boolean actionEnabled = false;
        boolean mNeedsSpatial = false;

        public void needsSpatial() {
            mNeedsSpatial = true;
        }

        public void setAction(TealAction ac) {
            theAction = ac;
        }

        public void setActionEnabled(boolean state) {
            actionEnabled = state;
        }

        public boolean getActionEnabled() {
            return actionEnabled;
        }

        public void setBounds(Bounds b) {
            testBounds = b;
        }

        public void nextSpatial() {
            if (theEngine != null) {
                double time = theEngine.getTime();
                score.setText(String.valueOf(time));
                if (actionEnabled) {
                    if (testBounds.intersect(new Point3d(playerCharge.getPosition()))) {
                        System.out.println("congratulations");
                        // Make this a one-shot
                        actionEnabled = false;
                        mSEC.stop();
                        minScore = Math.min(minScore, time);
                        if (theAction != null) {
                            theAction.triggerAction();
                        }
                    }
                }

            }
        }
    }

  

}
