/* $Id: TeachSpinLVQ.java,v 1.1 2006/08/06 22:49:00 cshubert Exp $ */

/**
 * A demonstration integration of SimLab and a network connection 
 * to a LabView experiment. This version uses a FIFO que to buffer 
 * network TCP messages from the labView application.
 *
 * @author Philip Bailey - Center for Educational Computing Initiatives / MIT
 * @version $Revision: 1.1 $
 */

package apps.physics.ilab;

import isocket.*;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import teal.app.*;
import teal.framework.TealAction;
import teal.ui.swing.LookAndFeelTweaks;
import teal.util.*;

public class TeachSpinLVQ extends TeachSpinBase implements DataReadyListener {

    private static final long serialVersionUID = 3256727268750668336L;
    // Connection to LabView
    String lvHost = "ceci-16.mit.edu";
    //String lvHost = "localhost";
    int basePort = 43970;
    IDataSocketC dataSocket = null;
    TealAction socketState = null;
    boolean socketsRunning = false;
    FIFO que = null;
    BucketB bb = null;

    public TeachSpinLVQ() {
        this("localhost", 43970, 2, 160, -1, DEFAULT);
    }

    public TeachSpinLVQ(String hostname, int port, int numLines, double coilsPerRing, int debugLevel, int lod) {

        super(numLines, coilsPerRing, debugLevel, lod);
        que = new FIFO(16, 8);
        TDebug.setGlobalLevel(1);
        TDebug.println(-1, "TeachSpinLV LabView host: " + hostname + ":" + port);
        title = "SimLab Teach Spin LVQ";
        lvHost = hostname;
        basePort = port;
        mSMC.setVisible(false);
        //m1.setMu(magMu * 0.1);
        // m1.setDrawn(true);  
        addActions();

        mSMC.init();
        theModel.requestRefresh();
        mGUI.refresh();

    }

    private synchronized void startSockets() {
        dataSocket = new IDataSocketC(lvHost, basePort);
        dataSocket.addDataReadyListener(this);
        dataSocket.start();
        bb = new BucketB();
        bb.start();
        TDebug.println(1, "dataSocket Priority: " + dataSocket.getPriority());
        //dataSocket.setPriority(10);
        //TDebug.println("dataSocket Priority: " +dataSocket.getPriority());
        if (dataSocket.isConnected()) TDebug.println(0, "Data socket started: " + dataSocket.getConnectedPort());

        if (dataSocket.isConnected()) {
            socketState.setName("Disconnect");
            socketState.setActionCommand("Disconnect");
            socketsRunning = true;
            TDebug.println(0, "Sockets started");
        } else {
            TDebug.println(-1, "Error connecting to " + lvHost + ":" + basePort);
        }

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
            bb.doContinue = false;
            bb = null;
        }
        //socketState.putValue("Name","Connect");
        socketState.setName("Connect");
        socketState.setActionCommand("Connect");
        socketsRunning = false;
        TDebug.println(0, "Sockets shutdown");
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
        theModel.requestRefresh();
        //startSockets();
    }

    public void dataReady(DataReadyEvent dbe) {
        String tag = null;
        Double val;
        //ISocket source = (ISocket) dbe.getSource();
        //TDebug.print(-1, "Port: " + source.getConnectedPort() + "\t");
        TDebug.println(1, "DBE created at: " + dbe.time);
        ArrayList data = (ArrayList) dbe.getData();
        if (data == null) return;
        boolean dataRecord = false;
        int count = data.size();
        double py = 0.;
        double tmpCurrent = 0.;
        long time = 0L;

        for (int i = 0; i < count; i++) {
            tag = (String) data.get(i++);
            val = (Double) data.get(i);
            TDebug.println(1, "\t'" + tag + "' = " + val);

            if (tag.compareToIgnoreCase("current") == 0) {
                dataRecord = true;
                tmpCurrent = -val.doubleValue();
            } else if (tag.compareToIgnoreCase("posy") == 0) {
                py = val.doubleValue() / 1000.;
            } else if (tag.compareToIgnoreCase("time") == 0) {
                time = val.longValue();
            } else if (tag.compareToIgnoreCase("same") == 0) {
                setSame(val.doubleValue() > 2);
            } else if (tag.compareToIgnoreCase("opposite") == 0) {
                setOpposite(val.doubleValue() > 2);
            } else if (tag.compareToIgnoreCase("top") == 0) {
                setTopOnly(val.doubleValue() > 2);
            }
        }
        if (dataRecord) {
            DataRec rec = new DataRec(time, py, tmpCurrent);
            que.add(rec);
            TDebug.println(1, "Que size = " + que.size());
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
        TDebug.println(1, "ActionComamand: " + e.getActionCommand());
        TDebug.println(1, "Action: " + e.paramString());
        TDebug.println(1, "Source: " + e.getSource());
        TDebug.println(1, "socketState: " + socketState);

        // Need to overload this action to deal with the data stream
        if (e.getActionCommand().compareToIgnoreCase(TealSimApp.DLIC_B) == 0) {
            try {
                stopSockets();
            } catch (Throwable th) {
            }
            //processData = false;
            super.actionPerformed(e);

        } else if (e.getActionCommand().compareToIgnoreCase("Connect") == 0) {
            TDebug.println(1, "Socket Command: " + socketState.getActionCommand());

            if (socketState.getActionCommand().compareToIgnoreCase("Connect") == 0) {
                try {
                    startSockets();
                } catch (Throwable ioe) {
                }
            } else if (socketState.getActionCommand().compareToIgnoreCase("Disconnect") == 0) {
                try {
                    stopSockets();
                } catch (Throwable ioe) {
                    TDebug.println("Error Stopping sockets: " + ioe.getMessage());
                }
            }
        }

        else {
            super.actionPerformed(e);
        }
    }

    public synchronized void dispose()

    {
        try {
            stopSockets();
        } catch (Throwable t) {
            TDebug.printThrown(-1, t);
        }
        super.dispose();
    }

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
            //            final BasicApp theApp = new TeachSpinLVQ(host,port,lines,numCoils,dbLvl,lod);
            //            BasicApp.initApplication();
            //             //theApp.addDefaultActions();
            //            theApp.setSize(1024,768);
            //            TDebug.setGlobalLevel(0);
            //            theApp.start();

            LookAndFeelTweaks.setLookAndFeel();
            TealBasicApp theApp = new TeachSpinLVQ(host, port, lines, numCoils, dbLvl, lod);
            theApp.show();
        } catch (Throwable thr) {
            TDebug.printThrown(0, thr);
        }
    }

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
        boolean doContinue = true;

        DataRec curRec = null;

        public void run() {

            while (doContinue) {
                try {
                    if (que.hasNext()) {
                        curRec = (DataRec) que.next();
                        waitTime = curRec.time - (lastTime + 10L);
                        if (waitTime > 0) {
                            sleep(waitTime);
                        }
                        m1.setY(curRec.posy);
                        setCurrent(curRec.value);
                        lastTime = curRec.time;
                        TDebug.println(1, "Frame at: " + System.currentTimeMillis());

                    } else {
                        sleep(cycleTime);
                    }
                } catch (InterruptedException ie) {
                    TDebug.println("run iterrupted");
                }
            }
        }
    }

}
