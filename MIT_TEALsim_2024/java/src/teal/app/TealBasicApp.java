/**
 * Teal/Studio Physics Project, Massachusetts Institute of Technology.
 *
 */

package teal.app;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import teal.browser.Browser;
import teal.core.AbstractElement;
import teal.core.Route;
import teal.core.HasID;
import teal.core.TElement;
import teal.framework.HasFramework;
import teal.framework.MenuBar;
import teal.framework.TAbstractMenuBar;
import teal.framework.TFramework;
import teal.framework.TGui;
import teal.framework.TStatusBar;
import teal.framework.TToolBar;
import teal.framework.TealAction;
import teal.ui.UIUtilities;
import teal.ui.swing.LookAndFeelTweaks;
import teal.ui.swing.StatusBar;
import teal.util.TDebug;

/**
 * Minimal base class for TEalSimApp. Do not use.
 * 
 * @author pbailey
 *
 * @deprecated
 */
public class TealBasicApp extends JFrame implements ActionListener, TFramework, TElement {

    private static final long serialVersionUID = 3906932261751109174L;

    protected TGui mGUI = null;
    protected static TealBasicApp theInstance;
    protected String id = "TEALapp";
    protected PropertyChangeSupport propSupport;
    protected String title = "Teal Application Framework";
    protected MenuBar mMenuBar;
    protected JFrame appFrame = null;
    protected JFileChooser fc = null;
    protected File curDir = null;
    protected boolean useTealLnF = true;
    protected Hashtable mElements;
    protected TStatusBar mStatusBar;
    protected TToolBar mToolBar;

    public TealBasicApp() {
        super();

        TDebug.println(1, "TealBasicApp Constructor:");

        mElements = new Hashtable();
        buildUI();
    }

    public static TealBasicApp getInstance() {
        return theInstance;
    }

    private void buildUI() {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        //JFrame.setDefaultLookAndFeelDecorated(true);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        ((JComponent) getContentPane()).setBorder(LookAndFeelTweaks.PANEL_BORDER);
        getContentPane().setLayout(LookAndFeelTweaks.createBorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        mMenuBar = new MenuBar();
        TealAction ta = new TealAction("Exit", this);
        addAction("File", ta);
        setJMenuBar(mMenuBar);

        mToolBar = new TToolBar();
        getContentPane().add(BorderLayout.NORTH, mToolBar);

        // a status bar with two zones, the message zone and a progress
        mStatusBar = new TStatusBar(true);
        getContentPane().add(BorderLayout.SOUTH, mStatusBar);

        //		TStatusBar statusbar = new TStatusBar();
        //		statusbar.addZone("message", new JLabel(" "), "*");
        //		statusbar.addZone("progress", new JProgressBar(), "150");
        //		getContentPane().add("South", statusbar);

        // set a text in the message zone
        //		((JLabel) statusbar.getZone("message")).setText(RESOURCE.getString("ready"));

        // put the progress in indeterminate state
        //		((JProgressBar) statusbar.getZone("progress")).setIndeterminate(true);

        initGUI();
        setSize(1024, 768);
        UIUtilities.centerOnScreen(this);
    }

    public void initGUI() {
        TDebug.println(1, "TealBasicApp InitGUI:");
        setGui(new SimGUI());
    }

    public void setGui(TGui gui) {
        TDebug.println(1, "TealBasicApp setGUI: " + gui);
        mGUI = gui;
        if (mGUI != null) {
            getContentPane().remove(mGUI.getPanel());
            getContentPane().add(BorderLayout.CENTER, mGUI.getPanel());
            mGUI.setFramework(this);
        }
        getContentPane().invalidate();
        getContentPane().validate();
        getContentPane().repaint();
    }

    public TGui getGui() {
        return mGUI;
    }

    public TAbstractMenuBar getTMenuBar() {
        return mMenuBar;
    }

    public TToolBar getTToolBar() {
        return null;
    }

 
    public void setTitle(String t) {
        title = t;
    }

    public String getTitle() {
        return title;
    }

    public Cursor getAppCursor() {
        Cursor c = getCursor();
        return c;
    }

    public void setAppCursor(Cursor cur) {
        setCursor(cur);
    }

    public JFileChooser getFileChooser() {
        if (fc == null) fc = new JFileChooser();
        return fc;
    }

    public void load() {
        File file = null;
        if (fc == null) fc = new JFileChooser();
        if (curDir != null) fc.setCurrentDirectory(curDir);
        int status = fc.showOpenDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {
            curDir = fc.getCurrentDirectory();

            file = fc.getSelectedFile();
            load(file);
        }
    }

    public void load(File file) {
        try {
            load(new FileInputStream(file));
        } catch (FileNotFoundException fnf) {
            TDebug.println(0, "File not found: " + file);
        }
    }

    public void load(InputStream input) {
        boolean hasNext = true;
        ArrayList elements = new ArrayList();
        removeElements();
        try {
            XMLDecoder e = new XMLDecoder(new BufferedInputStream(input));
            while (hasNext) {
                try {
                    Object obj = e.readObject();
                    TDebug.println(0, "Loaded: " + obj);
                    elements.add(obj);
                }
                //catch(Exception ie){
                //     TDebug.println("\tError: " + ie.getMessage);
                // }
                catch (ArrayIndexOutOfBoundsException ore) {
                    hasNext = false;
                }
            }
            e.close();
        } catch (Exception fnf) {
            TDebug.printThrown(0, fnf, " Trying to load input");
        }
        addElements(elements);
    }

    public void save() {

        File curDir = null;
        File file = null;

        if (fc == null) fc = new JFileChooser();
        if (curDir != null) fc.setCurrentDirectory(curDir);
        int status = fc.showSaveDialog(this);
        if (status == JFileChooser.APPROVE_OPTION) {

            curDir = fc.getCurrentDirectory();
            try {
                file = fc.getSelectedFile();

                XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(file)));
                ArrayList elements = new ArrayList(mElements.values());
                Iterator ie = elements.iterator();
                Object obj = null;
                while (ie.hasNext()) {
                    //try
                    //{
                    obj = ie.next();
                    TDebug.println(0, "Saving: " + obj);
                    e.writeObject(obj);
                    // } 
                    /*       
                     catch (InvocationTargetException ite) {
                     TDebug.printThrown(0,ite, "\tInvocationTargetException: " + obj);
                     }
                     catch (NotSerializableException nse) {
                     TDebug.printThrown(0,nse, "\tNotSerializable: " + obj);
                     }
                     */
                }
                e.close();

            }

            catch (IOException fnf) {
                TDebug.printThrown(0, fnf, " Trying to save file: " + file);
            }
        }
    }

    public TElement fetchTElement(String id) {
        TElement elm = (TElement) mElements.get(id);
        return elm;
    }

    public void addElement(Object element) throws IllegalArgumentException {

        addElement(element, true);
    }

    public void addElement(Object element, boolean addToList) throws IllegalArgumentException {
        //TDebug.println("BasicApp addElement: "+ element);
        if (element instanceof HasID) {
            addTElement((HasID) element, addToList);
        } else if (element instanceof Action) {
            addAction((Action) element);
        } else if (element instanceof Component) {
            addComponent((Component) element);
        } else {
            throw new IllegalArgumentException("Error: element type of object " + element + " is not supported");
        }
    }

    public void removeElements() {
        Collection elements = new ArrayList(mElements.values());
        removeElements(elements);
    }

    public void removeElements(Collection elements) {
        Iterator it = elements.iterator();
        while (it.hasNext()) {
            removeElement(it.next());
        }
    }

    public void removeElement(Object element) {
        if (element instanceof TElement) {
            removeTElement((TElement) element);
        } else if (element instanceof Action) {
            removeAction((Action) element);
        } else if (element instanceof Component) {
            removeComponent((Component) element);
        }
    }

    public void addElements(Collection elements) throws IllegalArgumentException {
        Iterator it = elements.iterator();
        while (it.hasNext()) {
            addElement(it.next(), true);
        }
    }

    public void addAction(Action elm) {
        if (elm instanceof Component) {
            mGUI.addComponent((Component) elm);
        }
        if (elm instanceof Action) {
            addAction((Action) elm);
        }
    }

    public void removeAction(Action a) {
    }

    public void addAction(String target, Action ac) {
        TDebug.println(1, "AddAction:");
        if (mMenuBar != null) TDebug.println(1, "AddAction:");
        mMenuBar.addAction(target, ac);
    }

    public void removeAction(String target, Action ac) {
        if (mMenuBar != null) mMenuBar.removeAction(target, ac);
    }

    public void addTElement(HasID elm) {
        addTElement(elm, true);
    }

    public void addTElement(HasID elm, boolean addToList) {
        AbstractElement.checkID(elm);
        TDebug.println(2, "TealBasicApp addTElement: " + elm);
        if (addToList) mElements.put(elm.getID(), elm);
        if (elm instanceof HasFramework) {
            ((HasFramework)elm).setFramework(this);
        }
        if (elm instanceof Component) {
            mGUI.addComponent((Component) elm);
        }
        if (elm instanceof Action) {
            addAction((Action) elm);
        }
    }

    public void removeTElement(HasID elm) {
        if (elm instanceof Component) {
            mGUI.removeComponent((Component) elm);
        }
        if (elm instanceof Action) {
            removeAction((Action) elm);
        }
    }

    public void addComponent(Component elm) {
        mGUI.addComponent(elm);
    }

    public void removeComponent(Component elm) {
        mGUI.removeComponent(elm);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareToIgnoreCase("Exit") == 0) {
            dispose();
            System.exit(0);
        } else if (e.getActionCommand().compareToIgnoreCase("Save") == 0) {
            save();
        }
    }

    public String toString() {
        return (id);
    }

    public String getID() {
        return this.id;
    }

    public void setID(String id) {
        String temp = this.id;
        this.id = new String(id);
        firePropertyChange("ID", temp, id);
    }

    public Object getProperty(String name)
    //throws NoSuchMethodException
    {
        TDebug.println(2, " In getProperty() " + getID() + ": " + name);
        Object obj = null;
        try {
            PropertyDescriptor pd = new PropertyDescriptor(name, this.getClass());
            Method theMethod = pd.getReadMethod();
            if (theMethod != null) {
                obj = theMethod.invoke(this, null);
            } else {
                TDebug.println(2, this + ": Getter method for " + name + " not found");
            }
        } catch (IntrospectionException ie) {
            TDebug.println(2, this + " IntrospectionEx: " + ie.getMessage());
        } catch (InvocationTargetException cnfe) {
            TDebug.println(2, cnfe.getMessage());
        } catch (IllegalAccessException ille) {
            TDebug.println(2, ille.getMessage());
        }

        return obj;
    }

    public boolean setProperty(String name, Object prop)
    //throws NoSuchMethodException
    {
        TDebug.println(2, " In setProperty() " + getID() + ": " + name + " = " + prop.toString());
        boolean status = false;
        try {
            PropertyDescriptor pd = new PropertyDescriptor(name, this.getClass());
            Method theMethod = pd.getWriteMethod();
            if (theMethod != null) {
                Object param[] = { prop };
                theMethod.invoke(this, param);
                status = true;
            } else {
                TDebug.println(2, this + ": Setter method for " + name + " not found");
            }
        } catch (IntrospectionException ie) {
            TDebug.println(2, this + " IntrospectionEx: " + ie.getMessage());
        } catch (InvocationTargetException cnfe) {
            TDebug.println(2, cnfe.getMessage());
        } catch (IllegalAccessException ille) {
            TDebug.println(2, ille.getMessage());
        }

        return status;
    }

    public Browser openBrowser(String str) {
        Browser browser = new Browser(str);
        browser.setSize(550, 400);
        browser.setVisible(true);
        return browser;
    }

    /**
     * Check if there are any listeners for a specific property.
     */
    public boolean hasPropertyChangeListeners(String propertyName) {
        return propSupport.hasListeners(propertyName);
    }

    /** the following methods wrap access to the PropertyChangeSupport member */
    public void propertyChange(PropertyChangeEvent pce) {
        if (pce != null) {
            TDebug.println(3, getID() + ": in propertyChange trying to set " + pce.getPropertyName());
            setProperty(pce.getPropertyName(), pce.getNewValue());
        }
    }

    /**
     * Add a PropertyChangeListener to the listener list.
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    /**
     * Add a PropertyChangeListener for a specific property.
     */
    public synchronized void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        TDebug.println(3, "addingPropertyChangeListener for " + propertyName);
        propSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Fire an existing PropertyChangeEvent to any registered listeners.
     */
    public synchronized void firePropertyChange(PropertyChangeEvent evt) {
        if (evt != null) {
            TDebug.println(3, "Fire propertyChange:" + id + "  " + evt.getPropertyName());
            propSupport.firePropertyChange(evt);
        }
    }

    /**
     * Check if there are any listeners for a specific property.
     */
    public boolean hasListeners(String propertyName) {
        return propSupport.hasListeners(propertyName);
    }

    /**
     * Remove a PropertyChangeListener from the listener list.
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }

    /**
     * Remove a PropertyChangeListener from the listener list.
     */
    public synchronized void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(propertyName, listener);
    }
    
      /** 
     * Establish a route from this object's source attribute and the targeted 
     * attribute of another TElement. 
     **/
    public void addRoute(Route r)
    {
        addPropertyChangeListener(r.getSrcProperty(),r);
    }
    
    /** 
     * Establish a route from this object's source attribute and the targeted 
     * attribute of another TElement. 
     **/
    public void addRoute(String attribute, TElement target, String targetName)
    {
        Route r = new Route(attribute,target,targetName);
        addRoute(r);
    }
 
     /** 
     * Remove a route from this object's source attribute and the target's output attribute. 
     **/
    public void removeRoute(String attribute, TElement listener, String targetName)
    {
        PropertyChangeListener [] listeners = propSupport.getPropertyChangeListeners(attribute);
        if (listeners.length > 0)
        {
            Route r = new Route(attribute,listener,targetName);
            for(int i=0; i< listeners.length;i++)
            {
                if(r.equals(listeners[i])){
                    propSupport.removePropertyChangeListener(listeners[i]);
                }
            }
        }
    }
    

    public static void main(String[] args) throws Exception {
        //LookAndFeelTweaks.setLookAndFeel();
        TealBasicApp main = new TealBasicApp();
        main.show();
    }

	/* (non-Javadoc)
	 * @see teal.core.TElementManager#getTElementByID(java.lang.String)
	 */
	public HasID getTElementByID(String id) {
		if (mElements.containsKey(id)) {
			return (HasID)mElements.get(id);
		}
		return null;
	}

	public TStatusBar getStatusBar() {
		// TODO Auto-generated method stub
		return null;
	}

	public JFrame getTheWindow() {
		// TODO Auto-generated method stub
		return null;
	}
}