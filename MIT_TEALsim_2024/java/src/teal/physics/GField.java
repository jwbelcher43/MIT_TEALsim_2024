/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: GField.java,v 1.5 2007/08/17 19:38:29 jbelcher Exp $ 
 * 
 */

package teal.physics;

import java.util.Iterator;

import javax.vecmath.Vector3d;

import teal.core.TElement;
import teal.field.CompositeField;
import teal.field.Field;
import teal.util.TDebug;

/** This class is a gravitational field implementation of CompositeField.
 *
 */
public class GField extends CompositeField
{
	
	
	public int getType()
	{
		return Field.G_FIELD;
	}
	
	public GField()
	{
		super();
	}
	
	
	public void add(TElement obj)
		throws ClassCastException
	{
		try
		{
			GeneratesG ob = (GeneratesG) obj;
			objects.add(ob);
		}
		catch(ClassCastException e)
		{
			TDebug.println(0,"ClassCastException to GeneratesG for " + obj.getID());
			throw new ClassCastException("ClassCastException to GeneratesG for " + obj.getID());
		}
	}
	
	
	/** This method gets the Gravitational Field due to all the GeneratesG objects together in the collection
	 *
	 * @param pos Position at which the gravitational field has to be calculated.
	 * @param data Will be filled with the return result.
	 * @return Gravitational Field of all the GeneratesE objects in the collection.
	 */
	public Vector3d get(Vector3d pos,Vector3d data)
	{
		data.set(0.,0.,0.);
		Iterator it = objects.iterator();
		while (it.hasNext())
		{
			GeneratesG obj = (GeneratesG) it.next();
			if (obj.isGeneratingG())
			{
				data.add(obj.getG(pos));
			}
		}
		
	    return data;
		
	}
	
	
	/** This method gets the Gravitational Field due to all the GeneratesG objects together in the collection
	 *
	 * @param pos Position at which the gravitational field has to be calculated.
	 * @return Gravitational Field of all the GeneratesG objects in the collection.
	 */
	public Vector3d get(Vector3d pos)
	{
		Vector3d field = new Vector3d();
		
		Iterator it = objects.iterator();
		while (it.hasNext())
		{
			GeneratesG obj = (GeneratesG) it.next();
			if (obj.isGeneratingG())
			{
				field.add(obj.getG(pos));
			}
		}
		
	    return field;
		
	}
	
	public Vector3d get(Vector3d pos,Vector3d data, double t)
	{
	    data.set(0.,0.,0.);
		
		Iterator it = objects.iterator();
		while (it.hasNext())
		{
			GeneratesG obj = (GeneratesG) it.next();
			if (obj.isGeneratingG())
			{
				data.add(obj.getG(pos,t));
			}
		}
		
	    return data;
		
	}
	
	public Vector3d get(Vector3d pos, double t)
	{
	    Vector3d data = new Vector3d();
		
		Iterator it = objects.iterator();
		while (it.hasNext())
		{
			GeneratesG obj = (GeneratesG) it.next();
			if (obj.isGeneratingG())
			{
				data.add(obj.getG(pos,t));
			}
		}
		
	    return data;
		
	}
	
	/** Similar to the above function but excludes one GeneratesG object when calculating the field due to all the objects in the collection.
	 *
	 * @param xobj The object that has to excluded in calculating the total electric field.
	 * @param pos Position at which the gravitational field has to be calculated.
	 * @return Gravitational Field of all the GeneratesG objects in the collection, except 'obj'.
	 */
	public Vector3d get(Vector3d pos,TElement xobj)
	{
		Vector3d field = new Vector3d();
		Iterator it = objects.iterator();
		while (it.hasNext())
		{
			GeneratesG obj = (GeneratesG) it.next();             
			if ((obj != xobj) && (obj.isGeneratingG()))
			{
				field.add(obj.getG(pos));
			}
		}
		
	    return field;
		
	}
	
	public Vector3d get(Vector3d pos,TElement xobj,double t)
	{
	    Vector3d field = new Vector3d();
		Iterator it = objects.iterator();
		while (it.hasNext())
		{
			GeneratesG obj = (GeneratesG) it.next();
			if ((obj != xobj) && (obj.isGeneratingG()))
			{
				field.add(obj.getG(pos,t));
			}
		}
		
	    return field;
		
	}


	/** This method is empty, and is included for compatibility.
	 *
	 * @param pos Position.
	 * @return Zero.
	 */
	public double getFlux(Vector3d pos)
	{
	    return 0.;
	}

	public String toString(){
		return "G Field";
	}
}







