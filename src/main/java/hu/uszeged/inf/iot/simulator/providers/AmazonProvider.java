package hu.uszeged.inf.iot.simulator.providers;

import hu.uszeged.inf.iot.simulator.entities.Application;
import hu.uszeged.inf.xml.model.ProvidersModel;

public class AmazonProvider extends Provider{
	double AMAZON;
	
	@Override
	public String toString() {
		return "[AMAZON=" + AMAZON +" "+this.getFrequency()+"]";
	}


	public AmazonProvider(Application app,String providerfile) {
		this.app=app;
		try {
			ProvidersModel.loadProviderXML(providerfile,this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		subscribe(this.getHighestStopTime(Long.MAX_VALUE));
	}
	
	
	public void tick(long fires) {		
		if(this.blockPrice>0 && this.blockSize>0){
			this.AMAZON= ((this.app.sumOfGeneratedData() / this.blockSize) + 1) * this.blockPrice / this.messageCount;
		}
		
		if(this.app.isSubscribed()==false) {
			unsubscribe();
		}
	}
}
