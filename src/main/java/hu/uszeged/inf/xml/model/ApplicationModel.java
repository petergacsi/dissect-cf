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

package hu.uszeged.inf.xml.model;

import java.io.File;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** This class is a helper class for creating objects from XML files using JAXB.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
@XmlRootElement( name = "application" )
@XmlAccessorType(XmlAccessType.PROPERTY) 
public class ApplicationModel {

		public long tasksize;
		public String cloud;
		public String instance;
		public String provider;
		public long freq;
		public String name;
		
		@Override
		public String toString() {
			return "ApplicationModel [tasksize=" + tasksize + ", cloud=" + cloud + ", instance=" + instance
					+ ", iotPricing=" + provider + ", freq=" + freq + ", name=" + name + "]";
		}
		@XmlElement( name = "name" )
		public void setName(String name) {
			this.name = name;
		}

		@XmlAttribute( name = "tasksize", required = true )
		public void setTasksize(long tasksize) {
			this.tasksize = tasksize;
		}

		@XmlElement( name = "cloud" )
		public void setCloud(String cloud) {
			this.cloud = cloud;
		}

		@XmlElement( name = "instance" )
		public void setInstance(String instance) {
			this.instance = instance;
		}

		@XmlElement( name = "provider" )
		public void setIotPricing(String provider) {
			this.provider = provider;
		}
		
		@XmlElement( name = "freq" )
		public void setFreq(long freq) {
			this.freq = freq;
		}

		public static ArrayList<ApplicationModel> loadApplicationXML(String appfile)throws JAXBException{
				 File file = new File( appfile);
				 JAXBContext jaxbContext = JAXBContext.newInstance( ApplicationsModel.class );
				 Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				 ApplicationsModel app = (ApplicationsModel)jaxbUnmarshaller.unmarshal( file );				 
				 return app.applicationList;
		}

}
