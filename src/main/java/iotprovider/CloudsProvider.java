package iotprovider;

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
import iot.extension.Application.VmCollector;

public class CloudsProvider extends Provider {

	private long usedMessage = 0;

	public CloudsProvider(long simulatedTime) {
		super(simulatedTime);

	}

	@Override
	protected void IotCostCounter(int filesize) {
			
		//this.setUserIotCost(0.0);
		//amazon
		if(this.getBofPrice()>0 && this.getBlockOfData()>0){
			if (filesize <= this.getBlockOfData()) {
				this.setUserIotCost(Station.allstationsize / filesize * this.getBofPrice() * this.getExchangeRate()
						/ this.getBofMessagecount());
			} else {
				this.setUserIotCost(((Station.allstationsize / this.getBlockOfData()) + 1) * this.getBofPrice()
						* this.getExchangeRate() / this.getBofMessagecount());
			}
		}
		//bluemix
		if(this.bmList.size()!=0){
			double tmp= (double) Station.allstationsize / (double)1048576;
			for(Bluemix bm : this.bmList){
				if (tmp <= bm.mbto && tmp >= bm.mbfrom) {
					this.setPricePerMB(bm.price);
				}
			}
			this.setUserIotCost(tmp*this.getPricePerMB());
		}
		//oracle
		if(this.getAmMessagesPerMonthPerDevice()>0){
			for(Application a : Application.getApp()){
				for(Station s : a.stations){
					long month = s.getSd().getLifetime()/(this.getFreq());
					if(month==0){
						month=1;
						this.setUserIotCost(this.getUserIotCost()+this.getDevicepricePerMonth()*s.getSd().getSensornumber()*month);
					}else if(s.getSd().getLifetime()%(this.getFreq())!=0){
						this.setUserIotCost(this.getUserIotCost()+this.getDevicepricePerMonth()*s.getSd().getSensornumber()*(month+1));
					}else{
						this.setUserIotCost(this.getUserIotCost()+this.getDevicepricePerMonth()*s.getSd().getSensornumber()*month);
					}
					/* additional cost*/
					long device = s.getMessagecount()/s.getSd().getSensornumber();// 1 device hany uzenetet generalt
					s.setMessagecount(0); 
					if(device>this.getMessagesPerMonthPerDevice()){
						device-=this.getMessagesPerMonthPerDevice();
						long whole=device/this.getAmMessagesPerMonthPerDevice();
						if(whole==0){
							this.setUserIotCost(this.getUserIotCost()+this.getAmDevicepricePerMonth());
						}else if((device%this.getAmMessagesPerMonthPerDevice())!=0){
							this.setUserIotCost(this.getUserIotCost()+this.getAmDevicepricePerMonth()*(whole+1));
						}else{
							this.setUserIotCost(this.getUserIotCost()+this.getAmDevicepricePerMonth()*(whole));
						}
					} 
				}
			}
		}
		//azure
		if(this.getPricePerMB()>=0 && this.getMessagesPerDay()>0 && filesize<=(this.getMessagesizePerKB()*1024)){
			long totalMassages=Station.allstationsize  / filesize;
			long msg = totalMassages - usedMessage;
			usedMessage= msg;
			if(msg<=this.getMessagesPerDay()){
				long month = Timed.getFireCount()/((this.getFreq()*31));
				if(month==0){
					month=1;
					this.setUserIotCost(this.getPricePerMonth()*month);
				}else if(Timed.getFireCount()%(this.getFreq())!=0){
					this.setUserIotCost(this.getPricePerMonth()*(month+1));
				}else{
					this.setUserIotCost(this.getPricePerMonth()*month);
				}
			}else{
				System.err.println("You can't use this tier of Azure!");
				System.exit(1);
			}
		}
		
	}

	
	
	@Override
	protected void CloudCostCounter() {
		double cost1=0,cost2,temp=0;
		int j=0;
		for(Application a : Application.getApp()){
			for(VmCollector vmc : a.vmlist){
				if(vmc.isWorked()){
					cost1 += vmc.workingTime/60/1000/60*this.getHourPrice();
					temp += vmc.workingTime;
				}
				
			}
		}
		
		for(Application a : Application.getApp()){
			for(VmCollector vmcl : a.vmlist){
				if(vmcl.isWorked()){
					j++;
				}
			}
		}
		cost2 = j*this.getInstancePrice();
		this.setUserCloudCost((cost1+cost2));
	}
}
