package hu.uszeged.inf.iot.simulator.refactored;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class FogAppliance extends ComputingAppliance{
	public Application parentApp;
	

	public FogAppliance(String loadfile, String name, double x, double y) throws IOException, SAXException, ParserConfigurationException {
		super(loadfile, name, x, y);
		
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
