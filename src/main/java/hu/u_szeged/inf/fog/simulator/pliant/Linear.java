/*
 *  ========================================================================
 *  DIScrete event baSed Energy Consumption simulaTor 
 *    					            for Clouds, Federations and Fog(DISSECT-CF-Fog)
 *  ========================================================================
 *  
 *  This file is part of DISSECT-CF.
 *  
 *  DISSECT-CF is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at
 *  your option) any later version.
 *  
 *  DISSECT-CF is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *  General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with DISSECT-CF.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  (C) Copyright 2019, Jozsef Daniel Dombi (dombijd@inf.u-szeged.hu)
 */

package hu.u_szeged.inf.fog.simulator.pliant;

import java.util.Collections;
import java.util.Vector;

/**
 * Normalize values to 0 to 1. This solution is some kind of linear transformation.
 * TODO: refactoring needed! 
 * @author Jozsef Daniel Dombi (dombijd@inf.u-szeged.hu)
 */
public class Linear implements INormalizer {

	public Vector<Double> normalizeincremental(Vector source_vector) {
		
		Vector<Double> result = new Vector<Double>(source_vector.size());
		
		if(source_vector.get(0) instanceof Double)
		{
			double max = (Double) Collections.max(source_vector) + 1.0;
			double min = (Double) Collections.min(source_vector) - 1.0;
			double dist =(double)max-min;
			for(int i=0;i<source_vector.size();i++)
			{			
				result.add(((Double) source_vector.get(i) - min) / (dist));				
			}
		}		
		if(source_vector.get(0) instanceof Long)
		{
			long max = (Long) Collections.max(source_vector) + 1;
			long min = (Long) Collections.min(source_vector) - 1;
			double dist = (double) max-min;		
			for(int i=0;i<source_vector.size();i++)
			{			
				result.add((double)((double)((Long) source_vector.get(i) - min)/dist));		
			}
		}
		
		return result;
	}

	public Vector normalizedecremental(Vector source_vector) {
		
		Vector<Double> result = new Vector<Double>(source_vector.size());
		
		if(source_vector.get(0) instanceof Double)
		{
			double max = (Double) Collections.max(source_vector) + 1.0;
			double min = (Double) Collections.min(source_vector) - 1.0;
			double dist =(double)min-max;
			for(int i=0;i<source_vector.size();i++)
			{			
				result.add(((Double) source_vector.get(i) - max) / (dist));				
			}
		}		
		if(source_vector.get(0) instanceof Long)
		{
			long max = (Long) Collections.max(source_vector) + 1;
			long min = (Long) Collections.min(source_vector) - 1;
			double dist =(double)min-max;		
			for(int i=0;i<source_vector.size();i++)
			{			
				result.add((double)((double)((Long) source_vector.get(i) - max)/dist));		
			}
		}		
		return result;
	}
	
}
