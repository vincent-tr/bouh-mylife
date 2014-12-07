package org.mylife.home.common.collections;

import java.util.ArrayList;
import java.util.List;

/**
 * Item pour faire un arbre
 * 
 * @author pumbawoman
 * 
 * @param <E>
 */
public class TreeNode<E> {

	private E element;
	private TreeNode<E> parent;
	private final List<TreeNode<E>> children = new ArrayList<TreeNode<E>>();

	public E getElement() {
		return element;
	}

	public void setElement(E element) {
		this.element = element;
	}

	public TreeNode<E> getParent() {
		return parent;
	}

	public void setParent(TreeNode<E> parent) {
		this.parent = parent;
	}

	public List<TreeNode<E>> getChildren() {
		return children;
	}

}
