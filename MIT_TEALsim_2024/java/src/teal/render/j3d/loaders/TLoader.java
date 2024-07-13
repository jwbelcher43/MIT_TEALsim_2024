/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TLoader.java,v 1.4 2007/07/16 22:04:56 pbailey Exp $ 
 * 
 */

package teal.render.j3d.loaders;

import java.net.URL;
import javax.media.j3d.*;


public interface TLoader /*extends com.sun.j3d.loaders.Loader*/ {
	public BranchGroup getBranchGroup(String fileName,String texturePath);
    public BranchGroup getBranchGroup(URL loadUrl);
}
	
