package providers;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class BluemixProvider extends Provider{

	public BluemixProvider(String datafile) throws ParserConfigurationException, SAXException, IOException {
		super();
		this.readProviderXml(datafile, this);	
	}
	
	@Override
	public void tick(long fires) {
		double localcost = 0;
		for(Provider.Tier t : this.getTierList()){
			//this.setUserCost(userCost);
		}
		
		
	}

}
