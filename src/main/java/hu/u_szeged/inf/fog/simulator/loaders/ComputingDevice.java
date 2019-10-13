package hu.u_szeged.inf.fog.simulator.loaders;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement( name = "device" )
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ComputingDevice {
	 
	public String deviceName;
	
	@XmlElement( name = "deviceName")
	public void setChildName(String deviceName) {
		this.deviceName = deviceName;
	}

	@Override
	public String toString() {
		return "DeviceName [DeviceName=" + deviceName + "]";
	}
	
	
	
}
