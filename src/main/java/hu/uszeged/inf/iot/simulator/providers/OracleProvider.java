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

import hu.uszeged.inf.iot.simulator.entities.Device;
import hu.uszeged.inf.iot.simulator.entities.Station;
import hu.uszeged.inf.iot.simulator.fog.Application;

public class OracleProvider extends Provider {
	double ORACLE;
	
	@Override
	public String toString() {
		return "[ORACLE=" + ORACLE + " "+	this.getFrequency()+"]";
	}


	public OracleProvider(Application app) {
		super();
		this.app=app;
	}
	
	public void tick(long fires) {

		if(this.amMessagesPerMonthPerDevice>0){
				for(Device s : this.app.getStations()){
					long month = s.getStopTime()/(this.getFrequency());
					if(month==0){
						month=1;
						this.ORACLE=this.ORACLE+this.devicepricePerMonth*((Station) s).getNumOfSensors()*month;
					}else if(s.getStopTime()%(this.getFrequency())!=0){
						this.ORACLE=this.ORACLE+this.devicepricePerMonth*((Station) s).getNumOfSensors()*(month+1);
					}else{
						this.ORACLE=this.ORACLE+this.devicepricePerMonth*((Station) s).getNumOfSensors()*month;
					}
					// additional cost
					long device = s.getMessageCount()/((Station) s).getNumOfSensors();// 1 device hany uzenetet generalt
					s.setMessageCount(0); 
					if(device>this.messagesPerMonthPerDevice){
						device-=this.messagesPerMonthPerDevice;
						long whole=device/this.amMessagesPerMonthPerDevice;
						if(whole==0){
							this.ORACLE+=this.amDevicepricePerMonth;
						}else if((device%this.amMessagesPerMonthPerDevice)!=0){
							this.ORACLE+=this.amDevicepricePerMonth*(whole+1);
						}else{
							this.ORACLE+=this.amDevicepricePerMonth*(whole);
						}
					} 
				}
			if(this.shouldStop) {
				unsubscribe();
			}
			
		}
	}


	@Override
	public void startProvider() {
		subscribe(oracleFreq);
		this.shouldStop=false;
	}
}
