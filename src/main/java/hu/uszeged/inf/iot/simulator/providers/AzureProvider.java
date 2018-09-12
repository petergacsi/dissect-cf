package hu.uszeged.inf.iot.simulator.providers;

import hu.uszeged.inf.iot.simulator.entities.Application;

public class AzureProvider extends Provider{
	double AZURE;
	
	@Override
	public String toString() {
		return "[AZURE=" + AZURE + "]";
	}


	public AzureProvider(Application app) {
		this.app=app;
	}
	
	
	public void tick(long fires) {
		if(this.app.isSubscribed()==false) {
			unsubscribe();
		}
		/*
		if(this.messagesPerDay>0 && this.avarageFileSize()<=(this.messagesizePerKB*1024)){
			long totalMassages=this.app.sumOfData()  / this.avarageFileSize();
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
				this.AZURE=-1;
			}
}*/
	}
}
