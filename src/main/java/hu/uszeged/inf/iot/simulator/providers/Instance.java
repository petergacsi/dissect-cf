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
