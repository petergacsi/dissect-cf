package hu.uszeged.inf.iot.simulator.examples;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.fog.Application;
import hu.uszeged.inf.iot.simulator.fog.ComputingAppliance;
import hu.uszeged.inf.iot.simulator.fog.Station;
import hu.uszeged.inf.iot.simulator.providers.Instance;

public class ScenarioFogvsCloud {
	
	private static String SCENARIO = "FogVsCloud_XMLs";
	
	public static void main(String[] args) {
			
		
		
		String fogfile_type1=ScenarioBase.resourcePath+"/"+ SCENARIO +"/LPDSFog_type1.xml"; 
		String fogfile_type2=ScenarioBase.resourcePath+"/"+ SCENARIO +"/LPDSFog_type2.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"/"+ SCENARIO +"/LPDSCloud.xml";
//		String appliancefile = ScenarioBase.resourcePath+"/generatingXML/output3.xml"; 
//		String stationfile=ScenarioBase.resourcePath+"/generatingXML/stations_output.xml";
		String appliancefile = ScenarioBase.resourcePath+"/"+ SCENARIO +"/Scenario_3_45_160.xml";
		String stationfile = ScenarioBase.resourcePath+"/"+ SCENARIO +"/Stations.xml";
		String instancefile=ScenarioBase.resourcePath+"/"+ SCENARIO +"/InstanceIOT.xml";
		
		Map<String, String> iaasloaders = new HashMap<String, String>();
		iaasloaders.put("cloud", cloudfile);
		iaasloaders.put("fog_type1", fogfile_type1);
		iaasloaders.put("fog_type2", fogfile_type2);
		
		Instance.loadInstance(instancefile);
		try {
			ComputingAppliance.loadAppliances(appliancefile, iaasloaders);
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
			Station.loadDevice(stationfile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("");
		System.out.println("------------------------");
		
		
		
		// TODO: IoT pricing set-up
		
		// Start the simulation
		long starttime = System.nanoTime();
		Timed.simulateUntilLastEvent();
		long stopttime = System.nanoTime();
		// Print some informations to the monitor / in file
		ScenarioBase.printInformation((stopttime-starttime));
	}

}
