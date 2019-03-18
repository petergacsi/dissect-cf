package hu.uszeged.inf.iot.simulator.providers;

import hu.uszeged.inf.iot.simulator.fog.Application;

public class BluemixProvider extends Provider{
	
	
	@Override
	public String toString() {
		return  "[BLUEMIX=" + BLUEMIX +" "+this.getFrequency()+"]";
	}


	double BLUEMIX;
	
	public BluemixProvider(Application app) {
		super();
		this.app=app;
	}
	
	
	public void tick(long fires) {		
		if(this.bmList.size()!=0){
			double tmp= (double) this.app.sumOfProcessedData / (double)1048576; // 1 MB
			double cost=0.0;
 			for(Bluemix bm : this.bmList){
				if (tmp <= bm.mbto && tmp >= bm.mbfrom) {
					cost = bm.cost;
				}
			}
 			
			this.BLUEMIX=tmp*cost;
		}
		if(this.shouldStop) {
			unsubscribe();
		}
	}


	@Override
	public void startProvider() {
		subscribe(Integer.MAX_VALUE);
	}
}
