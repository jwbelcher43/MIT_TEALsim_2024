/* $Id: DipoleInduction.java,v 1.1 2007/12/19 18:51:55 jbelcher Exp $ */

/**
 * A demonstration implementation of the TFramework.
 * 
 * @author Philip Bailey - Center for Educational Computing Initiatives / MIT
 * @version $Revision: 1.1 $
 */

package tealsim.physics.em;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.TransparencyAttributes;
import javax.swing.JButton;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.math.RectangularPlane;
import teal.render.j3d.Node3D;
import teal.render.j3d.WallNode;
import teal.render.viewer.SelectEvent;
import teal.render.viewer.SelectListener;
import teal.render.viewer.TViewer;
import teal.sim.collision.SphereCollisionController;
import teal.sim.control.VisualizationControl;
import teal.sim.engine.SimEngine;
import teal.sim.engine.TEngineControl;
import teal.physics.em.EMEngine;
import teal.physics.physical.PentagonBox;
import teal.physics.physical.RectangularBox;
import teal.physics.physical.Wall;
import teal.physics.em.PointCharge;
import teal.sim.simulation.SimWorld;
import teal.sim.spatial.FieldConvolution;
import teal.ui.control.ControlGroup;
import teal.ui.control.PropertyDouble;
import teal.util.TDebug;
import teal.visualization.dlic.DLIC;

public class DipoleInduction extends SimWorld implements SelectListener {

    private static final long serialVersionUID = 3256443620520571449L;

    JButton groundButton = null;
    JButton ungroundButton = null;
    private VisualizationControl vis;
    final private int N = 8;
    private PointCharge[] pointCharges = new PointCharge[2 * N];
    PropertyDouble slider1 = null;
    Wall myWallG = null;
    private PointCharge centralCharge = null;
    private double pointChargeRadius = 0.3;
    
    double wallscale = 2.0;
    double wheight = 1.5;
    double wallElasticity = 1.0;
    Vector3d wallheight = new Vector3d(0., 0., wheight);
    Appearance myAppearance;
    
    protected FieldConvolution mDLIC = null;

    public DipoleInduction() {

        super();
        //super.initialize();
        title = "Box Induction";
        setID("Box Induction");
        // Building the world.
        EMEngine emModel = new EMEngine();
        emModel.setBoundingArea(new BoundingSphere(new Point3d(), 8));
        emModel.setDeltaTime(0.25);
        emModel.setDamping(0.1);
        emModel.setGravity(new Vector3d(0., 0., 0.));
        //		theEngine.setShowTime(true);
        setEngine(emModel);

       // mViewer.setNavigationMode(TViewer.ORBIT | TViewer.VP_ZOOM | TViewer.VP_TRANSLATE);

        RectangularPlane rec = new RectangularPlane(new Vector3d(-12., -12., 0.), new Vector3d(-12., 12., 0.),
            new Vector3d(12., 12., 0.));
        mDLIC = new FieldConvolution();
        mDLIC.setSize(new Dimension(512, 512));
        mDLIC.setVisible(false);  
        mDLIC.setComputePlane(rec);

        // Creating components.

        // -> Rectangular Walls
        myAppearance = Node3D.makeAppearance(new Color3f(Color.GRAY), 0.5f, 0.5f, false);
        myAppearance.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.NICEST, 0.5f));
        // west wall
        addWall(new Vector3d(-4., 0., 0.), new Vector3d(0., 8., 0.), wallheight);
        // north wall
        addWall(new Vector3d(0., 4., 0.), new Vector3d(8., 0., 0.), wallheight);
        // east wall
        addWall(new Vector3d(4., 0., 0.), new Vector3d(0., 8., 0.), wallheight);
        // south wall
        //addWall(new Vector3d(0., -4., 0.), new Vector3d(8., 0., 0.), wallheight);
        myWallG = new Wall(new Vector3d(0., -4., 0.), new Vector3d(8., 0., 0.), wallheight);
        myWallG.setElasticity(wallElasticity);
        myWallG.setColor(Color.GREEN);
        myWallG.setPickable(false);
        WallNode myNode = (WallNode) myWallG.getNode3D();
        myNode.setFillAppearance(myAppearance);
        addElement(myWallG);
        
        // -> Point Charges
        int pos = N;
        int neg = N;
        for (int i = 0; i < 2 * N; i++) {
            pointCharges[i] = new PointCharge();
            pointCharges[i].setRadius(pointChargeRadius);
            pointCharges[i].setMass(1.0);

            double charge = Math.random() > 0.5 ? 1. : -1.;
            if (charge > 0.) {
                if (pos == 0)
                    charge = -1.;
                else pos--;
            }
            if (charge < 0.) {
                if (neg == 0)
                    charge = 1.;
                else neg--;
            }
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
        centralCharge.setPosition(new Vector3d(0.,6.,0.));
        centralCharge.setRadius(.8);
        centralCharge.setPauliDistance(pointChargeRadius * 2.);
        centralCharge.setMass(1.0);
        centralCharge.setCharge(0.);
        centralCharge.setID("centralCharge");
        centralCharge.setPickable(true);
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
        slider1.setValue(0.);
        slider1.setText("Central Charge");
        slider1.setBorder(null);
        ControlGroup controls;
        controls = new ControlGroup();
        controls.setText("Parameters");
        controls.add(slider1);
        
        groundButton = new JButton(new TealAction("Ground", "Ground", this));
        groundButton.setFont(groundButton.getFont().deriveFont(Font.BOLD));
        groundButton.setBounds(40, 570, 195, 24);
        controls.add(groundButton);

        ungroundButton = new JButton(new TealAction("Unground", "Unground", this));
        ungroundButton.setFont(ungroundButton.getFont().deriveFont(Font.BOLD));
        ungroundButton.setBounds(40, 600, 195, 24);
        controls.add(ungroundButton);
        
        addElement(controls);
        vis = new VisualizationControl();
        vis.setFieldConvolution(mDLIC);
        vis.setConvolutionModes(DLIC.DLIC_FLAG_E|DLIC.DLIC_FLAG_EP);

   

        addElement(vis);

        resetPointCharges();
        addActions();
        addSelectListener(this);
        mViewer.doStatus(0);
        mSEC.init();
        resetCamera();
        reset();
    }

    void addActions() {
        TealAction ta = new TealAction("Pentagon2", this);
        addAction("Help", ta);

      
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareToIgnoreCase("Charging by Induction") == 0) {
            if ((mFramework != null) && (mFramework instanceof TFramework)) {
                ((TFramework)mFramework).openBrowser("help/chargebyinduction.html");
            }
        } else if (e.getActionCommand().compareToIgnoreCase("Ground") == 0) {
            ground();
        } else if (e.getActionCommand().compareToIgnoreCase("Unground") == 0) {
            unground();
        } else {
            super.actionPerformed(e);
        }
    }

    public void propertyChange(PropertyChangeEvent pce) {
        super.propertyChange(pce);
    }

    public void reset() {
        super.reset();
        resetPointCharges();
        mSEC.stop();
        //resetCamera();
    }

    private void resetPointCharges() {
        Point3d[] positions = new Point3d[2 * N];
        Point3d position = null;
        double r1 = 1. + pointChargeRadius * 1.1;
        double r2 = 4. * Math.cos(Math.PI / 5.) - pointChargeRadius * 1.1;
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
        mViewer.setLookAt(new Point3d(0.0, 0.0, 1.), new Point3d(), new Vector3d(0., 1., 0.));
    }

    public synchronized void dispose() {
        super.dispose();
    }

    public void processSelection(SelectEvent se) {
        TDebug.println(0, se.getSource() + " select state = " + se.getStatus());
    }
    
    
    private void ground() {	 
        myWallG.setPosition(new Vector3d(0., -6.5, 0.));
        //myWallG.setEdge1(new Vector3d(8., 0., 0.));
    }

    private void unground() {

        myWallG.setPosition(new Vector3d(0., -4., 0.));
        //myWallG.setEdge1(new Vector3d(8., 0., 0.));
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
}
