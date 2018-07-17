package hu.uszeged.inf.iot.simulator.entities;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.xml.bind.JAXBException;
import hu.mta.sztaki.lpds.cloud.simulator.io.*;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;
import hu.uszeged.xml.model.ApplicationModel;
import hu.uszeged.xml.model.DeviceModel;
import hu.mta.sztaki.lpds.cloud.simulator.*;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;

/**
 * This class simulates one of the IoT world's entity which is the smart device.
 * Behavior of the class depends on time (as recurring event). Az osztaly
 * szimulalja egy IoT okos eszkoz mukodeset. A szimulacio soran idotol fuggo,
 * visszatero esemenykent mukodik.
 */
public class Station extends Device{

	/**
	 * This class helps to handle the attributon of station because for
	 * transparency and instantiation. Kulon osztaly a Station fobb adatainak a
	 * konnyebb attekinthetoseg es peldanyositas erdekeben.
	 */
	public static class Stationdata {

		/**
		 *
		 * The final time when the station send data. Az utolso ido, mikor a
		 * station meg adatot fog kuldeni.
		 */
		private long lifetime;

		/**
		 * The time when the station starts generating and sending data. Az az
		 * ido, mikor a station elkezdi az adatgeneralast es az adatkuldest.
		 */
		long starttime;

		/**
		 * The time when the station stops generating and sending data. Az az
		 * ido, mikor a station befejezi az adatgeneralast es az adatkuldest.
		 */
		 long stoptime;

		/**
		 * Size of the generated data. A generalt adatmerete.
		 */
		private long filesize;

		/**
		 * Number of the station's sensors. A station szenzorjainak a szama.
		 */
		private int sensornumber;

		/**
		 * The frequncy which tells the time between generating-sending data. A
		 * frekvencia, amely megmondja mennyi ido teljen el 2 adatgeneralas es
		 * adatkuldes kozott.
		 */
		private long freq;

		/**
		 * Name ID of the station. Station azonosito.
		 */
		private String name;

		/**
		 * Name ID of the repository which will receive the data A repository
		 * ID, amelyik fogadja az adatot.
		 */
		private String torepo;

		/**
		 * It tells how much unit of data will be locally store before sending.
		 * Arany, amely megmondja, hogy hany egysegnyi adat legyen lokalisan
		 * eltarolva kuldes elott.
		 */
		private int ratio;

		/**
		 * Getter method for data sending-generating frequency of the station.
		 * Getter metodus a station frekvenciajahoz.
		 */
		public long getFreq() {
			return freq;
		}

		/**
		 * Getter method for the final time when data sending can happen. Getter
		 * metodus a vegso idohoz amikor tortenhet meg adatkuldes.
		 */
		public long getLifetime() {
			return lifetime;
		}

		/**
		 * Getter method for the number of station's sensors. Getter metodus a
		 * station szenzorainak szamahoz.
		 */
		public int getSensornumber() {
			return sensornumber;
		}

		
		
		/**
		 * Constructor creates useful and necessary data for work of a station.
		 * 
		 * @param lt
		 *            final time to sending data - utolso ido adatkuldeshez
		 * @param st
		 *            time when the data sending and data generating starts - az
		 *            az ido, mikor elkezdodik az adatgeneralas es adatkuldes
		 * @param stt
		 *            time when the data sending and data generating stops - az
		 *            az ido, mikor befejezodik az adatgeneralas es adatkuldes
		 * @param filesize2
		 *            size of the generated data - a generalt adat merete
		 * @param sn
		 *            count of the sensors which generate the data - szenzorok
		 *            szama,amelyek adatot generalnak
		 * @param freq
		 *            time frequency between generate-send data - frekvencia 2
		 *            adatgeneralas-kuldes kozott
		 * @param name
		 *            ID of the station - station azonosito
		 * @param torepo
		 *            ID of the repository (its name) - a cel repository neve
		 * @param ratio2
		 *            how much unit of data will be locally store before sending
		 *            - lokalis adattarolasi aranyt mondja meg az adatkuldes
		 *            elott
		 */
		public Stationdata(long st, long stt, long filesize2, int sn, long freq, String name, String torepo,
				double ratio2) {
			this.starttime = st;
			this.stoptime = stt;
			this.filesize = filesize2;
			this.sensornumber = sn;
			this.freq = freq;
			this.name = name;
			this.torepo = torepo;
			this.ratio = ratio2;
		}

		/**
		 * toString can be useful for debuging or loging. toString metodus
		 * debugolashoz,logolashoz.
		 */
		@Override
		public String toString() {
			return "Stationdata [lifetime=" + lifetime + ", starttime=" + starttime + ", stoptime=" + stoptime
					+ ", filesize=" + filesize + ", sensornumber=" + sensornumber + ", freq=" + freq + ", name=" + name
					+ ", torepo=" + torepo + ", ratio=" + ratio + "]";
		}
	}

	
	public boolean founded;
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
	/**
	 * It contains the all important data of the station. A station osszes
	 * fontos adatat tartalmazza.
	 */
	private Stationdata sd;

	/**
	 * Local repository where the data are stored. Lokalis repository az
	 * adattarolashoz.
	 */
	private Repository repo;

	/**
	 * Reference for the repository which will receive the data. Referencia a
	 * cel repository-ra, ami fogadja az adatot.
	 */
	private Repository torepo;

	/**
	 * Size of the local repository. A lokalis repository merete.
	 */
	private long reposize;

	/**
	 * It contains the time when the station start to work. Tartalmazza a
	 * szimulalt idot,amikor a station elkezd mukodni.
	 */
	private long time;

	/**
	 * Necessary to organize the station's repository and the cloud repository
	 * in the same network. Szukseges a lokalis es a cel repository kozos
	 * halozatba szervezeshez.
	 */
	private HashMap<String, Integer> lmap;

	/**
	 * ID of the the common network. A kozos halozat azonositoja.
	 */
	private int lat;

	/**
	 * Helper variable for the shared nothing cloud. Segedvaltozo a shared
	 * nothing felhohoz.
	 */
	private int i;

	/**
	 * Reference for a virtual machine which is used when we send data directly
	 * to repository of a physical machine. Referencia a virtualis gepre ha
	 * kozvetlenul kuldunk adotot egy fizikai gep repository-nak
	 */
	private VirtualMachine vm;

	/**
	 * Reference for a phisical machine which is used when we send data directly
	 * to repository of a physical machine. Referencia a fizikai gepre ha
	 * kozvetlenul kuldunk adotot egy fizikai gep repository-nak
	 */
	private PhysicalMachine pm;

	/**
	 * True if the station is working,otherwise false. Erteke igaz,ha mukodik a
	 * station, egyebkent hamis
	 */
	private boolean isWorking;

	/**
	 * If true the Metering() method will be randomly delayed. Ha igaz,akkor a
	 * Metering() metodus random kesleltetve lesz.
	 */
	private boolean randommetering;

	/**
	 * Reference for an IaaS Service which is the same network with this
	 * station. Referencia arra a felhore, amellyel a station kozos halozatban
	 * van.
	 */
	public Cloud cloud;

	/**
	 * It's an ID which is important when we use more clouds. A tobb felhos
	 * szimulacioknal ez alapjan kerulhet azonositasra a celfelho.
	 */
	int cloudnumber;

	/**
	 * Important when we check if all data has arrived. Fontos,amikor
	 * ellenorizzuk, hogy az osszes adat el lett-e kuldve.
	 */
	private int messagecount;

	/**
	 * This ArrayList can contains all stations which we use during the
	 * simulation. A szimulacio soran ebben az ArrayList-ben tudjuk eltarolni az
	 * osszes station-unket.
	 */
	private static ArrayList<Station> stations = new ArrayList<Station>();

	/**
	 * It's an Array which is important when we use more clouds to check the
	 * sent data. A tobb felhos szimulacioknal ez alapjan kerulhet ellenorzesre
	 * az adatmennyisegek.
	 */
	private static long[] stationvalue;

	/**
	 * It cointains the all size of data which was generated by this station. Az
	 * osszes generalt adat meretet tarolja, amit ez a station generalt.
	 */
	public long generatedfilesize;

	/**
	 * It cointains the all size of data which was generated by stations. Az
	 * osszes generalt adat meretet tarolja, amit az osszes station generalt.
	 */
	public static long allstationsize = 0;

	/**
	 * This getter method give the main data of the station. A station fobb
	 * adatait tartalmazza.
	 */
	public Stationdata getSd() {
		return sd;
	}

	/**
	 * Static getter method to reach all stations from everywhere. Statikus
	 * getter metodus, hogy az osszes station elerheto legyen.
	 */
	public static ArrayList<Station> getStations() {
		return stations;
	}

	/**
	 * Getter method for the Array which contains ID of the cloud. Getter a
	 * felhoazonositokat tartalmazo tombhoz.
	 */
	public static long[] getStationvalue() {
		return stationvalue;
	}

	/**
	 * Setter method for the Array which contains ID of the cloud. Setter a
	 * felhoazonositokat tartalmazo tombhoz.
	 */
	public static void setStationvalue(long[] stationvalue) {
		Station.stationvalue = stationvalue;
	}

	/**
	 * Getter method for the number of messages which was generated by the
	 * station. Getter a station altal aloallitott uzenetek szamahoz.
	 */
	public int getMessagecount() {
		return messagecount;
	}

	/**
	 * Getter method for the ID of cloud which belongs to this station. Getter
	 * metodus a felho azonositohoz, amelyhez ez a station tartozik.
	 */
	public int getCloudnumber() {
		return cloudnumber;
	}

	/**
	 * Setter method for the ID of cloud which belongs to this station. Setter
	 * metodus a felho azonositohoz, amelyhez ez a station tartozik.
	 */
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
		isWorking = sd.lifetime == -1 ? false : true;
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
			cloud.getIaas().repositories.get(0).deregisterObject(this.so);

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
		for (Repository tmp : this.cloud.getIaas().repositories) {
			if (tmp.getName().equals(torepo)) {
				r = tmp;
			} else {
				for (PhysicalMachine pm : this.cloud.getIaas().machines) {
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
		for (PhysicalMachine pm : this.cloud.getIaas().machines) {
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
		if (Timed.getFireCount() < (sd.lifetime + this.time) && Timed.getFireCount() >= (sd.starttime + this.time)
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
		if (this.repo.getFreeStorageCapacity() == reposize && Timed.getFireCount() > (sd.lifetime + this.time)) {
			this.stopMeter();
		}

		// kozponti tarolo a cel repo - target is a cloud
		if (this.cloud.getIaas().repositories.contains(this.torepo)) {
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
			if (this.pm.getState().equals(State.RUNNING) && this.i == 0) {
				i++;
				try {
					if (!this.pm.isHostingVMs()) {
						this.vm = this.pm.requestVM(this.cloud.getVa(), this.cloud.getArc(),
								this.cloud.getIaas().repositories.get(0), 1)[0];
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
			}
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
			Station s = new Station(dm.maxinbw,dm.maxoutbw,dm.diskbw,dm.reposize,sd,false,dm.strategy);
		}
	}

	@Override
	public void installionProcess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdownProcess() {
		// TODO Auto-generated method stub
		
	}
}