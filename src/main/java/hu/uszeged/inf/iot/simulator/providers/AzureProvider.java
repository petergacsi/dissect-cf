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

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.entities.Device;
import hu.uszeged.inf.iot.simulator.fog.Application;

public class AzureProvider extends Provider{
	double AZURE;
	private long usedMessage;

	@Override
	public String toString() {
		return "[AZURE=" + AZURE + " "+this.getFrequency()+"]";
	}

	public AzureProvider(long azureFreq,double pricePerMonth,long messagesPerDay,long messagesizePerKB,Application app) {
		super(app);
		this.azureFreq=azureFreq;
		this.pricePerMonth=pricePerMonth;
		this.messagesPerDay=messagesPerDay;
		this.messagesizePerKB=messagesizePerKB;
		this.startProvider();
	}

	public AzureProvider(Application app) {
		super();
		this.usedMessage=0;
		this.app=app;
	}
	
	public long avarageFileSize() {
		long tmp=0;
		for(Device s : this.app.getStations()) {
			tmp+=s.getFilesize();
		}
		return tmp/this.app.getStations().size();
	}
	
	public void tick(long fires) {
		if(this.app.isSubscribed()==false) {
			unsubscribe();
		}
		
		if(this.messagesPerDay>0 && this.avarageFileSize()<=(this.messagesizePerKB*1024)){
			long totalMassages=this.app.getSumOfProcessedData() / this.avarageFileSize();
			long msg = totalMassages - usedMessage;
			usedMessage= msg;
			
			if(msg<=this.messagesPerDay){
				System.out.println(Timed.getFireCount()+ " "+this.getFrequency());
				long month = Timed.getFireCount()/this.getFrequency();
				if(month==0){
					month=1;
					this.AZURE=this.pricePerMonth*month;
				}else if(Timed.getFireCount()%(this.getFrequency())!=0){
					this.AZURE=this.pricePerMonth*(month+1);
				}else{
					this.AZURE=this.pricePerMonth*month;
				}
			}else{
				this.AZURE=-1;
			}
		}
		if(this.shouldStop) {
			unsubscribe();
		}
	}


	@Override
	public void startProvider() {
		subscribe(this.azureFreq);
		shouldStop=false;
	}
}
