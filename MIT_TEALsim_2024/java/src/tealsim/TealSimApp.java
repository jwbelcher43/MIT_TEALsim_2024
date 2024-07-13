/*
 * TEALsim - TEAL Project, CECI/MIT
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TealSimApp.java,v 1.4 2008/06/03 15:29:43 pbailey Exp $
 * 
 */

package tealsim;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.UIManager;

import teal.app.SimPlayer;
import teal.app.SimPlayerApp;
import teal.framework.TealAction;
import teal.sim.simulation.TSimulation;
import teal.sim.simulation.Simulation3D;
import teal.util.TDebug;

/**
 * Parses arguments to load a specific simulation on startup
 * or a defined collection of Simulations which may be launched from the menubar.
 * Currently only supports simulations which have a default constructor.
 * arguments are:
 * <br/><indent> -n fully_qualified_class_name</indent>
 * <br/><indent> -a allows the user to select a number of Electro-Magnetic and mechanical simulations
 * <br/>Note: the simulation classes must be in the current classpath.
 * 
 * 
 * @see SimPlayer
 * 
 *
 * @author Philip Bailey
 *
 */

public class TealSimApp extends SimPlayerApp implements ActionListener {

    private static final long serialVersionUID = 3258689927121220656L;
    private boolean checkFrameRate = false;
    
    public TealSimApp() {
        super();
    }
     
    protected void addActions(){
		TealAction ta;
		ta = new TealAction("Box Induction","tealsim.physics.em.boxInduction", this);
		thePlayer.addAction("Actions", ta);
      ta = new TealAction("Capacitor","tealsim.physics.em.Capacitor", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Charge By Induction","tealsim.physics.em.ChargeByInduction", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Charged Metal Slab","tealsim.physics.em.ChargedMetalSlab", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Charge in Magnetic Field","tealsim.physics.em.ChargeInMagneticFieldGame", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Coulomb's Law","tealsim.physics.em.CoulombsLaw", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Electrostatic Force","tealsim.physics.em.ElectrostaticForce", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("EM Radiator","tealsim.physics.em.EMRadiatorApp", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("EM Videogame","tealsim.physics.em.EMVideogame", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("EM Zoo","tealsim.physics.em.EMZoo", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("ExB Drift","tealsim.physics.em.ExBDrift", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Falling Coil","tealsim.physics.em.FallingCoil", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Falling Magnet","tealsim.physics.em.FallingMagnet", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Faraday's Law","tealsim.physics.em.FaradaysLaw", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Faraday's Law Rotation","tealsim.physics.em.FaradaysLawCylindrcalMagnet", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Faraday's Law Rotation","tealsim.physics.em.FaradaysLawRotation", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Faraday's Law Rotation","tealsim.physics.em.FaradaysLawTwoCoils", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Floating Coil","tealsim.physics.em.FloatingCoil", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Mapping Fields","tealsim.physics.em.MappingFields", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Pentagon","tealsim.physics.em.Pentagon", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Point Charge","tealsim.physics.em.PCharges", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Radiating Charge","tealsim.physics.em.RadiationCharge", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Seafloor","tealsim.physics.em.SeafloorApp", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("TeachSpin","tealsim.physics.em.TeachSpin", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Torque on an Electric Dipole","tealsim.physics.em.TorqueOnDipoleE", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Torque on a Magnetic Dipole","tealsim.physics.em.TorqueOnDipoleB", this);
      thePlayer.addAction("Actions", ta);
      //ta = new TealAction("Two Rings","tealsim.physics.em.TwoRings", this);
      //addAction("Actions", ta);
      ta = new TealAction("Van deGraff","tealsim.physics.em.VandeGraff", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Wire and Magnet","tealsim.physics.em.WireAndMagnet", this);
	  thePlayer.addAction("Actions", ta);
    
      
      /*
      ta = new TealAction("Inclined Plane","tealsim.physics.mech.InclinedPlaneApp", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Acceleration on an Inclined Plane","tealsim.physics.mech.GalileosInclinedPlane", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Simple Pendulum","tealsim.physics.mech.SimplePendulumApp", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Physical Pendulum","tealsim.physics.mech.WeightedPhysicalPendulumApp", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Impulse Pendulum","tealsim.physics.mech.SimpleImpulsePendulumApp", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Newton's Cradle","tealsim.physics.mech.NewtonsCradle", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Rigid Bar","tealsim.physics.mech.RigidBarApp", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Circular Motion","tealsim.physics.mech.", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Projectile","tealsim.physics.mech.ProjectileApp", this);
      thePlayer.addAction("Actions", ta);
      ta = new TealAction("Celestial Mechanics","tealsim.physics.mech.CelestialApp", this);
      thePlayer.addAction("Actions", ta);
	*/
    }
    
    public void actionPerformed(ActionEvent e)
    throws IllegalArgumentException{
        thePlayer.loadSimClass(e.getActionCommand());
        //TDebug.println("Target FrameRate: " + ((Simulation3D)thePlayer.getTSimulation()).getEngine().getFrameRate());
        if(checkFrameRate){
        	((Simulation3D)thePlayer.getTSimulation()).getEngine().setCheckFrameRate(checkFrameRate);
        }
    }


    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            TealSimApp theApp = new TealSimApp();
            int loadFlagIndex = -1;
            int actionsFlagIndex = -1;
            if (args.length > 0) {
                for (int i = 0; i < args.length; ++i) {
                    if (args[i].compareTo("-n") == 0) {
                    	loadFlagIndex = i;
                    } else if (args[i].compareTo("-a") == 0) {
                    	actionsFlagIndex = i;
                    }
                    else if (args[i].compareTo("-c") == 0) {
                    	theApp.checkFrameRate = true;
                    }
                }
 
                if (actionsFlagIndex > -1) {
                	theApp.addActions();
                }
                if (loadFlagIndex > -1) {
                	int loadLen = (loadFlagIndex > actionsFlagIndex) ? (args.length - loadFlagIndex) : (actionsFlagIndex - loadFlagIndex);
                    String[] loadArgs = new String[loadLen]; 
                    System.arraycopy(args,loadFlagIndex,loadArgs,0,Math.max(actionsFlagIndex,loadLen));
                    
                    if (loadArgs.length >= 2) { // its a class name
                        String arg2 = loadArgs[1];
                        Class simClass = Class.forName(arg2);
                        TSimulation temp = (TSimulation) simClass.newInstance();
                        
                        
                        // Triplet-based implementation of command line parsing.  Instead of property/value pairs acting
                        // only on the simulation, we now use element/property/value triplets, where element is a 
                        // TElement of the simulation that we want to change a property on.  Using "sim" for the element
                        // argument looks for the property on the simulation itself, reducing to the previous implementation.
                        //if (args.length >= 5 && ((args.length - 2) % 3 == 0)) {
                        if ((loadArgs.length - 2 > 0)) {
                        	if ((loadArgs.length - 2) % 3 == 0) {
                       
	                        	for (int i = 2; i < loadArgs.length; i += 3) {
	                        		String elementName = loadArgs[i];
	                        		String propName = loadArgs[i+1];
	                        		String propValue = loadArgs[i+2];
	                        		
	                        		temp.setProperty(elementName,propName,propValue);
	                        	}
                        	} else {
                            	throw (new Exception("Invalid number of command line arguments."));
                            }
                        }
                        theApp.thePlayer.load(temp); 
                    }
                }  
            }
            theApp.setLocationRelativeTo(null);
            theApp.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
