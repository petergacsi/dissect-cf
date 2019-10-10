package hu.uszeged.inf.iot.simulator.examples;

import java.io.File;
import java.util.concurrent.TimeUnit;

import hu.uszeged.inf.iot.simulator.fog.Application;
import hu.uszeged.inf.iot.simulator.fog.Application.VmCollector;
import hu.uszeged.inf.iot.simulator.fog.ComputingAppliance;
import hu.uszeged.inf.iot.simulator.fog.Device;
import hu.uszeged.inf.iot.simulator.fog.FogApp;
import hu.uszeged.inf.iot.simulator.fog.Station;;

public abstract class ScenarioBase {
		public final static String resourcePath = new StringBuilder(System.getProperty("user.dir")).
			append(File.separator).
			append("src").
			append(File.separator).
			append("main").
			append(File.separator).
			append("resources").
			append(File.separator).
			append("fog_extension").
			append(File.separator).
			toString();
	
	 static void printInformation(long t) {
		System.out.println("~~Informations about the simulation:~~");
		double totalCost=0.0;
		long generatedData=0,processedData=0,arrivedData=0;
		int usedVM = 0;
		int tasks = 0;
		long timeout=Long.MIN_VALUE;
		long highestApplicationStopTime = Long.MIN_VALUE;
		long highestStationStoptime = Long.MIN_VALUE;
		for (ComputingAppliance c : ComputingAppliance.allComputingAppliance) {
			System.out.println("computingAppliance: " + c.name);
			//long highestStationStoptime=Long.MIN_VALUE;
			for (Application a : c.applications) {
				System.out.println(a.name);
				totalCost+=a.instance.calculateCloudCost(a.sumOfWorkTime);
				processedData+=a.sumOfProcessedData;
				arrivedData+=a.sumOfArrivedData;
				usedVM+=a.vmlist.size();
				
				
				for (VmCollector vmcl : a.vmlist) {
						tasks += vmcl.taskCounter;
						System.out.println(vmcl.id +" "+vmcl.vm + " tasks: " + vmcl.taskCounter + " worktime: " + vmcl.workingTime + " installed at: "
								+ vmcl.installed+" restarted: "+vmcl.restarted);
				}
				
				
				for(Device d : a.ownStations) {
					generatedData+=d.sumOfGeneratedData;
						
					if(d.stopTime>highestStationStoptime) {
						highestStationStoptime=d.stopTime;
					}
				}
				
				if (a.stopTime > highestApplicationStopTime) {
					highestApplicationStopTime = a.stopTime;
				}
				
				System.out.println(a.name+" stations: " + a.ownStations.size()+ " cost:"+a.instance.calculateCloudCost(a.sumOfWorkTime));
				
			}
			
			
			
			System.out.println();
		}
		timeout = highestApplicationStopTime - highestStationStoptime;
		System.out.println("VMs " + usedVM + " tasks: " + tasks);
		System.out.println("Generated/processed/arrived data: " + generatedData + "/" + processedData+ "/"+arrivedData);
		System.out.println("Cost: "+totalCost);
		System.out.println("Last applicationStoptime: " + highestApplicationStopTime);
		System.out.println("Last station to stop: " + highestStationStoptime);
		System.out.println("timeout: "+((double)timeout/1000/60) +" min");
		System.out.println("Runtime: "+TimeUnit.MILLISECONDS.convert(t, TimeUnit.NANOSECONDS));
		

	}
}