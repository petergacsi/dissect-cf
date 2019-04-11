package hu.uszeged.inf.iot.simulator.refactored;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class FogDevice extends ComputingAppliance{
	public Application parentApp;
	

	public FogDevice(String loadfile, String name) throws IOException, SAXException, ParserConfigurationException {
		super(loadfile, name);
		
	}
	
	public void addParentApp(Application app) {
		this.parentApp = app;
	}
	
	public String toString() {
		String apps = "";
		for (Application app : applications) {
			apps += app.name + " ";
		}
		return "ComputingAppliance name:" + name + " applications: " + apps
				+ " parentApp: " + parentApp.name; //+ " " + group;
	}

}
