package hu.uszeged.inf.iot.simulator.refactored;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class CloudDevice extends ComputingAppliance{
	
	public CloudDevice(String loadfile, String name) throws IOException, SAXException, ParserConfigurationException {
		super(loadfile, name);
		
	}
	
}
