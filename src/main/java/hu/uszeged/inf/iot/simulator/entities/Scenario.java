package hu.uszeged.inf.iot.simulator.entities;

import java.io.File;
import java.util.concurrent.TimeUnit;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.uszeged.inf.iot.simulator.entities.Application.VmCollector;
import hu.uszeged.inf.iot.simulator.providers.Instance;

public class Scenario {
	static Cloud a,b,c;
	public static void main(String[] args) throws Exception {
		// XML config files
		String resourcePath = new StringBuilder(System.getProperty("user.dir")).
										append(File.separator).
										append("target").
										append(File.separator).
										append("resources").
										append(File.separator).
										toString();
		
		
		String CScloudfile=resourcePath+"/resources_cscs/LPDSCloud.xml"; // this one should use in scenario_1
		String CSstationfile=resourcePath+"/resources_cscs/WeatherStationL.xml"; // this one should use in scenario_1
		
		String cloudfile=resourcePath+"LPDSCloud.xml"; // this one should use in scenario_2
		String cloudfile3=resourcePath+"LPDSCloud3.xml"; // this one should use in scenario_3
		String stationfile=resourcePath+"wsF.xml"; // this one should use in scenario_2-3
		
		String appfile=resourcePath+"NEWApplication.xml";
		String instancefile=resourcePath+"NEWInstance.xml";
		String providerfile=resourcePath+"Pricing.xml";
				
		//newSetup
		String newScen=resourcePath+"/resources_cscs/new.xml";

		
		// Set up the clouds
		a= new Cloud(cloudfile3,"cloud1");
		b= new Cloud(cloudfile3,"cloud2");
		c = new Cloud(cloudfile3 ,"cloud3");
		// Load the virtual machine instances, the applications and finally the devices
		Instance.loadInstance(instancefile);
		Application.loadApplication(appfile);
		Station.loadDevice(newScen);
		//Provider.loadProvider(providerfile); 
		
		// Start the simulation
		long starttime = System.nanoTime();
		Timed.simulateUntilLastEvent();
		long stopttime = System.nanoTime();
		// Print some informations to the monitor / in file
		printInformation((stopttime-starttime));
		TimelineGenerator.generate();
	}
	
	private static void printInformation(long t) {
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
				totalCost+=a.instance.calculateCloudCost(a.sumOfWorkTime);
				processedData+=a.sumOfProcessedData;
				usedVM+=a.vmlist.size();
				
				for (VmCollector vmcl : a.vmlist) {
						tasks += vmcl.taskCounter;
						System.out.println(vmcl.id +" "+vmcl.vm + " tasks: " + vmcl.taskCounter + " worktime: " + vmcl.workingTime + " installed at: "
								+ vmcl.installed+" restarted: "+vmcl.restarted);
				}
				for(Device d : a.stations) {
					generatedData+=d.sumOfGeneratedData;
					
					if(d.stopTime>highestStationStoptime)
						highestStationStoptime=d.stopTime;
				}
				if((a.stopTime-highestStationStoptime)>timeout) {
					timeout=(a.stopTime-highestStationStoptime);
					
				}
				System.out.println(" stations: " + a.stations.size());
			}
			System.out.println();
		}
		System.out.println("VMs " + usedVM + " tasks: " + tasks);
		System.out.println("Generated/processed data: " + generatedData + "/" + processedData);
		System.out.println("Cost: "+totalCost);
		System.out.println("timeout: "+timeout/1000/60 +" min, real timeout: "+((timeout/1000/60)-15)+" min");
		System.out.println("Runtime: "+TimeUnit.SECONDS.convert(t, TimeUnit.NANOSECONDS));
		
		System.out.println(a.iaas.repositories.get(0));
		System.out.println(b.iaas.repositories.get(0));
		System.out.println(c.iaas.repositories.get(0));
	}
}