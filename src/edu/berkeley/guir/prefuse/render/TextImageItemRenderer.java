package edu.berkeley.guir.prefuse.render;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.util.FontLib;

/**
 * Renders an item as an image and a text string.
 * 
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class TextImageItemRenderer extends ShapeRenderer {

	public static final int ALIGNMENT_LEFT   = 0;
	public static final int ALIGNMENT_RIGHT  = 1;
	public static final int ALIGNMENT_CENTER = 2;
	public static final int ALIGNMENT_BOTTOM = 1;
	public static final int ALIGNMENT_TOP    = 0;

	protected Graphics2D m_g;
	protected BufferedImage m_buff;
	
	protected ImageFactory m_images = new ImageFactory();
	
	protected String m_labelName = "label";
	protected String m_imageName = "image";
	
	protected int m_xAlign = ALIGNMENT_CENTER;
	protected int m_yAlign = ALIGNMENT_CENTER;
	protected int m_horizBorder = 3;
	protected int m_vertBorder  = 0;
	protected int m_imageMargin = 4;
	
	protected Font m_font = new Font("SansSerif", Font.PLAIN, 10);	
	protected Rectangle2D m_imageBox  = new Rectangle2D.Float();
	protected Point2D     m_tmpPoint = new Point2D.Float();

	public TextImageItemRenderer() {
		// TODO: this is hacky. Is there a better way to achieve this?
		m_buff = new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
		m_g = (Graphics2D)m_buff.getGraphics();
	} //

	/**
	 * Sets maximum image dimensions, used to control scaling of loaded images
	 * @param width the max width of images (-1 for no limit)
	 * @param height the max height of images (-1 for no limit)
	 */
	public void setMaxImageDimensions(int width, int height) {
		m_images.setMaxImageDimensions(width, height);
	} //

	/**
	 * Get the attribute name of the text to draw.
	 * @return the text attribute name
	 */
	public String getTextAttributeName() {
		return m_labelName;
	} //
	
	/**
	 * Set the attribute name for the text to draw.
	 * @param name the text attribute name
	 */
	public void setTextAttributeName(String name) {
		m_labelName = name;
	} //

	/**
	 * Returns the text to draw. Subclasses can override this class to
	 * perform custom text rendering.
	 * @param item the item to represent as a <code>String</code>
	 * @return a <code>String</code> to draw
	 */
	protected String getText(VisualItem item) {
		return (String)item.getAttribute(m_labelName);
	} //
	
	/**
	 * Get the attribute name of the image to draw.
	 * @return the image attribute name
	 */
	public String getImageAttributeName() {
		return m_imageName;
	} //
	
	/**
	 * Set the attribute name for the image to draw.
	 * @param name the image attribute name
	 */
	public void setImageAttributeName(String name) {
		m_imageName = name;
	} //	
	
	/**
	 * Returns a URL for the image to draw. Subclasses can override 
	 * this class to perform custom image selection.
	 * @param item the item for which to select an image to draw
	 * @return an <code>Image</code> to draw
	 */
	protected String getImageLocation(VisualItem item) {
		return item.getAttribute(m_imageName);
	} //
	
	protected Image getImage(VisualItem item) {
		String imageLoc = getImageLocation(item);
		return ( imageLoc == null ? null : m_images.getImage(imageLoc) );
	} //

	/**
	 * @see edu.berkeley.guir.prefuse.render.ShapeRenderer#getRawShape(edu.berkeley.guir.prefuse.VisualItem)
	 */
	protected Shape getRawShape(VisualItem item) {
		if ( m_g == null ) { return null; }
		
        double size = item.getSize();
        
		// get image dimensions
		Image img = getImage(item);
		double ih = (img==null ? 0 : size*img.getHeight(null));
		double iw = (img==null ? 0 : size*img.getWidth(null));
		
		// get text dimensions
		m_font = item.getFont();
        if ( size != 1 )
          m_font = FontLib.getFont(m_font.getName(), m_font.getStyle(),
                      (int)Math.round(size*m_font.getSize()));
        
		String s = getText(item);
		if ( s == null ) { s = ""; }
		
        FontMetrics fm = m_g.getFontMetrics(m_font);
		int th = fm.getHeight();
		int tw = fm.stringWidth(s);
		
		double w = tw + iw + 
                size*(2*m_horizBorder + (tw>0 && iw>0 ? m_imageMargin : 0));
		double h = Math.max(th, ih) + size*2*m_vertBorder;
		
		getAlignedPoint(m_tmpPoint, item, w, h, m_xAlign, m_yAlign);
		m_imageBox.setRect(m_tmpPoint.getX(),m_tmpPoint.getY(),w,h);
		return m_imageBox;
	} //
	
	/**
	 * Helper method, which calculates the top-left co-ordinate of a node
	 * given the node's alignment.
	 */
	protected static void getAlignedPoint(Point2D p, VisualItem item, 
            double w, double h, int xAlign, int yAlign)
    {
		double x = item.getX(), y = item.getY();
		if ( xAlign == ALIGNMENT_CENTER ) {
			x = x-(w/2);
		} else if ( xAlign == ALIGNMENT_RIGHT ) {
			x = x-w;
		}
		if ( yAlign == ALIGNMENT_CENTER ) {
			y = y-(h/2);
		} else if ( yAlign == ALIGNMENT_BOTTOM ) {
			y = y-h;
		}
		p.setLocation(x,y);
	} //

	/**
	 * @see edu.berkeley.guir.prefuse.render.Renderer#render(java.awt.Graphics2D, edu.berkeley.guir.prefuse.VisualItem)
	 */
	public void render(Graphics2D g, VisualItem item) {
		Paint fillColor = item.getFillColor();
		Paint itemColor = item.getColor();
		Shape shape = getShape(item);
		if (shape != null) {
			switch (getRenderType()) {
				case RENDER_TYPE_DRAW :
					g.setPaint(itemColor);
					g.draw(shape);
					break;
				case RENDER_TYPE_FILL :
					g.setPaint(fillColor);
					g.fill(shape);
					break;
				case RENDER_TYPE_DRAW_AND_FILL :
					g.setPaint(fillColor);
					g.fill(shape);
					g.setPaint(itemColor);
					g.draw(shape);
					break;
			}

			String s = getText(item);
			Image img = getImage(item);
			if ( s == null && img == null )
				return;
						
			Rectangle r = shape.getBounds();
            double size = item.getSize();
			double x = size*(r.x + m_horizBorder);
			
			if ( img != null ) {
				Composite comp = g.getComposite();
				if ( itemColor != null && fillColor instanceof Color) {
					int alpha = ((Color)fillColor).getAlpha();
					if ( alpha < 255 ) {
						AlphaComposite alphaComp = 
							AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 
								((float)alpha)/255);
						g.setComposite(alphaComp);
					}
				}
                
                double w = size*img.getWidth(null);
                double h = size*img.getHeight(null);
                double y = r.y + (r.height-h)/2;
                
                int ix = (int)Math.round(x), iy = (int)Math.round(y);
                if ( size == 1 )
                    g.drawImage(img, ix, iy, null);
                else
                    g.drawImage(img, ix, iy, (int)Math.round(w), 
                            (int)Math.round(h), null);
				x += w + (s!=null ? size*m_imageMargin : 0);
				g.setComposite(comp);
			}
			if ( s != null ) {
				g.setPaint(itemColor);
				g.setFont(m_font);
				FontMetrics fm = m_g.getFontMetrics(m_font);
				int y = r.y + (r.height-fm.getHeight())/2+fm.getAscent();
				g.drawString(s, Math.round(x), y);
			}
		}
	} //
	
	/**
	 * Get the horizontal alignment of this node with respect to it's
	 * location co-ordinate.
	 * @return the horizontal alignment
	 */
	public int getHorizontalAlignment() {
		return m_xAlign;
	} //
	
	/**
	 * Get the vertical alignment of this node with respect to it's
	 * location co-ordinate.
	 * @return the vertical alignment
	 */
	public int getVerticalAlignment() {
		return m_yAlign;
	} //
	
	/**
	 * Set the horizontal alignment of this node with respect to it's
	 * location co-ordinate.
	 * @param align the horizontal alignment
	 */	
	public void setHorizontalAlignment(int align) {
		m_xAlign = align;
	} //
	
	/**
	 * Set the vertical alignment of this node with respect to it's
	 * location co-ordinate.
	 * @param align the vertical alignment
	 */	
	public void setVerticalAlignment(int align) {
		m_yAlign = align;
	} //
	
} // end of class TextImageItemRenderer
