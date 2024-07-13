/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: VisualizationControl.java,v 1.31 2008/07/15 10:02:08 jbelcher Exp $ 
 * 
 */

package teal.sim.control;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;

import teal.field.Potential;
import teal.framework.HasFramework;
import teal.framework.TAbstractFramework;
import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.sim.engine.TEngineControl;
import teal.sim.engine.TEngine;
import teal.physics.em.EMEngine;
import teal.sim.spatial.FieldConvolution;
import teal.sim.spatial.FieldDirectionGrid;
import teal.sim.spatial.FieldLine;
import teal.sim.spatial.FieldLineManager;
import teal.ui.ProgressBar;
import teal.ui.control.ControlGroup;
import teal.ui.control.PropertyInteger;
import teal.util.TDebug;
import teal.visualization.dlic.DLIC;

/**
 * @author pbailey
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class VisualizationControl extends ControlGroup implements HasFramework, ActionListener {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 9163152182205240108L;
    public static final int CHANGE_FL_COLORMODE = 0x01;
    public static final int CHANGE_FL_SYMMETRY = 0x02;
    //public static final int CHANGE_FL_COLORMODE = 0x01;
    //public static final int CHANGE_FL_COLORMODE = 0x01;

    private TFramework fWork;
    private FieldConvolution fconvolution;
    private FieldDirectionGrid fieldVis;
    private FieldLineManager manager = null;
    private JCheckBox showFVisCB;
    private JCheckBox showLinesCB;
    private JCheckBox colorModeCB;
    private ProgressBar convoProgress;
    private PropertyInteger fvSlider;
    private PropertyInteger flSlider;
    private int convoFlags;
    private ArrayList convoButtons = null;

    private boolean showVectorField = true;
    private boolean showFieldLines = true;
    private int actionFlags = CHANGE_FL_COLORMODE | CHANGE_FL_SYMMETRY;
    private boolean perVertexColor = true;

    /**
     * 
     */
    public VisualizationControl() {
        super();
        setText("Generate Field Visualization");
        showFVisCB = new JCheckBox("Show Vector Field Grid", showVectorField);
        showFVisCB.addActionListener(this);
        fvSlider = new PropertyInteger();
        fvSlider.setMinimum(0);
        fvSlider.setMaximum(25);
        fvSlider.setPaintTicks(true);
        fvSlider.setValue(20);
        fvSlider.setText("Resolution");

        showLinesCB = new JCheckBox("Field Lines", showFieldLines);
        showLinesCB.addActionListener(this);

        flSlider = new PropertyInteger();
        flSlider.setMinimum(1);
        flSlider.setMaximum(80);
        flSlider.setPaintTicks(true);
        flSlider.setValue(40);
        flSlider.setText("Number of Lines");

        colorModeCB = new JCheckBox("Vertex Coloring");
        colorModeCB.addActionListener(this);
        colorModeCB.setSelected(perVertexColor);
        setFVControlsVisible(false);
        setFLControlsVisible(false);
        setDLICControlsVisible(false);
        add(showFVisCB);
        add(fvSlider);
        add(showLinesCB);
        add(flSlider);
        add(colorModeCB);

    }

    public TFramework getFramework() {
        return fWork;
    }

    /* (non-Javadoc)
     * @see teal.sim.engine.HasEngine#setModel(teal.sim.engine.TEngine)
     */
    public void setFramework(TFramework eMgr) {
        if ((fWork != null) && (fWork != eMgr) && (fWork instanceof TFramework)) {
        	TFramework tfWork = (TFramework)fWork;
            //remove any existing Elements
            if (fconvolution != null) {
                tfWork.removeTElement(fconvolution);
            }
            if (fieldVis != null) {
                tfWork.removeTElement(fieldVis);
            }
            if (manager != null) {
                tfWork.removeTElement(manager);
            }
        }
        if(eMgr instanceof TFramework) {
	        fWork = eMgr;
	        if (fWork != null) {
	        	TFramework tfWork = (TFramework)fWork;
	            if (fconvolution != null) {
	                tfWork.addTElement(fconvolution, false);
	            }
	            if (fieldVis != null) {
	                tfWork.addTElement(fieldVis, false);
	            }
	            if (manager != null) {
	                tfWork.addTElement(manager, false);
	            }
	        }
        }
    }

    public void setActionFlags(int value) {
        actionFlags = value;
        setFLControlsVisible(showLinesCB.isSelected());
    }

    private void setFLControlsVisible(boolean b) {
        showLinesCB.setVisible(b);
        flSlider.setVisible(b && ((actionFlags & CHANGE_FL_SYMMETRY) == CHANGE_FL_SYMMETRY));
        colorModeCB.setVisible(b && ((actionFlags & CHANGE_FL_COLORMODE) == CHANGE_FL_COLORMODE));
    }

    private void setFVControlsVisible(boolean b) {
        showFVisCB.setVisible(b);
        fvSlider.setVisible(b);
    }

    private void setDLICControlsVisible(boolean b) {
        showFVisCB.setVisible(b);
        fvSlider.setVisible(b);
    }

    public void setConvolutionModes(int cFlags) {
        convoFlags = cFlags;
        buildConvoActions();
    }

    public int getConvoFlags() {
        return convoFlags;
    }

    public void setFieldConvolution(FieldConvolution fc) {
        fconvolution = fc;
        buildConvoActions();
        if (fconvolution != null) {
            mElements.add(fc);
            if ((fWork != null) && (fWork instanceof TFramework)) {
                ((TFramework)fWork).addTElement(fconvolution, false);
            }
        }
    }

    public void setColorPerVertex(boolean state) {
        colorModeCB.setSelected(state);
        if (manager != null) {
            manager.setColorMode(state);
        }
    }

    private void buildConvoActions() {
        if (convoButtons != null) {
            if (!convoButtons.isEmpty()) {
                Iterator it = convoButtons.iterator();
                while (it.hasNext()) {
                    JButton b = (JButton) it.next();
                    remove(b);
                }
            }
            convoButtons = null;
        }
        
        if ((convoFlags > 0) && (fconvolution != null)) {
            JButton but = null;
            convoButtons = new ArrayList();
            
            if ((convoFlags & DLIC.DLIC_FLAG_E) == DLIC.DLIC_FLAG_E) {
                but = new JButton(new TealAction("Electric Field:  Grass Seeds", String.valueOf(DLIC.DLIC_FLAG_E), this));
                but.setFont(but.getFont().deriveFont(Font.BOLD));
                convoButtons.add(but);
                add(but);
            }
            
            if ((convoFlags & DLIC.DLIC_FLAG_B) == DLIC.DLIC_FLAG_B) {
                but = new JButton(new TealAction("Magnetic Field:  Iron Filings", String.valueOf(DLIC.DLIC_FLAG_B), this));
                but.setFont(but.getFont().deriveFont(Font.BOLD));
                convoButtons.add(but);
                add(but);
            }
            
            if ((convoFlags & DLIC.DLIC_FLAG_G) == DLIC.DLIC_FLAG_G) {
                but = new JButton(new TealAction("Gravity", String.valueOf(DLIC.DLIC_FLAG_G), this));
                convoButtons.add(but);
                add(but);
            }
            
            if ((convoFlags & DLIC.DLIC_FLAG_P) == DLIC.DLIC_FLAG_P) {
                but = new JButton(new TealAction("Pauli Forces", String.valueOf(DLIC.DLIC_FLAG_P), this));
                convoButtons.add(but);
                add(but);
            }
            
            if ((convoFlags & DLIC.DLIC_FLAG_EP) == DLIC.DLIC_FLAG_EP) {
                but = new JButton(new TealAction("Electric Potential", String.valueOf(DLIC.DLIC_FLAG_EP), this));
                but.setFont(but.getFont().deriveFont(Font.BOLD));
                convoButtons.add(but);
                add(but);
            }
            
            if ((convoFlags & DLIC.DLIC_FLAG_BP) == DLIC.DLIC_FLAG_BP) {
                but = new JButton(new TealAction("Magnetic Potential", String.valueOf(DLIC.DLIC_FLAG_BP), this));
                convoButtons.add(but);
                add(but);
            }
            
            if ((convoFlags & DLIC.DLIC_FLAG_EF) == DLIC.DLIC_FLAG_EF) {
    //            but = new JButton(new TealAction("Electic Flux", String.valueOf(DLIC.DLIC_FLAG_EF), this));
                but = new JButton(new TealAction("Image of Fx(x,y) = g(x,y), Fy(x,y)=h(x,y)", String.valueOf(DLIC.DLIC_FLAG_EF), this));

                convoButtons.add(but);
                add(but);
            }
            
            if ((convoFlags & DLIC.DLIC_FLAG_BF) == DLIC.DLIC_FLAG_BF) {
 //               but = new JButton(new TealAction("Magnetic Flux", String.valueOf(DLIC.DLIC_FLAG_BF), this));
                but = new JButton(new TealAction("Image of Fx(x,y) = -h(x,y), Fy(x,y)=g(x,y)", String.valueOf(DLIC.DLIC_FLAG_BF), this));
                convoButtons.add(but);
                add(but);
            }
            
            if (convoProgress == null) {
                convoProgress = new ProgressBar();
                fconvolution.addProgressEventListener(convoProgress);
                add(convoProgress);
            }
        }

    }

    public FieldConvolution getFieldConvolution() {
        return fconvolution;
    }

    public void setFieldLineManager(FieldLineManager mgr) {
        if (mgr != null) {
            manager = mgr;
            flSlider.addRoute(manager, "symmetryCount");
            manager.setSymmetryCount(((Integer) flSlider.getValue()).intValue());
            manager.setColorMode(perVertexColor ? FieldLine.COLOR_VERTEX : FieldLine.COLOR_VERTEX_FLAT);
            setFLControlsVisible(true);
            mElements.add(manager);
            if ((fWork != null) && (fWork instanceof TFramework)) {
                ((TFramework)fWork).addTElement(manager, false);
            }
        } else {
            setFLControlsVisible(false);
        }

    }

    public FieldLineManager getFieldLineManager() {
        return manager;
    }

    public void setFieldVisGrid(FieldDirectionGrid mgr) {
        if (mgr != null) {
            fieldVis = mgr;
            fvSlider.addRoute("value", fieldVis, "resolution");
            fieldVis.setResolution(((Integer) fvSlider.getValue()).intValue());
            setFVControlsVisible(true);
            setShowFV(mgr.isDrawn());
            mElements.add(mgr);
            if ((fWork != null) && (fWork instanceof TFramework)) {
                ((TFramework)fWork).addTElement(fieldVis, false);
            }
        } else {
            setFVControlsVisible(false);
        }

    }

    public FieldDirectionGrid getFieldVisGrid() {
        return fieldVis;
    }

    public void setSymmetryCount(int num) {
        flSlider.setValue(num);
    }

    public int getSymmetryCount() {
        return (((Integer) flSlider.getValue()).intValue());
    }

    public void setShowFV(boolean state) {
        if (showFVisCB.isSelected() != state) showFVisCB.setSelected(state);
        setFVEnabled(state);
    }

    protected void setFVEnabled(boolean state) {
        fvSlider.setEnabled(state);
        fieldVis.setDrawn(state);
    }

    public void setShowLines(boolean state) {
        if (showLinesCB.isSelected() != state) showLinesCB.setSelected(state);
        setLinesEnabled(state);

    }

    protected void setLinesEnabled(boolean state) {
        flSlider.setEnabled(state);
        colorModeCB.setEnabled(state);
        manager.setDrawn(state);
    }

    public void propertyChange(PropertyChangeEvent pce) {
        if (pce.getSource() == colorModeCB) {
            String pn = pce.getPropertyName();
            if (pn.compareTo("value") == 0) {
                boolean state = ((Boolean) pce.getNewValue()).booleanValue();
                manager.setColorMode(state);
            }
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == showLinesCB) {
            setLinesEnabled(showLinesCB.isSelected());

        } else if (evt.getSource() == showFVisCB) {
            setFVEnabled(showFVisCB.isSelected());

        } else if (evt.getSource() == colorModeCB) {
            perVertexColor = colorModeCB.isSelected();
            manager.setColorMode(colorModeCB.isSelected() ? FieldLine.COLOR_VERTEX : FieldLine.COLOR_VERTEX_FLAT);

        }

        else {
            int cmd = Integer.parseInt(evt.getActionCommand());
            if (fconvolution != null) {
            	Cursor cr = null;
            	if(fWork instanceof TFramework) {
	                cr = ((TFramework)fWork).getAppCursor();
	                ((TFramework)fWork).setAppCursor(new Cursor(Cursor.WAIT_CURSOR));
            	}
                Thread.yield();
                TEngine model = fconvolution.getSimEngine();
                if (model != null) {
                    TEngineControl smc = model.getEngineControl();
                    if (smc.getSimState() == TEngineControl.RUNNING) {
                        smc.stop();
                        model.doRefresh();
                        Thread.yield();
                    }

                    switch (cmd) {
                        case DLIC.DLIC_FLAG_E:
                            fconvolution.setField(((EMEngine)model).getEField());
                            fconvolution.generateFieldImage();
                            break;
                        case DLIC.DLIC_FLAG_B:
                            fconvolution.setField(((EMEngine)model).getBField());
                            fconvolution.generateFieldImage();
                            break;
                        case DLIC.DLIC_FLAG_G:
                            fconvolution.setField(((EMEngine)model).getGField());
                            fconvolution.generateFieldImage();
                            break;
                        case DLIC.DLIC_FLAG_P:
                            fconvolution.setField(((EMEngine)model).getPField());
                            fconvolution.generateFieldImage();
                            break;
                        case DLIC.DLIC_FLAG_EP:
                            fconvolution.setField(new Potential(((EMEngine)model).getEField()));
                            fconvolution.generateFieldImage();
                            break;
                        case DLIC.DLIC_FLAG_BP:
                            fconvolution.setField(new Potential(((EMEngine)model).getBField()));
                            fconvolution.generateFieldImage();
                            break;
                        case DLIC.DLIC_FLAG_EF:
                            fconvolution.setField(((EMEngine)model).getEField());
                            fconvolution.generateColorMappedFluxImage();
                            break;
                        case DLIC.DLIC_FLAG_BF:
                            fconvolution.setField(((EMEngine)model).getBField());
                            fconvolution.generateColorMappedFluxImage();
                            break;
                        default:
                            break;
                    }
                    fconvolution.getImage();
                } else {
                    TDebug.println(0, "DLIC model is null");
                }
            	if(fWork instanceof TFramework) {
            		((TFramework)fWork).setAppCursor(cr);
            	}
            }
        }

    }

}
