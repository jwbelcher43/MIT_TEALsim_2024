/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: SelectManagerImpl.java,v 1.11 2007/07/16 22:04:58 pbailey Exp $ 
 * 
 */

package teal.render.viewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import teal.render.TAbstractRendered;


public class SelectManagerImpl implements SelectManager
{
  
        ArrayList selectListeners;
        protected ArrayList selected;
  
   public SelectManagerImpl()
   {
        selectListeners = new ArrayList();
        selected = new ArrayList();
    }

    public void addSelectListener(SelectListener listener)
    {
        selectListeners.add(listener);
    }
    
    public void removeSelectListener(SelectListener listener)
    {
        selectListeners.remove(listener);
    }
    
    public void notifySelectListeners(SelectEvent se)
    {
		Iterator it = selectListeners.iterator();
		while( it.hasNext())
		{
			Object obj = it.next();
			((SelectListener)obj).processSelection(se);
		}
    }
    
    public synchronized void addSelected(TAbstractRendered obj,boolean clear)
    {
        if (clear)
        {
                clearSelected();
        }
        obj.setSelected(true);
        SelectEvent se = new SelectEvent(obj,clear ? SelectEvent.SELECT : SelectEvent.MULTI_SELECT);
        selected.add(obj);
        notifySelectListeners(se);
    }
    
    public synchronized void clearSelected()
    {
        Iterator it = selected.iterator();
        while (it.hasNext())
        {
            TAbstractRendered ren = (TAbstractRendered) it.next();
            it.remove();
            ren.setSelected(false);
            SelectEvent se = new SelectEvent(ren,SelectEvent.NOT_SELECTED);
            notifySelectListeners(se);
        }
    }
    
    public synchronized void removeSelected(TAbstractRendered obj)
    {
        if (obj != null)
        {
            boolean state = selected.remove(obj);
            if (obj.isSelected())
            {
                obj.setSelected(false);
            }
            if (state)    
            {    
                SelectEvent se = new SelectEvent(obj,SelectEvent.NOT_SELECTED);
                notifySelectListeners(se);
            }
        }
    }
    
    public Collection getSelected()
    {
        return (Collection) selected.clone();
    }
    
    public int getNumberSelected()
    {
        return selected.size();
    }
    
    public boolean isSelectionEmpty() {
		return selected.isEmpty();
    }
    
    public void noPickResult() {
		clearSelected();
    }
    
    public boolean disableVpBehaviorWhileSelecting() {
    		return true;
    }
}