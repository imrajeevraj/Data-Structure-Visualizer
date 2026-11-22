package edu.rungta.dsv.controller;

import edu.rungta.dsv.model.*;
import java.util.*;

/** Owns the active model. Swing and parsing do not leak into model classes. */
public final class Controller {
    private VisualizableModel model;
    public Controller() { select(StructureType.AVL); }
    public void select(StructureType type) { model = create(type); }
    public static VisualizableModel create(StructureType type) {
        return switch (type) {
            case LINKED_LIST -> new LinkedListModel(); case STACK -> new StackModel();
            case QUEUE -> new QueueModel(); case BST -> new BSTModel(); case AVL -> new AVLModel();
        };
    }
    public VisualizableModel model() { return model; }
    public OperationTrace execute(String operation, String value, String index) {
        StructureType type = model.type();
        boolean needsValue = operation.equals("Insert") || operation.equals("Push") || operation.equals("Enqueue")
                || operation.equals("Search") || (operation.equals("Delete") && type.isTree());
        boolean needsIndex = type == StructureType.LINKED_LIST && (operation.equals("Insert") || operation.equals("Delete"));
        return model.execute(operation, needsValue ? parse(value, "Value") : null, needsIndex ? parse(index, "Index") : null);
    }
    public static int parse(String text, String label) {
        try { return Integer.parseInt(text.trim()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException(label + " must be a whole number from -2147483648 to 2147483647."); }
    }
    public OperationTrace demo(String name) {
        StructureType type = model.type();
        VisualizableModel demo = create(type);
        int[] values = switch (name) {
            case "LL rotation" -> new int[]{30,20,10}; case "RR rotation" -> new int[]{10,20,30};
            case "LR rotation" -> new int[]{30,10,20}; case "RL rotation" -> new int[]{10,30,20};
            case "Sorted input" -> new int[]{10,20,30,40,50,60,70};
            default -> type.isTree() ? new int[]{40,20,60,10,30,50,70} : new int[]{12,24,36,48};
        };
        List<StepSnapshot> steps = new ArrayList<>();
        String op = type == StructureType.STACK ? "Push" : type == StructureType.QUEUE ? "Enqueue" : "Insert";
        for (int v : values) steps.addAll(demo.execute(op, v, demo.size()).steps());
        model = demo;
        return new OperationTrace(name, steps, "Demo complete: " + name + ". Try a search, deletion or traversal next.", "Per insert: " + (type == StructureType.AVL ? "O(log n)" : type == StructureType.BST ? "O(h)" : "O(1)"));
    }
}
