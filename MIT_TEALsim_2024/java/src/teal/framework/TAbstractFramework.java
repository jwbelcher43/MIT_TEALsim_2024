/*
 * $Id: TAbstractFramework.java,v 1.5 2007/07/16 22:04:46 pbailey Exp $
 */

package teal.framework;

import javax.swing.JFrame;

public interface TAbstractFramework {

    public JFrame getTheWindow();
    public void setTitle(String t);
}
