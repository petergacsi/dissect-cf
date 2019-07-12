package hu.uszeged.inf.iot.simulator.fog;

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
	public static List<Application> applications = new ArrayList<Application>();
	//public static List<FogApp> fogApplications = new ArrayList<FogApp>();
	protected long tasksize;
	
	
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
	public String type;
	
	public int incomingData;
	
	

	public Application(final long freq, long tasksize, String instance, String name, String type,double noi ,ComputingAppliance computingAppliance) {
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
		
		
//		this.childComputingDevice = new ArrayList<ComputingAppliance>();
		
		
		Application.applications.add(this);
		
		if (type != null) {
		this.freq=freq;
		subscribe(freq);
		}
		
		/*this.freq = freq;
		subscribe(freq);*/
		
		
		this.instance = Instance.getInstances().get(instance);
		
		this.type=type;
	
		
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
	
	public static List<FogApp> getFogApplications(){
		List<FogApp> fogApplications = new ArrayList<FogApp>(); 
		for (Application app : applications) {
			if (app.type.equals("FogApp")) {
				fogApplications.add((FogApp)app);
			}
		}
		return fogApplications;
	}

	
	public ComputingAppliance getARandomNeighbourAppliance() {
		Random ran = new Random();
		if (this.computingAppliance.neighbours.size() == 0) {
			return null;
		}
		int randomIndex = ran.nextInt(this.computingAppliance.neighbours.size());
		return this.computingAppliance.neighbours.get(randomIndex);
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
	
	public void handleDataTransderToNeighbourAppliance(long unprocessedData) {
		ComputingAppliance ca = this.getARandomNeighbourAppliance();
		if (ca != null) {
			Application app = this.getARandomApplication(ca);
			try {
				//System.out.println("Ide küldtük: " +  app.name);
				this.initiateDataTransferToNeighbourAppliance(unprocessedData, ca, app);
			} catch (NetworkException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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