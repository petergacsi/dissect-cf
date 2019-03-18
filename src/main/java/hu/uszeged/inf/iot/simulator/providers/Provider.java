package hu.uszeged.inf.iot.simulator.providers;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.fog.Application;
import hu.uszeged.inf.xml.model.ProvidersModel;


public abstract class Provider extends Timed{
	public static String PROVIDERFILE;
	public ArrayList<Bluemix> bmList;
	Application app;
	public boolean shouldStop;
	
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
		Provider.PROVIDERFILE=providerfile;
		for(Application app: Application.applications) {
			app.providers.add(new BluemixProvider(app));
			app.providers.add(new AmazonProvider(app));
			app.providers.add(new OracleProvider(app));
			app.providers.add(new AzureProvider(app));
		}
	}

	Provider(){
		bmList = new ArrayList<Bluemix>();
		
		try {
			ProvidersModel.loadProviderXML(Provider.PROVIDERFILE,this);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.startProvider();
	}

	public abstract void startProvider();
	
	@Override
	public void tick(long fires) {
		// TODO Auto-generated method stub
		
	}

}
