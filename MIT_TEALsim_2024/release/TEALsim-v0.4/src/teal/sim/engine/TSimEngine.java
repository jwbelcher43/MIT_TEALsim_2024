/*
 * $Id: TSimEngine.java,v 1.3 2007/12/04 21:00:34 pbailey Exp $ 
 */

package teal.sim.engine;

import java.util.Collection;

import javax.media.j3d.Bounds;
import javax.vecmath.Vector3d;

import teal.core.HasID;
import teal.render.viewer.TViewer;
import teal.sim.TSimElement;
import teal.sim.properties.PhysicalElement;

public interface TSimEngine extends TEngine {

	/**
     * adds an element to the TSimulation and insures that it is added
     * to all the lists and viewers it should be.
     */
    public void addSimElement(TSimElement obj);

    public void addSimElements(Collection objects);

    /**
     * Removes all objects ,viewers, spatial, etc from the world.
     */
    public void removeAll();

    /**
     * Removes an Element from the simulation insuring that it is
     * removed from all lists,viewers and cleaned up.
     */
    public void removeSimElement(TSimElement obj);

    /**
     * Removes a collection of SimElements from the simulation, insuring that they are
     * removed from all lists,viewers and cleaned up.
     * 
     * @param objects collection to remove.
     */
    public void removeSimElements(Collection objects);
    
    /** 
     * Allows the addition of <code>TViewer</code> objects. This method is
     * internally called by the <code>addSimElement</code> methods. For most
     * <code>SimElement</code>s, the add operation is basically an addition
     * to the proper list. Adding a viewer might entail additional configuration,
     * which justifies having the associated procedure in a standalone method.  
     */
    public void addViewer(TViewer viewer);
	public void removeViewer(TViewer viewer);
	public void removeViewers();
	
	 /**
     * Returns the bounding area of the world.
     * 
     * @return bounding area.
     */
    public Bounds getBoundingArea();
    /**
     * Sets the bounding area of the world.
     * 
     * @param bounds bounding area.
     */
    public void setBoundingArea(Bounds bounds);

    /**
     * Returns the time step of the world.
     * 
     * @return time step.
     */
    public double getDeltaTime();


    /**
     * Sets the time step of the simulation.
     * 
     * @param dTime time step.
     */
    public void setDeltaTime(double dTime);

    /**
     * Returns the current simulation time (milliseconds since simulation start?)
     * 
     * @return time, in milliseconds, since the start of the simulation.
     */
    public double getTime();
    /**
     * Sets the current time of the simulation (in milliseconds).  Note that this method does not advance (or rewind) the
     * simulation to the desired time, but rather just resets the clock.
     * 
     * @param time new time.
     */
    public void setTime(double time);

    public boolean getShowTime();
    
    /**
     * Sets whether the simulation time is printed to the console.
     * 
     * @param b
     */
    public void setShowTime(boolean b);


    /**
     * Returns the framerate of the simulation.
     * 
     * @return frame rate (frames per second).
     */
    public double getFrameRate();
    
    /**
     * Sets the desired frame rate of the simulation.  The effectiveness of this method depends on the computational
     * complexity of the simulation.
     * 
     * @param fps desired frame rate (frames per second).
     */
    public void setFrameRate(double fps);
    
    public void setCheckFrameRate(boolean b);

    public void checkFrameRate();
    
    /**
     * This should return the forces on the supplied PhysicalElement.  I don't think this is being used anymore?
     * 
     * @param ph
     * @return forces
     */
    public Vector3d getForces(PhysicalElement ph);

   

}
