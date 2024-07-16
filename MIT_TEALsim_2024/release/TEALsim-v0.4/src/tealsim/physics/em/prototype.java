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
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.plot.Graph;
import teal.plot.PlotProperties;
import teal.render.Rendered;
import teal.render.geometry.Cylinder;
import teal.render.j3d.Node3D;
import teal.render.j3d.ShapeNode;
import teal.render.j3d.WallNode;
import teal.render.j3d.loaders.Loader3DS;
import teal.render.primitives.Line;
import teal.render.scene.TNode3D;
import teal.sim.collision.SphereCollisionController;
import teal.sim.engine.EngineObj;
import teal.sim.engine.TEngine;
import teal.physics.em.MagneticDipole;
import teal.physics.em.SimEM;
import teal.physics.em.EMEngine;
import teal.physics.physical.Wall;
import teal.physics.em.ConstantField;
import teal.physics.em.PointCharge;
import teal.sim.properties.IsSpatial;
import teal.sim.simulation.SimWorld;
import teal.sim.spatial.FieldConvolution;
import teal.sim.spatial.FieldLineManager;
import teal.ui.control.ControlGroup;
import teal.ui.control.PropertyDouble;
import teal.ui.swing.JTaskPaneGroup;
import teal.util.TDebug;
import teal.util.URLGenerator;

/**
 * @author danziger
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class prototype extends SimEM {

    private static final long serialVersionUID = 3256443586278208051L;
    boolean loadModels = true;
	double metricScale = 1.5;  //25.0 / 39.37;
    MagneticDipole m1;
    double baseModelOff = -0.20;
    double magLen = 0.24;
    double magR = 0.09;
	Graph position_graph;
	PlotProperties position_plot;
	PropertyDouble velXSlider;
	PropertyDouble velYSlider;
	PropertyDouble velZSlider;
	JButton but = null;
    JButton but1 = null;
    JTaskPaneGroup vis;
    JLabel label;
    JLabel score;
    double minScore = 100000000.;
    PointCharge playerCharge;
    Watcher watch;
    double wallscale = 2.0;
    double wheight = 1.0;
    double wallElasticity = 1.0;
    Vector3d wallheight = new Vector3d(0., 0., wheight);
    Appearance myAppearance;
	double velX = 10.;
	double velY = 0.;
	double velZ = 0.;
    
    protected FieldConvolution mDLIC = null;
    FieldLineManager fmanager = null;
    ConstantField BField;
    ConstantField EField;
    SphereCollisionController sccx;

    public prototype() {

        super();
        title = "Charge In A Magnetic Field";
        
       
        TDebug.setGlobalLevel(0);

        // Building the world.
        theEngine.setDamping(0.0);
        theEngine.setGravity(new Vector3d(0., 0., 0.));
        

        BField = new ConstantField(new Vector3d(0., 0., 0.), new Vector3d(0, 0., 1.), 1.);
        BField.setID("cylField");
        BField.setMagnitude(0.);
        //BField.addPropertyChangeListener("magnitude", this);
        //BField.setModel(theEngine);
        BField.setType(ConstantField.B_FIELD);
        addElement(BField);
        
        EField = new ConstantField(new Vector3d(0., 0., 0.), new Vector3d(0, 1., 0.), .1);
        EField.setID("cylField");
        EField.setMagnitude(0.);
        //EField.addPropertyChangeListener("magnitude", this);
        //EField.setModel(theEngine);
        EField.setType(ConstantField.E_FIELD);
        addElement(EField);

        // Creating components.
        
        // magnet
        double scale = 10.0 / 39.37;
        m1 = new MagneticDipole();
        m1.setMu(10.);
        m1.setPosition(new Vector3d(0., 0., 0.));
        m1.setDirection(new Vector3d(0, 0, 1));
        m1.setPickable(false);
        m1.setRotable(false);
        m1.setMoveable(false);
        m1.setRadius(magR);
        m1.setLength(magLen);


        if (loadModels) {

			// Here we load an external model and set it to be the model used by the MagneticDipole.
			// This is a model of a magnet (a silver chamfered cylinder) that replaces the generic cylinder used by default.
			TNode3D node3 = new Loader3DS().getTNode3D(URLGenerator
					.getResource("models/Magnet_At_Zero.3DS"));
			node3.setScale(metricScale);
			m1.setNode3D(node3);
			m1.setModelOffsetPosition(new Vector3d(0.,-0.5,0.));
        //    m1.setDrawn(false);
        }
        addElement(m1);
        // -> Rectangular Walls
        myAppearance = Node3D.makeAppearance(new Color3f(Color.GRAY), 0.5f, 0.5f, false);
        myAppearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.5f));
/*    old code is below
        double dwidth = 0.; 
        double left = 10.;
        // west (left)  wall
        addWall(new Vector3d(-10, 0., 0.), new Vector3d(0., left, 0.), wallheight);
        //north (top) wall
        addWall(new Vector3d(-dwidth*.5, 5, 0.), new Vector3d(20-dwidth, 0., 0.), wallheight);
        // southwall
        addWall(new Vector3d(0., -5., 0.), new Vector3d(20., 0., 0.), wallheight);
        //east wall
        addWall(new Vector3d(left, -dwidth*.5, 0.), new Vector3d(0., left-dwidth, 0.), wallheight);
        Line l = new Line(new Vector3d(-left, 0., 0.), new Vector3d(left, 0., 0.));
        l.setColor(Color.WHITE);
        addElement(l);
*/
        
        double cube = 15.; 
        // west (left)  wall
        addWall(new Vector3d(-cube*.5, 0., 0.), new Vector3d(0., cube, 0.), new Vector3d(0., 0., cube));
        //north (top) wall
        addWall(new Vector3d(0, cube*.5, 0.), new Vector3d(cube, 0., 0.), new Vector3d(0., 0., cube));
        // southwall
        addWall(new Vector3d(0., -cube*.5, 0.), new Vector3d(cube, 0., 0.), new Vector3d(0., 0., cube));
        //east wall
        addWall(new Vector3d(cube*.5, 0., 0.), new Vector3d(0., cube, 0.), new Vector3d(0., 0., cube));
        // far wall
        addWall(new Vector3d(0, 0., -cube*.5), new Vector3d(cube, 0, 0.), new Vector3d(0., cube, 0));
        // close wall
        addWall(new Vector3d(0, 0., cube*.5), new Vector3d(cube, 0, 0.), new Vector3d(0., cube, 0));
        Line l = new Line(new Vector3d(-cube*.5, 0., 0.), new Vector3d(cube*.5, 0., 0.));
        l.setColor(Color.WHITE);
        addElement(l);
        
        
        // set stationary charge
        double pointChargeRadius = 0.4;
        PointCharge chargeNW = new PointCharge();
        chargeNW.setRadius(2*pointChargeRadius);
        //chargeNW.setPauliDistance(4.*pointChargeRadius);
        chargeNW.setMass(1.0);
        chargeNW.setCharge(40.0);
        chargeNW.setID("chargeNW");
        chargeNW.setPickable(false);
        chargeNW.setColliding(false);
        chargeNW.setGeneratingP(true);
        chargeNW.setPosition(new Vector3d(cube*.5+2.*pointChargeRadius, cube*.5+2.*pointChargeRadius, 0.));
        chargeNW.setMoveable(false);
        addElement(chargeNW);
        
        // Set moveable charge
       
        playerCharge = new PointCharge();
        playerCharge.setRadius(pointChargeRadius);
        //playerCharge.setPauliDistance(4.*pointChargeRadius);
        playerCharge.setMass(1.);
        playerCharge.setCharge(10.);
        playerCharge.setID("playerCharge");
        playerCharge.setPickable(false);
        playerCharge.setColliding(true);
        playerCharge.setGeneratingP(true);
        playerCharge.setPosition(new Vector3d(-3., 0., 0.));
        playerCharge.setVelocity(new Vector3d(5., 0., 0.));
        playerCharge.setMoveable(true);
        sccx = new SphereCollisionController(playerCharge);
        sccx.setRadius(pointChargeRadius);
        sccx.setTolerance(0.1);
        sccx.setMode(SphereCollisionController.WALL_SPHERE);
        playerCharge.setCollisionController(sccx);
        //playerCharge.addPropertyChangeListener("charge",this );
        
        addElement(playerCharge);
        PropertyDouble chargeSlider = new PropertyDouble();
        chargeSlider.setText("Charge:");
        chargeSlider.setMinimum(-10.);
        chargeSlider.setMaximum(10.);
        chargeSlider.setBounds(40, 535, 415, 50);
        chargeSlider.setPaintTicks(true);
        chargeSlider.addRoute(playerCharge, "charge");
        chargeSlider.setValue(5.);
        chargeSlider.setVisible(true);
        
        velXSlider = new PropertyDouble();
        velXSlider.setText("X Velocity:");
        velXSlider.setMinimum(-10.);
        velXSlider.setMaximum(10.);
        velXSlider.setBounds(40, 535, 415, 50);
        velXSlider.setPaintTicks(true);
        velXSlider.addPropertyChangeListener("value",this);
        velXSlider.setValue(10.);

        velYSlider = new PropertyDouble();
        velYSlider.setText("Y velocity:");
        velYSlider.setMinimum(-10.);
        velYSlider.setMaximum(10.);
        velYSlider.setBounds(40, 535, 415, 50);
        velYSlider.setPaintTicks(true);
        velYSlider.addPropertyChangeListener("value",this);
        velYSlider.setValue(0.);
        
        velZSlider = new PropertyDouble();
        velZSlider.setText("Z velocity:");
        velZSlider.setMinimum(-10.);
        velZSlider.setMaximum(10.);
        velZSlider.setBounds(40, 535, 415, 50);
        velZSlider.setPaintTicks(true);
        velZSlider.addPropertyChangeListener("value",this);
        velZSlider.setValue(0.);

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
        params.add(velXSlider);
        params.add(velYSlider);
        params.add(velZSlider);
        params.add(label);
        params.add(score);
        addElement(params);
        //tp.add(params);
        
    	// Create a graph of the height of the coil, and add it to the GUI.  
        // This involves creating a graph, adding a "plot" (which defines the 
        // quantities being plotted), and adding it in its own Control Group.
        
		// Graph constructor.
		position_graph = new Graph();		
		position_graph.setSize(400, 200);		
		position_graph.setXRange(-10, 10.);		
		position_graph.setYRange(-5., 5.);
		position_graph.setWrap(true);
		position_graph.setClearOnWrap(true);
		position_graph.setXLabel("y");		
		position_graph.setYLabel("z");
		// Here we create the PlotItem being drawn by this graph.  
		// We want to plot the y-position of the RingOfCurrent versus time, 
		// so we use  PlotProperties
		position_plot = new PlotProperties();
		position_plot.setObjectX(playerCharge); 
		position_plot.setPropertyX("x");  
		position_plot.setObjectY(playerCharge); 
		position_plot.setPropertyY("y");  
		// adds the supplied PlotItem to the graph.
		position_graph.addPlotItem(position_plot);
		
		// Here we create a new Control Group for the graph, and add the graph to that Group.
		ControlGroup graphPanel = new ControlGroup();
		graphPanel.setText("Graphs");
		graphPanel.addElement(position_graph);
	//	addElement(graphPanel);
		
 
        //tp.add(vis);
        //addElement(tp);

        addActions();
        watch.setActionEnabled(true);
        
        theEngine.setDeltaTime(.05);
        mSEC.init();

        resetCamera();
        reset();
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

    	if (pce.getSource()==velXSlider) {
    		velX= ((Double)velXSlider.getValue()).doubleValue();
    	    playerCharge.setVelocity(new Vector3d(velX, velY, velZ));
    	}
      	if (pce.getSource()==velYSlider) {
    		velY= ((Double)velYSlider.getValue()).doubleValue();
    	    playerCharge.setVelocity(new Vector3d(velX, velY, velZ));
    	}
      	
      	if (pce.getSource()==velZSlider) {
    		velZ= ((Double)velZSlider.getValue()).doubleValue();
    	    playerCharge.setVelocity(new Vector3d(velX, velY, velZ));
    	}
        super.propertyChange(pce);
    }

    public void reset() {
        mSEC.stop();
        mSEC.reset();
        resetPointCharges();
        //theEngine.requestRefresh();
        watch.setActionEnabled(true);
        position_graph.clear(0);
    }

    private void resetPointCharges() {
    	//velX = 1.;
    	//velY= 0.;
        playerCharge.setPosition(new Vector3d(-4.0, 0.0, 0.));
        playerCharge.setVelocity(new Vector3d(velX, velY, velZ));
    }

    public void resetCamera() {
        mViewer.setLookAt(new Point3d(0.0, 0.0, 1.5), new Point3d(), new Vector3d(0., 1., 0.));

    }

    public class Watcher extends EngineObj implements IsSpatial {

        private static final long serialVersionUID = 3761692286114804280L;
        Bounds testBounds = new BoundingSphere(new Point3d(10.,10.,0.),.2);
        //Bounds testBounds = new BoundingBox(new Point3d(8., -16., -1.5), new Point3d(12., -12., 1.5));
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
                        //if (theAction != null) {
                          //  theAction.triggerAction();
                       // }
                    }
                }

            }
        }
    }

  

}
