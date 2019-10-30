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

/**
 * This function helps for decision making
 * @author dombijd
 *
 */
public class Kappa {

	public Kappa()
	{
		lambda = 1.0;
		nu = 0.5;
	}
	
	public Kappa(double lambda, double nu)
	{
		this.lambda = lambda;
		this.nu = nu;
	}
	
	public Double getAt(Double x)
	{		
		return 1.0 / (1.0 + Math.pow(( (nu/(1.0-nu)) * ((1.0-x) / x)), lambda) );
	}
	
	private double lambda;
	private double nu;
	
}
