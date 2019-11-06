package hu.u_szeged.inf.fog.simulator.demo;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;

/**
 * This class represents how to create a virtual machine and compute task on it
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
public class VmTask {

	public static void main(String[] args) throws SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException, NetworkException, VMManagementException {
		
		// this variable is only for the creation of the repositories, we do not deal with energy measurement currently 
		final EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions =
				PowerTransitionGenerator.generateTransitions(20, 296, 493, 50, 108);
		
		// a repository can't exist without latency, but now we won't use it
		HashMap<String, Integer> latencyMap = new HashMap<String, Integer>();
		
		// creating a repository with 16 GBs of storage
		Repository repo = new Repository(17179869184L, "repo", 1000000, 1000000, 1000000, latencyMap, 
				transitions.get(PowerTransitionGenerator.PowerStateKind.storage),transitions.get(PowerTransitionGenerator.PowerStateKind.network));
		
		repo.setState(NetworkNode.State.RUNNING);
		
		/* to create a VM, we need first a physical machine with
		   - 8 CPU cores,
		   - 1 core processing power
		   - 8 GB RAM
		   - 10-10 unit booting/shutdown time 
		*/
		PhysicalMachine pm = new PhysicalMachine(8, 1, 8589934592L,repo, 10, 10,transitions.get(PowerTransitionGenerator.PowerStateKind.host));
		
		// Start the PM
		pm.turnon();
		
		
		// we start the simulation until the last event
		Timed.simulateUntilLastEvent();
		
		// we can check that creation time from the VM image is 800/4=200 unit of time 
		System.out.println("Time: "+Timed.getFireCount()+ " PM state: "+pm.getState());

		// for a VM, first we need a virtual machine image, it needs 800 instruction for creating a VM from it, and it needs 1 GB of free space on a PM
		VirtualAppliance va = new VirtualAppliance("va", 800, 0, false, 1073741824L);
		
		// we have to register it to the PM's repository
		repo.registerObject(va);
		
		// we have to define the resource needs of the VM, now it is 4 CPU cores, 1 core processing power and 4 GB RAM
		AlterableResourceConstraints arc = new AlterableResourceConstraints(4,1,4294967296L);
		
		// Ask for a VM 
		VirtualMachine vm = pm.requestVM(va, arc, repo, 1)[0];
				
		// we continue the simulation until the last event
		Timed.simulateUntilLastEvent();
		
		// we can check the time when the VM is ready
		System.out.println("Time: "+Timed.getFireCount()+ " PM state: "+pm.getState()+ " VM state: "+vm.getState());
		
		// now we can simulate a compute task with 100000 instructions and 100% VM usage
		vm.newComputeTask(100000, ResourceConsumption.unlimitedProcessing, new ConsumptionEventAdapter() {
			
			// Once our VM completed its tasks 
			@Override
			public void conComplete() {
				System.out.println("Time: "+Timed.getFireCount());
			}
		});
		
		// we finished the simulation until the last event
		Timed.simulateUntilLastEvent();
	}
	
}
