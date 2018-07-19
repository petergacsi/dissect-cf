package hu.uszeged.inf.iot.simulator.entities;

import java.util.ArrayList;
import java.util.TreeMap;
import javax.xml.bind.JAXBException;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChangeException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.uszeged.inf.iot.simulator.providers.Instance;
import hu.uszeged.inf.xml.model.ApplicationModel;

public class Application extends Timed {

	public class VmCollector {

		VirtualMachine vm;
		boolean isworking;
		int tasknumber;
		boolean worked;
		PhysicalMachine pm;
		long lastWorked;
		public long workingTime;
		

		public boolean isWorked() {
			return worked;
		}

		public long letehozva;

		VmCollector(VirtualMachine vm) {
			this.vm = vm;
			this.isworking = false;
			this.tasknumber = 0;
			this.worked = false;
			this.workingTime = 0;
			this.lastWorked = Timed.getFireCount();
			this.letehozva = Timed.getFireCount();
		}
	}

	boolean stationStarter;
	public static ArrayList<Application> applications = new ArrayList<Application>();
	public long allWorkTime;
	public ArrayList<VmCollector> vmlist;
	private long allgenerateddatasize = 0;
	static long allprocessed = 0;
	private long localfilesize = 0;
	private long temp;
	public TreeMap<Long, Integer> tmap = new TreeMap<Long, Integer>();
	private static int feladatszam = 0;
	private long tasksize;
	public Cloud cloud;
	public ArrayList<Station> stations;
	String name;
	Instance instance;




	public static void loadApplication(String appfile) throws JAXBException {
		for (ApplicationModel am : ApplicationModel.loadApplicationXML(appfile)) {
			new Application(am.freq, am.tasksize, am.cloud, am.instance, am.name);
		}
	}

	private Application(final long freq, long tasksize, String cloud, String instance, String name) {
		this.vmlist = new ArrayList<VmCollector>();
		this.stations = new ArrayList<Station>();
		this.tasksize = tasksize;
		this.allWorkTime=0;
		this.cloud = Cloud.addApplication(this, cloud);
		this.name = name;
		if (cloud != null) {
			subscribe(freq);
		}
		this.instance = Instance.instances.get(instance);
		Application.applications.add(this);
		this.cloud.iaas.repositories.get(0).registerObject(this.instance.va);
	}

	/**
	 * megkeresi az elso szabad virtualis gepet, amelyik epp nem dolgozik
	 */
	private VmCollector VmSearch() {
		VmCollector vmc = null;

		for (int i = 0; i < this.vmlist.size(); i++) {
			// System.out.println(this.vmlist.get(i).vm.getState()+"
			// "+Timed.getFireCount()+ " + " + this.instance.arc +" + "+
			// this.name);
			if ((this.vmlist.get(i).isworking == false
					&& this.vmlist.get(i).vm.getState().equals(VirtualMachine.State.RUNNING))) {
				vmc = this.vmlist.get(i);
				return vmc;
			}
		}
		return vmc;
	}

	private void generateAndAddVM() {
		try {
			if (this.turnonVM() == false) {

				// TODO: ide kene 1 feltetel h ne kezdjen el random vm-eket
				// fölöslegesen létrehozni
				// for (int i = 0; i < this.vmlist.size(); i++) {
				// System.err.println(this.vmlist.get(i).vm);
				// }
				boolean vanpm = false;
				for (PhysicalMachine pm : this.cloud.iaas.machines) {
					// System.err.println(pm.availableCapacities);

					if (pm.availableCapacities.getRequiredCPUs() >= this.instance.arc.getRequiredCPUs()) {
						vanpm = true;

					}
				}
				if (vanpm) { // csak akkor kerjunk uj vm-et ha van fizikai
								// eroforrasunkra.
					// System.out.println(this.cloud.iaas.repositories.get(0).toString());

					this.vmlist.add(new VmCollector(this.cloud.iaas.requestVM(this.instance.va, this.instance.arc,
							this.cloud.iaas.repositories.get(0), 1)[0]));
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * A metodus megkeresi es ujrainditja az elso SHUTDOWN allapotban levo
	 * virtualis gepet.
	 */
	private boolean turnonVM() {
		for (int i = 0; i < this.vmlist.size(); i++) {
			if ((this.vmlist.get(i).vm.getState().equals(
					VirtualMachine.State.SHUTDOWN) /*
													 * || TODO: Destroyed
													 * allapot jelentese?!
													 * Application.vmlist.get(i)
													 * .vm.getState().equals(
													 * VirtualMachine.State.
													 * DESTROYED)
													 */)
			/*
			 * && this.vmlist.get(i).pm!=null &&
			 * this.cloud.getArc().compareTo(this.vmlist.get(i).pm.
			 * freeCapacities)<=0
			 */
			) {
				try {
					if (this.vmlist.get(i).pm.freeCapacities.getRequiredCPUs() == 0.0) {
						// return false;
						// TODO: megvizsgalni,hogy ez egyaltalan elofordulhat?
						// nem hozunk letre random vm-eket szoval elmeletileg ez
						// az if torolheto, at kell gondolni.
						System.err.println("Application - turnonVM method");
						System.exit(0);
					}
					this.vmlist.get(i).vm.switchOn(this.vmlist.get(i).pm.allocateResources(this.instance.arc, false,
							PhysicalMachine.defaultAllocLen), this.cloud.iaas.repositories.get(0));

				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * A metodus - ha nincs olyan VM amit ujra lehet inditani - letrehoz egyet.
	 */

	/**
	 * A metodus megvizsgalja, hogy van-e olyan Station, amelyik meg uzemel.
	 */
	private boolean checkStationState() { // TODO probably wrong, but lets see
		for (Station s : this.stations) {
			// System.out.println(s + " "+ Timed.getFireCount());
			if (s.isSubscribed()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Megkeresi az osszes futo, de eppen nem dolgozo VM-et es 1 kivetelevel
	 * leallitja mind.
	 */
	private void turnoffVM() {
		int reqshutdown = 0;
		for (VmCollector vmcl : this.vmlist) {
			if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING) && vmcl.isworking == false) {
				reqshutdown++;
			}
		}
		int stopped_vm = 0;
		for (VmCollector vmcl : this.vmlist) {
			if (reqshutdown > 1) {
				if (vmcl.vm.getState().equals(VirtualMachine.State.RUNNING) && vmcl.isworking == false) {
					reqshutdown--;
					try {
						vmcl.lastWorked = Timed.getFireCount();
						vmcl.vm.switchoff(false);
						stopped_vm++;
					} catch (StateChangeException e) {
						e.printStackTrace();
					}
				}
			}
		}
		// System.out.println("leallitott VM: "+stopped_vm + " ido:
		// "+Timed.getFireCount());
	}

	private void countVmRunningTime() {
		for (VmCollector vmc : this.vmlist) {
			if (vmc.vm.getState().equals(VirtualMachine.State.RUNNING)) {
				vmc.workingTime += (Timed.getFireCount() - vmc.lastWorked);
				allWorkTime+=(Timed.getFireCount() - vmc.lastWorked);
				vmc.lastWorked = Timed.getFireCount();
				
			}
		}
	}

	private long sumOfData() {
		long temp = 0;
		for (Station s : this.stations) {
			temp += s.generatedfilesize;
		}
		return temp;
	}

	@Override
	/**
	 * Ez a metodus hivodik meg az applikacio frekvenciaja szerint. Feladata a
	 * virtualis gepeknek valo feladatosztas, es a virtualis gepek kezelese
	 */
	public void tick(long fires) {
		// ha erkezett be a kozponti repoba feldolgozatlan adat
		this.localfilesize = (this.sumOfData() - this.allgenerateddatasize);
		// System.out.println(this.sumOfData());
		if (this.localfilesize > 0) {

			long processed = 0;
			boolean havevm = true;
			while (this.localfilesize != processed && havevm) { // akkor addig
																// inditsunk
																// feladatokat a
																// VM-en, amig
																// fel nem lett
																// dolgozva az
																// osszes
				if (this.localfilesize - processed > this.tasksize) {
					this.temp = this.tasksize; // maximalis feldolgozott meret
				} else {
					this.temp = (this.localfilesize - processed);
				}
				//TODO: should delete the burned value
				final double noi = this.temp == this.tasksize ? 2400 : (double) (2400 * this.temp / this.tasksize);
				/* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
				// System.out.println(Application.temp+" : "+processed+ " :
				// "+Application.localfilesize+" : "+fires);
				final VmCollector vml = this.VmSearch();
				if (vml == null) {
					this.generateAndAddVM();

					havevm = false;
				} else {
					try {
						processed += this.temp;
						final String printtart = vml.vm + " started at " + Timed.getFireCount();
						vml.isworking = true;
						Application.feladatszam++;
						vml.pm = vml.vm.getResourceAllocation().getHost();

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
											System.out.println(name + " " + printtart + " finished at "
													+ Timed.getFireCount() + " with " + ii + " bytes,lasted "
													+ (Timed.getFireCount() - i) + " ,noi: " + iii);

									}
								});
						this.allgenerateddatasize += this.temp; 
						Application.allprocessed += this.temp;
					} catch (NetworkException e) {
						e.printStackTrace();
					}
				}
			}
		}
		this.countVmRunningTime();

		/* ------------------------------------ */
		/*
		 * int task = 0; for (VmCollector vmcl : this.vmlist) { if
		 * (vmcl.tasknumber >= 0 && vmcl.isworking &&
		 * vmcl.vm.getState().equals(VirtualMachine.State.RUNNING)) { task++; }
		 * }
		 */
		// this.tmap.put(Timed.getFireCount(), task);
		 this.turnoffVM();

		// kilepesi feltetel az app szamara
		if (Application.feladatszam == 0 && checkStationState() && Timed.getFireCount() > getLongestStoptime()) {
			unsubscribe();
			System.out.println("Application " + this.name + " has stopped @" + Timed.getFireCount()+" price: "+this.instance.calculateCloudCost(allWorkTime));

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

	long getLongestStoptime() {
		long max = -1;
		for (Station s : this.stations) {
			if (s.sd.stoptime > max) {
				max = s.sd.stoptime;
			}
		}
		return max;
	}

	public static void addStation(Station s, Application a) {
		a.stations.add(s);
		s.app = a;
	}
}