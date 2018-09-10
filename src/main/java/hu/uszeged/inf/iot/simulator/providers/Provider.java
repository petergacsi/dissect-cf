package hu.uszeged.inf.iot.simulator.providers;

import javax.xml.bind.JAXBException;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.uszeged.inf.xml.model.InstanceModel;

public class Provider {
	public static void loadProvider(String providerfile) throws JAXBException {
		for(ProviderModel im : ProviderModel.loadInstanceXML(providerfile)) {

		}
	}
}
