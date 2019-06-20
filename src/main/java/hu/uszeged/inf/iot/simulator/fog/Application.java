package hu.uszeged.inf.iot.simulator.fog;

import java.util.ArrayList;
import javax.xml.bind.JAXBException;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.uszeged.inf.iot.simulator.entities.Device;
import hu.uszeged.inf.iot.simulator.providers.Instance;
import hu.uszeged.inf.iot.simulator.providers.Provider;
import hu.uszeged.inf.iot.simulator.system.Cloud;
import hu.uszeged.inf.iot.simulator.util.TimelineGenerator;
import hu.uszeged.inf.iot.simulator.util.TimelineGenerator.TimelineCollector;
import hu.uszeged.inf.xml.model.ApplicationModel;

public class Application extends Timed {

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
	public static ArrayList<Application> cloudApplications = new ArrayList<Application>();
	public static ArrayList<Application> fogApplications = new ArrayList<Application>();
	private long tasksize;
	public Cloud fog;
	public ArrayList<Device> stations;
	public String name;
	public Instance instance;
	public ArrayList<Provider> providers;
	public ArrayList<VmCollector> vmlist;
	public long sumOfWorkTime;
	public long sumOfProcessedData;
	private long allocatedData;
	private int currentTask;
	public long stopTime;
	public long sumOfArrivedData;
	private long freq;
	private VmCollector broker;
	private String type;
	
	public static void loadApplication(String appfile) throws JAXBException {
		for (ApplicationModel am : ApplicationModel.loadApplicationXML(appfile)) {
			new Application(am.freq, am.tasksize, am.cloud, am.instance, am.name,"fog",0);
		}
	}

	public Application(final long freq, long tasksize, String cloud, String instance, String name, String type,double noi) {
		if(noi>0) {
			defaultNoi=noi;
		}
		this.vmlist = new ArrayList<VmCollector>();
		this.stations = new ArrayList<Device>();
		this.tasksize = tasksize;
		//this.allWorkTime=0;
		//this.fog = Cloud.addApplication(this, cloud);
		this.name = name;
		if (cloud != null) {
			this.freq=freq;
			subscribe(freq);
		}
		this.instance = Instance.getInstances().get(instance);
		this.type=type;
		if(type.equals("fog")) {
			Application.fogApplications.add(this);
		}else{
			Application.cloudApplications.add(this);
		}
		
		this.fog.getIaas().repositories.get(0).registerObject(this.instance.getVa());
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
				VirtualMachine vm =this.fog.getIaas().requestVM(this.instance.getVa(), this.instance.getArc(),this.fog.getIaas().repositories.get(0), 1)[0];
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
	
	private VmCollector VmSearch() {
		for (int i = 0; i < this.vmlist.size(); i++) {
			if ((this.vmlist.get(i).isWorking == false
					&& this.vmlist.get(i).vm.getState().equals(VirtualMachine.State.RUNNING) && !this.vmlist.get(i).id.equals("broker"))) {
				return this.vmlist.get(i);

			}
		}
		return null;
	}
	
	private boolean generateAndAddVM() {
		try {
			if (this.turnonVM() == false) {
				for (PhysicalMachine pm : this.fog.getIaas().machines) {
					if (pm.isReHostableRequest(this.instance.getArc())) {
						VirtualMachine vm = pm.requestVM(this.instance.getVa(), this.instance.getArc(),
								this.fog.getIaas().repositories.get(0), 1)[0];
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

	private boolean turnonVM() {
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
	
	private void turnoffVM() {
		
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
		for(VirtualMachine vm : this.fog.getIaas().listVMs()) {
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
		return (usedCPU / this.fog.getIaas().getRunningCapacities().getRequiredCPUs())*100;
	}
	
	
	
	public static void addStation(Device s, hu.uszeged.inf.iot.simulator.system.Application a) {
		a.getStations().add(s);
		s.setApp(a);
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
	
	public void restartApplication() throws VMManagementException, NetworkException {
		System.out.println(this.name+" application has been restarted!");
		subscribe(this.freq);
		this.startBroker();
	}
	
		
	public void tick(long fires) {
		long unprocessedData = (this.sumOfArrivedData - this.sumOfProcessedData);

		if (unprocessedData > 0) {
			System.out.print(Timed.getFireCount()+" unprocessed data: "+unprocessedData+ " "+this.name+" ");
			long processedData = 0;

			while (unprocessedData != processedData) { 
				if (unprocessedData - processedData > this.tasksize) {
					this.allocatedData = this.tasksize; 
				} else {
					this.allocatedData = (unprocessedData - processedData);
				}
				final VmCollector vml = this.VmSearch();
				if (vml == null) {
					double ratio = ((double)unprocessedData/this.tasksize);
										
					if(ratio>2) {
//						if(this.fog.cloud!=null) {
//							try {
//								if(this.fog.app.isSubscribed()) {
//									this.sumOfArrivedData-=unprocessedData;
//									final Fog foggy = (Fog) this.fog;
//									final long unprocessed = unprocessedData;
//									NetworkNode.initTransfer(unprocessedData, ResourceConsumption.unlimitedProcessing, 
//											this.fog.iaas.repositories.get(0), this.fog.cloud.iaas.repositories.get(0), new ConsumptionEvent() {
//
//												@Override
//												public void conComplete() {
//													foggy.app.sumOfArrivedData +=  unprocessed;
//												}
//
//												@Override
//												public void conCancelled(ResourceConsumption problematic) {
//													
//												}
//										
//									});
//								}else {
//									try {
//										this.fog.app.restartApplication();
//									} catch (VMManagementException e) {
//										// TODO Auto-generated catch block
//										e.printStackTrace();
//									}
//								}
//								
//							} catch (NetworkException e) {
//								e.printStackTrace();
//							}
//						}
					}
					System.out.print("data/VM: "+ratio+" unprocessed after exit: "+unprocessedData+ " decision:");
					this.generateAndAddVM();
					
					break;
					
					
					
				} else {
					try {
						final double noi = this.allocatedData == this.tasksize ? defaultNoi : (double) (2400 * this.allocatedData / this.tasksize);
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
										stopTime=Timed.getFireCount();
										timelineList.add(new TimelineCollector(vmStartTime,Timed.getFireCount(),vml.id));
											System.out.println(name +" "+vml.id+ " started@ " + vmStartTime + " finished@ "
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
			System.out.println(" load(%): "+this.getLoadOfCloud());
		}
		this.countVmRunningTime();
		this.turnoffVM();

		if (this.currentTask == 0 && checkStationState()) {
			unsubscribe();
			for(Provider p : this.providers) {
				if(p.isSubscribed()) {
					p.shouldStop=true;
				}
			}
			StorageObject so = new StorageObject(this.name, this.sumOfProcessedData, false);
			if(!this.fog.getIaas().repositories.get(0).registerObject(so)){
				this.fog.getIaas().repositories.get(0).deregisterObject(so);
				this.fog.getIaas().repositories.get(0).registerObject(so);
			}
			
			for (VmCollector vmcl : this.vmlist) {
				try {
					if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)) {
						if(vmcl.id.equals("broker")) {
							vmcl.pm=vmcl.vm.getResourceAllocation().getHost();
						}
						vmcl.vm.switchoff(true);
						
					}
				} catch (StateChangeException e) {
					e.printStackTrace();
				}
			}
		}
	}
}