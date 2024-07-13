/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: SimEngine.java,v 1.16 2008/12/23 19:47:21 jbelcher Exp $ 
 * 
 */

package teal.sim.engine;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.WakeupOnBehaviorPost;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import teal.core.AbstractElement;
import teal.core.Referenced;
import teal.core.TUpdatable;
import teal.math.Integratable;
import teal.math.MetaIntegratable;
import teal.math.RungeKutta4;
import teal.render.Rendered;
import teal.render.TAbstractRendered;
import teal.render.viewer.AbstractViewer3D;
import teal.render.viewer.TViewer;
import teal.sim.TSimElement;
import teal.sim.collision.CollisionController;
import teal.sim.collision.HasCollisionController;
import teal.sim.properties.ForceModel;
import teal.sim.properties.IsSpatial;
import teal.sim.properties.PhysicalElement;
import teal.sim.properties.Stepping;
import teal.util.TDebug;
import teal.visualization.image.ImageGenerator;

/**
 * <code>SimEngine</code> is the central simulation engine.
 * 
 * <p>
 * All entities that can potentially interact with the simulation engine will be
 * collectively labeled as the engine's clients. The typical clients of the
 * engine are:
 * 
 * <p>- The user.
 * <p>- The application.
 * <p>- The viewer.
 * 
 * <p>
 * When an application is constructed, the engine must be notified of all
 * <code>TSimElement</code> objects to be included in the simulation, which it
 * compartmentalizes into lists of specific families. The engine naturally
 * properly manages removals as well, if these objects need to be removed later
 * in the simulation.
 * 
 * <p>
 * Clients, and specifically the application, and its user, control the engine
 * primarily through a <code>EngineControl</code> object. The
 * <code>SimEngineControl</code> instance of the latter is architectured to
 * contain the thread object within which the engine runs. As such, it can
 * directly initialize, start, step, stop and reset the simulation. These
 * actions are triggered through control methods, which place the engine in one
 * of several states, enumerated in <code>EngineControl</code>.
 * 
 * <p>
 * The simulation state controls the flow within the run loop of the engine.
 * 
 */

public class SimEngine extends AbstractElement implements TSimEngine, Integratable {

	private static final long serialVersionUID = 3257007661496152120L;

	protected RenderBehavior renderBehavior = null;

	protected TEngineControl engineControl;

	/**
	 * The bounds of the simulation space.
	 */
	protected Bounds bounds;

	/**
	 * Collision state value, where no collision has occurred.
	 */
	public static final int NOCOLLISION = 0;

	/**
	 * Collision state value, where at least a pair of <code>
	 * HasCollisionController</code>
	 * objects interpenetrate, that is satisfy all interpenetration conditions,
	 * as determined by their <code>CollisionController</code>s.
	 */
	public static final int INTERPENETRATION = 1;

	/**
	 * Collision state value, where at least a pair of <code>
	 * HasCollisionController</code>
	 * objects collide with each other, that is satisfy all collision
	 * conditions, as determined by their <code>CollisionController</code>s.
	 */
	public static final int COLLISION = 2;

	/**
	 * Collision state value, where at least a pair of <code>
	 * HasCollisionController</code>
	 * objects adhere to each other, that is satisfy all adherence conditions,
	 * as determined by their <code>CollisionController</code>s.
	 */
	public static final int ADHERENCE = 4;
	
	/**
	 * Are there objects which annihilate when they collide?
	 */
	protected boolean Annihilating;
	
	private List allElements;

	/**
	 * List of objects of type <code>TViewer</code> that were added to the
	 * engine.
	 * 
	 * @see teal.render.viewer.TViewer
	 * @see #addSimElement(TSimElement)
	 * @see #addSimElements(Collection)
	 * @see #removeSimElement(TSimElement)
	 * @see #removeSimElements(Collection)
	 */
	protected List simViewers;

	/**
	 * List of all objects of type <code>Rendered</code> that were added to
	 * the engine.
	 * 
	 * @see teal.render.TAbstractRendered
	 * @see #addSimElement(TSimElement)
	 * @see #addSimElements(Collection)
	 * @see #removeSimElement(TSimElement)
	 * @see #removeSimElements(Collection)
	 */
	protected List renderedObjs;

	/**
	 * List of all objects of type <code>IsSpatial</code> that were added to
	 * the engine.
	 * 
	 * @see teal.sim.properties.IsSpatial
	 * @see #addSimElement(TSimElement)
	 * @see #addSimElements(Collection)
	 * @see #removeSimElement(TSimElement)
	 * @see #removeSimElements(Collection)
	 */

	protected List spatialObjs;

	/**
	 * List of all objects of type <code>Integratable</code> that were added
	 * to the engine.
	 * 
	 * @see teal.math.Integratable
	 * @see #addSimElement(TSimElement)
	 * @see #addSimElements(Collection)
	 * @see #removeSimElement(TSimElement)
	 * @see #removeSimElements(Collection)
	 */
	protected List integratingObjs;

	/**
	 * List of all objects of type <code>MetaIntegratable</code> that were
	 * added to the engine.
	 * 
	 * @see teal.math.MetaIntegratable
	 * @see #addSimElement(TSimElement)
	 * @see #addSimElements(Collection)
	 * @see #removeSimElement(TSimElement)
	 * @see #removeSimElements(Collection)
	 */
	protected List metaintegratingObjs;

	/**
	 * List of all objects of type <code>Stepping</code> that were added to
	 * the engine.
	 * 
	 * @see teal.sim.properties.Stepping
	 * @see #addSimElement(TSimElement)
	 * @see #addSimElements(Collection)
	 * @see #removeSimElement(TSimElement)
	 * @see #removeSimElements(Collection)
	 */
	protected List steppingObjs;

	/**
	 * List of all objects of type <code>TUpdatable</code> that were added to
	 * the engine.
	 * 
	 * @see teal.core.TUpdatable
	 * @see #addSimElement(TSimElement)
	 * @see #addSimElements(Collection)
	 * @see #removeSimElement(TSimElement)
	 * @see #removeSimElements(Collection)
	 */

	protected List updatableObjs;

	/**
	 * List of all objects of type <code>HasCollisionController</code> that
	 * were added to the engine.
	 * 
	 * @see teal.sim.collision.HasCollisionController
	 * @see #addSimElement(TSimElement)
	 * @see #addSimElements(Collection)
	 * @see #removeSimElement(TSimElement)
	 * @see #removeSimElements(Collection)
	 */
	protected List collisionObjs;

	/**
	 * List of all objects of type <code>ImageGenerator</code> that were added
	 * to the engine.
	 * 
	 * @see teal.visualization.image.ImageGenerator
	 * @see #addSimElement(TSimElement)
	 * @see #addSimElements(Collection)
	 * @see #removeSimElement(TSimElement)
	 * @see #removeSimElements(Collection)
	 */
	protected List imageGenerators = null;

	/**
	 * <code>forces</code> is the list of all objects of type
	 * <code>ForceModel</code> that were added to the world.
	 * 
	 * @see teal.sim.properties.ForceModel
	 */
	protected List forces;

	/**
	 * Indicates the number of dependent variables that are to be integrated.
	 * Used during Runge-Kutta numerical integration.
	 * 
	 * @see teal.math.RungeKutta4
	 */
	protected int nbDependentValues;

	/** Controls the logic of the worlds run() method */
	/**
	 * Simulation state variable, takes any of the possible state values
	 * described in <code>EngineControl</code>. A client modifies this state
	 * only through an object of the latter type. The state governs the flow of
	 * the main loop in <code>run</code>.
	 * 
	 * @see TEngineControl#NOT
	 * @see TEngineControl#INIT
	 * @see TEngineControl#RUNNING
	 * @see TEngineControl#PAUSED
	 * @see TEngineControl#ENDED
	 * @see #run()
	 */
	protected int simState;

	/**
	 * Additional simulation state variable triggered in circumstances when the
	 * <code>run</code> loop should temporarily suspend regular operation when
	 * in <code>EngineControl.RUNNING</code> mode, and instead stay idle, and
	 * consitently yield the engine thread. Such measures are necessary when,
	 * for instance, doing step-by-step simulation.
	 * 
	 * Development note: there might be a way to altogether avoid using waiting
	 * mode by properly synchronyzing all methods which might interfere with the
	 * <code>run</code> loop.
	 * 
	 * @see TEngineControl#RUNNING
	 * @see #run()
	 * @see #nextStep()
	 */
	protected boolean waiting;

	/**
	 * Spatial processing, as performed by <code>doSpatial</code> is only
	 * performed when this variable is true. This only happens when spatial
	 * processing is properly requested through <code>requestSpatial</code>.
	 */
	protected boolean mNeedsSpatial = false;

	/**
	 * Refresh processing, as executed by <code>doRefresh</code> is only
	 * performed when this flag is set to true. This only happens when spatial
	 * processing is properly requested through <code>requestSpatial</code>.
	 */
	protected boolean mNeedsRefresh = false;

	/**
	 * Reorder processing, as executed by <code>doReorder</code> is only
	 * performed when this flag is set to true. This only happens when reorder
	 * processing is properly requested through <code>requestReorder</code>.
	 */
	protected boolean mNeedsReorder = false;

	/**
	 * List of all objects that are tagged as the prime causes for the
	 * reordering request. The list is used to specify priorities within the
	 * reordering algorithm.
	 * 
	 * @see #requestReorder(HasCollisionController)
	 * @see #resolveReorder()
	 */
	ArrayList reorderTagged;

	/**
	 * delay is the short time in milliseconds the thread waits after every
	 * simulation cycle before starting the next cycle.
	 */
	protected long delay = 10L;
	/**
	 * The amount of time in milliseconds that the <code>run</code> loop
	 * sleeps after a frame is computed, so that the frame rate stays as
	 * adjusted, when the engine is in the <code>EngineControl.RUNNING</code>
	 * state.
	 * 
	 * @see #run()
	 */
	protected long padDelay = 0L;

	/**
	 * The amount of time in milleseconds that the <code>run</code> loop
	 * sleeps when the engine is initialized, but is not in the
	 * <code>EngineControl.RUNNING</code> state.
	 * 
	 * @see #run()
	 */
	protected long idleDelay = 33L;

	/**
	 * The engine time. Time is the independent variable in all the numerical
	 * integrations within the engine.
	 */
	protected double time;

	/**
	 * The simulation step size. During each dynamic cycle, the simulation
	 * advances by <code>deltaTime<code>.
	 * 
	 *  @see #doDynamic()
	 */
	protected double deltaTime;

	/**
	 * Frame count variable. Calculates the number of simulation frames computed
	 * at any point in the simulation.
	 * 
	 * @see #nextStep()
	 * @see #run()
	 * 
	 */
	private long frame = 0;

	protected boolean isCheckingRate = false;

	/**
	 * The amount of time in milliseconds between two consecutive frames. This
	 * time includes both the frame computation, and any additional padded delay
	 * that is added to maintain the desired frame rate.
	 * 
	 * @see #padDelay
	 */
	protected long frameDelay = 50L;

	/**
	 * System time when last the average frame rate was computed.
	 * 
	 * @see #checkFrameRate()
	 */
	protected long oldTime = 0L;

	/**
	 * Number of frames since last the average frame rate was computed.
	 * 
	 * @see #checkFrameRate()
	 */
	protected int frameCount = 0;

	/**
	 * Number of frames over which the average frame rate is to be computed.
	 * 
	 * @see #checkFrameRate()
	 */
	protected int frameCountMax = 20;

	protected long startTime;

	protected long endTime = 0;

	/**
	 * Flag which triggers displaying the simulation time at every frame.
	 */
	private boolean showtime = false;

	/**
	 * Flag which indicates that the engine thread has started. It is initially
	 * set to false, and only set to true once the <code>run</code> loop is
	 * entered.
	 */
	private boolean threadStarted = false;

	/**
	 * List of objects scheduled for addition in the beginning of the
	 * <code>run</code> loop. This is part of a system which prevents
	 * additions and removals of objects to and from the engine during the frame
	 * computatation.
	 * 
	 * @see #run()
	 */

	private ArrayList toAddList = new ArrayList();

	/**
	 * List of objects scheduled for removal in the beginning of the
	 * <code>run</code> loop. This is part of a system which prevents
	 * additions and removals of objects to and from the engine during the frame
	 * computatation.
	 * 
	 * @see #run()
	 */
	private ArrayList toRemoveList = new ArrayList();

	private boolean isIntegrating = true;

	/**
	 * Default constructor of the engine.
	 * 
	 * <p>
	 * The following steps are carried out:
	 * <p> - The default <code>AbstractElement</code> ID is set to "theEngine".
	 * <p> - The simulation state is initialized to
	 * <code>EngineControl.NOT</code>.
	 * <p> - The default simulation step size is set to 1 sec.
	 * <p> - No title is assigned to the simulation.
	 * <p> - Object lists are created.
	 * <p> - Default spatial bounds of the simulation are set.
	 * <p> - Gravitational, magnetic, electric, and Pauli composite fields are
	 * initialized.
	 */

	public SimEngine() {
		super();
		id = "theEngine";
		simState = TEngineControl.NOT;
		time = 0.0;
		deltaTime = 1.0;

		allElements = new ArrayList();
		simViewers = new ArrayList();
		forces = new ArrayList();
		collisionObjs = new ArrayList();
		spatialObjs = new ArrayList();
		renderedObjs = new ArrayList();
		steppingObjs = new ArrayList();
		updatableObjs = new ArrayList();
		/*
		 * Should recall why these two lists were carefully synchronized.
		 */
		integratingObjs = Collections.synchronizedList(new ArrayList());
		metaintegratingObjs = Collections.synchronizedList(new ArrayList());
		reorderTagged = new ArrayList();

		bounds = new BoundingBox(new Point3d(-200, -200, -200), new Point3d(200, 200, 200));
		waiting = false;
	}

	/**
	 * Calls the default constructor, but overrides the default
	 * <code>AbstractElement</code> ID by the specified one.
	 * 
	 * @param idStr
	 * 
	 */
	public SimEngine(String idStr) {
		this();
		id = idStr;
	}

	/**
	 * Primarily responds to property change events on the spatial bounds of the
	 * simulation, by calling <code>setBoundingArea</code>.
	 * 
	 * @see #bounds
	 * @see #setBoundingArea(Bounds)
	 * @see #setBoundingArea(Point3d, Point3d)
	 */
	public void propertyChange(PropertyChangeEvent pce) {
		if (pce.getPropertyName().compareTo("boundingArea") == 0)
			setBoundingArea((Bounds) pce.getNewValue());
		else
			super.propertyChange(pce);
	}

	public void setEngineControl(TEngineControl simCtl) {
		engineControl = simCtl;
		if(((HasEngine)simCtl).getEngine() != this)
			((HasEngine)simCtl).setEngine(this);
	}

	public TEngineControl getEngineControl() {
		return engineControl;
	}

	public void setBoundingArea(Point3d lower, Point3d upper) {
		BoundingBox bb = new BoundingBox(lower, upper);
		setBoundingArea(bb);
	}

	public void setBoundingArea(Bounds bb) {
		Bounds old = getBoundingArea();
		bounds = bb;
		firePropertyChange("boundingArea", old, getBoundingArea());
		Iterator viewers = simViewers.iterator();
		while (viewers.hasNext()) {
			TViewer view = (TViewer) viewers.next();
			view.setBoundingArea(bb);
		}
	}

	public Bounds getBoundingArea() {
		return bounds;
	}

	public void setIdleDelay(long delay) {
		Long old = new Long(idleDelay);
		idleDelay = delay;
		firePropertyChange("idleDelay", old, new Long(idleDelay));
	}

	public long getIdleDelay() {
		return idleDelay;
	}

	/**
	 * Always returns true, as the engine is always integrating. The fact that
	 * the engine implements <code>Integratable</code> itself is to facilitate
	 * using <code>RungeKutta4</code>.
	 * 
	 * @see teal.math.Integratable
	 * @see teal.math.RungeKutta4
	 */
	public boolean isIntegrating() {
		return isIntegrating;
	}

	/**
	 * Adjusts the frame rate by internally properly setting the required frame
	 * to frame time, which is nothing but the inverse of the rate, expressed in
	 * milliseconds.
	 * 
	 * @see #frameDelay
	 */
	public void setFrameRate(double rate) {
		frameDelay = new Double(1000. / rate).longValue();
	}

	public double getFrameRate() {
		return 1000.0 / frameDelay;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see teal.sim.engine.TEngine#setTime(double)
	 */
	public void setTime(double t) {
		Double old = new Double(time);
		time = t;
		firePropertyChange("time", old, new Double(time));
	}

	public double getTime() {
		return time;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see teal.sim.engine.TEngine#setDeltaTime(double)
	 */
	public void setDeltaTime(double t) {
		Double old = new Double(deltaTime);
		deltaTime = t;
		firePropertyChange("deltaTime", old, new Double(deltaTime));
	}

	public double getDeltaTime() {
		return deltaTime;
	}
	
    public boolean IsAnnihilatingElement(TSimElement elm){
    	return false;
    }
    
	public void setAnnihilating(boolean Annihilating) {
		this.Annihilating = Annihilating;
	}

	public boolean getAnnihilating() {
		return Annihilating;
	}

	public int getSimState() {
		return simState;
	}

	public synchronized void setSimState(int state) {
		// TDebug.println(1,"SimEngine Setting simState: " + state);
		if (state != simState) {
			PropertyChangeEvent pce = new PropertyChangeEvent(this, "simState",
					new Integer(simState), new Integer(state));
			simState = state;
			if (engineControl != null) {
				if (engineControl instanceof EngineControl)
					((EngineControl) engineControl).displaySimControl(state);
			}
			propSupport.firePropertyChange(pce);
		}
	}

	public synchronized void dispose() {
		destroy();
	}

	protected synchronized void destroy() {
		stop();
		removeAll();
		setSimState(TEngineControl.NOT);

	}

	public synchronized void init() {
		requestRefresh();
		requestSpatial();
		doRefresh();
		setSimState(TEngineControl.INIT);
	}
	
	public void not(){
		stop();
		setSimState(TEngineControl.NOT);
	}

	public synchronized void stop() {
		if (simState == TEngineControl.ENDED)
			return;
		setSimState(TEngineControl.PAUSED);
	}

	public synchronized void end() {
		if (simState == TEngineControl.ENDED)
			return;
		setSimState(TEngineControl.ENDED);
	}

	public synchronized void start() {
		if (simState == TEngineControl.ENDED)
			return;
		setSimState(TEngineControl.RUNNING);
		waiting = false;
	}

	public synchronized void step() {
		if (simState == TEngineControl.ENDED)
			return;
		setSimState(TEngineControl.PAUSED);
		singleStep();
	}

	public synchronized void resume() {
		start();
	}

	public synchronized void reset() {
		stop();
		time = 0.;
		setImagesValid(false);
		setSimState(TEngineControl.INIT);
		requestRefresh();
	}

	/**
	 * Performs the central loop.
	 */

	public void run() {
		if(simState == TEngineControl.NOT){
			TDebug.println(2,"SimEngine run: EXITING");
			threadStarted = false;
			return;
		}
		threadStarted = true;
		// TDebug.println(2,"theEngine.run():");
		// TDebug.println(2,"SimEngine run: thread=" + theThread.getName());
		int i = 0;
		while (i < 1000000) {
			Thread.yield();
			TDebug.println(3,"SimEngine.run() i \t" + i + ": simState " + simState +",  ");
			if(simState == TEngineControl.NOT){
				TDebug.println(1,"SimEngine run: EXITING IN WHILE LOOP BEFORE SYNCH ADD/REMOVE");
				threadStarted = false;
				return;
			}
			try {
				synchronized( this){
					// Run-time addition and removal of SimElements.
					if (!toRemoveList.isEmpty() && simState != TEngineControl.NOT) {
						synchRemoveSimElements(toRemoveList);
						toRemoveList.clear();
					}
					if (!toAddList.isEmpty() && simState != TEngineControl.NOT) {
						synchAddSimElements(toAddList);
							toAddList.clear();
					}
				}
				switch (simState) {
				case TEngineControl.NOT:
					TDebug.println(2,"SimEngine run: EXITING FROM WITHIN WHILE LOOP AFTER SYNCH ADD/REMOVE");
					threadStarted = false;
					return;
				case TEngineControl.INIT:
				case TEngineControl.PAUSED:
					// TDebug.println(2,"SimEngine run: PAUSED");
					doReorder();
					doRefresh();
					Thread.sleep(idleDelay);
					break;
                
                case TEngineControl.ENDED:
                    // TDebug.println(2,"SimEngine run: PAUSED");
                    doReorder();
                    doRefresh();                   
                    break;
				case TEngineControl.RUNNING:
					// TDebug.println(2,"SimEngine run: RUNNING wait =" +
					// waiting);
					synchronized (this) {
						if (waiting) {
							Thread.yield();
							Thread.sleep(idleDelay);
						} else {
							startTime = System.currentTimeMillis();
							nextStep();
							Thread.sleep(delay);
							endTime = System.currentTimeMillis();
							padDelay = frameDelay - (startTime - endTime);
							if (padDelay > 0L) {
								Thread.sleep(padDelay);
							}
							if (isCheckingRate) {
								checkFrameRate();
							}
						}
					}
					break;
				}
			} catch (InterruptedException e) {
				TDebug.println(1, "SimEngine run InterruptedException: "
						+ e.getMessage());
			}
			i++;
		}
		threadStarted = false;
	}

	/**
	 * Advances the simulation by the next frame.
	 */
	private synchronized void nextStep() {
		doReorder();
		doDynamic();
		update();
		doRefresh();
		frame++;
	}

	/* package *//**
					 * Advances the simulation by a single step.
					 */
	synchronized void singleStep() {
		waiting = true;
		nextStep();
	}

	public void getDependentDerivatives(double[] depDerivatives, int offset,
			double time) {
		Iterator it = integratingObjs.iterator();
		while (it.hasNext()) {
			Integratable object = (Integratable) it.next();
			if (object.isIntegrating()) {
				object.getDependentDerivatives(depDerivatives, offset, time);
				offset += object.getNumberDependentValues();
			}
		}
	}

	/**
	 * Return an array of doubles holding the current values of your dependent
	 * variables.
	 */
	public void getDependentValues(double[] depValues, int offset) {

		// **************************************************************
		// Reaction Computation
		// (It seems having this here alone is good enough).
		// **************************************************************
		resolveGlobalReactions();

		// **************************************************************
		// Dependent Values
		// **************************************************************

		Iterator it = integratingObjs.iterator();
		while (it.hasNext()) {
			Integratable object = (Integratable) it.next();
			if (object.isIntegrating()) {
				object.getDependentValues(depValues, offset);
				offset += object.getNumberDependentValues();
			}
		}
	}

	public int getNumberDependentValues() {
		int number = 0;
		Iterator it = integratingObjs.iterator();
		while (it.hasNext()) {
			Integratable object = (Integratable) it.next();
			if (object.isIntegrating()) {
				number += object.getNumberDependentValues();
			}
		}
		return number;
	}

	/**
	 * The setDependentValues method of the world now incorporates a special
	 * meta-integration section which resolves the interdependencies between
	 * object variables that are not explicitly dependent variables, such as the
	 * current in a ring. The simplistic method of successive iterations is
	 * capable of yielding acceptable solution in most cases, but might diverge
	 * as well. When that happens in the case of the ring, the result is a large
	 * jump in current. A possible remedy would be the implementation of a
	 * Newton-Raphson family method.
	 */
	public void setDependentValues(double[] newDepValues, int offset) {
		// **************************************************************
		// Traditional Section
		// **************************************************************
		Iterator it = integratingObjs.iterator();
		while (it.hasNext()) {
			Integratable object = (Integratable) it.next();
			if (object.isIntegrating()) {
				object.setDependentValues(newDepValues, offset);
				offset += object.getNumberDependentValues();
			}
		}

		// **************************************************************
		// Meta-integration Section
		// **************************************************************
		boolean converged = true;
		int iter = 0;
		int maxiter = 10;
		do {
			iter++;
			converged = true;
			it = metaintegratingObjs.iterator();
			while (it.hasNext()) {
				MetaIntegratable p = (MetaIntegratable) it.next();
				if (p.isIntegrating())
					converged = converged && p.solveInterdependencies();
			}
		} while (iter < maxiter && !converged && metaintegratingObjs.size() > 1);

		// **************************************************************
		// Reaction Computation
		// (it seems we don't really need this here).
		// **************************************************************

		// resolveGlobalReactions();

	}

	private void resolveGlobalReactions() {
		// **************************************************************
		// Reaction Computation
		// (where exactly this should be performed needs to be
		// investigated better)
		// **************************************************************

		boolean converged = true;
		int iter = 0;
		int maxiter = 10;
		Iterator it = null;
		do {
			iter++;
			converged = true;
			it = collisionObjs.iterator();
			while (it.hasNext()) {
				HasCollisionController p = (HasCollisionController) it.next();
				if (p.isColliding())
					converged = converged && p.solveReactionStep();
			}

		} while (iter < maxiter && !converged && collisionObjs.size() > 1);
	}

	/**
	 * Return the double value of your independent variable. For example time.
	 */
	public double getIndependentValue() {
		return deltaTime;
	}

	/**
	 * Computes the next step of the World, this procedure is used by the World
	 * Dynamic behavior, the World calculates the nextStep of each Integratable
	 * object and stores the new values into shadow values. At every step of the
	 * world this method is called :<br>
	 * It compute the next values of all the parameters of all the objects<br>
	 */

protected synchronized void doDynamic() {
        // TDebug.println(1,"doDynamic()");
        final int max_reductionCount = 16;
        int reductionCount = 0;
        int trackingCount = 0;

        boolean collisionTrackingMode = false;
        double effectiveDeltaTime = deltaTime;

        double cumulativeDeltaTime = 0.;

        do {

            do {
                //	
                // if( !collisionTrackingMode ) {
                // System.out.println("{ Regular Step }");
                // } else {
                // System.out.println("[ Reduced Step (" + trackingCount + ") -
				// Collision Tracking ]");
                // }
                trackingCount++;

                if (trackingCount > max_reductionCount) {
                    System.out
                        .println("(trackingCount depth reached.) >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                }

                // [------ doDynamic call
				// -----------------------------------------------]
                doStepping(effectiveDeltaTime);
                	
                nbDependentValues = getNumberDependentValues();
                // Save world information.
                double[] initialValues = new double[nbDependentValues];
                getDependentValues(initialValues, 0);
                try {
                    double[] newValues = RungeKutta4.integrate(this, time, effectiveDeltaTime);
                    TDebug.println(2," SimEngine Int:" + time + " pos " + newValues[1] + " vel " + newValues[4]);
     //               TDebug.println(1," SimEngine Int:" + time + " pos " + newValues[1] + " vel " + newValues[4]);
                    setDependentValues(newValues, 0);
                } catch (ArithmeticException ae) {
                    TDebug.printThrown(0, ae);
                }
                // [---------------------------------------------------------------------]

                // [------ Collision loop
				// -----------------------------------------------]
                reductionCount = 0;
                boolean thereWasPenetration = false;
                boolean thereWasCollision = false;
                do {
                    // Check for collisions.
                    int collisionResult = checkCollisions();

                    // Respond to the collision check outcome.
                    switch (collisionResult) {
                        case INTERPENETRATION:
                            //	
                            // System.out.println("-> Interpenetration");
                            // if( thereWasCollision ) {
                            // System.out.println("(Interpenetration follows a
							// collision.)
							// >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                            // }
                            thereWasPenetration = true;
                            thereWasCollision = false;
                            effectiveDeltaTime /= 2.;
                            // Undo, followed by redo (dynamic).
                            setDependentValues(initialValues, 0);
                            reductionCount++;
                            //	
                            // System.out.println("[ Reduced Step (" +
							// reductionCount + ") - Undo/Redo ]");
                            nbDependentValues = getNumberDependentValues();
                            try {
                                double[] newValues = RungeKutta4.integrate(this, time, effectiveDeltaTime);
                                setDependentValues(newValues, 0);
                            } catch (ArithmeticException ae) {
                                TDebug.printThrown(0, ae);
                            }
                            break;
                        case COLLISION:
                            //	
                            // System.out.println("-> Collision");
                            thereWasPenetration = false;
                            thereWasCollision = true;
                            break;
                        case ADHERENCE:
                        //	
                        // System.out.println("-> Adherence");
                        case NOCOLLISION:
                            //	
                            // System.out.println("-> None");
                            if (thereWasPenetration) {
                                collisionTrackingMode = true;
                            } else if (thereWasCollision) {
                                collisionTrackingMode = false;
                            }

                            thereWasPenetration = false;
                            thereWasCollision = false;
                            break;
                        default:
                            thereWasPenetration = false;
                            thereWasCollision = false;
                            break;
                    }
                } while ((thereWasPenetration || thereWasCollision) && (reductionCount <= max_reductionCount));

                // Forced loop termination aftermath.
                if (reductionCount > max_reductionCount) {
                    System.out
                        .println("(reductionCount depth reached.) >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                    setDependentValues(initialValues, 0); // Undo dynamic.
                    collisionTrackingMode = false;
                }
                // [---------------------------------------------------------------------]

                // [------ Update pre-collision motion state
				// ----------------------------]
                if (collisionTrackingMode) {
                    Iterator it = collisionObjs.iterator();
                    while (it.hasNext()) {
                        ((HasCollisionController) it.next()).updateCollision();
                    }
                }
                // [------ delta-time accumulation
				// --------------------------------------]
                cumulativeDeltaTime += effectiveDeltaTime;

                if (showtime)
                    if (effectiveDeltaTime < deltaTime) TDebug.println("effectiveDeltaTime: " + effectiveDeltaTime);

                // Hypothetically reducing the step size here should work, but
				// it's not.
                // effectiveDeltaTime /= 2.;
            } while (collisionTrackingMode);

            effectiveDeltaTime = deltaTime - cumulativeDeltaTime;

            // [------ Update pre-collision motion state
			// ----------------------------]
            if (cumulativeDeltaTime < deltaTime) {
                Iterator it = collisionObjs.iterator();
                while (it.hasNext()) {
                    ((HasCollisionController) it.next()).updateCollision();
                }
            }

        } while (cumulativeDeltaTime < deltaTime);
        // [------ setTime call
		// -------------------------------------------------]
        setTime(time + cumulativeDeltaTime);

        if (showtime) TDebug.println("time: " + time + "\t cumulativeDeltaTime: " + cumulativeDeltaTime);

        // TDebug.println(cumulativeDeltaTime);

    }
        
        /**
		 * This method checks for collisions between all colliding objects in
		 * the simulation and returns a value representing the type of
		 * collisions detected (COLLISION, NOCOLLISION, ADHERENCE, or
		 * INTERPENETRATION). Only one instance of INTERPENETRATION is required
		 * to return that result, as it guarantees that the simulation will have
		 * to enter "time reduction mode" to resolve the collision(s).
		 * 
		 * @return flag indicating collision state detected.
		 */
	public int checkCollisions() {

		if (collisionObjs.size() > 0) {
			int status = 0;
			boolean penetration = false;
			boolean adherence = false;
			boolean collision = false;
			boolean was_adherence = false;
			Object xsim = null, ysim = null;
			HasCollisionController x = null, y = null;
			CollisionController xcg = null, ycg = null;

			int N = collisionObjs.size();
			ListIterator it1 = null, it2 = null;
			Iterator it = null;
			int index;

			ArrayList collisionList = new ArrayList();

			// Interpenetration
			it1 = collisionObjs.listIterator();
			index = 0;
			while (index < N - 1) {
				xsim = it1.next();
				x = (HasCollisionController) xsim;
				index++;
				if (!x.isColliding())
					continue;
				xcg = x.getCollisionController();
				it2 = collisionObjs.listIterator(index);
				while (it2.hasNext()) {
					ysim = it2.next();
					y = (HasCollisionController) ysim;
					if (!y.isColliding())
						continue;
					ycg = y.getCollisionController();
					status = xcg.collisionStatus(ycg);
					penetration = ((status & CollisionController.INTERPENETRATES) != 0);
					collision = ((status & CollisionController.COLLIDES) != 0);
					adherence = ((status & CollisionController.ADHERES) != 0);
					was_adherence = x.isAdheredTo(y);
					if (penetration) {
						return INTERPENETRATION;
					}
					if (collision) {
						collisionList.add(xcg);
						collisionList.add(ycg);
						
		               // System.out.println(" collision from sim engine "+x+ "    "+y);
                        if (IsAnnihilatingElement((TSimElement)xsim) && IsAnnihilatingElement((TSimElement)ysim) && Annihilating) {
                        	((Rendered)xsim).setDrawn(false);
                         	((Rendered)ysim).setDrawn(false);
                        	removeSimElement((TSimElement) xsim);
                        	removeSimElement((TSimElement) ysim);
                        }
					}
					/*
					 * if (adherence) { if (!was_adherence) {
					 * add_adherenceList.add(x); add_adherenceList.add(y); } }
					 * else { if (was_adherence) { rem_adherenceList.add(x);
					 * rem_adherenceList.add(y); } }
					 */
				}

			}

			// Collision
			it = collisionList.iterator();
			while (it.hasNext()) {
				xcg = (CollisionController) it.next();
				ycg = (CollisionController) it.next();
				xcg.resolveCollision(ycg);
			}
			if (collisionList.size() != 0) {
				return COLLISION;
			}

			// Adherence
			/*
			 * it = add_adherenceList.iterator(); while (it.hasNext()) { x =
			 * (HasCollisionController) it.next(); y = (HasCollisionController)
			 * it.next(); x.addAdheredObject(y); y.addAdheredObject(x); } it =
			 * rem_adherenceList.iterator(); while (it.hasNext()) { x =
			 * (HasCollisionController) it.next(); y = (HasCollisionController)
			 * it.next(); x.removeAdheredObject(y); y.removeAdheredObject(x); }
			 * if (add_adherenceList.size() != 0) { return ADHERENCE; }
			 */

			boolean adherence_occured = false;
			it1 = collisionObjs.listIterator();
			index = 0;
			while (index < N - 1) {
				x = (HasCollisionController) it1.next();
				index++;
				if (!x.isColliding())
					continue;
				xcg = x.getCollisionController();
				it2 = collisionObjs.listIterator(index);
				while (it2.hasNext()) {
					y = (HasCollisionController) it2.next();
					if (!y.isColliding())
						continue;
					ycg = y.getCollisionController();
					status = xcg.collisionStatus(ycg);
					adherence = ((status & CollisionController.ADHERES) != 0);
					was_adherence = x.isAdheredTo(y);
					if (adherence) {
						if (!was_adherence) {
							x.addAdheredObject(y);
							y.addAdheredObject(x);
						}
					} else {
						if (was_adherence) {
							x.removeAdheredObject(y);
							y.removeAdheredObject(x);
						}
					}
				}

			}
			if (adherence_occured) {
				return ADHERENCE;
			}

		}

		return NOCOLLISION;
	}

	// ********************************************************
	// All reorder actions should be requested.
	// (Built on the same model as refresh requests.)
	// ********************************************************
	/**
	 * Requests a Reorder on the supplied colliding object. A Reorder ensures
	 * that the supplied object does not overlap any other objects in the world
	 * due to USER INTERACTION (as opposed to simulated motion, which is
	 * resolved by the collision detection mechanism).
	 * 
	 * @param x
	 *            object to Reorder.
	 */
	public void requestReorder(HasCollisionController x) {
		reorderTagged.add(x);
		mNeedsReorder = true;
	}

	/**
	 * Reordering is the action of resolving spatial overlaps of physical
	 * objects by systematic repositioning. These overlaps are not those induced
	 * by collision, in which case we refer to the situation as
	 * interpenetration, but are due either to a random initialization or a user
	 * dragging a physical objects onto another.
	 * 
	 * A reordering is performed only if the client
	 * 
	 */

	// ********************************************************
	// Executes reordering, if needed.
	// (Built on the same model as refresh requests.)
	// ********************************************************
	/* package */void doReorder() {
		boolean needsUpdate = false;
		if (mNeedsReorder) {
			synchronized (this) {
				mNeedsReorder = false;
				needsUpdate = resolveReorder();
			}
		}
		if (needsUpdate)
			update();
		return;
	}

	synchronized private boolean resolveReorder() {
		boolean needsUpdate = false;
		if (collisionObjs.size() > 0) {
			// ********************************************************
			// Initialization.
			// ********************************************************
			boolean repeat = false;
			int status = 0;
			boolean penetration = false;
			HasCollisionController x = null, y = null;
			CollisionController xcg = null, ycg = null;
			int N = collisionObjs.size();
			ListIterator it1 = null, it2 = null;
			Iterator it = null;
			int index;
			int repeatCount, max_repeatCount;
			// ********************************************************
			// Inform the reorder-causers that they initially have
			// the priority to push all others.
			// ********************************************************
			it = reorderTagged.iterator();
			while (it.hasNext()) {
				x = (HasCollisionController) it.next();
				xcg = x.getCollisionController();
				xcg.pushPriority = 1;
			}
			// ********************************************************
			// Perform successive pushes.
			// ********************************************************
			repeatCount = 0;
			max_repeatCount = 16;
			do {
				repeat = false;
				it1 = collisionObjs.listIterator();
				index = 0;
				while (index < N - 1) {
					x = (HasCollisionController) it1.next();
					index++;
					if (!x.isColliding())
						continue;
					xcg = x.getCollisionController();
					it2 = collisionObjs.listIterator(index);
					while (it2.hasNext()) {
						y = (HasCollisionController) it2.next();
						if (!y.isColliding())
							continue;
						ycg = y.getCollisionController();
						status = xcg.collisionStatus(ycg);
						penetration = ((status & CollisionController.INTERPENETRATES) != 0);
						if (penetration) {
							xcg.resolveAdherence(ycg);
							repeat = true;
							needsUpdate = true;
						}
					}
				}
				repeatCount++;
			} while (repeat && repeatCount < max_repeatCount);
			// TDebug.println(">>>> Resolve ended.");
			// ********************************************************
			// In case of forced interruption, make sure to resolve
			// the situation as soon as possible.
			// ********************************************************
			if (repeatCount == max_repeatCount) {
				mNeedsReorder = true;
			}
			// ********************************************************
			// Cancel the pushing capabilities of all objects.
			// ********************************************************
			it = collisionObjs.iterator();
			while (it.hasNext()) {
				x = (HasCollisionController) it.next();
				if (!x.isColliding())
					continue;
				xcg = x.getCollisionController();
				xcg.pushPriority = 0;
			}
			reorderTagged.clear();
		}
		// ********************************************************
		// Since adherence resolution only changes the shadow
		// values, we need an update in the end.
		// ********************************************************

		return needsUpdate;
	}

	/**
	 * Calls the <code>nextStep</code> method of all elements in the
	 * <code>Stepping</code> list.
	 */
	protected void doStepping(double effectiveDeltaTime) {
		Iterator it = steppingObjs.iterator();
		while (it.hasNext()) {
			Stepping obj = (Stepping) it.next();
			obj.nextStep(effectiveDeltaTime);
		}
	}

	/**
	 * Calls the <code>nextSpatial</code> method of all elements in the
	 * <code>spatialObjs</code> list. The execution is performed conditionally
	 * if the <code>mNeedsSpatial</code> flag is set to true. This flag is
	 * reset after the method is executed.
	 */
	protected void doSpatial() {
		if (mNeedsSpatial) {
			setImagesValid(false);
			Iterator it = spatialObjs.iterator();
			while (it.hasNext()) {
				IsSpatial obj = (IsSpatial) it.next();
				obj.nextSpatial();
			}
			releaseSpatial();
		}
	}

	/**
	 * Calls the <code>update</code> method of all elements in the
	 * <code>TUpdatable</code> list. Most implementations of the update method
	 * will involve setting the published values of the dependent variables
	 * within the given object, to the shadow values, which are temporary values
	 * maintained throughout the integration or reordering processes.
	 * 
	 * @see #run()
	 * @see #doReorder()
	 */
	protected void update() {
		Iterator it = updatableObjs.iterator();
		if (it != null) {
			while (it.hasNext()) {
				TUpdatable p2 = (TUpdatable) it.next();
				p2.update();
			}
		}
	}

	public boolean getShowTime() {
		return showtime;
	}

	public void setShowTime(boolean b) {
		showtime = b;
	}

	public void requestSpatial() {
		mNeedsSpatial = true;
		requestRefresh();
	}

	public void releaseSpatial() {
		mNeedsSpatial = false;
	}

	/**
	 * To be used when object properties are changed via set methods, or when
	 * new objects are added or the world is initialized.
	 */
	public void requestRefresh() {
		mNeedsRefresh = true;
	}

	public void releaseRefresh() {
		mNeedsRefresh = false;
	}

	public void doRefresh() {
		if (mNeedsRefresh) {
			synchronized (this) {
				doSpatial();
				releaseRefresh();
			}
			triggerRender();
		}
	}

	protected synchronized void setImagesValid(boolean b) {
		if (imageGenerators != null) {
			Iterator it = imageGenerators.iterator();
			while (it.hasNext()) {
				((ImageGenerator) it.next()).setValid(b);
			}
		}
	}

	/**
	 * Run-time object addition to the world is now handled by requst. If the
	 * world thread has started [threadStarted, a flag set from within run()],
	 * addition occurs at the start of the run loop. Otherwise, the objects are
	 * added immediately.
	 * 
	 * <p>* Info from old addSimElement(TSimElement): Used to add a SimElement
	 * to the simulation, the SimElement list and all object type specific
	 * lists.
	 * 
	 * <p>* Info from old addSimElement(TSimElement, boolean): Used to add an
	 * SimElement to the simulation, the simElement list now only maintains a
	 * local ccopy for ease in clearing the world. The maintanance of the
	 * general element list has been moved to TEALapplet.
	 * 
	 * @param obj
	 */
	public void addSimElement(TSimElement obj) {
		if (threadStarted) {
			toAddList.add(obj);
		} else {
			synchAddSimElement(obj);
		}
	}

	synchronized protected void synchAddSimElement(TSimElement obj) {
		/*
		 * The status save and restore scheme used below insures, if this method
		 * is called at any point other than the top of the run loop, (this is
		 * possible, if addSimElement is called say, when the engine thread has
		 * not yet started), that we do not concurrently proceed with a regular
		 * run.
		 * 
		 * It might be wiser to rely on a more robust way to do this.
		 */

		boolean status = waiting;
		if (!status)
			waiting = true;

		allElements.add(obj);

		// Viewers
		if (obj instanceof TViewer) {
			addViewer((TViewer) obj);
		} else {
			/*
			 * All <code>HasEngine</code> objects other than viewers get
			 * informed about the engine at this point. The reason viewers are
			 * excluded is that they handle this internally in <code>addViewer(TViewer)</code>.
			 */
			if (obj instanceof HasSimEngine) {
				((HasSimEngine) obj).setSimEngine(this);
			}
		}

		// ImageGenerator
		if (obj instanceof ImageGenerator) {
			if (imageGenerators == null)
				imageGenerators = new ArrayList();
			imageGenerators.add(obj);

		}

		if (obj instanceof ForceModel) {
			forces.add(obj);
		}

		if (obj instanceof HasCollisionController) {
			collisionObjs.add(obj);
		}

		if (obj instanceof Stepping) {
			steppingObjs.add(obj);
		}

		if (obj instanceof TUpdatable) {
			updatableObjs.add(obj);
		}

		if (obj instanceof Integratable) {
			integratingObjs.add(obj);
		}

		if (obj instanceof MetaIntegratable) {
			metaintegratingObjs.add(obj);
		}

		if (obj instanceof IsSpatial) {
			((IsSpatial) obj).needsSpatial();
			spatialObjs.add(obj);
		}

		if (obj instanceof TAbstractRendered) {
			renderedObjs.add(obj);
		}

		requestRefresh();
		waiting = status;
		//System.out.println("synchAddElement() ADDING : " + obj);
	}

	public void removeSimElement(TSimElement obj) {
		if (threadStarted) {
			toRemoveList.add(obj);
		} else {
			synchRemoveSimElement(obj);
		}
	}

	public synchronized void synchRemoveSimElement(TSimElement obj) {
		/*
		 * The status save and restore scheme used below insures, if this method
		 * is called at any point other than the top of the run loop, (this is
		 * possible, if removeSimElement is called say, when the engine thread
		 * has not yet started), that we do not concurrently proceed with a
		 * regular run.
		 * 
		 * It might be wiser to rely on a more robust way to do this.
		 */
		boolean status = waiting;
		if (!status)
			waiting = true;

		if (obj instanceof TAbstractRendered) {
			renderedObjs.remove(obj);
		}

		if (obj instanceof ForceModel) {
			forces.remove(obj);
		}

		if (obj instanceof IsSpatial) {
			spatialObjs.remove(obj);
		}

		if (obj instanceof HasCollisionController) {
			collisionObjs.remove(obj);
		}

		if (obj instanceof MetaIntegratable) {
			metaintegratingObjs.remove(obj);
		}

		if (obj instanceof Integratable) {
			integratingObjs.remove(obj);
		}

		if (obj instanceof TUpdatable) {
			updatableObjs.remove(obj);
		}

		if (obj instanceof Stepping) {
			steppingObjs.remove(obj);
		}

		if (obj instanceof ImageGenerator) {
			imageGenerators.remove(obj);
		}

		if (obj instanceof TViewer) {
			simViewers.remove(obj);
		}

		if (obj instanceof HasSimEngine) {
			((HasSimEngine) obj).setSimEngine(null);
		}
		if (obj instanceof Referenced) {
			((Referenced) obj).removeReferents();
		}

		allElements.remove(obj);
		requestRefresh();
		waiting = status;
	}

	public void addSimElements(Collection objects) {
		//System.out.println("SimEngine addSimElements() being called!");
		if (threadStarted) {
			toAddList.add(objects);
		} else {
			synchAddSimElements(objects);
		}
	}

	public void removeSimElements(Collection objects) {
		if (threadStarted) {
			toRemoveList.add(objects);
		} else {
			synchRemoveSimElements(objects);
		}
	}

	private synchronized void synchAddSimElements(Collection objects) {
		Iterator it = objects.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof TSimElement)
				synchAddSimElement((TSimElement) obj);
		}
	}

	private synchronized void synchRemoveSimElements(Collection objects) {
		Iterator it = objects.iterator();
		while (it.hasNext()) {
			Object obj = it.next();
			if (obj instanceof TSimElement)
				synchRemoveSimElement((TSimElement) obj);
		}
	}

	/**
	 * Removes all objects that have been added to the world via
	 * <code>addSimElement</code> or <code>addSimElements</code>.
	 */
	public synchronized void removeAll() {
		List all = new ArrayList(allElements);
		removeSimElements(all);
	}

	/**
	 * Returns a cloned copy of the model's current <code>Spatial</code>s,
	 * changes to this list will not effect the engine. Objects must be removed
	 * from the engine via the <code>removeElement</code> methods.
	 */
	public Collection getSpatials() {
		return new ArrayList(spatialObjs);
	}

	public Vector3d impulse(PhysicalElement ph) {
		return new Vector3d();
	}

	public Vector3d getForces(PhysicalElement ph) {
		Vector3d f = new Vector3d();
		Iterator it = forces.iterator();
		while (it.hasNext())
			f.add(((ForceModel) it.next()).getForce(ph));
		return f;
	}

	/**
	 * Checks a given point to determine whether it is within the engine's
	 * bounds.
	 * 
	 * @return True if given point within bounds, false otherwise.
	 * @see #bounds
	 */
	public boolean checkBounds(Point3d te) {
		return bounds.intersect(te);
	}

	public void setCheckFrameRate(boolean state) {
		isCheckingRate = state;
	}

	public void checkFrameRate() {
		frameCount++;
		if (frameCount > frameCountMax) {
			long newTime = System.currentTimeMillis();
			TDebug.println("FrameRate: "
					+ (frameCount / (double) (newTime - oldTime)) * 1000.0);
			oldTime = newTime;
			frameCount = 0;
		}
	}

	/*
	 * **************************************************************************
	 * Viewer related methods and classes.
	 * **************************************************************************
	 */

	/**
	 * Posts a postId to <code>renderBehavior</code>, which wakes up and
	 * iterates over all viewers, invoking each one's <code>render</code>
	 * method. If <code>renderBehavior</code> is <code>null</code>, then
	 * the iteration is performed directly here.
	 * 
	 * @see #renderBehavior
	 * @see RenderBehavior
	 */

	protected void triggerRender() {
		if (renderBehavior != null) {
			renderBehavior.postId(RenderBehavior.POST_RENDER);
		} else {
			Iterator iv = simViewers.iterator();
			if (iv != null) {
				while (iv.hasNext()) {
					TViewer sv = (TViewer) iv.next();
					sv.render();
				}
			}
		}
		
	}

	/**
	 * Called upon completion of a Viewer rendering.
	 */
	public void renderComplete(TViewer viewer) {
		waiting = false;
	}

	/**
	 * Adds a new viewer to the engine.
	 * 
	 * Develoment remarks: The addition of a viewer of type <code>
	 * teal.render.j3d.ViewerJ3D</code>
	 * simply overrides the existing <code>renderBehavior</code> object. This
	 * suggests that, as things are, multiple viewers might not be supported.
	 */
	public synchronized void addViewer(TViewer viewer) {
		/*
		 * All elements added to the world must have proper IDs, this is insured
		 * by invoking the static <code>AbstractElement.checkID</code> method.
		 * 
		 * @see teal.core.AbstractElement#checkID(TElement)
		 */
		AbstractElement.checkID(viewer);
		/*
		 * Standard addition.
		 */
		if (viewer instanceof HasSimEngine) {
			((HasSimEngine) viewer).setSimEngine(this);
		}
		simViewers.add(viewer);
		viewer.setRenderListener(this);
		/*
		 * Viewer configuration.
		 */
		if (viewer instanceof teal.render.j3d.ViewerJ3D) {
			renderBehavior = new RenderBehavior();
			renderBehavior
					.setSchedulingBounds(AbstractViewer3D.sInfiniteBounds);
			BranchGroup behaviorGroup = new BranchGroup();
			behaviorGroup.addChild(renderBehavior);
			((teal.render.j3d.ViewerJ3D) viewer).getScene().addChild(
					behaviorGroup);
			renderBehavior.setEnable(true);
		}
	}
    
    public synchronized void enableRender(boolean b)
    {
        if(renderBehavior != null)
            renderBehavior.setEnable(b);
    }

	public synchronized void removeViewer(TViewer viewer) {
		if (viewer instanceof HasSimEngine) {
			((HasSimEngine) viewer).setSimEngine(null);
		}
		simViewers.remove(viewer);
		viewer.clearRenderListener();
	}

	public synchronized void removeViewers() {
		Collection viewers = new ArrayList(simViewers);
		Iterator it = viewers.iterator();
		while (it.hasNext()) {
			TViewer viewer = (TViewer) it.next();
			removeViewer(viewer);
		}
	}

	/**
	 * <code>RenderBehavior</code> extends the abstract class
	 * <code>Behavior</code>, to provide scheduling for the rendering.
	 * <code>SimEngine.triggerRender()</code> simply wakes up this behavior
	 * which goes on to properly render each viewer.
	 * 
	 * @see javax.media.j3d.Behavior
	 * @see SimEngine#triggerRender()
	 * @see SimEngine#renderBehavior
	 */
	/* package */class RenderBehavior extends Behavior {

		/**
		 * PostId used for the standard wakeup criterion, <code>wakeup</code>.
		 * This value is used directly in <code>SimEngine.triggerRender()</code>,
		 * by invoking <code>postId(int)</code> on the behavior.
		 * 
		 * @see SimEngine#triggerRender()
		 * @see javax.media.j3d.Behavior.postId(int)
		 */
		public static final int POST_RENDER = 137;

		/**
		 * Standard wakeup criterion, initialized in <code>initialize()</code>
		 * to correspond to the postId defined by <code>POST_RENDER</code>.
		 * 
		 * @see #initialize()
		 */
		WakeupOnBehaviorPost wakeup = null;

		/**
		 * Initializes the behavior by creating a standard wakeup criterion
		 * corresponding to the postId defined by <code>POST_RENDER</code,
		 * then arms the first wakeup.
		 */
		public void initialize() {
			wakeup = new WakeupOnBehaviorPost(this, POST_RENDER);
			wakeupOn(wakeup);
		}

		/**
		 * Iterates through all viewers and invokes the <code>render</code>
		 * method on each. Before the iteration, the engine is put in "waiting"
		 * state. After the iteration a <code>wakeupOn(...)</code> call is
		 * made to rearm the next wakeup.
		 */
		public void processStimulus(Enumeration criteria) {
			waiting = true;
			Iterator iv = simViewers.iterator();
			if (iv != null) {
				while (iv.hasNext()) {
					TViewer sv = (TViewer) iv.next();
					sv.render();
				}
			}
			wakeupOn(wakeup);
			//System.out.println("RenderBehavior processStimulus (SPAM!)");
		}
	}

	/*
	 * **************************************************************************
	 * Deprecated or unused.
	 * **************************************************************************
	 */

	/**
	 * Not effective for the engine. The engins is always integrating. The fact
	 * that the engine implements <code>Integratable</code> itself is to
	 * facilitate using <code>RungeKutta4</code>.
	 * 
	 * @see teal.math.Integratable
	 * @see teal.math.RungeKutta4
	 */
	public void setIntegrating(boolean b) {
		isIntegrating = b;
	}
	
	public List getViewers() {
		return simViewers;
	}
	
	public void printAllElements() {
		System.out.println("SimEngine printAllElements():");
		for (int i = 0; i < allElements.size(); i++) {
			System.out.println("Element " + i + ": " + allElements.get(i).toString());
		}
	}
}
