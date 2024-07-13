/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: SimWorld.java,v 1.14 2007/12/04 21:01:11 pbailey Exp $ 
 * 
 */

package teal.sim.simulation;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.Action;

import teal.app.SimGUI;
import teal.core.AbstractElement;
import teal.core.HasElementManager;
import teal.core.HasID;
import teal.core.TElement;
import teal.framework.HasFramework;
import teal.framework.MenuElement;
import teal.framework.TAbstractFramework;
import teal.framework.TFramework;
import teal.framework.TGui;
import teal.framework.TealAction;
import teal.physics.em.EMEngine;
import teal.render.TAbstractRendered;
import teal.render.j3d.ViewerJ3D;
import teal.render.viewer.SelectListener;
import teal.render.viewer.SelectManager;
import teal.render.viewer.SelectManagerImpl;
import teal.render.viewer.TViewer;
import teal.render.viewer.TViewer3D;
import teal.sim.TSimElement;
import teal.sim.engine.TEngineControl;
import teal.sim.engine.SimEngine;
import teal.sim.engine.EngineControl;
import teal.sim.engine.TEngine;
import teal.sim.engine.TSimEngine;
import teal.ui.control.ControlGroup;
import teal.util.TDebug;

/**
 * The SimWorld provides management for a simulation with a default 3D viewer and
 * and SimEngine. THis class is the basis for most of the tealsim Simulations.
 *
 * @version $Revision: 1.14 $
 * @author Phil Bailey
 */
public class SimWorld extends Simulation3D {

    private static final long serialVersionUID = 3258126942908789043L;
    
    protected SimEngine theEngine;
    
    public SimWorld() {
        super();
       
        ViewerJ3D viewer = new ViewerJ3D();
        viewer.setID("Viewer 3D");
        viewer.setRenderOrder(new SimDrawOrder());
        viewer.setVisible(true);
        setViewer(viewer);
        //		mViewer.setPreferredSize(new Dimension(450, 450));
        //		mViewer.setMinimumSize(new Dimension(50, 50));
        

        SelectManager select = new SelectManagerImpl();
        setSelectManager(select);
        
        

        EngineControl sec = new EngineControl(EngineControl.DO_ALL);
        //sec = new SimEngineControl(SimEngineControl.DO_DEFAULT);
        sec.setID("SMC");       
        sec.setBounds(45, 473, 400, 35);
        //sec.setSize(new Dimension(400, 35));
        sec.setVisible(true);
        setEngineControl(sec);
        
        theGUI = new SimGUI();
        
        setEngine(new SimEngine());

        TealAction ta = new TealAction(TViewer.RESET_CAMERA, this);
        addAction("View", ta);
    }
 
    public void setEngine(TSimEngine model) {
    	if(theEngine != null){
    		theEngine.dispose();
    		theEngine = null;
    	}
    	if(model instanceof EMEngine){
    		theEngine = (EMEngine) model;
    	}
    	else{
    		throw new IllegalArgumentException("Wrong engine type in SimEM");
    	}
        if(mViewer != null)
        	theEngine.addViewer(mViewer);
        if(mSEC != null)
        	mSEC.setEngine((TEngine)theEngine);
        loadEngine();
        
    }

    public TSimEngine getEngine() {
        return theEngine;
    }
    
    public void addSimElement(TSimElement elm){
    	theEngine.addSimElement(elm);
    }
    public void removeSimElement(TSimElement elm){
    	theEngine.removeSimElement(elm);
    }


}
