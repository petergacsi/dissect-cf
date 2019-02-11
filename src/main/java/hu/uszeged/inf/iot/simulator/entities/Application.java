package hu.uszeged.inf.iot.simulator.entities;

import java.util.ArrayList;
import java.util.TreeMap;
import javax.xml.bind.JAXBException;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.uszeged.inf.iot.simulator.providers.Instance;
import hu.uszeged.inf.iot.simulator.providers.Provider;
import hu.uszeged.inf.xml.model.ApplicationModel;

public class Application extends Timed {

	public class VmCollector {
		PhysicalMachine pm;
		VirtualMachine vm;
		boolean isworking;
		int tasknumber;
		boolean worked;
		long lastWorked;
		public long workingTime;
		int interationCounter;
		String id;
		double restarted;
		
		public boolean isWorked() {
			return worked;
		}

		public long installed;

		@Override
		public String toString() {
			return "VmCollector [pm=" + pm + ", vm=" + vm + ", isworking=" + isworking + ", tasknumber=" + tasknumber
					+ ", worked=" + worked + ", lastWorked=" + lastWorked + ", workingTime=" + workingTime
					+ ", interationCounter=" + interationCounter + ", id=" + id + ", installed=" + installed + "]";
		}

		VmCollector(VirtualMachine vm) {
			this.vm = vm;
			this.isworking = false;
			this.tasknumber = 0;
			this.worked = false;
			this.workingTime = 0;
			this.lastWorked = Timed.getFireCount();
			this.installed = Timed.getFireCount();
			this.id=Integer.toString(vmlist.size());
			this.restarted=0;
		}
	}

	boolean stationStarter;
	public static ArrayList<Application> applications = new ArrayList<Application>();
	public ArrayList<Provider> providers;
	public long allWorkTime;
	public ArrayList<VmCollector> vmlist;
	public long allgenerateddatasize = 0, stationgenerated = 0;
	static long allprocessed = 0;
	private long localfilesize = 0;
	private long temp;
	public TreeMap<Long, Integer> tmap = new TreeMap<Long, Integer>();
	private static int feladatszam = 0;
	private long tasksize;
	public Cloud cloud;
	public ArrayList<Device> stations;
	public String name;
	Instance instance;

	public static void loadApplication(String appfile) throws JAXBException {
		for (ApplicationModel am : ApplicationModel.loadApplicationXML(appfile)) {
			new Application(am.freq, am.tasksize, am.cloud, am.instance, am.name);
		}
	}

	private Application(final long freq, long tasksize, String cloud, String instance, String name) {
		this.vmlist = new ArrayList<VmCollector>();
		this.stations = new ArrayList<Device>();
		this.tasksize = tasksize;
		this.allWorkTime=0;
		this.cloud = Cloud.addApplication(this, cloud);
		this.name = name;
		if (cloud != null) {
			subscribe(freq);
		}
		this.instance = Instance.instances.get(instance);
		Application.applications.add(this);
		this.cloud.iaas.repositories.get(0).registerObject(this.instance.va);
		this.startBroker();
		providers = new ArrayList<Provider>();
	}

	private void startBroker() {

		try {
			VmCollector vmc = new VmCollector(this.cloud.iaas.requestVM(this.instance.va, this.instance.arc,
					this.cloud.iaas.repositories.get(0), 1)[0]);
			vmc.id="broker";
			this.vmlist.add(vmc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private VmCollector VmSearch() {
		for (int i = 0; i < this.vmlist.size(); i++) {
			if ((this.vmlist.get(i).isworking == false
					&& this.vmlist.get(i).vm.getState().equals(VirtualMachine.State.RUNNING) && !this.vmlist.get(i).id.equals("broker"))) {
				return this.vmlist.get(i);

			}
		}
		return null;
	}
	
	private void generateAndAddVM() {
		try {
			if (this.turnonVM() == false) {
				for (PhysicalMachine pm : this.cloud.iaas.machines) {
					if (pm.isHostableRequest(this.instance.arc)) {
						VirtualMachine vm = pm.requestVM(this.instance.va, this.instance.arc,
								this.cloud.iaas.repositories.get(0), 1)[0];
						if(vm!=null) {
							VmCollector vmc = new VmCollector(vm);
							vmc.pm=pm;
							this.vmlist.add(vmc);
						}
						
						return;
				
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*private boolean turnonVM() {
		for (int i = 0; i < this.vmlist.size(); i++) {
			if (this.vmlist.get(i).vm.getState().equals(VirtualMachine.State.SHUTDOWN) && this.vmlist.get(i).vm.getResourceAllocation()!=null){
				try {
					this.vmlist.get(i).vm.switchOn(this.vmlist.get(i).vm.getResourceAllocation(), this.cloud.iaas.repositories.get(0));			
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	} */


	/*private PhysicalMachine findRA() {
		for(PhysicalMachine pm : this.cloud.iaas.machines) {
			if(pm.isHostableRequest(this.instance.arc)) {
				return pm;
			}
		}
		return null;
	}*/
	
	private boolean turnonVM() {
		for (int i = 0; i < this.vmlist.size(); i++) {
			if (this.vmlist.get(i).vm.getState().equals(VirtualMachine.State.SHUTDOWN) && this.vmlist.get(i).pm.isReHostableRequest(this.instance.arc)){
				try {
					ResourceAllocation ra = this.vmlist.get(i).pm.allocateResources(this.instance.arc, false,
							PhysicalMachine.defaultAllocLen);
							
					this.vmlist.get(i).vm.switchOn(ra, null);	
					this.vmlist.get(i).lastWorked = Timed.getFireCount();
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
}
	
	private void turnoffVM() {
		
		for (VmCollector vmcl : this.vmlist) {
			
			if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING) && !vmcl.id.equals("broker") && vmcl.isworking==false &&  vmcl.installed<(Timed.getFireCount()-this.getFrequency())) {
				try {
					vmcl.restarted++;
					vmcl.lastWorked = Timed.getFireCount();
					vmcl.vm.switchoff(false);
					
					
				} catch (StateChangeException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void tick(long fires) {
		// ha erkezett be a kozponti repoba feldolgozatlan adat
		this.localfilesize = (this.sumOfData() - this.allgenerateddatasize);
		// System.out.println(this.sumOfData());
		if (this.localfilesize > 0) {
			long processed = 0;
			boolean havevm = true;
			while (this.localfilesize != processed && havevm) { // akkor addig
																// inditsunk
																// feladatokat a
																// VM-en, amig
																// fel nem lett
																// dolgozva az
																// osszes
				if (this.localfilesize - processed > this.tasksize) {
					this.temp = this.tasksize; // maximalis feldolgozott meret
				} else {
					this.temp = (this.localfilesize - processed);
				}
				//TODO: should delete the burned value
				final double noi = this.temp == this.tasksize ? 2400 : (double) (2400 * this.temp / this.tasksize);
				/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
				// System.out.println(Application.temp+" : "+processed+ " :
				// "+Application.localfilesize+" : "+fires);
				final VmCollector vml = this.VmSearch();
				if (vml == null) {
					this.generateAndAddVM();
					havevm = false;
				} else {
					try {
						processed += this.temp;
						final String printtart = vml.vm + " started at " + Timed.getFireCount();
						vml.isworking = true;
						Application.feladatszam++;

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
											System.out.println(name + " " + printtart + " finished at "
													+ Timed.getFireCount() + " with " + ii + " bytes,lasted "
													+ (Timed.getFireCount() - i) + " ,noi: " + iii);

									}
								});
						this.allgenerateddatasize += this.temp; 
						Application.allprocessed += this.temp;
					} catch (NetworkException e) {
						e.printStackTrace();
					}
				}
			}
		}
		

		this.countVmRunningTime();
		this.turnoffVM();

		// kilepesi feltetel az app szamara
		if (Application.feladatszam == 0 && checkStationState()) {
			unsubscribe();
			if(this.stations.size()==0) {
				System.out.println("Application " + this.name + " has stopped @" + Timed.getFireCount()+" price: "+this.instance.calculateCloudCost(0)+" IoT costs: "+providers);
			}else {
				System.out.println("Application " + this.name + " has stopped @" + Timed.getFireCount()+" price: "+this.instance.calculateCloudCost(allWorkTime)+" IoT costs: "+providers);
			}
			
			
			
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

		
	public double getCurrentCostofApp() {
		return this.instance.calculateCloudCost(this.allWorkTime);
	}
	
	public double getCurrentCostofVM(VmCollector vmc) {
		return this.instance.calculateCloudCost(vmc.workingTime);
	}
	
	public double getLoadOfCloud(){
		double usedCPU=0.0;
		for(VirtualMachine vm : this.cloud.iaas.listVMs()) {
			if(vm.getResourceAllocation() == null)
				return 0;
			usedCPU+=vm.getResourceAllocation().allocated.getRequiredCPUs();
		}
		//System.out.println(this.cloud.name + " load: "+ (usedCPU / this.cloud.iaas.getRunningCapacities().getRequiredCPUs())*100  );
		return (usedCPU / this.cloud.iaas.getRunningCapacities().getRequiredCPUs())*100;
	}
	
	private void countVmRunningTime() {
		for (VmCollector vmc : this.vmlist) {
			if ( /* vmc.vm!=null &&*/ vmc.vm.getState().equals(VirtualMachine.State.RUNNING)) {
				vmc.workingTime += (Timed.getFireCount() - vmc.lastWorked);
				allWorkTime+=(Timed.getFireCount() - vmc.lastWorked);
				vmc.lastWorked = Timed.getFireCount();
				
			}
		}
	}

	private boolean checkStationState() { // TODO probably wrong, but lets see
		
			for (Device s : this.stations) {
				// System.out.println(s + " "+ Timed.getFireCount());
				if (s.isSubscribed()) {
					return false;
				}
			}
		
		return true;
	}

	public long sumOfData() {
		long temp = 0;
		for (Device s : this.stations) {
			temp += s.generatedFilesize;
		}
		return temp;
	}

	public static void addStation(Device s, Application a) {
		a.stations.add(s);
		s.app = a;
	}
}