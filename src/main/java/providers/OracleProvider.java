package providers;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import iot.extension.Application;
import iot.extension.Scenario;
import iot.extension.Station;
import providers.Provider.Tier;

public class OracleProvider extends Provider {
	
	public OracleProvider(String datafile,long lifetime,String name) throws ParserConfigurationException, SAXException, IOException {
		super();
		this.lifetime=lifetime;
		this.readProviderXml(datafile, this,name);	
	}

	@Override
	public void tick(long fires) {
		this.setUserCost(0.0);
		double timeProportion = ((double)this.lifetime/(double)(this.getPeriod()*24*60*60*1000));
		double fee=0;
		long allmessage = 0;
		for(Application a : Scenario.getApp()){
			for(Station s : a.stations){
				allmessage += s.getMessagecount();
				int device = s.getMessagecount()/s.sd.getSensornumber();// 1 device hany uzenetet generalt
				int i=0;
				for(Tier t : this.getTierList()){
					if(t.isConstantFee()==false){
						
						double messageProportion = (t.getMessagecount()*timeProportion);

						if(messageProportion<device){
							i++;
							fee = this.getTierList().get(i).getPrice()*timeProportion*s.sd.getSensornumber();
						}
					}
				}
				this.setUserCost(this.getUserCost()+fee);
			}
		}
		double tmp = allmessage/this.getTierList().get(0).getMessagecount()*timeProportion*this.getTierList().get(4).getPrice();
		this.setUserCost(this.getUserCost()+tmp);
	}

	@Override
	protected Tier searchCategory() {
		return null;
	}
	
	@Override
	public String toString() {
		return "OracleProvider [getUserCost()=" + getUserCost() +"]";
	}

}
