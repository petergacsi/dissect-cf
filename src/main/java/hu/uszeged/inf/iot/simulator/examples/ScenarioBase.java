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


package hu.uszeged.inf.iot.simulator.examples;

import java.io.File;
import java.util.concurrent.TimeUnit;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.entities.Device;
import hu.uszeged.inf.iot.simulator.entities.Station;
import hu.uszeged.inf.iot.simulator.fog.Application;
import hu.uszeged.inf.iot.simulator.fog.Cloud;
import hu.uszeged.inf.iot.simulator.fog.Application.VmCollector;
import hu.uszeged.inf.iot.simulator.providers.Instance;
import hu.uszeged.inf.iot.simulator.providers.Provider;
import hu.uszeged.inf.iot.simulator.util.TimelineGenerator;

public abstract class ScenarioBase {
	final static String resourcePath = new StringBuilder(System.getProperty("user.dir")).
			append(File.separator).
			append("target").
			append(File.separator).
			append("resources").
			append(File.separator).
			toString();
	
	 static void printInformation(long t) {
		System.out.println("~~Informations about the simulation:~~");
		double totalCost=0.0;
		long generatedData=0,processedData=0;
		int usedVM = 0;
		int tasks = 0;
		long timeout=Long.MIN_VALUE;
		for (Cloud c : Cloud.clouds.values()) {
			System.out.println("cloud: " + c.name);
			long highestStationStoptime=Long.MIN_VALUE;
			for (Application a : c.applications) {
				System.out.println(a.name);
				totalCost+=a.instance.calculateCloudCost(a.sumOfWorkTime);
				processedData+=a.sumOfProcessedData;
				usedVM+=a.vmlist.size();
				
				for (VmCollector vmcl : a.vmlist) {
						tasks += vmcl.taskCounter;
						System.out.println(vmcl.id +" "+vmcl.vm + " tasks: " + vmcl.taskCounter + " worktime: " + vmcl.workingTime + " installed at: "
								+ vmcl.installed+" restarted: "+vmcl.restarted);
				}
				for(Device d : a.stations) {
					generatedData+=d.getSumOfGeneratedData();
					
					if(d.getStopTime()>highestStationStoptime)
						highestStationStoptime=d.getStopTime();
				}
				if((a.stopTime-highestStationStoptime)>timeout) {
					timeout=(a.stopTime-highestStationStoptime);
					
				}
				System.out.println(a.name+" stations: " + a.stations.size()+ " cost:"+a.instance.calculateCloudCost(a.sumOfWorkTime));
				System.out.println(a.providers);
			}
			System.out.println();
		}
		System.out.println("VMs " + usedVM + " tasks: " + tasks);
		System.out.println("Generated/processed data: " + generatedData + "/" + processedData);
		System.out.println("Cost: "+totalCost);
		System.out.println("timeout: "+((double)timeout/1000/60) +" min");
		System.out.println("Runtime: "+TimeUnit.SECONDS.convert(t, TimeUnit.NANOSECONDS));
		

	}
}