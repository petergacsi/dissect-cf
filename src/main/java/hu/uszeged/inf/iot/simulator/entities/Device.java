package hu.uszeged.inf.iot.simulator.entities;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;

public abstract class Device extends Timed {
	
	protected static final int latency=11;
	protected static HashMap<String, Integer> lmap = new HashMap<String, Integer>();
	
	protected Application app;
	protected DeviceNetwork dn;
	protected long sumOfGeneratedData;
	protected int messageCount;
	protected Repository cloudRepository;
	protected long startTime;
	protected long stopTime;
	protected long filesize;
	

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


	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}

   public static class DeviceNetwork {
	  Repository localRepository;
	  String repoName;
		  
		public DeviceNetwork(long maxinbw, long maxoutbw, long diskbw, long repoSize,String repoName) {
			this.repoName=repoName;
			localRepository = new Repository(repoSize, repoName, maxinbw, maxoutbw, diskbw, lmap, defaultStorageTransitions, defaultNetworkTransitions);
			try {
				localRepository.setState(NetworkNode.State.RUNNING);
			} catch (NetworkException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}

	}
  
  //TODO: these burned values need to be deleted
	public static final double minpower = 20;
	public static final double idlepower = 200;
	public static final double maxpower = 300;
	public static final double diskDivider = 10;
	public static final double netDivider = 20;
	public final static Map<String, PowerState> defaultStorageTransitions;
	public final static Map<String, PowerState> defaultNetworkTransitions;
  
	static {
		try {
			EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = PowerTransitionGenerator
					.generateTransitions(minpower, idlepower, maxpower, diskDivider, netDivider);
			defaultStorageTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.storage);
			defaultNetworkTransitions = transitions.get(PowerTransitionGenerator.PowerStateKind.network);
		} catch (Exception e) {
			throw new IllegalStateException("Cannot initialize the default transitions");
		}
	}

}
