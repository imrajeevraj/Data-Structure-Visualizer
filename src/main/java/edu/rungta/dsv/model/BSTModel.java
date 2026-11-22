package edu.rungta.dsv.model;

import java.util.*;

public class BSTModel extends AbstractModel {
    protected TreeNode root;
    @Override public StructureType type() { return StructureType.BST; }
    @Override protected int rootId() { return root == null ? 0 : root.id; }
    protected static int height(TreeNode n) { return n == null ? 0 : n.height; }
    protected static int balance(TreeNode n) { return n == null ? 0 : height(n.left) - height(n.right); }
    protected static void update(TreeNode n) { n.height = 1 + Math.max(height(n.left), height(n.right)); }
    private int snapshotHeights(TreeNode n, Map<Integer,Integer> heights) {
        if (n == null) return 0;
        int h = 1 + Math.max(snapshotHeights(n.left,heights),snapshotHeights(n.right,heights));
        heights.put(n.id,h); return h;
    }
    @Override protected List<NodeView> nodes() {
        List<NodeView> result = new ArrayList<>(); Map<Integer,Integer> heights = new HashMap<>();
        heights.put(0,0); snapshotHeights(root,heights); collect(root,result,heights); return result;
    }
    private void collect(TreeNode n, List<NodeView> out, Map<Integer,Integer> heights) {
        if (n == null) return;
        // Compute displayed metadata from the snapshot topology, including intermediate rebalancing states.
        out.add(new NodeView(n.id, n.key, 0, n.left == null ? 0 : n.left.id, n.right == null ? 0 : n.right.id,
                heights.get(n.id), heights.get(n.left == null ? 0 : n.left.id) - heights.get(n.right == null ? 0 : n.right.id)));
        collect(n.left, out, heights); collect(n.right, out, heights);
    }
    protected TreeNode find(int key) {
        TreeNode n = root;
        while (n != null && n.key != key) n = key < n.key ? n.left : n.right;
        return n;
    }
    private void tracePath(int key) {
        TreeNode n = root;
        while (n != null) {
            record("Compare " + key + " with " + n.key + (key == n.key ? ": match." : key < n.key ? ": go left." : ": go right."), "COMPARE", n.id);
            if (key == n.key) break;
            n = key < n.key ? n.left : n.right;
        }
    }
    @Override public OperationTrace execute(String op, Integer value, Integer index) {
        supported(op);
        int key = 0;
        if (op.equals("Insert") || op.equals("Delete") || op.equals("Search")) key = required(value);
        if (op.equals("Insert")) {
            if (find(key) != null) throw new IllegalArgumentException("Key " + key + " already exists. Trees reject duplicate keys.");
            room();
        }
        begin(op + " starts at the root.");
        String result, cost = type() == StructureType.AVL ? "O(log n)" : "O(h), worst O(n)";
        switch (op) {
            case "Insert" -> {
                tracePath(key);
                TreeNode added = new TreeNode(nextId++, key);
                if (root == null) root = added;
                else {
                    TreeNode p = root;
                    while (true) {
                        if (key < p.key) { if (p.left == null) { p.left = added; break; } p = p.left; }
                        else { if (p.right == null) { p.right = added; break; } p = p.right; }
                    }
                }
                size++;
                record("Attach " + key + " as a new leaf.", "LINK", added.id);
                restorePath(key);
                result = "Inserted " + key + (type() == StructureType.AVL ? ". AVL balance restored." : ". BST order preserved.");
            }
            case "Delete" -> {
                tracePath(key);
                TreeNode victim = find(key);
                if (victim == null) { result = key + " was not found. Structure unchanged."; break; }
                int repairKey = key;
                if (victim.left != null && victim.right != null) {
                    TreeNode successor = victim.right;
                    record("Two children: find the smallest key in the right subtree.", "SUCCESSOR", victim.id);
                    while (successor.left != null) { record("Follow left to find the inorder successor.", "VISIT", successor.id); successor = successor.left; }
                    record("Successor is " + successor.key + ". Replace the key and detach the successor atomically.", "REPLACE", victim.id, successor.id);
                    repairKey = successor.key;
                    TreeNode parent = parentOf(successor);
                    victim.key = successor.key;
                    if (parent.left == successor) parent.left = successor.right; else parent.right = successor.right;
                } else {
                    record("Detach " + key + "; its only child, if present, takes its position.", "REMOVE", victim.id);
                    TreeNode parent = parentOf(victim), child = victim.left != null ? victim.left : victim.right;
                    attach(parent, victim, child);
                }
                size--;
                output.add(key); record("Removed " + key + ". Inspect the path back to the root.", "LINK");
                restorePath(repairKey);
                result = "Deleted " + key + (type() == StructureType.AVL ? ". AVL balance restored." : ". BST order preserved.");
            }
            case "Search" -> {
                tracePath(key); TreeNode match = find(key);
                if (match != null) { output.add(key); record("Key " + key + " found.", "FOUND", match.id); }
                result = match == null ? key + " was not found." : "Found " + key + ". Structure unchanged.";
            }
            case "Clear" -> { root = null; size = 0; result = "Tree cleared."; cost = "O(1) references*"; }
            default -> { traverse(root, op); result = op + ": " + output; cost = "O(n)"; }
        }
        return finish(op, result, cost);
    }
    private void traverse(TreeNode n, String order) {
        if (n == null) return;
        if (order.equals("Preorder")) visit(n);
        traverse(n.left, order);
        if (order.equals("Inorder")) visit(n);
        traverse(n.right, order);
        if (order.equals("Postorder")) visit(n);
    }
    private void visit(TreeNode n) { output.add(n.key); record("Visit " + n.key + ". Output: " + output, "VISIT", n.id); }
    protected List<TreeNode> repairPath(int key) {
        List<TreeNode> path = new ArrayList<>(); TreeNode n = root;
        while (n != null) {
            path.add(n);
            // Equality after two-child deletion means the successor now occupies this node.
            n = key < n.key ? n.left : n.right;
        }
        Collections.reverse(path); return path;
    }
    protected void restorePath(int key) { for (TreeNode n : repairPath(key)) update(n); }
    protected TreeNode parentOf(TreeNode target) {
        if (target == root) return null;
        TreeNode n = root;
        while (n != null) {
            if (n.left == target || n.right == target) return n;
            n = target.key < n.key ? n.left : n.right;
        }
        throw new IllegalStateException("Node is detached");
    }
    protected void attach(TreeNode parent, TreeNode oldRoot, TreeNode newRoot) {
        if (parent == null) root = newRoot;
        else if (parent.left == oldRoot) parent.left = newRoot;
        else if (parent.right == oldRoot) parent.right = newRoot;
        else throw new IllegalStateException("Parent link missing");
    }
    @Override public void checkInvariants() {
        Set<Integer> seen = new HashSet<>(); verify(root, Long.MIN_VALUE, Long.MAX_VALUE, seen);
        if (seen.size() != size) throw new IllegalStateException("Wrong tree size");
    }
    private int verify(TreeNode n, long low, long high, Set<Integer> seen) {
        if (n == null) return 0;
        if (!seen.add(n.id) || n.key <= low || n.key >= high) throw new IllegalStateException("Invalid BST order or cycle");
        int l = verify(n.left, low, n.key, seen), r = verify(n.right, n.key, high, seen);
        if (n.height != 1 + Math.max(l, r)) throw new IllegalStateException("Incorrect stored height");
        if (type() == StructureType.AVL && Math.abs(l-r) > 1) throw new IllegalStateException("AVL unbalanced at " + n.key);
        return n.height;
    }
}
