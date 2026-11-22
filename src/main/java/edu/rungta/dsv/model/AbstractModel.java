package edu.rungta.dsv.model;

import java.util.*;

public abstract class AbstractModel implements VisualizableModel {
    protected int size, nextId = 1;
    private List<StepSnapshot> steps;
    protected final List<Integer> output = new ArrayList<>();
    @Override public int size() { return size; }
    protected abstract List<NodeView> nodes();
    protected abstract int rootId();
    @Override public StepSnapshot snapshot() {
        return new StepSnapshot(type(), nodes(), rootId(), Set.of(), List.of(), "Choose an operation to explore this structure.", "READY");
    }
    protected void begin(String message) { steps = new ArrayList<>(); output.clear(); record(message, "START"); }
    protected void record(String message, String phase, int... ids) {
        Set<Integer> highlights = new HashSet<>();
        for (int id : ids) if (id != 0) highlights.add(id);
        steps.add(new StepSnapshot(type(), nodes(), rootId(), highlights, output, message, phase));
    }
    protected OperationTrace finish(String operation, String result, String complexity) {
        checkInvariants(); record(result, "DONE");
        return new OperationTrace(operation, steps, result, complexity);
    }
    protected void room() { if (size >= LIMIT) throw new IllegalArgumentException("The demo limit is 30 nodes. Remove a node before inserting."); }
    protected void nonempty() { if (size == 0) throw new IllegalArgumentException("This structure is empty. Add a value first."); }
    protected int required(Integer value) { if (value == null) throw new IllegalArgumentException("Enter an integer value."); return value; }
    protected void supported(String operation) {
        if (!Arrays.asList(type().operations()).contains(operation)) throw new IllegalArgumentException("Unsupported operation: " + operation);
    }
}
