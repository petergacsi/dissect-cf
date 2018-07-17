package hu.uszeged.inf.iot.simulator.entities;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.providers.Instance;

public class NEWScenario {

	public static void main(String[] args) throws Exception {
		// XML config files
		String cloudfile="/home/student/Desktop/markus/dissect-cf/src/main/resources/LPDSCloud.xml";
		String appfile="/home/student/Desktop/dissect/dissect-cf/src/main/java/hu/uszeged/xml/model/Application.xml";
		String instancefile="";
		String stationfile="";
		
		// Set up the clouds
		Cloud cloud1=new Cloud(cloudfile,"cloud1");
	
		// Load the virtual machine instances, the applications and finally the devices
		Instance.loadInstance(instancefile);
		Application.loadApplication(appfile);
		Station.loadDevice(stationfile);
		
		// Start the simulation
		Timed.simulateUntilLastEvent();
		
		// Print some informations to the monitor / in file
	}
}
