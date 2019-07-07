package hu.uszeged.inf.iot.simulator.refactored;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.uszeged.inf.iot.simulator.refactored.ScenarioBase;
import hu.uszeged.inf.iot.simulator.providers.Instance;
import hu.uszeged.inf.iot.simulator.refactored.Device.DeviceNetwork;

public class Example2Refactored {

public static void main(String[] args) throws Exception {
		
		String fogfile=ScenarioBase.resourcePath+"/fog_extension_example/LPDSFog.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"/fog_extension_example/LPDSCloud.xml";
		String appliancefile = ScenarioBase.resourcePath+"/fog_extension_example/Appliances.xml"; 
		String CSstationfile=ScenarioBase.resourcePath+"/fog_extension_example/WeatherStation.xml";
		String instancefile=ScenarioBase.resourcePath+"/fog_extension_example/InstanceIOT.xml";
		

		Instance.loadInstance(instancefile);
		ComputingAppliance.loadAppliances(appliancefile, fogfile);
		
//		Application.makeAllReleations();
		Station.loadDevice(CSstationfile);
		
		System.out.println("");
		System.out.println("------------------------");
		
		
		
		
//		System.out.println();
//		Print out the topology
//			//Devices and their apps
//		for (Application app : Application.applications)
//			//throws null
//			for (ComputingAppliance computingAppliance : app.childComputingDevice) {
//				System.out.println(computingAppliance);
//				System.out.println(computingAppliance.neighbours);
//				System.out.println("ParentApp: " + computingAppliance.parentApp.name);
//			}
//		
//		
//		
//		
//			Stations and their parent FogApps
//		for (FogApp fogApp : Application.fogApplications) {
//			System.out.println(fogApp);
//			for (Device d : fogApp.ownStations) {
//				System.out.print(d);
//			}
//			System.out.println("");
//		}
//		System.out.println();
		
		
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
