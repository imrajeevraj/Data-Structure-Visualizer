package edu.rungta.dsv.model;

import java.util.List;

public record OperationTrace(String operation, List<StepSnapshot> steps, String result, String complexity) {
    public OperationTrace { steps = List.copyOf(steps); }
}
