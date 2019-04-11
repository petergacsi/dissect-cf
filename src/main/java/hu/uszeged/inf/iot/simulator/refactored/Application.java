package hu.uszeged.inf.iot.simulator.refactored;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.uszeged.inf.iot.simulator.providers.Instance;
import hu.uszeged.inf.iot.simulator.providers.Provider;
import hu.uszeged.inf.iot.simulator.util.TimelineGenerator.TimelineCollector;

public abstract class Application extends Timed {

	public class VmCollector {
		@Override
		public String toString() {
			return "VmCollector [vm=" + vm + ", id=" + id + "]";
		}

		PhysicalMachine pm;
		public VirtualMachine vm;
		boolean isWorking;
		public int taskCounter;
		long lastWorked;
		public long workingTime;
		public String id;
		public long installed;
		public int restarted;

		VmCollector(VirtualMachine vm) {
			this.vm = vm;
			this.isWorking = false;
			this.taskCounter = 0;
			this.workingTime = 0;
			this.lastWorked = Timed.getFireCount();
			this.installed = Timed.getFireCount();
			this.id=Integer.toString(vmlist.size());
			this.restarted=0;
		}
	}
	public static double defaultNoi = 2400;
	public ArrayList<TimelineCollector> timelineList = new ArrayList<TimelineCollector>();
	
	//need to store all applications?

	public static ArrayList<FogApp> fogApplications = new ArrayList<FogApp>();
	public static ArrayList<GateWayApp> gateWayApplications = new ArrayList<GateWayApp>();
	protected long tasksize;
	
	
	public ComputingAppliance computingDevice;
	public List<FogDevice> childComputingDevice;
	
	public String name;
	public Instance instance;
	public ArrayList<Provider> providers;
	public ArrayList<VmCollector> vmlist;
	public long sumOfWorkTime;
	public long sumOfProcessedData;
	protected long allocatedData;
	protected int currentTask;
	public long stopTime;
	public long sumOfArrivedData;
	protected long freq;
	protected VmCollector broker;
	protected String type;
	
	public abstract void loadApplication(String appfile) throws JAXBException;

	public Application(final long freq, long tasksize, String cloud, String instance, String name, String type,double noi ,ComputingAppliance computingAppliance) {
		if(noi>0) {
			defaultNoi=noi;
		}
		this.vmlist = new ArrayList<VmCollector>();
		this.tasksize = tasksize;
		//this.allWorkTime=0;
		
		this.name = name;
		
		//create relation between a device and its apps
		this.computingDevice = computingAppliance;
		this.computingDevice.applications.add(this);
		
		if (cloud != null) {
			this.freq=freq;
			subscribe(freq);
		}
		this.instance = Instance.instances.get(instance);
		this.type=type;
	
		
		this.computingDevice.iaas.repositories.get(0).registerObject(this.instance.va);
		try {
			this.startBroker();
		} catch (VMManagementException e) {
			e.printStackTrace();
		} catch (NetworkException e) {
			e.printStackTrace();
		}
		providers = new ArrayList<Provider>();
		this.sumOfWorkTime=0;
		this.sumOfProcessedData = 0;
		this.currentTask = 0;
		this.sumOfArrivedData=0;
		
	}
	
	
	//create a relation between app and its devices
		public void makeRelationBetweenDevices(List<FogDevice> listOfChildDevices) {
			this.childComputingDevice = listOfChildDevices;
			for (FogDevice computingAppliance : childComputingDevice) {
				computingAppliance.addParentApp(this);
			}
		}
	

	private void startBroker() throws VMManagementException, NetworkException {
		if(this.vmlist.contains(this.broker) && this.broker.pm.isReHostableRequest(this.instance.arc)) {
			ResourceAllocation ra = this.broker.pm.allocateResources(this.instance.arc, false,
					PhysicalMachine.defaultAllocLen);
			this.broker.restarted++;		
			this.broker.vm.switchOn(ra, null);
			this.broker.lastWorked = Timed.getFireCount();
			for(Provider p : this.providers) {
				p.startProvider();
			}
		}else {
			try {
				VirtualMachine vm =this.computingDevice.iaas.requestVM(this.instance.va, this.instance.arc,this.computingDevice.iaas.repositories.get(0), 1)[0];
				if(vm!=null) {
					VmCollector vmc = new VmCollector(vm);
					vmc.id="broker";
					this.vmlist.add(vmc);
					this.broker=vmc;
			 }

			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		
	}
	
	protected VmCollector VmSearch() {
		for (int i = 0; i < this.vmlist.size(); i++) {
			if ((this.vmlist.get(i).isWorking == false
					&& this.vmlist.get(i).vm.getState().equals(VirtualMachine.State.RUNNING) && !this.vmlist.get(i).id.equals("broker"))) {
				return this.vmlist.get(i);

			}
		}
		return null;
	}
	
	protected boolean generateAndAddVM() {
		
		try {
			if (this.turnonVM() == false) {
				for (PhysicalMachine pm : this.computingDevice.iaas.machines) {
					if (pm.isReHostableRequest(this.instance.arc)) {
						VirtualMachine vm = pm.requestVM(this.instance.va, this.instance.arc,
								this.computingDevice.iaas.repositories.get(0), 1)[0];
						if(vm!=null) {
							VmCollector vmc = new VmCollector(vm);
							vmc.pm=pm;
							this.vmlist.add(vmc);
							System.out.print(" asked new VM");
							return true;
						}
						
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	protected boolean turnonVM() {
		for (int i = 0; i < this.vmlist.size(); i++) {
			if (this.vmlist.get(i).vm.getState().equals(VirtualMachine.State.SHUTDOWN) && this.vmlist.get(i).pm.isReHostableRequest(this.instance.arc)){
				try {
					ResourceAllocation ra = this.vmlist.get(i).pm.allocateResources(this.instance.arc, false,
							PhysicalMachine.defaultAllocLen);
					this.vmlist.get(i).restarted++;		
					this.vmlist.get(i).vm.switchOn(ra, null);	
					this.vmlist.get(i).lastWorked = Timed.getFireCount();
					System.out.print(" turned on VM");
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
}
	
	protected void turnoffVM() {
		
		for (VmCollector vmcl : this.vmlist) {
			
			if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING) && !vmcl.id.equals("broker") && vmcl.isWorking==false ) {
					//&& vmcl.installed<(Timed.getFireCount()-this.getFrequency()) ) {
				try {
					vmcl.vm.switchoff(false);					
				} catch (StateChangeException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public double getLoadOfCloud(){
		double usedCPU=0.0;
		for(VirtualMachine vm : this.computingDevice.iaas.listVMs()) {
			if(vm.getResourceAllocation() == null) {
				usedCPU+=0.0;
			}else {
				usedCPU+=vm.getResourceAllocation().allocated.getRequiredCPUs();
			}
		}
		// TODO: why IaaS runningCapacities isn't equals with pm's capacities? 
		/* 
		double t=0.0;
		for(PhysicalMachine pm : this.cloud.iaas.runningMachines) {
			t+=pm.getCapacities().getRequiredCPUs();
		}
		System.out.println(t+"/"+this.cloud.iaas.getRunningCapacities().getRequiredCPUs()+"/" +this.cloud.iaas.getCapacities().getRequiredCPUs());
		*/
		return (usedCPU / this.computingDevice.iaas.getRunningCapacities().getRequiredCPUs())*100;
	}
	
	
	
	
	public double getCurrentCostofApp() {
		return this.instance.calculateCloudCost(this.sumOfWorkTime);
	}
	
	protected void countVmRunningTime() {
		for (VmCollector vmc : this.vmlist) {
			if ( vmc.vm.getState().equals(VirtualMachine.State.RUNNING)) {
				vmc.workingTime += (Timed.getFireCount() - vmc.lastWorked);
				sumOfWorkTime+= (Timed.getFireCount() - vmc.lastWorked);
				vmc.lastWorked = Timed.getFireCount();
			}
		}
	}
	
	public void restartApplication() throws VMManagementException, NetworkException {
		System.out.println(this.name+" application has been restarted!");
		subscribe(this.freq);
		this.startBroker();
	}
	
		
	public abstract void tick(long fires);

	@Override
	public String toString() {
		return "Application [computingDevice=" + computingDevice + ", name=" + name + "]";
	}
	
	
	
	
	
	
}