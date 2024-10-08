/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: EngineControl.java,v 1.9 2008/01/05 12:49:24 jbelcher Exp $ 
 * 
 */

package teal.sim.engine;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import teal.ui.*;
import teal.util.*;

/**
 * SimEngineControl defines a UI panel for controlling the simulation engine (SimEngine).  This includes buttons to
 * start, stop, pause, step, and reset the simulation.
 *
 */
public class EngineControl extends UIPanel implements HasEngine, TEngineControl, ActionListener {

    private static final long serialVersionUID = 3545521694467962936L;

    public final static int DO_STEP = 1;
    public final static int DO_START = 2;
    public final static int DO_STOP = 4;
    public final static int DO_RESET = 8;
    public final static int DO_END = 16;
    public final static int DO_ALL = DO_STEP | DO_START | DO_STOP | DO_END | DO_RESET;
    public final static int DO_DEFAULT = DO_START | DO_STOP | DO_RESET;

    //protected int simState;

    private JButton stepButton = null;
    private JButton stopButton = null;
    private JButton endButton = null;
    private JButton resetButton = null;
    private JButton startButton = null;

    protected TEngine theModel = null;
    protected Thread worldThread = null;

    private ArrayList resetActionListenerList = new ArrayList();

    public EngineControl() {
        this(DO_ALL);
    }

    public EngineControl(int buttonMask) {
        buildPanel(buttonMask);
    }

    public EngineControl(TEngine model) {
        this();
        setEngine(model);
    }

    public TEngine getEngine() {
        return theModel;
    }

    public void setEngine(TEngine model) {
        theModel = model;
        if (theModel != null) {
        	if(theModel.getEngineControl() != this)
        		theModel.setEngineControl(this);
        }
    }

    public void rebuildPanel(int buttonMask) {
        if (stepButton != null) {
            remove(stepButton);
            stepButton = null;
        }
        if (startButton != null) {
            remove(startButton);
            startButton = null;
        }
        if (stopButton != null) {
            remove(stopButton);
            stopButton = null;
        }
        if (endButton != null) {
            remove(endButton);
            endButton = null;
        }
        if (resetButton != null) {
            remove(resetButton);
            resetButton = null;
        }
        buildPanel(buttonMask);
    }

    protected void buildPanel(int buttonMask) {
        if ((buttonMask & DO_STEP) == DO_STEP) {
            ImageIcon stepButtonIcon = new ImageIcon(URLGenerator.getResource("icons/step.png"));
            stepButton = new JButton(stepButtonIcon);
            stepButton.setBorderPainted(false);
            stepButton.setToolTipText("Step simulation");
            stepButton.setActionCommand("STEP");
            stepButton.addActionListener(this);
            add(stepButton);
        }
        
        if ((buttonMask & DO_START) == DO_START) {
            ImageIcon startButtonIcon = new ImageIcon(URLGenerator.getResource("icons/play.png"));
            startButton = new JButton(startButtonIcon);
            startButton.setBorderPainted(false);
            startButton.setToolTipText("Start simulation");
            startButton.setActionCommand("START");
            startButton.addActionListener(this);
            add(startButton);
        }
        
        if ((buttonMask & DO_STOP) == DO_STOP) {
            ImageIcon stopButtonIcon = new ImageIcon(URLGenerator.getResource("icons/pause.png"));
            stopButton = new JButton(stopButtonIcon);
            stopButton.setBorderPainted(false);
            stopButton.setToolTipText("Pause simulation");
            stopButton.setActionCommand("STOP");
            stopButton.addActionListener(this);
            add(stopButton);
        }
        
        if ((buttonMask & DO_END) == DO_END) {
            ImageIcon endButtonIcon = new ImageIcon(URLGenerator.getResource("icons/stop.png"));
            endButton = new JButton(endButtonIcon);
            endButton.setBorderPainted(false);
            endButton.setToolTipText("End simulation");
            endButton.setActionCommand("END");
            endButton.addActionListener(this);
            add(endButton);
        }
        
        if ((buttonMask & DO_RESET) == DO_RESET) {
            ImageIcon resetButtonIcon = new ImageIcon(URLGenerator.getResource("icons/reset.png"));
            resetButton = new JButton(resetButtonIcon);
            resetButton.setBorderPainted(false);
            resetButton.setToolTipText("Reset simulation");
            resetButton.setActionCommand("RESET");
            resetButton.addActionListener(this);
            add(resetButton);
        }

        setSize(getPreferredSize());
        displaySimControl(getSimState());
    }

    /**
     * The model control now itself issues reset actions, immediately after
     * it is notified of reset button's action, in actionPerformed(ActionEvent).
     * This guarantees that the world is properly reset, before other listeners,
     * such as the application, are notified. Previously, the button notified
     * others as well, which sometimes occured before the world was reset,
     * conflicting with what the listeners expected. 
     * 
     * @param ac
     */
    public void addResetActionListener(ActionListener ac) {
        if (resetButton != null)
        // resetButton.addActionListener(ac);
            resetActionListenerList.add(ac);

    }

    public void setResetActionListener(ActionListener ac) {
        if (resetButton != null)
        // resetButton.addActionListener(ac);
            resetActionListenerList.clear();
        resetActionListenerList.add(ac);

    }

    public void removeResetActionListener(ActionListener ac) {
        if (resetButton != null)
        // resetButton.removeActionListener(ac);
            resetActionListenerList.remove(ac);
    }

    private void checkThread() {
        if ((worldThread == null) || (!worldThread.isAlive())) initThread();
    }
    
    private void killThread() {
    		theModel.setSimState(TEngineControl.NOT);
    }

    private void initThread() {
		if (theModel.getSimState() == TEngineControl.NOT) {
			theModel.init();
		}
		worldThread = new Thread(theModel, "WorldTread");
		TDebug.println(1, "new world thread: " + worldThread.getName());
		worldThread.start();
    }

    /*
     * Places the TSimulation in an unknown state, no processing should be done.
     */
    public synchronized void not() {
        //theEngine.stop();
        killThread();
        displaySimControl(getSimState());
    }
    /*
     * Initializes the TSimulation
     */
    public synchronized void init() {
        checkThread();
        theModel.init();
        displaySimControl(getSimState());
    }

    /*
     * Starts the TSimulation
     */
    public synchronized void start() {
        checkThread();
    	theModel.start();
        displaySimControl(getSimState());
    }

    /*
     * Single Step a TSimulation.
     */
    public synchronized void step() {
		theModel.stop();
		theModel.step();
        displaySimControl(getSimState());
        if (resetButton != null) {
            if (!resetButton.isEnabled()) resetButton.setEnabled(true);
        }
    }

    /*
     * Pauses the TSimulation
     */
    public synchronized void stop() {
        checkThread();
        theModel.stop();
        displaySimControl(getSimState());
    }

    public synchronized void resume() {
        start();
    }

    public synchronized void reset() {
        if (theModel != null) theModel.reset();
        displaySimControl(getSimState());
    }

    /*
     * Ends the TSimulation
     */
    public synchronized void end() {
        checkThread();
        theModel.end();
        displaySimControl(getSimState());
        if (resetButton != null) {
            if (!resetButton.isEnabled()) resetButton.setEnabled(true);
        }

    }

    public synchronized void dispose() {
        if (theModel != null) theModel.dispose();
        displaySimControl(getSimState());
    }

    public int getSimState() {
        if(theModel != null) {
        	return theModel.getSimState();
        }
        return TEngineControl.NOT;
    }

    public synchronized void setSimState(int state) {
        switch (state) {
            case TEngineControl.NOT:
                if (theModel != null) {
                		theModel.setSimState(TEngineControl.NOT);
                }
                break;
            case TEngineControl.INIT:
                init();
                break;
            case TEngineControl.RUNNING:
                start();
                break;
            case TEngineControl.PAUSED:
                stop();
                break;
            case TEngineControl.ENDED:
                end();
                break;
            default:
                break;
        }
    }

    public void paint(Graphics g) {
        super.paint(g);
        //theEngine.render();
    }

    public void displaySimControl(int state) {
        switch (state) {
            case TEngineControl.NOT:
                if (stepButton != null) stepButton.setEnabled(false);
                if (startButton != null) startButton.setEnabled(false);
                if (stopButton != null) stopButton.setEnabled(false);
                if (resetButton != null) resetButton.setEnabled(false);
                break;
            case TEngineControl.INIT:
                if (stepButton != null) stepButton.setEnabled(true);
                if (startButton != null) startButton.setEnabled(true);
                if (stopButton != null) stopButton.setEnabled(false);
                if (resetButton != null) resetButton.setEnabled(false);
                break;
            case TEngineControl.RUNNING:
                if (stepButton != null) stepButton.setEnabled(false);
                if (startButton != null) startButton.setEnabled(false);
                if (stopButton != null) stopButton.setEnabled(true);
                if (resetButton != null) resetButton.setEnabled(true);
                break;
            case TEngineControl.PAUSED:
                if (stepButton != null) stepButton.setEnabled(true);
                if (startButton != null) startButton.setEnabled(true);
                if (stopButton != null) stopButton.setEnabled(false);
                if (resetButton != null) resetButton.setEnabled(true);
                break;
            case TEngineControl.ENDED:
                if (stepButton != null) stepButton.setEnabled(false);
                if (startButton != null) startButton.setEnabled(false);
                if (stopButton != null) stopButton.setEnabled(false);
                if (resetButton != null) resetButton.setEnabled(true);
                break;

        }

    }

    /*
     * TControl policy
     *  - Only one experiment can be performed by run. - Once an experiment starts, all others can no more be selected. -
     * In the beginning of the experiment, check boxes are disabled for the obvious reason that there is no IDraw data
     * to use. - Whenever some IDraw data is available, i.e. after pressing the IDraw button, the check boxes are
     * enabled. - The check boxes are enabled/disabled selected/unselected according to the rule of "never leaving the
     * viewer empty". Whenever there is no data to display in the background, the objects come back.
     */

    public void actionPerformed(ActionEvent e) {
        // *********************************************************
        TDebug.println(2, "mSEC action: " + e.getActionCommand());
        if (e.getActionCommand().equals("START")) {
            start();
        } else if (e.getActionCommand().equals("STOP")) {
            stop();
        } else if (e.getActionCommand().equals("END")) {
            end();
        } else if (e.getActionCommand().equals("RESUME")) {
            start();
        } else if (e.getActionCommand().equals("EXIT")) {
            // if (! inApplet) {
            System.exit(0);
            // }
        } else if (e.getActionCommand().equals("RESET")) {
            reset();
            Iterator it = resetActionListenerList.iterator();
            while (it.hasNext()) {
                ActionListener ac = (ActionListener) it.next();
                ac.actionPerformed(e);
            }
        } else if (e.getActionCommand().equals("STEP")) {
            step();
        } else if (e.getActionCommand().equals("REFRESH")) {
            theModel.requestRefresh();
        }

    }

}
