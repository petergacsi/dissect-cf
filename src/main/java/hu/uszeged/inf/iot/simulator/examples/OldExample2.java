package hu.uszeged.inf.iot.simulator.examples;

import java.util.ArrayList;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.uszeged.inf.iot.simulator.entities.Device.DeviceNetwork;
import hu.uszeged.inf.iot.simulator.entities.Station;
import hu.uszeged.inf.iot.simulator.providers.AmazonProvider;
import hu.uszeged.inf.iot.simulator.providers.AzureProvider;
import hu.uszeged.inf.iot.simulator.providers.BluemixProvider;
import hu.uszeged.inf.iot.simulator.providers.BluemixProvider.Bluemix;
import hu.uszeged.inf.iot.simulator.system.Application;
import hu.uszeged.inf.iot.simulator.system.Cloud;
import hu.uszeged.inf.iot.simulator.providers.Instance;
import hu.uszeged.inf.iot.simulator.providers.OracleProvider;

public class OldExample2 {
	
	public static void main(String[] args) throws Exception {
	String cloudfile = ScenarioBase.resourcePath+"LPDS-1.xml";

	// Setting up cloud, application, instance and station
	new Cloud(cloudfile,"cloud1");
		
	VirtualAppliance va = new VirtualAppliance("va1", 100, 0, false, 10000000000L);
	AlterableResourceConstraints arc = new AlterableResourceConstraints(4,0.001,2147483648L);
	new Instance(va,arc,0.0000000105,"instance1");
	
	Application app1 = new Application(5*60*1000, 250000, "cloud1", "instance1", "tesz-app", -1);
	Application app2 = new Application(5*60*1000, 250000, "cloud1", "instance1", "tesz-app2", -1);
	
	for(int i=0;i<15;i++) {
		DeviceNetwork dn  = new DeviceNetwork(10000, 10000, 10000, 10000, "tesztRepo"+i, null, null);
		new Station(dn, 0,24*60*60*1000,50, "random", 5,60*1000,true).startMeter();
	}

	// Setting up IoT pricing
	ArrayList<Bluemix> bmList = new ArrayList<Bluemix>();
	bmList.add(new Bluemix(0,499999,0.00097));
	bmList.add(new Bluemix(450000,6999999,0.00068));
	bmList.add(new Bluemix(7000000,Long.MAX_VALUE,0.00014));
	
	new BluemixProvider(bmList,app1);
	
	new AmazonProvider(5,1000000,512,app1);
	
	new AzureProvider(86400000,421.65,6000000,4,app2);
	
	new OracleProvider(2678400000L,0.93,15000,0.02344,1000,app2);
	
	// Start the simulation
	long starttime = System.nanoTime();
	Timed.simulateUntilLastEvent();
	long stopttime = System.nanoTime();
	// Print some informations to the monitor / in file
	ScenarioBase.printInformation((stopttime-starttime));

	}
}
