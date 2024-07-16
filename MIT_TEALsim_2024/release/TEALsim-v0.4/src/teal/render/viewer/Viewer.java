/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: Viewer.java,v 1.23 2007/07/16 22:04:58 pbailey Exp $ 
 * 
 */

package teal.render.viewer;

import java.awt.*;
import java.awt.geom.*;
import java.beans.*;
import java.util.*;

import javax.media.j3d.*;

import teal.render.*;
import teal.ui.*;
import teal.util.*;

/**
 * Provides the abstract viewer panel where the Renderd objects are displayed. 
 *
 *
 * @author Phil Bailey - Center for Educational Computing Initiatives / MIT
 *
 */

public abstract class Viewer extends UIPanel implements TViewer
{
    
    
    protected RenderListener renderListener;
    protected Bounds mBounds;
	protected AffineTransform affTrans;
	protected TAbstractRendered pickObject;
    protected boolean show_ob = true;
	protected Collection dontDraw = null;
    protected int pickMode;
    protected boolean isPicking = false;
    protected int navMode = TViewer.NONE;
    protected SelectManager selectManager = null;

		
    public void setShowObjects( boolean x ) { show_ob = x; repaint(); }
   
    public boolean getShowObjects() { return show_ob; }
	
    
    /**
     * A list of all drawable objects that have to be rendered in the view.
     * The position of the objects drawn on the
     * panel change very often, so for every render cycle all
     * the objects in this list are painted onto the screen.
     */
    //protected SortedList drawObjects;
    // uncomment setRenderOrder() contents if using SortedList
    protected ArrayList drawObjects;
    
    public Viewer() {
        super();
        setLayout(null);
		affTrans = null;
        mBounds = null;
		
        drawObjects = new SortedList();
        selectManager = null;
    }
 
    public SelectManager getSelectManager()
    {
        return selectManager;
    }
    public void setSelectManager(SelectManager sm)
    {
        selectManager = sm;
    }
    public void setRenderOrder(Comparator cmp){
        //drawObjects.setComparator(cmp);
    }
    
    public void checkRefresh(){}
    //public void checkStart(){}
    //public void checkStop(){}


    /**
     * The method called once all objects have been updated.
	 * The off screen foreground image is redrawn with the new information.
     * Repaint is then called to put the new data to the screen, after the
	 * repaint is complete <code>renderComplete()</code> is called.
     */
    public synchronized void render() {
		//TDebug.println(2,getID() + " render called");
		render(true);
	}

    public abstract void render(boolean doRepaint);

    public RenderListener getRenderListener() {
        return renderListener;
    }
    
	public void renderComplete()
	{
		if (renderListener != null)
			renderListener.renderComplete( this );
	}
    
    public void setRenderListener(RenderListener rl) {
        renderListener = rl;

    }
    public void clearRenderListener() {
        renderListener = null;

    }
	/**
	* gets the Graphics2D for this component.
	*/
	public abstract Graphics2D getGraphics2D();

    /**
	* gets the world coordinates of the viewable area.
	*/
    public Bounds getBoundingArea() {
        return mBounds;
    }
    
    /**
    * Sets the World coordinates displayed within the viewArea, also re-calculates the
    * drawing transform.
    */
    
    public void setBoundingArea(Bounds bb) {
        PropertyChangeEvent pce = new PropertyChangeEvent(this,"boundingArea",
        new BoundingBox(mBounds), new BoundingBox(bb));
        mBounds = bb;
		makeTransform(getWidth(),getHeight());
        firePropertyChange(pce);
		TDebug.println(1, getID() +": bounds=" +mBounds);
		render(true);
    }
    public void clear(){};
	public void initialize()
	{
	}

    /**
     * Adds an TDrawable Object to the simulation.
     *
     * @param draw
     */
    public void addDrawable(TAbstractRendered draw) {
		synchronized(drawObjects) {
			drawObjects.add(draw);
		}
    }
    
    
    
    /** Removes a TDrawable object.
     *
     * @param draw
     */
    public void removeDrawable(TAbstractRendered draw) {
        drawObjects.remove(draw);
    }
    
    public AffineTransform getAffineTransform() {
         return affTrans;
     }
	 
     /**
       * Sets the current transform, does no bounds checking,this should
       * not be called from the application.
       * @param trans
       */
      protected void setTransform(AffineTransform trans) {
     this.affTrans = trans;
      }
    
     public AffineTransform getInvertedAffineTransform(){
	 	AffineTransform iat = null;
		if (affTrans != null)
		{
			try
			{
				iat = affTrans.createInverse();
			}
			catch(NoninvertibleTransformException nte)
			{
				TDebug.println(0,nte.getMessage());
				nte.printStackTrace();
			}
		}
		
    	return iat;
    }
 
	protected abstract void makeTransform(double w,double h);
    
	
	public void destroy()
	{
 		//simListeners.clear();
		drawObjects.clear();
		mBounds = null;

	}
	
	public void addDontDraw(Object obj)
	{
		if(dontDraw == null)
			dontDraw = new Vector();
		if(! (dontDraw.contains(obj)))
			dontDraw.add(obj);
            processDrawnObjs(obj, false);    
	}
		
	public void removeDontDraw(Object obj)
	{
		if(dontDraw != null);
		{
			dontDraw.remove(obj);
            processDrawnObjs(obj, true);
		}
	}
	
    
    protected synchronized void processDrawnObjs(Object type,boolean state)
    {
        //TDebug.println(0,"ProcessingDrawnObjs:");
    	synchronized(drawObjects) {
	        Iterator it = drawObjects.iterator();
	        while( it.hasNext())
	        {
	            TDrawable d = (TDrawable) it.next();
	            //TDebug.println(0,"\t" + d);
	            if (type instanceof Class) {
	                if(((Class) type).isInstance(d)) {
	                    d.setDrawn(state);
	                    //TDebug.println(0,"\t\t" + state);
	               
	                }				
	            }
	            else if( d.equals(type)) {
	                d.setDrawn(state);
	                //TDebug.println(0,"\t\t" + state);
	              
	            }
	        }
    	}
    }
    
    
    
	protected boolean checkDraw(TDrawable d)
	{
        if (d.isDrawn())
        {
		    boolean status = true;
		
		    if ((dontDraw != null) && (dontDraw.size() >0))
		    {
			    Iterator it = dontDraw.iterator();
			    while (it.hasNext())
			    {
				    Object obj = it.next();
				    if (obj instanceof Class)
				    {
					    if(((Class) obj).isInstance(d)){
						    status = false;
						    break;
					    }				
				    }
				    else if( d.equals(obj)){
					    status = false;
					    break;
				    }
			    }
		
		    }
				
		    return(status);
        }
        else
        {
            return false;
        }
	}
    
    
 

}

