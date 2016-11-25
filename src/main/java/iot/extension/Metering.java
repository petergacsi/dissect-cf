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
	private Repository r;
	private Station s;
	private int cloudnumber;
	
	public Metering(Station s, int i, int filesize,long delay,int cloudnumber) {
		super(delay);
		this.r = s.getRepo();
		this.i = i;
		this.filesize = filesize;
		this.s = s;
		this.cloudnumber = cloudnumber;
	}

	@Override
	/**
	 * A metodus meghivasakor letrejon a StorageObject, majd lementodik az adott Station tarolojaba
	 */
	protected void eventAction() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yy:MM:dd:HH:mm:ss:SS");
		StorageObject so = new StorageObject(this.s.getName() + " " + this.filesize + " " + this.i + " "
				+ Timed.getFireCount() + " " + sdf.format(cal.getTime()), this.filesize, false);
		if(this.r.registerObject(so)){
			this.s.generatedfilesize+=this.filesize;
			Station.allstationsize+=this.filesize;
			Scenario.stationvalue[cloudnumber]+=this.filesize;
		};
		
		/*for (Station s : Station.stations) {
			if (s.getName().equals(this.sName)) {
				if(s.getRepo().registerObject(so)){
					s.generatedfilesize+=this.filesize;
					Station.allstationsize+=this.filesize;
				};
			}
		}*/
	}
}
