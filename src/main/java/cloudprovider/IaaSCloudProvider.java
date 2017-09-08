package cloudprovider;

import java.lang.reflect.InvocationTargetException;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling.PhysicalMachineController;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.Scheduler;

public class IaaSCloudProvider extends IaaSService {

	
	public IaaSCloudProvider(Class<? extends Scheduler> s, Class<? extends PhysicalMachineController> c)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		super(s, c);
	}

	public interface QuoteProvider {
		double getPerTickQuote(ResourceConstraints rc);
	}
	
}
