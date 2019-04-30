package hu.uszeged.inf.iot.simulator.refactored;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class CloudAppliance extends ComputingAppliance{
	
	public CloudAppliance(String loadfile, String name, double x , double y) throws IOException, SAXException, ParserConfigurationException {
		super(loadfile, name, x, y);
		
	}
	
}
