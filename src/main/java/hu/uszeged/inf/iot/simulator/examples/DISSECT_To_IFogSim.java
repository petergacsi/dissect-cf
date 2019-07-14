package hu.uszeged.inf.iot.simulator.examples;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.fog.ComputingAppliance;
import hu.uszeged.inf.iot.simulator.fog.Station;
import hu.uszeged.inf.iot.simulator.providers.Instance;

public class DISSECT_To_IFogSim {

	public static void main(String[] args) throws Exception{
		
		String fogfile=ScenarioBase.resourcePath+"/fog_extension_example/Scenario_DISSECT_To_IFogSim/LPDSFog.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"/fog_extension_example/Scenario_DISSECT_To_IFogSim/LPDSCloud.xml";
		String appliancefile = ScenarioBase.resourcePath+"/fog_extension_example/Scenario_DISSECT_To_IFogSim/Appliances.xml"; 
		String CSstationfile=ScenarioBase.resourcePath+"/fog_extension_example/Scenario_DISSECT_To_IFogSim/WeatherStation.xml";
		String instancefile=ScenarioBase.resourcePath+"/fog_extension_example/Scenario_DISSECT_To_IFogSim/InstanceIOT.xml";
		

		Instance.loadInstance(instancefile);
		ComputingAppliance.loadAppliances(appliancefile, fogfile);
		
		Station.loadDevice(CSstationfile);
		
		System.out.println("");
		System.out.println("------------------------");
		

		// TODO: IoT pricing set-up
		
		// Start the simulation
		long starttime = System.nanoTime();
		Timed.simulateUntilLastEvent();
		long stopttime = System.nanoTime();
		// Print some informations to the monitor / in file
		ScenarioBase.printInformation((stopttime-starttime));
		//TimelineGenerator.generate();
		
	}
	
}
