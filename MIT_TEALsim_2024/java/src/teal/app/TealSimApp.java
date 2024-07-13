/* $Id: TealSimApp.java,v 1.44 2007/08/14 19:36:37 pbailey Exp $ */
/**
 * A demonstration implementation of the TFramework.
 * 
 * @author Philip Bailey - Center for Educational Computing Initiatives / MIT
 * @version $Revision: 1.44 $
 */

package teal.app;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.media.j3d.BoundingSphere;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import teal.core.TElement;
import teal.field.Field;
import teal.field.Potential;
import teal.field.Vector3dField;
import teal.framework.TealAction;
import teal.math.RectangularPlane;
import teal.render.viewer.AbstractViewer3D;
import teal.render.viewer.SelectManagerImpl;
import teal.sim.TSimElement;
import teal.sim.engine.TEngineControl;
import teal.sim.engine.SimEngine;
import teal.sim.engine.EngineControl;
import teal.physics.em.EMEngine;
import teal.physics.physical.PhysicalObject;
import teal.physics.em.MagneticDipole;
import teal.physics.em.RingOfCurrent;
import teal.sim.spatial.FieldConvolution;
import teal.sim.spatial.FieldDirectionGrid;
import teal.sim.spatial.FieldLine;
import teal.sim.spatial.FluxFieldLine;
import teal.ui.control.PropertyInteger;
import teal.util.TDebug;
/**
 * Simple TealSim application, to be used as a base class for applications that directly
 * implement an application. Does not work with TSimulations.
 * 
 * @author pbailey
 * @deprecated
 */
public class TealSimApp extends TealDefaultApp {

    private static final long serialVersionUID = 3760566394747826744L;

    /** Action command to trigger an IDraw B field calculation */
    public final static String DLIC_B = "DLIC_B";
    /** Action command to trigger an IDraw E field calculation */
    public final static String DLIC_E = "DLIC_E";
    /** Action command to trigger an IDraw EP calculation */
    public final static String DLIC_BP = "DLIC_BP";
    public final static String DLIC_EP = "DLIC_EP";
    public final static String DLIC_EF = "DLIC_EF";
    public final static String DLIC_BF = "DLIC_BF";
    public final static String DLIC_BCMF = "DLIC_BCMF";
    public final static String DLIC_EPOTENTIAL = "DLIC_EPOTENTIAL"; // Electric
    // Potential.
    /** Action command to trigger a reset */
    public final static String RESET = "RESET";
    /** Action command to trigger a camera reset to a default location */
    public final static String RESET_CAMERA = "Reset Camera";
    /** Action command to trigger a camera reset to a default location */
    public final static String VIEW_STATUS = "View Status";
    /** Action command to toggle fieldline display */
    public final static String TOGGLE_LINES = "TOGGLE_LINES";

    public static boolean showLoadOptions = false;

    /** The Simulation model */
    protected SimEngine theModel;
    /** the models Simulation controller */
    protected EngineControl mSMC;
    /** the lineConvolution image generator associated with this viewer */
    protected FieldConvolution mDLIC = null;
    protected boolean showFieldLines = true;
    protected boolean showVectorField = false;
    double magS = 0.004;
    double magH = 0.01;
    int magMax = 100;
    double magMinD = 0.005;
    protected double fLen = 0.008; //0.008; //0.005
    protected int kMax = 160 * 1;
    double searchRad = 0.005;
    double minD = 0.01;
    protected int fMode = FieldLine.RUNGE_KUTTA;
    //int fMode = FieldLine.EULER;
    protected int symCount = 80;
    protected FieldDirectionGrid fv;
    protected PropertyInteger fvSlider;
    protected PropertyInteger flSlider;
    protected Vector fLines = new Vector();

    public TealSimApp() {
        super();
        TDebug.println("TealSimApp Constructor:");
        id = "SimApp";
        title = "SimApp";
        theModel = new SimEngine();
        theModel.setBoundingArea(new BoundingSphere(new Point3d(0., 2., 0), 8));
        theModel.addViewer(mViewer);
        mSMC = new EngineControl(EngineControl.DO_ALL);
        //mSEC = new SimEngineControl(SimEngineControl.DO_DEFAULT);
        mSMC.setID("SMC");
        mSMC.setEngine(theModel);
        mSMC.addResetActionListener(this);
        mSMC.setBounds(45, 473, 400, 35);
        //mSEC.setSize(new Dimension(400, 35));
        mSMC.setVisible(true);
        addElement(mSMC, false);
        mDLIC = new FieldConvolution();
        mDLIC.setSize(new Dimension(512, 512));
        mDLIC.setComputePlane(new RectangularPlane(theModel.getBoundingArea()));
        addElement(mDLIC, false);
        mDLIC.setVisible(false);
        mDLIC.addProgressEventListener(mStatusBar);
        TealAction ta = new TealAction("Reset Camera", RESET_CAMERA, this);
        addAction("View", ta);
        //ta = new TealAction("View Status", this);
        //addAction("Actions", ta);

        // Should comment these out
        if (showLoadOptions) {
            ta = new TealAction("Load", this);
            addAction("File", ta);
            ta = new TealAction("Save", this);
            addAction("File", ta);
            ta = new TealAction("RemoveAll", this);
            addAction("File", ta);
        }

        //initGUI();
        TDebug.println("GUI = " + mGUI.getClass().getName());
    }

    public void initGUI() {
        //System.out.println("TealSimApp initGUI: ");
        setGui(new SimGUI());
        //setGui(new SimGUI());
    }

    public void addTElement(TElement element, boolean addToList) throws IllegalArgumentException {
        TDebug.println(1, "TealSimApp addTElement: " + element);
        if (element instanceof TSimElement) {
            if (theModel != null) {
                TDebug.println(2, element + " added to SimEngine!");
                theModel.addSimElement((TSimElement) element);
            } else {
                TDebug.println(2, element + " was not added to SimEngine!");
            }
        }
        super.addTElement(element, addToList);
    }

    public void removeTElement(TElement element) {
        if (element instanceof TSimElement) {
            theModel.removeSimElement((TSimElement) element);
        }
        super.removeTElement(element);
    }

    protected void reset() {
        mSMC.stop();
    }

    public void resetCamera() {
        TDebug.println("TealSimApp: resetCamera");
        mViewer.displayBounds();
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.compareToIgnoreCase(RESET) == 0) {
            TDebug.println(1, "Reset called");
            reset();
        } else if (command.compareToIgnoreCase(RESET_CAMERA) == 0) {
            TDebug.println(1, "Reset Camera called");
            resetCamera();
        } else if (command.compareToIgnoreCase(VIEW_STATUS) == 0) {
            TDebug.println(1, "view Status");
            mViewer.doStatus();
            // Load & save stubs
        } else if (command.compareToIgnoreCase("Load") == 0) {
            TDebug.println(1, "Load");
            load();
        } else if (command.compareToIgnoreCase("Save") == 0) {
            TDebug.println(1, "save");
            save();
        } else if (command.compareToIgnoreCase("RemoveAll") == 0) {
            TDebug.println(1, "removeAll");
            removeElements();
        } else if (command.compareToIgnoreCase(DLIC_B) == 0) {
            mStatusBar.setText("Generating B Field image...");
            generateFieldImage(((EMEngine)theModel).getBField());
        } else if (command.compareToIgnoreCase(DLIC_E) == 0) {
            mStatusBar.setText("Generating E Field image...");
            generateFieldImage(((EMEngine)theModel).getEField());
        } else if (command.compareToIgnoreCase(DLIC_EF) == 0) {
            mStatusBar.setText("Generating E Flux image...");
            generateFluxImage(((EMEngine)theModel).getEField());
        } else if (command.compareToIgnoreCase(DLIC_EP) == 0) {
            mStatusBar.setText("Generating E Potential image...");
            generateFieldImage((Vector3dField) new Potential(((EMEngine)theModel).getEField()));
        } else if (command.compareToIgnoreCase(DLIC_BP) == 0) {
            mStatusBar.setText("Generating B Potential image...");
            generateFieldImage((Vector3dField) new Potential(((EMEngine)theModel).getBField()));
        } else if (command.compareToIgnoreCase(DLIC_BF) == 0) {
            mStatusBar.setText("Generating B Flux image...");
            generateFluxImage(((EMEngine)theModel).getBField());
        } else if (command.compareToIgnoreCase(DLIC_BCMF) == 0) {
            generateColorMappedFluxImage(((EMEngine)theModel).getBField());
        } else if (command.compareToIgnoreCase(DLIC_EPOTENTIAL) == 0) {
            mStatusBar.setText("Generating E Potential image...");
            generatePotentialImage(((EMEngine)theModel).getEField());
        } else if (command.compareToIgnoreCase("CLEAR_DLIC") == 0) {
            theModel.requestSpatial();
        } else if (e.getActionCommand().compareToIgnoreCase("TOGGLE_VECTOR_FIELD") == 0) {
            if (((JCheckBox) e.getSource()).isSelected() == false) {
                showVectorField = false;
                fvSlider.setEnabled(showVectorField);
                fv.setDrawn(showVectorField);
            } else {
                showVectorField = true;
                fvSlider.setEnabled(showVectorField);
                fv.setDrawn(showVectorField);
            }
            fv.needsSpatial();
            theModel.requestRefresh();
        } else if (command.compareToIgnoreCase(TOGGLE_LINES) == 0) {
//            if (showFieldLines) {
//                try {
//                    mViewer.addDontDraw(Class.forName("teal.sim.spatial.FieldLine"));
//                } catch (ClassNotFoundException ce) {
//                    TDebug.println(0, "Class Not Found: " + ce.getMessage());
//                }
//                showFieldLines = false;
//            } else {
//                try {
//                    mViewer.removeDontDraw(Class.forName("teal.sim.spatial.FieldLine"));
//                } catch (ClassNotFoundException ce) {
//                    TDebug.println(0, ce.getMessage());
//                }
//                showFieldLines = true;
//            }
//            if (flSlider != null) {
//                flSlider.setEnabled(showFieldLines);
//            }
//            theEngine.requestSpatial();
//            theEngine.requestRefresh();
        } else {
            super.actionPerformed(e);
        }
        System.gc();
    }

    public int getNumLines() {
        return symCount;
    }

    public void setNumLines(int num) {
        Iterator it = fLines.iterator();
        while (it.hasNext()) {
            FieldLine f = (FieldLine) it.next();
            f.setSymmetryCount(num);
        }
        theModel.requestRefresh();
        symCount = num;
    }

    protected void generatePotentialImage(Field field) {
        Cursor cr = getCursor();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        if (mSMC.getSimState() == TEngineControl.RUNNING) {
            mSMC.stop();
            theModel.doRefresh();
            Thread.yield();
        }
        mDLIC.setField(field);
        mDLIC.generatePotentialImage();
        mDLIC.getImage();
        setCursor(cr);
    }

    protected void generateFluxImage(Field field) {
        Cursor cr = getCursor();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        if (mSMC.getSimState() == TEngineControl.RUNNING) {
            mSMC.stop();
            theModel.doRefresh();
            Thread.yield();
        }
        mDLIC.setField(field);
        mDLIC.generateFluxImage();
        setCursor(cr);
    }

    protected void generateColorMappedFluxImage(Field field) {
        Cursor cr = getCursor();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        if (mSMC.getSimState() == TEngineControl.RUNNING) {
            mSMC.stop();
            theModel.doRefresh();
            Thread.yield();
        }
        mDLIC.setField(field);
        mDLIC.generateColorMappedFluxImage();
        setCursor(cr);
    }

    protected void generateFieldImage(Vector3dField field) {
        Cursor cr = getCursor();
        setCursor(new Cursor(Cursor.WAIT_CURSOR));
        if (mSMC.getSimState() == TEngineControl.RUNNING) {
            mSMC.stop();
            theModel.doRefresh();
            Thread.yield();
        }
        mDLIC.setField(field);
        mDLIC.generateImage();
        setCursor(cr);
    }

    protected FieldLine makeFLine(double val, PhysicalObject obj, Color col) {
        return makeFLine(val, obj, null, fLen, kMax, fMode);
    }

    protected FieldLine makeFLine(double val, PhysicalObject obj, Color color, double fLen, int kMax, int fMode) {
        Color col = color;
        Vector3d start = new Vector3d(0, 0, 0);
        Vector3d positive = new Vector3d(1, 0, 0);
        FluxFieldLine fl;
        if (obj == null) {
            fl = new FluxFieldLine(val, start, positive, searchRad);
        } else {
            if (obj instanceof RingOfCurrent) {
                fl = new FluxFieldLine(val, obj, true, true);
            } else if (obj instanceof MagneticDipole) {
                fl = new FluxFieldLine(val, obj, true, false);
                fl.setObjRadius(searchRad);
            } else {
                return null;
            }
        }
        fl.setMinDistance(minD * 0.5);
        fl.setIntegrationMode(fMode);
        fl.setKMax(kMax);
        fl.setSArc(fLen);
        fl.setColorMode(FieldLine.COLOR_VERTEX);
        fl.setReceivingFog(true);
        if (col != null) {
            fl.setColor(col);
        }
        return fl;
    }

    public synchronized void dispose() {
        if (mDLIC != null) {
            removeElement(mDLIC);
            mDLIC.dispose();
        }
        super.dispose();
    }

    public static void main(String args[]) {
        TealSimApp theApp = new TealSimApp();
        theApp.show();
    }

    public void load(InputStream input) {
        boolean hasNext = true;
        removeElements();
        if (theModel != null) {
            theModel.removeAll();
            theModel = null;
        }
        if (mSMC != null) {
            removeElement(mSMC);
            mSMC.dispose();
            mSMC = null;

        }
        if (theModel != null) {
            theModel.removeAll();
            theModel = null;
        }
        if (mViewer != null) {
            mViewer.dispose();
            mViewer = null;
        }
        AbstractViewer3D viewer = null;
        SimEngine world = null;
        EngineControl smc = null;
        try {
            XMLDecoder e = new XMLDecoder(new BufferedInputStream(input));
            Object obj = e.readObject();
            if (obj instanceof AbstractViewer3D) {
                viewer = (AbstractViewer3D) obj;
            } else {
                TDebug.println("ObjectType not as expectd: " + obj);
            }
            obj = e.readObject();
            if (obj instanceof SimEngine) {
                world = (SimEngine) obj;
            } else {
                TDebug.println("ObjectType not as expectd: " + obj);
            }
            obj = e.readObject();
            if (obj instanceof EngineControl) {
                smc = (EngineControl) obj;
            } else {
                TDebug.println("SMC ObjectType not as expectd: " + obj);
            }
            mSelect = new SelectManagerImpl();
            mViewer = viewer;
            mViewer.setSelectManager(mSelect);
            addElement(mViewer, false);

            theModel = world;
            theModel.addViewer(mViewer);
            mSMC = smc;
            mSMC.setEngine(theModel);
            mSMC.setResetActionListener(this);
            addElement(mSMC, false);

            while (hasNext) {
                try {
                    obj = e.readObject();
                    TDebug.println(0, "Loaded: " + obj);
                    addElement(obj);
                }
                catch (ArrayIndexOutOfBoundsException ore) {
                    hasNext = false;
                }
            }
            e.close();
        } catch (Exception fnf) {
            TDebug.printThrown(0, fnf, " Trying to load input");
        }
    }

    public void save() {

        File curDir = null;
        File file = null;

        if (fc == null) fc = new JFileChooser();
        if (curDir != null) fc.setCurrentDirectory(curDir);
        int status = fc.showSaveDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {

            curDir = fc.getCurrentDirectory();
            try {
                file = fc.getSelectedFile();

                XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));
                TDebug.println(0, "Saving: " + mViewer);
                e.writeObject(mViewer);
                TDebug.println(0, "Saving: " + theModel);
                e.writeObject(theModel);
                TDebug.println(0, "Saving: " + mSMC);
                e.writeObject(mSMC);
                ArrayList elements = new ArrayList(mElements.values());
                Iterator ie = elements.iterator();
                Object obj = null;
                while (ie.hasNext()) {
                    obj = ie.next();
                    TDebug.println(0, "Saving: " + obj);
                    e.writeObject(obj);
                }
                e.close();

            }

            catch (IOException fnf) {
                TDebug.printThrown(0, fnf, " Trying to save file: " + file);
            }
        }
    }

}