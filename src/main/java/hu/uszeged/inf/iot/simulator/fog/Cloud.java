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

package hu.uszeged.inf.iot.simulator.fog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.util.CloudLoader;

/**
 * This class includes an infrastructure as a service (IaaS) which serves applications.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 *
 */
public class Cloud {

	/**
	 * A reference for the the IaaS service.
	 */
	private IaaSService iaas;
	
	/**
	 * This map contains the all generated clouds, the key should be the name of the cloud.
	 */
	private static HashMap<String, Cloud> clouds = new HashMap<String, Cloud>();
	
	/**
	 * This list contains the all of generated application which run on this cloud.
	 */
	private ArrayList<Application> applications;
	
	/**
	 * The Id of the cloud.
	 */
	private String name;

	/**
	 * A static method which pairs an application with a cloud.
	 * @param app The installed application. 
	 * @param cloud The cloud resources.
	 * @return Returns with the cloud.
	 */
	public static Cloud addApplication(Application app,String cloud) {
		Cloud c = getClouds().get(cloud);
		c.getApplications().add(app);
		return c;
	}
	
	/**
	 * Constructor for creating resources based on XML file. 
	 * @param cloudfile The path of the XML file, leave it to null and use setter method otherwise.
	 * @param name The Id of the cloud.
	 */
	public Cloud(String cloudfile,String name){
		if(cloudfile!=null) {
			try {
				this.setIaas(CloudLoader.loadNodes(cloudfile));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			Cloud.getClouds().put(name,this);
			this.name=name;
		}
		this.applications=new ArrayList<Application>();
	}

	/**
	 * Getter method for the IaaS Service.
	 */
	public IaaSService getIaas() {
		return iaas;
	}

	/**
	 * Setter method for the IaaS Service.
	 */
	public void setIaas(IaaSService iaas) {
		this.iaas = iaas;
	}

	/**
	 * Getter method for the clouds map.
	 */
	public static HashMap<String, Cloud> getClouds() {
		return clouds;
	}

	/**
	 * Getter method for the application list.
	 */
	public ArrayList<Application> getApplications() {
		return applications;
	}

	/**
	 * Getter method for the cloud Id.
	 */
	public String getName() {
		return name;
	}




	
}
