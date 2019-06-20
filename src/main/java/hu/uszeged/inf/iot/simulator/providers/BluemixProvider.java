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

import hu.uszeged.inf.iot.simulator.system.Application;

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
		/** 
		 * The second tag of the interval.
		 */
		double mbto;
		
		/**
		 * The first tag of the interval.
		 */
		double mbfrom;
		double cost;
		
		
		/**
		 * ToString method for debugging.
		 */
		@Override
		public String toString() {
			return "Bluemix [mbto=" + mbto + ", mbfrom=" + mbfrom + ", cost=" + cost + "]";
		}

		/**
		 * Constructor for initializing an interval.
		 * @param mbto The second tag of the interval.
		 * @param mbfrom The first tag of the interval.
		 * @param cost The cost of the interval.
		 */
		public Bluemix(double mbto, double mbfrom, double cost) {
			this.mbto=mbto;
			this.mbfrom=mbfrom;
			this.cost=cost;
		}
	}
	
	/**
	 * ToString method for debugging.
	 */
	@Override
	public String toString() {
		return  "[BLUEMIX=" + BLUEMIX +" "+this.getFrequency()+"]";
	}

	/**
	 * The final cost of Azure.
	 */
	double BLUEMIX;
	
	/**
	 * This constructor should be used only in case of XML files.
	 * @param app The application which is monitored by this provider.
	 */
	public BluemixProvider(Application app) {
		super();
		this.app=app;
	}
	
	/**
	 * Constructor which helps create provider like Bluemix without XML files.
	 * @param bmList List of the intervals
	 * @param app The application which is monitored by this provider.
	 */
	public BluemixProvider(ArrayList<Bluemix> bmList,Application app) {
		super(app);
		this.bmList=bmList;
		this.startProvider();
	}
	
	/**
	 * This method calculates the costs based on the frequency of the class.
	 */
	public void tick(long fires) {		
		
		if(this.bmList.size()!=0){
			double tmp= (double) this.app.getSumOfProcessedData() / 1048576; // 1 MB
			double cost=0.0;
			
 			for(Bluemix bm : this.bmList){

				if (tmp <= bm.mbto && tmp >= bm.mbfrom) {
					cost = bm.cost;
					
				}
			
			}
 			this.BLUEMIX=tmp*cost;
		}
		if(this.shouldStop) {
			unsubscribe();
		}
	}

	/**
	 * This method starts the work of the provider with the subscription.
	 */
	@Override
	public void startProvider() {
		subscribe(Integer.MAX_VALUE);
	}
}
