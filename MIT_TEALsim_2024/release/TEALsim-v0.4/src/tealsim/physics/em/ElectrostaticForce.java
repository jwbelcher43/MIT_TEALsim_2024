/* $Id: ElectrostaticForce.java,v 1.7 2007/12/04 21:02:12 pbailey Exp $ */

/**
 * A demonstration implementation of the TealFramework.
 *
 * @author Philip Bailey - Center for Educational Computing Initiatives / MIT
 * @version $Revision: 1.7 $
 */

package tealsim.physics.em;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.ArrayList;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.swing.JButton;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import teal.field.Field;
import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.math.RectangularPlane;
import teal.render.j3d.Node3D;
import teal.render.j3d.WallNode;
import teal.render.viewer.TViewer;
import teal.sim.collision.CollisionController;
import teal.sim.collision.SphereCollisionController;
import teal.sim.control.VisualizationControl;
import teal.physics.em.SimEM;
import teal.physics.em.EField;
import teal.sim.engine.GenericForce;
import teal.physics.physical.RectangularBox;
import teal.physics.physical.Wall;
import teal.physics.em.CylindricalField;
import teal.physics.em.PointCharge;
import teal.sim.spatial.FieldConvolution;
import teal.sim.spatial.FieldDirectionGrid;
import teal.sim.spatial.FieldLine;
import teal.sim.spatial.FieldLineManager;
import teal.sim.spatial.FluxFieldLine;
import teal.ui.control.ControlGroup;
import teal.ui.control.PropertyDouble;
import teal.ui.swing.JTaskPane;
import teal.ui.swing.JTaskPaneGroup;
import teal.util.TDebug;
import teal.visualization.dlic.DLIC;

public class ElectrostaticForce extends SimEM {

    private static final long serialVersionUID = 3258417248187135287L;

    int extras = 0;
    boolean doModel = false;
    JTaskPane taskPane;
    JTaskPaneGroup paramgroup, visgroup;

    JButton but = null;
    JButton but1 = null;
    PointCharge pointCharge;
    PointCharge imageCharge;
    CylindricalField cylField;
    PropertyDouble slidercylField;
    Wall upperPlate;
    Wall lowerPlate;
    //SimRendered vgModel;
    //Node3D node1;
    //SimRendered vgModel2;
    //Node3D node2;
    File curDir = null;
    FieldLineManager fmanager;
    FieldConvolution mDLIC = null;

    double potentialScale = 1100.;

    Vector3d pcPosition = new Vector3d(0., 2., 0.);
    double scale = 158.226;
    Vector3d modelPosition = new Vector3d(-117.634, -373.871, -1235.645);
    Tuple3d cameras[] = { new Point3d(0.0, 0.17, 0.7), new Point3d(0., 0.17, 0.), new Vector3d(0., 1., 0.),
            new Point3d(3.0, 2.0, 100.), new Point3d(0.0, 2.0, 0.), new Vector3d(0., 1., 0.),
            new Point3d(0.0, 20.0, 0.), new Point3d(0.0, 0.0, 0.), new Vector3d(0., 0., 01.),
            new Point3d(20.0, 0.0, 0.), new Point3d(0.0, 0.0, 0.), new Vector3d(0., 01., 0.) };

    // PointCharge pc;    
    public ElectrostaticForce() {

        super();

        title = "Electrostatic Force";
        
 

        TDebug.setGlobalLevel(0);
        //TDebug.println(0, "TDebug global level set to 0");
        theEngine.setBoundingArea(new BoundingSphere(new Point3d(0., 2., 0), 8));
        mViewer.setNavigationMode(TViewer.ORBIT | TViewer.VP_ZOOM);
        theEngine.setShowTime(false);

        FieldDirectionGrid fv = new FieldDirectionGrid();
        fv.setType(Field.E_FIELD);
        fv.setResolution(0);
        addElement(fv);
        mDLIC = new FieldConvolution();
        mDLIC.setComputePlane(new RectangularPlane(theEngine.getBoundingArea()));
        mDLIC.setField((EField) theEngine.getEField());

        // *********************** Generating various plates ***************************

        RectangularBox stopPlateGenerator = new RectangularBox();
        stopPlateGenerator.setPosition(new Vector3d(0., 5. - 0.125, 0.));
        stopPlateGenerator.setOrientation(new Vector3d(1., 0., 0.));
        stopPlateGenerator.setNormal(new Vector3d(0., 1., 0.));
        stopPlateGenerator.setLength(2.);
        stopPlateGenerator.setWidth(2.);
        stopPlateGenerator.setHeight(0.25);
        ArrayList stopPlateWalls = (ArrayList) stopPlateGenerator.getWalls();
        double min_ = Double.POSITIVE_INFINITY;
        for (int i = 0; i < stopPlateWalls.size(); i++) {
            Wall currentWall = (Wall) stopPlateWalls.get(i);
            WallNode currentWallNode = (WallNode) currentWall.getNode3D();
            double y = currentWall.getPosition().y;
            if (y < min_) {
                min_ = y;
                upperPlate = currentWall;
            }
            currentWall.setElasticity(0.);
            Appearance appearance = Node3D.makeAppearance(new Color3f(Color.LIGHT_GRAY), 0.5f, 0.0f, false);
            currentWallNode.setFillAppearance(appearance);
        }
        addElements(stopPlateWalls);

        RectangularBox floorPlateGenerator = new RectangularBox();
        floorPlateGenerator.setPosition(new Vector3d(0., -5., 0.));
        floorPlateGenerator.setOrientation(new Vector3d(1., 0., 0.));
        floorPlateGenerator.setNormal(new Vector3d(0., 1., 0.));
        floorPlateGenerator.setLength(18.);
        floorPlateGenerator.setWidth(4.);
        floorPlateGenerator.setHeight(10.);
        ArrayList floorPlateWalls = (ArrayList) floorPlateGenerator.getWalls();
        double max_ = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < floorPlateWalls.size(); i++) {
            Wall currentWall = (Wall) floorPlateWalls.get(i);
            WallNode currentWallNode = (WallNode) currentWall.getNode3D();
            double y = currentWall.getPosition().y;
            if (y > max_) {
                max_ = y;
                lowerPlate = currentWall;
            }
            currentWall.setElasticity(0.);
            Appearance appearance = Node3D.makeAppearance(new Color3f(Color.GRAY), 0.5f, 0.0f, false);
            currentWallNode.setFillAppearance(appearance);
        }
        addElements(floorPlateWalls);

        RectangularBox ceilingPlateGenerator = new RectangularBox();
        ceilingPlateGenerator.setPosition(new Vector3d(0., 11., 0.));
        ceilingPlateGenerator.setOrientation(new Vector3d(1., 0., 0.));
        ceilingPlateGenerator.setNormal(new Vector3d(0., 1., 0.));
        ceilingPlateGenerator.setLength(18.);
        ceilingPlateGenerator.setWidth(4.);
        ceilingPlateGenerator.setHeight(10.);
        ArrayList ceilingPlateWalls = (ArrayList) ceilingPlateGenerator.getWalls();
        for (int i = 0; i < ceilingPlateWalls.size(); i++) {
            Wall currentWall = (Wall) ceilingPlateWalls.get(i);
            WallNode currentWallNode = (WallNode) currentWall.getNode3D();
            currentWall.setElasticity(0.);
            Appearance appearance = Node3D.makeAppearance(new Color3f(Color.GRAY), 0.5f, 0.0f, false);
            currentWallNode.setFillAppearance(appearance);
        }
        addElements(ceilingPlateWalls);

        /*
         // Changing the colors of the upper and lower plates.
         WallNode tempWallNode = (WallNode) upperPlate.getNode3D();
         Appearance tempAppearance = Node3D.makeAppearance(Color.LIGHT_GRAY,0.5f,0.5f,false);
         tempWallNode.setFillAppearance(tempAppearance);
         
         tempWallNode = (WallNode) lowerPlate.getNode3D();
         tempAppearance = Node3D.makeAppearance(Color.LIGHT_GRAY,0.5f,0.5f,false);
         tempWallNode.setFillAppearance(tempAppearance);
         */

        // *****************************************************************************

        pointCharge = new PointCharge();
        pointCharge.setID("pointCharge");
        //pointCharge.setCharge(5.2);
        pointCharge.setCharge(0.0);
        pointCharge.setPosition(pcPosition);
        pointCharge.setPickable(false);
        pointCharge.setMoveable(true);
        pointCharge.setRadius(1.);
        pointCharge.addPropertyChangeListener("position", this);
        pointCharge.addPropertyChangeListener("charge", this);
        SphereCollisionController scd = new SphereCollisionController(pointCharge);
        scd.setRadius(1.);
        pointCharge.setCollisionController(scd);
        pointCharge.setColliding(true);
        addElement(pointCharge);

        imageCharge = new PointCharge();
        imageCharge.setID("imageCharge");
        //imageCharge.setCharge(-5.2);
        imageCharge.setCharge(0.0);
        imageCharge.setPosition(new Vector3d(0., -2.0, 0.));
        imageCharge.setPickable(false);
        imageCharge.setMoveable(false);
        imageCharge.setRadius(1.);
        imageCharge.setDrawn(false);
        //imageCharge.setGeneratingE(false);

        addElement(imageCharge);

        theEngine.setDamping(0.0);
        theEngine.setGravity(new Vector3d(0., -1., 0.));

        //testing CylindricalField
        cylField = new CylindricalField(new Vector3d(0., 0., 0.), new Vector3d(0, 1., 0.), 0.2);
        cylField.setID("cylField");
        //cylField.addPropertyChangeListener("magnitude",this);
        addElement(cylField);

        double[] flux = new double[8];
        flux[0] = theEngine.getEField().getFlux(new Vector3d(1.5, 0., 0.));
        flux[1] = theEngine.getEField().getFlux(new Vector3d(2., 0., 0.));
        flux[2] = theEngine.getEField().getFlux(new Vector3d(4.0, 0., 0.));
        flux[3] = theEngine.getEField().getFlux(new Vector3d(6.0, 0., 0.));
        flux[4] = theEngine.getEField().getFlux(new Vector3d(8.0, 0., 0.));
        flux[5] = theEngine.getEField().getFlux(new Vector3d(10., 0., 0.));
        flux[6] = theEngine.getEField().getFlux(new Vector3d(12., 0., 0.));
        flux[7] = theEngine.getEField().getFlux(new Vector3d(14., 0., 0.));

        Vector3d startScan = new Vector3d(0.1, 6.0, 0.);
        fmanager = new FieldLineManager();
        fmanager.setElementManager(this);

        for (int i = 2; i < 8; i++) {
            TDebug.println(1, "flux[" + i + "] = " + flux[i]);
            FieldLine fl = new FluxFieldLine(flux[i] * 0.05, startScan, new Vector3d(1., 0., 0.), 8.0);
            fl.setType(Field.E_FIELD);
            fl.setKMax(60);
            fl.setSArc(0.1);
            fl.setBuildDir(FieldLine.BUILD_BOTH);
            fl.setSymmetryCount(2);
            fl.setColorScale(fl.getColorScale()*100.0);
            //addElement(fl);
            fmanager.addFieldLine(fl);
            //TDebug.println(2,"fluxline added");
        }
        fmanager.setColorScale(0.1);
        addElement(fmanager);
        
        addElement(new GenericForce(new Vector3d(0.0, -0.04, 0.)));

        //mSEC.setBounds(45, 500, 400, 32);

        slidercylField = new PropertyDouble();
        slidercylField.setText("Potential (Volts):");
        slidercylField.setMinimum(0.);
        slidercylField.setMaximum(0.45455 * potentialScale);
        slidercylField.setPaintTicks(true);
        //slidercylField.setBounds(500, 350, 400, 32);
        slidercylField.setBounds(35, 530, 415, 50);
        slidercylField.setBorder(null);
        //slidercylField.addRoute(cylField, "magnitude");
        slidercylField.addRoute(this, "magnitude");
        //slidercylField.addPropertyChangeListener("value", this);
        slidercylField.setValue(0.0);
        //addElement(slidercylField);
        slidercylField.setVisible(true);

        
        ControlGroup paramgroup = new ControlGroup();
        paramgroup.setText("Parameters");
        paramgroup.add(slidercylField);
        addElement(paramgroup);

        VisualizationControl vis = new VisualizationControl();
        vis.setFieldConvolution(mDLIC);
        vis.setConvolutionModes(DLIC.DLIC_FLAG_E);
        vis.setFieldLineManager(fmanager);
        vis.setActionFlags(0);
        vis.setSymmetryCount(2);
        vis.setColorPerVertex(false);
        addElement(vis);
        

        theEngine.setDeltaTime(0.25);

        
        addActions();
        mSEC.init();
        reset();
        resetCamera(0);
        
    }

    public void setScale(double s) {
        scale = s;
        //node1.setScale(scale);
        //node2.setScale(scale);
    }

    public double getScale() {
        return scale;
    }

    void addActions() {
        TealAction ta = new TealAction("Electrostatic Force", this);
        addAction("Help", ta);

        

        
        //        ta = new TealAction("Reset Camera", this);
        //        addAction("Actions", ta);

        if (extras > 0) {
            ta = new TealAction("View 1", this);
            addAction("Actions", ta);
            ta = new TealAction("Closeup", this);
            addAction("Actions", ta);
            ta = new TealAction("Camera 3", this);
            addAction("Actions", ta);
            ta = new TealAction("Camera 4", this);
            addAction("Actions", ta);
            ta = new TealAction("Show 1", this);
            addAction("Actions", ta);
            ta = new TealAction("Hide 1", this);
            addAction("Actions", ta);
            ta = new TealAction("Show 2", this);
            addAction("Actions", ta);
            ta = new TealAction("Hide 2", this);
            addAction("Actions", ta);
            ta = new TealAction("ViewStatus", this);
            addAction("Actions", ta);
            ta = new TealAction("ModelPosition", this);
            addAction("Actions", ta);

        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareToIgnoreCase("Electrostatic Force") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/esforce.html");
        	}
        } else if (e.getActionCommand().compareToIgnoreCase("Reset Camera") == 0) {
            resetCamera(0);
        } 
        /*
         else if (extras > 0)
         {
         if (e.getActionCommand().compareToIgnoreCase("Show 1") == 0) {
         vgModel.setDrawn(true);			
         }
         else if (e.getActionCommand().compareToIgnoreCase("Hide 1") == 0) {
         vgModel.setDrawn(false);				
         }
         else if (e.getActionCommand().compareToIgnoreCase("Show 2") == 0) {
         vgModel2.setDrawn(true);				
         }
         else if (e.getActionCommand().compareToIgnoreCase("Hide 2") == 0) {
         vgModel2.setDrawn(false);				
         }
         else if (e.getActionCommand().compareToIgnoreCase("View 1") == 0) {
         resetCamera(1);			
         }
         else if (e.getActionCommand().compareToIgnoreCase("viewstatus") == 0) {
         mViewer.doStatus(-1);			
         } 
         else if (e.getActionCommand().compareToIgnoreCase("modelposition") == 0) {
         System.out.println("Model Position: " + vgModel.getPosition());			
         } 
         else if (e.getActionCommand().compareToIgnoreCase("closeup") == 0) {
         resetCamera(0);			
         } 
         else if (e.getActionCommand().compareToIgnoreCase("Camera 3") == 0) {
         resetCamera(2);			
         }
         else if (e.getActionCommand().compareToIgnoreCase("Camera 4") == 0) {
         resetCamera(3);			
         }
         } */
        else {
            super.actionPerformed(e);
        }
    }

    public double getMagnitude() {
        return 0.;
    }

    public void setMagnitude(double mag) {
        mag /= potentialScale;
        cylField.setMagnitude(mag);
        CollisionController pCcc = pointCharge.getCollisionController();
        CollisionController lPcc = lowerPlate.getCollisionController();
        if (((pCcc.collisionStatus(lPcc) | CollisionController.TOUCHES) == 0)
            || ((pCcc.collisionStatus(lPcc) | CollisionController.TOUCHES) == 0) || pointCharge.isAdheredTo(lowerPlate)
            || pointCharge.isAdheredTo(upperPlate)) {
            pointCharge.setCharge(mag * 4.0 * Math.PI);
            //TDebug.println(0,"Touching a plate!");
        } else {
            //TDebug.println(0,"Not touching a plate!");
        }
        //imageCharge.setCharge(-mag*4.0*Math.PI);

    }

    public void propertyChange(PropertyChangeEvent pce) {
        Object source = pce.getSource();
        if (source == pointCharge) {
            if (pce.getPropertyName().compareTo("charge") == 0) {
                Double val = (Double) pce.getNewValue();
                imageCharge.setCharge(-val.doubleValue());
            } else if (pce.getPropertyName().compareTo("position") == 0) {
                Vector3d pos = (Vector3d) pce.getNewValue();
                imageCharge.setY(-pos.y);
                
            }
            //pointCharge.reconcile();
            //		} else if (source == pc) {
            //			TDebug.println(0, "PC Position: " + pc.getPosition());
        } else {
            super.propertyChange(pce);
        }
    }

//    protected void saveDLICImage() {
//        if (mDLIC.isImageGenerated()) {
//            BufferedImage img = (BufferedImage) mDLIC.getImage();
//            if (img != null) {
//                if (fc == null) fc = new JFileChooser();
//                if (curDir != null) fc.setCurrentDirectory(curDir);
//                int status = fc.showSaveDialog(this);
//                if (status == JFileChooser.APPROVE_OPTION) {
//                    File file = null;
//                    curDir = fc.getCurrentDirectory();
//                    try {
//                        file = fc.getSelectedFile();
//                        ImageIO.writeJPEG(img, 300, file);
//                    } catch (IOException fnf) {
//                        TDebug.printThrown(fnf, " Trying to save file: " + file);
//                    }
//
//                }
//            }
//        }
//    }

    public void reset() {
        pointCharge.setPosition(pcPosition);
        pointCharge.setVelocity(new Vector3d());
        //resetCamera(0);
        slidercylField.setValue(0.);
        theEngine.requestRefresh();
    }

    private void resetCamera(int loc) {
        int offset = loc;
        mViewer.setLookAt((Point3d) cameras[offset++], (Point3d) cameras[offset++], (Vector3d) cameras[offset]);

    }

    
}
