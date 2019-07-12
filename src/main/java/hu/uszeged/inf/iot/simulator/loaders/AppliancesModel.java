package hu.uszeged.inf.iot.simulator.loaders;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "appliances")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class AppliancesModel {

	ArrayList<ApplianceModel> applianceList;
	
	public ArrayList<ApplianceModel> getAppliances(){
		return applianceList;
	}
	
	@XmlElement( name = "appliance")
	public void setAppliances( ArrayList<ApplianceModel> appliances ) {
		this.applianceList = appliances;
	}
	
	public void add( ApplianceModel appliance ) {
		if (this.applianceList == null) {
			
			this.applianceList = new ArrayList<ApplianceModel>();
		}
		this.applianceList.add( appliance );
	}
	
	
	
	 @Override
	    public String toString(){
	        StringBuffer str = new StringBuffer();
	        for( ApplianceModel appliance : this.applianceList )
	        {	
	            str.append( appliance.toString() );
	            str.append("\n");
	        }
	        return str.toString();
	    }

	
}
