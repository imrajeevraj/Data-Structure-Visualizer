package edu.rungta.dsv.view;

import edu.rungta.dsv.animation.AnimationPlayer;
import edu.rungta.dsv.controller.Controller;
import edu.rungta.dsv.model.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

/** Entire workspace is a JPanel, making layout and actions testable without a desktop. */
public final class WorkspacePanel extends JPanel {
    private final Controller controller = new Controller();
    private final VisualizationPanel canvas = new VisualizationPanel();
    private final AnimationPlayer player;
    private final Map<StructureType,JButton> navigation = new EnumMap<>(StructureType.class);
    private final JLabel title = Theme.label("",30,Theme.TEXT), subtitle = Theme.label("",14,Theme.MUTED);
    private final JLabel structureBadge = Theme.label("",12,Theme.ACCENT), sizeStat = Theme.label("0",24,Theme.TEXT), heightStat = Theme.label("—",24,Theme.TEXT);
    private final JLabel phase = Theme.label("READY",12,Theme.ACCENT), stepCount = Theme.label("No operation yet",12,Theme.MUTED);
    private final JLabel complexity = Theme.label("—",19,Theme.ORANGE), timelineLabel = Theme.label("Ready to explore",12,Theme.MUTED);
    private final JTextArea explanation = Theme.text("",15,Theme.TEXT), output = Theme.text("—",14,Theme.ACCENT), algorithm = Theme.text("",13,Theme.MUTED);
    private final JTextArea status = Theme.text("Welcome. Select a structure, enter a value and run an operation.",13,Theme.MUTED);
    private final JTextArea history = Theme.text("Your operations will appear here.",12,Theme.MUTED);
    private final List<String> historyEntries = new ArrayList<>();
    private final JComboBox<String> operation = new JComboBox<>(), examples = new JComboBox<>();
    private final JTextField valueInput = Theme.input("25",7), indexInput = Theme.input("0",3);
    private final JPanel valueGroup, indexGroup;
    private final JButton run = Theme.button("Run operation  →",true), demo = Theme.button("Load example",false),
            play = Theme.button("Play",true), next = Theme.button("Next →",false), previous = Theme.button("← Back",false),
            finish = Theme.button("Go to end",false), reset = Theme.button("Reset",false);
    private final JSlider timeline = new JSlider(0,0,0), speed = new JSlider(1,5,3);
    private final JLabel speedLabel = Theme.label("0.7 s / step",12,Theme.MUTED);
    private boolean syncing;
    private int quizIndex, quizScore;

    public WorkspacePanel() {
        super(new BorderLayout()); setBackground(Theme.BG);
        player = new AnimationPlayer(this::showStep,this::updatePlayback);
        operation.setName("operation"); valueInput.setName("value"); indexInput.setName("index"); run.setName("run");reset.setName("reset");
        play.setName("play");next.setName("next");previous.setName("previous");finish.setName("finish");demo.setName("demo");examples.setName("examples");timeline.setName("timeline");
        add(sidebar(),BorderLayout.WEST);
        JPanel content = Theme.panel(new BorderLayout(0,14),Theme.BG); content.setBorder(new EmptyBorder(18,24,12,24));
        add(content,BorderLayout.CENTER);
        JPanel top = Theme.panel(new BorderLayout(0,14),Theme.BG);
        JPanel header = Theme.panel(new BorderLayout(),Theme.BG);
        JPanel heading = Theme.panel(new BorderLayout(0,5),Theme.BG);heading.add(title,BorderLayout.NORTH);heading.add(subtitle,BorderLayout.SOUTH);header.add(heading,BorderLayout.CENTER);
        JPanel actions = Theme.panel(new FlowLayout(FlowLayout.RIGHT,8,4),Theme.BG);
        JButton export = Theme.button("Export PNG",false);export.addActionListener(e -> exportPng());actions.add(export);actions.add(reset);header.add(actions,BorderLayout.EAST);
        top.add(header,BorderLayout.NORTH);
        JPanel controls = Theme.panel(new FlowLayout(FlowLayout.LEFT,12,9),Theme.PANEL);controls.setBorder(new LineBorder(Theme.LINE,1,true));
        Theme.combo(operation);Theme.combo(examples);
        operation.setPreferredSize(new Dimension(158,39));controls.add(field("OPERATION",operation));
        valueGroup=field("INTEGER VALUE",valueInput);controls.add(valueGroup);
        indexGroup=field("INDEX (0-BASED)",indexInput);controls.add(indexGroup);
        JPanel runWrap=field(" ",run);controls.add(runWrap);top.add(controls,BorderLayout.CENTER);
        content.add(top,BorderLayout.NORTH);

        JPanel center=Theme.panel(new BorderLayout(18,0),Theme.BG);
        JPanel stage=Theme.panel(new BorderLayout(),Theme.PANEL);stage.setBorder(new LineBorder(Theme.LINE,1,true));
        JPanel stageHeader=Theme.panel(new BorderLayout(),Theme.PANEL);stageHeader.setBorder(new EmptyBorder(6,16,6,12));
        stageHeader.add(structureBadge,BorderLayout.WEST);
        JPanel zoomTools=Theme.panel(new FlowLayout(FlowLayout.RIGHT,5,0),Theme.PANEL);
        zoomTools.add(Theme.label("●  Active / visited",11,Theme.ACCENT));
        JButton minus=Theme.button("−",false),plus=Theme.button("+",false),normal=Theme.button("Fit",false);
        minus.setToolTipText("Zoom out");plus.setToolTipText("Zoom in");normal.setToolTipText("Reset zoom");
        minus.addActionListener(e -> canvas.setZoom(Math.max(.5,canvas.zoom()-.1)));plus.addActionListener(e -> canvas.setZoom(Math.min(1.8,canvas.zoom()+.1)));normal.addActionListener(e -> canvas.fit());
        zoomTools.add(minus);zoomTools.add(normal);zoomTools.add(plus);stageHeader.add(zoomTools,BorderLayout.EAST);
        stage.add(stageHeader,BorderLayout.NORTH);
        JScrollPane drawing = new JScrollPane(canvas);drawing.setBorder(new MatteBorder(1,0,1,0,Theme.LINE));drawing.getViewport().setBackground(Theme.BG);
        Theme.scroll(drawing);drawing.getHorizontalScrollBar().setUnitIncrement(24);drawing.getVerticalScrollBar().setUnitIncrement(24);stage.add(drawing,BorderLayout.CENTER);
        JPanel exampleBar=Theme.panel(new FlowLayout(FlowLayout.LEFT,10,7),Theme.PANEL);
        exampleBar.add(Theme.label("TRY AN EXAMPLE",10,Theme.MUTED));examples.setPreferredSize(new Dimension(150,34));exampleBar.add(examples);exampleBar.add(demo);
        stage.add(exampleBar,BorderLayout.SOUTH);center.add(stage,BorderLayout.CENTER);
        center.add(inspector(),BorderLayout.EAST);content.add(center,BorderLayout.CENTER);

        JPanel bottom=Theme.panel(new BorderLayout(0,10),Theme.BG);
        JPanel playerCard=Theme.panel(new BorderLayout(14,8),Theme.PANEL);playerCard.setBorder(new CompoundBorder(new LineBorder(Theme.LINE,1,true),new EmptyBorder(12,14,12,14)));
        JPanel track=Theme.panel(new BorderLayout(12,0),Theme.PANEL);timeline.setOpaque(false);timeline.setFocusable(true);track.add(timeline,BorderLayout.CENTER);track.add(timelineLabel,BorderLayout.EAST);playerCard.add(track,BorderLayout.NORTH);
        JPanel transport=Theme.panel(new FlowLayout(FlowLayout.LEFT,8,0),Theme.PANEL);transport.add(previous);transport.add(play);transport.add(next);transport.add(finish);playerCard.add(transport,BorderLayout.WEST);
        JPanel tempo=Theme.panel(new FlowLayout(FlowLayout.RIGHT,8,0),Theme.PANEL);speed.setOpaque(false);speed.setPreferredSize(new Dimension(100,35));speed.setToolTipText("Animation speed: slow to fast");tempo.add(speedLabel);tempo.add(speed);playerCard.add(tempo,BorderLayout.EAST);
        Theme.slider(timeline);Theme.slider(speed);
        bottom.add(playerCard,BorderLayout.CENTER);status.setRows(2);bottom.add(status,BorderLayout.SOUTH);content.add(bottom,BorderLayout.SOUTH);
        bindActions();select(StructureType.AVL);
    }
    private JPanel field(String label,JComponent component) {
        JPanel p=Theme.panel(new BorderLayout(0,6),Theme.PANEL);p.add(Theme.label(label,10,Theme.MUTED),BorderLayout.NORTH);p.add(component,BorderLayout.CENTER);return p;
    }
    private JPanel sidebar() {
        JPanel side=Theme.panel(new BorderLayout(),Theme.PANEL);side.setPreferredSize(new Dimension(208,600));side.setBorder(new CompoundBorder(new MatteBorder(0,0,0,1,Theme.LINE),new EmptyBorder(27,18,22,18)));
        JPanel top=Theme.panel(new BorderLayout(0,30),Theme.PANEL);
        JPanel brand=Theme.panel(new GridLayout(3,1,0,4),Theme.PANEL);
        JLabel logo=Theme.label("S / STRUCTURA",20,Theme.ACCENT);logo.setFont(Theme.font(Font.BOLD,20));brand.add(logo);brand.add(Theme.label("Data Structure Visualizer",11,Theme.MUTED));brand.add(Theme.label("INTERACTIVE LEARNING LAB",9,Theme.MUTED));top.add(brand,BorderLayout.NORTH);
        JPanel nav=Theme.panel(new GridLayout(9,1,0,6),Theme.PANEL);nav.add(Theme.label("01  /  LINEAR STRUCTURES",10,Theme.MUTED));
        for(StructureType type:StructureType.values()) {
            if(type==StructureType.BST) nav.add(Theme.label("02  /  SEARCH TREES",10,Theme.MUTED));
            String symbol=String.format("%02d",type.ordinal()+1);
            JButton button=Theme.button(symbol+"   "+(type==StructureType.BST?"BST":type.title),false);button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setName("nav-"+type.name());button.addActionListener(e -> select(type));navigation.put(type,button);nav.add(button);
        }
        JButton learn=Theme.button("Quick reference",false);learn.addActionListener(e -> showReference());nav.add(learn);
        JButton quiz=Theme.button("Practice / viva",false);quiz.addActionListener(e -> practice());nav.add(quiz);top.add(nav,BorderLayout.CENTER);side.add(top,BorderLayout.NORTH);
        JPanel footer=Theme.panel(new GridLayout(5,1,0,4),Theme.PANEL);footer.add(Theme.label("●  OFFLINE & READY",10,Theme.ACCENT));footer.add(Theme.label("Java 17+  ·  Swing",11,Theme.MUTED));footer.add(Theme.label("Built for curious minds.",11,Theme.MUTED));footer.add(Theme.label("Rajeev Ranjan",12,Theme.TEXT));footer.add(Theme.label("RU-25-11103  /  2026–27",10,Theme.MUTED));side.add(footer,BorderLayout.SOUTH);return side;
    }
    private JComponent inspector() {
        JPanel panel=Theme.panel(new BorderLayout(0,16),Theme.PANEL);panel.setBorder(new EmptyBorder(18,18,18,18));panel.setPreferredSize(new Dimension(267,400));
        JPanel stats=Theme.panel(new GridLayout(1,2,15,0),Theme.PANEL);stats.add(stat("NODES / 30",sizeStat));stats.add(stat("HEIGHT",heightStat));panel.add(stats,BorderLayout.NORTH);
        JPanel info=Theme.panel(new GridBagLayout(),Theme.PANEL);GridBagConstraints c=new GridBagConstraints();c.gridx=0;c.weightx=1;c.fill=GridBagConstraints.HORIZONTAL;c.anchor=GridBagConstraints.NORTHWEST;
        int row=0;
        for(JComponent comp:new JComponent[]{Theme.label("STEP INSPECTOR",10,Theme.MUTED),phase,stepCount,explanation,Theme.label("OUTPUT",10,Theme.MUTED),output,Theme.label("THEORETICAL TIME",10,Theme.MUTED),complexity,Theme.label("ALGORITHM NOTES",10,Theme.MUTED),algorithm,Theme.label("RECENT OPERATIONS",10,Theme.MUTED),history}) {
            c.gridy=row++;c.insets=new Insets(0,0,comp==explanation||comp==output||comp==algorithm?18:8,0);info.add(comp,c);
        }
        explanation.setRows(3);output.setRows(1);algorithm.setRows(4);history.setRows(3);
        c.gridy=row;c.weighty=1;info.add(Theme.panel(new BorderLayout(),Theme.PANEL),c);
        JScrollPane scroll=new JScrollPane(info);Theme.scroll(scroll);scroll.setBorder(null);scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);scroll.getVerticalScrollBar().setUnitIncrement(16);panel.add(scroll,BorderLayout.CENTER);return panel;
    }
    private JPanel stat(String text,JLabel number) {JPanel p=Theme.panel(new BorderLayout(0,5),Theme.PANEL);p.add(Theme.label(text,10,Theme.MUTED),BorderLayout.NORTH);p.add(number,BorderLayout.CENTER);return p;}
    private void bindActions() {
        operation.addActionListener(e -> updateInputs());run.addActionListener(e -> execute());valueInput.addActionListener(e -> execute());indexInput.addActionListener(e -> execute());
        reset.addActionListener(e -> select(controller.model().type()));
        demo.addActionListener(e -> {if(!demo.isEnabled())return;loadTrace(controller.demo((String)examples.getSelectedItem()));});
        play.addActionListener(e -> {if(player.playing())player.pause();else player.play();});next.addActionListener(e -> player.next());previous.addActionListener(e -> player.previous());finish.addActionListener(e -> player.seek(player.total()-1));
        timeline.addChangeListener(e -> {if(!syncing)player.seek(timeline.getValue());});
        speed.addChangeListener(e -> {int delay=new int[]{1600,1100,700,400,160}[speed.getValue()-1];player.setDelay(delay);speedLabel.setText(String.format(Locale.ROOT,"%.2g s / step",delay/1000.0));});
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE,InputEvent.CTRL_DOWN_MASK),"play");
        getActionMap().put("play",new AbstractAction(){public void actionPerformed(ActionEvent e){play.doClick();}});
        getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,InputEvent.CTRL_DOWN_MASK),"next");
        getActionMap().put("next",new AbstractAction(){public void actionPerformed(ActionEvent e){next.doClick();}});
    }
    public void select(StructureType type) {
        player.cancel();controller.select(type);title.setText(type.title);subtitle.setText(type.subtitle);structureBadge.setText("LIVE CANVAS  /  "+type.title.toUpperCase(Locale.ROOT));
        for(var entry:navigation.entrySet())entry.getValue().setForeground(entry.getKey()==type?Theme.ACCENT:Theme.MUTED);
        operation.setModel(new DefaultComboBoxModel<>(type.operations()));
        examples.setModel(new DefaultComboBoxModel<>(type==StructureType.AVL?new String[]{"Balanced tree","LL rotation","RR rotation","LR rotation","RL rotation","Sorted input"}:type==StructureType.BST?new String[]{"Balanced tree","Sorted input"}:new String[]{"Starter sequence"}));
        historyEntries.clear();history.setText("Your operations will appear here.");complexity.setText("—");indexInput.setText("0");valueInput.setText("25");canvas.fit();showStep(controller.model().snapshot());updateInputs();updatePlayback();
        status.setForeground(Theme.MUTED);status.setText("Ready. "+type.subtitle+"  Load an example or enter your own values.");
    }
    private void execute() {
        if(!run.isEnabled())return;
        try {loadTrace(controller.execute((String)operation.getSelectedItem(),valueInput.getText(),indexInput.getText()));}
        catch(IllegalArgumentException e){status.setForeground(Theme.ORANGE);status.setText("Check your input: "+e.getMessage());}
    }
    public void loadTrace(OperationTrace trace) {
        complexity.setText(trace.complexity());
        historyEntries.add(0,trace.operation()+"  ·  "+controller.model().size()+" nodes");if(historyEntries.size()>5)historyEntries.remove(5);history.setText(String.join("\n",historyEntries));
        status.setForeground(Theme.MUTED);status.setText("Use Play or Next to explore. Go to end to begin another operation. Reset cancels this demonstration.");
        player.load(trace);player.play();
    }
    private void showStep(StepSnapshot step) {
        canvas.showStep(step);explanation.setText(step.explanation());explanation.setCaretPosition(0);phase.setText(step.phase());output.setText(step.output().isEmpty()?"—":step.output().toString());
        sizeStat.setText(Integer.toString(step.nodes().size()));
        heightStat.setText(step.type().isTree()?Integer.toString(step.nodes().stream().filter(n -> n.id()==step.rootId()).mapToInt(NodeView::height).findFirst().orElse(0)):"—");
        if(player.trace()!=null && player.index()==player.total()-1){status.setForeground(Theme.ACCENT);status.setText(player.trace().result());}
    }
    private void updatePlayback() {
        if(player==null)return;
        boolean locked=player.reviewing()||player.playing();run.setEnabled(!locked);operation.setEnabled(!locked);demo.setEnabled(!locked);examples.setEnabled(!locked);
        valueInput.setEnabled(!locked);indexInput.setEnabled(!locked);play.setEnabled(player.total()>0);play.setText(player.playing()?"Pause":player.total()>0&&player.index()==player.total()-1?"Replay":"Play");
        next.setEnabled(player.total()>0&&player.index()<player.total()-1);previous.setEnabled(player.index()>0);finish.setEnabled(next.isEnabled());
        syncing=true;timeline.setMaximum(Math.max(0,player.total()-1));timeline.setValue(player.index());timeline.setEnabled(player.total()>0);syncing=false;
        stepCount.setText(player.total()==0?"No operation yet":"Step "+(player.index()+1)+" of "+player.total());timelineLabel.setText(player.total()==0?"Ready to explore":(player.index()+1)+" / "+player.total()+" steps");
    }
    private void updateInputs() {
        String op=(String)operation.getSelectedItem();if(op==null)return;StructureType type=controller.model().type();
        valueGroup.setVisible(op.equals("Insert")||op.equals("Push")||op.equals("Enqueue")||op.equals("Search")||(type.isTree()&&op.equals("Delete")));
        indexGroup.setVisible(type==StructureType.LINKED_LIST&&(op.equals("Insert")||op.equals("Delete")));
        algorithm.setText(notes(type,op));revalidate();
    }
    private String notes(StructureType type,String op) {
        if(op.equals("Clear"))return "Drop the access references. Java reclaims unreachable nodes later. *Garbage collection is excluded from the O(1) reference update.";
        if(op.equals("Inorder"))return "left → node → right\nProduces ascending keys in a BST or AVL tree.";
        if(op.equals("Preorder"))return "node → left → right\nVisit each subtree's root before its children.";
        if(op.equals("Postorder"))return "left → right → node\nVisit children before their parent.";
        return switch(type){
            case LINKED_LIST -> "Indices start at 0.\nFollow next links to locate a node. Head insertion and append with a tail pointer take O(1).";
            case STACK -> "LIFO: last in, first out.\nPush, pop and peek use the top reference. Each takes O(1).";
            case QUEUE -> "FIFO: first in, first out.\nEnqueue at rear. Dequeue and peek at front. Each takes O(1).";
            case BST -> "Compare keys, then go left or right. Operations depend on height h. A skewed tree can reach O(n).";
            case AVL -> "BST ordering + balance.\nbf = height(left) − height(right). Valid bf: −1, 0, +1. Rotate when |bf| > 1.";
        };
    }
    private void exportPng() {
        JFileChooser chooser=new JFileChooser();chooser.setDialogTitle("Export current visual step");chooser.setSelectedFile(new File("structura-"+controller.model().type().name().toLowerCase(Locale.ROOT)+".png"));
        if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        File file=chooser.getSelectedFile();if(!file.getName().toLowerCase(Locale.ROOT).endsWith(".png"))file=new File(file.getParentFile(),file.getName()+".png");
        if(file.exists()&&JOptionPane.showConfirmDialog(this,"Replace the existing image?","Export PNG",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{ImageIO.write(canvas.exportImage(),"png",file);status.setForeground(Theme.ACCENT);status.setText("Exported the current step to "+file.getAbsolutePath());}
        catch(IOException e){status.setForeground(Theme.ORANGE);status.setText("Could not save image: "+e.getMessage());}
    }
    private void showReference() {
        JTextArea text=Theme.text("FIVE STRUCTURES, FIVE IDEAS\n\nLinked List  •  nodes connected by next references\nStack  •  LIFO, access at top\nQueue  •  FIFO, front and rear references\nBST  •  left keys < node key < right keys\nAVL  •  BST order, with |balance factor| ≤ 1\n\nAVL ROTATIONS\nLL: 30, 20, 10 → rotate right\nRR: 10, 20, 30 → rotate left\nLR: 30, 10, 20 → left, then right\nRL: 10, 30, 20 → right, then left\nEvery example finishes with root 20.\n\nPLAYBACK\nCtrl+Space: play / pause   •   Ctrl+Right: next step\nDrag the timeline to revisit an earlier immutable snapshot.\nGo to end unlocks new operations. Reset empties the structure.\nSwitching structures also starts an empty demonstration.\n\nCOMPLEXITY\nTheoretical cost describes the core data-structure algorithm.\nSnapshots and invariant checks add overhead; animation time is not a benchmark.\nEach snapshot costs O(n); s steps need O(s × n) memory.\nThe visual demo is limited to 30 live nodes.",14,Theme.TEXT);
        text.setPreferredSize(new Dimension(540,560));JOptionPane.showMessageDialog(this,text,"Structura · Quick reference",JOptionPane.PLAIN_MESSAGE);
    }
    private void practice() {
        String[][] questions={
            {"Push 10, push 20, then pop. Which value is returned?","10","20","Both","1","A stack is LIFO: the most recently pushed value is removed first."},
            {"Enqueue 10, enqueue 20, then dequeue. Which value is returned?","10","20","Neither","0","A queue is FIFO: the oldest enqueued value leaves first."},
            {"Insert 30, 10, 20 into an AVL tree. Which rotation case occurs?","LL","LR","RR","1","The new key is in the right subtree of the left child: rotate left, then right."},
            {"Which traversal outputs ascending keys for a BST?","Preorder","Postorder","Inorder","2","Inorder visits left subtree, node, then right subtree."},
            {"What is the worst-case search cost in an unbalanced BST?","O(1)","O(log n)","O(n)","2","Sorted insertion can create a chain of height n."},
            {"Why copy nodes into immutable animation snapshots?","Preserve earlier states","Sort values","Reduce every operation to O(1)","0","Live references would change as the model changes, corrupting earlier playback frames."}
        };
        String[] q=questions[quizIndex];int answer=JOptionPane.showOptionDialog(this,q[0],"Viva practice · "+(quizIndex+1)+" / "+questions.length,JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,new String[]{q[1],q[2],q[3]},q[1]);
        if(answer<0)return;boolean correct=answer==Integer.parseInt(q[4]);if(correct)quizScore++;
        JOptionPane.showMessageDialog(this,(correct?"Correct! ":"Let's review. ")+q[5]+"\nScore: "+quizScore+" / "+(quizIndex+1),"Viva practice",JOptionPane.INFORMATION_MESSAGE);
        quizIndex++;if(quizIndex==questions.length){quizIndex=0;quizScore=0;}
    }
    public void shutdown(){player.cancel();}
    @Override public void paint(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);super.paint(g);g.dispose();}
    public Controller controller(){return controller;}
    public AnimationPlayer player(){return player;}
    public VisualizationPanel canvas(){return canvas;}
}
