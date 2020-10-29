/*
 * Util 2011-2011 Jaebong Lee
 * novaever@gmail.com
 * http://jbong.net/
 */

package net.jbong.Util;

public class IdGenerator {
	private int mNewId;
	
	public IdGenerator() {
		mNewId = 0;
	}
	
	public IdGenerator(int lastExistId) {
		mNewId = lastExistId;
	}
	
	public int getNewId() {
		return ++mNewId;
	}
}
