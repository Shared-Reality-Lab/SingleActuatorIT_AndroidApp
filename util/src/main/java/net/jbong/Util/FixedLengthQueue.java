package net.jbong.Util;

import java.util.Iterator;
import java.util.LinkedList;

public class FixedLengthQueue<E> implements Iterable<E> {
	private LinkedList<E> mQueue;
	
	private int mLength = 20;
	
	public FixedLengthQueue(int length) {
		mQueue = new LinkedList<E>();
		
		mLength = length;
	}
	
	public synchronized int add(E element) {
		mQueue.addLast(element);
		
		int size = mQueue.size();

		if (size > mLength) {
			mQueue.removeFirst();
			size--;
		}
		
		return size;
	}
	
	public synchronized int curSize() {
		return mQueue.size();
	}
	
	public int getLength() {
		return mLength;
	}
	
	public synchronized void clear() {
		mQueue.clear();
	}
	
	public Iterator<E> iterator() {
		Iterator<E> iterator = new Iterator<E>() {
			int seq = 0;
			
			public boolean hasNext() {
				return seq < mQueue.size();
			}

			public E next() {
				return mQueue.get(seq++);
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
		
		return iterator;
	}
}
