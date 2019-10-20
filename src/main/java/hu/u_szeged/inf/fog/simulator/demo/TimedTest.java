package hu.u_szeged.inf.fog.simulator.demo;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;

public class TimedTest extends Timed{

	String id;

	TimedTest(String id, long freq){
		this.id=id;
		
		// the events should be registered
		subscribe(freq);
	}
	@Override
	public void tick(long fires) {
		
		if(Timed.getFireCount()>=500) {
			
			// we deleted all of the future events of this object
			unsubscribe();
		}
		
		System.out.println(this.id+": time: "+ Timed.getFireCount());
		
		
		
	}

	public static void main(String[] args) {
		
		// creating the recurrent events
		new TimedTest("te1", 100);
		
		new TimedTest("te2", 99);

		// we start the simulation until the last event
		Timed.simulateUntilLastEvent();
	}
}
