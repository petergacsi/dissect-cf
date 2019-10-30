/*
 *  ========================================================================
 *  DIScrete event baSed Energy Consumption simulaTor 
 *    					             for Clouds, Federations and Fog(DISSECT-CF-Fog)
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

import java.util.Vector;

/**
 * @author Jozsef Daniel Dombi (dombijd@inf.u-szeged.hu)
 */
public class Sigmoid<E> implements INormalizer{
	
	public Sigmoid()
	{
		shift = 0.0;
		lambda = 1;
	}
	
	public Sigmoid(Double lambda, Double shift)
	{
		this.shift = shift;
		this.lambda = lambda;
	}
	

	public Vector<Double> normalizeincremental(Vector<?> source_vector) {		
		
		Vector<Double> result = new Vector<Double>(source_vector.size());
		
		for(int i=0;i<source_vector.size();i++)
		{
			Double value = null;
			if(source_vector.get(i) instanceof Double)
				value = (Double)source_vector.get(i);
			if(source_vector.get(i) instanceof Long)
				value = (Double)source_vector.get(i);			
			result.add(getat(value));
		}
		return result;
	}

	public Vector<Double> normalizedecremental(Vector<?> source_vector) {
		Vector<Double> result = new Vector<Double>(source_vector.size());
		
		for(int i=0;i<source_vector.size();i++)
		{
			Double value = null;
			if(source_vector.get(i) instanceof Double)
				value = (Double)source_vector.get(i);
			if(source_vector.get(i) instanceof Long)
				value = (Double)source_vector.get(i);			
			result.add(getat((-1)*value));
		}
		return result;
	}	
	
	public Double getat(Double x)
	{
		return 1 / (1 + Math.pow(Math.E, (-1) * lambda * (x - shift)));		
	}	
	
	double lambda;
	double shift;
}
