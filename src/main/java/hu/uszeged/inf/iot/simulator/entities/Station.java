package hu.uszeged.inf.iot.simulator.entities;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import hu.mta.sztaki.lpds.cloud.simulator.io.*;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;
import hu.uszeged.xml.model.DeviceModel;
import hu.mta.sztaki.lpds.cloud.simulator.*;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;

public class Station extends Device{

	private class StorObjEvent implements ConsumptionEvent {
		private String so;
		private StorObjEvent(String soid) {
			this.so = soid;
		}

		@Override
		public void conComplete() {
			localRepository.deregisterObject(this.so);
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
	
	static class Stationdata {

		long starttime;
		long stoptime;
		private long filesize;
		private int sensornumber;
		private long freq;
		 String name;
		private String torepo;
		private double ratio;

		
		public Stationdata(long st, long stt, long filesize, int sn, long freq,String name,
				double ratio) {
			this.starttime = st;
			this.stoptime = stt;
			this.filesize = filesize;
			this.sensornumber = sn;
			this.freq = freq;
			this.name = name;
			this.ratio = ratio;
		}

	}

	// TODO: these burned values need to delete
	public static final double minpower = 20;
	public static final double idlepower = 200;
	public static final double maxpower = 300;
	public static final double diskDivider = 10;
	public static final double netDivider = 20;
	public final static Map<String, PowerState> defaultStorageTransitions;
	public final static Map<String, PowerState> defaultNetworkTransitions;
	
	static {
		try {
			EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = PowerTransitionGenerator
					.generateTransitions(minpower, idlepower, maxpower, diskDivider, netDivider);
			defaultStorageTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.storage);
			defaultNetworkTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.network);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot initialize the default transitions");
		}
	}
	
	 Stationdata sd;
	 Repository localRepository;
	private Repository cloudRepository;
	private HashMap<String, Integer> lmap;
	private int lat;
	public Application app;
	public long generatedfilesize;
	private String strategy;
	public static long allstationsize = 0;
	long realStartTime; 
	
	public Station(long maxinbw, long maxoutbw, long diskbw, long reposize, final Stationdata sd,String strategy) throws NetworkException {
		this.strategy=strategy;
		this.sd = sd;
		this.lmap = new HashMap<String, Integer>();
		this.lat = 11;
		this.lmap.put(sd.name, lat);
		this.lmap.put(sd.torepo, lat);
		this.localRepository = new Repository(reposize, sd.name, maxinbw, maxoutbw, diskbw, lmap, defaultStorageTransitions, defaultNetworkTransitions);
		this.localRepository.setState(NetworkNode.State.RUNNING);	
	}

	

	/**
	 * This method send all data from the local repository to the cloud
	 * repository. Elkuldi a lokalis taroloban talalhato StorageObject-eket a
	 * celrepository-ba.
	 * 
	 * @param r
	 *            a cel repository
	 */
	private void startCommunicate() throws NetworkException {
		if(this.cloudRepository.getCurrState().equals(Repository.State.RUNNING)){
			
			for (StorageObject so : localRepository.contents()) {
				StorObjEvent soe = new StorObjEvent(so.id);
				localRepository.requestContentDelivery(so.id, this.cloudRepository, soe);
			}
		}
		
	}

	public void startMeter() {
		if (this.isSubscribed()==false) {
			installionProcess(this);
			subscribe(this.sd.freq);
			this.cloudRepository = this.app.cloud.iaas.repositories.get(0);
			this.realStartTime = Timed.getFireCount();
		}
	}


	private void stopMeter() {
		unsubscribe();
		// TODO: fix the "cheat"
		this.cloudRepository.registerObject(new StorageObject(this.sd.name, generatedfilesize, false));
	}

	@Override
	public void tick(long fires) {
		// a meres a megadott ideig tart csak - metering takes the given time
		if (Timed.getFireCount() < (sd.stoptime + this.realStartTime) && Timed.getFireCount() >= (sd.starttime + this.realStartTime)
				&& Timed.getFireCount() <= (sd.stoptime + this.realStartTime)) {

			for (int i = 0; i < sd.sensornumber; i++) {
					new Metering(this, i, sd.filesize, 1);
			}
		}
		// a station mukodese addig amig az osszes SO el nem lett kuldve -
		// stations work while there are data unsent
		if (this.localRepository.getFreeStorageCapacity() == this.localRepository.getMaxStorageCapacity() && Timed.getFireCount() > (sd.stoptime + this.realStartTime)) {
			this.stopMeter();
		}


			// repository then sending the data
			try {
				if (this.cloudRepository != null) {
					if ((this.localRepository.getMaxStorageCapacity() - this.localRepository.getFreeStorageCapacity()) >= sd.ratio
							* sd.filesize || isSubscribed() == false) {
						this.startCommunicate();
					}
				}
			} catch (NetworkException e) {
				e.printStackTrace();
			}
	}
	


	
	public static void loadDevice(String stationfile) throws Exception {
		for(DeviceModel dm : DeviceModel.loadDeviceXML(stationfile)) {
			Stationdata sd = new Stationdata(dm.starttime,dm.stoptime,dm.filesize,dm.sensor,dm.freq,dm.name,dm.ratio);
			new Station(dm.maxinbw,dm.maxoutbw,dm.diskbw,dm.reposize,sd,dm.strategy);
		}
	}

	public  void installionProcess(final Station s) {
		 if(this.strategy.equals("load")){				
				new DeferredEvent(this.sd.starttime) {
					
					@Override
					protected void eventAction() {
						double min = Double.MAX_VALUE-1.0;
						int choosen = -1;
						for(int i=0;i< Application.applications.size();i++ ){
							double loadRatio = (Application.applications.get(i).stations.size())/(Application.applications.get(i).cloud.iaas.machines.size());
							if(loadRatio<min){
								min=loadRatio;
								choosen = i;
							}
						}
						Application.addStation(s, Application.applications.get(choosen));	
					}
				};
			}else if(this.strategy.equals("random")){

				Random randomGenerator = new Random();
				int rnd = randomGenerator.nextInt(Application.applications.size());
				Application.addStation(this, Application.applications.get(rnd));

			}else if(this.strategy.equals("cost")) {
				 double min=Integer.MAX_VALUE-1.0;
				 int choosen=-1;
				for(int i=0;i<Application.applications.size();++i){
					if(Application.applications.get(i).instance.pricePerTick<min){
						min = Application.applications.get(i).instance.pricePerTick;
						choosen = i;
					}
				}
				Application.addStation(this, Application.applications.get(choosen));
			}
		
		
	}

	@Override
	public void shutdownProcess() {	
		// TODO: this method will handle the 'shutdown for a while' process
	}
}