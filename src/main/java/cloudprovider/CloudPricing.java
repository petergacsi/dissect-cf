package cloudprovider;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;

public abstract class CloudPricing {
	protected IaaSService iaas;
	protected CostAnalyserandPricer cap;
	/**
	 * 
	 * @param rc
	 *            if null then the default instance price should be returned
	 * @return
	 */
	public abstract double getPerTickQuote(ResourceConstraints rc);
	
	public void setIaaSService(IaaSService iaas,CostAnalyserandPricer cap){
		this.iaas=iaas;
		this.cap = cap;
	}

}
 