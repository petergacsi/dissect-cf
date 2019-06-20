package hu.uszeged.inf.iot.simulator.refactored;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import hu.uszeged.inf.iot.simulator.providers.Instance;

public class Printing {

	public static void main(String[] args) {
		String fogfile=ScenarioBase.resourcePath+"/fog_extension_example/LPDSFog.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"/fog_extension_example/LPDSCloud.xml";
		String appliancefile = ScenarioBase.resourcePath+"/fog_extension_example/Appliances.xml"; 
		String instancefile=ScenarioBase.resourcePath+"/fog_extension_example/InstanceIOT.xml";
		String CSstationfile=ScenarioBase.resourcePath+"/fog_extension_example/WeatherStation.xml";

		
		Instance.loadInstance(instancefile);
		
		
		try {
			ComputingAppliance.loadAppliances(appliancefile, fogfile);
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
		
		Application.makeAllReleations();
		try {
			Station.loadDevice(CSstationfile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		List<ApplianceModel> appliances = null;
//		try {
//			 appliances = ApplianceModel.loadAppliancesXML(appliancefile);
//		} catch (JAXBException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		for (ApplianceModel am : appliances) {
//			System.out.println(am.name);
//			System.out.println(am.xcoord);
//			System.out.println(am.ycoord);
//			System.out.println(am.applications.get(0).childDevice);
//			System.out.println(am.neighbourAppliances);
//			System.out.println("------");
//		} 
//		
//		
		for (Application app: Application.applications) {
			System.out.println(app);
			System.out.println("-----");
			for (ComputingAppliance ca : app.childComputingDevice) {
				System.out.println(ca);
			}
			System.out.println("-----");
		}
		
	}

}
