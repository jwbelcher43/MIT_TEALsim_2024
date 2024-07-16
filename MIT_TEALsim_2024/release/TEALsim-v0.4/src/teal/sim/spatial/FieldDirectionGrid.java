/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: FieldDirectionGrid.java,v 1.10 2008/11/10 18:58:27 jbelcher Exp $ 
 * 
 */

package teal.sim.spatial;

import java.awt.*;
import java.util.*;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;

import teal.math.*;
import teal.render.j3d.*;
import teal.render.scene.*;
import teal.util.*;

public class FieldDirectionGrid extends SpatialField {

    private static final long serialVersionUID = 3544677265209635383L;
    protected int resolution = 12;
    protected int xres = 12;
    protected int yres = 12;
    protected GridIterator vgIterator = null;
    protected boolean scaleByMagnitude = false;

    public FieldDirectionGrid() {
        super();
        setPickable(false);
    }

    protected TNode3D makeNode() {
        TNode3D node = (TNode3D) new ArrayNode();
        updateNode3D((TArrayNode) node);
        node.setPickable(false);
        return node;
    }

    public void setResolution(int res) {
        if (res != resolution) {
            if (mNode != null) {
                synchronized (mNode) {
                    maxShowNodes((ArrayNode) mNode, res * res);
                }
            }
            if (vgIterator != null) {
                if (vgIterator instanceof RectangularPlane) ((RectangularPlane) vgIterator).setResolution(res, res);
            }

            //TDebug.println(1,id + ": setting resolution to: " + res);
            int old = resolution;
            resolution = res;
            xres = res;
            yres = res;
            firePropertyChange("resolution", old, res);
            needsSpatial();
        }
    }

    public int getResolution() {
        return resolution;
    }

    public void setXres(int res) {
        if (res != xres) {
            if (mNode != null) {
                synchronized (mNode) {
                    maxShowNodes((ArrayNode) mNode, xres * yres);
                }
            }
            if (vgIterator != null) {
                if (vgIterator instanceof RectangularPlane) ((RectangularPlane) vgIterator).setResolution(xres, yres);
            }

            int old = xres;
            xres = res;
            //yres = res;
            firePropertyChange("xres", old, xres);
            needsSpatial();
        }
    }

    public void setYres(int res) {
        if (res != yres) {
            if (mNode != null) {
                synchronized (mNode) {
                    maxShowNodes((ArrayNode) mNode, xres * yres);
                }
            }
            if (vgIterator != null) {
                if (vgIterator instanceof RectangularPlane) ((RectangularPlane) vgIterator).setResolution(xres, yres);
            }

            int old = yres;
            yres = res;
            firePropertyChange("yres", old, yres);
            needsSpatial();
        }
    }

    public void setGridIterator(GridIterator iter) {
        vgIterator = iter;
    }

    public GridIterator getGridIterator() {
        return vgIterator;
    }

    public void nextSpatial() {
        registerRenderFlag(GEOMETRY_CHANGE);
    }

    public Bounds getBoundingArea() {

        return bounds;
    }

    private void maxShowNodes(ArrayNode node, int number) {
        int count = node.getNodeCount();
        node.setVisible(0, number - 1, true);
        if (count > number) {
            node.setVisible(number, count - 1, false);
        }
    }

    public void render() {
        if (mNeedsSpatial) {
            renderFlags |= GEOMETRY_CHANGE;
            mNeedsSpatial = false;
        }
        if (mNode == null) return;
        if ((renderFlags & VISIBILITY_CHANGE) == VISIBILITY_CHANGE) {
            mNode.setVisible(showNode);
            renderFlags ^= VISIBILITY_CHANGE;
        }
        if (isDrawn) {
            if ((renderFlags & GEOMETRY_CHANGE) == GEOMETRY_CHANGE) {
                updateNode3D((TArrayNode) mNode);
                renderFlags ^= GEOMETRY_CHANGE;
            }
            super.render();
        }

    }

    public void updateNode3D(TArrayNode node) {
        if ((resolution == 0) || (node == null)) return;
        double len = 1.;
        boolean setLength = true;

        if (theEngine != null && resolution > 0) {

            if (vgIterator == null) {
                vgIterator = new RectangularPlane(theEngine.getBoundingArea());
                ((RectangularPlane) vgIterator).setResolution(resolution, resolution);
                //((RectangularPlane)vgIterator).setResolution(xres,yres);

            }
            len = vgIterator.getDX() * 0.75;
            //System.out.println("len = " + len);
            synchronized (node) {
                //if ( node.getNodeCount() < (resolution * resolution)) {
                if (node.getNodeCount() < (xres * yres)) {
                    TDebug.println(1, "\tUpdateNode3d adding nodes");

                    //Appearance app = Node3D.makeAppearance(getColor());
                    //for(int i = node.getNodeCount(); i < resolution*resolution;i++)
                    for (int i = node.getNodeCount(); i < xres * yres; i++) {
                        TShapeNode ar = (TShapeNode) new SolidArrowNode();
                        ar.setVisible(true);
                        ar.setPickable(false);
                        ar.setColor(mColor);
                        ar.setScale(len);
                        ar.setModelOffsetPosition(new Vector3d(0.,-0.5,0.));
                        ((TArrayNode) node).addNode(ar);
                    }

                }

                TNode3D arrow = (TNode3D) ((TArrayNode) node).get(0);
                if (arrow != null) {
                    if (scaleByMagnitude == false) {
                        Vector3d scale = arrow.getScale();
                        if (scale.x == len) setLength = false;
                        }
                }
                Vector3d pos = new Vector3d();
                Iterator nodes = node.iterator();
                vgIterator.reset();
                while (vgIterator.hasNext() && nodes.hasNext()) {
                    pos = vgIterator.nextVec();
                    Vector3d fieldValue = field.get(pos);
                    arrow = (TNode3D) nodes.next();

                    if (fieldValue.length() > 0.) {
                        if (!arrow.isVisible()) {
                            arrow.setVisible(true);
                        }
                        arrow.setPosition(pos);
                        arrow.setDirection(fieldValue);
                        
                        //TDebug.println(0,"FieldValue.length() = " + fieldValue.length());
                        if (setLength) {
                            if (scaleByMagnitude == false) {
                                arrow.setScale(len);
                            } else {
                                arrow.setScale(fieldValue.length());
                            }
                            //double offsetmag = scaleByMagnitude ? fieldValue.length() : len;
                            //Vector3d offset = new Vector3d(0.,-offsetmag,0.);
                            //offset.scale(-0.5);
                            //arrow.setModelOffsetPosition(offset);
                        }
                        
                        

                    } else {
                        if (arrow.isVisible()) {
                            arrow.setVisible(false);
                        }
                    }

                }
            }
        }

    }

    public ImageIcon getIcon() {
        return (ImageIcon) IconCreator.getIcon("FieldLine.gif");
    }

 

    public void setColor(Color c) {
        super.setColor(c);
        if (mNode != null) {
            //Appearance app = Node3D.makeAppearance(getColor());
            Iterator it = ((TArrayNode) mNode).iterator();
            while (it.hasNext()) {
                TShapeNode ar = (TShapeNode) it.next();
                ar.setColor(mColor);
            }
        }
    }

    /**
     * @return Returns the scaleByMagnitude.
     */
    public boolean isScaleByMagnitude() {
        return scaleByMagnitude;
    }

    /**
     * @param scaleByMagnitude The scaleByMagnitude to set.
     */
    public void setScaleByMagnitude(boolean scaleByMagnitude) {
        this.scaleByMagnitude = scaleByMagnitude;
    }
}
