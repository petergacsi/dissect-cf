package hu.uszeged.inf.iot.simulator.providers;

import hu.uszeged.inf.iot.simulator.entities.Application;
import hu.uszeged.inf.iot.simulator.entities.Device;
import hu.uszeged.inf.iot.simulator.entities.Station;
import hu.uszeged.inf.xml.model.ProvidersModel;

public class OracleProvider extends Provider {
	double ORACLE;
	
	@Override
	public String toString() {
		return "[ORACLE=" + ORACLE + " "+	this.getFrequency()+"]";
	}


	public OracleProvider(Application app) {
		super();
		this.app=app;
	}
	
	public void tick(long fires) {

		if(this.amMessagesPerMonthPerDevice>0){
				for(Device s : this.app.stations){
					long month = s.getStopTime()/(this.getFrequency());
					if(month==0){
						month=1;
						this.ORACLE=this.ORACLE+this.devicepricePerMonth*((Station) s).getSensorNum()*month;
					}else if(s.getStopTime()%(this.getFrequency())!=0){
						this.ORACLE=this.ORACLE+this.devicepricePerMonth*((Station) s).getSensorNum()*(month+1);
					}else{
						this.ORACLE=this.ORACLE+this.devicepricePerMonth*((Station) s).getSensorNum()*month;
					}
					// additional cost
					long device = s.getMessageCount()/((Station) s).getSensorNum();// 1 device hany uzenetet generalt
					s.setMessageCount(0); 
					if(device>this.messagesPerMonthPerDevice){
						device-=this.messagesPerMonthPerDevice;
						long whole=device/this.amMessagesPerMonthPerDevice;
						if(whole==0){
							this.ORACLE+=this.amDevicepricePerMonth;
						}else if((device%this.amMessagesPerMonthPerDevice)!=0){
							this.ORACLE+=this.amDevicepricePerMonth*(whole+1);
						}else{
							this.ORACLE+=this.amDevicepricePerMonth*(whole);
						}
					} 
				}
			if(this.shouldStop) {
				unsubscribe();
			}
			
		}
	}


	@Override
	public void startProvider() {
		subscribe(oracleFreq);
		this.shouldStop=false;
	}
}
