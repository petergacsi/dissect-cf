package hu.uszeged.inf.iot.simulator.refactored;

import java.io.File;
import java.util.concurrent.TimeUnit;
import hu.uszeged.inf.iot.simulator.refactored.Device;
import hu.uszeged.inf.iot.simulator.refactored.Application;
import hu.uszeged.inf.iot.simulator.refactored.Application.VmCollector;
import hu.uszeged.inf.iot.simulator.refactored.ComputingAppliance;;

public abstract class ScenarioBase {
	final static String resourcePath = new StringBuilder(System.getProperty("user.dir")).
			append(File.separator).
			append("src").
			append(File.separator).
			append("main").
			append(File.separator).
			append("resources").
			append(File.separator).
			toString();
	
	 static void printInformation(long t) {
		System.out.println("~~Informations about the simulation:~~");
		double totalCost=0.0;
		long generatedData=0,processedData=0,arrivedData=0;
		int usedVM = 0;
		int tasks = 0;
		long timeout=Long.MIN_VALUE;
		for (ComputingAppliance c : ComputingAppliance.allComputingDevice) {
			System.out.println("computingAppliance: " + c.name);
			long highestStationStoptime=Long.MIN_VALUE;
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
				
				if (a instanceof FogApp) {
				FogApp app = (FogApp) a;
				
				
				for(Device d : app.ownStations) {
					generatedData+=d.sumOfGeneratedData;
					
					if(d.stopTime>highestStationStoptime)
						highestStationStoptime=d.stopTime;
				}
				if((a.stopTime-highestStationStoptime)>timeout) {
					timeout=(a.stopTime-highestStationStoptime);
					
				}
				System.out.println(a.name+" stations: " + app.ownStations.size()+ " cost:"+a.instance.calculateCloudCost(a.sumOfWorkTime));
				//System.out.println(a.providers);
				} else {
					
					System.out.println(a.name+ " cost:"+a.instance.calculateCloudCost(a.sumOfWorkTime));
	
					
				}
				
				
				
				
				
			}
			
			
			
			System.out.println();
		}
		System.out.println("VMs " + usedVM + " tasks: " + tasks);
		System.out.println("Generated/processed/arrived data: " + generatedData + "/" + processedData+ "/"+arrivedData);
		System.out.println("Cost: "+totalCost);
		System.out.println("timeout: "+((double)timeout/1000/60) +" min");
		System.out.println("Runtime: "+TimeUnit.SECONDS.convert(t, TimeUnit.NANOSECONDS));
		

	}
}