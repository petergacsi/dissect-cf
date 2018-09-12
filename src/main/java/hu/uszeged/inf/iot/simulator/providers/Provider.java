package hu.uszeged.inf.iot.simulator.providers;

import java.util.ArrayList;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.entities.Application;
import hu.uszeged.inf.xml.model.ProvidersModel;

public class Provider extends Timed{

	
	@Override
	public String toString() {
		return "Provider [BLUEMIX=" + BLUEMIX + ", AMAZON=" + AMAZON + ", AZURE=" + AZURE + ", ORACLE=" + ORACLE + "]";
	}
	public ArrayList<Bluemix> bmList;

	public double BLUEMIX;
	public double AMAZON;
	public double AZURE;
	public double ORACLE;
	
	public long blockSize;
	public long messageCount;
	public double price;

	public double devicepricePerMonth;
	public long messagesPerMonthPerDevice;
	public double amDevicepricePerMonth;
	public long amMessagesPerMonthPerDevice;
	public long oracleFreq;
	
	public long azureFreq;
	public double pricePerMonth;
	public long messagesPerDay;
	public long messagesizePerKB;
	
	private static Provider p=null;
	
	public static class Bluemix{
		double mbto;
		double mbfrom;
		double cost;
		
		@Override
		public String toString() {
			return "Bluemix [mbto=" + mbto + ", mbfrom=" + mbfrom + ", cost=" + cost + "]";
		}

		public Bluemix(double mbto, double mbfrom, double cost) {
			this.mbto=mbto;
			this.mbfrom=mbfrom;
			this.cost=cost;
		}
	}
	
	public static void loadProvider(String providerfile){
		try {
			for(Application app: Application.applications) {
				ProvidersModel.loadProviderXML(providerfile,new Provider(app));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private Provider(Application app){
		bmList = new ArrayList<Bluemix>();
		app.provider=this;
	}
	@Override
	public void tick(long fires) {
		// TODO Auto-generated method stub
		
	}
}
