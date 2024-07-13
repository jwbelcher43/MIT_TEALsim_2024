/* $Id: ExploringPotential.java,v 1.12 2008/12/23 20:01:07 jbelcher Exp $ */
/**
 * @author John Belcher 
 * Revision: 1.0 $
 */

package tealsim.physics.em;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.Random;
import javax.media.j3d.*;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;
import javax.vecmath.*;

import teal.render.j3d.loaders.Loader3DS;

import teal.config.Teal;
import teal.field.Field;
import teal.framework.TFramework;
import teal.framework.TealAction;
import teal.math.RectangularPlane;
import teal.plot.PlotProperties;
import teal.plot.Graph;
import teal.render.Rendered;
import teal.sim.collision.SphereCollisionController;
import teal.sim.control.VisualizationControl;
import teal.sim.engine.SimEngine;
import teal.sim.engine.TEngineControl;
import teal.sim.spatial.ComponentForceVector;
import teal.sim.spatial.FieldConvolution;
import teal.sim.spatial.FieldDirectionGrid;
import teal.sim.spatial.FieldValue;
import teal.sim.spatial.FieldVector;
import teal.sim.spatial.SpatialTextLabel;
import teal.physics.physical.Ball;
import teal.physics.physical.PhysicalObject;
import teal.physics.physical.Wall;
import teal.physics.em.EMEngine;
import teal.physics.em.MagneticDipole;
import teal.physics.em.PointCharge;
import teal.physics.em.RingOfCurrent;
import teal.physics.em.SimEM;
import teal.render.geometry.Cylinder;
import teal.render.geometry.Sphere;
import teal.render.j3d.*;
import teal.render.primitives.Line;
import teal.render.scene.TShapeNode;
import teal.ui.control.*;
import teal.ui.UIPanel;
import teal.util.TDebug;
import teal.visualization.dlic.DLIC;



public class Landscape extends SimEM {

    private static final long serialVersionUID = 3257008735204554035L;
	Ball ball;
	double arrowScale = 0.05;
    double pointChargeRadius = 0.2; 
    double initialMult = 4.5;
	
	FieldVector theArrow;
	JTextArea messages;
	boolean clearText = false;
	
	Vector3d curPos = null;
	KeyAdapter kListener;
	UIPanel gameControls;
	 JRadioButton rad1;
	  VisualizationControl vis;
	    ControlGroup changeSign;
	    ControlGroup moveAvatar;
     JRadioButton rad2;     
 	/** ButtonGroup for the two ways to show E*/
 	ButtonGroup optionsGroup1;
 	/** Radio button for scale by magnitude.*/
     JRadioButton rad3;
 	/** Radio button for same length E vectors.*/
     JRadioButton rad4;
     double potpower = .5;
    double widthtotal = 5.;
    int initialObserver = 5;
    int halfwall =12;  // this is approximately half the number of squares on a side
	int nwall = 2*halfwall+1;  // this the number of squares on a side, it is odd by construction
    double sizewall = widthtotal/nwall;  // this is the length of one square, so if we have 25 squares of length 0.2, the total length will be 5.
    int wrongGuess = 40;
    
    Wall[][] wall;

    boolean[][] visited;
    int chargeCount;
    int numMoves=0;
    int idxX = 0;
	int idxY = 0;
	int max = 0;
	int count = 0;
	double refPowerPotential = 0.;
	double scale = 0.35;
	double PotentialScale = 0.;
	double maxPotential;
	double minPotential;
    Random rand;
	SpatialTextLabel lbl,lb3,lb7,lb9;
	SpatialTextLabel lb2,lb4,lb6,lb8;
    
    /** Slider for the y-position of the cylinder/sphere.  */
    PropertyDouble posSlider_y = new PropertyDouble();
    /** Slider for the x-position of the cylinder/sphere.  */
    PropertyDouble posSlider_x = new PropertyDouble();
    
    /** Slider for the y-position of the cylinder/sphere.  */
    PropertyDouble posSlider1_y = new PropertyDouble();
    /** Slider for the x-position of the cylinder/sphere.  */
    PropertyDouble posSlider1_x = new PropertyDouble();
    
    PointCharge pcA;
    PointCharge pcB;
    protected FieldConvolution mDLIC = null;


    /** An imported 3DS object (an icon for the observer).  */
    Rendered observer;
    Rendered iconobserver;
    Rendered arrowE;
    
    /** A 3D node for the observer icon. */
    Node3D observerNode;
    Node3D iconobserverNode;
    Node3D arrowENode;
    ControlGroup ScaleByMagnitude;
    double powerScale = .3;
    double normFactor = .1;

	
    public Landscape() {
        super();

        TDebug.setGlobalLevel(0);

        title = "Electrostatic Potential Landscapes";
        
		///// Set properties on the SimEngine /////
		// Bounding area represents the characteristic size of the space.
		// setDeltaTime() sets the time step of the simulation.
		// setDamping() sets the damping on the system.
        setMarkers();
        
        vis = new VisualizationControl();
        
        vis.setText("Field Visualization");
        vis.setFieldConvolution(mDLIC);
        vis.setConvolutionModes(DLIC.DLIC_FLAG_E|DLIC.DLIC_FLAG_EP);
       //  vis.setFieldVisGrid(fv);
    //    addElement(vis);

        
        BoundingSphere bs = new BoundingSphere(new Point3d(0, 1.6, 0), 03.5);
        theEngine.setBoundingArea(bs);
        theEngine.setDeltaTime(0.005); 
        mViewer.setBoundingArea(bs);
        mViewer.setBackgroundColor(new Color(37*4,49*4,255));
              
        mDLIC = new FieldConvolution();
        RectangularPlane rec =  new RectangularPlane(new Vector3d(-widthtotal/2.,-widthtotal/2., 0.),
				new Vector3d(-widthtotal/2.,widthtotal/2.,0.), new Vector3d(widthtotal/2.,widthtotal/2.,0.));
        mDLIC.setSize(new Dimension(512,512));
        mDLIC.setComputePlane(rec);
        mSEC.rebuildPanel(0);
        //addElement(mDLIC);
        rand = new Random();
        
        // We create a two D array of walls.
		// Wall constructor.  	
        
        wall = new Wall[nwall][nwall];
        visited = new boolean[nwall][nwall];
        for (int i = 0; i < nwall; i++) 
        {
        	for (int j = 0; j < nwall; j++) 
        	{
        		Vector3d posnow = new Vector3d(getPosition(i,j));
        		wall[i][j]= new Wall(getPosition(i,j), 
                		new Vector3d(0., sizewall, 0.), new Vector3d(sizewall, 0., 0.));
        		addElement(wall[i][j])	;
        	//System.out.println(" i " + i +  " j " + j + " wall.pos " + posnow );
        	} 	
       }
        
   
        max = nwall -1;
        theArrow = new FieldVector();
		theArrow.setColor(Teal.PointChargePositiveColor);
		theArrow.setArrowScale(.75);
		theArrow.setDrawn(false);
 		theArrow.setNormFactor(normFactor);
 		theArrow.setPowerScale(powerScale);
 		theArrow.setScaleByMagnitude(false);
        addElement(theArrow);
   
        // Additional UI Controls
        GridBagLayout gbl =new GridBagLayout();
        GridBagConstraints con = new GridBagConstraints();
        con.gridwidth = GridBagConstraints.REMAINDER; //end row

        gameControls = new UIPanel();
        gameControls.setLayout(gbl);
        UIPanel buttonGrid = new UIPanel();
        buttonGrid.setLayout(new GridLayout(3,3));
        Button btn =new Button("1");
        btn.setEnabled(true);
        //btn.setOpaque(true);
        btn.setBackground(Color.red);
        //btn.setForeground(Color.red);
        btn.addActionListener(this);
        buttonGrid.add(btn);
        btn =new Button("2");
        btn.setEnabled(true);
        
        btn.addActionListener(this);
        buttonGrid.add(btn);btn =new Button("3");
        btn.setEnabled(true);
        //btn.setOpaque(true);
        btn.setBackground(Color.yellow);
        //btn.setForeground(Color.yellow);
        btn.addActionListener(this);
        buttonGrid.add(btn);btn =new Button("4");
        btn.setEnabled(true);
        btn.addActionListener(this);
        buttonGrid.add(btn);btn =new Button("5");
        btn.setEnabled(true);
        btn.addActionListener(this);
        buttonGrid.add(btn);btn =new Button("6");
        btn.setEnabled(true);
        btn.addActionListener(this);
        buttonGrid.add(btn);btn =new Button("7");
        //btn.setOpaque(true);
        //btn.setForeground(Color.green);
        btn.setBackground(Color.green);
        btn.setEnabled(true);
        btn.addActionListener(this);
        buttonGrid.add(btn);btn =new Button("8");
        btn.setEnabled(true);
        btn.addActionListener(this);
        buttonGrid.add(btn);btn =new Button("9");
       // btn.setOpaque(true);
        btn.setBackground(Color.blue);
        //btn.setForeground(Color.blue);
        btn.setEnabled(true);
        btn.addActionListener(this);
        buttonGrid.add(btn);
        UIPanel options = new UIPanel();
        options.setBorder(BorderFactory.createLineBorder(Color.black));

      
        
        JButton scoreBtn = new JButton("Get Score");
        scoreBtn.addActionListener(this);
        JButton newBtn = new JButton("New game");
        newBtn.addActionListener(this);
        gbl.setConstraints(buttonGrid, con);
        
        messages = new JTextArea();
        messages.setColumns(32);
        messages.setRows(4);
        messages.setLineWrap(true);
        messages.setWrapStyleWord(true);
        messages.setVisible(true);
        messages.setText("");

        gameControls.add(buttonGrid);
      //  gameControls.setName("fffff");
        gbl.setConstraints(options,con);
  //      gameControls.add(options);
        //gbl.setConstraints(scoreBtn, con);
        //gameControls.add(scoreBtn);
        gbl.setConstraints(newBtn, con);
       // gameControls.add(newBtn);
        gbl.setConstraints(messages, con);
        //gameControls.add(messages);
        moveAvatar = new ControlGroup();
        moveAvatar.setText("Controls for Moving Avatar");
        moveAvatar.add(gameControls);
        addElement(moveAvatar);

        rad1 = new JRadioButton("Change sign of charge one");        
        rad2 = new JRadioButton("Change sign of charge two");        
        rad1.addActionListener(this);
        rad2.addActionListener(this);
        changeSign = new ControlGroup();
        changeSign.setText("Change Sign of Charges");
        changeSign.add(rad1);
        changeSign.add(rad2);
        addElement(changeSign);
        
        UIPanel options1 = new UIPanel();
        options1.setBorder(BorderFactory.createLineBorder(Color.black));
       options1.setLayout(new GridLayout(2,1));
        optionsGroup1 = new ButtonGroup();
        rad3 = new JRadioButton("Scale E Arrow Length by E^0.3"); 

        rad4 = new JRadioButton("Make E Arrow Length Always the Same"); 
        rad4.setSelected(true);
        rad3.addActionListener(this);
        rad4.addActionListener(this);
		optionsGroup1.add(rad4);
		optionsGroup1.add(rad3);
		options1.add(rad4);
		options1.add(rad3);   
		   ScaleByMagnitude= new ControlGroup();
		    ScaleByMagnitude.add(options1);
	        ScaleByMagnitude.setText("Choose E Field Scaling");
	 


        observer = new Rendered();
        observerNode = new Node3D();
        iconobserver = new Rendered();
        iconobserverNode = new Node3D();
        arrowE = new Rendered();
        arrowENode = new Node3D();
               
        double scale3DS = 0.025; // this is an overall scale factor for .3DS objects
        Loader3DS max = new Loader3DS();
        BranchGroup bg01 = max.getBranchGroup("models/man1.3DS","models/maps/");
        BranchGroup bg02 = max.getBranchGroup("models/arrowE.3DS","models/maps/");
        iconobserverNode.addContents(bg01);
        iconobserverNode.setScale(scale3DS);
        iconobserver.setNode3D(iconobserverNode);
        iconobserver.setPosition(new Vector3d(0.,4.,-0.));
        addElement(iconobserver);
    	theArrow.setPosition(getPosition(initialObserver,initialObserver));
        arrowENode.addContents(bg02);
        arrowENode.setScale(scale3DS);

    	
        arrowE.setNode3D(arrowENode);
        arrowE.setPosition(new Vector3d(0.,0.,0.));
        theArrow.setDrawn(true);
  //      theArrow.setDrawn(false);
    	arrowE.setDirection(theArrow.getValue());
   // 	arrowE.setDirection(new Vector3d(1.,0.,0.));
  //      addElement(arrowE);
        observer.setNode3D(new SphereNode(0.08,16));
        

          TDebug.println(0,"Offset: " + observerNode.getModelOffsetPosition().toString()); 
       
        observer.setDrawn(false);
        observer.setColor(new Color(0,0,0));
        addElement(observer);
  
        pcA = new PointCharge();
        pcA.setPosition(new Vector3d(0.,sizewall*initialMult,0.));
        addElement(pcA);
        pcB = new PointCharge();
        pcA.setPosition(new Vector3d(0.,-sizewall*initialMult,0.));
        addElement(pcB);
        setPosition();
     // create the sliders for charge position   
        posSlider_x.setText("X Position 1");
        posSlider_x.setMinimum(-3.);
        posSlider_x.setMaximum(3.0);
        posSlider_x.setPaintTicks(true);
        posSlider_x.addPropertyChangeListener("value", this);
        posSlider_x.setValue(sizewall/2.);
        posSlider_x.setVisible(true);
        
        posSlider_y.setText("Y Position 1 ");
        posSlider_y.setMinimum(-3.);
        posSlider_y.setMaximum(3.0);
        posSlider_y.setPaintTicks(true);
        posSlider_y.addPropertyChangeListener("value", this);
        posSlider_y.setValue(sizewall*initialMult);
        posSlider_y.setVisible(true);
        
        posSlider1_x.setText("X Position 2");
        posSlider1_x.setMinimum(-3.);
        posSlider1_x.setMaximum(3.0);
        posSlider1_x.setPaintTicks(true);
        posSlider1_x.addPropertyChangeListener("value", this);
        posSlider1_x.setValue(sizewall/2.);
        posSlider1_x.setVisible(true);
        
        posSlider1_y.setText("Y Position 2");
        posSlider1_y.setMinimum(-3.);
        posSlider1_y.setMaximum(3.0);
        posSlider1_y.setPaintTicks(true);
        posSlider1_y.addPropertyChangeListener("value", this);
        posSlider1_y.setValue(-sizewall*initialMult);
        posSlider1_y.setVisible(true);
     // add the sliders to the control group and add the control group to the scene
        ControlGroup controls = new ControlGroup();
        controls.setText("Change Location of Charges");
        controls.add(posSlider_y);
        controls.add(posSlider_x);
        controls.add(posSlider1_y);
        controls.add(posSlider1_x);
        addElement(controls);
        
        addElement(ScaleByMagnitude);
        VisualizationControl vizPanel = new VisualizationControl();     
        vizPanel.setFieldConvolution(mDLIC); 
		vizPanel.setConvolutionModes(DLIC.DLIC_FLAG_E|DLIC.DLIC_FLAG_EP);
        addElement(vizPanel);

        // set paramters for mouseScale 
        Vector3d mouseScale = mViewer.getVpTranslateScale();
        
        mouseScale.x *= 0.05;
        mouseScale.y *= 0.05;
        mouseScale.z *= 0.5;
        mViewer.setVpTranslateScale(mouseScale);
        
        setPosition();
        mSEC.init(); 
        mSEC.setVisible(false);
        resetCamera();
        // addAction for pulldown menus on TEALsim windows     
        addActions();
        reset();
        showResults();
        
    }

   
    void addActions() {
        TealAction tb = new TealAction("Electrostatic Landscapes", this);
        addAction("Help", tb);
        TealAction ta = new TealAction("Execution & View", this);
        addAction("Help", ta);
   
    }
    
    public void propertyChange(PropertyChangeEvent pce) {
        Object source = pce.getSource();
        if (source == posSlider_x) {
            double posX = ((Double) pce.getNewValue()).doubleValue(); 
            posX = posX/sizewall;
            posX= sizewall*(1.*(int)posX+.5);
            Vector3d pcaPosition = pcA.getPosition();
            pcaPosition.x = posX;
            pcA.setPosition(pcaPosition);
            getPotentialMaxMin();
        	moveObserver();
            showResults();
            
        } else if (source == posSlider_y) {
            double posY = ((Double) pce.getNewValue()).doubleValue();
            posY = posY/sizewall;
            posY= sizewall*(1.*(int)posY+.5);
            Vector3d pcaPosition = pcA.getPosition();
            pcaPosition.y = posY;
            pcA.setPosition(pcaPosition);
            getPotentialMaxMin();
        	moveObserver();
            showResults();
            
        }   else   if (source == posSlider1_x) {
                double posX = ((Double) pce.getNewValue()).doubleValue();
                posX = posX/sizewall;
                posX= sizewall*(1.*(int)posX+.5);
                Vector3d pcbPosition = pcB.getPosition();
                pcbPosition.x = posX;
                pcB.setPosition(pcbPosition);
                getPotentialMaxMin();
            	moveObserver();
                showResults();
                
            } else if (source == posSlider1_y) {
                double posY = ((Double) pce.getNewValue()).doubleValue();
                posY = posY/sizewall;
                posY= sizewall*(1.*(int)posY+.5);
                Vector3d pcbPosition = pcB.getPosition();
                pcbPosition.y = posY;
                pcB.setPosition(pcbPosition);
                getPotentialMaxMin();
            	moveObserver();
                showResults();
      
        } else {
            super.propertyChange(pce);
        }
 
    }
    public void actionPerformed(ActionEvent e) {
   
        String actionCmd = e.getActionCommand();
        TDebug.println(1, " Action comamnd: " + actionCmd);
        if (actionCmd.length() == 1){
        	checkMove(actionCmd.charAt(0));
        }  
        else if (actionCmd.compareToIgnoreCase("Electrostatic Landscapes") == 0) {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).openBrowser("help/landscape.html");
        	}
        }  
        else if (actionCmd.compareToIgnoreCase("Execution & View") == 0) 
        {
        	if(mFramework instanceof TFramework) {
        		((TFramework)mFramework).openBrowser("help/executionView.html");
        	}
        
        } 
        else if (actionCmd.compareToIgnoreCase("Get Score") == 0) 
        {
        	TDebug.println(0,getScore());
        
        } 
        else if (actionCmd.compareToIgnoreCase("New Game") == 0) 
        {
        	setPosition();
        
        } 
        else if(e.getSource() == rad1){
        //	if(pcA.getCharge() > 0 &&  pcB.getCharge() > 0 && (Math.rint(pcA.getCharge()) == Math.rint(pcB.getCharge()))){
        	double newcharge = pcA.getCharge();
        	pcA.setCharge(-newcharge);
            getPotentialMaxMin();
        	moveObserver();
       		showResults();
       	}
    
        else if(e.getSource() == rad2){
        	double newcharge = pcB.getCharge();
        	pcB.setCharge(-newcharge);
            getPotentialMaxMin();
        	moveObserver();
       		showResults();
        }
        else if(e.getSource() == rad3){
        	theArrow.setScaleByMagnitude(true);		
        	moveObserver();
        	}
        else if(e.getSource() == rad4){
        	theArrow.setScaleByMagnitude(false);	
        	moveObserver();
        	}
        
           super.actionPerformed(e);
        
    }
    
    void resultMessage(String str){
    	StringBuffer buf = new StringBuffer(str);
    	//buf.append("\nYou have a total of " + numMoves + " points!");
    	//buf.append("\nCharge A: = " + pcA.getCharge() + "\nCharge B: = " + pcB.getCharge());
    	TDebug.println(0,buf.toString());
    	messages.setText(buf.toString()); 	
    	clearText = true;
    }
    
    void scoreMessage(String str){
    	StringBuffer buf = new StringBuffer(str);
    //	buf.append("\nYou have a total of " + numMoves + " moves!");
    	//buf.append("\nCharge A: = " + pcA.getCharge() + "\nCharge B: = " + pcB.getCharge());
    	TDebug.println(0,buf.toString());
    	messages.setText(buf.toString()); 	
    	clearText = true;
    }
    
    void errorMessage(String str){
    	StringBuffer buf = new StringBuffer(str);
    //	buf.append("\nYou won with " + numMoves + " points!");
    	TDebug.println(0,buf.toString());
    	messages.setText(buf.toString());
    	clearText = true;
    }

    public void reset() {
		theEngine.requestRefresh();
    }

    public void resetCamera() {
        mViewer.setLookAt(new Point3d(0.0, -0.25, .44), 
        		new Point3d(0., 0.0, 0.), new Vector3d(0., 1., 0.));
    }
    
    Vector3d getPosition(int x, int z){
    	return new Vector3d(sizewall*(x)-nwall*sizewall/2.+ sizewall/2.,
    			sizewall*(z)-nwall*sizewall/2+sizewall/2.,0.); 
    }
    
    double getScaledPotential(int x, int y){
    	double potential = powerPotential(theEngine.getEField().getPotential(getPosition(x,y)));
    	//System.out.println(" minPotential " + minPotential + " maxPotential " + maxPotential + " potential " + potential);
		double result = -1.*scale*nwall*sizewall + 2.*scale*nwall*sizewall*(potential-minPotential)/(maxPotential-minPotential);
	//	TDebug.println(0,"X: " + x + " Y: " + y + " Potential: " + potential + " Result: " + result);
		return result;    	
    }
    
    public void getPotentialMaxMin(){
    	double potential = 0.;
    	minPotential = 100000000.;
    	maxPotential = -100000000.;
     	for (int i = 0; i < nwall; i++) 
        {
        	for (int j = 0; j < nwall; j++) 
        	{         		
        		potential = powerPotential(theEngine.getEField().getPotential(getPosition(i,j)));
        		if ( potential > maxPotential) maxPotential = potential;
        		if ( potential < minPotential) minPotential = potential;
        	    //System.out.println(" i "	+i+" j "	+j+" maxPotential " + maxPotential + " minPotential " + minPotential);
        	} 	
        }

       //System.out.println(" maxPotential " + maxPotential + " minPotential " + minPotential);
	
    }
    
    public double powerPotential(double potential){
       	double signPot = Math.signum(potential);
    	double pot = signPot*Math.pow(Math.abs(potential),potpower);
    	return pot;
    }
    
    public void moveObserver(){
    	Vector3d pos = getPosition(idxX,idxY);
    	theArrow.setPosition(pos);
    	arrowE.setPosition(pos);
    	Vector3d arrowdirection = null;
    	double potential = getScaledPotential(idxX,idxY); 
    	Vector3d posup = new Vector3d(0.,0.,potential);
    	Vector3d postot = new Vector3d();
    	postot.add(pos,posup);
    	// System.out.println("position" + pos);
    	theEngine.requestRefresh();    	
    	// observer.setPosition(pos);
    	arrowdirection = theArrow.getValue();
    	//arrowdirection.normalize();
    //	System.out.println("before set arrowdirection idxX " + idxX + "  idxY " + idxY + " arrowdirection " + arrowdirection);
    	arrowE.setDirection(arrowdirection);
    	arrowdirection = arrowE.getDirection();
     //  	System.out.println("after set arrowdirection idxX "+ idxX + "  idxY " + idxY + " arrowdirection " + arrowdirection);
    	theEngine.requestRefresh();    	
       	arrowdirection = theArrow.getValue();
    	//arrowdirection.normalize();
    //	System.out.println("2 before set arrowdirection idxX " + idxX + "  idxY " + idxY + " arrowdirection " + arrowdirection);
    	arrowE.setDirection(arrowdirection);
    	arrowdirection = arrowE.getDirection();
    //   	System.out.println("2 after set arrowdirection idxX "+ idxX + "  idxY " + idxY + " arrowdirection " + arrowdirection);
   // 	System.out.println(" idxX " + idxX + "  idxY " + idxY + "  x pos " + postot.x + " ypos " + postot.y);
   // 	System.out.println(" sizewall "+sizewall);
        iconobserver.setPosition(postot);
      //  mViewer.setLookAt(new Point3d(postot.x,postot.y,postot.z), 
        	//	new Point3d(0., 0.0, 0.), new Vector3d(0., 1., 0.));
    
    	if(!visited[idxX][idxY]){
    		moveWall(idxX,idxY);
    		visited[idxX][idxY] = true;
    		numMoves++;
    	}
   // 	scoreMessage("Your score is  " + numMoves);
    	theEngine.requestRefresh();    	
    }
    
    void moveWall(int x, int y){
    	
		Vector3d loc =wall[x][y].getPosition();
		loc.z = getScaledPotential(x,y);
		wall[x][y].setPosition(loc);

    }
    
    void checkMove(char code){
    	boolean moved = false;
    	if(clearText){
    		messages.setText("");
    		clearText = false;
    	}
    		
    		TDebug.println(1,"Code: " + code);
    		switch (code){
    		case '1':
    			if(idxX >0 && idxY < max){
    				idxX--;
    				idxY++;
    				moved = true;
    			}
    			break;
    		case '2':
    			if(idxY < max){
    				idxY++;
    				moved = true;
    			}
    			break;
    		case '3':
    			if(idxX < max && idxY < max){
    				idxX++;
    				idxY++;
    				moved = true;
    			}
    			break;
    		
    		case '4':
    			if(idxX > 0){
    				idxX--;
    				moved = true;
    			}
    			break;
    		case '6':
    			if(idxX <max){
    				idxX++;
    				moved = true;
    			}
    			break;
    		
    		
    		case '7':
    			if(idxX >0 && idxY > 0){
    				idxX--;
    				idxY--;
    				moved = true;
    			}
    			break;
    		case '8':
    			if(idxY > 0){
    				idxY--;
    				moved = true;
    			}
    			break;
    		case '9':
    			if(idxX < max && idxY > 0){
    				idxX++;
    				idxY--;
    				moved = true;
    			}
    			break;
    		default:
    			break;
    		}
    		if(moved){
    			moveObserver();
    		//	scoreMessage("Your score is  " + numMoves);
    			
    		}
    	}
    
    void setPosition(){

  //  	optionsGroup.clearSelection();
  //  	messages.setText("");
    	clearText = false;
    	// reset walls
    	 for (int i = 0; i < nwall; i++) 
         {
         	for (int j = 0; j < nwall; j++) 
         	{         		
         		wall[i][j].setPosition(getPosition(i,j));
         		visited[i][j] = true;
         	} 	
         }
    	// pick charges of charges
 			pcB.setRadius(pointChargeRadius);
	 		pcA.setRadius(pointChargeRadius);
    			pcB.setCharge(1.);

    			pcA.setCharge(-1.);
    		
	 		
    	// set positions of charges
 
    		pcA.setPosition(new Vector3d(sizewall/2.,sizewall*initialMult,0.));
    		pcA.setDrawn(true);
    	 	pcB.setPosition(new Vector3d(sizewall/2.,-sizewall*initialMult,0.));
    		pcB.setDrawn(true);
    		

        
    	idxX = initialObserver;
        idxY = initialObserver;
        //refPowerPotential = powerPotential(theEngine.getEField().getPotential(getPosition(idxX,idxY)));
        // find the min and max potential values for this configuration of charges

        getPotentialMaxMin();
        moveObserver();
        numMoves = 0;
    }
    
    public Vector3d zeroset(int rand) {
    	Vector3d result = null;
    	if (rand == 0) result = new Vector3d(-nwall*sizewall/2.+sizewall/2.,-nwall*sizewall/2.+sizewall/2.,0.);
    	if (rand == 1) result = new Vector3d(0.,-nwall*sizewall/2+sizewall/2.,0.);
    	if (rand == 2) result = new Vector3d(0.,0.,0.);
    	if (rand == 3) result = new Vector3d(-nwall*sizewall/2+sizewall/2.,0.,0.);
    	return result;
    }
    	
    void showResults(){
    	pcA.setDrawn(true);
    	pcB.setDrawn(true);
    	getPotentialMaxMin();
    	for (int i = 0; i < nwall; i++) 
        {
        	for (int j = 0; j < nwall; j++) 
        	{         		
        		moveWall(i,j);
        	} 	
        }
    	theEngine.requestRefresh(); 
    //    mDLIC.generatePotentialImage();
    //    theEngine.requestRefresh(); 
    }

    void setMarkers(){
    	double sizeMarker = 1.;
        double radius = widthtotal/10;
        double offset = widthtotal/2.+ radius;
        Vector3d pos1label  = new Vector3d(-offset-sizeMarker/2.,offset,-.8);
		lbl = new SpatialTextLabel("1", pos1label );
		lbl.setBaseScale(sizeMarker);
		lbl.setColor(Color.red);
		addElement(lbl);
		
		Vector3d pos3label  = new Vector3d(offset,offset,-.8);
		lb3 = new SpatialTextLabel("3", pos3label );
		lb3.setBaseScale(sizeMarker);
		lb3.setPositionOffset(new Vector3d(0.05, 0.0, 0.));
		lb3.setRefDirectionOffset(0.0);
		lb3.setUseDirectionOffset(true);
		lb3.setColor(Color.yellow);
		addElement(lb3);
		
        Vector3d pos7label  = new Vector3d(-offset-sizeMarker/2.,-offset-sizeMarker/2.,-.8);
		lb7 = new SpatialTextLabel("7", pos7label );
		lb7.setBaseScale(sizeMarker);
		lb7.setPositionOffset(new Vector3d(0.05, 0.0, 0.));
		lb7.setRefDirectionOffset(0.0);
		lb7.setUseDirectionOffset(true);
		lb7.setColor(Color.green);
		addElement(lb7);
		
		Vector3d pos9label  = new Vector3d(offset,-offset-sizeMarker/2.,-.8);
		lb9 = new SpatialTextLabel("9", pos9label );
		lb9.setBaseScale(sizeMarker);
		lb9.setPositionOffset(new Vector3d(0.05, 0.0, 0.));
		lb9.setRefDirectionOffset(0.0);
		lb9.setUseDirectionOffset(true);
		lb9.setColor(Color.blue);
		addElement(lb9);
		
        Vector3d pos4label  = new Vector3d(-offset-sizeMarker/2.,0.,-.8);
		lb4 = new SpatialTextLabel("4", pos4label );
		lb4.setBaseScale(sizeMarker);
		lb4.setPositionOffset(new Vector3d(0.05, 0.0, 0.));
		lb4.setRefDirectionOffset(0.0);
		lb4.setUseDirectionOffset(true);
		lb4.setColor(Color.gray);
		addElement(lb4);
		
		Vector3d pos6label  = new Vector3d(offset,0.,-.8);
		lb6 = new SpatialTextLabel("6", pos6label );
		lb6.setBaseScale(sizeMarker);
		lb6.setPositionOffset(new Vector3d(0.05, 0.0, 0.));
		lb6.setRefDirectionOffset(0.0);
		lb6.setUseDirectionOffset(true);
		lb6.setColor(Color.gray);
		addElement(lb6);
		
        Vector3d pos2label  = new Vector3d(0.,offset,-.8);
		lb2 = new SpatialTextLabel("2", pos2label );
		lb2.setBaseScale(sizeMarker);
		lb2.setPositionOffset(new Vector3d(0.05, 0.0, 0.));
		lb2.setRefDirectionOffset(0.0);
		lb2.setUseDirectionOffset(true);
		lb2.setColor(Color.gray);
		addElement(lb2);
		
		Vector3d pos8label  = new Vector3d(0.,-offset-sizeMarker/2.,-.8);
		lb8 = new SpatialTextLabel("8", pos8label );
		lb8.setBaseScale(sizeMarker);
		lb8.setPositionOffset(new Vector3d(0.05, 0.0, 0.));
		lb8.setRefDirectionOffset(0.0);
		lb8.setUseDirectionOffset(true);
		lb8.setColor(Color.gray);
		addElement(lb8);
		
     // add four lines to outline the base square
        
        Line one = new Line(new Vector3d(-widthtotal/2.,-widthtotal/2., 0.), new Vector3d(-widthtotal/2.,widthtotal/2., 0.));
        one.setColor(Color.white);
        addElement(one);
        Line two = new Line(new Vector3d(-widthtotal/2.,-widthtotal/2., 0.), new Vector3d(widthtotal/2.,-widthtotal/2., 0.));
        two.setColor(Color.white);
        addElement(two);
        Line three = new Line(new Vector3d(widthtotal/2.,widthtotal/2., 0.), new Vector3d(-widthtotal/2.,widthtotal/2., 0.));
        three.setColor(Color.white);
        addElement(three);
        Line four = new Line(new Vector3d(widthtotal/2.,widthtotal/2., 0.), new Vector3d(widthtotal/2.,-widthtotal/2., 0.));
        four.setColor(Color.white);
        addElement(four);

    
    }
String getScore(){
	StringBuffer buf = new StringBuffer();

	showResults();
//	buf.append("There were " +chargeList.size() + " charges.");
//	buf.append("\nPositive: " + pos +" Negitive: " + neg);
	buf.append("\nYou moved  total of " + numMoves + " times.");
	messages.setText(buf.toString());
	messages.setVisible(true);
	return buf.toString();
}

    
   
    			
	
    	
    

    
    
}

