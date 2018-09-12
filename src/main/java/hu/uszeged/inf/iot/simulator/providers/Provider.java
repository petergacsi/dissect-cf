package hu.uszeged.inf.iot.simulator.providers;

import java.util.ArrayList;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.entities.Application;


public class Provider extends Timed{

	public ArrayList<Bluemix> bmList;
	Application app;

	
	public long blockSize;
	public long messageCount;
	public double blockPrice;

	public double devicepricePerMonth;
	public long messagesPerMonthPerDevice;
	public double amDevicepricePerMonth;
	public long amMessagesPerMonthPerDevice;
	public long oracleFreq;
	
	public long azureFreq;
	public double pricePerMonth;
	public long messagesPerDay;
	public long messagesizePerKB;
		
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
				app.providers.add(new BluemixProvider(app,providerfile));
				app.providers.add(new AmazonProvider(app,providerfile));
				app.providers.add(new OracleProvider(app));
				app.providers.add(new AzureProvider(app));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	Provider(){
		bmList = new ArrayList<Bluemix>();
	}

	@Override
	public void tick(long fires) {
		// TODO Auto-generated method stub
		
	}

}
