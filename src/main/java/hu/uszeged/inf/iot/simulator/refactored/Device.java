package hu.uszeged.inf.iot.simulator.refactored;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;
import hu.uszeged.inf.iot.simulator.refactored.Application;

public abstract class Device extends Timed {
	
	protected static final int latency=11;
	protected static HashMap<String, Integer> lmap = new HashMap<String, Integer>();
	
	public Application app;
	protected DeviceNetwork dn;
	public long sumOfGeneratedData;
	protected int messageCount;
	protected Repository cloudRepository;
	protected long startTime;
	public long stopTime;
	protected long filesize;
	
	
	public double x;
	public double y;
	

	public long getStopTime() {
		return stopTime;
	}

	public long getFilesize() {
		return filesize;
	}

	public abstract void shutdownProcess();
	
	public static void loadDevice(String stationfile) throws Exception {
		// need to override
	}

	public DeviceNetwork getDn() {
		return dn;
	}
	
	public void setApp(Application a) {
		this.app = a;
	}


	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}
	
	
	public double calculateDistance(Application app) {
		double result = Math.sqrt(
				Math.pow((this.x - app.computingDevice.x),2) + 
				Math.pow((this.y - app.computingDevice.y),2)
				);
		return result;
	}
	
	
	
   public static class DeviceNetwork {
	  Repository localRepository;
	  String repoName;
		  
		public DeviceNetwork(long maxinbw, long maxoutbw, long diskbw, long repoSize,String repoName,Map<String, PowerState> storageTransitions,Map<String, PowerState> networkTransitions) {
			this.repoName=repoName;
			if(storageTransitions==null) {
				storageTransitions=defaultStorageTransitions();
			}
			if(networkTransitions==null) {
				networkTransitions=defaultNetworkTransitions();
			}
			localRepository = new Repository(repoSize, repoName, maxinbw, maxoutbw, diskbw, lmap, storageTransitions, networkTransitions);
			try {
				localRepository.setState(NetworkNode.State.RUNNING);
			} catch (NetworkException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}

	}
  
	private static Map<String, PowerState> defaultStorageTransitions(){
		double minpower = 20;
		double idlepower = 200;
		double maxpower = 300;
		double diskDivider = 10;
		double netDivider = 20;
		EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = null;
		try {
			transitions = PowerTransitionGenerator
					.generateTransitions(minpower, idlepower, maxpower, diskDivider, netDivider);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return transitions.get(PowerTransitionGenerator.PowerStateKind.storage);
	}
	
	private static Map<String, PowerState> defaultNetworkTransitions(){
		double minpower = 20;
		double idlepower = 200;
		double maxpower = 300;
		double diskDivider = 10;
		double netDivider = 20;
		EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = null;
		try {
			transitions = PowerTransitionGenerator
					.generateTransitions(minpower, idlepower, maxpower, diskDivider, netDivider);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return transitions.get(PowerTransitionGenerator.PowerStateKind.network);
	}
	

}
