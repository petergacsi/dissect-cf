package iot.extension;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;
import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import iot.extension.Application.VmCollector;
import providers.Provider;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;

/**
 * Ez az osztaly dolgozza fel a Station-ok altal generalt adatokat ComputeTask-okban.
 * Elinditja a Station-öket, illetve kezeli a virtualis gepek inditasat es leallitasat is.
 */
public class Application extends Timed {

	/**
	 * Osztalyba foglalja az adott virtualis gepet, a VM altal elvegzett osszes feladat 
	 * es az adott VM allapotat (dolgozik-e eppen feladaton,vagy sem).
	 */
	public static class VmCollector {
		@Override
		public String toString() {
			return "VmCollector [vm=" + vm + ", isworking=" + isworking + ", tasknumber=" + tasknumber + ", worked="
					+ worked + ", pm=" + pm + "]";
		}

		 VirtualMachine vm;
		 boolean isworking;
		 int tasknumber;
		 boolean worked;
		 PhysicalMachine pm;

		VmCollector(VirtualMachine vm, boolean isworking) {
			this.vm = vm;
			this.isworking = isworking;
			this.tasknumber = 0;
			this.worked = false;
		}
	}
	
	private  int i = 0;
	public ArrayList<VmCollector> vmlist;
	private int print;
	private boolean delay;
	private  long allgenerateddatasize = 0;
	private  long localfilesize = 0;
	private  long temp;
	public  TreeMap<Long, Integer> tmap = new TreeMap<Long, Integer>();
	private static int feladatszam = 0;
	private long tasksize;
	private Cloud cloud;
	public ArrayList<Station> stations;
	private String name;
	
	
	public String getName() {
		return name;
	}

	/**
	 * Privat konstruktor, hogy csak osztalyon belulrol lehessen hivni
	 */
	public Application(final long freq,long tasksize, boolean delay, int print,Cloud cloud,ArrayList<Station> stations,String name) {
		subscribe(freq);
		this.print = print;
		this.vmlist = new ArrayList<VmCollector>();
		this.delay = delay;
		this.tasksize=tasksize;
		this.cloud = cloud;
		this.stations = stations;
		this.name = name;
	}

	/**
	 * megkeresi az elso szabad virtualis gepet, amelyik epp nem dolgozik
	 */
	private VmCollector VmSearch() {
		VmCollector vmc = null;
		for (int i = 0; i < this.vmlist.size(); i++) {
			if (this.vmlist.get(i).isworking == false
					&& this.vmlist.get(i).vm.getState().equals(VirtualMachine.State.RUNNING)) {
				vmc = this.vmlist.get(i);
				return vmc;
			}
		}
		return vmc;
	}

	/**
	 * A metodus elinditja az osszes Station mukodeset
	 */
	private void startStation() {
		if(Scenario.scenscan==0){
			System.out.println("Scenario started at: " + Timed.getFireCount());
			Scenario.scenscan++;
		}
		if (this.i == 0) {
			this.i++;
			for (final Station s : this.stations) {
				Random randomGenerator = new Random();
				int randomInt = randomGenerator.nextInt(21);
				if (delay) {
					new DeferredEvent((long) randomInt * 60 * 1000) {

						@Override
						protected void eventAction() {
							s.startMeter(s.sd.freq);
						}
					};
				} else {
					s.startMeter(s.sd.freq);
				}
			}
		}
	}

	/**
	 * A metodus megkeresi es ujrainditja az elso SHUTDOWN allapotban levo virtualis gepet.
	 */
	private boolean turnonVM(){
		for (int i = 0; i < this.vmlist.size(); i++) {
			if ((this.vmlist.get(i).vm.getState().equals(VirtualMachine.State.SHUTDOWN) /*|| TODO: Destroyed allapot jelentese?!
					Application.vmlist.get(i).vm.getState().equals(VirtualMachine.State.DESTROYED)*/) 
					&& this.vmlist.get(i).pm!=null) {
				try {
					this.vmlist.get(i).vm.switchOn(
							this.vmlist.get(i).pm.allocateResources( this.cloud.getArc(), false, PhysicalMachine.defaultAllocLen),
							 this.cloud.getIaas().repositories.get(0));
				} catch (Exception e) {
					e.printStackTrace();
				} 
				return true;
			}
		}
		return false;
	}
	/**
	 * A metodus - ha nincs olyan  VM amit ujra lehet inditani - letrehoz egyet.
	 */
	private void generateAndAddVM() {
		try {
			if(this.turnonVM()==false){
				this.vmlist.add(new VmCollector(
						 this.cloud.getIaas().requestVM( this.cloud.getVa(),  this.cloud.getArc(),  this.cloud.getIaas().repositories.get(0), 1)[0], false));	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * A metodus megvizsgalja, hogy van-e olyan Station, amelyik meg uzemel.
	 */
	private boolean checkStationState() { // TODO probably wrong, but lets see
		boolean i = true;
		for (Station s : this.stations) {
			if (s.isSubscribed()) {
				return false;
			}
		}
		return i;
	}

	/**
	 *  Megkeresi az osszes futo, de eppen nem dolgozo VM-et es 1 kivetelevel leallitja mind.
	 */
	private void turnoffVM(){
		int reqshutdown = 0;
		for (VmCollector vmcl : this.vmlist) {
			if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING) && vmcl.isworking == false) {
				reqshutdown++;
			}
		}
		int stopped_vm=0;
		for (VmCollector vmcl : this.vmlist) {
			if (reqshutdown > 1) {
				if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING) && vmcl.isworking == false) {
					reqshutdown--;
					try {
						vmcl.vm.switchoff(true);
						stopped_vm++;
					} catch (StateChangeException e) {
						e.printStackTrace();
					}
				}
			}
		}
		//System.out.println("leallitott VM: "+stopped_vm + " ido: "+Timed.getFireCount());
	}	
	
	@Override
	/**
	 * Ez a metodus hivodik meg az applikacio frekvenciaja szerint. Feladata a virtualis gepeknek valo feladatosztas,
	 * es a virtualis gepek kezelese
	 */
	public void tick(long fires) {
		if (this.vmlist.isEmpty()) {
			this.generateAndAddVM(); //
		}
		if (this.VmSearch() != null) {
			this.startStation();
		}
		 // ha erkezett be a kozponti repoba feldolgozatlan adat
		this.localfilesize = (Scenario.stationvalue[this.stations.get(0).getCloudnumber()] - this.allgenerateddatasize); 
		if (this.localfilesize > 0) { 
			long processed = 0;
			boolean havevm = true;
			while (this.localfilesize != processed && havevm) { // akkor addig inditsunk feladatokat a VM-en, amig fel nem lett dolgozva az osszes
				if (this.localfilesize - processed > this.tasksize) {
					this.temp = this.tasksize; // maximalis feldolgozott meret
				} else {
					this.temp = (this.localfilesize - processed);
				}
				
				final double noi = this.temp == this.tasksize ? 2400 : (double) (2400 * this.temp / this.tasksize);
				/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
				// System.out.println(Application.temp+" : "+processed+ " :
				// "+Application.localfilesize+" : "+fires);
				final VmCollector vml = this.VmSearch();
				if (vml == null) {
					this.generateAndAddVM();
					havevm=false;
				} else {
					try {
						processed += this.temp;
						final String printtart = vml.vm + " started at " + Timed.getFireCount();
						vml.isworking = true;
						Application.feladatszam++;
						vml.pm=vml.vm.getResourceAllocation().getHost();
						
						vml.vm.newComputeTask(noi, ResourceConsumption.unlimitedProcessing,
								new ConsumptionEventAdapter() {
									long i = Timed.getFireCount();
									long ii = temp;
									double iii = noi;

									@Override
									public void conComplete() {
										vml.isworking = false;
										vml.worked = true;
										vml.tasknumber++;
										Application.feladatszam--;
										if (print == 1) {
											System.out.println(name+" "+printtart + " finished at " + Timed.getFireCount()
													+ " with " + ii + " bytes,lasted " + (Timed.getFireCount() - i)
													+ " ,noi: " + iii);
										}

									}
								});
						this.allgenerateddatasize += this.temp; // kilepesi
						// feltetelhez

					} catch (NetworkException e) {
						e.printStackTrace();
					}
				}
			}
			//System.out.println("osszes: "+Station.allstationsize +" feldolgozott: " +Application.allgenerateddatasize+ " ido: "+Timed.getFireCount());
		}
		this.turnoffVM();

		/* ------------------------------------ */
		int task = 0;
		for (VmCollector vmcl : this.vmlist) {
			if (vmcl.tasknumber > 0 && vmcl.worked && vmcl.isworking
					&& vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)) {
				task++;
			}
		}
		this.tmap.put(Timed.getFireCount(), task);

		// kilepesi feltetel az app szamara
		if (Application.feladatszam == 0 && checkStationState()
				&& (Scenario.stationvalue[this.stations.get(0).getCloudnumber()]) == this.allgenerateddatasize
				&& this.allgenerateddatasize != 0) {
			unsubscribe();
			Provider.stopProvider();
			System.out.println("~~~~~~~~~~~~");
			Scenario.scenscan=Timed.getFireCount();
			
			for (VmCollector vmcl : this.vmlist) {
				try {
					if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)) {
						vmcl.vm.switchoff(true);
					}
				} catch (StateChangeException e) {
					e.printStackTrace();
				}
			}
		}
	}
}