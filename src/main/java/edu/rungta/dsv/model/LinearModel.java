package edu.rungta.dsv.model;

import java.util.*;

/** Shared explicit node-chain mechanics; concrete models expose different operations. */
public abstract class LinearModel extends AbstractModel {
    private ListNode head, tail;
    @Override protected int rootId() { return head == null ? 0 : head.id; }
    @Override protected List<NodeView> nodes() {
        List<NodeView> result = new ArrayList<>();
        for (ListNode n = head; n != null; n = n.next)
            result.add(new NodeView(n.id, n.value, n.next == null ? 0 : n.next.id, 0, 0, 0, 0));
        return result;
    }
    @Override public OperationTrace execute(String op, Integer value, Integer index) {
        supported(op);
        boolean insert = op.equals("Insert") || op.equals("Push") || op.equals("Enqueue");
        boolean delete = op.equals("Delete") || op.equals("Pop") || op.equals("Dequeue");
        int at = 0, v = 0;
        if (insert || op.equals("Search")) v = required(value);
        if (op.equals("Insert") || op.equals("Delete")) {
            if (index == null || index < 0 || index > size || (delete && index == size))
                throw new IllegalArgumentException("Index must be between 0 and " + (insert ? size : Math.max(0, size - 1)) + (size == 0 && delete ? "; the list is empty." : "."));
            at = index;
        }
        if (insert) room();
        if (delete || op.equals("Peek")) nonempty();
        if (op.equals("Enqueue")) at = size;
        begin(op + " starts with " + size + " node(s).");
        String result, cost;
        if (insert) {
            int oldSize = size;
            ListNode predecessor = null;
            if (at == size && size > 0) {
                predecessor = tail; record("Use the tail/rear reference to append directly.", "LOCATE", tail.id);
            } else if (at > 0) {
                predecessor = head;
                for (int i = 0; i < at - 1; i++) {
                    record("Visit index " + i + "; follow next to find the predecessor.", "VISIT", predecessor.id);
                    predecessor = predecessor.next;
                }
                record("Predecessor found at index " + (at - 1) + ".", "LOCATE", predecessor.id);
            }
            ListNode added = new ListNode(nextId++, v);
            if (at == 0) { added.next = head; head = added; if (size == 0) tail = added; }
            else { added.next = predecessor.next; predecessor.next = added; if (at == size) tail = added; }
            size++;
            record("Link node " + v + " at index " + at + ". Update the access references.", "LINK", added.id);
            result = "Inserted " + v + " at index " + at + ".";
            cost = at == 0 || at == oldSize ? "O(1)" : "O(n)";
        } else if (delete) {
            ListNode predecessor = null, removed = head;
            for (int i = 0; i < at; i++) {
                record("Visit index " + i + "; follow next.", "VISIT", removed.id);
                predecessor = removed; removed = removed.next;
            }
            record("Remove " + removed.value + "; reconnect the chain around this node.", "REMOVE", removed.id);
            if (predecessor == null) head = removed.next; else predecessor.next = removed.next;
            if (removed == tail) tail = predecessor;
            size--; output.add(removed.value);
            record("References updated. " + (size == 0 ? "Both ends are now null." : "Remaining nodes keep their order."), "LINK");
            result = "Removed " + removed.value + "."; cost = at == 0 ? "O(1)" : "O(n)";
        } else if (op.equals("Peek")) {
            output.add(head.value); record("Read the " + (type() == StructureType.STACK ? "top" : "front") + " without removing it.", "READ", head.id);
            result = "Peek returns " + head.value + ". Structure unchanged."; cost = "O(1)";
        } else if (op.equals("Clear")) {
            head = tail = null; size = 0; result = "Structure cleared."; cost = "O(1) references*";
        } else {
            int i = 0; boolean found = false;
            for (ListNode n = head; n != null; n = n.next, i++) {
                if (op.equals("Traverse")) output.add(n.value);
                record("Visit index " + i + ": value " + n.value + ".", "VISIT", n.id);
                if (op.equals("Search") && n.value == v) { output.add(i); found = true; record("First match at index " + i + ".", "FOUND", n.id); break; }
            }
            result = op.equals("Traverse") ? "Traversal: " + output : found ? "Found " + v + " at index " + i + "." : v + " was not found.";
            cost = "O(n)";
        }
        return finish(op, result, cost);
    }
    @Override public void checkInvariants() {
        Set<Integer> ids = new HashSet<>(); ListNode last = null;
        for (ListNode n = head; n != null; n = n.next) {
            if (!ids.add(n.id)) throw new IllegalStateException("Cycle or duplicate node ID"); last = n;
        }
        if (ids.size() != size || last != tail || (size == 0 && (head != null || tail != null)) || (tail != null && tail.next != null))
            throw new IllegalStateException("Invalid chain endpoints or size");
    }
}
