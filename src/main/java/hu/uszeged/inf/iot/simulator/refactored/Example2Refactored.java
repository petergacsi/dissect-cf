package hu.uszeged.inf.iot.simulator.refactored;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.uszeged.inf.iot.simulator.providers.Instance;
import hu.uszeged.inf.iot.simulator.refactored.Device.DeviceNetwork;

public class Example2Refactored {

public static void main(String[] args) throws Exception {
		
		String fogfile=ScenarioBase.resourcePath+"/resources_cscs/LPDSCloud.xml"; 
		String cloudfile=ScenarioBase.resourcePath+"LPDSCloud.xml"; 
		// Set up the clouds
		FogAppliance f1 = new FogAppliance(fogfile,"fog1", 8, 6);
		FogAppliance f2 = new FogAppliance(fogfile,"fog2", 13 ,8);
		
		FogAppliance f3 = new FogAppliance(fogfile,"fog3", 7, 8);
		FogAppliance f4 = new FogAppliance(fogfile,"fog4", 9, 9);
		ComputingAppliance c = new CloudAppliance(cloudfile,"cloud1", 11 , 11);
		
		VirtualAppliance va = new VirtualAppliance("va1", 100, 0, false, 1000000);
		AlterableResourceConstraints arc = new AlterableResourceConstraints(4,0.001,2147483648L);
		new Instance(va,arc,(1.0/60),"instance1");
		
		Application appCloud = new CloudApp(5*60*1000, 250, "cloud1", "instance1", "AppCloud","cloud", -1 ,c);
		
		
		Application appFog1 = new FogApp(5*60*1000, 150, "fog1", "instance1", "AppFog1","fog", -1, f1);
		Application appFog2 = new FogApp(5*60*1000, 350, "fog2", "instance1", "AppFog2","fog", -1, f2);
		Application appFog3 = new FogApp(5*60*1000, 250, "fog3", "instance1", "AppFog3","fog", -1, f3);
		Application appFog4 = new FogApp(5*60*1000, 200, "fog4", "instance1", "AppFog4","fog", -1, f4);
		
		
		//create relation between apps and childDevices
		appCloud.makeRelationBetweenDevices(Arrays.asList(f1,f2,f3,f4));
		
		
		//Example1 = "2 appCloud with +1 Fog
		/*CloudApp appCloud1 = new CloudApp(5*60*1000, 250, "cloud1", "instance1", "AppCloud1","cloud", -1 ,c);
		FogDevice f5 = new FogDevice(fogfile, "fog5");
		Application appFog5 = new FogApp(5*60*1000, 250, "fog5", "instance1", "AppFog5","fog", -1, f5);
		appCloud1.makeRelationBetweenDevices(Arrays.asList(f5));
		*/
		
		//Example2 = two levels of Fog Devices
		/*FogDevice f5 = new FogDevice(fogfile, "fog5");
		Application appFog5 = new FogApp(5*60*1000, 250, "fog5", "instance1", "AppFog5","fog", -1, f5);
		FogDevice f6 = new FogDevice(fogfile, "fog6");
		Application appFog6 = new FogApp(5*60*1000, 250, "fog6", "instance1", "AppFog6","fog", -1, f6);
		
		appFog3.makeRelationBetweenDevices(Arrays.asList(f5, f6));
		*/
		
		//example datas for distanceStrategy and DistancePerInBandWidthStrategy
		//the latter not fully implemented yet
		double[][] exampleDatas = {
								{5,9},
								{8.12,9.28},
								{8.82,10.1},
								{14.36,6.72},
								{12.44,6.94},
								{13.62,7.6},
								{15.06,8.16},
								{11.96,5.26},
								{14.36,5.62},
								{14.44,8.7},
								{6.22,8.38},
								{6.74,6.02},
								{5.48,7.56},
								{7.42,6.92},
								{7.36, 4.74},
									};
		
		for(int i=0;i<15;i++) {
			DeviceNetwork dn  = new DeviceNetwork(10000, 10000, 10000, 10000, "tesztRepo"+i, null, null);
			new Station(dn, 0, 24*60*60*1000, 50, "distance", 5 , 60*1000, 1, exampleDatas[i][0], exampleDatas[i][1]).startMeter();
		}
		
		
		
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
			for (Device d : fogApp.ownStations) {
				System.out.print(d);
			}
			System.out.println("");
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
