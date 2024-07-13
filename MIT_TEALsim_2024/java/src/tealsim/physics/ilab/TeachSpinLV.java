/* $Id: TeachSpinLV.java,v 1.6 2007/11/02 23:01:00 pbailey Exp $ */

/**
 * A demonstration integration of SimLab and a network connection to a LabView
 * experiment. This version uses a FIFO que to buffer network TCP messages from
 * the labView application.
 * 
 * @author Philip Bailey - Center for Educational Computing Initiatives / MIT
 * @version $Revision: 1.6 $
 */

package tealsim.physics.ilab;

import isocket.IDataSocketC;
import isocket.DataReadyListener;
import isocket.DataReadyEvent;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.media.j3d.BoundingSphere;
import javax.vecmath.Point3d;

import teal.app.*;
import teal.field.Field;
import teal.framework.TealAction;
import teal.sim.control.VisualizationControl;
import teal.sim.spatial.FieldDirectionGrid;
import teal.util.*;
import teal.visualization.dlic.DLIC;
import tealsim.physics.em.TeachSpinBase;



public class TeachSpinLV extends TeachSpinBase implements DataReadyListener {

	private static final long serialVersionUID = 3904682678141007920L;
 
	final static int NOT_SET = 0;
	final static int COIL_STATE = 1;
	final static int SWITCH_STATE = 2;
	final static int CONNECTION_STATE = 4;
	final static int OPPOSITE_SW = 8;
	final static int SAME_SW = 16;
	final static int TOP_SW = 32;

	// Connection to LabView

	String lvHost = "ni-ilabs.mit.edu";
	//String lvHost = "localhost";
	int basePort = 43970;
	
	IDataSocketC dataSocket = null;
	TealAction socketState = null;
	boolean socketsRunning = false;
	FIFO que = null;
	BucketB bb = null;
	boolean cacheMode = false;
	private FieldDirectionGrid fv;
	
	public TeachSpinLV() {
		this("localhost", 43970, 2, 160, -1, MODEL);
	}

	public TeachSpinLV(String hostname, int port, int numLines, double coilsPerRing, int debugLevel, int lod) {

		super(numLines, coilsPerRing, debugLevel, lod);
		que = new FIFO(16, 8);
		TDebug.setGlobalLevel(0);
		TDebug.println(-1, "TeachSpinLV LabView host: " + hostname + ":" + port);
		BoundingSphere bs = new BoundingSphere(new Point3d(0., 0, 0.), 0.400);
		mViewer.setBoundingArea(bs);
		title = "SimLab Teach Spin LabView";
		lvHost = hostname;
		basePort = port;
		mSEC.setVisible(false);
		
		fv = new FieldDirectionGrid();
		fv.setType(Field.B_FIELD);
		fv.setDrawn(false);
		
		VisualizationControl vizPanel = new VisualizationControl();
		vizPanel.setFieldConvolution(mDLIC);
		vizPanel.setConvolutionModes(DLIC.DLIC_FLAG_B | DLIC.DLIC_FLAG_BP);
		vizPanel.setFieldLineManager(fmanager);
		vizPanel.setFieldVisGrid(fv);
		vizPanel.setShowFV(false);
		addElement(vizPanel);
		
		addActions();

		mSEC.init();
		theEngine.requestRefresh();
		theGUI.refresh();

	}
	public String getHost(){
		return lvHost;
	}
	
	
	public void setHost(String host){
		lvHost = host;
	}
	public int getPort(){
		return basePort;
	}
	public void setPort(int port){
		basePort = port;
	}
	public double getNumCoils(){
		return numCoils;
	}
	public void setNumCoils(double num){
		numCoils = num;
	}

	public void setState(int flags) {

		switch (flags) {
			case 0 :
				TDebug.println(0, "No Switches are set!");
				mFramework.getStatusBar().setText("No Switches are set!", false);
				break;
			case OPPOSITE_SW :
				setOpposite(true);
				break;
			case SAME_SW :
				setSame(true);
				break;
			case TOP_SW :
				setTopOnly(true);
				break;
			case OPPOSITE_SW | SAME_SW :
				TDebug.println(0, "Switch Error: opposite & same are set!");
				mFramework.getStatusBar().setText("Switch Error: opposite & same are set!", false);
				break;
			case OPPOSITE_SW | TOP_SW :
				TDebug.println(0, "Switch Error: opposite & topOnly are set!");
				mFramework.getStatusBar().setText("Switch Error: opposite & topOnly are set!", false);
				break;
			case SAME_SW | TOP_SW :
				TDebug.println(0, "Switch Error: Same & topOnly are set!");
				mFramework.getStatusBar().setText("Switch Error: Same & topOnly are set!", false);
				break;
			case OPPOSITE_SW | SAME_SW | TOP_SW :
				TDebug.println(0, "Switch Error: All are set!");
				mFramework.getStatusBar().setText("Switch Error: All are set!", false);
				break;
			default :
				break;
		}
	}

	private synchronized void startSockets() {
		dataSocket = new IDataSocketC(lvHost, basePort);
		dataSocket.addDataReadyListener(this);
		dataSocket.start();
		bb = new BucketB(cacheMode);
		bb.start();
		TDebug.println(1, "dataSocket Priority: " + dataSocket.getPriority());

		//dataSocket.setPriority(10);
		//TDebug.println("dataSocket Priority: " +dataSocket.getPriority());
		//if(dataSocket.isConnected())
		//   TDebug.println(0, "Data socket started: " +
		// dataSocket.getConnectedPort());
		if (dataSocket.isConnected()) {
			socketState.setName("Disconnect");
			socketState.setActionCommand("Disconnect");
			socketsRunning = true;
			TDebug.println(0, "Sockets started");
			mFramework.getStatusBar().setText("Connection started", true);
		} else {
			TDebug.println(-1, "Error connecting to " + lvHost + ":" + basePort);
			mFramework.getStatusBar().setText("Error connecting to " + lvHost, false);
		}
	}

	private synchronized void disconnect() throws Throwable {
		if (dataSocket != null) {
			if (dataSocket.isConnected()) {
				dataSocket.write('z');
			}
		}
		TDebug.println(0, "Disconnect requested");
		mFramework.getStatusBar().setText("Disconnect requested", true);
	}

	private synchronized void stopSockets() throws Throwable {
		if (dataSocket != null) {
			dataSocket.removeDataReadyListener(this);
			if (dataSocket.isConnected()) {
				dataSocket.write('z');
				dataSocket.shutdown();
			}
			dataSocket = null;
		}
		if (bb != null) {
			//bb.doContinue = false;
			bb = null;
		}
		
		//socketState.putValue("Name","Connect");
		socketState.setName("Connect");
		socketState.setActionCommand("Connect");
		socketsRunning = false;
		TDebug.println(0, "Connection shutdown");
		mFramework.getStatusBar().setText("Connection shutdown", true);
	}

	private synchronized void disconnectSockets() throws Throwable {
		if (dataSocket != null) {
			dataSocket.removeDataReadyListener(this);
			if (dataSocket.isConnected()) {
				dataSocket.write('z');
				dataSocket.shutdown();
			}
			dataSocket = null;
		}

		if (bb != null) {
		    //bb.doContinue = false;
			bb = null;
		}
		//socketState.putValue("Name","Connect");
		socketState.setName("Connect");
		socketState.setActionCommand("Connect");
		socketsRunning = false;
		TDebug.println(0, "Sockets shutdown");
		mFramework.getStatusBar().setText("Connection shutdown", true);
	}



	public void reset() {
		try {
			stopSockets();
		} catch (Throwable th) {
			TDebug.printThrown(-1, th, "Error on stopSockets()");
		}
		m1.setPosition(magPos);
		mViewer.displayBounds();
		mDLIC.setVisible(false);
		theEngine.requestRefresh();
	}

	public void dataReady(DataReadyEvent dbe) {
		int dataType = NOT_SET;
		String tag = null;
		Double val;
		//ISocket source = (ISocket) dbe.getSource();
		//TDebug.print(-1, "Port: " + source.getConnectedPort() + "\t");
		TDebug.println(1, "DBE created at: " + dbe.time);
		ArrayList data = (ArrayList) dbe.getData();
		if (data == null)
			return;
		int count = data.size();
		double py = 0.;
		double tmpCurrent = 0.;
		long time = 0L;
		int swState = 0;
		boolean connected = false;
		for (int i = 0; i < count; i++) {
			tag = (String) data.get(i++);
			val = (Double) data.get(i);
			TDebug.println(1, "\t'" + tag + "' = " + val);

			if (tag.compareToIgnoreCase("current") == 0) {
				dataType = COIL_STATE;
				tmpCurrent = -val.doubleValue();
			} else if (tag.compareToIgnoreCase("posy") == 0) {
				py = val.doubleValue() / 1000.;
			} else if (tag.compareToIgnoreCase("time") == 0) {
				time = val.longValue();
			} else if (tag.compareToIgnoreCase("same") == 0) {
				dataType = SWITCH_STATE;
				if (val.doubleValue() > 2)
					swState |= SAME_SW;
			} else if (tag.compareToIgnoreCase("opposite") == 0) {
				if (val.doubleValue() > 2)
					swState |= OPPOSITE_SW;
			} else if (tag.compareToIgnoreCase("top") == 0) {
				if (val.doubleValue() > 2)
					swState |= TOP_SW;
			} else if (tag.compareToIgnoreCase("status") == 0) {
				dataType = CONNECTION_STATE;
				connected = (val.doubleValue() > 2);
			}
		}
		if (dataType == COIL_STATE) {
			DataRec rec = new DataRec(time, py, tmpCurrent);
			que.add(rec);
			TDebug.println(1, "Que size = " + que.size());
		} else if (dataType == SWITCH_STATE) {
			setState(swState);
		} else if (dataType == CONNECTION_STATE) {
			if (!connected) {
				try {
					stopSockets();
				} 
				catch (Throwable t) {
				}
			}
		}
	}

	void addActions() {
		// TAction a = null;
		//System.out.println("adding the action");
		socketState = new TealAction("Connect", this);
		addAction("Actions", socketState);
		//super.addActions();
	}

	public void actionPerformed(ActionEvent e) {
		TDebug.println(3, "ActionComamand: " + e.getActionCommand());
		TDebug.println(3, "Action: " + e.paramString());
		TDebug.println(3, "Source: " + e.getSource());
		TDebug.println(3, "socketState: " + socketState);

		// Need to overload this action to deal with the data stream
		if (e.getActionCommand().compareToIgnoreCase(DLIC.DLIC_B) == 0) {
			try {
                bb.setContinue(false);
				stopSockets();               
			} catch (Throwable th) {

			}
			//processData = false;
			super.actionPerformed(e);
		}else if (e.getActionCommand().compareToIgnoreCase(DLIC.DLIC_BP) == 0) {
			try {
                bb.setContinue(false);
				stopSockets();
			} 
			catch (Throwable th) {
			}
			//processData = false;
			super.actionPerformed(e);
		} else if (e.getActionCommand().compareToIgnoreCase("Connect") == 0) {
			TDebug.println(1, "Socket Command: " + socketState.getActionCommand());
			if (socketState.getActionCommand().compareToIgnoreCase("Connect") == 0) {
				try {
					startSockets();
				} 
				catch (Throwable ioe) {
				}
			} else if (socketState.getActionCommand().compareToIgnoreCase("Disconnect") == 0) {
				try {
					disconnectSockets();
				} catch (Throwable ioe) {
					TDebug.println("Error Stopping sockets: " + ioe.getMessage());
				}
			}
		} 
		
		 /*
		else if (e.getActionCommand().compareToIgnoreCase(TOGGLE_LINES) == 0) {
			if (showFieldLines) {
				try {
					mViewer.addDontDraw(Class.forName("teal.sim.spatial.FieldLine"));
				} catch (ClassNotFoundException ce) {
					TDebug.println(0, "Class Not Found: " + ce.getMessage());
				}
				showFieldLines = false;

			} else {
				try {
					mViewer.removeDontDraw(Class.forName("teal.sim.spatial.FieldLine"));

				} catch (ClassNotFoundException ce) {
					TDebug.println(0, ce.getMessage());
				}
				showFieldLines = true;
			}
			//flSlider.setEnabled(showFieldLines);
			theEngine.requestSpatial();
			theEngine.requestRefresh();
		} 
		*/
		else {
			super.actionPerformed(e);
		}
	}

	public synchronized void dispose() {
		try {
			stopSockets();
		} catch (Throwable t) {
			TDebug.printThrown(-1, t);
		}
		super.dispose();
	}
/*
	public static void main(String args[]) {
		String host = "ceci-16.mit.edu";

		int port = 43970;

		int lines = 2;

		double numCoils = 33.;

		int lod = MODEL;

		int dbLvl = -1;

		// need to add real parameter parsing here

		if (args.length >= 1) {

			host = args[0];

		}

		if (args.length >= 2) {

			try {

				port = Integer.parseInt(args[1]);

			} catch (NumberFormatException nfx) {

			}

		}

		if (args.length >= 3) {

			try {

				lines = Integer.parseInt(args[2]);

			} catch (NumberFormatException nfx2) {

			}

		}

		if (args.length >= 4) {

			try {

				numCoils = Double.parseDouble(args[3]);

			} catch (NumberFormatException nfx3) {

			}

		}

		if (args.length >= 5) {

			try {

				dbLvl = Integer.parseInt(args[4]);

			} catch (NumberFormatException nfx4) {

			}

		}

		if (args.length >= 6) {

			try {

				lod = Integer.parseInt(args[5]);

			} catch (NumberFormatException nfx4) {

			}

		}

		try {

			//            final BasicApp theApp = new

			// TeachSpinLV(host,port,lines,numCoils,dbLvl,lod);

			//            BasicApp.initApplication();

			//             //theApp.addDefaultActions();

			//            theApp.setSize(1024,768);

			//            TDebug.setGlobalLevel(0);

			//            theApp.start();

			LookAndFeelTweaks.setLookAndFeel();

			TealBasicApp theApp = new TeachSpinLV(host, port, lines, numCoils, dbLvl, lod);

			theApp.show();

		} catch (Throwable thr) {

			TDebug.printThrown(0, thr);

		}

	}
	*/

	class DataRec {
		long time;
		double posy;
		double value;

		DataRec(long t, double py, double val) {
			time = t;
			posy = py;
			value = val;
		}
	}

	class BucketB extends Thread {
		long waitTime = 0L;
		long cycleTime = 10L;
		long lastTime = Long.MAX_VALUE;
		long frameStart = Long.MAX_VALUE;
		long curTime = 0L;
		boolean doContinue = true;
		DataRec curRec = null;
        boolean cacheMode = false;
		
		BucketB(boolean pruneCache)
		{
			super();
			cacheMode = pruneCache;
		}

        public void setContinue(boolean b)
        {
            doContinue = b;
        }

		public void run()
		{
			while (doContinue)
			{
				try
				{
					if (que.hasNext())
					{
						curTime = System.currentTimeMillis();
						if (!cacheMode)
						{
							curRec = (DataRec) que.next();
							waitTime = curRec.time - (lastTime + 10L);
							if (waitTime > 0)
							{
								sleep(waitTime);
							}
						}
						else
						{
							int count = 0;
							while (que.hasNext())
							{
								curRec = (DataRec) que.next();
								count++;
								waitTime = curRec.time - (lastTime + 10L);
								waitTime -= (curTime - frameStart);
								TDebug.println(1, "WaitTime: "+ waitTime);
								if (waitTime >= 0)
								{
									TDebug.println(1," Count = " + count);
									if(count <= 1)
										sleep(waitTime);
									break;
								}
							}
						}
						// Send a new info block
						frameStart =  curTime;
						m1.setY(curRec.posy);
						setCurrent(curRec.value);
						lastTime = curRec.time;
						TDebug.println(1, "Frame at: " + curTime);
					}
					else
					{
						sleep(cycleTime);
					}
				} catch (InterruptedException ie)
				{
					TDebug.println("run iterrupted");
				}	
			}
		}
	}

}
