package hu.uszeged.inf.iot.simulator.entities;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;

public abstract class Device extends Timed {

	public abstract void installionProcess();
	
	public abstract void shutdownProcess();
	
	public static void loadDevice(String stationfile) throws Exception {
		// need to override
	};

}
