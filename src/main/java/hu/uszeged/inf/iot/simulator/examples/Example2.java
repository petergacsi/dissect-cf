package hu.uszeged.inf.iot.simulator.examples;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.uszeged.inf.iot.simulator.entities.Station;
import hu.uszeged.inf.iot.simulator.entities.Device.DeviceNetwork;
import hu.uszeged.inf.iot.simulator.fog.Application;
import hu.uszeged.inf.iot.simulator.fog.Cloud;
import hu.uszeged.inf.iot.simulator.fog.Fog;
import hu.uszeged.inf.iot.simulator.providers.Instance;
import hu.uszeged.inf.iot.simulator.providers.Provider;
import hu.uszeged.inf.iot.simulator.util.TimelineGenerator;

public class Example2 {

public static void main(String[] args) throws Exception {
		
		String fogfile=ScenarioBase.resourcePath+"/resources_cscs/LPDSCloud.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"LPDSCloud.xml"; 
		// Set up the clouds
		new Cloud(cloudfile,"cloud1");
		Fog f = new Fog(fogfile,"fog1");
		VirtualAppliance va = new VirtualAppliance("va1", 100, 0, false, 1000000);
		AlterableResourceConstraints arc = new AlterableResourceConstraints(4,0.001,2147483648L);
		new Instance(va,arc,(1.0/60),"instance1");
		new Application(5*60*1000, 2500000, "cloud1", "instance1", "teszt", -1);
		DeviceNetwork dn  = new DeviceNetwork(10000, 10000, 10000, 10000, "tesztRepo", null, null);
		new Station(dn, 0,24*60*60*1000,50, "random", 5,60*1000,1).startMeter();
		
		// Start the simulation
		long starttime = System.nanoTime();
		Timed.simulateUntilLastEvent();
		long stopttime = System.nanoTime();
		// Print some informations to the monitor / in file
		ScenarioBase.printInformation((stopttime-starttime));
		//TimelineGenerator.generate();
	}

}
