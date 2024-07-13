/**
 * $ID:$
 */
package teal.physics.physical;

import teal.app.SimGUI;
import teal.framework.TealAction;
import teal.render.j3d.ViewerJ3D;
import teal.render.viewer.SelectManager;
import teal.render.viewer.SelectManagerImpl;
import teal.render.viewer.TViewer;
import teal.sim.TSimElement;
import teal.sim.engine.EngineControl;
import teal.sim.engine.SimEngine;
import teal.sim.engine.TEngine;
import teal.sim.engine.TSimEngine;
import teal.sim.simulation.SimDrawOrder;
import teal.sim.simulation.Simulation3D;

// temp until a real Kinematic Engine
import teal.physics.em.EMEngine;


/**
 * @author pbailey
 *
 */
public class SimKinematic extends Simulation3D {
	
	protected EMEngine theEngine;
	
	public SimKinematic()
	{
	      super();
	       
	      	

	        
	        SelectManager select = new SelectManagerImpl();
	        setSelectManager(select);
	        setEngine(new EMEngine());
		
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
