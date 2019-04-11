package hu.uszeged.inf.iot.simulator.refactored;

import java.util.Arrays;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.uszeged.inf.iot.simulator.refactored.Station;
import hu.uszeged.inf.iot.simulator.refactored.Device.DeviceNetwork;
import hu.uszeged.inf.iot.simulator.refactored.ScenarioBase;
import hu.uszeged.inf.iot.simulator.refactored.Application;
import hu.uszeged.inf.iot.simulator.refactored.CloudDevice;
import hu.uszeged.inf.iot.simulator.refactored.FogDevice;
import hu.uszeged.inf.iot.simulator.providers.Instance;

public class ExampleWithGWs {

public static void main(String[] args) throws Exception {
		
		String fogfile=ScenarioBase.resourcePath+"/resources_cscs/LPDSCloud.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"LPDSCloud.xml"; 
		
		//Root, Level 0 Cloud
		ComputingAppliance c = new CloudDevice(cloudfile,"cloud1");
		
		//Level 1 FogDevices
		FogDevice f1 = new FogDevice(fogfile,"fog1");
		FogDevice f2 = new FogDevice(fogfile,"fog2");
		
		//Level 2 Leaf devices, Gateways
		FogDevice f1_1 = new FogDevice(fogfile,"fog1_1");
		FogDevice f1_2 = new FogDevice(fogfile,"fog1_2");
		
		FogDevice f2_1 = new FogDevice(fogfile, "fog2_1");
		
		
		
		VirtualAppliance va = new VirtualAppliance("va1", 100, 0, false, 1000000);
		AlterableResourceConstraints arc = new AlterableResourceConstraints(4,0.001,2147483648L);
		new Instance(va,arc,(1.0/60),"instance1");
		
		//CloudApp for c(CloudDevice)
		Application appCloud = new CloudApp(5*60*1000, 250, "cloud1", "instance1", "AppCloud","cloud", -1 ,c);
		
		//FogApp for f1 and f2, (Normal FogDevices)
		Application appFog1 = new FogApp(5*60*1000, 250, "fog1", "instance1", "AppFog1","fog", -1, f1);
		Application appFog2 = new FogApp(5*60*1000, 250, "fog2", "instance1", "AppFog2","fog", -1, f2);
		
		
		Application appFog1_1 = new GateWayApp(5*60*1000, 250, "fog1_1", "instance1", "AppFog1_1","fog", -1, f1_1);
		Application appFog1_2 = new GateWayApp(5*60*1000, 250, "fog1_2", "instance1", "AppFog1_2","fog", -1, f1_2);
		Application appFog2_1 = new GateWayApp(5*60*1000, 250, "fog2_1", "instance1", "AppFog2_1","fog", -1, f2_1);
		
		//create relation between apps and childDevices
		appCloud.makeRelationBetweenDevices(Arrays.asList(f1,f2));
		appFog1.makeRelationBetweenDevices(Arrays.asList(f1_1, f1_2));
		appFog2.makeRelationBetweenDevices(Arrays.asList(f2_1));
		
		for(int i=0;i<15;i++) {
			DeviceNetwork dn  = new DeviceNetwork(10000, 10000, 10000, 10000, "tesztRepo"+i, null, null);
			new Station(dn, 0, 24*60*60*1000, 50, "random", 5 , 60*1000, 1).startMeter();
		}
		
		
		/*Group g = new Group(Arrays.asList(f1, f2));
		System.out.println(g.toString());*/
		
		System.out.println();
		//Print out the topology
			//Devices and their apps
		for (Application app : c.applications)
			for (ComputingAppliance computingAppliance : app.childComputingDevice) {
				System.out.println(computingAppliance);
			}
		
			//Stations and their parent FogApps
		for (FogApp fogApp : Application.fogApplications) {
			System.out.println(fogApp);
		}
		System.out.println();
		
		
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

