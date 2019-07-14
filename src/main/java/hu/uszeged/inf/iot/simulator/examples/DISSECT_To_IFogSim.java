package hu.uszeged.inf.iot.simulator.examples;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.fog.ComputingAppliance;
import hu.uszeged.inf.iot.simulator.fog.Station;
import hu.uszeged.inf.iot.simulator.providers.Instance;

public class DISSECT_To_IFogSim {

	public static void main(String[] args){
		
		String fogfile=ScenarioBase.resourcePath+"/fog_extension_example/Scenario_DISSECT_To_IFogSim/LPDSFog.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"/fog_extension_example/Scenario_DISSECT_To_IFogSim/LPDSCloud.xml";
		String appliancefile = ScenarioBase.resourcePath+"/fog_extension_example/Scenario_DISSECT_To_IFogSim/Appliances.xml"; 
		String CSstationfile=ScenarioBase.resourcePath+"/fog_extension_example/Scenario_DISSECT_To_IFogSim/WeatherStation.xml";
		String instancefile=ScenarioBase.resourcePath+"/fog_extension_example/Scenario_DISSECT_To_IFogSim/InstanceIOT.xml";
		
		Map<String, String> iaasLoaders = new HashMap<String, String>();
		iaasLoaders.put("cloud", cloudfile);
		iaasLoaders.put("fog", fogfile);
		
		Instance.loadInstance(instancefile);
		
		try {
			ComputingAppliance.loadAppliances(appliancefile, iaasLoaders);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Station.loadDevice(CSstationfile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("");
		System.out.println("------------------------");
		
		
		// Start the simulation
		long starttime = System.nanoTime();
		Timed.simulateUntilLastEvent();
		long stopttime = System.nanoTime();
		ScenarioBase.printInformation((stopttime-starttime));
		
	}
	
}
