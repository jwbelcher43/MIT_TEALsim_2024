/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: NamedValue.java,v 1.5 2007/12/24 23:03:48 jbelcher Exp $ 
 * 
 */

package teal.util;

/**
 * NamedValue.java
 *
 * @author	Philip Bailey - Center for Educational Computing Initiatives / MIT
 * @version	1.0 - July 1998
 *
 * Utility class for db3d, stores a String name and any object
 * Designed to hold fieldName & data/dataType may have many uses.
 * @see Map
 */
public class NamedValue implements java.lang.Comparable, java.io.Serializable {

    private static final long serialVersionUID = 3256437023551336496L;
    String name;
    Object value;

    public NamedValue(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public NamedValue(String name, double value) {
        this.name = name;
        this.value = new Double(value);
    }

    public NamedValue(String name, int value) {
        this.name = name;
        this.value = new Integer(value);
    }

    public NamedValue() {
        this.name = null;
        this.value = null;
    }

    public int compareTo(Object obj) throws ClassCastException {
        if (obj instanceof NamedValue) {
            return name.compareTo(((NamedValue) obj).getName());
        } else return name.compareTo((String) obj);
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String toString() {
        return name;
    }
}