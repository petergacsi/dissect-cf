package hu.uszeged.inf.iot.simulator.entities;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.uszeged.inf.iot.simulator.entities.Application.VmCollector;
import hu.uszeged.inf.iot.simulator.providers.Instance;

public class Scenario {

	public static void main(String[] args) throws Exception {
		// XML config files
		String resource = "/home/student/Desktop/dissect/dissect-cf/src/main/resources/";
		String cloudfile=resource+"LPDSCloud.xml";
		String cloudfile2=resource+"LPDSCloud2.xml";
		String appfile=resource+"NEWApplication.xml";
		String instancefile=resource+"NEWInstance.xml";
		String stationfile=resource+"NEWWeatherStation.xml";
		
		// Set up the clouds
		new Cloud(cloudfile,"cloud1");
		new Cloud(cloudfile2,"cloud2");
		
		// Load the virtual machine instances, the applications and finally the devices
		Instance.loadInstance(instancefile);
		Application.loadApplication(appfile);
		Station.loadDevice(stationfile);
		
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
						System.out.println(vmcl.vm + " tasks: " + vmcl.tasknumber + " worktime: " + vmcl.workingTime + " installed at: "
								+ vmcl.installed);
					

				}
				System.out.println(	a.name + " VMs " + usedVM + " tasks: " + tasks + " stations: " + a.stations.size());

			}
			for (PhysicalMachine pm : c.iaas.machines) {
				System.out.println(pm);
			}
			System.out.println("\n");
		}
		System.out.println("\n");
		System.out.println("Generated/processed data: " + Station.allstationsize + "/" + Application.allprocessed);
	}
}