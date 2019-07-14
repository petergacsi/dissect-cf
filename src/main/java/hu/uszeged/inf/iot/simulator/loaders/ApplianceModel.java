package hu.uszeged.inf.iot.simulator.loaders;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import hu.uszeged.inf.iot.simulator.fog.ComputingAppliance;

@XmlRootElement( name = "appliance")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ApplianceModel {
	
	
	public String name;
	public double xcoord;
	public double ycoord;
	public String parentApp;
	public ArrayList<ApplicationModel> applications;
	public ArrayList<ComputingDevice> neighbourAppliances; 
	public String file;
	
	@Override
	public String toString() {
		return "ApplianceModel [name=" + name + ", xcoord=" + xcoord + ", ycoord=" + ycoord + ", applications="
				+ applications + ", neighbourAppliances=" + neighbourAppliances + "]";
	}
	
	
	@XmlElement(name = "name" )
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name = "xcoord" )
	public void setXcoord(double xcoord) {
		this.xcoord = xcoord;
	}
	
	@XmlElement(name = "ycoord" )
	public void setYcoord(double ycoord) {
		this.ycoord = ycoord;
	}
	
	@XmlElement(name = "parentApp")
	public void setParentApp(String parentApp) {
		this.parentApp = parentApp;
	}
	
	//to read applications from xml
	//-----
	public ArrayList<ApplicationModel> getApplications(){
		return applications;
	}
	
	@XmlElementWrapper( name = "applications" )
	@XmlElement( name = "application")
	public void setApplications(ArrayList<ApplicationModel> applications) {
		this.applications = applications;
	}
	
	public void add( ApplicationModel applicationModel) {
		if ( this.applications == null) {
			this.applications = new ArrayList<ApplicationModel>();
		}
		this.applications.add(applicationModel);
	}
	//-----
	
	
	//to read neighbourappliances from xml
	//-----
	public ArrayList<ComputingDevice> getNeighbourAppliances(){
		return neighbourAppliances;
	}
	
	@XmlElementWrapper( name = "neighbourAppliances")
	@XmlElement( name = "device")
	public void setNeighbourAppliances(ArrayList<ComputingDevice> neighbourAppliances) {
		this.neighbourAppliances = neighbourAppliances;
	}
	
	public void add ( ComputingDevice device) {
		if (this.neighbourAppliances == null) {
			this.neighbourAppliances = new ArrayList<ComputingDevice>();
		}
		this.neighbourAppliances.add(device);
	}
	
	@XmlElement(name = "file")
	public void setFile(String file) {
		this.file = file;
	}
	
	//-----
	
	public static ArrayList<ApplianceModel> loadAppliancesXML(String appliancefile) throws JAXBException {
		File file = new File(appliancefile);
		JAXBContext jaxbContext = JAXBContext.newInstance( AppliancesModel.class );
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		AppliancesModel appliances = (AppliancesModel) jaxbUnmarshaller.unmarshal( file );
		return appliances.applianceList;
	}
	
}
