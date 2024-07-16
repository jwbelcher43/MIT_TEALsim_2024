/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: MagneticDipoleBeanInfo.java,v 1.7 2007/07/17 15:46:55 pbailey Exp $ 
 * 
 */

package teal.physics.em;

import java.beans.*;
import java.util.*;

import teal.util.TDebug;

public class MagneticDipoleBeanInfo extends DipoleBeanInfo
{

	protected static ArrayList sProperties =null;
	protected static Class baseClass = MagneticDipole.class;
	
	static
	{
       try
       {
            PropertyDescriptor pd = null;
			sProperties = new ArrayList(DipoleBeanInfo.getPropertyList());
/*
			pd = new PropertyDescriptor("boundingArea",baseClass);
			pd.setBound(true);
			sProperties.add(pd);
			
*/
			//pd = new PropertyDescriptor("dipoleMoment",baseClass);
			//pd.setBound(true);
			//sProperties.add(pd);
			pd = new PropertyDescriptor("mu",baseClass);
			pd.setBound(true);
			sProperties.add(pd);
			pd = new PropertyDescriptor("avoidSingularity",baseClass);
			sProperties.add(pd);
			pd = new PropertyDescriptor("avoidSingularityScale",baseClass);
			sProperties.add(pd);
			pd = new PropertyDescriptor("feelsBField",baseClass);
			sProperties.add(pd);
		
			TDebug.println(baseClass.getName()+"BeanInfo: array complete");    
		}
        catch(IntrospectionException ie)
        {
           TDebug.println(ie.getMessage());
		}
           
	} 

	public static Collection getPropertyList()
	{
		return sProperties;
	}

	public PropertyDescriptor[] getPropertyDescriptors()
	{
		return (PropertyDescriptor[]) sProperties.toArray(sPropertyTemplate);
	}

}