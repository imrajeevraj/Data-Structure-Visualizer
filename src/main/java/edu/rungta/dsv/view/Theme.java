package edu.rungta.dsv.view;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import java.awt.*;

public final class Theme {
    private Theme() {}
    public static final Color BG = new Color(13,19,30), PANEL = new Color(20,29,43), CARD = new Color(26,38,54),
            LINE = new Color(44,60,79), TEXT = new Color(233,240,246), MUTED = new Color(148,167,186),
            ACCENT = new Color(94,230,189), BLUE = new Color(121,175,255), ORANGE = new Color(255,190,112);
    public static Font font(int style, int size) { return new Font("Segoe UI",style,size); }
    public static void install() {
        UIManager.put("Panel.background", BG); UIManager.put("Label.foreground", TEXT);
        UIManager.put("Label.font",font(Font.PLAIN,14)); UIManager.put("Button.font",font(Font.BOLD,13));
        UIManager.put("ComboBox.background", CARD); UIManager.put("ComboBox.foreground", TEXT);
        UIManager.put("ComboBox.selectionBackground", LINE); UIManager.put("ComboBox.selectionForeground", TEXT);
        UIManager.put("ComboBox.disabledBackground", CARD); UIManager.put("ComboBox.disabledForeground", MUTED);
        UIManager.put("ComboBox.font",font(Font.PLAIN,14)); UIManager.put("TextField.caretForeground",ACCENT);
        UIManager.put("ToolTip.background",CARD); UIManager.put("ToolTip.foreground",TEXT);
        UIManager.put("ScrollBar.width",12);
    }
    public static JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text); label.setFont(font(Font.PLAIN,size)); label.setForeground(color); return label;
    }
    public static JButton button(String text, boolean primary) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(!isEnabled() ? PANEL : primary ? ACCENT : getModel().isRollover() ? LINE : CARD);
                g.fillRoundRect(0,0,getWidth(),getHeight(),12,12); g.dispose(); super.paintComponent(graphics);
            }
        };
        b.setForeground(primary ? BG : TEXT); b.setContentAreaFilled(false); b.setFocusPainted(true);
        b.setBorder(new EmptyBorder(10,15,10,15)); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    public static JTextField input(String value, int columns) {
        JTextField f = new JTextField(value,columns); f.setFont(font(Font.PLAIN,15)); f.setForeground(TEXT); f.setBackground(CARD);
        f.setBorder(new CompoundBorder(new LineBorder(LINE,1,true),new EmptyBorder(9,10,9,10))); return f;
    }
    public static JPanel panel(LayoutManager layout, Color color) { JPanel p = new JPanel(layout); p.setBackground(color); return p; }
    public static JTextArea text(String value, int size, Color color) {
        JTextArea t = new JTextArea(value); t.setFont(font(Font.PLAIN,size)); t.setForeground(color); t.setOpaque(false);
        t.setEditable(false); t.setLineWrap(true); t.setWrapStyleWord(true); t.setFocusable(true); return t;
    }
    public static void combo(JComboBox<String> combo) {
        combo.setBackground(CARD); combo.setForeground(TEXT);combo.setBorder(new LineBorder(LINE));
        combo.setUI(new BasicComboBoxUI(){
            @Override protected JButton createArrowButton(){return new BasicArrowButton(SwingConstants.SOUTH,CARD,CARD,MUTED,CARD);}
        });
        combo.setRenderer(new DefaultListCellRenderer(){
            @Override public Component getListCellRendererComponent(JList<?> list,Object value,int index,boolean selected,boolean focused){
                JLabel label=(JLabel)super.getListCellRendererComponent(list,value,index,selected,focused);
                label.setBackground(selected?LINE:CARD);label.setForeground(TEXT);label.setBorder(new EmptyBorder(6,8,6,8));return label;
            }
        });
    }
    public static void scroll(JScrollPane pane) {
        for(JScrollBar bar:new JScrollBar[]{pane.getHorizontalScrollBar(),pane.getVerticalScrollBar()}){
            bar.setUI(new BasicScrollBarUI(){
                @Override protected void configureScrollBarColors(){thumbColor=LINE.brighter();trackColor=BG;}
                @Override protected JButton createDecreaseButton(int orientation){return zeroButton();}
                @Override protected JButton createIncreaseButton(int orientation){return zeroButton();}
                private JButton zeroButton(){JButton b=new JButton();b.setPreferredSize(new Dimension(0,0));return b;}
                @Override protected void paintThumb(Graphics g,JComponent c,Rectangle r){g.setColor(LINE.brighter());g.fillRoundRect(r.x+2,r.y+2,Math.max(2,r.width-4),Math.max(2,r.height-4),8,8);}
            });
        }
        pane.getViewport().setBackground(PANEL);pane.setBackground(PANEL);
    }
    public static void slider(JSlider slider) {
        slider.setUI(new BasicSliderUI(slider){
            @Override public void paintTrack(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();g.setColor(LINE);g.fillRoundRect(trackRect.x,trackRect.y+trackRect.height/2-2,trackRect.width,4,4,4);g.setColor(ACCENT);g.fillRoundRect(trackRect.x,trackRect.y+trackRect.height/2-2,Math.max(0,xPositionForValue(slider.getValue())-trackRect.x),4,4,4);g.dispose();}
            @Override public void paintThumb(Graphics graphics){graphics.setColor(slider.isEnabled()?ACCENT:MUTED);graphics.fillOval(thumbRect.x,thumbRect.y+thumbRect.height/2-6,12,12);}
        });
    }
}
