package bstmap;


import java.util.*;

/**
 * Implementation of interface Map61B with BST as core data structure.
 *
 * @author Your name here
 */
public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class Node {
        /* (K, V) pair stored in this Node. */
        private K key;
        private V value;

        /* Children of this Node. */
        private Node left;
        private Node right;

        private Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    private Node root;  /* Root node of the tree. */
    private int size; /* The number of key-value pairs in the tree */

    /* Creates an empty BSTMap. */
    public BSTMap() {
        this.clear();
    }

    /* Removes all of the mappings from this map. */
    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            throw new IllegalArgumentException("calls get() with a null key");
        }

        return keySet().contains(key);
    }

    /** Returns the value mapped to by KEY in the subtree rooted in P.
     *  or null if this map contains no mapping for the key.
     */
    private V getHelper(K key, Node p) {
        if (key == null) {
            throw new IllegalArgumentException("calls get() with a null key");
        }
        if (p == null) {
            return null;
        }

        int cmp = key.compareTo(p.key);
        if (cmp > 0) {
            return getHelper(key, p.right);
        } else if (cmp < 0) {
            return getHelper(key, p.left);
        } else {
            return p.value;
        }
    }

    /** Returns the value to which the specified key is mapped, or null if this
     *  map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        return getHelper(key, root);
    }

    /** Returns a BSTMap rooted in p with (KEY, VALUE) added as a key-value mapping.
      * Or if p is null, it returns a one node BSTMap containing (KEY, VALUE).
     */
    private Node putHelper(K key, V value, Node p) {
        if (p == null) {
            // Update the size!
            size += 1;
            return new Node(key, value);
        }

        int cmp = key.compareTo(p.key);
        if (cmp < 0) {
            p.left = putHelper(key, value, p.left);
        } else if (cmp > 0) {
            p.right = putHelper(key, value, p.right);
        } else {
            p.value = value;
        }
        return p;
    }

    /** Inserts the key KEY
     *  If it is already present, updates value to be VALUE.
     */
    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("calls get() with a null key");
        }
        // Don't forget to update the root
        root = putHelper(key, value, root);
    }

    /* Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return size;
    }

    //////////////// EVERYTHING BELOW THIS LINE IS OPTIONAL ////////////////

    /* Returns a Set view of the keys contained in this map. */
    @Override
    public Set<K> keySet() {
        Set<K> res = new HashSet<>();
        // Traverse the tree
        keySetHelper(root, res);
        return res;
    }

    private void keySetHelper(Node p, Set<K> s) {
        if (p == null) {
            return;
        }
        // DFS Traversal
        s.add(p.key);
        keySetHelper(p.left, s);
        keySetHelper(p.right, s);
    }

    /** Removes KEY from the tree if present
     *  returns VALUE removed,
     *  null on failed removal.
     */
    @Override
    public V remove(K key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        V v = get(key);
        if (v != null) {
            return remove(key, v);
        }
        return null;
    }


    /** Removes the key-value entry for the specified key only if it is
     *  currently mapped to the specified value.  Returns the VALUE removed,
     *  null on failed removal.
     **/
    @Override
    public V remove(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        V v = get(key);
        if (v != null) {
            root = removeHelper(key, value, root);
            size = sizeT(root);
        }
        return v;
    }


    private Node removeHelper(K key, V value, Node p) {
        int cmp = key.compareTo(p.key);
        if (cmp > 0) {
            p.right = removeHelper(key, value, p.right);
        } else if (cmp < 0) {
            p.left = removeHelper(key, value, p.left);
        } else {
            if (p.value.equals(value)) {
                if (p.left == null) {
                    return p.right;
                }
                if (p.right == null) {
                    return p.left;
                }
                // Two children
                // 1. Find successor(or predecessor)
                Node successor = findMin(p.right);
//                Node predecessor = findMax(p.left);

                // 2. Exchange the value of the successor and the node to be deleted.
                K sKey = successor.key;
                V sValue = successor.value;
                p.key = sKey;
                p.value = sValue;
                p.right = removeHelper(sKey, sValue, p.right);
                return p;
            }
        }
        return p;
    }

    /**
     * Find the smallest element in the tree rooted at p
     * @param p
     * @return
     */
    private Node findMin(Node p) {
        if (p.left == null) {
            return p;
        }
        return findMin(p.left);
    }

    /**
     * Find the maximum element in the tree rooted at p
     * @param p
     * @return
     */
    private Node findMax(Node p) {
        if (p.right == null) {
            return p;
        }
        return findMax(p.right);
    }

    /**
     * Return the size of the tree rooted at p
     * @return
     */
    private int sizeT(Node p) {
        if (p == null) {
            return 0;
        }
        return sizeT(p.left) + sizeT(p.right) + 1;
    }


    @Override
    public Iterator<K> iterator() {
        List<K> allKeys;
        allKeys = new ArrayList<>();
        getAllKeys(allKeys, root);
        return allKeys.iterator();
    }


    private void getAllKeys(List<K> container, Node p) {
        if (p == null) {
            return;
        }
        // Pre-DFS Traversal
        container.add(p.key);
        getAllKeys(container, p.left);
        getAllKeys(container, p.right);
    }

    // Helper Method in visualizing trees, should be commented when submitted
    private List<Node> branches(Node p) {
        List<Node> childNodes = new ArrayList<>();
        if (p == null) {
            return childNodes;
        }
        if (p.left != null) {
            childNodes.add(p.left);
        }
        if (p.right != null) {
            childNodes.add(p.right);
        }
        return childNodes;
    }

    private void printTreeStructure() {
        printTreeStructure(root, 0);
    }

    private void printTreeStructure(Node p, int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append(" ");
        }
        sb.append(p.key);
        System.out.println(sb.toString());
        List<Node> allChildren = branches(p);
        for (Node n: allChildren) {
            printTreeStructure(n, indent + 1);
        }
    }

    public void printInOrder() {
        printInOrderHelper(root);
    }

    private void printInOrderHelper(Node p) {
        if (p == null) {
            return;
        }
        printInOrderHelper(p.left);
        System.out.println(p.key);
        printInOrderHelper(p.right);
    }
}
