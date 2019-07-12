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
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement( name = "instance" )
@XmlAccessorType(XmlAccessType.PROPERTY) 
public class InstanceModel {
	
		@Override
	public String toString() {
		return "Instances [ram=" + ram + ", cpuCores=" + cpuCores + ", pricePerTick=" + pricePerTick
				+ ", coreProcessingPower=" + coreProcessingPower + ", startupProcess=" + startupProcess + ", networkLoad="
				+ networkLoad + ", reqDisk=" + reqDisk + ", name=" + name + "]";
	}

		public long ram;
		public int cpuCores;
		public double pricePerTick;
		public double coreProcessingPower;
		public long startupProcess;
		public long networkLoad;
		public long reqDisk;
		public String name;

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
		public void setCorProcessingPower(double coreProcessingPower) {
			this.coreProcessingPower = coreProcessingPower;
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


		 public static ArrayList<InstanceModel> loadInstanceXML(String datafile) throws JAXBException {
			  File file = new File( datafile );
			  JAXBContext jaxbContext = JAXBContext.newInstance( InstancesModel.class );
			  Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			  InstancesModel instances = (InstancesModel)jaxbUnmarshaller.unmarshal( file );
			  //System.out.println( instances );
			  return instances.instanceList; 
		}
}
