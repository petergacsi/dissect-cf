package providers;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import iot.extension.Application;
import iot.extension.Scenario;
import iot.extension.Station;

public class CloudsProvider extends Provider {

	public CloudsProvider() {
		super();
	}

	@Override
	protected void costCounter(int filesize) {
		//amazon
		if(this.getBofPrice()>0 && this.getBlockOfData()>0){
			if (filesize <= this.getBlockOfData()) {
				this.setUserCost(Station.allstationsize / filesize * this.getBofPrice() * this.getExchangeRate()
						/ this.getBofMessagecount());
			} else {
				this.setUserCost(((Station.allstationsize / this.getBlockOfData()) + 1) * this.getBofPrice()
						* this.getExchangeRate() / this.getBofMessagecount());
			}
		}
		//bluemix
		if(this.getPricePerMB()>0){
			double tmp = (double) Station.allstationsize / 1048576;
			this.setUserCost(tmp*this.getPricePerMB());
		}
		//oracle
		if(this.getAmMessagesPerMonthPerDevice()>0){
			for(Application a : Scenario.getApp()){
				for(Station s : a.stations){
					long month = s.sd.getLifetime()/(this.getFreq());
					if(month==0){
						month=1;
						this.setUserCost(this.getUserCost()+this.getDevicepricePerMonth()*s.sd.getSensornumber()*month);
					}else if(s.sd.getLifetime()%(this.getFreq())!=0){
						month=month+1;
						this.setUserCost(this.getUserCost()+this.getDevicepricePerMonth()*s.sd.getSensornumber()*(month+1));
					}else{
						this.setUserCost(this.getUserCost()+this.getDevicepricePerMonth()*s.sd.getSensornumber()*month);
					}
					/* additional cost*/
					long device = s.getMessagecount()/s.sd.getSensornumber();// 1 device hany uzenetet generalt
					s.setMessagecount(0); 
					if(device>this.getMessagesPerMonthPerDevice()){
						device-=this.getMessagesPerMonthPerDevice();
						long whole=device/this.getAmMessagesPerMonthPerDevice();
						if(whole==0){
							this.setUserCost(this.getUserCost()+this.getAmDevicepricePerMonth());
						}else if((device%this.getAmMessagesPerMonthPerDevice())!=0){
							this.setUserCost(this.getUserCost()+this.getAmDevicepricePerMonth()*(whole+1));
						}else{
							this.setUserCost(this.getUserCost()+this.getAmDevicepricePerMonth()*(whole));
						}
					} 
				}
			}
		}
		//azure
		
		
		
		/******************************************************************/
		System.out.println("Usercost is: " + this.getUserCost());
		boolean tmp = false;
		for(Application a : Scenario.getApp()){
			if(a.isSubscribed()){
				tmp = true;
			}
		}
		if(tmp==false){
			this.stopProvider();
		}
	}

	@Override
	public void tick(long fires) {
		this.costCounter(this.filesize);
	}
}
