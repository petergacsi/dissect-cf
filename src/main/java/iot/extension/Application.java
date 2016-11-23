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
			//this.pm=this.vm.getResourceAllocation().getHost(); TODO: ezt miert kommenteztem ki?
		}
	}

	private static int i = 0;
	public static Application app;
	public static ArrayList<VmCollector> vmlist;
	private int print;
	private boolean delay;
	private static long allgenerateddatasize = 0;
	private static long localfilesize = 0;
	private static long temp;
	public static TreeMap<Long, Integer> hmap = new TreeMap<Long, Integer>();
	private static int feladatszam = 0;
	private long tasksize;

	/**
	 * Ez az osztaly egy Singleton osztaly, csak 1 peldany letezhet belole, az osztaly peldanyositasa
	 * ezzel a metodushivassal tortenhet.
	 * @param freq az applikacio ismetlesi frekvenciaja
	 * @param tasksize a maximalisan feldolgozando byte-ok szama feladatonkent
	 * @param delay a Station-ok inditasanak kesleltese adhato meg
	 * @param print logolasi funkciohoz: 1 - igen, 2 - nem
	 */
	static Application getInstance(final long freq,long tasksize, boolean delay, int print) {
		if (app == null) {
			app = new Application(freq,tasksize, delay, print);
		} else {
			System.out.println("Nem hozhato letre meg egy Application peldany!");
		}
		return Application.app;
	}

	/**
	 * Privat konstruktor, hogy csak osztalyon belulrol lehessen hivni
	 */
	private Application(final long freq,long tasksize, boolean delay, int print) {
		subscribe(freq);
		this.print = print;
		Application.vmlist = new ArrayList<VmCollector>();
		this.delay = delay;
		this.tasksize=tasksize;
	}

	/**
	 * megkeresi az elso szabad virtualis gepet, amelyik epp nem dolgozik
	 */
	private VmCollector VmSearch() {
		VmCollector vmc = null;
		for (int i = 0; i < Application.vmlist.size(); i++) {
			if (Application.vmlist.get(i).isworking == false
					&& Application.vmlist.get(i).vm.getState().equals(VirtualMachine.State.RUNNING)) {
				vmc = Application.vmlist.get(i);
				return vmc;
			}
		}
		return vmc;
	}

	/**
	 * A metodus elinditja az osszes Station mukodeset
	 */
	private void startStation() {
		if (Application.i == 0) {
			Application.i++;
			System.out.println("Scenario started at: " + Timed.getFireCount());
			for (final Station s : Station.stations) {
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
		for (int i = 0; i < Application.vmlist.size(); i++) {
			if ((Application.vmlist.get(i).vm.getState().equals(VirtualMachine.State.SHUTDOWN) /*|| TODO: Destroyed allapot jelentese?!
					Application.vmlist.get(i).vm.getState().equals(VirtualMachine.State.DESTROYED)*/) 
					&& Application.vmlist.get(i).pm!=null) {
				try {
					Application.vmlist.get(i).vm.switchOn(
							Application.vmlist.get(i).pm.allocateResources(Cloud.getArc(), false, PhysicalMachine.defaultAllocLen),
							Cloud.getIaas().repositories.get(0));
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
				Application.vmlist.add(new VmCollector(
					Cloud.getIaas().requestVM(Cloud.getVa(), Cloud.getArc(), Cloud.getIaas().repositories.get(0), 1)[0], false));	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * A metodus megvizsgalja, hogy van-e olyan Station, amelyik meg uzemel.
	 */
	private boolean checkStationState() {
		boolean i = true;
		for (Station s : Station.stations) {
			if (s.isSubscribed()) {
				i = false;
			}
		}
		return i;
	}

	/**
	 *  Megkeresi az osszes futo, de eppen nem dolgozo VM-et es 1 kivetelevel leallitja mind.
	 */
	private void turnoffVM(){
		int reqshutdown = 0;
		for (VmCollector vmcl : Application.vmlist) {
			if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING) && vmcl.isworking == false) {
				reqshutdown++;
			}
		}
		int stopped_vm=0;
		for (VmCollector vmcl : Application.vmlist) {
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
		if (Application.vmlist.isEmpty()) {
			this.generateAndAddVM(); //
		}
		if (this.VmSearch() != null) {
			this.startStation();
		}
		 // ha erkezett be a kozponti repoba feldolgozatlan adat
		Application.localfilesize = (Station.allstationsize - Application.allgenerateddatasize); 
		if (Application.localfilesize > 0) { 
			long processed = 0;
			boolean havevm = true;
			while (Application.localfilesize != processed && havevm) { // akkor addig inditsunk feladatokat a VM-en, amig fel nem lett dolgozva az osszes
				if (Application.localfilesize - processed > this.tasksize) {
					Application.temp = this.tasksize; // maximalis feldolgozott meret
				} else {
					Application.temp = (Application.localfilesize - processed);
				}
				
				final double noi = Application.temp == this.tasksize ? 2400 : (double) (2400 * Application.temp / this.tasksize);
				/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
				// System.out.println(Application.temp+" : "+processed+ " :
				// "+Application.localfilesize+" : "+fires);
				final VmCollector vml = this.VmSearch();
				if (vml == null) {
					this.generateAndAddVM();
					havevm=false;
				} else {
					try {
						processed += Application.temp;
						final String printtart = vml.vm + " started at " + Timed.getFireCount();
						vml.isworking = true;
						Application.feladatszam++;
						vml.pm=vml.vm.getResourceAllocation().getHost();
						vml.vm.newComputeTask(noi, ResourceConsumption.unlimitedProcessing,
								new ConsumptionEventAdapter() {
									long i = Timed.getFireCount();
									long ii = Application.temp;
									double iii = noi;

									@Override
									public void conComplete() {
										vml.isworking = false;
										vml.worked = true;
										vml.tasknumber++;
										Application.feladatszam--;
										if (print == 1) {
											System.out.println(printtart + " finished at " + Timed.getFireCount()
													+ " with " + ii + " bytes,lasted " + (Timed.getFireCount() - i)
													+ " ,noi: " + iii);
										}

									}
								});
						Application.allgenerateddatasize += Application.temp; // kilepesi
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
		for (VmCollector vmcl : Application.vmlist) {
			if (vmcl.tasknumber > 0 && vmcl.worked && vmcl.isworking
					&& vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)) {
				task++;
			}
		}
		Application.hmap.put(Timed.getFireCount(), task);

		// kilepesi feltetel az app szamara
		if (Application.feladatszam == 0 && checkStationState()
				&& Station.allstationsize == Application.allgenerateddatasize
				&& Application.allgenerateddatasize != 0) {
			unsubscribe();
			System.out.println("~~~~~~~~~~~~");
			System.out.println("Scenario finished at: "+Timed.getFireCount());
			for (VmCollector vmcl : Application.vmlist) {
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