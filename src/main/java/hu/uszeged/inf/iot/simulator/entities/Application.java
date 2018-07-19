package hu.uszeged.inf.iot.simulator.entities;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;


import javax.xml.bind.JAXBException;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.uszeged.inf.iot.simulator.providers.Instance;
//import hu.uszeged.inf.iot.simulator.providers.Provider;
import hu.uszeged.xml.model.ApplicationModel;
import hu.uszeged.xml.model.InstanceModel;

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
	//public static ArrayList<Application> getApp() {
	//	return app;
	//}
	boolean stationStarter;
	public static ArrayList<Application> applications = new ArrayList<Application>();
	private static long finishedTime;
	private static int starterVar = 0; 
	private  int i = 0;
	public ArrayList<VmCollector> vmlist;
	private int print;
	private boolean delay;
	private  long allgenerateddatasize = 0;
	static long allprocessed =0;
	private  long localfilesize = 0;
	private  long temp;
	public  TreeMap<Long, Integer> tmap = new TreeMap<Long, Integer>();
	private static int feladatszam = 0;
	private long tasksize;
	public Cloud cloud;
	public ArrayList<Station> stations;
	private String name;
	//private Provider provider; //TODO: app = user, egyeni arazas
	Instance instance;
	
	public static long getFinishedTime() {
		return finishedTime;
	}

	public static void setFinishedTime(long finishedTime) {
		Application.finishedTime = finishedTime;
	}

	public String getName() {
		return name;
	}

	public static void loadApplication(String appfile) throws JAXBException {
		for(ApplicationModel am : ApplicationModel.loadApplicationXML(appfile)) {
			new Application(am.freq,am.tasksize,am.cloud,am.instance,am.name);
		}
	}

	private Application(final long freq,long tasksize,String cloud,String instance,String name) {
		this.vmlist = new ArrayList<VmCollector>();
		this.stations = new ArrayList<Station>();
		this.tasksize=tasksize;
		this.cloud = Cloud.addApplication(this,cloud);
		this.name = name;
		if(cloud!=null) {
			subscribe(freq);
		}
		this.instance= Instance.instances.get(instance);
		Application.applications.add(this);
		this.cloud.iaas.repositories.get(0).registerObject(this.instance.va);
	}

	/**
	 * megkeresi az elso szabad virtualis gepet, amelyik epp nem dolgozik
	 */
	private VmCollector VmSearch() {
		VmCollector vmc = null;
		
		for (int i = 0; i < this.vmlist.size(); i++) {
		//	System.out.println(this.vmlist.get(i).vm.getState()+" "+Timed.getFireCount()+ " + " + this.instance.arc +" + "+ this.name);
			if ((this.vmlist.get(i).isworking == false
					&& this.vmlist.get(i).vm.getState().equals(VirtualMachine.State.RUNNING))) {
				vmc = this.vmlist.get(i);
				return vmc;
			}
		}
		return vmc;
	}

	private void generateAndAddVM() {
		try {
			if(this.turnonVM()==false){
				
				//TODO: ide kene 1 feltetel h ne kezdjen el random vm-eket fölöslegesen létrehozni
				//for (int i = 0; i < this.vmlist.size(); i++) {
				//	System.err.println(this.vmlist.get(i).vm);
				//}
				boolean vanpm = false;
				for(PhysicalMachine pm : this.cloud.iaas.machines){
					//System.err.println(pm.availableCapacities);
					
					if(pm.availableCapacities.getRequiredCPUs()>=this.instance.arc.getRequiredCPUs()){
						vanpm=true;
						
					}
				}
				if(vanpm){ // csak akkor kerjunk uj vm-et ha van fizikai eroforrasunkra.
					//System.out.println(this.cloud.iaas.repositories.get(0).toString());
									
					this.vmlist.add(new VmCollector(
							 this.cloud.iaas.requestVM( this.instance.va,  this.instance.arc,  this.cloud.iaas.repositories.get(0), 1)[0]));	
				}	
				
			}
		} catch (Exception e) {
			e.printStackTrace();
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
							this.vmlist.get(i).pm.allocateResources( this.instance.arc, false, PhysicalMachine.defaultAllocLen),
							 this.cloud.iaas.repositories.get(0));
					
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
	

	/**
	 * A metodus megvizsgalja, hogy van-e olyan Station, amelyik meg uzemel.
	 */
	private boolean checkStationState() { // TODO probably wrong, but lets see
		for (Station s : this.stations) {
			//System.out.println(s + " "+ Timed.getFireCount());
			if (s.isSubscribed()) {
				return false;
			}
		}
		return true;
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
	
	
	private long sumOfData() {
		long temp = 0;
		for(Station s : this.stations) {
			temp+=s.generatedfilesize;
		}
		return temp;
	}
	@Override
	/**
	 * Ez a metodus hivodik meg az applikacio frekvenciaja szerint. Feladata a virtualis gepeknek valo feladatosztas,
	 * es a virtualis gepek kezelese
	 */
	public void tick(long fires) {
		 // ha erkezett be a kozponti repoba feldolgozatlan adat
		this.localfilesize =( this.sumOfData() - this.allgenerateddatasize); 
		//System.out.println(this.sumOfData());
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
										if(print==1)
											System.out.println(name+" "+printtart + " finished at " + Timed.getFireCount()
													+ " with " + ii + " bytes,lasted " + (Timed.getFireCount() - i)
													+ " ,noi: " + iii);
										

									}
								});
						this.allgenerateddatasize += this.temp; // kilepesi
						// feltetelhez
						Application.allprocessed+=this.temp;
					} catch (NetworkException e) {
						e.printStackTrace();
					}
				}
			}
			//System.out.println("osszes: "+Station.allstationsize +" feldolgozott: " +Application.allgenerateddatasize+ " ido: "+Timed.getFireCount());
		}
		//this.countVmRunningTime();	

		/* ------------------------------------ */
		/* int task = 0;
		for (VmCollector vmcl : this.vmlist) {
			if (vmcl.tasknumber >= 0  && vmcl.isworking
					&& vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)) {
				task++;
			}
		}*/
		//this.tmap.put(Timed.getFireCount(), task);
	//	this.turnoffVM();
		
	
		// kilepesi feltetel az app szamara
		if (Application.feladatszam == 0 && checkStationState() 	
				&&   Timed.getFireCount()>getLongestStoptime()) {
					unsubscribe();
					System.out.println("Application "+this.name+" has stopped @"+Timed.getFireCount());
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
	
	long getLongestStoptime(){
		long max = -1;
		for(Station s :  this.stations){
			if(s.sd.stoptime>max){
				max = s.sd.stoptime;
			}
	}
		//System.out.println(max);
	return max;
	}
	public static void addStation(Station s,Application a) {
		a.stations.add(s);
		s.app=a;
	}
}