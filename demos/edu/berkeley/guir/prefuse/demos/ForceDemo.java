package edu.berkeley.guir.prefuse.demos;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

import edu.berkeley.guir.prefuse.Display;
import edu.berkeley.guir.prefuse.EdgeItem;
import edu.berkeley.guir.prefuse.ItemRegistry;
import edu.berkeley.guir.prefuse.NodeItem;
import edu.berkeley.guir.prefuse.VisualItem;
import edu.berkeley.guir.prefuse.action.ColorFunction;
import edu.berkeley.guir.prefuse.action.GraphEdgeFilter;
import edu.berkeley.guir.prefuse.action.GraphNodeFilter;
import edu.berkeley.guir.prefuse.action.RepaintAction;
import edu.berkeley.guir.prefuse.activity.ActionPipeline;
import edu.berkeley.guir.prefuse.activity.Activity;
import edu.berkeley.guir.prefuse.event.ControlAdapter;
import edu.berkeley.guir.prefuse.graph.Graph;
import edu.berkeley.guir.prefuse.graph.GraphLib;
import edu.berkeley.guir.prefuse.render.DefaultEdgeRenderer;
import edu.berkeley.guir.prefuse.render.DefaultNodeRenderer;
import edu.berkeley.guir.prefuse.render.DefaultRendererFactory;
import edu.berkeley.guir.prefuse.render.TextItemRenderer;
import edu.berkeley.guir.prefusex.controls.DragControl;
import edu.berkeley.guir.prefusex.controls.NeighborHighlightControl;
import edu.berkeley.guir.prefusex.controls.PanControl;
import edu.berkeley.guir.prefusex.controls.ZoomControl;
import edu.berkeley.guir.prefusex.force.DragForce;
import edu.berkeley.guir.prefusex.force.ForcePanel;
import edu.berkeley.guir.prefusex.force.ForceSimulator;
import edu.berkeley.guir.prefusex.force.NBodyForce;
import edu.berkeley.guir.prefusex.force.SpringForce;
import edu.berkeley.guir.prefusex.layout.ForceDirectedLayout;

/**
 * Application demo of a graph visualization using an interactive
 * force-based layout.
 *
 * @version 1.0
 * @author <a href="http://jheer.org">Jeffrey Heer</a> prefuse(AT)jheer.org
 */
public class ForceDemo extends Display {

    private JFrame     frame;
    private ForcePanel fpanel;
    
    private ForceSimulator m_fsim;
    private String         m_textField;
    private ItemRegistry   m_registry;
    private Activity       m_pipeline;
    
    private Font frameCountFont = new Font("SansSerif", Font.PLAIN, 14);
    
    public ForceDemo(Graph g, ForceSimulator fsim) {
        this(g, fsim, "label");
    } //
    
    public ForceDemo(Graph g, ForceSimulator fsim, String textField) {
        // set up component first
        m_fsim = fsim;
        m_textField = textField;
        m_registry = new ItemRegistry(g);
        this.setItemRegistry(m_registry);
        initRenderers();
        m_pipeline = initPipeline();
        setSize(700,700);
        pan(350,350);
        this.addControlListener(new NeighborHighlightControl());
        this.addControlListener(new DragControl(false));
        this.addControlListener(new MouseOverControl());
        this.addControlListener(new PanControl(false));
        this.addControlListener(new ZoomControl(false));
    } //
    
    public void runDemo() {
        // now set up application window
        fpanel = new ForcePanel(m_fsim);
        
        frame = new JFrame("Force Simulator Demo");
        Container c = frame.getContentPane();
        c.setLayout(new BorderLayout());
        c.add(this, BorderLayout.CENTER);
        c.add(fpanel, BorderLayout.EAST);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                Dimension d = frame.getSize();
                Dimension p = fpanel.getSize();
                Insets in = frame.getInsets();
                ForceDemo.this.setSize(d.width-in.left-in.right-p.width,
                        d.height-in.top-in.bottom);
            } //
            
        });
        frame.pack();
        frame.setVisible(true);
        
        // start force simulation
        m_pipeline.runNow();
    } //
    
    private void initRenderers() {
        TextItemRenderer    nodeRenderer = new TextItemRenderer();
        nodeRenderer.setRenderType(TextItemRenderer.RENDER_TYPE_FILL);
        nodeRenderer.setRoundedCorner(8,8);
        nodeRenderer.setTextAttributeName(m_textField);
        DefaultNodeRenderer nRenderer = new DefaultNodeRenderer();
        DefaultEdgeRenderer edgeRenderer = new DefaultEdgeRenderer();    
        m_registry.setRendererFactory(new DefaultRendererFactory(
                nodeRenderer, edgeRenderer, null));
    } //
    
    private ActionPipeline initPipeline() {
        ActionPipeline pipeline = new ActionPipeline(m_registry,-1,20);
        pipeline.add(new GraphNodeFilter());
        pipeline.add(new GraphEdgeFilter());
        pipeline.add(new ForceDirectedLayout(m_fsim, false));
        pipeline.add(new DemoColorFunction());
        pipeline.add(new RepaintAction());
        return pipeline;
    } //
    
    public static void main(String argv[]) {
        String file = (argv.length==0 ? "etc/friendster.xml" : argv[0]);
        //String file = "../prefuse/etc/terror.xml";
        //Graph g;
        //try {
        //    g = (new XMLGraphReader()).loadGraph(file);
        //} catch ( Exception e ) { e.printStackTrace(); return; }
        
        Graph g = GraphLib.getGrid(15,15);
        
        System.out.println("Visualizing Graph: "
            +g.getNodeCount()+" nodes, "+g.getEdgeCount()+" edges");
        
        ForceSimulator fsim = new ForceSimulator();
        fsim.addForce(new NBodyForce(-0.4f, 0.9f));
        fsim.addForce(new SpringForce(4E-5f, 75f));
        fsim.addForce(new DragForce(-0.005f));
        
        ForceDemo fdemo = new ForceDemo(g, fsim);
        fdemo.runDemo();
    } //

    public class DemoColorFunction extends ColorFunction {
        private Color pastelRed = new Color(255,125,125);
        private Color pastelOrange = new Color(255,200,125);
        private Color lightGray = new Color(220,220,255);
        public Paint getColor(VisualItem item) {
            if ( item instanceof EdgeItem ) {
                Boolean h = (Boolean)item.getVizAttribute("highlight");
                if ( h != null && h.booleanValue() )
                    return pastelOrange;
                else
                    return Color.LIGHT_GRAY;
            } else {
                return Color.BLACK;
            }
        } //
        public Paint getFillColor(VisualItem item) {
            Boolean h = (Boolean)item.getVizAttribute("highlight");
            if ( h != null && h.booleanValue() )
                return pastelOrange;
            else if ( item instanceof NodeItem ) {
                if ( item.isFixed() )
                    return pastelRed;
                else
                    return lightGray;
            } else {
                return Color.BLACK;
            }
        } //        
    } //
    
    /**
     * Tags and fixes the node under the mouse pointer.
     */
    public class MouseOverControl extends ControlAdapter {
        
        public void itemEntered(VisualItem item, MouseEvent e) {
            ((Display)e.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            item.setFixed(true);
        } //
        
        public void itemExited(VisualItem item, MouseEvent e) {
            ((Display)e.getSource()).setCursor(Cursor.getDefaultCursor());
            item.setFixed(false);
        } //
        
        public void itemReleased(VisualItem item, MouseEvent e) {
            item.setFixed(false);
        } //
        
    } // end of inner class FocusControl
    
} // end of class ForceDemo
