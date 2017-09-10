package cloudprovider;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling.PhysicalMachineController;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.Scheduler;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.notifications.SingleNotificationHandler;
import hu.mta.sztaki.lpds.cloud.simulator.notifications.StateDependentEventHandler;

public class IaaSCloudProvider extends IaaSService implements ForwardingRecorder {

	
	public IaaSCloudProvider(Class<? extends Scheduler> s, Class<? extends PhysicalMachineController> c)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		super(s, c);
	}

	public interface QuoteProvider {
		double getPerTickQuote(ResourceConstraints rc);
	}
	
	public interface VMListener {
		void newVMadded(VirtualMachine[] vms);
	}

	@Override
	public VirtualMachine[] requestVM(VirtualAppliance va, ResourceConstraints rc, Repository vaSource, int count)
			throws hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException, NetworkException {
		reqVMcalled = true;
		return notifyVMListeners(super.requestVM(va, rc, vaSource, count));
	}

	@Override
	public VirtualMachine[] requestVM(VirtualAppliance va, ResourceConstraints rc, Repository vaSource, int count,
			HashMap<String, Object> schedulingConstraints)
					throws hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException, NetworkException {
		reqVMcalled = true;
		return notifyVMListeners(super.requestVM(va, rc, vaSource, count, schedulingConstraints));
	}

	private VirtualMachine[] notifyVMListeners(VirtualMachine[] received) {
		notifyMe.notifyListeners(received);
		return received;
	}
	
	private boolean reqVMcalled = false;
	
	private final StateDependentEventHandler<VMListener, VirtualMachine[]> notifyMe = new StateDependentEventHandler<VMListener, VirtualMachine[]>(
			new SingleNotificationHandler<VMListener, VirtualMachine[]>() {
				@Override
				public void sendNotification(VMListener onObject, VirtualMachine[] payload) {
					onObject.newVMadded(payload);
				}
			});
	private QuoteProvider qp = new QuoteProvider() {
		@Override
		public double getPerTickQuote(ResourceConstraints rc) {
			return 1;
		}
	};

	public void setVMListener(VMListener newListener) {
		notifyMe.subscribeToEvents(newListener);
	}

	public void setQuoteProvider(QuoteProvider qp) {
		this.qp = qp;
	}

	public double getResourceQuote(ResourceConstraints rc) {
		return qp.getPerTickQuote(rc);
	}

	public void resetForwardingData() {
		reqVMcalled = false;
	}

	public boolean isReqVMcalled() {
		return reqVMcalled;
	}

	
}
