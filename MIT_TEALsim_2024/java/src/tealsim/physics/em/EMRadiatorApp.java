/*
 * Created on Nov 10, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package tealsim.physics.em;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

import javax.media.j3d.BoundingSphere;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.render.j3d.ViewerJ3D;
import teal.render.viewer.TViewer;
import teal.sim.constraint.LDPSConstraint;
import teal.sim.engine.TEngineControl;
import teal.sim.function.VectorGenerator;
import teal.physics.physical.Ball;
import teal.physics.em.SimEM;
import teal.physics.em.EMEngine;
import teal.physics.em.EMRadiator;
import teal.sim.simulation.SimWorld;
import teal.sim.spatial.FieldVector;
import teal.sim.spatial.RadiationFieldLine;
import teal.ui.control.ControlGroup;
import teal.ui.control.PropertyCheck;
import teal.ui.control.PropertyCombo;
import teal.util.TDebug;

public class EMRadiatorApp extends SimEM {

    private static final long serialVersionUID = 3690197650553452345L;

    EMRadiator radiator;
    LDPSConstraint rad_constraint = null;
    Ball rad_gizmo = null;
    PropertyCheck generate = null;
    VectorGenerator wave = null;
    PropertyCombo genselect = null;
    boolean isGenerating = false;
    RadiationFieldLine[] rfls;
    
    
    public EMRadiatorApp() {

        super();
        TDebug.setGlobalLevel(0);
        title = "Generating Plane Wave Radiation   ";
        
        

        // Here we set the bounding area of the model.  This should represent the characteristic size of the simulation 
        // space.
        theEngine.setBoundingArea(new BoundingSphere(new Point3d(), 10));
        // Here we set the generalized velocity-based damping of the system.
        theEngine.setDamping(0.0);
        // Here we set the gravitional force acting on the system (no gravity, in this case).
        theEngine.setGravity(new Vector3d(0., 0., 0.));
        // Here we set the time step of the simulation.
        theEngine.setDeltaTime(0.5);
       
        // Here we set the mouse-based camera navigation modes.  "ORBIT_ALL" indicates we are enabling translation, 
        // rotation, and zooming.
        mViewer.setNavigationMode(TViewer.ORBIT_ALL);
        // setShowGizmos() determines whether or not transform gizmos will appear on selected objects.
        mViewer.setShowGizmos(false);
        // Here we set the simulation controls to be visible.
        mSEC.setVisible(true);
        mSEC.rebuildPanel(0);


        

        
        // Below we create a series of FieldVectors (arrows) that will show the direction of the electric and magnetic 
        // fields along the axis perpendicular to the plate (x-axis).
        FieldVector fieldvec;
        for (int i = 0; i < 160; i++) {
            fieldvec = new FieldVector(new Vector3d(-20 + i * 0.25, 0, 0), FieldVector.E_FIELD, true);
            addElement(fieldvec);
            fieldvec = new FieldVector(new Vector3d(-20 + i * 0.25, 0, 0), FieldVector.B_FIELD, true);
            addElement(fieldvec);
        }

        // Below we create an EMRadiator object, which represents an oscillating plate of current.
        radiator = new EMRadiator();
        radiator.setSelectable(false);
        radiator.setMoveable(true);
        radiator.setPickable(false);
        radiator.setColliding(false);
        // setPropSpeed() sets the propagation speed of the fields it generates.
        radiator.setPropSpeed(2);
        // setHistoryLength() sets the history length of the radiator.  This is a feature of the algorithm used to generate
        // propagating fields, and essentially represents the number of unique field values generated by the radiator.
        // See EMRadiator for more information.
        radiator.setHistoryLength(200);

        addElement(radiator);

        // Below we add two RadiationFieldLines to trace the position of the plate.  This is a case of using one type of
        // for a purpose it wasn't intended for.  Despite the fact that this is technically a fieldline, we are not using
        // it to show a field.
        //
        // Note that since these fieldlines aren't being added to a FieldLineManager, they must be added to the world 
        // directly via addElement().
        RadiationFieldLine rfl = new RadiationFieldLine(radiator, Math.PI * 0.5);
        rfl.setKMax(100);
        rfl.setPropSpeed(1.);
        rfl.setColor(new Color(255, 255, 255));
        addElement(rfl);

        rfl = new RadiationFieldLine(radiator, Math.PI * -0.5);
        rfl.setKMax(100);
        rfl.setPropSpeed(1.);
        rfl.setColor(new Color(255, 255, 255));
        addElement(rfl);

        // Here we add a spring constraint to the EMRadiator.  When the user manipulates the plate, they are in fact 
        // manipulating this spring constraint, which in turn applies a force to the plate.  This effectively smooths out
        // discontinuities introduced to the system by user interaction (discontinuities in acceleration are ok, whereas 
        // discontinuities in position are not).
        rad_constraint = new LDPSConstraint();
        rad_constraint.setPoint(new Vector3d(0., 0., 0.));
        rad_constraint.setK1(2.0); //8.);
        rad_constraint.setK2(4.); //30.);
        rad_constraint.setP(1.); //2.5); //0.25);
        radiator.setConstraint(rad_constraint);
        radiator.setConstrained(true);
        
        rad_gizmo = new Ball();
        rad_gizmo.setPosition(radiator.getPosition());
        rad_gizmo.setPickable(true);
        rad_gizmo.setSelectable(true);
        rad_gizmo.setColliding(false);
        rad_gizmo.setRadius(0.5);
        rad_gizmo.setColor(Color.LIGHT_GRAY);
        
        // We add a propertyChangeListener to this object, so that when the user moves the dummy object, the constraint 
        // is moved with it.
        rad_gizmo.addPropertyChangeListener("position", this);
        addElement(rad_gizmo);

        // Below we create a VectorGenerator, which will act as a signal generator to drive the motion of the plate when
        // the generator is enabled.  It should generate vectors interpolated sinusoidally between the two supplied points.
        // The output of the generator will be sent to the plate by way of a propertyChangeEvent.
        isGenerating = true;
        wave = new VectorGenerator(new Vector3d(0, -10, 0), new Vector3d(0, 10, 0), 3, false);
        wave.setScale(1.);
        wave.setSpeed(0.15);
        //wave.setHz(0.50);
        wave.setStepping(isGenerating);
        wave.addPropertyChangeListener("value", this);
        addElement(wave);

        
        // Here we create a GUI checkbox that will enable or disable the VectorGenerator.
        generate = new PropertyCheck();
        generate.setText("Motion On:");
        generate.setValue(isGenerating);
        generate.addPropertyChangeListener("value", this);
        generate.setLabelWidth(70);
        
        // Here we create a Group to hold the checkbox.
        ControlGroup params = new ControlGroup();
        params.setText("Parameters");
        params.add(generate);
        addElement(params);

        
        mViewer.setBackgroundColor(new Color(255,255,255));
        ((ViewerJ3D)mViewer).setFogColor(new Color3f(1.f,1.f,1.f));

        

        addActions();
        resetCamera();

        mSEC.init();
        mSEC.start();

    }

    public void clearFieldLines() {
        for (int i = 0; i < rfls.length; i++) {
            rfls[i].clearHistory();
        }
    }
    
    // This method resets the camera transform to it's original state.
    public void resetCamera() {
        mViewer.setLookAt(new Point3d(-1.0,1., 2.2), new Point3d(0., 0.0, 0.), new Vector3d(0., 1., 0.));
    }

    public void reset() {
//        rad_gizmo.setPosition(new Vector3d());
//        rad_constraint.setPoint(new Vector3d());
//        wave.reset();
//        radiator.clearHistory();

        theEngine.requestRefresh();
    }

    // This is the method that is called whenever a propertyChangeEvent is fired (assuming a Listener has been created
    // for that particular event).  This is where we define what happens when a particular event occurs.
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce == null) return;
        try {
            TDebug.println(1, "RadiationCharge.propertyChange: " + pce.getSource() + " -> " + pce.getPropertyName());
            Object source = pce.getSource();
            
            // If the source of the propertyChangeEvent is the VectorGenerator, set the position of our dummy gizmo 
            // (and hence the plate) to the position given by the generator.
            if (source == wave) {
                String pn = pce.getPropertyName();
                if (pn.compareTo("value") == 0) {
                    rad_gizmo.setPosition((Vector3d) pce.getNewValue());
                }
            }

            // If the event came from the GUI checkbox, set the VectorGenerator to the state given by the checkbox.
            else if (source == generate) {
                String pn = pce.getPropertyName();
                
                if (pn.compareTo("value") == 0) {
                    boolean state = ((Boolean) pce.getNewValue()).booleanValue();
                    
                    wave.setStepping(state);
                    isGenerating = state;
                    
                }
            } 
            // If the event came from the dummy object being moved, update the position of the constraint to reflect this
            // change.  Also, limit the vertical component of the dummy motion, so the user can't move it too far above or
            // below the x-axis.
            else if (pce.getSource() == rad_gizmo) {
                if (pce.getPropertyName().equalsIgnoreCase("position")) {
                    Vector3d position = (Vector3d) pce.getNewValue();
                    if (position.y > 20) position.y = 20;
                    if (position.y < -20) position.y = -20;
                    position.x = 0;
                    position.z = 0;
                    rad_gizmo.setPosition(position, false);
                    rad_constraint.setPoint(position);
                    theEngine.requestRefresh();
                    int state = mSEC.getSimState();

                    if (state != TEngineControl.RUNNING) {
                        mSEC.start();
                    }
                }
            } else {
                super.propertyChange(pce);
            }
        } catch (Exception e) {
            //TDebug.printThrown(0,e);
        }
    }
    // Add a help file item under the Help menu.
    void addActions() {
        TealAction ta = new TealAction("EM Radiator", this);
        addAction("Help", ta);
    }
    // Handle the help file ActionEvent.
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareToIgnoreCase("EM Radiator") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework) mFramework).openBrowser("help/emradiator.html");
        	}
        } else {
            super.actionPerformed(e);
        }
    }

   
}
