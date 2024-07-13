/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TMaterial.java,v 1.4 2007/07/16 22:04:57 pbailey Exp $ 
 * 
 */

package teal.render.scene;

import javax.vecmath.Color3f;

import teal.render.HasColor;

/** Interface for the
 */

public interface TMaterial extends TSceneObject, HasColor{

    public final static int FRONT_FACE = 1;
    public final static int BACK_FACE = 2;
    public final static int FRONT_AND_BACK_FACE =FRONT_FACE|BACK_FACE;
    
    public void setShininess(float shine);
    public float getShininess();
    public void setFaceMode(int mode);
    public int getFaceMode();
    public Color3f getEmissive();
    public Color3f getDiffuse();
    public Color3f getSpecular();
    public Color3f getAmbient();
    public void setEmissive(Color3f col);
    public void setDiffuse(Color3f col);
    public void setSpecular(Color3f col);
    public void setAmbient(Color3f col);
    public void set(TMaterial mat);

}
