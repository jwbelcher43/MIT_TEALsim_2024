 /*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: Route.java,v 1.12 2007/08/13 22:05:59 pbailey Exp $
 * 
 */

 package teal.core;
 
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;

import teal.util.*;

 /**
 * Provides an optimized propertyChangeListener to the class it is added to.
 * Redirects a propertyChange call to the target's set<i>TargetProperty</i> method. 
 * The first call to the setProperty method caches the method as part of the Route object.
 **/
public class Route implements PropertyChangeListener, Serializable{

        /**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -8574349728185776203L;
		protected String srcProp;
        protected Object target;
        protected String targetProp;
        protected Method mMethod;
        protected boolean init = false;
        
        public Route()
        {
        }

        public Route(String attName, TElement targetObj, String tName) {
            srcProp = attName;
            target = targetObj;
            targetProp = tName;
        }
        
 
        
        public boolean equals(Object obj) {
            boolean status = false;
            if (obj instanceof Route)
            {
                Route rec = (Route) obj;
                if ((this.srcProp.compareTo(rec.srcProp) == 0) 
                    && (this.target == rec.target) && (this.targetProp.compareTo(rec.targetProp) == 0))
                {
                    status = true;
                }
            }
            return status;
        }

        public Object getTarget()
        {
            return target;
        }
        public void setTarget(Object obj)
        {
            target = obj;
        }   
        
        public String getTargetProperty()
        {
            return targetProp;
        }
        
        public void setTargetProperty(String str)
        {
            targetProp = str;
        }
        
        public String getSrcProperty()
        {
            return srcProp;
        }
        
        public void setSrcPropery(String str)
        {
            srcProp = str;
        }
        

        
        
        
    /** 
     * The actual dispatch of the propertyChange method. If the method 
     * has not been cached an attempt to resolve the method call will 
     * be performed.
     */
    public void propertyChange(PropertyChangeEvent pce) {
        TDebug.println(1," Route - property: " + pce.getPropertyName());
        if((srcProp.compareTo(pce.getPropertyName()) != 0)){
        		TDebug.println(0,"Route propertyChange: NOT INTERRESTED");
        		return;
        }
        if ((mMethod == null) && (init == false))
        {
            TDebug.println(1," in propertyChange trying find setMethod ");
                getSetMethod(targetProp);
        }
        if (mMethod == null)
        {
            TDebug.println(1,"Error: No method found for " + targetProp);
            return;
        }
        Object params[] = new Object[1];
        params[0] = pce.getNewValue();
        try
        {
            mMethod.invoke(target,params); 
        }
        catch (InvocationTargetException cnfe) {
            TDebug.println(1, " InvocTargetEx: " + cnfe.getMessage());
        } catch (IllegalAccessException ille) {
            TDebug.println(1, "IllegalAccess: " + ille.getMessage());
        }
    }


   protected void getSetMethod(String name) {
        TDebug.println(3, " In getSetMethod(): " + name );
    
        //Object param[] = { prop };
        //Class classType[] = { prop.getClass() };
        
        Method theMethod = null;
        try {
            PropertyDescriptor pd = new PropertyDescriptor(name, target.getClass());
            Class paramClass=  pd.getPropertyType();
            TDebug.println(1,"set: "+name + "   param type = " + paramClass.getName());
            theMethod = pd.getWriteMethod();
            
            if (theMethod != null) {
                mMethod = theMethod;
                //paramType = paramClass;
            } else {
                TDebug.println(1, "Setter method for " + name + " not found");
                mMethod = null;
                //paramType = null;
            }
            init = true;
        } catch (IntrospectionException ie) {
            TDebug.println(1," Warning: Setter IntrospectionEx: " + ie.getMessage() + "  "
                + this.getClass().getName());
        }
        /* catch (InvocationTargetException cnfe) {
            TDebug.println(0, getID() + " InvocTargetEx: " + cnfe.getMessage());
        } catch (IllegalAccessException ille) {
            TDebug.println(0, getID() + "IllegalAccess: " + ille.getMessage());
        }
        */
    }


 
}