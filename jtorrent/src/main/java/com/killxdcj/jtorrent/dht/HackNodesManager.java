package com.killxdcj.jtorrent.dht;

import java.util.ArrayList;
import java.util.List;

public class HackNodesManager {
	List<Node> allNode = new ArrayList<>();

	public List<Node> getAllNode() {
		List<Node> oldNodes = allNode;
		allNode = new ArrayList<>();
		return oldNodes;
	}

	public void putNode(Node node) {
		if (allNode.size() > 1000) {
			return;
		}
		allNode.add(node);
	}
}
