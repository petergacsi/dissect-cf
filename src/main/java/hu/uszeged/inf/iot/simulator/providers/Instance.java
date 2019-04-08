/*
 *  ========================================================================
 *  DIScrete event baSed Energy Consumption simulaTor 
 *    					             for Clouds and Federations (DISSECT-CF)
 *  ========================================================================
 *  
 *  This file is part of DISSECT-CF.
 *  
 *  DISSECT-CF is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at
 *  your option) any later version.
 *  
 *  DISSECT-CF is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *  General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with DISSECT-CF.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  (C) Copyright 2019, Andras Markus (markusa@inf.u-szeged.hu)
 */

package hu.uszeged.inf.iot.simulator.providers;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.uszeged.inf.xml.model.InstanceModel;

/**
 * This class represents all of the necessary data for applying virtual machines in applications.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
public class Instance {
	
	/**
	 * The virtual machine image.
	 */
	private VirtualAppliance va;
	
	/**
	 * The Id of the instance.
	 */
	String name;
	
	/**
	 * The resource need of the virtual machine.
	 */
	private AlterableResourceConstraints arc;
	
	/**
	 * The price of one time unit.
	 */
	private double pricePerTick;
	
	/**
	 * It stores the final cost of the instance.
	 */
	private double cost;
	
	/**
	 * Map for the all of possible instances. The key should be the Id of the instance.
	 */
	private static HashMap<String,Instance> instances = new HashMap<String,Instance>();
	
	/**
	 * With this method we can load many instances from XML file.
	 * @param instancefile  The path of the XML file.
	 */
	public static void loadInstance(String instancefile) {
		try {
			for(InstanceModel im : InstanceModel.loadInstanceXML(instancefile)) {
				Instance i = new Instance(new VirtualAppliance(im.name, im.startupProcess, im.networkLoad, false, im.reqDisk), 
								new AlterableResourceConstraints(im.cpuCores,im.coreProcessingPower,im.ram), im.pricePerTick,im.name);
				getInstances().put(i.name,i);
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The constructor initializes the variables.
	 * @param va The VM image.
	 * @param arc The resource need of the VM.
	 * @param pricePerTick The time unit cost of the VM.
	 * @param name The Id of VM.
	 */
	public Instance(VirtualAppliance va,AlterableResourceConstraints arc,double pricePerTick,String name){
		this.va=(va);
		this.arc=(arc);
		this.pricePerTick=(pricePerTick);
		this.name=name;
		getInstances().put(this.name,this);
	}
	
	/**
	 * It calculates the actual cost of the VM.
	 * @param time The elapsed time.
	 * @return The elapsed time * unit price.
	 */
	public double calculateCloudCost(long time){
		cost=time*getPricePerTick();
		return time*getPricePerTick();
	}

	/**
	 * Getter for the resource need of the VM.
	 */
	public AlterableResourceConstraints getArc() {
		return arc;
	}
	
	/**
	 * Getter for the unit price.
	 */
	public double getPricePerTick() {
		return pricePerTick;
	}

	/**
	 * Getter for the VM image.
	 */
	public VirtualAppliance getVa() {
		return va;
	}

	/**
	 * Getter for the instance map.
	 */
	public static Map<String,Instance> getInstances() {
		return instances;
	}

}
