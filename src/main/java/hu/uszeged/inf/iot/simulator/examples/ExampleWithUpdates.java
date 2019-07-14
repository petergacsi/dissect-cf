package hu.uszeged.inf.iot.simulator.examples;

import java.util.HashMap;
import java.util.Map;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.fog.ComputingAppliance;
import hu.uszeged.inf.iot.simulator.fog.Station;
import hu.uszeged.inf.iot.simulator.providers.Instance;

public class ExampleWithUpdates {

public static void main(String[] args) throws Exception {
		
		String fogfile=ScenarioBase.resourcePath+"/fog_extension_example/LPDSFog.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"/fog_extension_example/LPDSCloud.xml";
		String appliancefile = ScenarioBase.resourcePath+"/fog_extension_example/Appliances.xml"; 
		String CSstationfile=ScenarioBase.resourcePath+"/fog_extension_example/WeatherStation.xml";
		String instancefile=ScenarioBase.resourcePath+"/fog_extension_example/InstanceIOT.xml";
		
		Map<String, String> iaasloaders = new HashMap<String, String>();
		iaasloaders.put("cloud", cloudfile);
		iaasloaders.put("fog", fogfile);
		
		Instance.loadInstance(instancefile);
		ComputingAppliance.loadAppliances(appliancefile, iaasloaders);
		
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
