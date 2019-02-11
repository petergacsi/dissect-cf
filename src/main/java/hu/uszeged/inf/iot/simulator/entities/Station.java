package hu.uszeged.inf.iot.simulator.entities;

import java.util.Random;
import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.uszeged.inf.xml.model.DeviceModel;

public class Station extends Device{
	
	private class StorObjEvent implements ConsumptionEvent {
		private String so;
		long size;
		private StorObjEvent(String soid,Long size) {
			this.so = soid;
			this.size= size;
		}

		@Override
		public void conComplete() {
			//sentData+=this.size;
			dn.localRepository.deregisterObject(this.so);
			// TODO: fix this "cheat"
			app.cloud.iaas.repositories.get(0).deregisterObject(this.so);
		}

		@Override
		public void conCancelled(ResourceConsumption problematic) {
			try {
				throw new Exception("Deleting StorageObject from local repository is unsuccessful!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public int sensorNum;
	private long freq;
	private double ratio;
	private String strategy;
	public static long allstationsize = 0;
	
	
	public int getSensorNum() {
		return sensorNum;
	}

	//long sentData;
	public Station(DeviceNetwork dn, long startTime,long stopTime,long filesize, String strategy,int sensorNum,
			long freq,double ratio)  {
		this.startTime=startTime;
		this.stopTime=stopTime;
		this.filesize=filesize;
		this.strategy=strategy;
		this.dn=dn;
		this.sensorNum=sensorNum;
		this.freq=freq;
		this.ratio=ratio;
				
		installionProcess(this);
		this.startMeter();
		this.setMessageCount(0);
		//this.sentData=0;
	}

	private void startCommunicate() throws NetworkException {
		if(this.cloudRepository.getCurrState().equals(Repository.State.RUNNING)){
			for (StorageObject so : this.dn.localRepository.contents()) {
				StorObjEvent soe = new StorObjEvent(so.id,so.size);
				this.dn.localRepository.requestContentDelivery(so.id, this.cloudRepository, soe);

			}
		}
	}

	public void startMeter() {
		Random rand = new Random();
		int  delay = rand.nextInt(20)*1000*60;
		
		if (this.isSubscribed()==false) {
			new DeferredEvent(this.startTime+delay) {
				
				@Override
				protected void eventAction() {
					subscribe(freq);
					cloudRepository = app.cloud.iaas.repositories.get(0);
					//realStartTime = Timed.getFireCount();
				}
			};
			
		}
	}

	private void stopMeter() {
		unsubscribe();
		// TODO: fix the "cheat"
		this.cloudRepository.registerObject(new StorageObject(this.dn.repoName, generatedFilesize, false));
	}

	@Override
	public void tick(long fires) {
		
		// a meres a megadott ideig tart csak - metering takes the given time
		if (Timed.getFireCount() < (stopTime ) && Timed.getFireCount() >= (startTime)) {
			for (int i = 0; i < sensorNum; i++) {
					new Sensor(this, i, filesize, 1);
			}
		}
		// a station mukodese addig amig az osszes SO el nem lett kuldve -
		// stations work while there are data unsent
		if (this.dn.localRepository.getFreeStorageCapacity() == this.dn.localRepository.getMaxStorageCapacity() && Timed.getFireCount() > stopTime ) {
			this.stopMeter();
		}


			// repository then sending the data
			try {
				if (this.cloudRepository != null) {
					if ((this.dn.localRepository.getMaxStorageCapacity() - this.dn.localRepository.getFreeStorageCapacity()) >= ratio
							* filesize || isSubscribed() == false) {
						this.startCommunicate();
					}
				}
			} catch (NetworkException e) {
				e.printStackTrace();
			}
	}

	public static void loadDevice(String stationfile) throws Exception {
		for(DeviceModel dm : DeviceModel.loadDeviceXML(stationfile)) {
			for(int i=0;i<dm.number;i++){
				DeviceNetwork dn = new DeviceNetwork(dm.maxinbw,dm.maxoutbw,dm.diskbw,dm.reposize,dm.name);
				new Station(dn,dm.starttime,dm.stoptime,dm.filesize,dm.strategy,dm.sensor,dm.freq,dm.ratio);
			}
			
		}
	}

	public  void installionProcess(final Station s) {
		if(this.strategy.equals("load")){		
				new RuntimeStrategy(this);
		}else if(this.strategy.equals("random")){
				new RandomStrategy(this);
		}else if(this.strategy.equals("cost")) {
				new CostStrategy(this);
		}else if(this.strategy.equals("fuzzy")){
				new FuzzyStrategy(this);	
		}
	}

	@Override
	public void shutdownProcess() {	
		// TODO: this method will handle the 'shutdown for a while' process
	}
}