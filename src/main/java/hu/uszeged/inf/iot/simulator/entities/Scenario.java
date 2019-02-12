package hu.uszeged.inf.iot.simulator.entities;

import java.io.File;
import java.util.concurrent.TimeUnit;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.uszeged.inf.iot.simulator.entities.Application.VmCollector;
import hu.uszeged.inf.iot.simulator.providers.Instance;
import hu.uszeged.inf.iot.simulator.providers.Provider;

public class Scenario {

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
		String cloudfile2=resourcePath+"LPDSCloud2.xml"; // this one should use in scenario_3
		String stationfile=resourcePath+"wsF.xml"; // this one should use in scenario_2-3
		
		String appfile=resourcePath+"NEWApplication.xml";
		String instancefile=resourcePath+"NEWInstance.xml";
		String providerfile=resourcePath+"Pricing.xml";
				
		//newSetup
		String newScen=resourcePath+"/resources_cscs/new.xml";

		
		// Set up the clouds
		new Cloud(CScloudfile,"cloud1");
		new Cloud(cloudfile,"cloud2");
		new Cloud(cloudfile2,"cloud3");
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
	}
	
	private static void printInformation(long t) {
		System.out.println("~~Informations about the simulation:~~");
		double totalCost=0.0;
		long generatedData=0,processedData=0;
		int usedVM = 0;
		int tasks = 0;
		
		for (Cloud c : Cloud.clouds.values()) {
			System.out.println("cloud: " + c.name);
			for (Application a : c.applications) {

				totalCost+=a.instance.calculateCloudCost(a.sumOfWorkTime);
				processedData+=a.sumOfProcessedData;
				usedVM+=a.vmlist.size();
				
				for (VmCollector vmcl : a.vmlist) {

						tasks += vmcl.taskCounter;
						System.out.println(vmcl.id +" "+vmcl.vm + " tasks: " + vmcl.taskCounter + " worktime: " + vmcl.workingTime + " installed at: "
								+ vmcl.installed);
				}
				for(Device d : a.stations) {
					generatedData+=d.sumOfGeneratedData;
				}
				System.out.println(" stations: " + a.stations.size() + " stopped: "+a.stopTime);

			}
			System.out.println();
		}
		System.out.println("VMs " + usedVM + " tasks: " + tasks);
		System.out.println("Generated/processed data: " + generatedData + "/" + processedData);
		System.out.println(totalCost);
		System.out.println(TimeUnit.SECONDS.convert(t, TimeUnit.NANOSECONDS));
	}
}