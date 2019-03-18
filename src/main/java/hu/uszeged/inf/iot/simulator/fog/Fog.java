package hu.uszeged.inf.iot.simulator.fog;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class Fog extends Cloud{

	public Fog(String cloudfile, String name) throws IOException, SAXException, ParserConfigurationException {
		super(cloudfile, name);
	}

}
