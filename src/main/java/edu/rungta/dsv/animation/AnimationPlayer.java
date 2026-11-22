package edu.rungta.dsv.animation;

import edu.rungta.dsv.model.*;
import javax.swing.Timer;
import java.util.function.Consumer;

/** Timer and callbacks run on Swing's event dispatch thread; model is never mutated here. */
public final class AnimationPlayer {
    private final Timer timer;
    private final Consumer<StepSnapshot> listener;
    private final Runnable stateChanged;
    private OperationTrace trace;
    private int index;
    public AnimationPlayer(Consumer<StepSnapshot> listener, Runnable stateChanged) {
        this.listener = listener; this.stateChanged = stateChanged;
        timer = new Timer(700, e -> advance());
    }
    public void load(OperationTrace trace) { timer.stop(); this.trace = trace; index = 0; show(); }
    public void play() {
        if (trace == null) return;
        if (index == total() - 1) index = 0;
        show(); if (total() > 1) timer.start(); stateChanged.run();
    }
    public void pause() { timer.stop(); stateChanged.run(); }
    private void advance() {
        if (trace == null) return;
        if (index < total()-1) index++;
        if (index == total()-1) timer.stop();
        show();
    }
    public void next() { timer.stop(); advance(); }
    public void previous() { seek(index-1); }
    public void seek(int target) { timer.stop(); if (trace != null) { index = Math.max(0, Math.min(total()-1,target)); show(); } }
    public void cancel() { timer.stop(); trace = null; index = 0; stateChanged.run(); }
    public void setDelay(int milliseconds) { timer.setDelay(milliseconds); timer.setInitialDelay(milliseconds); }
    public boolean playing() { return timer.isRunning(); }
    public boolean reviewing() { return trace != null && index < total()-1; }
    public int index() { return index; }
    public int total() { return trace == null ? 0 : trace.steps().size(); }
    public OperationTrace trace() { return trace; }
    private void show() { if (trace != null) listener.accept(trace.steps().get(index)); stateChanged.run(); }
}
