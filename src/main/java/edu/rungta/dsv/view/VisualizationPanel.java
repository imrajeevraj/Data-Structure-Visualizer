package edu.rungta.dsv.view;

import edu.rungta.dsv.model.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/** Inorder-rank tree layout keeps every node distinct, even for a skewed 30-node BST. */
public final class VisualizationPanel extends JPanel implements Scrollable {
    private StepSnapshot step;
    private double zoom = 1;
    private boolean autoFit = true;
    private final Map<Integer, Point2D.Double> positions = new HashMap<>();
    private final Map<Integer, NodeView> byId = new HashMap<>();
    private int baseWidth = 720, baseHeight = 440;
    public VisualizationPanel() { setBackground(Theme.BG); setToolTipText("Linear/BST nodes show stable IDs; AVL nodes show height and balance."); }
    public StepSnapshot current() { return step; }
    public void setZoom(double zoom) { autoFit=false;this.zoom = zoom; updateSize(); }
    public void fit() {autoFit=true;updateSize();}
    public double zoom() {
        if(autoFit && getParent() instanceof JViewport viewport){Dimension d=viewport.getExtentSize();return Math.max(.65,Math.min(1,Math.min((d.width-12.0)/baseWidth,(d.height-12.0)/baseHeight)));}
        return zoom;
    }
    public void showStep(StepSnapshot step) { this.step = step; layoutNodes(); updateSize(); repaint(); }
    private void updateSize() { setPreferredSize(new Dimension((int)(baseWidth*zoom),(int)(baseHeight*zoom))); revalidate(); repaint(); }
    @Override public Dimension getPreferredSize(){double z=zoom();return new Dimension((int)(baseWidth*z),(int)(baseHeight*z));}
    @Override public Dimension getPreferredScrollableViewportSize(){return getPreferredSize();}
    @Override public int getScrollableUnitIncrement(Rectangle r,int orientation,int direction){return 24;}
    @Override public int getScrollableBlockIncrement(Rectangle r,int orientation,int direction){return orientation==SwingConstants.VERTICAL?r.height-24:r.width-24;}
    @Override public boolean getScrollableTracksViewportWidth(){return getParent() instanceof JViewport v&&getPreferredSize().width<v.getWidth();}
    @Override public boolean getScrollableTracksViewportHeight(){return getParent() instanceof JViewport v&&getPreferredSize().height<v.getHeight();}
    private void layoutNodes() {
        positions.clear(); byId.clear(); baseWidth = 650; baseHeight = 300;
        if (step == null || step.nodes().isEmpty()) return;
        for (NodeView n : step.nodes()) byId.put(n.id(),n);
        if (step.type().isTree()) {
            int[] rank = {0}; treeLayout(step.rootId(),0,rank);
            baseWidth = Math.max(650,step.nodes().size()*100+100);
            double maxY = positions.values().stream().mapToDouble(p -> p.y).max().orElse(100);
            baseHeight = Math.max(300,(int)maxY+90);
        } else if (step.type() == StructureType.STACK) {
            for (int i=0;i<step.nodes().size();i++) positions.put(step.nodes().get(i).id(),new Point2D.Double(325,75+i*100));
            baseHeight = Math.max(300,step.nodes().size()*100+65);
        } else {
            int columns = 5;
            for (int i=0;i<step.nodes().size();i++) positions.put(step.nodes().get(i).id(),new Point2D.Double(96+(i%columns)*135,125+(i/columns)*145));
            baseWidth = 770; baseHeight = Math.max(440,((step.nodes().size()-1)/columns+1)*145+100);
        }
    }
    private void treeLayout(int id, int depth, int[] rank) {
        if (id == 0) return; NodeView n = byId.get(id);
        treeLayout(n.leftId(),depth+1,rank);
        positions.put(id,new Point2D.Double(100+(rank[0]++)*100,75+depth*105));
        treeLayout(n.rightId(),depth+1,rank);
    }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics); Graphics2D g = (Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(28,40,56)); for(int x=20;x<getWidth();x+=24) for(int y=20;y<getHeight();y+=24) g.fillOval(x,y,2,2);
        double zoom=zoom();g.scale(zoom,zoom);
        if (step == null || step.nodes().isEmpty()) {
            int cx = (int)(getWidth()/zoom/2), cy = (int)(getHeight()/zoom/2);
            g.setColor(Theme.CARD); g.fillRoundRect(cx-40,cy-85,80,65,18,18);
            g.setColor(Theme.ACCENT); g.setFont(Theme.font(Font.BOLD,30)); centered(g,"+",cx,cy-43);
            g.setColor(Theme.TEXT); g.setFont(Theme.font(Font.BOLD,22)); centered(g,"Every structure starts with one node.",cx,cy+18);
            g.setColor(Theme.MUTED); g.setFont(Theme.font(Font.PLAIN,14)); centered(g,"Enter a value above, or load a guided example.",cx,cy+48);
            g.dispose(); return;
        }
        double offset = Math.max(0,(getWidth()/zoom-baseWidth)/2);
        g.translate(offset,0);
        for(NodeView n:step.nodes()) {
            if(step.type().isTree()) { edge(g,n.id(),n.leftId(),"L"); edge(g,n.id(),n.rightId(),"R"); }
            else edge(g,n.id(),n.nextId(),"");
        }
        for(int i=0;i<step.nodes().size();i++) drawNode(g,step.nodes().get(i),i);
        g.dispose();
    }
    private void edge(Graphics2D g,int from,int to,String label) {
        if(to==0) return;
        Point2D.Double a=positions.get(from),b=positions.get(to);
        boolean wrapped = !step.type().isTree() && step.type()!=StructureType.STACK && b.y>a.y;
        g.setColor(Theme.LINE.brighter()); g.setStroke(new BasicStroke(1.8f));
        double ex,ey,angle;
        if(wrapped) {
            double routeY=a.y+73;
            Path2D path=new Path2D.Double(); path.moveTo(a.x,a.y+30);path.lineTo(a.x,routeY);path.lineTo(b.x,routeY);path.lineTo(b.x,b.y-34);
            g.draw(path);ex=b.x;ey=b.y-34;angle=Math.PI/2;
        } else {
            double dx=b.x-a.x,dy=b.y-a.y,d=Math.hypot(dx,dy);angle=Math.atan2(dy,dx);
            ex=b.x-dx/d*35;ey=b.y-dy/d*35;
            g.draw(new Line2D.Double(a.x+dx/d*34,a.y+dy/d*34,ex,ey));
        }
        Path2D arrow=new Path2D.Double();arrow.moveTo(ex,ey);arrow.lineTo(ex-9*Math.cos(angle-.45),ey-9*Math.sin(angle-.45));arrow.lineTo(ex-9*Math.cos(angle+.45),ey-9*Math.sin(angle+.45));arrow.closePath();g.fill(arrow);
        if(!label.isEmpty()) {g.setFont(Theme.font(Font.BOLD,10));g.setColor(Theme.MUTED);g.drawString(label,(float)((a.x+b.x)/2+7),(float)((a.y+b.y)/2-5));}
    }
    private void drawNode(Graphics2D g,NodeView n,int index) {
        Point2D.Double p=positions.get(n.id());int x=(int)p.x,y=(int)p.y;
        boolean active=step.highlights().contains(n.id());
        if(active) {g.setColor(new Color(94,230,189,24));g.fillOval(x-43,y-43,86,86);}
        g.setColor(active?new Color(30,69,65):Theme.CARD);g.fillRoundRect(x-34,y-30,68,60,18,18);
        g.setColor(active?Theme.ACCENT:Theme.LINE.brighter());g.setStroke(new BasicStroke(active?2.4f:1.4f));g.drawRoundRect(x-34,y-30,68,60,18,18);
        String value=Integer.toString(n.value());g.setFont(Theme.font(Font.BOLD,value.length()>7?10:value.length()>4?14:22));g.setColor(Theme.TEXT);centered(g,value,x,y+7);
        g.setFont(Theme.font(Font.PLAIN,10));g.setColor(Theme.MUTED);
        String meta=step.type()==StructureType.AVL?"h "+n.height()+"  |  bf "+(n.balance()>0?"+":"")+n.balance():"id "+n.id()+(step.type().isTree()?"":"  |  index "+index);
        if(step.type()==StructureType.STACK)g.drawString(meta,x+48,y+4);else centered(g,meta,x,y+48);
        String tag="";
        if(step.type().isTree() && n.id()==step.rootId()) tag="ROOT";
        else if(step.type()==StructureType.STACK && index==0) tag="TOP / LIFO";
        else if(step.type()==StructureType.QUEUE) tag=index==0?(step.nodes().size()==1?"FRONT + REAR":"FRONT"):index==step.nodes().size()-1?"REAR":"";
        else if(step.type()==StructureType.LINKED_LIST) tag=index==0?(step.nodes().size()==1?"HEAD + TAIL":"HEAD"):index==step.nodes().size()-1?"TAIL":"";
        g.setFont(Theme.font(Font.BOLD,10));g.setColor(Theme.ACCENT);centered(g,tag,x,y-44);
        if(active) {g.setColor(Theme.ACCENT);g.fillOval(x+26,y-33,8,8);}
        if(!step.type().isTree() && n.nextId()==0) {g.setFont(Theme.font(Font.PLAIN,10));g.setColor(Theme.MUTED);if(step.type()==StructureType.STACK)centered(g,"next: null",x,y+48);else g.drawString("next: null",x+42,y+5);}
    }
    private void centered(Graphics2D g,String s,int x,int y) {g.drawString(s,x-g.getFontMetrics().stringWidth(s)/2,y);}
    public BufferedImage exportImage() {
        Dimension old=getSize(); double oldZoom=zoom;boolean oldFit=autoFit;
        autoFit=false;zoom=1;setSize(baseWidth,baseHeight);
        List<String> lines=new ArrayList<>();FontMetrics metrics=getFontMetrics(Theme.font(Font.PLAIN,12));
        String description=step==null?"":step.phase()+" - "+step.explanation(),line="";
        for(String word:description.split(" ")){String candidate=line.isEmpty()?word:line+" "+word;if(metrics.stringWidth(candidate)>baseWidth-48&&!line.isEmpty()){lines.add(line);line=word;}else line=candidate;}
        lines.add(line);int headerHeight=48+lines.size()*16;
        BufferedImage image=new BufferedImage(baseWidth,baseHeight+headerHeight,BufferedImage.TYPE_INT_RGB);
        Graphics2D g=image.createGraphics();g.setColor(Theme.PANEL);g.fillRect(0,0,image.getWidth(),image.getHeight());
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Theme.TEXT);g.setFont(Theme.font(Font.BOLD,18));g.drawString("STRUCTURA  /  "+(step==null?"Data Structure Visualizer":step.type().title),24,29);
        g.setColor(Theme.MUTED);g.setFont(Theme.font(Font.PLAIN,12));for(int i=0;i<lines.size();i++)g.drawString(lines.get(i),24,49+i*16);
        g.translate(0,headerHeight);printAll(g);g.dispose();zoom=oldZoom;autoFit=oldFit;setSize(old);return image;
    }
}
