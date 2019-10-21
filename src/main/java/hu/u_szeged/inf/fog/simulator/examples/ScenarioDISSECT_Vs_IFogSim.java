package hu.u_szeged.inf.fog.simulator.examples;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.u_szeged.inf.fog.simulator.application.Application;
import hu.u_szeged.inf.fog.simulator.iot.Station;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.providers.Instance;

public class ScenarioDISSECT_Vs_IFogSim {
	
	private static String SCENARIO = "CompareToIFogSim_XMLs";
	
	public static void main(String[] args) {
			
		
		
/*
		String fogfile_type1=ScenarioBase.resourcePath+"/"+ SCENARIO +"/LPDSFog_type1.xml"; 
		String fogfile_type2=ScenarioBase.resourcePath+"/"+ SCENARIO +"/LPDSFog_type2.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"/"+ SCENARIO +"/LPDSCloud.xml";
		String appliancefile = ScenarioBase.resourcePath+"/"+ SCENARIO +"/Scenario_3_45_160.xml";
		String stationfile = ScenarioBase.resourcePath+"/"+ SCENARIO +"/Stations.xml";
		String instancefile=ScenarioBase.resourcePath+"/"+ SCENARIO +"/InstanceIOT.xml";
*/
		// iFogSim comparison:
		
		String fogfile_type1=ScenarioBase.resourcePath+"/"+ SCENARIO +"/1CPUStrong_Fog.xml"; 
		String fogfile_type2=ScenarioBase.resourcePath+"/"+ SCENARIO +"/3CPUStrong_Fog.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"/"+ SCENARIO +"/45CPUStrong_Cloud.xml";
		String appliancefile = ScenarioBase.resourcePath+"/"+ SCENARIO +"/Scenario_1_4_20.xml";
		String stationfile = ScenarioBase.resourcePath+"/"+ SCENARIO +"/Stations_80.xml";
		String instancefile=ScenarioBase.resourcePath+"/"+ SCENARIO +"/InstanceOf_1CPUStrongVM.xml";

		
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
		ScenarioBase.printInformation((stopttime-starttime),false);
	}

}
