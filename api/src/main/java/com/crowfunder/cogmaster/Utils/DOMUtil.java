package com.crowfunder.cogmaster.Utils;

import org.w3c.dom.Node;

public class DOMUtil {

    // get the next node that actually is of ELEMENT_NODE type
    public static Node getNextNode(Node node) {
        Node nextNode = node.getNextSibling();
        if (nextNode == null) {
            return null;
        }
        while (nextNode.getNodeType() != Node.ELEMENT_NODE) {
            nextNode = nextNode.getNextSibling();
            if (nextNode == null) {
                return null;
            }
        }
        return nextNode;
    }

    // get the first child that actually is the first child of ELEMENT_NODE type
    public static Node getFirstChild(Node node) {
        Node childNode = node.getFirstChild();
        if (childNode == null) {
            return null;
        }
        while (childNode.getNodeType() != Node.ELEMENT_NODE) {
            childNode = childNode.getNextSibling();
            if (childNode == null) {
                break;
            }
        }
        return childNode;
    }

}
