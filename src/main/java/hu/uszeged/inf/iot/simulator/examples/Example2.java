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

public class Example2 {

public static void main(String[] args) throws Exception {
		
		String fogfile=ScenarioBase.resourcePath+"/resources_cscs/LPDSCloud.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"LPDSCloud.xml"; 
		// Set up the clouds
		Fog f1 = new Fog(fogfile,"fog1");
		Fog f2 = new Fog(fogfile,"fog2");
		Fog f3 = new Fog(fogfile,"fog3");
		Fog f4 = new Fog(fogfile,"fog4");
		Cloud c = new Cloud(cloudfile,"cloud1");
			
		VirtualAppliance va = new VirtualAppliance("va1", 100, 0, false, 1000000);
		AlterableResourceConstraints arc = new AlterableResourceConstraints(4,0.001,2147483648L);
		new Instance(va,arc,(1.0/60),"instance1");
		Application appCloud = new Application(5*60*1000, 250, "cloud1", "instance1", "AppCloud","cloud", -1);
		new Application(5*60*1000, 250, "fog1", "instance1", "AppFog1","fog", -1);
		new Application(5*60*1000, 250, "fog2", "instance1", "AppFog2","fog", -1);
		new Application(5*60*1000, 250, "fog3", "instance1", "AppFog3","fog", -1);
		new Application(5*60*1000, 250, "fog4", "instance1", "AppFog4","fog", -1);
		
		Fog.registerApplication(appCloud, f1, c);
		Fog.registerApplication(appCloud, f2, c);
		Fog.registerApplication(appCloud, f3, c);
		Fog.registerApplication(appCloud, f4, c);
		
		
		for(int i=0;i<15;i++) {
			DeviceNetwork dn  = new DeviceNetwork(10000, 10000, 10000, 10000, "tesztRepo"+i, null, null);
			new Station(dn, 0,24*60*60*1000,50, "random", 5,60*1000,1).startMeter();
		}

		// TODO: IoT pricing set-up
		
		// Start the simulation
		long starttime = System.nanoTime();
		Timed.simulateUntilLastEvent();
		long stopttime = System.nanoTime();
		// Print some informations to the monitor / in file
		ScenarioBase.printInformation((stopttime-starttime));
		//TimelineGenerator.generate();
	}

}
