/**
 * $ID:$
 */
package teal.physics.em;

import teal.render.viewer.SelectManager;
import teal.render.viewer.SelectManagerImpl;
import teal.sim.TSimElement;
import teal.sim.engine.TEngine;
import teal.sim.engine.TSimEngine;
import teal.sim.simulation.Simulation3D;


/**
 * @author pbailey
 *
 */
public class SimEM extends Simulation3D {
	
	protected EMEngine theEngine;
	
	public SimEM()
	{
	      super();
	       
	        SelectManager select = new SelectManagerImpl();
	        setSelectManager(select);
	        // SimEM uses the EMEngine, which is set here
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
