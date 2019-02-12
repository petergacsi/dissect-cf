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
		boolean isWorking;
		int taskCounter;
		long lastWorked;
		public long workingTime;
		String id;
		public long installed;

		VmCollector(VirtualMachine vm) {
			this.vm = vm;
			this.isWorking = false;
			this.taskCounter = 0;
			this.workingTime = 0;
			this.lastWorked = Timed.getFireCount();
			this.installed = Timed.getFireCount();
			this.id=Integer.toString(vmlist.size());
		}
	}


	public static ArrayList<Application> applications = new ArrayList<Application>();
	private long tasksize;
	public Cloud cloud;
	public ArrayList<Device> stations;
	public String name;
	Instance instance;
	public ArrayList<Provider> providers;
	public ArrayList<VmCollector> vmlist;
	public long sumOfWorkTime;
	public long sumOfProcessedData;
	private long allocatedData;
	private int currentTask = 0;
	public long stopTime;
	
	public static void loadApplication(String appfile) throws JAXBException {
		for (ApplicationModel am : ApplicationModel.loadApplicationXML(appfile)) {
			new Application(am.freq, am.tasksize, am.cloud, am.instance, am.name);
		}
	}

	private Application(final long freq, long tasksize, String cloud, String instance, String name) {
		this.vmlist = new ArrayList<VmCollector>();
		this.stations = new ArrayList<Device>();
		this.tasksize = tasksize;
		//this.allWorkTime=0;
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
		this.sumOfWorkTime=0;
		sumOfProcessedData = 0;
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
			if ((this.vmlist.get(i).isWorking == false
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
					if (pm.isReHostableRequest(this.instance.arc)) {
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
			
			if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING) && !vmcl.id.equals("broker") && vmcl.isWorking==false) {
				try {
					vmcl.lastWorked = Timed.getFireCount();
					vmcl.vm.switchoff(false);					
				} catch (StateChangeException e) {
					e.printStackTrace();
				}
			}
		}
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
	
	public long sumOfGeneratedData() {
		long temp = 0;
		for (Device s : this.stations) {
			temp += s.sumOfGeneratedData;
		}
		return temp;
	}
	
	public static void addStation(Device s, Application a) {
		a.stations.add(s);
		s.app = a;
	}
	
	private boolean checkStationState() { 
		for (Device s : this.stations) {
			if (s.isSubscribed()) {
				return false;
			}
		}
	return true;
}
	
	public double getCurrentCostofApp() {
		return this.instance.calculateCloudCost(this.sumOfWorkTime);
	}
	
	private void countVmRunningTime() {
		for (VmCollector vmc : this.vmlist) {
			if ( vmc.vm.getState().equals(VirtualMachine.State.RUNNING)) {
				vmc.workingTime += (Timed.getFireCount() - vmc.lastWorked);
				sumOfWorkTime+= (Timed.getFireCount() - vmc.lastWorked);
				vmc.lastWorked = Timed.getFireCount();
				
			}
		}
	}
	
		
	public void tick(long fires) {

		long unprocessedData = (this.sumOfGeneratedData() - this.sumOfProcessedData);

		if (unprocessedData > 0) {
			
			long processedData = 0;

			while (unprocessedData != processedData) { 
				if (unprocessedData - processedData > this.tasksize) {
					this.allocatedData = this.tasksize; 
				} else {
					this.allocatedData = (unprocessedData - processedData);
				}
				final VmCollector vml = this.VmSearch();
				if (vml == null) {
					this.generateAndAddVM();
					break;
				} else {
					try {
						//TODO: should delete the burned value
						final double noi = this.allocatedData == this.tasksize ? 2400 : (double) (2400 * this.allocatedData / this.tasksize);
						processedData += this.allocatedData;
						vml.isWorking = true;
						this.currentTask++;

						vml.vm.newComputeTask(noi, ResourceConsumption.unlimitedProcessing,
								new ConsumptionEventAdapter() {
									long vmStartTime = Timed.getFireCount();
									long allocatedDataTemp = allocatedData;
									double noiTemp = noi;

									@Override
									public void conComplete() {
										vml.isWorking = false;
										vml.taskCounter++;
										currentTask--;
											System.out.println(name + " started@ " + vmStartTime + " finished@ "
													+ Timed.getFireCount() + " with " + allocatedDataTemp + " bytes, lasted "
													+ (Timed.getFireCount() - vmStartTime) + " ,noi: " + noiTemp);

									}
								});
						this.sumOfProcessedData += this.allocatedData; 
					} catch (NetworkException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		this.countVmRunningTime();
		this.turnoffVM();

		if (this.currentTask == 0 && checkStationState()) {
			unsubscribe();
			this.stopTime=Timed.getFireCount();

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