/* $Id: TeachSpinBase.java,v 1.2 2007/08/21 22:05:55 pbailey Exp $ */
/**
 * A stand-alone simulation of the TeachSpin coil experiment.
 * 
 * @author Philip Bailey - Center for Educational Computing Initiatives / MIT
 * @version $Revision: 1.2 $
 */
package apps.physics.ilab;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Vector;

import javax.media.j3d.*;
import javax.vecmath.*;

import teal.app.*;
import teal.framework.TealAction;
import teal.math.RectangularPlane;
import teal.render.geometry.*;
import teal.render.HasPosition;
import teal.render.j3d.*;
import teal.render.j3d.loaders.Loader3DS;
import teal.render.primitives.Helix;
import teal.render.primitives.Line;
import teal.render.scene.TShapeNode;
import teal.sim.SimRendered;
import teal.sim.engine.EngineRendered;
//import teal.sim.physical.PhysicalObject;
//import teal.sim.physical.em.*;
import teal.sim.properties.IsSpatial;
import teal.sim.spatial.*;
import teal.ui.control.meters.ControlMeter;
import teal.util.*;

import com.sun.j3d.utils.geometry.GeometryInfo;

public class TeachSpinBase extends TealSimApp {

	private static final long serialVersionUID = 3258129146076214067L;
    public static final int FRAME_RATE = 1;
	public static final int METER = 2;
	public static final int CURRENT = 4;
	public static final int DUMMY = 8;
	public static final int MODEL = 16;
	public static final int SHOW_RINGS = 32;
	public static final int GENERATOR = 64;
	public static final int PROBE = 128;
	public static final int DEFAULT = METER | MODEL;
	public static final int TEST = METER | DUMMY | SHOW_RINGS;
	public static final int ALL = FRAME_RATE | METER | MODEL | DUMMY | CURRENT;
	// LOD FLAG results
	boolean useModel = false;
	boolean useCurrentSlider = false;
	boolean useMeter = false;
	boolean showRings = false;
	int nLines = 2;
	// Fieldline defaults
	double magS = 0.004;
	double magH = 0.01;
	int magMax = 100;
	double magMinD = 0.005;
	double fLen = 0.008; //0.008; //0.005
	int kMax = 160 * 1;
	double searchRad = 0.005;
	double minD = 0.01;
	int fMode = FieldLine.RUNGE_KUTTA;
	int symCount = 120;
	// physical constants & default values
	double radius = 0.06;
	double origY = -0.003;
	double tRadius = 0.0075;
	double coilHOff = 0.037;
	double defaultCurrent = 0.0; //-0.163;
	double numCoils = 168.0;
	double magMass = 0.002;
	double magMu = 0.2;
	double iScale = 0.1;
	// Object positions
	Vector3d modelPos;
	Vector3d coilPos;
	Vector3d coil2Pos;
	Vector3d rocPos;
	Vector3d roc2Pos;
	Vector3d magPos;
	// World Elements
	MagneticDipole m1;
	RingOfCurrent roc;
	RingOfCurrent roc2;
	FieldLine roc2FL1 = null;
	
	// MOdels
	SimRendered pb1;
	SimRendered model_1;
	SimRendered model_2;
	//Optional Elements
	TellFlux tf = null;;
	GridNode gridNode;
	int gridSize = 60;
	// GUI components
	ControlMeter meter;
	// State Values
	boolean sameState = false;
	boolean invertCur = true;
	boolean doBoth = true;
	//boolean isGenerating = false;
	double curValue = 0.0;
	Tuple3d cameras[] = {new Point3d(0., 0.04, 0.15), new Point3d(0., 0., 0.), new Vector3d(0., 1., 0.), new Point3d(0, 2.0, 0.),
			new Point3d(0.0, 0., 0.), new Vector3d(0., 1., 0.)};
	Vector3d fluxPoints[] = {
			new Vector3d(radius + (tRadius * 2.0), coilHOff + (tRadius * 2.0), 0.),
			new Vector3d(radius - (tRadius * 2.0), coilHOff - (tRadius * 2.0), 0.)};
	
	protected Vector fLines;
	protected FieldLineManager fmanager;
	File curDir = null;

	private void setCamera(int loc) {
		int offset = 3 * loc;
		mViewer.setLookAt((Point3d) cameras[offset++], (Point3d) cameras[offset++], (Vector3d) cameras[offset]);
	}

	public TeachSpinBase() {
		this(2, 168., -1, ALL | GENERATOR);
	}

	public TeachSpinBase(int maxLines, double coilsPerRing, int debugLevel, int lod) {
		super();
		//setGui(new SimGUI());
		nLines = maxLines;
		useCurrentSlider = ((lod & CURRENT) == CURRENT);
		useModel = ((lod & MODEL) == MODEL);
		useMeter = ((lod & METER) == METER);
		showRings = ((lod & SHOW_RINGS) == SHOW_RINGS);
		TDebug.setGlobalLevel(debugLevel);
		//TDebug.setOutput("testOut.txt");
		modelPos = new Vector3d(0.038, -0.01, 0.);
		numCoils = coilsPerRing;
		roc2Pos = new Vector3d(0., -coilHOff, 0.);
		rocPos = new Vector3d(0., coilHOff, 0.);
		coilPos = new Vector3d(rocPos);
		coil2Pos = new Vector3d(coilPos.x, coilPos.y + 10, coilPos.z);
		magPos = new Vector3d(0., origY, 0.);
		try {
			title = "SimLab Teach Spin";
			//mViewer.setBounds(20, 20, 580, 540);
			//mViewer.getBackgroundNode().setColor(new Color3f(new Color(37, 49, 80)));
			BoundingSphere bs = new BoundingSphere(new Point3d(0., 0, 0.), 0.250);
			theModel.setBoundingArea(bs);
			mViewer.setBoundingArea(bs);
			mDLIC.setSize(new Dimension(512,512));
			mDLIC.setComputePlane(new RectangularPlane(bs));
			Vector3d mouseScale = mViewer.getMouseMoveScale();
			mouseScale.x *= 0.01;
			mouseScale.y *= 0.01;
			mouseScale.z *= 0.1;
			mViewer.setMouseMoveScale(mouseScale);
			mouseScale = mViewer.getVpTranslateScale();
			mouseScale.x *= 0.01;
			mouseScale.y *= 0.01;
			mouseScale.z *= 0.1;
			mViewer.setVpTranslateScale(mouseScale);
			mViewer.setShowGizmos(false);
			//Create the EM elements
			m1 = new MagneticDipole();
			m1.setRadius(0.005);
			m1.setLength(0.010);
			m1.setPosition(magPos);
			m1.setRotable(false);
			m1.setMoveable(false);
			m1.setPickable(false);
			m1.setMu(magMu);// 0.2
			m1.setMass(magMass);
			//m1.setDrawn(false);
			roc = new RingOfCurrent();
			roc.setCurrent(defaultCurrent);
			roc.setInducing(false);
			roc.setPosition(rocPos);
			roc.setPickable(false);
			roc.setMoveable(false);
			roc.setRotable(false);
			roc.setRadius(radius);
			roc.setTorusRadius(tRadius);
			//roc.setDrawn(showRings);
			roc2 = new RingOfCurrent();
			roc2.setCurrent(defaultCurrent);
			roc2.setInducing(false);
			roc2.setPosition(roc2Pos);
			roc2.setPickable(false);
			roc2.setMoveable(false);
			roc2.setRotable(false);
			roc2.setRadius(radius);
			roc2.setTorusRadius(tRadius);
			//roc2.setDrawn(showRings);
			//Create a Rendered3D to hold the Max model
			if (useModel) {
                Node3D bgCoil = (Node3D) new Loader3DS().getTNode3D(URLGenerator.getResource("resources/models/1_Coil_1.3DS"));
				//Node3D bgCoil = (Node3D) Factory3D.getNode(URLGenerator.getResource("resources/models/1_Coil_1.3DS"));
               //bgCoil.compile();
               
               roc.setNode3D(bgCoil);
               Transform3D trans = new Transform3D();
               trans.setScale(1./39.37);
               trans.setTranslation(new Vector3d(rocPos));
               roc.setModelOffsetTransform(trans);
               
               //There's probably some way to clone this resource without having to load it again, but I couldn't figure it out.
               Node3D bgCoil2 = (Node3D) new Loader3DS().getTNode3D(URLGenerator.getResource("resources/models/1_Coil_1.3DS"));
               //bgCoil2.duplicateNode(bgCoil,true);
               roc2.setNode3D(bgCoil2);
               Transform3D trans2 = new Transform3D(trans);
               trans2.setTranslation(new Vector3d(rocPos));
               roc2.setModelOffsetTransform(trans2);
               
			}
			
            
            addElement(m1);
			addElement(roc);
            addElement(roc2);
            
			// Add GUI components
			
			if (useMeter) {
				meter = new ControlMeter(160, 160);
				meter.setDisplayRange(-1., 1.);
				meter.setDeviceAndPropertyName("Coil 1", "Current");
			}

			//layoutNormal();
			
			if (useMeter && meter != null)
				addElement(meter);
			
			
			// Optioonal tools
			if (((lod & PROBE) == PROBE)) {
				tf = new TellFlux();
				tf.setPosition(new Vector3d(0.001, 0.001, 0.));
				tf.setPickable(true);
				tf.setSelectable(true);
				addElement(tf);
			}
			
			if (((lod & DUMMY) == DUMMY)) {
				makeDummy();
			}
			
			setCurrent(defaultCurrent);
			
			fLines = new Vector();
		
			FieldLine fl = null;
			
			/// top ring (roc) fieldlines
			fl = makeFLine(-60.0, roc, Color.WHITE, fLen, kMax, FieldLine.RUNGE_KUTTA);
			fl.setRKTolerance(1e-5);
			fl.setSArc(fLen*0.2);
			fl.setKMax(2*kMax);
			
			fLines.add(fl);
			addElement(fl);
		
			/// bottom ring (roc2) fieldlines
			roc2FL1 = makeFLine(-60.0, roc2, Color.WHITE, fLen, kMax, FieldLine.RUNGE_KUTTA);
			roc2FL1.setRKTolerance(1e-5);
			roc2FL1.setSArc(fLen*0.2);
			roc2FL1.setKMax(2*kMax);
			
			fLines.add(roc2FL1);
			addElement(roc2FL1);
		
			
			fl = makeFLine(60.0, m1, Color.WHITE, fLen, kMax, fMode);
			//fl.setRKTolerance(1e-5);
			fLines.add(fl);
			addElement(fl);
			
			fl = makeFLine(200.0, m1, Color.WHITE, fLen, kMax, fMode);
			fLines.add(fl);
			addElement(fl);
			
			fmanager = new FieldLineManager();
			fmanager.setFieldLines(fLines);
			addElement(fmanager);
	
			Helix line = new Helix(new Vector3d(0., 4. * coilHOff, 0.),(HasPosition) m1);
			line.setColor(new Color(200, 200, 200));
			line.setRadius(0.6f*line.getRadius());
			addElement(line);
			

			TealAction a = new TealAction("Do B", TealSimApp.DLIC_B, this);
			addAction("Actions", a);
			a = new TealAction("Do Potential", TealSimApp.DLIC_BP, this);
			addAction("Actions", a);
			
			//a = new TealAction("Do Flux", SimApp.DLIC_BF, this);
			//addAction("Actions", a);
			a = new TealAction("Toggle Lines", "FM_TOGGLE_LINES", this);
			addAction("Actions", a);
			theModel.setCheckFrameRate((lod & FRAME_RATE) == FRAME_RATE);
			mViewer.setFogEnabled(true);
			mViewer.setFogTransformFrontScale(0.);
			mViewer.setFogTransformBackScale(0.02);
						
		} catch (Exception ex) {
			TDebug.println(0, ex.getMessage());
			ex.printStackTrace();
		}
	}

	public void initGUI() {
		TDebug.println(1, "SimLabApp.initGUI() ");
		if (true) {
			setGui(new SimGUI());
			//setGui(new TDefaultGUI());
		} else {
			setGui(new SimGUI());
		}
		TDebug.println(1, "GUI = " + mGUI.getClass().getName());
	}



	public void reset() {
		mSMC.stop();
		m1.setPosition(magPos);
		mViewer.displayBounds();
		mDLIC.setVisible(false);
		theModel.requestRefresh();
	}

	synchronized void setOpposite(boolean state) {
		if (state) {
			setInvert(true);
			setBoth(true);
			mStatusBar.setText("Both Rings - Opposite Current", false);
		}
	}

	synchronized void setSame(boolean state) {
		if (state) {
			setInvert(false);
			setBoth(true);
			mStatusBar.setText("Both Rings - Same Current", false);
		}
	}

	synchronized void setTopOnly(boolean state) {
		if (state) {
			setInvert(false);
			setBoth(false);
			mStatusBar.setText("Top Ring Only", false);
		}
	}

	synchronized void setBoth(boolean state) {
		if (state != doBoth) {
			TDebug.println(0, "setting doBoth to: " + state);
		    if(!state)
                roc2.setCurrent(0.);
			roc2.setGeneratingB(state);
			roc2FL1.setDrawn(state);
            theModel.requestRefresh();

		}
			doBoth = state;
	}


	void setInvert(boolean state) {
		//TDebug.println(0,"setting inverter to: " + state);
		invertCur = state;
	
		setCurrent(getCurrent());
	}

	public double getCurrent() {
		return curValue;
	}

	public void setCurrent(double d) {
		//TDebug.println(0,"setCurrent: " + d);
		curValue = d;
		double cValue = -curValue * numCoils;
		//double cValue = -curValue ;
		roc.setCurrent(cValue);
		if (useMeter)
			meter.setValue(d);
		//current.setValue(d,false);
		if (doBoth) {
			try {
				if (invertCur) {
					roc2.setCurrent(-cValue);
				} else {
					roc2.setCurrent(cValue);
				}
			} catch (Exception eex) {
				TDebug.printThrown(0, eex);
			}
		}
		theModel.requestRefresh();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().compareToIgnoreCase("Camera Reset") == 0) {
			setCamera(0);
			;
		} else if (e.getActionCommand().compareToIgnoreCase("Camera 1") == 0) {
			setCamera(0);
		} else if (e.getActionCommand().compareToIgnoreCase("Camera 2") == 0) {
			setCamera(1);
		} else {
			super.actionPerformed(e);
		}
	}

	public void setNumCoils(double num) {
		numCoils = num;
		theModel.requestRefresh();
	}



	protected FieldLine makeFLine(double val, PhysicalObject obj, Color col) {
		return makeFLine(val, obj, null, fLen, kMax, fMode);
	}

	protected FieldLine makeFLine(double val, PhysicalObject obj, Color color, double fLen, int kMax, int fMode) {
		Color col = color;
		//col = null;
		Vector3d start = new Vector3d(0, 0, 0);
		Vector3d positive = new Vector3d(1, 0, 0);
		Vector3d negative = new Vector3d(-1, 0, 0);
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
		fl.setMinDistance(minD);
		fl.setIntegrationMode(fMode);
		fl.setKMax(kMax);
		fl.setSArc(fLen);
		fl.setSymmetryCount(symCount);
		fl.setColorMode(FieldLine.COLOR_VERTEX);
		fl.setColorScale(fl.getColorScale() * 5.);
		
		fl.setReceivingFog(true);
		
		return fl;
	}

	void makeDummy() {
		SimRendered cd1 = new SimRendered();
		SimRendered cd2 = new SimRendered();
		SimRendered d3 = new SimRendered();
		cd1.setPosition(new Vector3d(0., -coilHOff, 0.));
		cd2.setPosition(new Vector3d(0., coilHOff, 0.));
		TShapeNode cy1 = new ShapeNode();
		TShapeNode cy2 = new ShapeNode();
		TShapeNode cy3 = new ShapeNode();
		GeometryInfo cylg = Cylinder.makeGeometry(12, 0.065, 0.015);
		cy1.setGeometry(cylg);
		cy1.setColor(Color.GREEN);
		cy2.setGeometry(cylg);
		cy2.setColor(Color.BLUE);
		cd1.setNode3D(cy1);
		cd2.setNode3D(cy2);
		GeometryInfo g = Cylinder.makeGeometry(12, 0.005, 0.054);
		cy3.setGeometry(g);
		d3.setColor(Color.GRAY);
		d3.setNode3D(cy3);
		addElement(cd1);
		addElement(cd2);
		addElement(d3);
		double y = -0.04;
		for (int i = 0; i < 9; i++) {
			Line l = new Line(new Vector3d(-0.04, y, 0.), new Vector3d(0.04, y, 0.));
			l.setColor(Color.BLACK);
			y += 0.01;
			addElement(l);
		}
	}
	
	
	public class TellFlux extends EngineRendered implements IsSpatial {

		private static final long serialVersionUID = 3689068430771042099L;

        public TellFlux() {
			super();
		}

		public void needsSpatial() {
			
		}
		public void setPosition(Vector3d pos) {
			super.setPosition(pos);
			if (theEngine != null) {
				//TDebug.println("position: " + getPosition() + "\tFlux: " +
				// theEngine.getBField().getFlux(getPosition()));
			}
		}

		public void nextSpatial() {
			if (theEngine != null) {
				//TDebug.println("position: " + getPosition() + "\tFlux: " +
				// theEngine.getBField().getFlux(getPosition()));
			}
		}
	}
}