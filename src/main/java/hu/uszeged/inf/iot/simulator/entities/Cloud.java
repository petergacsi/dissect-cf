package hu.uszeged.inf.iot.simulator.entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.util.CloudLoader;

public class Cloud {

	public IaaSService iaas;
	public static HashMap<String, Cloud> clouds = new HashMap<String, Cloud>();
	public ArrayList<Application> applications;
	String name;
	
	public static Cloud addApplication(Application app,String cloud) {
		Cloud c = clouds.get(cloud);
		c.applications.add(app);
		return c;
	}
	
	public Cloud(String cloudfile,String name)throws IOException, SAXException, ParserConfigurationException {
		if (iaas == null) this.iaas = CloudLoader.loadNodes(cloudfile);
		applications = new ArrayList<Application>();
		Cloud.clouds.put(name,this);
		this.name=name;
	}
	
}
