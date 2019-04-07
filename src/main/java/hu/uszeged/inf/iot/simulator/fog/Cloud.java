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

public class Cloud {

	public IaaSService iaas;
	public static HashMap<String, Cloud> clouds = new HashMap<String, Cloud>();
	public ArrayList<Application> applications;
	public String name;
	public Cloud cloud;
	public Application app;
	public static Cloud addApplication(Application app,String cloud) {
		Cloud c = clouds.get(cloud);
		c.applications.add(app);
		return c;
	}
	
	public Cloud(String cloudfile,String name)throws IOException, SAXException, ParserConfigurationException {
		if(cloudfile!=null) {
			this.iaas = CloudLoader.loadNodes(cloudfile);
			Cloud.clouds.put(name,this);
			this.name=name;
		}
		applications = new ArrayList<Application>();
	}
	
}
