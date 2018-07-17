package hu.uszeged.xml.model;

import java.io.File;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlType( propOrder = { "ram", "cpuCores", "pricePerTick", "corProcessingPower" , "startupProcess","networkLoad", "reqDisk"} )
@XmlRootElement( name = "instance" )
public class InstanceModel {
	
		@Override
	public String toString() {
		return "Instances [ram=" + ram + ", cpuCores=" + cpuCores + ", pricePerTick=" + pricePerTick
				+ ", corProcessingPower=" + corProcessingPower + ", startupProcess=" + startupProcess + ", networkLoad="
				+ networkLoad + ", reqDisk=" + reqDisk + ", name=" + name + "]";
	}

		long ram;
		int cpuCores;
		double pricePerTick;
		double corProcessingPower;
		long startupProcess;
		long networkLoad;
		long reqDisk;
		String name;

		@XmlElement( name = "ram" )
		public void setRam(long ram) {
			this.ram = ram;
		}

		@XmlElement( name = "cpu-cores" )
		public void setCpuCores(int cpuCores) {
			this.cpuCores = cpuCores;
		}

		@XmlElement( name = "price-per-tick" )
		public void setPricePerTick(double pricePerTick) {
			this.pricePerTick = pricePerTick;
		}

		@XmlElement( name = "core-processing-power" )
		public void setCorProcessingPower(double corProcessingPower) {
			this.corProcessingPower = corProcessingPower;
		}

		@XmlElement( name = "startup-process" )
		public void setStartupProcess(long startupProcess) {
			this.startupProcess = startupProcess;
		}

		@XmlElement( name = "network-Load" )
		public void setNetworkLoad(long networkLoad) {
			this.networkLoad = networkLoad;
		}
		@XmlElement( name = "req-disk" )
		public void setReqDisk(long reqDisk) {
			this.reqDisk = reqDisk;
		}

		@XmlAttribute( name = "name", required = true )
		public void setName(String name) {
			this.name = name;
		}


		 public static ArrayList<InstanceModel> loadInstances(String datafile) throws JAXBException {
			  File file = new File( datafile );
			  JAXBContext jaxbContext = JAXBContext.newInstance( InstancesModel.class );
			  Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			  InstancesModel instances = (InstancesModel)jaxbUnmarshaller.unmarshal( file );
			  //System.out.println( instances );
			  return instances.instanceList; 
		}
}
