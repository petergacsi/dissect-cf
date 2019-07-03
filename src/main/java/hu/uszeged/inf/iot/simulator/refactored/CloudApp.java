package hu.uszeged.inf.iot.simulator.refactored;

import javax.xml.bind.JAXBException;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.uszeged.inf.iot.simulator.providers.Provider;
import hu.uszeged.inf.iot.simulator.refactored.Application;
import hu.uszeged.inf.iot.simulator.util.TimelineGenerator.TimelineCollector;
import hu.uszeged.inf.xml.model.ApplicationModel;

public class CloudApp extends Application{

	public CloudApp(long freq, long tasksize, String instance, String name, String type, double noi, ComputingAppliance computingAppliance) {
		super(freq, tasksize,  instance, name, type, noi, computingAppliance);
		// TODO Auto-generated constructor stub
	}

	


	@Override
	public void tick(long fires) {
		//System.out.println("CloudApp tick");
		
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
					
//					if (ratio > 2) {
//						this.handleDataTransderToNeighbourAppliance(unprocessedData);
//						
//					}
					
					System.out.print("data/VM: "+ratio+" unprocessed after exit: "+unprocessedData+ " decision:");
					this.generateAndAddVM();
					
					break;
				} 
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
						if(this.sumOfArrivedData<this.sumOfProcessedData) {
							System.out.println(Timed.getFireCount());System.exit(0);
						}
					} catch (NetworkException e) {
						e.printStackTrace();
					}
				
			}
			System.out.println(" load(%): "+this.getLoadOfCloud());
		}
		this.countVmRunningTime();
		this.turnoffVM();
		
		if (this.currentTask == 0 && this.incomingData == 0 && this.sumOfProcessedData==this.sumOfArrivedData) {
			unsubscribe();
			System.out.println(this.name + " leiratkozik " + this.sumOfArrivedData +" "+  this.sumOfProcessedData +" "+ unprocessedData);
			for(Provider p : this.providers) {
				if(p.isSubscribed()) {
					p.shouldStop=true;
				}
			}
			StorageObject so = new StorageObject(this.name, this.sumOfProcessedData, false);
			if(!this.computingDevice.iaas.repositories.get(0).registerObject(so)){
				this.computingDevice.iaas.repositories.get(0).deregisterObject(so);
				this.computingDevice.iaas.repositories.get(0).registerObject(so);
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
