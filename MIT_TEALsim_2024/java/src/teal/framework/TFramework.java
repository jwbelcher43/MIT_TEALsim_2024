/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TFramework.java,v 1.30 2007/07/26 15:07:13 pbailey Exp $
 * 
 */

package teal.framework;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Action;
import javax.swing.JFileChooser;

import teal.browser.Browser;
import teal.core.TElementManager;
import teal.ui.swing.StatusBar;

/**
 * The framework interface for all TEAL top level applications.
 *
 * @author mesrob
 * @author Andrew McKinney
 * @author Phil Bailey
 * @author Michael Danziger
 * @version $Revision: 1.30 $ 
 */
public interface TFramework extends TAbstractFramework, ActionListener, TElementManager {
    public TAbstractMenuBar getTMenuBar();
    public TStatusBar getStatusBar();
    public TToolBar getTToolBar();
    public void addAction(String str, Action a);
    public void removeAction(String str, Action a);
    public void addComponent(Component elm);
    public void removeComponent(Component elm);

    /**
     * Cleanup method, should be synchronized.
     */
    public void dispose();

    public Browser openBrowser(String urlString);

    public JFileChooser getFileChooser();

    public Cursor getAppCursor();

    public void setAppCursor(Cursor cur);

    // Not sure why this was added to the interface ?
    public void load(File input);
    
}
