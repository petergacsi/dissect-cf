package hu.uszeged.inf.iot.simulator.examples;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.entities.Station;
import hu.uszeged.inf.iot.simulator.fog.Application;
import hu.uszeged.inf.iot.simulator.fog.Cloud;
import hu.uszeged.inf.iot.simulator.providers.Instance;
import hu.uszeged.inf.iot.simulator.providers.Provider;
import hu.uszeged.inf.iot.simulator.util.TimelineGenerator;

public class Example1 {

	public static void main(String[] args) throws Exception {
		
		String instancefile = ScenarioBase.resourcePath+"Instance.xml";
		String providerfile = ScenarioBase.resourcePath+"Pricing.xml";
		String appfile = ScenarioBase.resourcePath+"Application.xml";
		
		String cloudfile1 = ScenarioBase.resourcePath+"LPDS-1.xml";
		String cloudfile2 = ScenarioBase.resourcePath+"LPDS-2.xml";
		String cloudfile3 = ScenarioBase.resourcePath+"LPDS-3.xml";
		
		// 1. scenario
		String devices1 = ScenarioBase.resourcePath+"Devices-1.xml";
		
		// 2-3. scenario
		String devices23 = ScenarioBase.resourcePath+"Devices-2-3.xml";
		
		// 4. scenario
		String devices4= ScenarioBase.resourcePath+"Devices-4.xml";
		
		// Set up the clouds
		new Cloud(cloudfile1,"cloud1");
		new Cloud(cloudfile1,"cloud2");
		new Cloud(cloudfile1 ,"cloud3");
		// Load the virtual machine instances, the applications and finally the devices
		Instance.loadInstance(instancefile);
		Application.loadApplication(appfile);
		Station.loadDevice(devices1);
		Provider.loadProvider(providerfile); 
		
		// Start the simulation
		long starttime = System.nanoTime();
		Timed.simulateUntilLastEvent();
		long stopttime = System.nanoTime();
		// Print some informations to the monitor / in file
		ScenarioBase.printInformation((stopttime-starttime));
		TimelineGenerator.generate();
	}

}
