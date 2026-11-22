package edu.rungta.dsv.model;

/** Immutable drawing data, never a reference to a live node. Zero is the null ID. */
public record NodeView(int id, int value, int nextId, int leftId, int rightId, int height, int balance) {}
