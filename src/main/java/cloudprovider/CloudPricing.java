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
	
	public void setIaaSService(IaaSService iaas){
		this.iaas=iaas;
		this.iaas.setCloudpricing(this);
	}
	
	public void setCostAnalyserandPricer(CostAnalyserandPricer cap){
		this.cap = cap;
	}

}
 