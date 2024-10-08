/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: Graph.java,v 1.8 2007/07/16 22:04:48 pbailey Exp $ 
 * 
 */

package teal.plot;

import java.util.*;

import teal.core.TUpdatable;
import teal.sim.TSimElement;

public class Graph extends teal.plot.ptolemy.Plot implements TUpdatable, TSimElement {

    private static final long serialVersionUID = 3761131530906252082L;

    protected Collection plotItems;

    public Graph() {
        super();
        plotItems = new ArrayList();

    }

    public synchronized void addPlotItem(PlotItem pi) {
        if (!plotItems.contains(pi)) {
            plotItems.add(pi);
        }
    }

    public synchronized void removePlotItem(PlotItem pi) {

        if (plotItems.contains(pi)) {
            plotItems.remove(pi);
        }
    }

    public void update() {
        Iterator it = plotItems.iterator();
        while (it.hasNext()) {
            PlotItem pi = (PlotItem) it.next();
            pi.doPlot(this);
        }
        repaint();
        Thread.yield();      
    }
}
