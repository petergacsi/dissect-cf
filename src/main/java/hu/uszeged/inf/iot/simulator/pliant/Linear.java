package hu.uszeged.inf.iot.simulator.pliant;

import java.util.Collections;
import java.util.Vector;

/**
 * Normalize values to 0 to 1. This solution is some kind of linear transformation.
 * @author dombijd
 */
public class Linear implements INormalizer {

	public Vector<Double> normalizeincremental(Vector source_vector) {
		
		Vector<Double> result = new Vector<Double>(source_vector.size());
		
		if(source_vector.get(0) instanceof Double)
		{
			Double max = (Double) Collections.max(source_vector) + 1.0;
			Double min = (Double) Collections.min(source_vector) - 1.0;
			Double dist =new Double(max-min);
			for(int i=0;i<source_vector.size();i++)
			{			
				result.add(((Double) source_vector.get(i) - min) / (dist));				
			}
		}		
		if(source_vector.get(0) instanceof Long)
		{
			Long max = (Long) Collections.max(source_vector) + 1;
			Long min = (Long) Collections.min(source_vector) - 1;
			Double dist =new Double(max-min);		
			for(int i=0;i<source_vector.size();i++)
			{			
				result.add(new Double(new Double((Long) source_vector.get(i) - min)/dist));		
			}
		}
		
		return result;
	}

	public Vector normalizedecremental(Vector source_vector) {
		
		Vector<Double> result = new Vector<Double>(source_vector.size());
		
		if(source_vector.get(0) instanceof Double)
		{
			Double max = (Double) Collections.max(source_vector) + 1.0;
			Double min = (Double) Collections.min(source_vector) - 1.0;
			Double dist =new Double(min-max);
			for(int i=0;i<source_vector.size();i++)
			{			
				result.add(((Double) source_vector.get(i) - max) / (dist));				
			}
		}		
		if(source_vector.get(0) instanceof Long)
		{
			Long max = (Long) Collections.max(source_vector) + 1;
			Long min = (Long) Collections.min(source_vector) - 1;
			Double dist =new Double(min-max);		
			for(int i=0;i<source_vector.size();i++)
			{			
				result.add(new Double(new Double((Long) source_vector.get(i) - max)/dist));		
			}
		}		
		return result;
	}
	
}
