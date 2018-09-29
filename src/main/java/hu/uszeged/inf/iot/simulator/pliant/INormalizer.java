package hu.uszeged.inf.iot.simulator.pliant;

import java.util.Vector;

public interface INormalizer <E> {

	//if the higher value is better
	public Vector normalizeincremental(Vector source_vector);
		
	//if the lower value is better
	public Vector normalizedecremental(Vector source_vector);
}
