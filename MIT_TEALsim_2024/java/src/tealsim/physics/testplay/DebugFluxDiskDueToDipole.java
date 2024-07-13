/* $Id: ExploringElectricField.java,v 1.1 2008/08/11 17:50:46 jbelcher Exp $ */
/**
 * @author John Belcher 
 * Revision: 1.0 $
 */

package tealsim.physics.testplay;
import java.awt.Color;

import javax.media.j3d.Transform3D;
import javax.vecmath.*;

import teal.sim.spatial.GeneralVector;
import teal.physics.em.SimEM;
import teal.util.TDebug;
import teal.math.SpecialFunctions;

/** An application to debug the calculation of the flux through a disk due to a magnetic dipole at the origin
 * @author John Belcher
 * @version 1.0 
 * */


public class DebugFluxDiskDueToDipole extends SimEM  {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	GeneralVector test = new GeneralVector();

    double arrowScale = 2.; 
    /** Source of electric field in scene.    */
    
    
    public DebugFluxDiskDueToDipole() {
        super();
        TDebug.setGlobalLevel(0);
        
   title = "Testing Flux Calculation and other randeom stuff";
   
   // change some features of the lighting, background color, etc., from the default values, if desired
   	mViewer.setBackgroundColor(new Color(0,0,120));
        
        test.setValue(new Vector3d(0,0,0));
        test.setDrawn(true);
        test.setScale(arrowScale);
        test.setColor(Color.red);
    	Transform3D offsetTrans = new Transform3D();

    //	offsetTrans.setRotation(new AxisAngle4d(0.,0.,1.,Math.PI));
		offsetTrans.setTranslation(new Vector3d(0., -.5, 0.));
		test.setModelOffsetTransform(offsetTrans);
        addElement(test);
        
     // set parameters for mouseScale 
             Vector3d mouseScale = mViewer.getVpTranslateScale();
             mouseScale.x *= 0.05;
             mouseScale.y *= 0.05;
             mouseScale.z *= 0.5;
             mViewer.setVpTranslateScale(mouseScale);
             
     
        mSEC.init(); 
        theEngine.requestRefresh();
        mSEC.setVisible(false);
        resetCamera();  
    
        mSEC.init();
        reset();
        
//here is where I do the stuff I am really interesting in, all the stuff above is just there so I don't break this application
        
        Vector3d posDisk = new Vector3d(0.,2.,0.);
        Vector3d NormalDisk = new Vector3d(0.,1.,0.);
        double radDisk = .3;
        for (int i = 0; i<15; i++) {
        double yvalue = 0.1+i*.1;
        posDisk.y = yvalue;
        posDisk.x = 0.0;
        // public static double FluxThroughRingDueToDipole(Vector3d posDip, Vector3d dirDip, Vector3d posDisk, Vector3d dirDisk, double radDisk, double dipMoment)
		double magneticflux = SpecialFunctions.FluxThroughRingDueToDipole(new Vector3d(0.,0.,0.), new Vector3d(0.,1.,0.), posDisk, NormalDisk, radDisk, 100.) ;  
   //     System.out.println("postion disk  " + posDisk + " magneticflux " + magneticflux );
        }
    }
	
    public void resetCamera() {
        mViewer.setLookAt(new Point3d(0.0, 0.0, 0.4), 
        	new Point3d(0., 0.0, 0.), new Vector3d(0., 1., 0.)); 
    }


}

   
    			
	
    	
    

   

