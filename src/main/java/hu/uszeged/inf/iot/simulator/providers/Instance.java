package hu.uszeged.inf.iot.simulator.providers;

import java.util.HashMap;

import javax.xml.bind.JAXBException;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.uszeged.inf.xml.model.InstanceModel;

public class Instance {
	public VirtualAppliance va;
	String name;
	public AlterableResourceConstraints arc;
	public double pricePerTick;
	public double cost;
	
	public static HashMap<String,Instance> instances = new HashMap<String,Instance>();
	
	public static void loadInstance(String instancefile) throws JAXBException {
		for(InstanceModel im : InstanceModel.loadInstanceXML(instancefile)) {
			Instance i = new Instance(new VirtualAppliance(im.name, im.startupProcess, im.networkLoad, false, im.reqDisk), 
							new AlterableResourceConstraints(im.cpuCores,im.coreProcessingPower,im.ram), im.pricePerTick,im.name);
			instances.put(i.name,i);
		}
	}
	
	public Instance(VirtualAppliance va,AlterableResourceConstraints arc,double pricePerTick,String name){
		this.va=va;
		this.arc=arc;
		this.pricePerTick=pricePerTick;
		this.name=name;
		instances.put(this.name,this);
	}
	
	public double calculateCloudCost(long time){
		cost=time*pricePerTick;
		return time*pricePerTick;
	}
}
