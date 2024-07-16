package teal.render.primitives;

import teal.render.Rendered;
import teal.render.j3d.SphereNode;
import teal.render.scene.TNode3D;
import teal.render.scene.TShapeNode;

public class Sphere extends Rendered {
	
	private static final long serialVersionUID = 1L;
	private int segments = 12;
	private double radius = 1.;
	private float transparency = 1.f;
	private boolean transparencyChanged = false;
	
	
	public Sphere() {
		super();
	}
	
	public Sphere(int segs, double radius) {
		this.segments = segs;
		this.radius = radius;
	}
	
	public TNode3D makeNode() {
		TShapeNode node = (TShapeNode) new SphereNode(1.f, segments);
		//TShapeNode node = (TShapeNode) new SphereNode();
		node.setColor(this.getColor());
		node.setScale(radius);
		return node;
	}
	
	public void render() {
		if (transparencyChanged) {
			((TShapeNode)mNode).setTransparency(transparency);
			transparencyChanged = false;
		}
		super.render();
	}

	/**
	 * @return Returns the radius.
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * @param radius The radius to set.
	 */
	public void setRadius(double radius) {
		this.radius = radius;
	}

	/**
	 * @return Returns the transparency.
	 */
	public float getTransparency() {
		return transparency;
	}

	/**
	 * @param transparency The transparency to set.
	 */
	public void setTransparency(float transparency) {
		this.transparency = transparency;
		transparencyChanged = true;
	}
	
	
}
