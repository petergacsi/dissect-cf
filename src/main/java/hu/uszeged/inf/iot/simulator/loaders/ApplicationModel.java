package hu.uszeged.inf.iot.simulator.loaders;

import java.io.File;
import java.util.ArrayList;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement( name = "application" )
@XmlAccessorType(XmlAccessType.PROPERTY) 
public class ApplicationModel {

		public long tasksize;
		public String type;
		public String parentDevice;
		public String instance;
		public String provider;
		public long freq;
		public String name;
		
		@Override
		public String toString() {
			return "ApplicationModel [tasksize=" + tasksize + ", type=" + type + ", instance=" + instance
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

		@XmlElement( name = "type" )
		public void setType(String type) {
			this.type = type;
		}
		
		@XmlElement( name = "parentDevice" )
		public void setParentDevice(String parentDevice) {
			this.parentDevice = parentDevice;
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
