/*
 *  ========================================================================
 *  DIScrete event baSed Energy Consumption simulaTor 
 *    					             for Clouds and Federations (DISSECT-CF)
 *  ========================================================================
 *  
 *  This file is part of DISSECT-CF.
 *  
 *  DISSECT-CF is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at
 *  your option) any later version.
 *  
 *  DISSECT-CF is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *  General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with DISSECT-CF.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  (C) Copyright 2019, Andras Markus (markusa@inf.u-szeged.hu)
 */

package hu.uszeged.inf.iot.simulator.entities;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.util.SeedSyncer;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;

/**
 * The Station realization of the Device class represents a simple, one-way entity which operates only sensors.
 * The goal of the class is managing of the data generation and data sending.
 * @author Andras Markus (markusa@inf.u-szeged.hu)
 */
public class Station extends Device{
	
	/**
	 * Number of the sensors. A sensor can represent for example a wind speed meter or rain sensor.
	 */
	private int numOfSensors;
	
	/**
	 * The frequency of the data measurement and sending process (e.g. in millisecond).
	 */
	private long freq;
	
	/**
	 * The chosen strategy of the station. The application choosing is based on the strategy. 
	 */
	private String strategy;
	
	/**
	 * Getter for the number of the sensors.
	 */
	public int getNumOfSensors() {
		return numOfSensors;
	}

	/**
	 * The constructor to create new static, one-way entity for data generation.
	 * @param dn The network settings including the local repository.
	 * @param startTime The simulated time when the data generation starts.
	 * @param stopTime The simulated time when the data generation stops (e.g. in ms).
	 * @param filesize The size of the generated data.
	 * @param strategy The application choosing strategy.
	 * @param numOfSensors The number of sensors.
	 * @param freq The frequency of the data generation and sanding.
	 * @param delay if true, gives randomness to the simulation (0-20 ms). 
	 */
	public Station(DeviceNetwork dn, long startTime,long stopTime,long filesize, String strategy,int numOfSensors,
			long freq,boolean delay)  {
		long late;
		if(delay) {
			late = Math.abs(SeedSyncer.centralRnd.nextLong()%20)*60*1000;
		}else {
			late = 0;
		}
		this.startTime=startTime+late;
		this.stopTime=stopTime+late;
		this.filesize=filesize*numOfSensors;
		this.strategy=strategy;
		this.dn=dn;
		this.numOfSensors=numOfSensors;
		this.freq=freq;
		this.setSumOfGeneratedData(0);				
		installationProcess(this);
		this.startMeter();
		this.setMessageCount(0);
	}
	
	/**
	 * This method sends all of the generated data (called StorageObject) to the cloud repository.
	 */
	private void startCommunicate() {
		for (StorageObject so : this.dn.getLocalRepository().contents()) {
			StorObjEvent soe = new StorObjEvent(so);
			//this.dn.localRepository.requestContentDelivery(so.id, this.cloudRepository, soe);
			try {
				NetworkNode.initTransfer(so.size, ResourceConsumption.unlimitedProcessing, this.dn.getLocalRepository(), this.cloudRepository, soe);
			} catch (NetworkException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * This method starts the station if it doesn't work yet.
	 */
	public void startMeter() {
		if (this.isSubscribed()==false) {
			new DeferredEvent(this.startTime) {
				
				@Override
				protected void eventAction() {
					subscribe(freq);
					cloudRepository = app.getCloud().getIaas().repositories.get(0);
				}
			};
		}
	}

	/**
	 * It stops the station.
	 */
	private void stopMeter() {
		unsubscribe();		
	}

	/**
	 * This method is called when time elapsed defined in the freq variable.
	 * The task of the method is control data generating and sending.
	 */
	@Override
	public void tick(long fires) {
		if (Timed.getFireCount() < (stopTime ) && Timed.getFireCount() >= (startTime)) {
			new Sensor(this, 1);
		}
		if (this.dn.getLocalRepository().getFreeStorageCapacity() == this.dn.getLocalRepository().getMaxStorageCapacity() && Timed.getFireCount() > stopTime ) {
			this.stopMeter();
		}

		if (this.cloudRepository.getCurrState().equals(Repository.State.RUNNING)) {
			this.startCommunicate();
		}
		if(!this.app.isSubscribed()) {
			this.app.restartApplication();
		}
	}

	/**
	 * The installation process depend on the value defined in the strategy variable.
	 * Based on the chosen strategy different application would processes the generated data.
	 * @param s The Station which goes through the installation.
	 */
	private void installationProcess(final Station s) {
		if(this.strategy.equals("load")){		
			new RuntimeStrategy(this);
		}else if(this.strategy.equals("random")){
			new RandomStrategy(this);
		}else if(this.strategy.equals("cost")) {
			new CostStrategy(this);
		}else if(this.strategy.equals("fuzzy")){
			new FuzzyStrategy(this);	
		} 				
	}

	/**
	 * This method performs the shutdown-restart process. 
	 */
	@Override
	public void shutdownProcess() {	
		// not implemented yet.
	}
	
	/**
	 * Private class to represent the successful or unsuccessful data sending.
	 */
	private class StorObjEvent implements ConsumptionEvent {
		
		/**
		 * 	File is under sending.
		 */
		private StorageObject so;
		
		/**
		 * Constructor for new event for data sending.
		 * @param so File is under sending.
		 */
		private StorObjEvent(StorageObject so) {
			this.so = so;

		}
		/**
		 * If the sending is successful this method will be called.
		 */
		@Override
		public void conComplete() {
			dn.getLocalRepository().deregisterObject(this.so);
			app.setSumOfArrivedData((app.getSumOfArrivedData() + this.so.size));
			StorageObject newObject = new StorageObject(app.getFinalStorageObject().id,(app.getFinalStorageObject().size+this.so.size),false);
			cloudRepository.deregisterObject(app.getFinalStorageObject().id);
			app.setFinalStorageObject((newObject));
			cloudRepository.registerObject(app.getFinalStorageObject());
		}

		/**
		 * If the sending is unsuccessful this method will be called.
		 */
		@Override
		public void conCancelled(ResourceConsumption problematic) {
			System.exit(0);
			System.err.println("error in file Station.");
			//TODO: this should never happen. Fixing later.
		}
	}
}