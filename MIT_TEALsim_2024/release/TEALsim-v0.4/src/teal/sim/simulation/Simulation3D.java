/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: Simulation3D.java,v 1.9 2008/06/03 15:31:07 pbailey Exp $ 
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
import teal.framework.TFramework;
import teal.framework.TGui;
import teal.framework.TealAction;
import teal.render.TAbstractRendered;
import teal.render.j3d.ViewerJ3D;
import teal.render.viewer.SelectListener;
import teal.render.viewer.SelectManager;
import teal.render.viewer.TViewer;
import teal.render.viewer.TViewer3D;
import teal.sim.TSimElement;
import teal.sim.engine.TEngineControl;
import teal.sim.engine.EngineControl;
import teal.sim.engine.TSimEngine;
import teal.ui.control.ControlGroup;
import teal.util.TDebug;

/**
 * The abstract class Simulation3D implements TSimulation and provides management for a simulation 
 * which includes a 3D viewer and simulation engine specified in a derived class.
 * All elements related to a specific simulation are managed by the Sim 
 * these could include Actions, simulated objects and controls. 
 * As envisioned a simulation will support 
 * an XML loader/dumper interface and should be able to be basic unit 
 * assigned to a TFramework which supports Simulations.
 * 
 * It may be extended into a simulation with a default constructor and specified Engine.
 * @see teal.sim.simulation.SimWorld
 * @see teal.physics.em.SimEM

 *
 * @version $Revision: 1.9 $
 * @author Phil Bailey
 */
public abstract class Simulation3D extends AbstractElement implements TSimulation {

    private static final long serialVersionUID = 3258126942908789043L;
    
    // Framework support
    protected String title;
    protected Hashtable mElements;
    protected ArrayList guiElements;
    protected ArrayList menuElements;
    protected ArrayList actions;
    protected TFramework mFramework;
    protected MenuElement[] meTemplate;

    // Sim  view, model, control support     
    protected SelectManager mSelect;
    protected TViewer3D mViewer;
    //protected TSimEngine theEngine;
    protected EngineControl mSEC;
    protected TGui theGUI;

    public Simulation3D() {
        super();
        mElements = new Hashtable();
        guiElements = new ArrayList();
        meTemplate = new MenuElement[0];
        menuElements = new ArrayList();
        actions = new ArrayList();

	    mFramework = null;
        ViewerJ3D viewer = new ViewerJ3D();
        viewer.setID("Viewer 3D");
        viewer.setRenderOrder(new SimDrawOrder());
        viewer.setVisible(true);
        setViewer(viewer);
        //		mViewer.setPreferredSize(new Dimension(450, 450));
        //		mViewer.setMinimumSize(new Dimension(50, 50));
        
        EngineControl sec = new EngineControl(EngineControl.DO_ALL);
        //sec = new EngineControl(EngineControl.DO_DEFAULT);
        sec.setID("SMC");       
        sec.setBounds(45, 473, 400, 35);

        sec.setVisible(true);
        setEngineControl(sec);
        
        theGUI = new SimGUI();
        
        TealAction ta = new TealAction(TViewer.RESET_CAMERA, this);
        addAction("View", ta);
       
    }
    
    public abstract void addSimElement(TSimElement elm);
    public abstract void removeSimElement(TSimElement elm);
    public abstract void setEngine(TSimEngine model);
    public abstract TSimEngine getEngine();
 
    public void initialize(){
	}

    public void setEngineControl(TEngineControl modelCtr) {
        mSEC = (EngineControl) modelCtr;
        if(getEngine() != null)
        	mSEC.setEngine(getEngine());
        if(mSEC instanceof EngineControl) {
        	((EngineControl)mSEC).addResetActionListener(this);
        }
        addElement(mSEC, false);
    }

    public TEngineControl getEngineControl() {
        return mSEC;
    }
    
    public SelectManager getSelectManager(){
    	return mSelect;	
    }
    
    public void setSelectManager(SelectManager sManager){
    	mSelect = sManager;
    	if(mViewer != null)
    		mViewer.setSelectManager(mSelect);
    }
    

    public void setViewer(TViewer3D viewer) {
        mViewer = viewer;
        if(getEngine() != null)
        	getEngine().addViewer(mViewer);
        
        if(mSelect != null)
        	mViewer.setSelectManager(mSelect);
        addElement(mViewer, false);
        loadViewer();
    }

    public TViewer3D getViewer() {
        return mViewer;
    }

    public TFramework getFramework() {
        return mFramework;
    }

    public void setFramework(TFramework fw) {
        mFramework = fw;
        Iterator it = mElements.values().iterator();
        while(it.hasNext()){
            Object obj = it.next();
            if(obj instanceof ControlGroup){
            	Iterator it2 = ((ControlGroup)obj).getElements().iterator();
            	while(it2.hasNext()){
            		Object obj2 = it2.next();
            		if(obj2 instanceof HasElementManager){
                        if(obj2 instanceof HasFramework)
                        {
                            ((HasFramework)obj2).setFramework(fw);
                        }
                        else{
                        	if(fw instanceof TFramework) {
                        		((HasElementManager)obj2).setElementManager((TFramework)fw);
                        	}
                        }
            		}
            	}
            }
            else if(obj instanceof HasElementManager){
                if(obj instanceof HasFramework)
                {
                    ((HasFramework)obj).setFramework(fw);
                }
                else{
                	if(fw instanceof TFramework) {
                		((HasElementManager)obj).setElementManager((TFramework)fw);
                	}
                }
            }
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String t) {
        title = t;
        if (mFramework != null) mFramework.setTitle(t);
    }

    public void addElement(Object element) throws IllegalArgumentException {

        addElement(element, true);
    }

    public void addElement(Object element, boolean addToList) throws IllegalArgumentException {
        TDebug.println(1, "Simulation3D addElement: " + element);
        if (element instanceof HasID) {
            addTElement((HasID) element, addToList);
        } else if (element instanceof Action) {
            addAction((Action) element);
        } else if (element instanceof Component) {
            addComponent((Component) element);
        } else {
            throw new IllegalArgumentException("Error: element type of object " + element + " is not supported");
        }
    }

    public void removeElements() {
        Collection elements = new ArrayList(mElements.values());
        removeElements(elements);
    }

    public void removeElements(Collection elements) {
        Iterator it = elements.iterator();
        while (it.hasNext()) {
            removeElement(it.next());
        }
    }

    public void removeElement(Object element) {
        if (element instanceof HasID) {
            removeTElement((HasID) element);
        } else if (element instanceof Action) {
            removeAction((Action) element);
        } else if (element instanceof Component) {
            removeComponent((Component) element);
        }
    }

    public void addElements(Collection elements) throws IllegalArgumentException {
        Iterator it = elements.iterator();
        while (it.hasNext()) {
            addElement(it.next(), true);
        }
    }

    public void addAction(Action elm) {
        TDebug.println(1, "Simulation3D addAction: " + elm);
        if (elm instanceof Component) {
            addComponent((Component) elm);
        }
        if (elm instanceof Action) {
            addAction((Action) elm);
        }
    }

    public void removeAction(Action a) {
    }

    public void addAction(String target, Action ac) {
        TDebug.println(1, "Simulation3D addMenuAction: " + ac);
        menuElements.add(new MenuElement(target, ac));
        if (mFramework != null) {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).addAction(target, ac);
        	}
        }
    }

    public void removeAction(String target, Action ac) {
        menuElements.remove(new MenuElement(target, ac));
        if (mFramework != null) {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).removeAction(target, ac);
        	}
        }
    }

    public MenuElement[] getMenuElements() {
        return (MenuElement[]) menuElements.toArray(meTemplate);
    }

    public void addTElement(HasID elm) {
        addTElement(elm, true);
    }

    public void addTElement(HasID elm, boolean addToList) {
        AbstractElement.checkID(elm);
        TDebug.println(1, "Simulation3D addTElement: " + elm);
        if (addToList) mElements.put(elm.getID(), elm);
        if (elm instanceof TSimElement) {
            if (getEngine() != null) {
                TDebug.println(2, elm + " added to SimEngine!");
                addSimElement((TSimElement) elm);
                
            } else {
                TDebug.println(2, elm + " was not added to SimEngine!");
            }
        }
        if(elm instanceof ControlGroup){
            Collection innerElements = ((ControlGroup)elm).getElements();
            Iterator it = innerElements.iterator();
            while(it.hasNext()){
                Object obj = it.next();
                TDebug.println(2,"ControlGroup element = " + obj);
                // Recursive call this should not be toally recursive as the obj is contained
                
                if(mFramework != null){
                    if(obj instanceof HasElementManager){
                        if(obj instanceof HasFramework){
                            ((HasFramework)obj).setFramework(mFramework);
                        }else{
                        	if(mFramework instanceof TFramework) {
                        		((HasElementManager)obj).setElementManager((TFramework)mFramework);
                        	}
                        }
                    }
                }
                if (obj instanceof TSimElement) {
                    if (getEngine() != null) {
                        TDebug.println(2, obj + ": TSimObject  added to SimEngine!");
                        addSimElement((TSimElement) obj);
                    } else {
                        TDebug.println(2, obj + " was not added to SimEngine!");
                    }
                }
                if (obj instanceof TAbstractRendered) {
                    mViewer.addDrawable((TAbstractRendered) obj);
                }
           }
        }
        if (elm instanceof TAbstractRendered) {
            mViewer.addDrawable((TAbstractRendered) elm);
        }

        if (elm instanceof Action) {
            addAction((Action) elm);
        }
        if (elm instanceof Component) {
            addComponent((Component) elm);
        }
        if(mFramework != null){
            if(elm instanceof HasElementManager){
         
                if(elm instanceof HasFramework){
                    ((HasFramework)elm).setFramework(mFramework);
                }else{
                	if(mFramework instanceof TFramework) {
                		((HasElementManager)elm).setElementManager((TFramework)mFramework);
                	}
                }
            }
        }

    }

    public void removeTElement(HasID elm) {
        if (elm instanceof TSimElement) {
            removeSimElement((TSimElement) elm);
        }
        if (elm instanceof TAbstractRendered) {
            if (((TAbstractRendered) elm).isSelected()) mSelect.removeSelected((TAbstractRendered) elm);
            mViewer.removeDrawable((TAbstractRendered) elm);
        }
        if(elm instanceof ControlGroup){
            Collection innerElements = ((ControlGroup)elm).getElements();
            Iterator it = innerElements.iterator();
            while(it.hasNext()){
                Object obj = it.next();
                TDebug.println(0,"ControlGroup element = " + obj);
                // Recursive call this should not be toally recursive as the obj is contained
                
                if(mFramework != null){
                    if(obj instanceof HasElementManager){
                        if(obj instanceof HasFramework){
                            ((HasFramework)obj).setFramework(null);
                        }else{
                            ((HasElementManager)obj).setElementManager(null);
                        }
                    }
                }
                if (obj instanceof TSimElement) {
                    if (getEngine() != null) {
                        TDebug.println(2, obj + ": TSimObject  removing SimEngine!");
                        removeSimElement((TSimElement) obj);
                    } else {
                        TDebug.println(2, obj + " was not added to SimEngine!");
                    }
                }
                if (obj instanceof TAbstractRendered) {
                    mViewer.removeDrawable((TAbstractRendered) obj);
                }
           }
        }
        if (elm instanceof Action) {
            removeAction((Action) elm);
        }
        if (elm instanceof Component) {
            removeComponent((Component) elm);
        }
        mElements.remove(elm);

    }

    public void addComponent(Component elm) {
        TDebug.println(1, "Simulation3D addComponent: " + elm);
        guiElements.add(elm);
    }

    public void removeComponent(Component elm) {
        guiElements.remove(elm);
        if(mFramework != null){
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).removeComponent(elm);
        	}
        }
    }

    public void addTElements(Collection elms) {
        Iterator it = elms.iterator();
        while (it.hasNext()) {
            addTElement((TElement) it.next());
        }
    }
    
    // Test
    public void addTElementsBulk(Hashtable elms) {
    	Hashtable elements = new Hashtable(mElements.size() + elms.size());
    	elements.putAll(mElements);
    	elements.putAll(elms);
    	mElements = elements;
    }
    
    public void removeTelementsBulk(Hashtable elms) {
    	Iterator it = elms.values().iterator();
    	while (it.hasNext()) {
    		//System.out.println("SimWorld::removeTelementsBulk() element = " + it.next().getClass().getName());
    		String key = ((TElement)it.next()).getID();
    		if (mElements.containsKey(key)) mElements.remove(key);
    	}
    }

    /**
     * This retuns all elements in the simulation including Viewers, actions, GUIelements, models and controls.
     * It will be used to construct the XML that specifies the simulation.
     * @see teal.sim.simulation.SimulationFactory
     **/
    public Collection getElements() {
        return mElements.values();
    }

    public Collection getGuiElements() {
        return guiElements;
    }

    public Collection getActions() {
        return actions;
    }

    public void addSelectListener(SelectListener listener) {
        mSelect.addSelectListener(listener);
    }

    public void removeSelectListener(SelectListener listener) {
        mSelect.removeSelectListener(listener);
    }

    public void addSelected(TAbstractRendered obj, boolean clear) {
        mSelect.addSelected(obj, clear);
    }

    public void removeSelected(TAbstractRendered obj) {
        mSelect.removeSelected(obj);
    }

    public void clearSelected() {

        mSelect.clearSelected();

    }

    public int getNumberSelected() {
        return mSelect.getNumberSelected();
    }

    public Collection getSelected() {
        return mSelect.getSelected();
    }

    public void reset() {
        mSEC.stop();
    }

    public void resetCamera() {
        TDebug.println(2, "Sim: resetCamera");
        mViewer.displayBounds();
    }

    public void dispose() {
    	mFramework = null;
    }

    public void actionPerformed(ActionEvent evt) {
        String command = evt.getActionCommand();
        if (command.compareToIgnoreCase("reset") == 0) {
            TDebug.println(1, "Reset called");
            reset();
        } else if (command.compareToIgnoreCase(TViewer.RESET_CAMERA) == 0) {
            TDebug.println(1, "Reset Camera called");
            resetCamera();
        } else if (command.compareToIgnoreCase("VIEW STATUS") == 0) {
            TDebug.println(1, "view Status");
            mViewer.doStatus();
            // Load & save stubs
        } else {
            if (mFramework != null) {
            	if(mFramework instanceof TFramework) {
            		((TFramework)mFramework).actionPerformed(evt);
            	}
            }
        }
    }
    
    public TEngineControl getSimModelControl() {
    	return mSEC;
    }
    
    public TGui getGui() {
    	
    	return theGUI;
    }
    
    public void setGui(TGui g) {
    	theGUI = g;
    }

	/* (non-Javadoc)
	 * @see teal.core.TElementManager#getTElementByID(java.lang.String)
	 */
	public HasID getTElementByID(String id) {
		if (mElements.containsKey(id)) {
			return (HasID)mElements.get(id);
		}
		return null;
	}
	
	// setProperty method for command line arguments
	// May want to throw some more specific exceptions here
	public void setProperty(String telement, String property, String value) {
		try {
			// get the Class representation of a String
			Class classArray[] = { Class.forName("java.lang.String") };
			
			// "sim" is a reserved keyword to represent the simulation itself
			if (telement.compareToIgnoreCase("sim") == 0) {
				// get the Class representation of the property we are trying to change
				Class propClass = this.getProperty(property).getClass();
				// get the Constructor for this Class that takes a String as an argument
	    		Constructor c = propClass.getConstructor(classArray);
	    		String s[] = {value};
	    		// set the property using a new instance of the obtained Constructor with the String argument "value"
	    		this.setProperty(property, c.newInstance(s));
			} else {
				// in this case we are trying to get at an element in the simulation.  Otherwise the process is the same.
				TElement t = (TElement)this.getTElementByID(telement);
				if (t != null) {
					Class propClass = t.getProperty(property).getClass();
		    		Constructor c = propClass.getConstructor(classArray);
		    		String s[] = {value};
		    		t.setProperty(property, c.newInstance(s));
				}
			}
		} catch (Exception e) {
			TDebug.println(e.getMessage());
            e.printStackTrace();
		}
	}
	
	protected void loadEngine() {
        if(getEngine() == null)
        	return;
        Iterator it = mElements.values().iterator();
        while(it.hasNext()){
            Object obj = it.next();
            if(obj instanceof ControlGroup){
            	Iterator it2 = ((ControlGroup)obj).getElements().iterator();
            	while(it2.hasNext()){
            		Object obj2 = it2.next();
            		if(obj2 instanceof TSimElement){
                        addSimElement((TSimElement)obj2);
            		}
            	}
            }
            else if(obj instanceof TSimElement){
            	   addSimElement((TSimElement)obj);
            }
        }
    }
	
	protected void loadViewer() {
        if(mViewer == null)
        	return;
        Iterator it = mElements.values().iterator();
        while(it.hasNext()){
            Object obj = it.next();
            if(obj instanceof ControlGroup){
            	Iterator it2 = ((ControlGroup)obj).getElements().iterator();
            	while(it2.hasNext()){
            		Object obj2 = it2.next();
            		if(obj2 instanceof TAbstractRendered){
                        mViewer.addDrawable((TAbstractRendered)obj2);
            		}
            	}
            }
            else if(obj instanceof TAbstractRendered){
            	mViewer.addDrawable((TAbstractRendered)obj);
            }
        }
    }


}
