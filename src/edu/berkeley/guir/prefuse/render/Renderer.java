package edu.berkeley.guir.prefuse.render;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import edu.berkeley.guir.prefuse.VisualItem;

/**
 * Interface for rendering VisualItems. Supports drawing as well as location
 * and bounding box routines.
 * 
 * @author newbergr
 * @author jheer
 */
public interface Renderer {
 
  /**
   * Render item into a Graphics2D context.
   * @param g the Graphics2D context
   * @param item the item to draw
   */
  public void render(Graphics2D g, VisualItem item);

  /**
   * Returns true if the Point is located inside coordinates of the item.
   * @param p the point to test for containment
   * @param item the item to test containment against
   * @return true if the point is contained within the the item, else false
   */
  public boolean locatePoint(Point2D p, VisualItem item);
  
  /**
   * Returns a reference to the bounding rectangle for the item.
   * @param item the item to compute the bounding box for
   * @return the item's bounding box as a Rectangle
   */
  public Rectangle getBoundsRef(VisualItem item);  
  
} // end of interface Renderer