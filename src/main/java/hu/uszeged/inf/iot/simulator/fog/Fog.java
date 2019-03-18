package hu.uszeged.inf.iot.simulator.fog;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class Fog extends Cloud{
	
	Cloud cloud;
	static ArrayList<Fog> fogs = new ArrayList<Fog>();

	public Fog(String cloudfile, String name) throws IOException, SAXException, ParserConfigurationException {
		super(cloudfile, name);
		fogs.add(this);
	}

	public static void addFog(Cloud c, Fog f) {
		f.cloud=c;
		c.fogs.add(f);
	}
}
