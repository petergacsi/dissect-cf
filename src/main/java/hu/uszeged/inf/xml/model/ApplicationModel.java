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


@XmlRootElement( name = "application" )
@XmlAccessorType(XmlAccessType.PROPERTY) 
public class ApplicationModel {

		public long tasksize;
		public String cloud;
		public String instance;
		public String iotPricing;
		public long freq;
		public String name;
		
		@Override
		public String toString() {
			return "ApplicationModel [tasksize=" + tasksize + ", cloud=" + cloud + ", instance=" + instance
					+ ", iotPricing=" + iotPricing + ", freq=" + freq + ", name=" + name + "]";
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

		@XmlElement( name = "iot-pricing" )
		public void setIotPricing(String iotPricing) {
			this.iotPricing = iotPricing;
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
				 // System.out.println( app );
				 
				 return app.applicationList;
		}
			
			 

		
}
