/*
 * TEALsim - MIT TEAL Project
 * Copyright (c) 2004 The Massachusetts Institute of Technology. All rights reserved.
 * Please see license.txt in top level directory for full license.
 * 
 * http://icampus.mit.edu/teal/TEALsim
 * 
 * $Id: Node3D.java,v 1.77 2008/08/21 17:56:35 jbelcher Exp $ 
 * 
 */

package teal.render.j3d;

import java.awt.Color;
import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.LineArray;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Switch;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix3d;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import teal.config.Teal;
import teal.render.HasPosition;
import teal.render.HasRotation;
import teal.render.TAbstractRendered;
import teal.render.scene.TNode3D;
import teal.render.viewer.Viewer;
import teal.util.TDebug;

import com.sun.j3d.utils.geometry.Cone;

/**
 * The Node3D provides general position and rotation management it
 * provides a TransformGroup for the management of a Rendered object,
 * child Nodes are added to this group.
 * Derived versions of this class may impose additional nodes between
 * the root and the content group.
 * 
 * Any geometry should be constructed with (0,0,0) as the center
 * as rotation and position are relative to absolute origin.
 *
 * The goal is to have the Node be able to manage its position
 * and representation with a small impact on the Rendered/TDrawable interface.
 * Pick behavior will rotate the rotate Node, or position the object without
 * modifying the geometry in most cases. Modifications as a result of
 * pick behavior will set the bound variables of the represented elements.
 *
 * Depending on the derived type a Shape3D  or PrimitiveShape3D,
 * may want to support multiple shapes, but will use multiple geometries
 * for now.
 *
 * @author Phil Bailey
 * @version $Revision: 1.77 $
 *
 **/

public class Node3D extends BranchGroup implements TNode3D {

    public final static Vector3d refDirection;
    public final static Vector3d refPosition;

    public final static LineArray sLine;
    public final static Geometry sStem;
    public final static Geometry sCone;
    public final static Geometry sBase;

    static {
        refDirection = new Vector3d(0., 01., 0.);
        refPosition = new Vector3d();

        sLine = new LineArray(2, GeometryArray.COORDINATES);
        sLine.setCoordinate(0, new Point3f(0, 0, 0));
        sLine.setCoordinate(0, new Point3f(0, 1, 0));


  //      sStem = teal.render.geometry.Cylinder.makeGeometry(20, .02, 2., 0.).getIndexedGeometryArray(true);
        sStem = teal.render.geometry.Cylinder.makeGeometry(20, .02, 1., .5).getIndexedGeometryArray(true);

  //      Cone cone = new Cone(0.1f, 0.15f);
        Cone cone = new Cone(0.07f, 0.3f);
        sCone = cone.getShape(Cone.BODY).getGeometry();
        sBase = cone.getShape(Cone.CAP).getGeometry();
    }

    protected boolean selected = false;
    protected boolean picked = false;
    protected boolean pickable = true;
    protected boolean selectable = false;
    protected boolean isRotable = false;
    protected boolean isRotating = false;
    protected boolean isVisible = true;

    /** visiblity controls */
    protected Switch mSwitch;
    /** Transform management */
    protected TransformGroup mTransform;
    /** The container for most nodes, also contains the model offset transform */
    protected TransformGroup mContents;
    
    // label stuff
//    protected Switch mLabelSwitch;
//    protected TransformGroup mLabelOffset;
//    protected Text2D mLabelShape;

    public Node3D() {
        super();

        setCapability(BranchGroup.ALLOW_DETACH);
        setCapability(Group.ALLOW_CHILDREN_EXTEND);
        setCapability(Group.ALLOW_CHILDREN_READ);
        setCapability(Group.ALLOW_CHILDREN_WRITE);
        setCapability(Node.ALLOW_PICKABLE_READ);
        setCapability(Node.ALLOW_PICKABLE_WRITE);
        setCapability(Node.ENABLE_PICK_REPORTING);
        setCapability(Node.ALLOW_AUTO_COMPUTE_BOUNDS_READ);

        mSwitch = new Switch();
        mSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
        mSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        mSwitch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        mSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
        mSwitch.setCapability(Group.ALLOW_CHILDREN_WRITE);
        mSwitch.setCapability(Node.ALLOW_PICKABLE_READ);
        mSwitch.setCapability(Node.ALLOW_PICKABLE_WRITE);
        mSwitch.setCapability(Node.ENABLE_PICK_REPORTING);
        mSwitch.setCapability(Node.ALLOW_AUTO_COMPUTE_BOUNDS_READ);
        addChild(mSwitch);

        mTransform = new TransformGroup();
        mTransform.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        mTransform.setCapability(Group.ALLOW_CHILDREN_READ);
        mTransform.setCapability(Group.ALLOW_CHILDREN_WRITE);
        mTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        mTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        mTransform.setCapability(Node.ALLOW_PICKABLE_READ);
        mTransform.setCapability(Node.ALLOW_PICKABLE_WRITE);
        mTransform.setCapabilityIsFrequent(Node.ENABLE_PICK_REPORTING);
        mTransform.setCapability(Node.ENABLE_PICK_REPORTING);
        mTransform.setCapability(Node.ALLOW_AUTO_COMPUTE_BOUNDS_READ);

        mContents = new TransformGroup();
        mContents.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        mContents.setCapability(Group.ALLOW_CHILDREN_READ);
        mContents.setCapability(Group.ALLOW_CHILDREN_WRITE);
        mContents.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        mContents.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        mContents.setCapabilityIsFrequent(Node.ENABLE_PICK_REPORTING);
        mContents.setCapability(Node.ALLOW_AUTO_COMPUTE_BOUNDS_READ);
        
        
        /////////////// label stuff
//        mLabelSwitch = new Switch();
//        mLabelSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
//        mLabelSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
//        mLabelSwitch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
//        mLabelSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
//        mLabelSwitch.setCapability(Group.ALLOW_CHILDREN_WRITE);
//        mLabelSwitch.setCapability(Node.ALLOW_PICKABLE_READ);
//        mLabelSwitch.setCapability(Node.ALLOW_PICKABLE_WRITE);
//        mLabelSwitch.setCapability(Node.ENABLE_PICK_REPORTING);
//        mLabelSwitch.setCapability(Node.ALLOW_AUTO_COMPUTE_BOUNDS_READ);
//        mLabelSwitch.setWhichChild(Switch.CHILD_ALL);
//        
//        mLabelOffset = new TransformGroup();
//        mLabelOffset.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
//        mLabelOffset.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
//        Transform3D t = new Transform3D();
//        t.setTranslation(new Vector3d(1.,1.,0));
//        //Billboard bb = new Billboard(mLabelOffset,Billboard.ROTATE_ABOUT_POINT,new Point3f(0.f,0.f,0.f));
//		//bb.setSchedulingBounds(new BoundingSphere(new Point3d(0.,0.,0.), 100.));
//		//mLabelOffset.addChild(bb);
//		//mLabelShape = new Text2D("This is a Text2D!!", new Color3f(1.f,1.f,1.f),"SansSerif",20,Font.PLAIN);
//		OrientedShape3D shape = new OrientedShape3D();
//		shape.setGeometry(new Text3D(new Font3D(new Font("Dialog", Font.BOLD, 12),new FontExtrusion()), "This is a Text3D!!"));
//		mLabelOffset.addChild(shape);
//		mLabelOffset.addChild(mLabelShape);
//		mLabelSwitch.addChild(mLabelOffset);
//		mContents.addChild(mLabelSwitch);
        
        mTransform.addChild(mContents);
        mSwitch.addChild(mTransform);

        setVisible(true);
    }

    public Node3D(TAbstractRendered element) {
        this();
        setElement(element);
    }

    public void setElement(TAbstractRendered element) {
        setUserData(element);
        if (element != null) {
            if (element instanceof HasPosition) setPosition(((HasPosition) element).getPosition());
            if (element instanceof HasRotation) setRotation(((HasRotation) element).getRotation());
        }
    }

    public TAbstractRendered getElement() {
        return (TAbstractRendered) getUserData();
    }

    /* (non-Javadoc)
     * @see teal.render.HasRotation#setRotable(boolean)
     */
    public void setRotable(boolean rotable) {
        isRotable = rotable;
    }

    /* (non-Javadoc)
     * @see teal.render.HasRotation#isRotable()
     */
    public boolean isRotable() {
        return isRotable;
    }

    /* (non-Javadoc)
     * @see teal.render.HasRotation#setRotating(boolean)
     */
    public void setRotating(boolean state) {
        isRotating = state;
    }

    /* (non-Javadoc)
     * @see teal.render.HasRotation#isRotating()
     */
    public boolean isRotating() {
        return isRotating;
    }

    /* (non-Javadoc)
     * @see teal.render.HasBoundingArea#getBoundingArea()
     */
    public Bounds getBoundingArea() {
        return getBounds();
    }

    /* (non-Javadoc)
     * @see teal.render.IsPickable#isPickable()
     */
    public boolean isPickable() {
        return pickable;
    }

    /** 
     * This in combination with setPickMode need to be looked at */
    public void setPickable(boolean b) {
        pickable = b;
        super.setPickable(b);
        mTransform.setPickable(b);
    }

    /** 
     * setPickMode seems to select which transformGroup reports the pick, this does not seem to be the correcct action.
     */
    public void setPickMode(int mode) {
        switch (mode) {
            case Viewer.ROTATE:
                mContents.setCapability(Node.ENABLE_PICK_REPORTING);
                break;
            case Viewer.TRANSLATE:
                mContents.clearCapability(Node.ENABLE_PICK_REPORTING);
                break;
            case Viewer.ZOOM:
                mContents.clearCapability(Node.ENABLE_PICK_REPORTING);
                break;
            case Viewer.NONE:
            default:
                mContents.clearCapability(Node.ENABLE_PICK_REPORTING);
                break;
        }
    }

    public boolean isPicked() {
        return picked;
    }

    public boolean getPicked() {
        return picked;
    }

    public void setPicked(boolean b) {
        picked = b;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean b) {
        selected = b;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public void setSelectable(boolean b) {
        selectable = b;
    }

    /* (non-Javadoc)
     * @see teal.render.HasPosition#setPosition(javax.vecmath.Vector3d)
     */
    public void setPosition(Vector3d pos) {
        Transform3D tr = getTransform3D();
        tr.setTranslation(pos);
        mTransform.setTransform(tr);

    }

    /* (non-Javadoc)
     * @see teal.render.HasPosition#getPosition()
     */
    public Vector3d getPosition() {
        Vector3d pos = new Vector3d();
        Transform3D trans = new Transform3D();
        mTransform.getTransform(trans);
        trans.get(pos);

        return pos;
    }

    /**
     * This sets the offset transform of the object's rendered model from the object's transform.
     * @param t offset Transform3D
     */
    public void setModelOffsetTransform(Transform3D t) {
        mContents.setTransform(t);
    }

    public Transform3D getModelOffsetTransform() {
        Transform3D t = new Transform3D();
        mContents.getTransform(t);
        return t;
    }

    public void setModelOffsetPosition(Vector3d offset) {
        Transform3D t = new Transform3D();
        mContents.getTransform(t);
        t.setTranslation(offset);
        mContents.setTransform(t);
    }

    public Vector3d getModelOffsetPosition() {
        Transform3D t = new Transform3D();
        mContents.getTransform(t);
        Vector3d offset = new Vector3d();
        t.get(offset);
        return offset;
    }

    /* (non-Javadoc)
     * @see teal.render.scene.TNode3D#getScale()
     */
    public Vector3d getScale() {
        Vector3d s = new Vector3d();
        Transform3D trans = new Transform3D();
        mTransform.getTransform(trans);
        trans.getScale(s);
        return s;
    }

    
    public void setScale(double s) {
        Transform3D trans = getTransform3D();
        trans.setScale(s);
        mTransform.setTransform(trans);
    }

    public void setScale(Vector3d s) {
        Transform3D trans = getTransform3D();
        trans.setScale(s);
        mTransform.setTransform(trans);
    }

    public Quat4d getRotation() {
        Transform3D trans = getTransform3D();
        Quat4d rot = new Quat4d();
        trans.get(rot);
        //System.out.println("Node3D::getRotation() returning: " + rot);
        return rot;
    }

    public void setRotation(Matrix3d rot) {
        Transform3D tr = getTransform3D();
        tr.setRotation(rot);
        mTransform.setTransform(tr);
        
    }

    public void setRotation(Quat4d quat) {
    	//System.out.println("Node3D::setRotation(Quat4d) setting to: " + quat);
        Transform3D tr = getTransform3D();
        tr.setRotation(quat);
        mTransform.setTransform(tr);
        
    }

    protected Transform3D getDirectionTransform(Vector3d refDirection, Vector3d newDirection) {
        TDebug.println(3, "setDirection: " + newDirection);
        if (newDirection.length() == 0) return getTransform3D();
        Vector3d direction = new Vector3d(newDirection);
        direction.normalize();
        Vector3d axis = new Vector3d();
        axis.cross(refDirection, direction);
        Transform3D trans = getTransform3D();
        AxisAngle4d axisAngle = null;
        double angle = refDirection.angle(direction);
        if (axis.length() != 0) {
            axis.normalize();
            axisAngle = new AxisAngle4d(axis.x, axis.y, axis.z, angle);
        } else {
            if (angle > Math.PI / 2.) {
                Vector3d u = new Vector3d();
                Vector3d v = new Vector3d();
                do {
                    u.set(Math.random(), Math.random(), Math.random());
                    v.set(refDirection);
                    v.normalize();
                    v.scale(u.dot(v));
                    u.sub(v);
                } while (u.length() < Teal.DoubleZero);
                u.normalize();
                axisAngle = new AxisAngle4d(u, angle);
            } else axisAngle = new AxisAngle4d(refDirection, 0.);
        }
        trans.setRotation(axisAngle);
        return trans;
    }

    public void setDirection(Vector3d newDirection) {
        Transform3D trans = getDirectionTransform(refDirection, newDirection);
        mTransform.setTransform(trans);
    }

    public Transform3D getTransform3D() {
        Transform3D trans = new Transform3D();
        mTransform.getTransform(trans);
        return trans;
    }

    public Transform3D getTransform() {
        return getTransform3D();
    }

    public void setTransform(Transform3D trans) {
        mTransform.setTransform(trans);
    }

    /* Need to figure what to do here */
    public void setTransform(Vector3d pos, Vector3d direction) {
        setPosition(pos);
        setDirection(direction);
    }

    public void setTransform(Vector3d pos, Vector3d direction, double scale) {
        setPosition(pos);
        Transform3D trans = getDirectionTransform(refDirection, direction);
        trans.setScale(scale);
        mTransform.setTransform(trans);
    }

    /* Need to figure what to do here */
    public void setTransform(Matrix3d rot, Vector3d pos, double scale) {
        Transform3D tran = new Transform3D();
        tran.set(pos);
        tran.setRotation(rot);
        tran.setScale(scale);
        mTransform.setTransform(tran);
    }

    public void addMarker() {
        addMarker(new com.sun.j3d.utils.geometry.Sphere((float) (Teal.PointChargeRadius)));
    }

    public void addMarker(Node objNode) {
        mTransform.addChild(objNode);
    }

    public void addContents(Node objNode) {
        mContents.addChild(objNode);
    }

    public Node getContents() {
        return mContents.getChild(0);
    }

    public int numContentItems() {
        return mContents.numChildren();
    }

    public Enumeration getAllContents() {
        return mContents.getAllChildren();
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean b) {
        isVisible = b;
        if (isVisible) {
            mSwitch.setWhichChild(Switch.CHILD_ALL);
        } else {
            mSwitch.setWhichChild(Switch.CHILD_NONE);
        }
    }

    public static Appearance makeAppearance() {
        return makeAppearance(new Color3f(Color.BLUE));
    }

    public static Appearance makeAppearance(Color3f c) {
        return makeAppearance(c, 0.5f);
    }

    public static Appearance makeAppearance(Color3f c, float shininess) {
        return makeAppearance(c, shininess, 0.f, true);
    }

    public static Appearance makeAppearance(Color3f c, float shininess, float trans, boolean backCull) {
        return makeAppearance(c, null, shininess, trans, backCull,PolygonAttributes.POLYGON_FILL);
    }

    public static Appearance makeAppearance(Color3f c, Color3f emissive, float shininess, float trans, boolean backCull){
        return makeAppearance(c, emissive, shininess, trans, backCull,PolygonAttributes.POLYGON_FILL);
    } 
    
    public static Appearance makeAppearance(Color3f c, Color3f emissive, float shininess, float trans, boolean backCull,int polygonMode) {
        Appearance app = new Appearance();
        if (!backCull) {
            PolygonAttributes polyAttribs = new PolygonAttributes(polygonMode,
                PolygonAttributes.CULL_NONE, 0);
            app.setPolygonAttributes(polyAttribs);
        }

        // Capabilities
        app.setCapability(Appearance.ALLOW_MATERIAL_READ);
        app.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_READ);
        app.setCapability(Appearance.ALLOW_COLORING_ATTRIBUTES_WRITE);
        app.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_READ);
        app.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
        app.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_READ);
        app.setCapability(Appearance.ALLOW_RENDERING_ATTRIBUTES_WRITE);
        //////
        //app.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_READ);
        //app.setCapability(Appearance.ALLOW_LINE_ATTRIBUTES_WRITE);

        // Color
        ColoringAttributes coloringAttributes = new ColoringAttributes(new Color3f(c), ColoringAttributes.SHADE_GOURAUD);
        coloringAttributes.setCapability(ColoringAttributes.ALLOW_COLOR_READ);
        app.setColoringAttributes(coloringAttributes);

        // Material
        Material mat = app.getMaterial();
        if (mat == null) {
            mat = new Material();
            mat.setCapability(Material.ALLOW_COMPONENT_WRITE);
            mat.setCapability(Material.ALLOW_COMPONENT_READ);
        }
        Color3f c3 = new Color3f(c);
        mat.setDiffuseColor(c3);
        mat.setSpecularColor(c3);
        //mat.setSpecularColor(new Color3f(1.f,1.f,1.f));
        c3.scale(0.9f);
        mat.setAmbientColor(c3);

        if (emissive != null) mat.setEmissiveColor(new Color3f(emissive));
        //Float f = new Float(shininess * 128);
        mat.setShininess(shininess*128.f);
        app.setMaterial(mat);

        // Transparency
        if(trans >= 0.02f){
            app.setTransparencyAttributes(new TransparencyAttributes(
        		TransparencyAttributes.NICEST, trans));
        }else{
            app.setTransparencyAttributes(null);
        }

        return app;
    }

    public static Appearance setShininess(Appearance app, float value) {
        Material mat = app.getMaterial();
        if (mat == null) {
            mat = new Material();
            mat.setCapability(Material.ALLOW_COMPONENT_WRITE);
            mat.setCapability(Material.ALLOW_COMPONENT_READ);
        }
        Float f = new Float(value * 128);
        mat.setShininess(f.intValue());
        app.setMaterial(mat);
        return app;
    }

    public static void setEmissive(Shape3D shape, Color base, float amount) {
        Appearance app = shape.getAppearance();
        if (app == null) {
            app = makeAppearance();
        }

        Material mat = app.getMaterial();
        if (mat == null) {
            mat = new Material();
            mat.setCapability(Material.ALLOW_COMPONENT_WRITE);
            mat.setCapability(Material.ALLOW_COMPONENT_READ);
        }
        Color3f col = new Color3f(base);
        col.scale(amount);
//      mat.setEmissiveColor(col);
        
        // Mac implementation of J3D may not support emissive colors... testing alternative method
        // make sure this is being called
        //System.out.println("Node3D.setEmissive() calling my changes!");
        Color3f oldcolor = new Color3f();
        mat.getAmbientColor(oldcolor);
        oldcolor.add(col);
        mat.setAmbientColor(oldcolor);
        
        app.setMaterial(mat);
        shape.setAppearance(app);
    }

    public static void initShape(Shape3D shape) {
        shape.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
        shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
        shape.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_READ);
        shape.setCapability(Shape3D.ALLOW_APPEARANCE_OVERRIDE_WRITE);
        shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
        shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

    }
}
