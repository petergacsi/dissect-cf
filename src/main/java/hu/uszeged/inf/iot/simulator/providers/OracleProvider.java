package hu.uszeged.inf.iot.simulator.providers;

import hu.uszeged.inf.iot.simulator.entities.Application;

public class OracleProvider extends Provider {
	double ORACLE;
	
	@Override
	public String toString() {
		return "[ORACLE=" + ORACLE + "]";
	}


	public OracleProvider(Application app) {
		this.app=app;
	}
	
	
	public void tick(long fires) {
		if(this.app.isSubscribed()==false) {
			unsubscribe();
		}
/*
		if(this.amMessagesPerMonthPerDevice>0){
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
					// additional cost
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
}*/
	}
}
