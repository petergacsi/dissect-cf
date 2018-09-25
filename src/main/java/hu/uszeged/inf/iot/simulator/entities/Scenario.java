package hu.uszeged.inf.iot.simulator.entities;

import java.io.File;

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
		String cloudfile=resourcePath+"LPDSCloud.xml";
		String cloudfile2=resourcePath+"LPDSCloud2.xml";
		String cloudfile3=resourcePath+"LPDSCloud3.xml";
		String appfile=resourcePath+"NEWApplication.xml";
		String instancefile=resourcePath+"NEWInstance.xml";
		String stationfile=resourcePath+"wsR.xml";
		String providerfile=resourcePath+"Pricing.xml";
		
		// Set up the clouds
		new Cloud(cloudfile,"cloud1");
		new Cloud(cloudfile2,"cloud2");
		new Cloud(cloudfile3,"cloud3");
		// Load the virtual machine instances, the applications and finally the devices
		Instance.loadInstance(instancefile);
		Application.loadApplication(appfile);
		Station.loadDevice(stationfile);
		Provider.loadProvider(providerfile); 
		
		// Start the simulation
		Timed.simulateUntilLastEvent();
		
		// Print some informations to the monitor / in file
		printInformation();
	}
	
	private static void printInformation() {
		System.out.println("~~Informations about the simulation:~~");

		for (Cloud c : Cloud.clouds.values()) {
			System.out.println("cloud: " + c.name);

			for (Application a : c.applications) {
				int usedVM = 0;
				int tasks = 0;
				for (VmCollector vmcl : a.vmlist) {
					
						usedVM++;
						tasks += vmcl.tasknumber;
						System.out.println(vmcl.id +" "+vmcl.vm + " tasks: " + vmcl.tasknumber + " worktime: " + vmcl.workingTime + " installed at: "
								+ vmcl.installed +" restarted: "+vmcl.restarted);
					

				}
				System.out.println(	a.name + " VMs " + usedVM + " tasks: " + tasks + " stations: " + a.stations.size());

			}
			for (PhysicalMachine pm : c.iaas.machines) {
				System.out.println(pm);
			}
			System.out.println("\n");
		}
		System.out.println("Generated/processed data: " + Station.allstationsize + "/" + Application.allprocessed);
	}
}