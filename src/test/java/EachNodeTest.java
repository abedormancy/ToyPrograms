package test.java;

import java.util.*;

public class EachNodeTest {

    public static void main(String[] args) {
        Node a1 = Node.build("a1");
        Node a2 = Node.build("a2");
        Node b1 = Node.build("b1");
        Node b2 = Node.build("b2");
        Node c1 = Node.build("c1");
        a1.addChildren(b1);
        b1.addChildren(c1);
        a2.addChildren(b2);
        c1.addChildren(a2);
        List<Node> root = new ArrayList<>();
        root.add(a1);
        root.add(a2);

        root.stream().forEach(Node::each);

        // b2.each();
        c1.getAllParents().forEach(System.out::println);
    }


}


class Node {

    /**
     * 节点id，唯一
     **/
    private String id;

    /**
     * 节点遍历时的路径
     **/
    private String path;

    /**
     * 子节点
     **/
    private List<Node> subNodes;

    /**
     * 父节点，支持多父节点
     */
    private List<Node> parentNodes;

    public static Node build(String id) {
        if (id == null || id.trim().length() == 0) {
            throw new IllegalArgumentException("Node.id can not be empty!");
        }
        Node node = new Node();
        node.id = id;
        node.path = id;
        return node;
    }

    /**
     * 添加子节点（thread safe)
     *
     * @param node
     */
    public void addChildren(Node node) {
        if (node == null) return;
        if (subNodes == null) { // DCL
            synchronized (this) {
                if (subNodes == null) {
                    subNodes = Collections.synchronizedList(new ArrayList<>());
                }
            }
        }
        subNodes.add(node);
        if (node.parentNodes == null) { // 双检查锁
            synchronized (node) {
                if (node.parentNodes == null) {
                    node.parentNodes = Collections.synchronizedList(new ArrayList<>());
                }
            }
        }
        node.parentNodes.add(this);
    }

    public Node getParent() {
        return parentNodes == null ? null : parentNodes.get(0);
    }

    public List<Node> getParents() {
        List<Node> nodeList = new ArrayList<>();
        if (parentNodes != null) {
            nodeList.addAll(parentNodes);
        }
        return nodeList;
    }

    public List<Node> getAllParents() {
        return getAllParents(this, new HashSet<>());
    }

    private static List<Node> getAllParents(Node node, Set<Node> added) {
        for (Node parent : node.getParents()) {
            if (added.add(parent)) {
                getAllParents(parent, added);
            }
        }
        return new ArrayList<>(added);
    }


    public String path() {
        return path;
    }

    public void reset() {
        path = id;
    }

    private void previous(Node node) {
        this.path = node.path() + " -> " + id();
    }

    public String id() {
        return id;
    }

    public static void eachNode(Node node) {
        each(node, new HashSet<>());
    }

    public void each() {
        eachNode(this);
    }

    /**
     * 节点遍历，解决回环节点重复遍历问题
     *
     * @param node    current node
     * @param eachSet 辅助 set，用于标识节点是否已经遍历过
     */
    private static void each(Node node, final Set<Node> eachSet) {
        // System.out.println(node.id());
        boolean flag = eachSet.add(node);
        int size = node.subNodes != null ? node.subNodes.size() : 0;
        for (int i = 0; flag && i < size; i++) {
            for (Node next : node.subNodes) {
                next.previous(node);
                each(next, eachSet);
                next.reset();
            }
        }
        if (size == 0 || !flag) {
            System.out.println("path: " + node.path());
        }
    }

    @Override
    public String toString() {
        return id;
    }
}