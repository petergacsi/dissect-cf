package hu.uszeged.inf.iot.simulator.refactored;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;

public class BrokerCheck extends DeferredEvent {

	private Application toApp;
	private Application fromApp;
	private long unprocessedData;
	private ComputingAppliance ca;
	long delay;
	
	public BrokerCheck(Application fromApp, Application toApp,  long unprocessedData, long delay) {
		super(delay);
		this.delay=delay;
		this.fromApp = fromApp;
		this.toApp = toApp;
		this.unprocessedData = unprocessedData;
	}

	@Override
	protected void eventAction() {
		if (toApp.broker.vm.getState().equals(VirtualMachine.State.RUNNING)) {
			final long unprocessed = unprocessedData;
			try {
				
				NetworkNode.initTransfer(unprocessedData, ResourceConsumption.unlimitedProcessing,
						fromApp.computingDevice.iaas.repositories.get(0), toApp.computingDevice.iaas.repositories.get(0), new ConsumptionEvent() {

							@Override
							public void conComplete() {
								toApp.sumOfArrivedData += unprocessed;
								toApp.incomingData--;
							}

							@Override
							public void conCancelled(ResourceConsumption problematic) {
							}

						});
			} catch (NetworkException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			//this.cancel();
		}else {
			new BrokerCheck(this.fromApp,this.toApp,this.unprocessedData, this.delay/2);
		}

	}

}
