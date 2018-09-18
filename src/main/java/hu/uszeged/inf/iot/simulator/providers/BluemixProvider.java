package hu.uszeged.inf.iot.simulator.providers;

import hu.uszeged.inf.iot.simulator.entities.Application;
import hu.uszeged.inf.xml.model.ProvidersModel;

public class BluemixProvider extends Provider{
	
	
	@Override
	public String toString() {
		return  "[BLUEMIX=" + BLUEMIX +" "+this.getFrequency()+"]";
	}


	double BLUEMIX;
	
	public BluemixProvider(Application app,String providerfile) {
		this.app=app;
		try {
			ProvidersModel.loadProviderXML(providerfile,this);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		subscribe(this.getHighestStopTime(Long.MAX_VALUE));
	}
	
	
	public void tick(long fires) {		
		if(this.bmList.size()!=0){
			double tmp= (double) this.app.sumOfData() / (double)1048576; // 1 MB
			double cost=0.0;
 			for(Bluemix bm : this.bmList){
				if (tmp <= bm.mbto && tmp >= bm.mbfrom) {
					cost = bm.cost;
				}
			}
 			
			this.BLUEMIX=tmp*cost;
		}

		if(this.app.isSubscribed()==false) {
			unsubscribe();
		}
	}
}
