package fr.avianey.mojo.androidgendrawable;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.apache.batik.gvt.GraphicsNode;

/**
 * <strong>Experimental</stong>
 * <p>
 * Use as a fallback when parsed svg files do not include "width" and "height" attributes on
 * the root &lt;svg&gt; node. Width and height are calculated according to the given Batik
 * BoundsType.
 * </p>
 * @author avianey
 * @deprecated
 */
public enum BoundsType {

	/**
	 * Returns the bounds of this node in user space. 
	 * This includes primitive paint, filtering, clipping and masking.
	 */
	all,
	/**
	 * Returns the bounds of the sensitive area covered by this node, 
	 * This includes the stroked area but does not include the effects of clipping, masking or filtering.
	 */
	sensitive,
	/**
	 * Returns the bounds of the sensitive area covered by this node, 
	 * This includes the stroked area but does not include the effects of clipping, masking or filtering.
	 */
	geometry,
	/**
	 * Returns the bounds of the area covered by this node's primitive paint. 
	 * This is the painted region of fill and stroke but does not account for clipping, masking or filtering.
	 */
	primitive;
	
	public Rectangle getBounds(GraphicsNode rootGN) {
		Rectangle2D r2d;
		switch (this) {
		case sensitive:
			r2d = rootGN.getSensitiveBounds();
			break;
		case geometry:
			r2d = rootGN.getGeometryBounds();
			break;
		case primitive:
			r2d = rootGN.getPrimitiveBounds();
			break;
		case all:
		default:
			r2d = rootGN.getBounds();
			break;
		}
		return r2d.getBounds();
	}
	
}
