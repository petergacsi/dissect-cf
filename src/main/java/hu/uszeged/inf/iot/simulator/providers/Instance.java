package hu.uszeged.inf.iot.simulator.providers;

import java.util.HashMap;

import javax.xml.bind.JAXBException;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.uszeged.inf.iot.simulator.entities.Application;
import hu.uszeged.inf.xml.model.InstanceModel;

public class Instance {
	public VirtualAppliance va;
	String name;
	public AlterableResourceConstraints arc;
	public double pricePerTick;
	
	public static HashMap<String,Instance> instances = new HashMap<String,Instance>();
	
	public static void loadInstance(String instancefile) throws JAXBException {
		for(InstanceModel im : InstanceModel.loadInstanceXML(instancefile)) {
			Instance i = new Instance();
			i.va = new VirtualAppliance(im.name, im.startupProcess, im.networkLoad, false, im.reqDisk);
			i.arc = new AlterableResourceConstraints(im.cpuCores,im.coreProcessingPower,im.ram);
			i.name=im.name; 
			i.pricePerTick=im.pricePerTick;
			instances.put(i.name,i);
		}
	}
	
	public double calculateCloudCost(long time){
		return time*pricePerTick;
	}
}
