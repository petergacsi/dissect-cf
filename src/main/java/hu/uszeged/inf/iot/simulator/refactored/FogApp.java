package hu.uszeged.inf.iot.simulator.refactored;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.uszeged.inf.iot.simulator.providers.Provider;
import hu.uszeged.inf.iot.simulator.util.TimelineGenerator.TimelineCollector;

public class FogApp extends Application {
	
	
	
	public List<Device> ownStations = new ArrayList<Device>(); 

	public FogApp(long freq, long tasksize,  String instance, String name, String type, double noi , ComputingAppliance computingAppliance) {
		super(freq, tasksize,  instance, name, type, noi, computingAppliance);
		
		
		//need to add fogApps to a list for installation strategy
		//Application.fogApplications.add(this);
		
	}

	
	
	public ComputingAppliance getParentDeviceOfApp() {
		return computingAppliance.parentApp.computingAppliance;
	}
	
	//add this app to a specific station => InstallationStrategy
	public void initiateDataTransferUp(long unprocessedData) throws NetworkException {
		
		
		this.computingAppliance.parentApp.incomingData++;
		this.sumOfArrivedData -= unprocessedData;
		if (this.computingAppliance.parentApp.isSubscribed()) {

			final long unprocessed = unprocessedData;
			NetworkNode.initTransfer(unprocessedData, ResourceConsumption.unlimitedProcessing,
					this.computingAppliance.iaas.repositories.get(0), this.getParentDeviceOfApp().iaas.repositories.get(0),
					new ConsumptionEvent() {

						@Override
						public void conComplete() {
							computingAppliance.parentApp.sumOfArrivedData += unprocessed;
							computingAppliance.parentApp.incomingData--;
							
						}

						@Override
						public void conCancelled(ResourceConsumption problematic) {
						}

					});
		} else {
			try {
				
				this.computingAppliance.parentApp.restartApplication();
				new BrokerCheck(this, this.computingAppliance.parentApp,unprocessedData, (this.computingAppliance.parentApp.freq/2));
			} catch (VMManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	private boolean checkStationState() { 
		for (Device s : this.ownStations) {
			if (s.isSubscribed()) {
				return false;
			}
		}
		return true;
	}
	
	
	

	@Override
	public void tick(long fires) {

		long unprocessedData = (this.sumOfArrivedData - this.sumOfProcessedData);
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
					double ratio = ((double)unprocessedData/this.tasksize);
										
					if (ratio > 8) {

						//Felfele vagy szomsz�dnak
						Random rng = new Random();
						int choice = rng.nextInt(2);

						if (choice == 1) {
							this.handleDataTransderToNeighbourAppliance(unprocessedData-processedData);
						} else {
							try {
								this.initiateDataTransferUp(unprocessedData-processedData);
							} catch (NetworkException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						
						//Csak felfele
//						try {
//							this.initiateDataTransferUp(unprocessedData-processedData);
//						} catch (NetworkException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
						
						
						//Csak szomsz�dnak
//					this.handleDataTransderToNeighbourAppliance(unprocessedData-processedData);
						

					}
					
					System.out.print("data/VM: " + ratio + " unprocessed after exit: " + unprocessedData + " decision:");
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
											System.out.println(name +" "+vml.id + " started@ " + vmStartTime + " finished@ "
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
				

		if ( (this.currentTask == 0 && this.incomingData == 0 &&
				this.sumOfProcessedData==this.sumOfArrivedData && this.checkStationState() )
				 ) {
//		
			
			System.out.println(this.name + " leiratkozik " + this.sumOfArrivedData +" "+  this.sumOfProcessedData +" "+ unprocessedData);
			unsubscribe();
			for(Provider p : this.providers) {
				if(p.isSubscribed()) {
					p.shouldStop=true;
				}
			}
			StorageObject so = new StorageObject(this.name, this.sumOfProcessedData, false);
			if(!this.computingAppliance.iaas.repositories.get(0).registerObject(so)){
				this.computingAppliance.iaas.repositories.get(0).deregisterObject(so);
				this.computingAppliance.iaas.repositories.get(0).registerObject(so);
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

	@Override
	public String toString() {
		
		return "fogApp=" + computingAppliance.name + " " + this.computingAppliance.x + " " +  this.computingAppliance.y +  " stations: " + this.ownStations.size();
	}
	
}
