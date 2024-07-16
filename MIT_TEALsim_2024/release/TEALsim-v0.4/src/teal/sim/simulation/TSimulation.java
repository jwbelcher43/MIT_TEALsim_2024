/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TSimulation.java,v 1.20 2007/12/04 21:01:11 pbailey Exp $ 
 * 
 */

package teal.sim.simulation;

import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Action;

import teal.core.HasID;
import teal.core.TElement;
import teal.core.TElementManager;
import teal.framework.HasFramework;
import teal.framework.MenuElement;
import teal.sim.engine.HasEngineControl;

import teal.ui.HasGUI;

/**
 * The TSimulation provides a interface for the specification and management 
 * of all elements related to a specific simulation. 
 * This could include Actions, GUI and simulated objects, the model may 
 * reside within the simulation. As envisioned a simulation will support 
 * an XML loader/dumper interface and should be able to be basic unit 
 * assigned to a TFramework which supports Simulations.
 *
 * @version $Revision: 1.20 $
 * @author Phil Bailey
 */

public interface TSimulation extends TElement, TElementManager, HasFramework, HasEngineControl,  HasID, ActionListener, HasGUI {
    public void initialize();
    public void setProperty(String element, String property, String value);
    public Collection getGuiElements();
    public Collection getActions();
    public MenuElement[] getMenuElements();
    public void dispose();
    public void reset();

    public void setTitle(String title);
    public String getTitle();

    public void addAction(Action ac);
    public void addAction(String section, Action ac);

}
