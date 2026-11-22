package edu.rungta.dsv.model;

import java.util.*;

public final class AVLModel extends BSTModel {
    @Override public StructureType type() { return StructureType.AVL; }
    @Override protected void restorePath(int key) {
        List<TreeNode> path = repairPath(key);
        for (int i = 0; i < path.size(); i++) {
            TreeNode n = path.get(i), parent = i+1 < path.size() ? path.get(i+1) : null;
            update(n);
            int bf = balance(n);
            record("Check " + n.key + ": balance = left height - right height = " + bf + ".", "BALANCE", n.id);
            if (bf > 1) {
                if (balance(n.left) < 0) {
                    record("LR case: rotate the left child left, then this node right.", "LR ROTATION", n.id, n.left.id);
                    rotateLeft(n.left, n);
                } else record("LL case: a right rotation restores balance.", "LL ROTATION", n.id);
                rotateRight(n, parent);
            } else if (bf < -1) {
                if (balance(n.right) > 0) {
                    record("RL case: rotate the right child right, then this node left.", "RL ROTATION", n.id, n.right.id);
                    rotateRight(n.right, n);
                } else record("RR case: a left rotation restores balance.", "RR ROTATION", n.id);
                rotateLeft(n, parent);
            }
        }
    }
    private void rotateLeft(TreeNode x, TreeNode parent) {
        TreeNode y = x.right;
        x.right = y.left; y.left = x; attach(parent, x, y); update(x); update(y);
        record("Left rotation at " + x.key + ": " + y.key + " becomes the subtree root.", "ROTATED LEFT", x.id, y.id);
    }
    private void rotateRight(TreeNode y, TreeNode parent) {
        TreeNode x = y.left;
        y.left = x.right; x.right = y; attach(parent, y, x); update(y); update(x);
        record("Right rotation at " + y.key + ": " + x.key + " becomes the subtree root.", "ROTATED RIGHT", x.id, y.id);
    }
}
