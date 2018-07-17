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

	public static HashMap<String, Cloud> clouds = new HashMap<String, Cloud>(); // IaaSService iaas = clouds.get("cloud1");
	public ArrayList<Application> applications = new ArrayList<Application>();
	
	public Cloud(String cloudfile,String name)throws IOException, SAXException, ParserConfigurationException {
		if (iaas == null) this.iaas = CloudLoader.loadNodes(cloudfile);
		Cloud.clouds.put(name,this);
		
	/*	TODO CloudPricing myProvider = new ResourceDependentProvider(datafile, provider);
		CostAnalyserandPricer theCostAnalyser = new CostAnalyserandPricer(this.iaas);
		myProvider.setIaaSService(this.iaas);
		myProvider.setCostAnalyserandPricer(theCostAnalyser);
		new CostAnalizerListener(3600000,iaasList,dispatcher,theCostAnalyser);*/
	}
	
}
