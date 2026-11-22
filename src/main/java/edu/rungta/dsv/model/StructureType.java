package edu.rungta.dsv.model;

public enum StructureType {
    LINKED_LIST("Linked List", "A chain of nodes. Follow every connection.", "LINEAR", new String[]{"Insert", "Delete", "Search", "Traverse", "Clear"}),
    STACK("Stack", "Last in, first out. Start at the top.", "LINEAR", new String[]{"Push", "Pop", "Peek", "Clear"}),
    QUEUE("Queue", "First in, first out. Keep things in order.", "LINEAR", new String[]{"Enqueue", "Dequeue", "Peek", "Clear"}),
    BST("Binary Search Tree", "Smaller to the left. Larger to the right.", "TREES", new String[]{"Insert", "Delete", "Search", "Inorder", "Preorder", "Postorder", "Clear"}),
    AVL("AVL Tree", "A search tree that keeps its balance.", "TREES", new String[]{"Insert", "Delete", "Search", "Inorder", "Preorder", "Postorder", "Clear"});

    public final String title, subtitle, category;
    private final String[] operations;
    StructureType(String title, String subtitle, String category, String[] operations) {
        this.title = title; this.subtitle = subtitle; this.category = category; this.operations = operations;
    }
    public String[] operations() { return operations.clone(); }
    public boolean isTree() { return this == BST || this == AVL; }
}
