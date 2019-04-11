package hu.uszeged.inf.iot.simulator.refactored;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.util.CloudLoader;

public class ComputingAppliance {
	
	//we need every device for logging, so we need a list of all ComputingAppliance
	public static List<ComputingAppliance> allComputingDevice = new ArrayList<ComputingAppliance>();
	
	//Every device have computing capacity
	public IaaSService iaas;
	
	//A device holds a list with a reference to its applications
	public ArrayList<Application> applications;
	
	//A reference to a grouping of devices;
	//public Group group;
	
	//A unique identifier
	public String name;
	
	
	public ComputingAppliance(String loadfile, String name) throws IOException, SAXException, ParserConfigurationException {
		if (loadfile != null) {
			//Cloudloader is in charge for create the appropiate machines
			this.iaas = CloudLoader.loadNodes(loadfile);
			this.name = name;
			this.applications = new ArrayList<Application>();
			
			//store all device in a list for logging purpose
			ComputingAppliance.allComputingDevice.add(this);
		}
	}
		
	
	/*public void setGroup(Group group) {
		this.group = group;
	}
	
	public Group getGroup() {
		return group;
	}*/


	@Override
	public String toString() {
		String apps = "";
		for (Application app : applications) {
			apps += app.name + " ";
		}
		return "ComputingAppliance name:" + name + " applications: " + apps;
	}
	
	
	
	
}
