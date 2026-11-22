package edu.rungta.dsv;

import edu.rungta.dsv.model.*;
import edu.rungta.dsv.controller.Controller;
import edu.rungta.dsv.animation.AnimationPlayer;
import edu.rungta.dsv.view.*;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

/** Dependency-free acceptance runner. The same model cases also run under JUnit 5. */
public final class Verification {
    private static int assertions;
    public static void check(boolean condition,String message) { assertions++;if(!condition)throw new AssertionError(message); }
    public static void equal(Object expected,Object actual) { check(Objects.equals(expected,actual),"Expected "+expected+", got "+actual); }
    public static void rejects(Runnable task) { boolean failed=false;try{task.run();}catch(IllegalArgumentException e){failed=true;}check(failed,"Expected validation error"); }
    private static List<Integer> values(VisualizableModel m) { return m.snapshot().nodes().stream().map(NodeView::value).toList(); }
    private static List<Integer> result(OperationTrace t) { return t.steps().get(t.steps().size()-1).output(); }
    private static void insert(VisualizableModel m,int v) {m.execute("Insert",v,m.size());}
    public static void linkedList() {
        var m=new LinkedListModel();insert(m,10);insert(m,20);m.execute("Insert",15,1);equal(List.of(10,15,20),result(m.execute("Traverse",null,null)));
        m.execute("Delete",null,0);m.execute("Delete",null,1);m.execute("Delete",null,0);equal(0,m.size());m.checkInvariants();
        insert(m,7);insert(m,7);equal(List.of(0),result(m.execute("Search",7,null)));check(m.snapshot().nodes().get(0).id()!=m.snapshot().nodes().get(1).id(),"Duplicate values need unique IDs");
        StepSnapshot before=m.snapshot();rejects(() -> m.execute("Insert",8,-1));rejects(() -> m.execute("Delete",null,2));equal(before,m.snapshot());
        m.execute("Clear",null,null);rejects(() -> m.execute("Delete",null,0));equal(List.of(),result(m.execute("Traverse",null,null)));
    }
    public static void stackQueue() {
        var s=new StackModel();s.execute("Push",10,null);s.execute("Push",20,null);equal(List.of(20),result(s.execute("Pop",null,null)));equal(List.of(10),result(s.execute("Peek",null,null)));equal(1,s.size());
        s.execute("Pop",null,null);rejects(() -> s.execute("Pop",null,null));rejects(() -> s.execute("Peek",null,null));
        var q=new QueueModel();q.execute("Enqueue",10,null);q.execute("Enqueue",20,null);equal(List.of(10),result(q.execute("Dequeue",null,null)));equal(List.of(20),result(q.execute("Peek",null,null)));q.execute("Dequeue",null,null);
        q.execute("Enqueue",30,null);equal(List.of(30),result(q.execute("Dequeue",null,null)));q.checkInvariants();rejects(() -> q.execute("Dequeue",null,null));rejects(() -> q.execute("Peek",null,null));
    }
    public static void bst() {
        var m=new BSTModel();for(int v:new int[]{40,20,60,10,30})insert(m,v);
        equal(List.of(10,20,30,40,60),result(m.execute("Inorder",null,null)));
        equal(List.of(40,20,10,30,60),result(m.execute("Preorder",null,null)));
        equal(List.of(10,30,20,60,40),result(m.execute("Postorder",null,null)));
        m.execute("Delete",40,null);equal(List.of(10,20,30,60),result(m.execute("Inorder",null,null)));
        StepSnapshot before=m.snapshot();rejects(() -> insert(m,30));equal(before,m.snapshot());m.execute("Search",30,null);equal(before,m.snapshot());
        m.execute("Delete",999,null);equal(before,m.snapshot());
        for(int v:new int[]{10,20,60,30})m.execute("Delete",v,null);equal(0,m.size());m.checkInvariants();
        insert(m,Integer.MIN_VALUE);insert(m,Integer.MAX_VALUE);insert(m,0);m.checkInvariants();
    }
    public static void rotations() {
        int[][] sequences={{30,20,10},{10,20,30},{30,10,20},{10,30,20}};String[] cases={"LL","RR","LR","RL"};
        for(int i=0;i<sequences.length;i++) {
            var m=new AVLModel();insert(m,sequences[i][0]);insert(m,sequences[i][1]);OperationTrace trace=m.execute("Insert",sequences[i][2],null);
            equal(20,m.snapshot().nodes().get(0).value());equal(List.of(10,20,30),result(m.execute("Inorder",null,null)));m.checkInvariants();
            final String expected=cases[i]+" ROTATION";check(trace.steps().stream().anyMatch(s -> s.phase().equals(expected)),"Missing "+expected);
            check(trace.steps().stream().anyMatch(s -> s.nodes().stream().anyMatch(n -> Math.abs(n.balance())>1)),"Must show pre-rotation imbalance");
            for(StepSnapshot s:trace.steps())snapshotTopology(s);
        }
    }
    private static void snapshotTopology(StepSnapshot step) {
        Map<Integer,NodeView> nodes=new HashMap<>();for(NodeView n:step.nodes())check(nodes.put(n.id(),n)==null,"Duplicate snapshot ID");
        Set<Integer> seen=new HashSet<>();walkSnapshot(step.rootId(),nodes,seen,Long.MIN_VALUE,Long.MAX_VALUE);equal(nodes.size(),seen.size());
    }
    private static int walkSnapshot(int id,Map<Integer,NodeView> nodes,Set<Integer> seen,long low,long high) {
        if(id==0)return 0;NodeView n=nodes.get(id);check(n!=null&&seen.add(id),"Snapshot dangling edge or cycle");check(n.value()>low&&n.value()<high,"Snapshot ordering");
        int l=walkSnapshot(n.leftId(),nodes,seen,low,n.value()),r=walkSnapshot(n.rightId(),nodes,seen,n.value(),high);
        equal(1+Math.max(l,r),n.height());equal(l-r,n.balance());return n.height();
    }
    public static void avlDeletion() {
        for(int[] sequence:new int[][]{{9,5,10,0,6,11,-1,1,2},{20,10,30,5,15,25,40,22,27},{4,2,5,1,3},{2,1,4,3,5}}) {
            var m=new AVLModel();TreeSet<Integer> ref=new TreeSet<>();for(int v:sequence){insert(m,v);ref.add(v);}
            for(int v:sequence){OperationTrace t=m.execute("Delete",v,null);ref.remove(v);m.checkInvariants();equal(new ArrayList<>(ref),result(m.execute("Inorder",null,null)));for(StepSnapshot step:t.steps())snapshotTopology(step);}
        }
    }
    public static void randomTrees() {
        Random random=new Random(20261014L);
        for(VisualizableModel m:List.of(new BSTModel(),new AVLModel())) {
            TreeSet<Integer> reference=new TreeSet<>();
            for(int i=0;i<2500;i++) {
                int v=random.nextInt(65)-32;
                if(random.nextBoolean()&&reference.size()<VisualizableModel.LIMIT) {
                    if(reference.contains(v)){StepSnapshot old=m.snapshot();rejects(() -> insert(m,v));equal(old,m.snapshot());}
                    else{insert(m,v);reference.add(v);}
                }else{m.execute("Delete",v,null);reference.remove(v);}
                m.checkInvariants();equal(reference.size(),m.size());equal(new ArrayList<>(reference),result(m.execute("Inorder",null,null)));snapshotTopology(m.snapshot());
            }
        }
    }
    public static void randomLinear() {
        Random random=new Random(19);var list=new LinkedListModel();List<Integer> ref=new ArrayList<>();
        for(int i=0;i<1000;i++) {
            if(ref.isEmpty()||(ref.size()<30&&random.nextBoolean())){int at=random.nextInt(ref.size()+1),v=random.nextInt(20);list.execute("Insert",v,at);ref.add(at,v);}
            else{int at=random.nextInt(ref.size());equal(List.of(ref.remove(at)),result(list.execute("Delete",null,at)));}
            list.checkInvariants();equal(ref,values(list));
        }
        for(StructureType type:List.of(StructureType.STACK,StructureType.QUEUE)) {
            VisualizableModel m=Controller.create(type);Deque<Integer> deque=new ArrayDeque<>();
            for(int i=0;i<600;i++) {
                if(deque.isEmpty()||(deque.size()<30&&random.nextBoolean())){int v=random.nextInt();m.execute(type==StructureType.STACK?"Push":"Enqueue",v,null);if(type==StructureType.STACK)deque.addFirst(v);else deque.addLast(v);}
                else equal(List.of(deque.removeFirst()),result(m.execute(type==StructureType.STACK?"Pop":"Dequeue",null,null)));
                m.checkInvariants();equal(new ArrayList<>(deque),values(m));
            }
        }
    }
    public static void limitsAndValidation() {
        for(StructureType type:StructureType.values()) {
            var m=Controller.create(type);String op=type==StructureType.STACK?"Push":type==StructureType.QUEUE?"Enqueue":"Insert";
            for(int i=0;i<30;i++)m.execute(op,i,m.size());StepSnapshot before=m.snapshot();rejects(() -> m.execute(op,99,m.size()));equal(before,m.snapshot());m.execute("Clear",null,null);equal(0,m.size());m.checkInvariants();
        }
        Controller c=new Controller();for(String bad:new String[]{"","abc","3.14","2147483648","-2147483649"})rejects(() -> c.execute("Insert",bad,""));equal(0,c.model().size());
        c.execute("Insert"," -2147483648 ","");c.execute("Insert","2147483647","");equal(2,c.model().size());c.select(StructureType.QUEUE);equal(0,c.model().size());
    }
    public static void immutableTraces() {
        var m=new AVLModel();var t=m.execute("Insert",30,null);StepSnapshot last=t.steps().get(t.steps().size()-1);String original=last.toString();insert(m,20);insert(m,10);m.execute("Delete",30,null);equal(original,last.toString());equal(1,last.nodes().size());
        boolean immutable=false;try{last.nodes().clear();}catch(UnsupportedOperationException e){immutable=true;}check(immutable,"Snapshot collection must be immutable");
        check(t.steps().get(0).nodes().isEmpty(),"Before snapshot must precede insertion");
    }
    public static void controllerDemos() {
        Controller c=new Controller();for(String name:List.of("LL rotation","RR rotation","LR rotation","RL rotation")){OperationTrace t=c.demo(name);equal(20,c.model().snapshot().nodes().get(0).value());check(t.steps().get(0).nodes().isEmpty(),"Demo starts empty");}
        c.select(StructureType.BST);c.demo("Sorted input");equal(7,c.model().snapshot().nodes().get(0).height());c.select(StructureType.AVL);c.demo("Sorted input");equal(3,c.model().snapshot().nodes().get(0).height());
    }
    public static void playback() throws Exception {
        var trace=new LinkedListModel().execute("Insert",10,0);AtomicReference<AnimationPlayer> reference=new AtomicReference<>();CountDownLatch done=new CountDownLatch(1);AtomicBoolean awaitingTimer=new AtomicBoolean();
        SwingUtilities.invokeAndWait(() -> {
            var p=new AnimationPlayer(s -> {if(awaitingTimer.get()&&s.phase().equals("DONE"))done.countDown();},() -> {});reference.set(p);p.load(trace);equal(0,p.index());p.next();equal(1,p.index());p.previous();equal(0,p.index());p.seek(999);equal(p.total()-1,p.index());p.seek(0);p.setDelay(5);p.play();check(p.playing(),"Player should run");p.pause();check(!p.playing(),"Pause must stop timer");awaitingTimer.set(true);p.play();
        });
        check(done.await(5,TimeUnit.SECONDS),"Timer must deliver the final frame");SwingUtilities.invokeAndWait(() -> {AnimationPlayer p=reference.get();equal(p.total()-1,p.index());check(!p.playing(),"Timer stops at final frame");p.play();p.cancel();equal(0,p.total());check(!p.playing(),"Cancel stops playback");});
    }
    private static Component named(Container root,String name) {
        for(Component c:root.getComponents()){if(name.equals(c.getName()))return c;if(c instanceof Container container){Component child=named(container,name);if(child!=null)return child;}}return null;
    }
    private static JButton button(WorkspacePanel w,String name){return (JButton)named(w,name);}
    private static void layout(Container c){c.doLayout();for(Component child:c.getComponents())if(child instanceof Container container)layout(container);}
    private static void render(WorkspacePanel panel,String name,int width,int height) throws Exception {
        panel.setSize(width,height);layout(panel);layout(panel);
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();panel.printAll(g);g.dispose();ImageIO.write(image,"png",Path.of("build/qa",name+".png").toFile());
    }
    @SuppressWarnings("unchecked")
    public static void interfaceChecks() throws Exception {
        Files.createDirectories(Path.of("build/qa"));AtomicReference<Throwable> error=new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            WorkspacePanel w=null;
            try{
                Theme.install();w=new WorkspacePanel();render(w,"01-empty",1440,900);
                ((JComboBox<String>)named(w,"examples")).setSelectedItem("LR rotation");button(w,"demo").doClick();w.player().pause();check(!button(w,"run").isEnabled(),"Mutations locked during partial playback");
                int rotation=0;for(int i=0;i<w.player().total();i++)if(w.player().trace().steps().get(i).phase().equals("LR ROTATION"))rotation=i;
                w.player().seek(rotation);render(w,"02-avl-rotation",1440,900);button(w,"finish").doClick();check(button(w,"run").isEnabled(),"Final frame unlocks controls");equal(20,w.controller().model().snapshot().nodes().get(0).value());
                ((JTextField)named(w,"value")).setText("bad");StepSnapshot before=w.controller().model().snapshot();button(w,"run").doClick();equal(before,w.controller().model().snapshot());
                button(w,"previous").doClick();check(!button(w,"run").isEnabled(),"Historical snapshot locks mutation");button(w,"reset").doClick();equal(0,w.player().total());equal(0,w.controller().model().size());
                w.select(StructureType.AVL);w.loadTrace(w.controller().demo("Balanced tree"));w.player().seek(w.player().total()-1);render(w,"03-avl-complete",1440,900);render(w,"04-small-window",1280,720);
                w.select(StructureType.LINKED_LIST);for(int i=0;i<30;i++)w.controller().execute("Insert",Integer.toString(i),Integer.toString(i));w.canvas().showStep(w.controller().model().snapshot());ImageIO.write(w.canvas().exportImage(),"png",Path.of("build/qa/05-list-30.png").toFile());
                w.select(StructureType.BST);for(int i=0;i<30;i++)w.controller().execute("Insert",Integer.toString(i),"");w.canvas().showStep(w.controller().model().snapshot());ImageIO.write(w.canvas().exportImage(),"png",Path.of("build/qa/06-skewed-bst.png").toFile());
                w.select(StructureType.STACK);w.loadTrace(w.controller().demo("Starter sequence"));w.player().seek(w.player().total()-1);render(w,"07-stack",1440,900);
                w.select(StructureType.QUEUE);w.loadTrace(w.controller().demo("Starter sequence"));w.player().seek(w.player().total()-1);render(w,"08-queue",1440,900);
            }catch(Throwable t){error.set(t);}finally{if(w!=null)w.shutdown();}
        });
        if(error.get()!=null)throw new AssertionError("UI verification failed",error.get());
    }
    @FunctionalInterface interface Checked {void run()throws Exception;}
    public static void main(String[] args)throws Exception {
        LinkedHashMap<String,Checked> cases=new LinkedHashMap<>();cases.put("Linked list indices, links and duplicate values",Verification::linkedList);cases.put("Stack LIFO and queue FIFO",Verification::stackQueue);cases.put("BST deletion and three traversals",Verification::bst);cases.put("AVL LL / RR / LR / RL trace topology",Verification::rotations);cases.put("AVL deletion and zero-child-balance cases",Verification::avlDeletion);cases.put("5,000 randomized BST / AVL updates",Verification::randomTrees);cases.put("2,200 randomized linear updates",Verification::randomLinear);cases.put("30-node limits and input validation",Verification::limitsAndValidation);cases.put("Immutable before / after snapshots",Verification::immutableTraces);cases.put("Guided demos and height comparison",Verification::controllerDemos);cases.put("Timer playback, pause, replay and cancel",Verification::playback);cases.put("Swing actions, validation, sizing and rendering",Verification::interfaceChecks);
        List<String> report=new ArrayList<>();long start=System.nanoTime();
        for(var entry:cases.entrySet()){entry.getValue().run();String line="PASS  "+entry.getKey();System.out.println(line);report.add(line);}
        String summary="PASS: "+cases.size()+" verification groups; "+assertions+" assertions; "+String.format(Locale.ROOT,"%.2f",(System.nanoTime()-start)/1e9)+" seconds.";System.out.println(summary);report.add(summary);Files.createDirectories(Path.of("build"));Files.write(Path.of("build/test-results.txt"),report);
    }
}
