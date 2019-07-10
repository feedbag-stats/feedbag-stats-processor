package aggregation.location;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

public enum LocationLevel {
	SOLUTION,
	PROJECT,
	PACKAGE,
	FILE;
	
	public static Collection<LocationLevel> getAll() {
		return ImmutableList.of(SOLUTION, PROJECT, PACKAGE, FILE);
	}
}
