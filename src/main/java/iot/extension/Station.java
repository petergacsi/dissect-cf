package iot.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import hu.mta.sztaki.lpds.cloud.simulator.io.*;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;

/**
 * Az osztaly szimulalja egy IoT eszkoz mukodeset, ehhez megvalositja az ososzaly tick() metodusat,
 * illetve kozos halozatba kerul a kozponti IaaS felhovel. 
 */
public class Station extends Timed {

	/**
	 * Kulon osztaly a Station fobb adatainak a konnyebb attekinthetoseg es peldanyositas erdekeben
	 */
	public static class Stationdata {
		private long lifetime;
		private long starttime;
		private long stoptime;
		private int filesize;
		private int sensornumber;
		long freq;
		private String name;
		private String torepo;
		private int ratio;

		/**
		 * @param lt lifetime, a szimulacio teljes idotartama
		 * @param st starttime, a station mukodesenek kezdete
		 * @param stt stoptime, a station mukodesenek vege
		 * @param fs filesize, egy meres soran generalt fajlmeret
		 * @param sn sensornumber, az osszes szenzorszam
		 * @param freq freq, az adatgeneralas es az adatkuldes ismetlodesenek frekvenciaja
		 * @param name name, egyedi azonosito
		 * @param torepo torepo, az a Repository, ahova a station a generalt adatokat fogja kuldeni 
		 * @param ratio ratio, erteketol fuggoen az adatok bizonyos ideig vissza lesznek tartva a kuldestol
		 */
		public Stationdata(long lt, long st, long stt, int fs, int sn, long freq, String name, String torepo,
				int ratio) {
			this.lifetime = lt;
			this.starttime = st;
			this.stoptime = stt;
			this.filesize = fs;
			this.sensornumber = sn;
			this.freq = freq;
			this.name = name;
			this.torepo = torepo;
			this.ratio = ratio;
		}

		@Override
		/**
		 * toString metodus, hasznos lehet debugolashoz
		 */
		public String toString() {
			return "name=" + name + ", lifetime=" + lifetime + ", starttime=" + starttime + ", stoptime=" + stoptime
					+ ", filesize=" + filesize + ", sensornumber=" + sensornumber + ", freq=" + freq + ", torepo="
					+ torepo + " ,ratio=" + ratio;
		}
	}

	public Stationdata sd;
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
	public static ArrayList<Station> stations = new ArrayList<Station>();
	public long generatedfilesize;
	public static long allstationsize=0;

	/**
	 * A konstruktor vegzi el a kozos halozatba szervezest az IaaS felhovel
	 * @param maxinbw a station tarolojanak halozati input savszelessege
	 * @param maxoutbw a station tarolojanak halozati output savszelessege
	 * @param diskbw a tarolo lemezenek savszelessege 
	 * @param reposize a tarolo merete
	 * @param sd tovabbi parametereket tarolo Stationdata objektum
	 * @param randommetering true eseten a meresek kesleltetve lesznek random ideig
	 */
	public Station(long maxinbw, long maxoutbw, long diskbw, long reposize, final Stationdata sd,boolean randommetering) {
		this.vm = null;
		this.i = 0;
		this.sd = sd;
		this.pm = this.findPm(sd.torepo);
		isWorking = sd.lifetime == -1 ? false : true;
		lmap = new HashMap<String, Integer>();
		lat = 11;
		lmap.put(sd.name, lat);
		lmap.put(sd.torepo, lat);
		this.reposize = reposize;
		repo = new Repository(this.reposize, sd.name, maxinbw, maxoutbw, diskbw, lmap);
		this.torepo = this.findRepo(sd.torepo);
		this.randommetering=randommetering;
	}

	/**
	 * TODO kommentezes!!!!!!!!
	 * @return
	 */
	public String getName() {
		return this.sd.name;
	}
	
	public Repository getRepo() {
		return repo;
	}

	public void setRepo(Repository repo) {
		this.repo = repo;
	}

	/**
	 * Ha sikeresen megerkezett a celrepo-ba a StorageObject, akkor a lokalis tarolobol torli azt.
	 */
	private class StorObjEvent implements ConsumptionEvent {
		private String so;

		private StorObjEvent(String soid) {
			this.so = soid;
		}
		// nem hasznalt metodus, kesobb meg hasznos lehet
		private long Size() {
			String[] splited = so.split("\\s+");
			return Integer.parseInt(splited[1]);
		}

		@Override
		public void conComplete() {
			repo.deregisterObject(this.so);
		}

		@Override
		public void conCancelled(ResourceConsumption problematic) {
			System.out.println("A StorageObject torlese sikertelen!");
		}
	}

	/**
	 * Elkuldi a lokalis taroloban talalhato StorageObject-eket a celrepoba
	 * @param r a cel repository
	 */
	private void startCommunicate(Repository r) throws NetworkException {
		for (StorageObject so : repo.contents()) {
			StorObjEvent soe = new StorObjEvent(so.id);
			repo.requestContentDelivery(so.id, r, soe);
		}
	}

	/**
	 * A metodus lathatosaga package private, hivasa az Application osztalybol tortenik, ez 
	 * inditja el a Station mukodeset.
	 * @param interval az ismetlodesi frekvencia
	 */
	 void startMeter(final long interval) {
		if (isWorking) {
			subscribe(interval);
			this.time=Timed.getFireCount();
		}
	}

	/**
	 * Leallitja a Station mukodeset, hivasa akkor fog bekovetkezni, ha a szimulalt ido
	 * meghaladja a Station lifetime-jat es mar minden StorageObject el lett kuldve
	 */
	private void stopMeter() {
		isWorking = false;
		unsubscribe();
	}

	/**
	 * Megkeresi a celrepot az IaaS felhoben
	 * @param torepo a celrepo azonositoja
	 */
	private Repository findRepo(String torepo) {
		Repository r = null;
		for (Repository tmp : Cloud.getIaas().repositories) {
			if (tmp.getName().equals(torepo)) {
				r = tmp;
			} else { // TODO: kell ez az else ag?!
				for (PhysicalMachine pm : Cloud.getIaas().machines) {
					if (pm.localDisk.getName().equals(torepo)) {
						r = pm.localDisk;
					}
				}
			}
		}
		return r;
	}

	/**
	 * Abban az esetben, ha a celrepo nem kozponti tarolo, hanem fizikai gep lemeze, akkor 
	 * a metodus megkeresi az adott fizikai gepet
	 * @param torepo
	 * @return
	 */
	private PhysicalMachine findPm(String torepo) {
		PhysicalMachine p = null;
		for (PhysicalMachine pm : Cloud.getIaas().machines) {
			if (pm.localDisk.getName().equals(torepo)) {
				p = pm;
			}
		}
		return p;
	}

	/**
	 * A tick() metodus folyamatosan hivodik meg a beallitott frekvencianak megfeleloen.
	 * Ebben a metodusban tortenik az adatgeneralas es a fajlkuldes, vegezetul pedig a Station leiratkozasa is
	 * ebben a metodusban tortenik meg, ha nincs mar tobb elvegzendo feladata az adott objektumnak.
	 */
	@Override
	public void tick(long fires) {
		// a meres a megadott ideig tart csak
		if (Timed.getFireCount() < (sd.lifetime+this.time) && Timed.getFireCount() >= (sd.starttime+this.time)
				&& Timed.getFireCount() <= (sd.stoptime+this.time)) {
		
			for (int i = 0; i < sd.sensornumber; i++) {
				if(this.randommetering==true){
					
					Random randomGenerator = new Random();
					int randomInt = randomGenerator.nextInt(60)+1;		
					new Metering(sd.name, i, sd.filesize,1000*randomInt);
				}else{
					
					new Metering(sd.name, i, sd.filesize,1);
				}
				

			}
		} 
		// de a station mukodese addig amig az osszes SO el nem lett kuldve
		if (this.repo.getFreeStorageCapacity() == reposize && Timed.getFireCount() > (sd.lifetime+this.time)) {
			this.stopMeter();
		}

		// kozponti tarolo a cel repo
		if (Cloud.getIaas().repositories.contains(this.torepo)) {
			// megkeresi a celrepo-t es elkuldeni annak
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
		// share nothing felho
		else {
			if (this.pm.getState().equals(State.RUNNING) && this.i == 0) {
				i++;
				try {
					if (!this.pm.isHostingVMs()) {
						this.vm = this.pm.requestVM(Cloud.getVa(), Cloud.getArc(),
								Cloud.getIaas().repositories.get(0), 1)[0];
					} else {
						this.vm = this.pm.listVMs().iterator().next();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// megkeresi a celrepo-t es elkuldeni annak
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
	 * toString metodus a Station fontosabb adatainak kiiratashoz,debugolashoz
	 */
	@Override
	public String toString() {
		return "Station [" + sd + ", reposize:" + this.repo.getMaxStorageCapacity() + ",fajlmeret "
				+ this.generatedfilesize + "]";
	}
}
