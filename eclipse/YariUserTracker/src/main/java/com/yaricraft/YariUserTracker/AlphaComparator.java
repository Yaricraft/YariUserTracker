package com.yaricraft.YariUserTracker;

import java.util.Comparator;

public class AlphaComparator implements Comparator<String> {
	public int compare(String obj1, String obj2) {
		return obj1.compareTo(obj2);
	}
}