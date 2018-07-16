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
class Metering extends DeferredEvent {
	/**
	 * It identifies which sensor generated the data. Azonositja, melyik szenzor
	 * generalta az adatot.
	 */
	private int sensorID;

	/**
	 * It stores the size of the metered data. Tarolja a mert adat meretet.
	 */
	private int filesize;

	/**
	 * This repository will store the data. Ez a repository fogja tarolni az
	 * adatot.
	 */
	private Repository r;

	/**
	 * This station contains the repository. Ez a station tarolja a
	 * repository-t.
	 */
	private Station s;

	/**
	 * When the simulation may stop it depends on all data are arrived. This var
	 * can help to check it. A szimulacio befejezesekor ez a valtozo segit
	 * leellenorizni, hogy az osszes adat megerkezett-e.
	 */
	private int cloudnumber;

	/**
	 * The constructor create the deferred event. A konstruktor letrehozza a
	 * kesleltett esemenyt.
	 * 
	 * @param s
	 *            station, which contains the repository - amelyik station
	 *            tartalmazza a repository-t
	 * @param sensorID
	 *            sensor, which create the data - amelyik szenzor letrehozza az
	 *            adatot
	 * @param filesize
	 *            the size of the generated data - a generalt adat merete
	 * @param delay
	 *            delay time for the event - kesleltetett ido az esemenyhez
	 */
	Metering(Station s, int sensorID, int filesize, long delay,int cloudnumber) {
		super(delay);
		this.r = s.getRepo();
		this.sensorID = sensorID;
		this.filesize = filesize;
		this.s = s;
		this.cloudnumber = cloudnumber;
	}

	/**
	 * This method is called when the delayed time passed. It creates and
	 * registers the generated data. A metodus meghivodik a kesletett ido
	 * leteltevel. Letrehozza es elmenti az adatot a repository-ba.
	 */
	@Override
	protected void eventAction() {
		StorageObject so = new StorageObject(
				this.s.getName() + " " + this.filesize + " " + this.sensorID + " " + Timed.getFireCount(),
				this.filesize, false);
		if (this.r.registerObject(so)) {
			this.s.generatedfilesize += this.filesize;
			Station.allstationsize += this.filesize;
			Station.getStationvalue()[cloudnumber] += this.filesize;
			this.s.setMessagecount(this.s.getMessagecount() + 1);
		}
		;
	}
}
