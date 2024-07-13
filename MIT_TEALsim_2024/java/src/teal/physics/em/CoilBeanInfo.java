/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: CoilBeanInfo.java,v 1.7 2007/07/17 15:46:53 pbailey Exp $ 
 * 
 */

package teal.physics.em;

import java.beans.PropertyDescriptor;
import java.util.*;

import teal.util.TDebug;

public class CoilBeanInfo extends RingOfCurrentBeanInfo
{

	protected static ArrayList sProperties =null;
	protected static Class baseClass = Coil.class;
	
	static
	{
		sProperties = new ArrayList(RingOfCurrentBeanInfo.getPropertyList());
/*
       try
       {
            PropertyDescriptor pd = null;


			pd = new PropertyDescriptor("",baseClass);
			pd.setBound(true);
			sProperties.add(pd);			



		}
        catch(IntrospectionException ie)
        {
           TDebug.println(ie.getMessage());
		}
*/
		TDebug.println(1,baseClass.getName()+"BeanInfo: array complete");    
           
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