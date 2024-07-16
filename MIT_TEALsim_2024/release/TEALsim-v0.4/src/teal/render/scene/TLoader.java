/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TLoader.java,v 1.4 2007/07/16 22:04:57 pbailey Exp $ 
 * 
 */

package teal.render.scene;

import java.net.*;

public interface TLoader /*extends com.sun.j3d.loaders.Loader*/ {
	public TNode3D getTNode3D(String fileName);
    public TNode3D getTNode3D(String fileName,String texturePath);
    public TNode3D getTNode3D(URL url);
}