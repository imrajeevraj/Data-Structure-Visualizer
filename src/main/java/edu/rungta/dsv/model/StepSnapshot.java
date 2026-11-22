package edu.rungta.dsv.model;

import java.util.List;
import java.util.Set;

public record StepSnapshot(StructureType type, List<NodeView> nodes, int rootId,
                           Set<Integer> highlights, List<Integer> output, String explanation, String phase) {
    public StepSnapshot {
        nodes = List.copyOf(nodes); highlights = Set.copyOf(highlights); output = List.copyOf(output);
    }
}
