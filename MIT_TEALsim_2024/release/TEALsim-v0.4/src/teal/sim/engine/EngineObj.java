/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: EngineObj.java,v 1.5 2007/12/04 21:00:35 pbailey Exp $ 
 * 
 */

package teal.sim.engine;

import teal.sim.*;

/**
 * The base for any object which may be included in a TEAL TSimulation
 *
 * @author Phil Bailey
 * @version $Revision: 1.5 $ 
 */


public class EngineObj extends SimObj implements  HasSimEngine
{

    private static final long serialVersionUID = 3544673979642949688L;
    
    protected transient TSimEngine theEngine = null;

    public void setSimEngine(TSimEngine engine)
    {
	theEngine = (SimEngine) engine;
    }

    public TSimEngine getSimEngine()
    {
		return theEngine;
    }
     
    protected boolean checkModel()
    {
		boolean status = false;
		if (theEngine != null)
		{
	    	status = true;
		}
		return status;
    }
 
 
}
