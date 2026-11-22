package edu.rungta.dsv.model;

public interface VisualizableModel {
    int LIMIT = 30;
    StructureType type();
    int size();
    StepSnapshot snapshot();
    OperationTrace execute(String operation, Integer value, Integer index);
    void checkInvariants();
}
