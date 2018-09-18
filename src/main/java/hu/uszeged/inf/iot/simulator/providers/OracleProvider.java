package hu.uszeged.inf.iot.simulator.providers;

import hu.uszeged.inf.iot.simulator.entities.Application;
import hu.uszeged.inf.iot.simulator.entities.Station;
import hu.uszeged.inf.xml.model.ProvidersModel;

public class OracleProvider extends Provider {
	double ORACLE;
	
	@Override
	public String toString() {
		return "[ORACLE=" + ORACLE + " "+	this.getFrequency()+"]";
	}


	public OracleProvider(Application app,String providerfile) {
		this.app=app;
		try {
			ProvidersModel.loadProviderXML(providerfile,this);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		subscribe(this.getHighestStopTime(this.oracleFreq));
	}

	
	public void tick(long fires) {
		if(this.app.isSubscribed()==false) {
			unsubscribe();
		}

		if(this.amMessagesPerMonthPerDevice>0){
				for(Station s : this.app.stations){
					long month = s.sd.stoptime/(this.getFrequency());
					if(month==0){
						month=1;
						this.ORACLE=this.ORACLE+this.devicepricePerMonth*s.sd.sensornumber*month;
					}else if(s.sd.stoptime%(this.getFrequency())!=0){
						this.ORACLE=this.ORACLE+this.devicepricePerMonth*s.sd.sensornumber*(month+1);
					}else{
						this.ORACLE=this.ORACLE+this.devicepricePerMonth*s.sd.sensornumber*month;
					}
					// additional cost
					long device = s.messageCount/s.sd.sensornumber;// 1 device hany uzenetet generalt
					s.messageCount=0; 
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
			
		}
	}
}
