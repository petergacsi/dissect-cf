package hu.u_szeged.inf.fog.simulator.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.u_szeged.inf.fog.simulator.iot.Device;
import hu.u_szeged.inf.fog.simulator.physical.ComputingAppliance;
import hu.u_szeged.inf.fog.simulator.providers.Instance;
import hu.u_szeged.inf.fog.simulator.providers.Provider;
import hu.u_szeged.inf.fog.simulator.util.TimelineGenerator.TimelineCollector;

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
	public static List<Application> applications = new ArrayList<Application>();
	//public static List<FogApp> fogApplications = new ArrayList<FogApp>();
	protected long tasksize;
	
	public List<Device> ownStations = new ArrayList<Device>();
	
	public ComputingAppliance computingAppliance;
	//public List<ComputingAppliance> childComputingDevice;
	
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
	
	public int incomingData;
	public double dataLoad;
	

	public Application(final long freq, long tasksize, String instance, String name, double noi ,ComputingAppliance computingAppliance) {
		if(noi>0) {
			defaultNoi=noi;
		}
		
		this.vmlist = new ArrayList<VmCollector>();
		this.tasksize = tasksize;
		//this.allWorkTime=0;
		
		this.name = name;
		
		//create relation between a device and its apps
		this.computingAppliance = computingAppliance;
		this.computingAppliance.applications.add(this);
		
		
		
		Application.applications.add(this);
		
		
		this.freq=freq;
		subscribe(freq);
		
		
		this.instance = Instance.getInstances().get(instance);
				
		this.computingAppliance.iaas.repositories.get(0).registerObject(this.instance.getVa());
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
		
		this.incomingData = 0;
		
	}
	
	
	public static Application getApplicationsByName(String name) {
		for (Application app : applications) {
			if (app.name.equals(name)) {
				return app;
			}
		}
		return null;
	}
	
	protected boolean checkStationState() {
		for (Device s : this.ownStations) {
			if (s.isSubscribed()) {
				return false;
			}
		}
		return true;
	}
	
	
	public Application getARandomApplication(ComputingAppliance ca) {
		for (Application application: ca.applications) {
			if (application.isSubscribed()) {
				return application;
			}
		}
		Random ran = new Random();
		int randomIndex = ran.nextInt(ca.applications.size());
		return ca.applications.get(randomIndex);
		
	}
		
	public void initiateDataTransferToNeighbourAppliance(long unprocessedData, ComputingAppliance ca,
			Application application) throws NetworkException {

		final Application app = application;
		
		app.incomingData++;
		this.sumOfArrivedData -= unprocessedData;
		if (app.isSubscribed()) {
			final long unprocessed = unprocessedData;
			NetworkNode.initTransfer(unprocessedData, ResourceConsumption.unlimitedProcessing,
					this.computingAppliance.iaas.repositories.get(0), ca.iaas.repositories.get(0), new ConsumptionEvent() {

						@Override
						public void conComplete() {
							app.sumOfArrivedData += unprocessed;
							app.incomingData--;
						}

						@Override
						public void conCancelled(ResourceConsumption problematic) {

						}

					});

		} else {
			try {
				
			
				app.restartApplication();
				new BrokerCheck(this, app, unprocessedData , (app.freq / 2));
			} catch (VMManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public ComputingAppliance getNeighbourFromAppliance(double treshHold) {
		List<ComputingAppliance> neighboursWithSufficientLoad = computingAppliance.getNeighboursWithSufficientLoad(treshHold);
		ComputingAppliance aRandomNeighbourApplianceFromSufficientAppliances = computingAppliance.getARandomNeighbourApplianceFromSufficientAppliances(neighboursWithSufficientLoad);
		return aRandomNeighbourApplianceFromSufficientAppliances;
	}
	
	public void handleDataTransferToNeighbourAppliance(long unprocessedData, ComputingAppliance ca) {
		Application app = ca.getLeastLoadedApplication();
		try {

			this.initiateDataTransferToNeighbourAppliance(unprocessedData, ca, app);
		} catch (NetworkException e) {

			e.printStackTrace();
		}

	}
	
		
		
	private void startBroker() throws VMManagementException, NetworkException {
		if(this.vmlist.contains(this.broker) && this.broker.pm.isReHostableRequest(this.instance.getArc())) {
			ResourceAllocation ra = this.broker.pm.allocateResources(this.instance.getArc(), false,
					PhysicalMachine.defaultAllocLen);
			this.broker.restarted++;		
			this.broker.vm.switchOn(ra, null);
			this.broker.lastWorked = Timed.getFireCount();
			for(Provider p : this.providers) {
				p.startProvider();
			}
		}else {
			try {
				VirtualMachine vm =this.computingAppliance.iaas.requestVM(this.instance.getVa(), this.instance.getArc(),this.computingAppliance.iaas.repositories.get(0), 1)[0];
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
				for (PhysicalMachine pm : this.computingAppliance.iaas.machines) {
					if (pm.isReHostableRequest(this.instance.getArc())) {
						VirtualMachine vm = pm.requestVM(this.instance.getVa(), this.instance.getArc(),
								this.computingAppliance.iaas.repositories.get(0), 1)[0];
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
			if (this.vmlist.get(i).vm.getState().equals(VirtualMachine.State.SHUTDOWN) && this.vmlist.get(i).pm.isReHostableRequest(this.instance.getArc())){
				try {
					ResourceAllocation ra = this.vmlist.get(i).pm.allocateResources(this.instance.getArc(), false,
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
		for(VirtualMachine vm : this.computingAppliance.iaas.listVMs()) {
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
		return (usedCPU / this.computingAppliance.iaas.getRunningCapacities().getRequiredCPUs())*100;
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
		System.out.println("\n" +  this.name+" application has been restarted at "+Timed.getFireCount());
		subscribe(this.freq);
		this.startBroker();
	}
	
	
	public double calculateDistance(Application app, Application other) {
		double result = Math.sqrt(
				Math.pow((other.computingAppliance.x - app.computingAppliance.x),2) + 
				Math.pow((other.computingAppliance.y - app.computingAppliance.y),2)
				);
		return result;
	}
	
			
	public abstract void tick(long fires);

	@Override
	public String toString() {
		return "Application [computingAppliance=" + computingAppliance + ", name=" + name + "]";
	}
	
	
	
	
	
	
}