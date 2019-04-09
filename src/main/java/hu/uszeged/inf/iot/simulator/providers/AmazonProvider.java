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

import hu.uszeged.inf.iot.simulator.fog.Application;

/**
 * This class represents the Amazon IoT provider. The price is based on the delivery cost
 * (the number of messages delivered by AWS IoT to devices or applications) and 
 * a message is a 512-byte block of data. 
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
public class AmazonProvider extends Provider{
	double AMAZON;
	
	@Override
	public String toString() {
		return "[AMAZON=" + AMAZON +" "+this.getFrequency()+"]";
	}


	public AmazonProvider(Application app) {
		super();
		this.app=app;
	}
	
	public AmazonProvider(double blockPrice, long messageCount, long blockSize,Application app) {
		super(app);
		this.blockPrice=blockPrice;
		this.messageCount=messageCount;
		this.blockSize=blockSize;
		this.startProvider();
	}
	
	
	public void tick(long fires) {		
		if(this.blockPrice>0 && this.blockSize>0){
			this.AMAZON= (((double)this.app.getSumOfProcessedData() / this.blockSize) + 1) * this.blockPrice / this.messageCount;
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
