package hu.uszeged.inf.iot.simulator.entities;

import java.util.ArrayList;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.uszeged.inf.iot.simulator.providers.Instance;

public class NEWScenario {

	public static void main(String[] args) throws Exception {
		// XML config files
		String cloudfile="/home/andris/Dokumentumok/szte/projektek/dissect-cf/src/main/resources/LPDSCloud.xml";
		String appfile="/home/andris/Dokumentumok/szte/projektek/dissect-cf/src/main/resources/NEWApplication.xml	";
		String instancefile="/home/andris/Dokumentumok/szte/projektek/dissect-cf/src/main/resources/NEWInstance.xml";
		String stationfile="/home/andris/Dokumentumok/szte/projektek/dissect-cf/src/main/resources/NEWWeatherStation.xml";
		
		// Set up the clouds
		new Cloud(cloudfile,"cloud1");
	
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
		
		for(Cloud c : Cloud.clouds.values()){
			System.out.println("cloud: "+c.name);
			for(PhysicalMachine pm : c.iaas.machines){
				System.out.println(pm);
			}
		}
		System.out.println("\n");
		System.out.println("Generated/processed data: "+Station.allstationsize +"/"+Application.allprocessed);
	}
}