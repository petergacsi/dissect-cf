package iot.extension;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import hu.mta.sztaki.lpds.cloud.simulator.io.*;
import hu.mta.sztaki.lpds.cloud.simulator.*;

/**
 * Az osztaly szimulalja az IoT rendszer szenzor entitasat.
 * Az osztaly idotol fuggo, de vissza nem tero esemenykent modellezi le egy meres folyamatat.
 */
public class Metering extends DeferredEvent {
	private int i;
	private String sName;
	private int filesize;

	public Metering(String sName, int i, int filesize,long delay) {
		super(delay);
		this.i = i;
		this.filesize = filesize;
		this.sName = sName;
	}

	@Override
	/**
	 * A metodus meghivasakor letrejon a StorageObject, majd lementodik az adott Station tarolojaba
	 */
	protected void eventAction() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yy:MM:dd:HH:mm:ss:SS");
		StorageObject so = new StorageObject(this.sName + " " + this.filesize + " " + this.i + " "
				+ Timed.getFireCount() + " " + sdf.format(cal.getTime()), this.filesize, false);
		for (Station s : Station.stations) {
			if (s.getName().equals(this.sName)) {
				if(s.getRepo().registerObject(so)){
					s.generatedfilesize+=this.filesize;
					Station.allstationsize+=this.filesize;
				};
			}
		}
	}
}
