package providers;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import iot.extension.Station;
import providers.Provider.Tier;

public class AmazonProvider extends Provider{

	public AmazonProvider(String datafile,long lifetime,String name) throws ParserConfigurationException, SAXException, IOException {
		super();
		this.lifetime=lifetime;
		this.readProviderXml(datafile, this,name);	
	}
	
	@Override
	protected Tier searchCategory() {
		if(this.getTierList().size()==1){
			return this.getTierList().get(0);
		}
		return null;
	}

	@Override
	public void tick(long fires) {
		Tier t = this.searchCategory();
		double bof = (double) Station.allstationsize / t.getBlockofdata();
		this.setUserCost(t.getPrice()/t.getMessagecount()*bof);
	}
	
	@Override
	public String toString() {
		return "AmazonProvider [getUserCost()=" + getUserCost() +"]";
	}

}
