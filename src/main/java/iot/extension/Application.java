package iot.extension;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;
import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import iotprovider.Provider;

/**
 * This class start virtual machines and run ComputeTasks on it depending on the generated data by the stations.
 * Ez az osztaly dolgozza fel a Station-ok altal generalt adatokat ComputeTask-okban.
 * Elinditja a Station-oket, illetve kezeli a virtualis gepek inditasat es leallitasat is.
 */
public class Application extends Timed {

	/**
	 * This class collects the important data about a working virtual machine (number of tasks, working time,etc.).
	 * Osztalyba foglalja az adott virtualis gepet, a VM altal elvegzett osszes feladat 
	 * es az adott VM allapotat (dolgozik-e eppen feladaton,vagy sem).
	 */
	public class VmCollector {
		
		/**
		 * toString method is useful for debuging and loging.
		 */
		 @Override
		public String toString() {
			return "VmCollector [vm=" + vm + ", isworking=" + isworking + ", tasknumber=" + tasknumber + ", worked="
					+ worked + ", pm=" + pm + ", lastWorked=" + lastWorked + ", workingTime=" + workingTime + "]";
		}
		
		/**
		 * This virtual machine runs the ComputeTask.
		 * Ez a virtualis gep fogja futtatni a ComputeTask-okat.
		 */
		VirtualMachine vm;
		
		/**
		 * Helper variable to check free virtual machines and working virtual machines.
		 * Segedvaltozo, amellyel eldontoheto, hogy eppen szabad-e a virtualis gep.
		 */
		boolean isworking;
		
		/**
		 * This variable store the number of finished tasks.
		 * Az elvegzett feladatok szamat tarolja.
		 */
		int tasknumber;
		
		/**
		 * True, if the VM run at least 1 ComputeTask.
		 * Igaz, ha legalabb egy feladatot elvegzett a virtualis gep.
		 */
		boolean worked;
		
		/**
		 * The phisycal machine which serve the virtual machine.
		 * A fizikai gep, amely kiszolgalja a virtualis gepet.
		 */
		PhysicalMachine pm;
		
		/**
		 * It store the last time when the vm run a task.
		 * Az utolso idot tartolja, amikor a virtualis gep inditott feladat.
		 */
		long lastWorked;
		
		/**
		 * Full time when the virtual machines worked.
		 * A teljes ido, amit egy virtualis gep munkaval toltott.
		 */
		public long workingTime;
		 
		/**
		 * Getter method which tell us that the virtual machine have already started a task.
		 * Getter metodus, ami megmondja, hogy csinalt-e mar feladatot a virtualis gep.
		 */
		public boolean isWorked(){
				return worked;
		}
		
		
		public long letehozva;
		/**
		 * Constructor create default VmCollector with default data.
		 * @param vm virtual machine which allow to start ComputeTask- virtual gep, ami indithat ComputeTask-ot
		 */
		VmCollector(VirtualMachine vm) {
			this.vm = vm;
			this.isworking = false;
			this.tasknumber = 0;
			this.worked = false;
			this.workingTime=0;
			this.lastWorked=Timed.getFireCount();
			this.letehozva=Timed.getFireCount();
		}
	}
	//TODO: here we go
	public static ArrayList<Application> getApp() {
		return app;
	}

	private static ArrayList<Application> app = new ArrayList<Application>();
	private static long finishedTime;
	private static int starterVar = 0; 
	private  int i = 0;
	public ArrayList<VmCollector> vmlist;
	private int print;
	private boolean delay;
	private  long allgenerateddatasize = 0;
	private  long localfilesize = 0;
	private  long temp;
	public  TreeMap<Long, Integer> tmap = new TreeMap<Long, Integer>();
	private static int feladatszam = 0;
	private long tasksize;
	public Cloud cloud;
	public ArrayList<Station> stations;
	private String name;
	private Provider provider; //TODO: app = user, egyeni arazas
	
	
	public static long getFinishedTime() {
		return finishedTime;
	}

	public static void setFinishedTime(long finishedTime) {
		Application.finishedTime = finishedTime;
	}

	public String getName() {
		return name;
	}

	/**
	 * Privat konstruktor, hogy csak osztalyon belulrol lehessen hivni
	 */
	public Application(final long freq,long tasksize, boolean delay, int print,Cloud cloud,ArrayList<Station> stations,String name,Provider p) {
		subscribe(freq);
		this.print = print;
		this.vmlist = new ArrayList<VmCollector>();
		this.delay = delay;
		this.tasksize=tasksize;
		this.cloud = cloud;
		this.stations = stations;
		this.name = name;
		this.provider=p;
	}

	/**
	 * megkeresi az elso szabad virtualis gepet, amelyik epp nem dolgozik
	 */
	private VmCollector VmSearch() {
		VmCollector vmc = null;
		for (int i = 0; i < this.vmlist.size(); i++) {
			if ((this.vmlist.get(i).isworking == false
					&& this.vmlist.get(i).vm.getState().equals(VirtualMachine.State.RUNNING))) {
				vmc = this.vmlist.get(i);
				return vmc;
			}
		}
		return vmc;
	}

	/**
	 * A metodus elinditja az osszes Station mukodeset
	 */
	private void startStation() {
		if(Application.starterVar==0){
			for(Provider p : Provider.getProviderList()){
				p.startProvider();
			}
			Provider.lateStart = Timed.getFireCount();
			System.out.println("Scenario started at: " + Timed.getFireCount());
			Application.starterVar++;
		}
		if (this.i == 0) {
			this.i++;
			for (final Station s : this.stations) {
				Random randomGenerator = new Random();
				int randomInt = randomGenerator.nextInt(21);
				if (delay) {
					new DeferredEvent((long) randomInt * 60 * 1000) {

						@Override
						protected void eventAction() {
							s.startMeter(s.getSd().getFreq());
						}
					};
				} else {
					s.startMeter(s.getSd().getFreq());
				}
			}
		}
	}

	/**
	 * A metodus megkeresi es ujrainditja az elso SHUTDOWN allapotban levo virtualis gepet.
	 */
	private boolean turnonVM(){
		for (int i = 0; i < this.vmlist.size(); i++) {
			if ((this.vmlist.get(i).vm.getState().equals(VirtualMachine.State.SHUTDOWN) /*|| TODO: Destroyed allapot jelentese?!
					Application.vmlist.get(i).vm.getState().equals(VirtualMachine.State.DESTROYED)*/) 
					/*&& this.vmlist.get(i).pm!=null && this.cloud.getArc().compareTo(this.vmlist.get(i).pm.freeCapacities)<=0 */
					){				
				try {
					if(this.vmlist.get(i).pm.freeCapacities.getRequiredCPUs()==0.0){
						//return false;
						//TODO: megvizsgalni,hogy ez egyaltalan elofordulhat? nem hozunk letre random vm-eket szoval elmeletileg ez az if torolheto, at kell gondolni.
						System.err.println("Application - turnonVM method");
						System.exit(0);
					}
					this.vmlist.get(i).vm.switchOn(
							this.vmlist.get(i).pm.allocateResources( this.cloud.getArc(), false, PhysicalMachine.defaultAllocLen),
							 this.cloud.getIaas().repositories.get(0));
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
				return true;
			}
		}
		return false;
	}
	/**
	 * A metodus - ha nincs olyan  VM amit ujra lehet inditani - letrehoz egyet.
	 */
	private void generateAndAddVM() {
		try {
			if(this.turnonVM()==false){
				//TODO: ide kene 1 feltetel h ne kezdjen el random vm-eket fölöslegesen létrehozni
				for (int i = 0; i < this.vmlist.size(); i++) {
					System.err.println(this.vmlist.get(i).vm);
				}
				boolean vanpm = false;
				for(PhysicalMachine pm : this.cloud.getIaas().machines){
					//System.err.println(pm.availableCapacities);
					
					if(pm.availableCapacities.getRequiredCPUs()>=this.cloud.getArc().getRequiredCPUs()){
						vanpm=true;
					}
				}
				if(vanpm){ // csak akkor kerjunk uj vm-et ha van fizikai eroforrasunkra.
					this.vmlist.add(new VmCollector(
							 this.cloud.getIaas().requestVM( this.cloud.getVa(),  this.cloud.getArc(),  this.cloud.getIaas().repositories.get(0), 1)[0]));	
				}	
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * A metodus megvizsgalja, hogy van-e olyan Station, amelyik meg uzemel.
	 */
	private boolean checkStationState() { // TODO probably wrong, but lets see
		boolean i = true;
		for (Station s : this.stations) {
			if (s.isSubscribed()) {
				return false;
			}
		}
		return i;
	}

	/**
	 *  Megkeresi az osszes futo, de eppen nem dolgozo VM-et es 1 kivetelevel leallitja mind.
	 */
	private void turnoffVM(){
		int reqshutdown = 0;
		for (VmCollector vmcl : this.vmlist) {
			if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING) && vmcl.isworking == false) {
				reqshutdown++;
			}
		}
		int stopped_vm=0;
		for (VmCollector vmcl : this.vmlist) {
			if (reqshutdown > 1) {
				if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING) && vmcl.isworking == false) {
					reqshutdown--;
					try {
						vmcl.lastWorked=Timed.getFireCount();
						vmcl.vm.switchoff(false);
						stopped_vm++;
					} catch (StateChangeException e) {
						e.printStackTrace();
					}
				}
			}
		}
		//System.out.println("leallitott VM: "+stopped_vm + " ido: "+Timed.getFireCount());
	}	
	
	
	private void countVmRunningTime(){
		for(VmCollector vmc : this.vmlist ){
			if(vmc.vm.getState().equals(VirtualMachine.State.RUNNING)){
				vmc.workingTime+=(Timed.getFireCount()-vmc.lastWorked);
				vmc.lastWorked=Timed.getFireCount();
			}
		}
	}
	
	@Override
	/**
	 * Ez a metodus hivodik meg az applikacio frekvenciaja szerint. Feladata a virtualis gepeknek valo feladatosztas,
	 * es a virtualis gepek kezelese
	 */
	public void tick(long fires) {
		if (this.vmlist.isEmpty()) {
			this.generateAndAddVM(); //
		}
		if (this.VmSearch() != null) {
			this.startStation();
			
		}
		 // ha erkezett be a kozponti repoba feldolgozatlan adat
		this.localfilesize = (Station.getStationvalue()[this.stations.get(0).getCloudnumber()] - this.allgenerateddatasize); 
		if (this.localfilesize > 0) { 
			long processed = 0;
			boolean havevm = true;
			while (this.localfilesize != processed && havevm) { // akkor addig inditsunk feladatokat a VM-en, amig fel nem lett dolgozva az osszes
				if (this.localfilesize - processed > this.tasksize) {
					this.temp = this.tasksize; // maximalis feldolgozott meret
				} else {
					this.temp = (this.localfilesize - processed);
				}
				
				final double noi = this.temp == this.tasksize ? 2400 : (double) (2400 * this.temp / this.tasksize);
				/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
				// System.out.println(Application.temp+" : "+processed+ " :
				// "+Application.localfilesize+" : "+fires);
				final VmCollector vml = this.VmSearch();
				if (vml == null) {
					this.generateAndAddVM();
					havevm=false;
				} else {
					try {
						processed += this.temp;
						final String printtart = vml.vm + " started at " + Timed.getFireCount();
						vml.isworking = true;
						Application.feladatszam++;
						vml.pm=vml.vm.getResourceAllocation().getHost();
						
						vml.vm.newComputeTask(noi, ResourceConsumption.unlimitedProcessing,
								new ConsumptionEventAdapter() {
									long i = Timed.getFireCount();
									long ii = temp;
									double iii = noi;

									@Override
									public void conComplete() {
										vml.isworking = false;
										vml.worked = true;
										vml.tasknumber++;
										Application.feladatszam--;
										if (print == 1) {
											System.out.println(name+" "+printtart + " finished at " + Timed.getFireCount()
													+ " with " + ii + " bytes,lasted " + (Timed.getFireCount() - i)
													+ " ,noi: " + iii);
										}

									}
								});
						this.allgenerateddatasize += this.temp; // kilepesi
						// feltetelhez

					} catch (NetworkException e) {
						e.printStackTrace();
					}
				}
			}
			//System.out.println("osszes: "+Station.allstationsize +" feldolgozott: " +Application.allgenerateddatasize+ " ido: "+Timed.getFireCount());
		}
		this.countVmRunningTime();	

		/* ------------------------------------ */
		int task = 0;
		for (VmCollector vmcl : this.vmlist) {
			if (vmcl.tasknumber >= 0  /*&& vmcl.isworking*/
					&& vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)) {
				task++;
			}
		}
		this.tmap.put(Timed.getFireCount(), task);
		this.turnoffVM();
		// kilepesi feltetel az app szamara
		if (Application.feladatszam == 0 && checkStationState()
				
				&& this.allgenerateddatasize != 0) {
			unsubscribe();
			System.out.println("~~~~~~~~~~~~");
			Application.finishedTime=Timed.getFireCount();
			
			for (VmCollector vmcl : this.vmlist) {
				try {
					if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)) {
						vmcl.vm.switchoff(true);
					}
				} catch (StateChangeException e) {
					e.printStackTrace();
				}
			}
		}
	}
}