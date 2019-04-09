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
 */

package hu.uszeged.inf.iot.simulator.providers;

import java.util.ArrayList;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.fog.Application;

/**
 * This class represents the Bluemix IoT provider which 
 * follows the “pay as you go” approach. Bluemix only charges 
 * after the MiB of data exchanged.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
public class BluemixProvider extends Provider{
	
	/**
	 * Helper class for managing Bluemix intervalls with its cost.
	 * @author Andras Markus (markusa@inf.u-szeged.hu)
	 *
	 */
	public static class Bluemix{
		double mbto;
		double mbfrom;
		double cost;
		
		@Override
		public String toString() {
			return "Bluemix [mbto=" + mbto + ", mbfrom=" + mbfrom + ", cost=" + cost + "]";
		}

		public Bluemix(double mbto, double mbfrom, double cost) {
			this.mbto=mbto;
			this.mbfrom=mbfrom;
			this.cost=cost;
		}
	}
	@Override
	public String toString() {
		return  "[BLUEMIX=" + BLUEMIX +" "+this.getFrequency()+"]";
	}


	double BLUEMIX;
	
	public BluemixProvider(Application app) {
		super();
		this.app=app;
	}
	
	public BluemixProvider(ArrayList<Bluemix> bmList,Application app) {
		super(app);
		this.bmList=bmList;
		this.startProvider();
	}
	
	public void tick(long fires) {		
		
		if(this.bmList.size()!=0){
			double tmp= (double) this.app.getSumOfProcessedData() / 1048576; // 1 MB
			double cost=0.0;
			
 			for(Bluemix bm : this.bmList){

				if (tmp >= bm.mbto && tmp <= bm.mbfrom) {
					cost = bm.cost;
					
				}
			
			}
 			this.BLUEMIX=tmp*cost;
		}
		if(this.shouldStop) {
			unsubscribe();
		}
	}


	@Override
	public void startProvider() {
		subscribe(Integer.MAX_VALUE);
	}
}
