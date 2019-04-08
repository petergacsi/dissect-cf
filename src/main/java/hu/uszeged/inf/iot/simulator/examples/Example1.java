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
		
		
		String CScloudfile=ScenarioBase.resourcePath+"/resources_cscs/LPDSCloud.xml"; // this one should use in scenario_1
		String CSstationfile=ScenarioBase.resourcePath+"/resources_cscs/WeatherStationL.xml"; // this one should use in scenario_1
		
		String appfile=ScenarioBase.resourcePath+"NEWApplication.xml";
		String instancefile=ScenarioBase.resourcePath+"NEWInstance.xml";
		String providerfile=ScenarioBase.resourcePath+"Pricing.xml";
		
		/*
		String cloudfile=ScenarioBase.resourcePath+"LPDSCloud.xml"; // this one should use in scenario_2
		String cloudfile3=ScenarioBase.resourcePath+"LPDSCloud3.xml"; // this one should use in scenario_3
		String stationfile=ScenarioBase.resourcePath+"wsF.xml"; // this one should use in scenario_2-3
		String newScen=ScenarioBase.resourcePath+"/resources_cscs/new.xml"; // this one should use in scenario_4
		*/
		
		// Set up the clouds
		new Cloud(CScloudfile,"cloud1");
		new Cloud(CScloudfile,"cloud2");
		new Cloud(CScloudfile ,"cloud3");
		// Load the virtual machine instances, the applications and finally the devices
		Instance.loadInstance(instancefile);
		Application.loadApplication(appfile);
		Station.loadDevice(CSstationfile);
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
