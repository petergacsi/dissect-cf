package hu.uszeged.inf.iot.simulator.entities;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;
import hu.uszeged.inf.iot.simulator.pliant.FuzzyIndicators;
import hu.uszeged.inf.iot.simulator.pliant.Sigmoid;
import hu.uszeged.inf.xml.model.DeviceModel;

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
	
	public static class Stationdata {

		long starttime;
		public long stoptime;
		public long filesize;
		public int sensornumber;
		private long freq;
		String name;
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
	
	public Stationdata sd;
	Repository localRepository;
	private Repository cloudRepository;
	private HashMap<String, Integer> lmap;
	private static int latency=11;
	public Application app;
	public long generatedfilesize;
	private String strategy;
	public static long allstationsize = 0;
	public int messageCount;
	//long realStartTime; 
	
	public Station(long maxinbw, long maxoutbw, long diskbw, long reposize, final Stationdata sd,String strategy) throws NetworkException {
		this.strategy=strategy;
		this.sd = sd;
		this.lmap = new HashMap<String, Integer>();
		this.localRepository = new Repository(reposize, sd.name, maxinbw, maxoutbw, diskbw, lmap, defaultStorageTransitions, defaultNetworkTransitions);
		this.localRepository.setState(NetworkNode.State.RUNNING);	
		installionProcess(this);
		this.startMeter();
		this.messageCount=0;
	}

	private void startCommunicate() throws NetworkException {
		if(this.cloudRepository.getCurrState().equals(Repository.State.RUNNING)){
			for (StorageObject so : localRepository.contents()) {
				StorObjEvent soe = new StorObjEvent(so.id);
				localRepository.requestContentDelivery(so.id, this.cloudRepository, soe);
			}
		}
	}

	public void startMeter() {
		Random rand = new Random();
		int  delay = rand.nextInt(20)*1000*60;
		
		if (this.isSubscribed()==false) {
			new DeferredEvent(this.sd.starttime+delay) {
				
				@Override
				protected void eventAction() {
					subscribe(sd.freq);
					cloudRepository = app.cloud.iaas.repositories.get(0);
					//realStartTime = Timed.getFireCount();
				}
			};
			
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
		if (Timed.getFireCount() < (sd.stoptime ) && Timed.getFireCount() >= (sd.starttime )) {
			for (int i = 0; i < sd.sensornumber; i++) {
					new Sensor(this, i, sd.filesize, 1);
			}
		}
		// a station mukodese addig amig az osszes SO el nem lett kuldve -
		// stations work while there are data unsent
		if (this.localRepository.getFreeStorageCapacity() == this.localRepository.getMaxStorageCapacity() && Timed.getFireCount() > sd.stoptime ) {
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
			for(int i=0;i<dm.number;i++){
				new Station(dm.maxinbw,dm.maxoutbw,dm.diskbw,dm.reposize,sd,dm.strategy);
			}
			
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
						lmap.put(sd.name, Station.latency);
						lmap.put(app.cloud.iaas.repositories.get(0).getName(), Station.latency);
					}
				};
			}else if(this.strategy.equals("random")){

				Random randomGenerator = new Random();
				int rnd = randomGenerator.nextInt(Application.applications.size());
				Application.addStation(this, Application.applications.get(rnd));
				this.lmap.put(sd.name, Station.latency);
				this.lmap.put(this.app.cloud.iaas.repositories.get(0).getName(), Station.latency);

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
				this.lmap.put(sd.name, Station.latency);
				this.lmap.put(this.app.cloud.iaas.repositories.get(0).getName(), Station.latency);
			}
			else if(this.strategy.equals("fuzzy")){
				new DeferredEvent(this.sd.starttime) {
					
					@Override
					protected void eventAction() {
						int rsIdx = fuzzyDecision(s);
						Application.addStation(s, Application.applications.get(rsIdx));
						lmap.put(sd.name, Station.latency);
						lmap.put(app.cloud.iaas.repositories.get(0).getName(), Station.latency);
					}
				};
				
				// 
				// Application.applications.get(i).instance.pricePerTick // ar
				// Application.applications.get(i).vmlist.size()() hany van most
				// Station.sd. (start stop time) -> ekkor kuld jeleket
				// Station.sd. freq -> adatgyujtes gyakoris�ga
				// Cloud.
				
				// Application.applications.get(i).stations
				// Application.applications.get(i).tasksize
				// Application.applications.get(i).vmsize
			
				//cloud info
				// Cloud.clouds.get(1)
				// Cloud.clouds.get(1).iaas.machines fizikai gepek
				// Application.applications.get(0).cloud - application cloud kapcsolat
				
				// Application.applications.get(0).allprocessed mennyit hajtott vegre
				//gyenge vm tovabb
				
				//vm gep tipusa
				// Application.applications.get(0).instance.arc.getRequiredCPUs()
				//
				//fel van-e iratkozva, ha felvan akkor fut
				// s.isSubscribed() 
				
			}
		
		
	}

	private int fuzzyDecision(Station s) {
		//System.out.println("test");
		Sigmoid sig = new Sigmoid(new Double(-4), new Double(0.00000004));
		Vector<Double> price = new Vector<Double>();
		for(int i=0;i<Application.applications.size();++i){
			
			price.add(sig.getat(Application.applications.get(i).instance.pricePerTick));
		}
		//System.out.println(price);
		
		Vector<Double> numberofvm = new Vector<Double>();
		sig = new Sigmoid(new Double(-0.25),new Double(5));
		for(int i=0;i<Application.applications.size();++i){
			numberofvm.add(sig.getat(new Double(Application.applications.get(i).vmlist.size())));
		}
		//System.out.println(numberofvm);
		
		Vector<Double> numberofstation = new Vector<Double>();
		sig = new Sigmoid(new Double(-0.125),new Double(350));
		for(int i=0;i<Application.applications.size();++i){					
			numberofstation.add(sig.getat(new Double(Application.applications.get(i).stations.size())));
		}
		//System.out.println(numberofstation);
		
		Vector<Double> preferVM = new Vector<Double>();
		sig = new Sigmoid(new Double(4),new Double(8));
		for(int i=0;i<Application.applications.size();++i){					
			preferVM.add(sig.getat(new Double(Application.applications.get(i).instance.arc.getRequiredCPUs())));
		}
		//System.out.println(preferVM);
		
		Vector<Double> preferVMMem = new Vector<Double>();
		sig = new Sigmoid(new Double(4),new Double(2147483648.0));
		for(int i=0;i<Application.applications.size();++i){					
			preferVMMem.add(sig.getat(new Double(Application.applications.get(i).instance.arc.getRequiredMemory())));
		}
		//System.out.println(preferVMMem);
		
		
		
		Vector<Double> score = new Vector<Double>();
		for(int i=0;i<price.size();++i){
			Vector<Double> temp = new Vector<Double>();
			temp.add(price.get(i));
			temp.add(numberofvm.get(i));
			temp.add(numberofstation.get(i));
			temp.add(preferVM.get(i));
			temp.add(preferVMMem.get(i));
			score.add(FuzzyIndicators.getAggregation(temp)*100);
		}
		Vector<Integer> finaldecision = new Vector<Integer>();
		for(int i=0;i<Application.applications.size();++i){
			finaldecision.add(i);	
		}
		for(int i=0;i<score.size();++i){
			for(int j = 0; j< score.get(i); j++) {
				finaldecision.add(i);
			}
		}
		Random rnd = new Random();
		Collections.shuffle(finaldecision);			
		int temp = rnd.nextInt(finaldecision.size());
		return finaldecision.elementAt(temp);		
		
		
	}
	
	@Override
	public void shutdownProcess() {	
		// TODO: this method will handle the 'shutdown for a while' process
	}
}