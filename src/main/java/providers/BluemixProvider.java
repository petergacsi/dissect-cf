package providers;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import iot.extension.Station;

public class BluemixProvider extends Provider{
		
	@Override
	public String toString() {
		return "BluemixProvider [getUserCost()=" + getUserCost() + " Tier: "+this.tierName+"]";
	}

	public BluemixProvider(String datafile,long lifetime,String name) throws ParserConfigurationException, SAXException, IOException {
		super();
		this.lifetime=lifetime;
		this.readProviderXml(datafile, this, name);	
	}
	
	@Override
	public void tick(long fires) {
		Tier t = searchCategory();
		this.tierName=t.getTierName();
		if(t!=null){
			this.setUserCost(t.getPrice()*(Station.allstationsize/1048576));
		}
	}

	@Override
	protected Tier searchCategory() {
		if(this.getTierList().size()==1){
			return this.getTierList().get(0);
		}
		for(Tier t : this.getTierList()){
			if(t.getMbFrom()<=(Station.allstationsize/1048576) && t.getMbTo()>=(Station.allstationsize/1048576)){
				return t;
			}
		}
		return null;
	}

}
