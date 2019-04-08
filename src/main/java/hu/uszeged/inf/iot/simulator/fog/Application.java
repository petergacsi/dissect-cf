/*
 *  ========================================================================
 *  DIScrete event baSed Energy Consumption simulaTor 
 *    					             for Clouds and Federations (DISSECT-CF)
 *  ========================================================================
 *  
 *  This file is part of DISSECT-CF.
 *  
 *  DISSECT-CF is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at
 *  your option) any later version.
 *  
 *  DISSECT-CF is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *  General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with DISSECT-CF.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  (C) Copyright 2019, Andras Markus (markusa@inf.u-szeged.hu)
 */

package hu.uszeged.inf.iot.simulator.fog;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.uszeged.inf.iot.simulator.entities.Device;
import hu.uszeged.inf.iot.simulator.providers.Instance;
import hu.uszeged.inf.iot.simulator.providers.Provider;
import hu.uszeged.inf.iot.simulator.util.TimelineGenerator.TimelineCollector;
import hu.uszeged.inf.xml.model.ApplicationModel;

/**
 * This class represents an independent application for consuming the cloud resources.
 * It includes the virtual machine management which is responsible for the data processing. 
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 *
 */
public class Application extends Timed {

	/**
	 * Helper class for monitoring and storing virtual machines.
	 * @author Andras Markus (markusa@inf.u-szeged.hu)
	 *
	 */
	public class VmCollector {
		
		/**
		 * toString method, can be useful for debugging.
		 */
		@Override
		public String toString() {
			return "VmCollector [vm=" + getVm() + ", id=" + getId() + "]";
		}

		/**
		 * Reference for the physical machine which serves the virtual machines.
		 */
		PhysicalMachine pm;
		
		/**
		 * The actual virtual machine.
		 */
		private VirtualMachine vm;
		
		/**
		 * Logical variable, true if the virtual machine is working 
		 * (~does ComputeTask for simulating data processing)
		 */
		boolean isWorking;
		
		/**
		 * Counter for the number of finished tasks.
		 */
		private int taskCounter;
		
		/**
		 * It contains the last time when the VM is processed task.
		 */
		long lastWorked;
		
		/**
		 * It contains all the time which the virtual machine spent with work.
		 */
		private long workingTime;
		
		/**
		 * The Id of the virtual machine.
		 */
		private String id;
		
		/**
		 * Contains the time when the VM has been created.
		 */
		private long installed;
		
		/**
		 * Contains the number of returning from shutdown state to running state for the vm.
		 */
		private int restarted;

		/**
		 * The constructor initializes the parameters with zero or with the actual simulated time.
		 * @param vm The monitored virtual machine.
		 */
		VmCollector(VirtualMachine vm) {
			this.vm=vm;
			this.isWorking = false;
			this.taskCounter=0;
			this.workingTime=0;
			this.lastWorked = Timed.getFireCount();
			this.installed=(Timed.getFireCount());
			this.id=(Integer.toString(getVmlist().size()));
			this.restarted=0;
		}

		/**
		 * Getter method for the number of finished tasks.
		 */
		public int getTaskCounter() {
			return taskCounter;
		}

		/**
		 * Getter method for the monitored virtual machine.
		 */
		public VirtualMachine getVm() {
			return vm;
		}

		/**
		 * Getter method for the total working time of the monitored virtual machine.
		 */
		public long getWorkingTime() {
			return workingTime;
		}

		/**
		 * Getter method for the id of the virtual machine.
		 */
		public String getId() {
			return id;
		}

		/**
		 * Getter method for the number of restarting of the virtual machine.
		 */
		public int getRestarted() {
			return restarted;
		}

		/**
		 * Getter method for the time when the VM was created.
		 */
		public long getInstalled() {
			return installed;
		}

	}
	
	/**
	 * Static string to avoid repeating.
	 */
	private static final String BROKER = "broker";
	
	/**
	 * Default number of instruction which a ComputeTask get in parameter.
	 */
	protected double defaultNoi = 2400;
	
	/**
	 * Helper list for the time line generator.
	 */
	public ArrayList<TimelineCollector> timelineList = new ArrayList<TimelineCollector>();
	
	/**
	 * Static list for all of generated applications.
	 */
	private static ArrayList<Application> applications = new ArrayList<Application>();
	
	/**
	 * This value tells the application how many data belong to one task.
	 */
	private long tasksize;
	
	/**
	 * Tells this application belongs to which cloud.
	 */
	private Cloud cloud;
	
	/**
	 * List of devices which belongs to this application.
	 */
	private ArrayList<Device> stations;
	
	/**
	 * The Id of the application.
	 */
	private String name;
	
	/**
	 * Contains all useful informations about the created virtual machine
	 * including CPU,memory and core processing power, virtual appliance
	 * and price per tick. 
	 */
	private Instance instance;
	
	/**
	 * List of the IoT providers of this application.
	 */
	private ArrayList<Provider> providers;
	
	/**
	 * List of the operated virtual machines.
	 */
	private ArrayList<VmCollector> vmlist;
	
	/**
	 * The total time of the running virtual machines.
	 */
	private long sumOfWorkTime;
	
	/**
	 * The total amount of processed data.
	 */
	private long sumOfProcessedData;
	
	/**
	 * The allocated data for one task.
	 */
	private long allocatedData;
	
	/**
	 * Helper variable for managing application shutdown process. 
	 * It stores the number of running tasks at a moment.
	 */
	private int currentTask;
	
	/**
	 * It contains the last time when the application worked.
	 */
	private long stopTime;
	
	/**
	 * It contains the amount of arrived data from the devices.
	 */
	private long sumOfArrivedData;
	
	/**
	 * The frequency of the application.
	 * The time when the virtual machine managing functions will be called periodically.
	 */
	private long freq;
	
	/**
	 * A special virtual machine which run when the application run as well.
	 * It simulates the communication between heterogeneous systems (the cloud and the devices).
	 */
	private VmCollector brokerVm;
	
	/**
	 * A always increased file to simulate the file reservation.
	 */
	private StorageObject finalStorageObject;
	
	/**
	 * It is load application from XML files.
	 * @param appfile The path of the file.
	 */
	public static void loadApplication(String appfile) {
		try {
			for (ApplicationModel am : ApplicationModel.loadApplicationXML(appfile)) {
				new Application(am.freq, am.tasksize, am.cloud, am.instance, am.name,0);
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Constructor for default values and it starts the broker service, loads the IoT providers.
	 * @param freq The frequency of the application - the periodical time for the VM managing functions.
	 * @param tasksize It tells how many data belongs to in one task.
	 * @param cloud Reference for the IaaS resources.
	 * @param instance It contains the cloud pricing and the VM parameters.
	 * @param name The id of the cloud.
	 * @param noi The number of instruction which is processed in case of fully task.
	 */
	public Application(final long freq, long tasksize, String cloud, String instance, String name,double noi) {
		if(noi>0) {
			defaultNoi=noi;
		}
		this.vmlist=(new ArrayList<VmCollector>());
		this.stations=(new ArrayList<Device>());
		this.tasksize = tasksize;
		this.cloud=(Cloud.addApplication(this, cloud));
		this.name=(name);
		if (cloud != null) {
			this.freq=freq;
			subscribe(freq);
		}
		this.instance=(Instance.instances.get(instance));
		Application.getApplications().add(this);
		
		this.getCloud().getIaas().repositories.get(0).registerObject(this.getInstance().va);
		this.setFinalStorageObject(new StorageObject(this.getName()+"SO", 0, false));
		this.getCloud().getIaas().repositories.get(0).registerObject(this.getFinalStorageObject());
		this.startBroker();
		this.providers=(new ArrayList<Provider>());
		this.sumOfWorkTime=(0);
		this.sumOfProcessedData=(0);
		this.currentTask = 0;
		this.setSumOfArrivedData(0);
	}

	/**
	 * This method restart the broker virtual machine if the application is stopped, 
	 * or if the broker virtual machine doesn't exist then generates one.
	 */
	private void startBroker()  {
		if(this.getVmlist().contains(this.brokerVm) && this.brokerVm.pm.isReHostableRequest(this.getInstance().arc)) {
			ResourceAllocation ra= null;
			try {
				ra = this.brokerVm.pm.allocateResources(this.getInstance().arc, false,
						PhysicalMachine.defaultAllocLen);
			} catch (VMManagementException e) {
				e.printStackTrace();
			}
			this.brokerVm.restarted=(this.brokerVm.getRestarted() + 1);		
			try {
				this.brokerVm.getVm().switchOn(ra, null);
			} catch (VMManagementException e) {
				e.printStackTrace();
			} catch (NetworkException e) {
				e.printStackTrace();
			}
			this.brokerVm.lastWorked = Timed.getFireCount();
			for(Provider p : this.getProviders()) {
				p.startProvider();
			}
		}else {
			try {
				VirtualMachine vm =this.getCloud().getIaas().requestVM(this.getInstance().va, this.getInstance().arc,this.getCloud().getIaas().repositories.get(0), 1)[0];
				if(vm!=null) {
					VmCollector vmc = new VmCollector(vm);
					vmc.id=(BROKER);
					this.getVmlist().add(vmc);
					this.brokerVm=vmc;
			 }

			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		
	}
	
	/**
	 * This method find a free virtual machine if exist.
	 * @return With the free virtual machine.
	 */
	private VmCollector vmSearch() {
		for (int i = 0; i < this.getVmlist().size(); i++) {
			if ((!this.getVmlist().get(i).isWorking
					&& this.getVmlist().get(i).getVm().getState().equals(VirtualMachine.State.RUNNING) && !this.getVmlist().get(i).getId().equals(BROKER))) {
				return this.getVmlist().get(i);

			}
		}
		return null;
	}
	
	/**
	 * This method tries to turn on a stopped virtual machine.
	 * If fails then tries to generate a new one.
	 */
	private void generateAndAddVM() {
		try {
			if (!this.turnonVM()) {
				for (PhysicalMachine pm : this.getCloud().getIaas().machines) {
					if (pm.isReHostableRequest(this.getInstance().arc)) {
						VirtualMachine vm = pm.requestVM(this.getInstance().va, this.getInstance().arc,
								this.getCloud().getIaas().repositories.get(0), 1)[0];
						if(vm!=null) {
							VmCollector vmc = new VmCollector(vm);
							vmc.pm=pm;
							this.getVmlist().add(vmc);
							System.out.print(" asked new VM");
							return;
						}
						
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method tries to turn on a virtual machine in state of shutdown.
	 * @return returns with true if the turning on is successful.
	 */
	private boolean turnonVM() {
		for (int i = 0; i < this.getVmlist().size(); i++) {
			if (this.getVmlist().get(i).getVm().getState().equals(VirtualMachine.State.SHUTDOWN) && this.getVmlist().get(i).pm.isReHostableRequest(this.getInstance().arc)){
				try {
					ResourceAllocation ra = this.getVmlist().get(i).pm.allocateResources(this.getInstance().arc, false,
							PhysicalMachine.defaultAllocLen);
					this.getVmlist().get(i).restarted=(this.getVmlist().get(i).getRestarted() + 1);		
					this.getVmlist().get(i).getVm().switchOn(ra, null);	
					this.getVmlist().get(i).lastWorked = Timed.getFireCount();
					System.out.print(" turned on VM");
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	/**
	 *  It turns off the unused virtual machines.
	 */
	private void turnoffVM() {
		
		for (VmCollector vmcl : this.getVmlist()) {
			if (vmcl.getVm().getState().equals(VirtualMachine.State.RUNNING) && !vmcl.getId().equals(BROKER) && !vmcl.isWorking ) {
				try {
					vmcl.getVm().switchoff(false);					
				} catch (StateChangeException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This method calculates the ratio of the used and free CPU cores. 
	 * @return Returns with the percentage value of the ratio of the used and free CPU cores.
	 */
	public double getLoadOfCloud(){
		double usedCPU=0.0;
		for(VirtualMachine vm : this.getCloud().getIaas().listVMs()) {
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
		return (usedCPU / this.getCloud().getIaas().getRunningCapacities().getRequiredCPUs())*100;
	}
	
	/**
	 * This method pairs a device with an application.
	 * @param s The device of which data will be processed by the second argument.
	 * @param a The application which will process the data.
	 */
	public static void addStation(Device s, Application a) {
		a.getStations().add(s);
		s.setApp(a);
	}
	
	/**
	 * This method checks if there are stations in running state.
	 * @return returns with true, if there is at least one station in running state.
	 */
	private boolean checkStationState() { 
		for (Device s : this.getStations()) {
			if (s.isSubscribed()) {
				return false;
			}
		}
	return true;
}
	/**
	 * Get the actual cost of the application.
	 * @return With the current price at a moment.
	 */
	public double getCurrentCostofApp() {
		return this.getInstance().calculateCloudCost(this.getSumOfWorkTime());
	}
	
	/**
	 * It calculates the total running time of all virtual machines.
	 */
	private void countVmRunningTime() {
		for (VmCollector vmc : this.getVmlist()) {
			if ( vmc.getVm().getState().equals(VirtualMachine.State.RUNNING)) {
				vmc.workingTime=(vmc.getWorkingTime() + (Timed.getFireCount() - vmc.lastWorked));
				sumOfWorkTime=(getSumOfWorkTime() + (Timed.getFireCount() - vmc.lastWorked));
				vmc.lastWorked = Timed.getFireCount();
			}
		}
	}
	
	/**
	 * If the application isn't running but it should it will be restarted.
	 */
	public void restartApplication() {
		System.out.println(this.getName()+" application has been restarted!");
		subscribe(this.freq);
		this.startBroker();
	}
	
	/**
	 * This method will be called periodically and 
	 * the goal of the method is to manage virtual machines and the data processing.	
	 */
	public void tick(long fires) {
		long unprocessedData = (this.getSumOfArrivedData() - this.getSumOfProcessedData());

		if (unprocessedData > 0) {
			System.out.print(Timed.getFireCount()+" unprocessed data: "+unprocessedData+ " "+this.getName()+" ");
			long processedData = 0;

			while (unprocessedData != processedData) { 
				if (unprocessedData - processedData > this.tasksize) {
					this.allocatedData = this.tasksize; 
				} else {
					this.allocatedData = (unprocessedData - processedData);
				}
				final VmCollector vml = this.vmSearch();
				if (vml == null) {
					double ratio = ((double)unprocessedData/this.tasksize);
					
					System.out.print("data/VM: "+ratio+" unprocessed after exit: "+unprocessedData+ " decision:");
					this.generateAndAddVM();
					
					break;
					
					
					
				} else {
					try {
						final double noi = this.allocatedData == this.tasksize ? defaultNoi : (this.defaultNoi * (double) this.allocatedData / this.tasksize);
						processedData += this.allocatedData;
						vml.isWorking = true;
						this.currentTask++;

						vml.getVm().newComputeTask(noi, ResourceConsumption.unlimitedProcessing,
								new ConsumptionEventAdapter() {
									long vmStartTime = Timed.getFireCount();
									long allocatedDataTemp = allocatedData;
									double noiTemp = noi;

									@Override
									public void conComplete() {
										vml.isWorking = false;
										vml.taskCounter=vml.getTaskCounter() + 1;
										currentTask--;
										stopTime=(Timed.getFireCount());
										timelineList.add(new TimelineCollector(vmStartTime,Timed.getFireCount(),vml.getId()));
											System.out.println(getName() +" "+vml.getId()+ " started@ " + vmStartTime + " finished@ "
													+ Timed.getFireCount() + " with " + allocatedDataTemp + " bytes, lasted "
													+ (Timed.getFireCount() - vmStartTime) + " ,noi: " + noiTemp);

									}
								});
						this.sumOfProcessedData=(this.getSumOfProcessedData() + this.allocatedData); 
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
			for(Provider p : this.getProviders()) {
				if(p.isSubscribed()) {
					p.shouldStop=true;
				}
			}
			
			for (VmCollector vmcl : this.getVmlist()) {
				try {
					if (vmcl.getVm().getState().equals(VirtualMachine.State.RUNNING)) {
						if(vmcl.getId().equals("broker")) {
							vmcl.pm=vmcl.getVm().getResourceAllocation().getHost();
						}
						vmcl.getVm().switchoff(true);
						
					}
				} catch (StateChangeException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Getter method for the all of the applications.
	 */
	public static ArrayList<Application> getApplications() {
		return applications;
	}

	/**
	 * Getter method for the all of the stations.
	 */
	public ArrayList<Device> getStations() {
		return stations;
	}

	/**
	 * Getter method for the cloud resource of the application.
	 */
	public Cloud getCloud() {
		return cloud;
	}

	/**
	 * Getter method for the Id of the application.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter method for the instance of the application.
	 */
	public Instance getInstance() {
		return instance;
	}

	/**
	 * Getter method for the IoT providers of the application.
	 */
	public List<Provider> getProviders() {
		return providers;
	}

	/**
	 * Getter method for the all virtual machines of the application.
	 */
	public List<VmCollector> getVmlist() {
		return vmlist;
	}
	
	/**
	 * Getter method for the total work time of the application.
	 */
	public long getSumOfWorkTime() {
		return sumOfWorkTime;
	}
	
	/**
	 * Getter method for the total amount of processed data by the application.
	 */
	public long getSumOfProcessedData() {
		return sumOfProcessedData;
	}

	/**
	 * Getter method for stop time of the application.
	 */
	public long getStopTime() {
		return stopTime;
	}

	/**
	 * Getter method for the amount of arrived data from devices.
	 */
	public long getSumOfArrivedData() {
		return sumOfArrivedData;
	}
	
	/**
	 * Getter method for the data which simulate reservation.
	 */
	public StorageObject getFinalStorageObject() {
		return finalStorageObject;
	}
	
	/**
	 * Setter method for the arrived data.
	 */
	public void setSumOfArrivedData(long sumOfArrivedData) {
		this.sumOfArrivedData = sumOfArrivedData;
	}
	
	/**
	 * Setter method for the data which simulate reservation.
	 */
	public void setFinalStorageObject(StorageObject finalStorageObject) {
		this.finalStorageObject = finalStorageObject;
	}

}