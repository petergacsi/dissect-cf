/*
 *  ========================================================================
 *  DIScrete event baSed Energy Consumption simulaTor 
 *    					             for Clouds and Federations (DISSECT-CF)
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
 *  (C) Copyright 2019, Andras Markus (markusa@inf.u-szeged.hu)
 *  (C) Copyright 2019, Jozsef Daniel Dombi (dombijd@inf.u-szeged.hu)
 */


package hu.uszeged.inf.iot.simulator.pliant;

import java.util.Vector;

public class FuzzyIndicators {

	private static double minmaxcorrigate = 0.00000005;
	
	public static Double getConjunction(Vector<Double> values)
	{
		Double result = 0.0;		
		for(int i=0;i<values.size();i++)
		{
			if(values.get(i) != 0.0 || values.get(i) != 1.0)
				result += ((1.0 - values.get(i)) / values.get(i));
			else
				result+=minmaxcorrigate;
		}		
		return (1.0 / (1.0 + result));
	}

	
	public static Double getAggregation(Vector<Double> values)
	{
		Double result = 1.0;		
		for(int i=0;i<values.size();i++)
		{
			if(values.get(i) != 0.0 || values.get(i) != 1.0)
				result *= ((1 - values.get(i)) / values.get(i));
			else
				result+=minmaxcorrigate;
		}		
		return (1 / (1 + result));
	}
}
