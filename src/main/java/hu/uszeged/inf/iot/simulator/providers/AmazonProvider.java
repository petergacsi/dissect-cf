package hu.uszeged.inf.iot.simulator.providers;

import hu.uszeged.inf.iot.simulator.entities.Application;
import hu.uszeged.inf.xml.model.ProvidersModel;

public class AmazonProvider extends Provider{
	double AMAZON;
	
	@Override
	public String toString() {
		return "[AMAZON=" + AMAZON +" "+this.getFrequency()+"]";
	}


	public AmazonProvider(Application app) {
		super();
		this.app=app;
	}
	
	
	public void tick(long fires) {		
		if(this.blockPrice>0 && this.blockSize>0){
			this.AMAZON= (((double)this.app.sumOfProcessedData / this.blockSize) + 1) * this.blockPrice / this.messageCount;
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
