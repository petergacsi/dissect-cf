package hu.uszeged.xml.model;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


//@XmlType( propOrder = { "name", "freq", "sensor" ,"time" ,"maxinbw", "maxoutbw", "diskbw", "repository" , "repofilesize","ratio", "strategy"} )
@XmlRootElement( name = "device" )
public class DeviceModel {

		String name;
		int number;
		long freq;
		int sensor;
		long filesize;
		long starttime;
		long stoptime;
		long maxinbw;
		long maxoutbw;
		long diskbw;
		String repository;
		long repofilesize;
		double ratio;
		String strategy;

		
		@Override
		public String toString() {
			return "DeviceModel [name=" + name + ", number=" + number + ", freq=" + freq + ", sensor=" + sensor
					+ ", filesize=" + filesize + ", starttime=" + starttime + ", stoptime=" + stoptime + ", maxinbw=" + maxinbw
					+ ", maxoutbw=" + maxoutbw + ", diskbw=" + diskbw + ", repository=" + repository + ", repofilesize="
					+ repofilesize + ", ratio=" + ratio + ", strategy=" + strategy + "]";
		}
		
		@XmlElement( name = "name" )
		public void setName(String name) {
			this.name = name;
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
		
		@XmlElement( name = "repository" )
		public void setRepository(String repository) {
			this.repository = repository;
		}
		
		@XmlElement( name = "repofilesize" )
		public void setRepofilesize(long repofilesize) {
			this.repofilesize = repofilesize;
		}
		
		@XmlElement( name = "ratio" )
		public void setRatio(double ratio) {
			this.ratio = ratio;
		}
		
		@XmlElement( name = "strategy" )
		public void setStrategy(String strategy) {
			this.strategy = strategy;
		}

		@XmlAttribute( name = "filesize", required = true )
		public void setfilesize(long filesize) {
			System.out.println("kurvaanyad");
			this.filesize = filesize;
		}

		@XmlAttribute( name = "number" )
		public void setNumber(int number) {
			System.out.println("kurvaanyad");
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
		

		public static void main(String[] args) throws JAXBException {
			  File file = new File( "/home/student/Desktop/dissect/dissect-cf/src/main/java/hu/uszeged/xml/model/WeatherStationM.xml" );
			  JAXBContext jaxbContext = JAXBContext.newInstance( DevicesModel.class );
			  Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			  DevicesModel device = (DevicesModel)jaxbUnmarshaller.unmarshal( file );
			  System.out.println( device );

		}
}
