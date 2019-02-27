package hu.uszeged.inf.iot.simulator.entities;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.util.SeedSyncer;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.uszeged.inf.xml.model.DeviceModel;

public class Station extends Device{
	
	public int sensorNum;
	private long freq;
	private String strategy;
	//  TODO: this function should be re-think
	//private double ratio;
	
	public int getSensorNum() {
		return sensorNum;
	}

	public Station(DeviceNetwork dn, long startTime,long stopTime,long filesize, String strategy,int sensorNum,
			long freq,double ratio)  {
		long delay = Math.abs(SeedSyncer.centralRnd.nextLong()%20)*60*1000;
		this.startTime=startTime+delay;
		this.stopTime=stopTime+delay;
		this.filesize=filesize*sensorNum;
		this.strategy=strategy;
		this.dn=dn;
		this.sensorNum=sensorNum;
		this.freq=freq;
		this.sumOfGeneratedData=0;				
		installionProcess(this);
		this.startMeter();
		this.setMessageCount(0);
	}

	private void startCommunicate() throws NetworkException {
		for (StorageObject so : this.dn.localRepository.contents()) {
			StorObjEvent soe = new StorObjEvent(so);
			//this.dn.localRepository.requestContentDelivery(so.id, this.cloudRepository, soe);
			NetworkNode.initTransfer(so.size, ResourceConsumption.unlimitedProcessing, this.dn.localRepository, this.cloudRepository, soe);
		}
	}

	public void startMeter() {
		if (this.isSubscribed()==false) {
			new DeferredEvent(this.startTime) {
				
				@Override
				protected void eventAction() {
					subscribe(freq);
					cloudRepository = app.cloud.iaas.repositories.get(0);
				}
			};
		}
	}

	private void stopMeter() {
		unsubscribe();		
	}

	@Override
	public void tick(long fires) {
		if (Timed.getFireCount() < (stopTime ) && Timed.getFireCount() >= (startTime)) {
			new Sensor(this, 1);
		}

		if (this.dn.localRepository.getFreeStorageCapacity() == this.dn.localRepository.getMaxStorageCapacity() && Timed.getFireCount() > stopTime ) {
			this.stopMeter();
		}

			try {
				if (this.cloudRepository.getCurrState().equals(Repository.State.RUNNING)) {
					this.startCommunicate();
				}
			} catch (NetworkException e) {
				e.printStackTrace();
			}
	}

	public static void loadDevice(String stationfile) throws Exception {
		for(DeviceModel dm : DeviceModel.loadDeviceXML(stationfile)) {
			for(int i=0;i<dm.number;i++){
				DeviceNetwork dn = new DeviceNetwork(dm.maxinbw,dm.maxoutbw,dm.diskbw,dm.reposize,dm.name+i,null,null);
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
	
	private class StorObjEvent implements ConsumptionEvent {
		private StorageObject so;
		
		private StorObjEvent(StorageObject so) {
			this.so = so;

		}

		@Override
		public void conComplete() {
			dn.localRepository.deregisterObject(this.so);
			// TODO: fix this "cheat"
			//app.cloud.iaas.repositories.get(0).deregisterObject(this.so);
					app.sumOfArrivedData+=this.so.size;
			
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
}