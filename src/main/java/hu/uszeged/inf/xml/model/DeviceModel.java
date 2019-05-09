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
@XmlRootElement( name = "device" )
@XmlAccessorType(XmlAccessType.PROPERTY) 
public class DeviceModel {

	public String name;
	public int number;
	public long freq;
	public int sensor;
	public long filesize;
	public long starttime;
	public long stoptime;
	public long maxinbw;
	public long maxoutbw;
	public long diskbw;
	public long reposize;
	public String strategy;
	public ShutdownModel sm;
		

		@XmlElement( name = "name" )
		public void setName(String name) {
			this.name = name;
		}

		@XmlElement( name = "shutdown" )
		public void setSm(ShutdownModel sm) {
			this.sm = sm;
		}

		@XmlElement( name = "freq" )
		public void setFreq(long freq) {
			this.freq = freq;
		}
		
		@XmlElement( name = "sensor" )
		public void setSensor(int sensor) {
			this.sensor = sensor;
		}
		
		@XmlElement( name = "maxinbw" )
		public void setMaxinbw(long maxinbw) {
			this.maxinbw = maxinbw;
		}
		
		@XmlElement( name = "maxoutbw" )
		public void setMaxoutbw(long maxoutbw) {
			this.maxoutbw = maxoutbw;
		}
		
		@XmlElement( name = "diskbw" )
		public void setDiskbw(long diskbw) {
			this.diskbw = diskbw;
		}
		
		
		@XmlElement( name = "reposize" )
		public void setReposize(long reposize) {
			this.reposize = reposize;
		}
				
		@XmlElement( name = "strategy" )
		public void setStrategy(String strategy) {
			this.strategy = strategy;
		}

		@XmlAttribute( name = "filesize", required = true )
		public void setfilesize(long filesize) {
			this.filesize = filesize;
		}

		@XmlAttribute( name = "number" )
		public void setNumber(int number) {

			this.number = number;
		}
		@XmlAttribute( name = "starttime", required = true )
		public void setStarttime(long starttime) {
			this.starttime =starttime;
		}

		@XmlAttribute( name = "stoptime", required = true )
		public void setStoptime(long stoptime) {
			this.stoptime = stoptime;
		}
		

		public static ArrayList<DeviceModel>  loadDeviceXML(String stationfile) throws JAXBException {
			  File file = new File( stationfile );
			  JAXBContext jaxbContext = JAXBContext.newInstance( DevicesModel.class );
			  Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			  DevicesModel device = (DevicesModel)jaxbUnmarshaller.unmarshal( file );
			  return device.deviceList;

		}
}
