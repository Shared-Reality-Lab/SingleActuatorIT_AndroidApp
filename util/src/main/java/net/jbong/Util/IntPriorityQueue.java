/*
 * Util 2011-2011 Jaebong Lee
 * novaever@gmail.com
 * http://jbong.net/
 */

package net.jbong.Util;

import java.util.LinkedList;
import java.util.NoSuchElementException;

import android.util.Log;

public class IntPriorityQueue<E> {
	private LinkedList<node<E>> mQueue;
	
	public IntPriorityQueue() {
		mQueue = new LinkedList<node<E>>();
	}
	
	public synchronized int add(int priority, E element) {
		final int size = mQueue.size();
		for (int i = 0; i < size; i++) {
			final node<E> node = mQueue.get(i);
			if (node.priority > priority) {
				mQueue.add(i, new node<E>(priority, element));
				return i;
			}
		}
		
		mQueue.add(new node<E>(priority, element));
		return size;
	}
	
	public synchronized int size() {
		return mQueue.size();
	}
	
	// if queue is empty then return Integer.MAX_VALUE
	public synchronized int getHighestPriority() {
		try {
			final node<E> node = mQueue.getFirst();
			
			return node.priority;
		} catch (NoSuchElementException e) {
			return Integer.MAX_VALUE;
		}
	}
	
	public synchronized E poll() {
		final node<E> node = mQueue.poll();
		
		if (node == null)
			return null;
		else
			return node.element;
	}
	
	public synchronized void clear() {
		mQueue.clear();
	}
	
	private class node<EE> {
		int priority;
		EE element;
		
		node(int priority, EE element) {
			this.priority = priority;
			this.element = element;
		}
	}
	
	// For debugging
	public void curStateLog(String tag, String iMsg) {
		Log.i(tag, iMsg);
		for (node<E> node: mQueue)
			Log.i(tag, "priority: " + node.priority);
	}
}
