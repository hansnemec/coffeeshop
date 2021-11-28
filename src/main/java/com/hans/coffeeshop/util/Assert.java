package com.hans.coffeeshop.util;

/**
 * Static util class to check method arguments and state invariants.
 * 
 * @author hans
 */
public final class Assert {
	
	private Assert() {
		// static util class - no instances
	}
	
	public static void notEmpty(String s, String msg) {
		if (s == null || s.trim().isEmpty()) {
			throw new IllegalArgumentException(msg);
		}
	}
	
	public static void isTrue(boolean condition, String msg) {
		if (!condition) {
			throw new IllegalArgumentException(msg);
		}
	}
}
