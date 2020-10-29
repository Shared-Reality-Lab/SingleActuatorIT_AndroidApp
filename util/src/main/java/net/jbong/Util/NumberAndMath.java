/*
 * Util 2011-2011 Jaebong Lee
 * novaever@gmail.com
 * http://jbong.net/
 */

package net.jbong.Util;

import java.util.Random;

public class NumberAndMath {
	public static int closeRound(float f) {
		if (f >= 0)
			return (int)Math.floor(f + 0.5);
		else
			return (int)Math.ceil(f - 0.5);
	}
	
	public static float getFrac(float f) {
		return (f - (float)Math.floor(f));
	}
	
	public static double getFrac(double f) {
		return (f - Math.floor(f));
	}
	
	public static int[] create1DimNArray(int startNum, int n) {
		int[] array = new int[n];
		
		for (int i = 0; i < n; i++)
			array[i] = startNum + i;
		
		return array;
	}
	
	public static int[] createShuffled1DimNArray(int startNum, int n, Random random) {
		int[] array = create1DimNArray(startNum, n);
		
		for (int i = n - 1; i > 0; i--) {
			int j = random.nextInt(i + 1);
			
			int temp = array[i];
			array[i] = array[j];
			array[j] = temp;
		}
		
		return array;
	}
}
