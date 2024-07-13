/* $Id: TealDefaultApp.java,v 1.25 2007/08/13 22:05:59 pbailey Exp $ */

/**
 * Provides an implementation that includes a Java 3D viewer and SimGUI. 
 * 
 * @author Philip Bailey - Center for Educational Computing Initiatives / MIT
 * @version $Revision: 1.25 $
 */
package teal.app;

import java.util.Collection;

import teal.core.AbstractElement;
import teal.core.TElement;
import teal.render.TAbstractRendered;
import teal.render.j3d.ViewerJ3D;
import teal.render.viewer.SelectListener;
import teal.render.viewer.SelectManager;
import teal.render.viewer.SelectManagerImpl;
import teal.render.viewer.TViewer3D;
import teal.util.TDebug;

/**
 * 
 * @author pbailey
 * @deprecated
 */
public class TealDefaultApp extends TealBasicApp implements  SelectManager {

    private static final long serialVersionUID = 3762813779385398578L;
    
    protected TViewer3D mViewer;
	protected SelectManager mSelect;

	public TealDefaultApp() {
		super();
		
		TDebug.println(1,"TealDefaultApp Constructor:");
		
        id = "DefaultApp";
        title = "Render Application";
        
		mSelect = new SelectManagerImpl();
		

		mViewer = new ViewerJ3D();
        TDebug.println(1," Viewer = " + mViewer.getClass().getName());
		mViewer.setID("Viewer 3D");
		mViewer.setSelectManager(mSelect);
		mViewer.setVisible(true);
        addElement(mViewer,false);
        
        TDebug.println(1," GUI = " + mGUI.getClass().getName());
	}

    public void initGUI(){
    	TDebug.println(1,"TealDefaultApp InitGUI:");
	    setGui(new SimGUI());
    }

	public void addTElement(TElement elm, boolean addToList) {
        //TDebug.println(1,"DefaultApp addTElement: " + elm);
		AbstractElement.checkID(elm);
		if (elm instanceof TAbstractRendered) {
			mViewer.addDrawable((TAbstractRendered) elm);
		}
		super.addTElement(elm,addToList);
	}

	public void removeTElement(TElement elm) {
		if (elm instanceof TAbstractRendered) {
			if (((TAbstractRendered) elm).isSelected())
				mSelect.removeSelected((TAbstractRendered) elm);
			mViewer.removeDrawable((TAbstractRendered) elm);
		}
		super.removeTElement(elm);
	}

	public void addSelectListener(SelectListener listener) {
		mSelect.addSelectListener(listener);
	}
	public void removeSelectListener(SelectListener listener) {
		mSelect.removeSelectListener(listener);
	}
	
	public void addSelected(TAbstractRendered obj, boolean clear) {
		mSelect.addSelected(obj, clear);
	}
	public void removeSelected(TAbstractRendered obj) {
		mSelect.removeSelected(obj);
	}

	public void clearSelected() {

		mSelect.clearSelected();

	}

	public int getNumberSelected() {
		return mSelect.getNumberSelected();
	}

	public Collection getSelected() {
		return mSelect.getSelected();
	}

	
	public static void main(String args[]) {
		//LookAndFeelTweaks.setLookAndFeel();
		TealDefaultApp theApp = new TealDefaultApp();
		theApp.show();
	}

	public void noPickResult() {
		// TODO Auto-generated method stub
		
	}

	public boolean disableVpBehaviorWhileSelecting() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isSelectionEmpty() {
		// TODO Auto-generated method stub
		return false;
	}
    
 
}
