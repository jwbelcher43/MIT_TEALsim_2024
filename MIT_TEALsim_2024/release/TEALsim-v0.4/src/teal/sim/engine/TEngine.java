/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TEngine.java,v 1.12 2007/12/04 21:00:34 pbailey Exp $ 
 * 
 */

package teal.sim.engine;

import java.util.Collection;

import javax.media.j3d.Bounds;
import javax.vecmath.Vector3d;

import teal.core.HasID;
import teal.core.TElement;
import teal.render.viewer.RenderListener;
import teal.render.viewer.TViewer;
import teal.sim.TSimElement;
import teal.sim.properties.PhysicalElement;

/** The TEngine provides the minimum interface for a simulation including both the simulated objects and forces
 * that make up the world and provide an interface to the renders that model the results. 
 * Details about a specific evironment 
 * are provided by the actual world implementation or additional interfaces. This is isolates HasEngine 
 * from needing too many details.
 */

public interface TEngine extends TEngineControl, TElement, RenderListener, Runnable {

	public void setEngineControl(TEngineControl control);
	public TEngineControl getEngineControl();
    

    /**
     * Checks to find if the world needs to be refreshed, and refreshes if needed.
     */
    public void doRefresh();
    public void enableRender(boolean state);
    
    public void requestRefresh();
    public void requestSpatial();
   
    public void renderComplete(TViewer viewer);

  

    /** Releases the world's resources, places the world's simState to NOT,
     * does not delete or destroy the actual world object */
    public void dispose();
    
 

}
