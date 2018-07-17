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

	
	public static class Stationdata {

		long starttime;
		long stoptime;
		private long filesize;
		private int sensornumber;
		private long freq;
		private String name;
		private String torepo;
		private double ratio;

		
		public Stationdata(long st, long stt, long filesize, int sn, long freq, String name, String torepo,
				double ratio) {
			this.starttime = st;
			this.stoptime = stt;
			this.filesize = filesize;
			this.sensornumber = sn;
			this.freq = freq;
			this.name = name;
			this.torepo = torepo;
			this.ratio = ratio;
		}

	}

	
	private Stationdata sd;
	private Repository repo;
	private Repository torepo;
	private long reposize;
	private long time;
	private HashMap<String, Integer> lmap;
	private int lat;
	private int i;
	private VirtualMachine vm;
	private PhysicalMachine pm;
	private boolean isWorking;
	private boolean randommetering;
	public Cloud cloud;
	int cloudnumber;
	private int messagecount;
	public Application app;
	private static ArrayList<Station> stations = new ArrayList<Station>();
	private static long[] stationvalue;
	public long generatedfilesize;
	public static long allstationsize = 0;
	

	public static ArrayList<Station> getStations() {
		return stations;
	}

	
	public static long[] getStationvalue() {
		return stationvalue;
	}

	public static void setStationvalue(long[] stationvalue) {
		Station.stationvalue = stationvalue;
	}

	
	public int getMessagecount() {
		return messagecount;
	}

	
	public int getCloudnumber() {
		return cloudnumber;
	}

	
	public void setCloudnumber(int cloudnumber) {
		this.cloudnumber = cloudnumber;
	}

	/**
	 * Getter method for the name of station. Getter metodus a station nevehez.
	 */
	public String getName() {
		return this.sd.name;
	}

	/**
	 * Setter method tfor an IaaS Service which is the same network with this
	 * station. Setter metodus a felhohoz, amivel a station kozos halozatban
	 * van.
	 */
	void setCloud(Cloud cloud) {
		this.cloud = cloud;
	}

	/**
	 * Getter method for the local repository of this station. Getter metodus a
	 * a station lokalis repository-jahoz.
	 */
	public Repository getRepo() {
		return repo;
	}

	/**
	 * Setter method for the local repository of this station. Setter metodus a
	 * a station lokalis repository-jahoz.
	 */
	public void setRepo(Repository repo) {
		this.repo = repo;
	}

	/**
	 * Setter method is useful for the pricing and checking methods. Setter
	 * metodus hasznos a koltsegszamitasnal es az ellenorzo metodusoknal.
	 */
	public void setMessagecount(int messagecount) {
		this.messagecount = messagecount;
	}

	 
	String strategy;
	/**
	 * Constructor creates the local repository based on the parameters and
	 * organize the local and the cloud repository to the same network. A
	 * konstruktor letrehozza a lokalis repository-t majd kozos halozatba
	 * szervezi a felho cep repository-javal.
	 * 
	 * @param maxinbw
	 *            in-bandwidth of the local repository - bejovo savszelesseg a
	 *            lokalis repository-nak
	 * @param maxoutbw
	 *            out-bandwidth of the local repository - kimeno savszelesseg a
	 *            lokalis repository-nak
	 * @param diskbw
	 *            disk-bandwidth of the local repository - disk savszelesseg a
	 *            lokalis repository-nak
	 * @param reposize
	 *            the size of the local repository - a lokalis repository merete
	 * @param sd
	 *            the main data of the station - a station fobb tulajdonsagai
	 * @param randommetering
	 *            if true the Metering() method will be delayed - ha true, akkor
	 *            a Metering() hivas keslelteve lesz
	 * @throws NetworkException 
	 */
	public Station(long maxinbw, long maxoutbw, long diskbw, long reposize, final Stationdata sd,
			boolean randommetering,String strategy) throws NetworkException {
		this.strategy=strategy;
		this.vm = null;
		this.i = 0;
		this.sd = sd;
		this.messagecount = 0;
		isWorking = sd.stoptime == -1 ? false : true;
		lmap = new HashMap<String, Integer>();
		lat = 11;
		lmap.put(sd.name, lat);
		lmap.put(sd.torepo, lat);
		this.reposize = reposize;
		repo = new Repository(this.reposize, sd.name, maxinbw, maxoutbw, diskbw, lmap, defaultStorageTransitions, defaultNetworkTransitions);
		this.randommetering = randommetering;
		this.repo.setState(NetworkNode.State.RUNNING);
		this.founded=false;
	}

	/**
	 * This event will delete the StorageObject from the local repository when
	 * it arrived. Ha sikeresen megerkezett a celrepository-ba a StorageObject,
	 * akkor a lokalis tarolobol torli azt.
	 */
	private class StorObjEvent implements ConsumptionEvent {

		/**
		 * It contains the ID of the event. Az esemeny azonositoja.
		 */
		private String so;

		/**
		 * Constructor give the local variable to the value of the parameter. A
		 * konstruktor parameterben kapja az esemeny azonositojat.
		 * 
		 * @param soid
		 *            the ID of the event
		 */
		private StorObjEvent(String soid) {
			this.so = soid;
		}

		/*
		 * Currently unused, can be useful later.
		 * 
		 * private long Size() { String[] splited = so.split("\\s+"); return
		 * Integer.parseInt(splited[1]); }
		 */

		/**
		 * It will be called when the StorageObject arrived, then it will be
		 * removed from the local repository. Ha a StorageObject megerkezett,
		 * akkor a lokalis repository-bol torlesre kerul.
		 */
		@Override
		public void conComplete() {
			repo.deregisterObject(this.so);
			cloud.iaas.repositories.get(0).deregisterObject(this.so);

		}

		/**
		 * It will be called when the transfer unsuccessful. Ha az adatkuldes
		 * hibas, ez hivodik meg.
		 */
		@Override
		public void conCancelled(ResourceConsumption problematic) {
			System.err.println("Deleting StorageObject from local repository is unsuccessful!");
		}
	}

	/**
	 * This method send all data from the local repository to the cloud
	 * repository. Elkuldi a lokalis taroloban talalhato StorageObject-eket a
	 * celrepository-ba.
	 * 
	 * @param r
	 *            a cel repository
	 */
	private void startCommunicate(Repository r) throws NetworkException {
		//System.out.println(repo + " "+Timed.getFireCount() + " "+r.getCurrState() +" "+ repo.getCurrState());
		if(r.getCurrState().equals(Repository.State.RUNNING)){
			
			for (StorageObject so : repo.contents()) {
				StorObjEvent soe = new StorObjEvent(so.id);
				repo.requestContentDelivery(so.id, r, soe);
			}
		}
		
	}

	/**
	 * This method will start the station ( data generating and sending) on the
	 * given frequence. A metodus elinditja a station mukodeset
	 * (adatgeneralas,adatkuldes) a kapott frekvencian.
	 * 
	 * @param interval
	 *            time between 2 datagenerating-datasending - az ismetlodesi
	 *            frekvencia 2 adatkuldes-adatgeneralas kozott
	 */
	public void startMeter(final long interval) {
		if (isWorking) {
			subscribe(interval);
			this.time = Timed.getFireCount();
			this.pm = this.findPm(sd.torepo);
			this.torepo = this.findRepo(sd.torepo);
		}
	}

	/**
	 * It stops the station. Used locally because the working time should depend
	 * on the station lifetime/stoptime. Leallitja a Station mukodeset, hivasa
	 * akkor fog bekovetkezni, ha a szimulalt ido meghaladja a Station
	 * lifetime-jat es mar minden StorageObject el lett kuldve.
	 */
	private void stopMeter() {
		isWorking = false;
		unsubscribe();
		this.torepo.registerObject(new StorageObject(this.sd.name, generatedfilesize, false));
	}

	/**
	 * It look for the target repository in the Iaas cloud. Megkeresi a celrepot
	 * az IaaS felhoben
	 * 
	 * @param torepo
	 *            name of the repository - a celrepo azonositoja
	 */
	private Repository findRepo(String torepo) {
		Repository r = null;
		for (Repository tmp : this.cloud.iaas.repositories) {
			if (tmp.getName().equals(torepo)) {
				r = tmp;
			} else {
				for (PhysicalMachine pm : this.cloud.iaas.machines) {
					if (pm.localDisk.getName().equals(torepo)) {
						r = pm.localDisk;
					}
				}
			}
		}
		return r;
	}

	/**
	 * If we send directly to a repository of phisical machine,then this method
	 * find that PM. Abban az esetben, ha a celrepo nem kozponti tarolo, hanem
	 * fizikai gep lemeze, akkor a metodus megkeresi az adott fizikai gepet
	 * 
	 * @param torepo
	 *            torepo name of the repository - a celrepo azonositoja
	 */
	private PhysicalMachine findPm(String torepo) {
		PhysicalMachine p = null;
		for (PhysicalMachine pm : this.cloud.iaas.machines) {
			if (pm.localDisk.getName().equals(torepo)) {
				p = pm;
			}
		}
		return p;
	}

	/**
	 * The tick() method will be called on the frequence of station. In the
	 * method was called the data generating, data sending, and it stops the
	 * station when every tasks are done. A tick() metodus folyamatosan hivodik
	 * meg a beallitott frekvencianak megfeleloen. Ebben a metodusban tortenik
	 * az adatgeneralas es a fajlkuldes, vegezetul pedig a Station leiratkozasa
	 * is ebben a metodusban tortenik meg, ha nincs mar tobb elvegzendo feladata
	 * az adott objektumnak.
	 */
	@Override
	public void tick(long fires) {
		// a meres a megadott ideig tart csak - metering takes the given time
		if (Timed.getFireCount() < (sd.stoptime + this.time) && Timed.getFireCount() >= (sd.starttime + this.time)
				&& Timed.getFireCount() <= (sd.stoptime + this.time)) {

			for (int i = 0; i < sd.sensornumber; i++) {
				if (this.randommetering == true) {

					Random randomGenerator = new Random();
					int randomInt = randomGenerator.nextInt(60) + 1;
					new Metering(this, i, sd.filesize, 1000 * randomInt,this.cloudnumber);

				} else {

					new Metering(this, i, sd.filesize, 1,this.cloudnumber);
				}

			}
		}
		// a station mukodese addig amig az osszes SO el nem lett kuldve -
		// stations work while there are data unsent
		if (this.repo.getFreeStorageCapacity() == reposize && Timed.getFireCount() > (sd.stoptime + this.time)) {
			this.stopMeter();
		}

		// kozponti tarolo a cel repo - target is a cloud
		if (this.cloud.iaas.repositories.contains(this.torepo)) {
			// megkeresi a celrepo-t es elkuldeni annak - looking for the
			// repository then sending the data
			try {
				if (this.torepo != null) {
					if ((this.repo.getMaxStorageCapacity() - this.repo.getFreeStorageCapacity()) >= sd.ratio
							* sd.filesize || isSubscribed() == false) {
						this.startCommunicate(this.torepo);
					}
				} else {
					System.out.println("Nincs kapcsolat a repo-k kozott!");
				}
			} catch (NetworkException e) {
				e.printStackTrace();
			}
		}
		// shared nothing cloud
		else {
		/*	if (this.pm.getState().equals(State.RUNNING) && this.i == 0) {
				i++;
				try {
					if (!this.pm.isHostingVMs()) {
						this.vm = this.pm.requestVM(this.cloud.getVa(), this.cloud.getArc(),
								this.cloud.iaas.repositories.get(0), 1)[0];
					} else {
						this.vm = this.pm.listVMs().iterator().next();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// megkeresi a celrepo-t es elkuldeni annak - looking for the
			// repository then sending the data
			try {
				if ((this.repo.getMaxStorageCapacity() - this.repo.getFreeStorageCapacity()) >= sd.ratio * sd.filesize
						|| isSubscribed() == false) {
					if (this.vm != null) {
						if (vm.getState().equals(VirtualMachine.State.RUNNING)) {
							this.startCommunicate(vm.getResourceAllocation().getHost().localDisk);
						}
					}
				}
			} catch (NetworkException e) {
				e.printStackTrace();
			}*/
		}
	}

	/**
	 * ToString method is useful for debuging and loging. toString metodus
	 * hasznos a debugolashoz, logolashoz.
	 */
	@Override
	public String toString() {
		return "Station [sd=" + sd + ", repo=" + repo + ", torepo=" + torepo + ", reposize=" + reposize + ", time="
				+ time + ", lmap=" + lmap + ", lat=" + lat + ", i=" + i + ", vm=" + vm + ", pm=" + pm + ", isWorking="
				+ isWorking + ", randommetering=" + randommetering + ", cloud=" + cloud + ", cloudnumber=" + cloudnumber
				+ ", messagecount=" + messagecount + ", generatedfilesize=" + generatedfilesize + "]";
	}

	
	public static void loadDevice(String stationfile) throws Exception {
		for(DeviceModel dm : DeviceModel.loadDeviceXML(stationfile)) {
			Stationdata sd = new Stationdata(dm.starttime,dm.stoptime,dm.filesize,dm.sensor,dm.freq,dm.name,dm.repository,dm.ratio);
			new Station(dm.maxinbw,dm.maxoutbw,dm.diskbw,dm.reposize,sd,false,dm.strategy);
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
	}
}