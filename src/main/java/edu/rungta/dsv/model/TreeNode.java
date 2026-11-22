package edu.rungta.dsv.model;
final class TreeNode {
    final int id;
    int key, height = 1;
    TreeNode left, right;
    TreeNode(int id, int key) { this.id = id; this.key = key; }
}
