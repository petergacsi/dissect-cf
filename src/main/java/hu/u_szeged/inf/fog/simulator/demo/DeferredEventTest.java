package hu.u_szeged.inf.fog.simulator.demo;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;

/**
 * This class represents one of the basic components of the simulator ensuring delayed events
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
public class DeferredEventTest extends DeferredEvent{

	
	public DeferredEventTest(long delay) {
		super(delay);
	}

	@Override
	protected void eventAction() {
		new TimedTest("tt2", 25);
	}
	
public static void main(String[] args) {
		
		// creating the recurrent events and a single, delayed event
		new TimedTest("tt1", 100);
		new DeferredEventTest(300);
		
		// we start the simulation until the last event
		Timed.simulateUntilLastEvent();
	}
}
