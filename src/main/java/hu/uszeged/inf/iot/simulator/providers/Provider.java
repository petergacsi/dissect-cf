package hu.uszeged.inf.iot.simulator.providers;

import java.util.ArrayList;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.xml.model.ProvidersModel;

public class Provider extends Timed{

	@Override
	public String toString() {
		return "Provider [bmList=" + bmList + ", blockSize=" + blockSize + ", messageCount=" + messageCount + ", price="
				+ price + ", devicepricePerMonth=" + devicepricePerMonth + ", messagesPerMonthPerDevice="
				+ messagesPerMonthPerDevice + ", amDevicepricePerMonth=" + amDevicepricePerMonth
				+ ", amMessagesPerMonthPerDevice=" + amMessagesPerMonthPerDevice + ", oracleFreq=" + oracleFreq
				+ ", azureFreq=" + azureFreq + ", pricePerMonth=" + pricePerMonth + ", messagesPerDay=" + messagesPerDay
				+ ", messagesizePerKB=" + messagesizePerKB + "]";
	}
	public ArrayList<Bluemix> bmList;

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
	
	public static Provider getInstance() {
		return Provider.p;
	}
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
			Provider p =new Provider();
			ProvidersModel.loadProviderXML(providerfile,p);
		//	System.out.println(p);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private Provider(){
		if(Provider.p==null) {
			bmList = new ArrayList<Bluemix>();
			Provider.p=this;
		}else {
			System.err.println("You can create only one IoT provider!");
			System.exit(0);
		}
		
	}
	@Override
	public void tick(long fires) {
		// TODO Auto-generated method stub
		
	}
}
