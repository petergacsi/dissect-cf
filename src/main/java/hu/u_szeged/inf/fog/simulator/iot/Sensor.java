package hu.u_szeged.inf.fog.simulator.iot;

import hu.mta.sztaki.lpds.cloud.simulator.io.*;
import hu.mta.sztaki.lpds.cloud.simulator.*;

class Sensor extends DeferredEvent {

	private Station s;

	Sensor(Station s, long delay) {
		super(delay);
		this.s = s;
	}

	@Override
	protected void eventAction() {
		StorageObject so = new StorageObject(this.s.getDn().repoName + " " + this.s.filesize + " " + this.s.sensorNum + " " + Timed.getFireCount(),
				this.s.filesize, false);

		if (this.s.dn.localRepository.registerObject(so)) {
			this.s.sumOfGeneratedData += this.s.filesize;
			this.s.messageCount+=1;
		}else {
			try {
				throw new Exception("Saving data into the local repository is unsuccessful!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
