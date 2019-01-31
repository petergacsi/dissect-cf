package hu.uszeged.inf.iot.simulator.entities;

import hu.mta.sztaki.lpds.cloud.simulator.io.*;
import hu.mta.sztaki.lpds.cloud.simulator.*;

/**
 * This class simulates one of the IoT world's entity which is the sensor.
 * Behavior of the class depends on time but it it models metering like a non
 * recurring event. Az osztaly szimulalja az IoT rendszer szenzor entitasat. Az
 * osztaly idotol fuggo, de vissza nem tero esemenykent modellezi le egy meres
 * folyamatat.
 */
class Sensor extends DeferredEvent {
	/**
	 * It identifies which sensor generated the data. Azonositja, melyik szenzor
	 * generalta az adatot.
	 */
	private int sensorID;

	/**
	 * It stores the size of the metered data. Tarolja a mert adat meretet.
	 */
	private long filesize;


	private Station s;

	

	Sensor(Station s, int sensorID, long filesize, long delay) {
		super(delay);
		this.sensorID = sensorID;
		this.filesize = filesize;
		this.s = s;

	}

	/**
	 * This method is called when the delayed time passed. It creates and
	 * registers the generated data. A metodus meghivodik a kesletett ido
	 * leteltevel. Letrehozza es elmenti az adatot a repository-ba.
	 */
	@Override
	protected void eventAction() {
		StorageObject so = new StorageObject(
				this.s.sd.name + " " + this.filesize + " " + this.sensorID + " " + Timed.getFireCount(),
				this.filesize, false);
		//System.out.println(this.s.localRepository.getFreeStorageCapacity());
		if (this.s.localRepository.registerObject(so)) {
			this.s.generatedfilesize += this.filesize;
			//System.out.println("hi fucker");
			Station.allstationsize += this.filesize;
			this.s.app.stationgenerated+=this.filesize;
			this.s.messageCount++;
		}
		;
	}
}
