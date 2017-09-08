package cloudprovider;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;

public interface CloudProviderInterface extends IaaSCloudProvider.QuoteProvider{
	void setIaaSService(IaaSService iaas);
	public AlterableResourceConstraints getArc();
}
