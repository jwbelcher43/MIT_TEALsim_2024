/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: TGeometry.java,v 1.2 2007/07/16 22:04:52 pbailey Exp $
 * 
 */

package teal.render.geometry;

/**
 * The TGeometry interface defines a general structure for indexed geometry arrays.
 * 
 * @author Andrew McKinney
 * @author Phil Bailey
 * @author Michael Danziger
 * @version $Revision: 1.2 $
 */

public interface TGeometry {

    public static final int POINT = 0x001;
    public static final int LINE = 0x002;
    public static final int TRIANGLE = 0x004;
    public static final int QUAD = 0x008;
    public static final int POLYGON = 0x010;

    public static final int FACE = 0x020;
    public static final int EDGE = 0x040;

    public static final int NORMALS = 0x0100;
    public static final int COLORS = 0x0200;
    public static final int TEXTURE2 = 0x0400;
    public static final int TEXTURE3 = 0x0800;

    public static final int STRIP = 0x1000;
    public static final int FAN = 0x2000;
    public static final int LOOP = 0x4000;
    public static final int INDEXED = 0x8000;
}
