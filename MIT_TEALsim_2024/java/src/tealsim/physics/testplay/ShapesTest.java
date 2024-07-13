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
import teal.render.Rendered;

import teal.render.j3d.ShapeNode;
import teal.render.j3d.AmpereanRectangleNode3D;
import teal.render.primitives.Line;

/** An application to explore various shapes and so on in the context of TealSim structure.  
 * @author John Belcher
 * @version 1.0 
 * */


public class ShapesTest extends SimEM  {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	AmpereanRectangleNode3D testnode3d;
    Rendered DipoleHolder = new Rendered();
    
    
    public ShapesTest() {
        super();
        TDebug.setGlobalLevel(0);
        
	    title = "Testing Shapes";
	   // Hide run controls       
	    mSEC.rebuildPanel(0);
	    
	   // change some features of the lighting, background color, etc., from the default values, if desired
	   	mViewer.setBackgroundColor(new Color(0,0,0));
	   	
        testnode3d = new AmpereanRectangleNode3D(1.,3.,.02);

       	Transform3D offsetTrans = new Transform3D();
    	offsetTrans.setRotation(new AxisAngle4d(0.,0.,1.,0.5*Math.PI));
		offsetTrans.setTranslation(new Vector3d(0., .8, 0.));
        testnode3d.setModelOffsetTransform(offsetTrans);
        DipoleHolder.setNode3D(testnode3d);
        addElement(DipoleHolder);

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
        

        
    }
	
    public void resetCamera() {
        mViewer.setLookAt(new Point3d(0.0, 0.0, 0.4), 
        	new Point3d(0., 0.0, 0.), new Vector3d(0., 1., 0.)); 
    }


}

   
    			
	
    	
    

   

