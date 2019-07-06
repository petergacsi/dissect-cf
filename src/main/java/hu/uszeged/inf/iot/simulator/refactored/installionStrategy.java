package hu.uszeged.inf.iot.simulator.refactored;

import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.uszeged.inf.iot.simulator.refactored.Application;
import hu.uszeged.inf.iot.simulator.pliant.FuzzyIndicators;
import hu.uszeged.inf.iot.simulator.pliant.Kappa;
import hu.uszeged.inf.iot.simulator.pliant.Sigmoid;

public abstract class installionStrategy{
	public void install(final Device s) {
	}
		
	public void makeRelationBetweenStationsAndApp(Device s, FogApp app) {
		//Set a reference to the parentApp
		s.setApp(app);
		//Store the station in FogApp.ownStation member
		app.ownStations.add(s);
	}
	
	
}



class RandomStrategy extends installionStrategy{

	public RandomStrategy(Device s) {
		this.install(s);
	}
	
	
	
	@Override
	public void install(Device s) {
		Random randomGenerator = new Random();
		int rnd = randomGenerator.nextInt(Application.getFogApplications().size());
		
		//connect the Stations with the FogApps
		makeRelationBetweenStationsAndApp(s, Application.getFogApplications().get(rnd));
		
		Device.lmap.put(s.getDn().repoName, Device.latency);
		Device.lmap.put(s.app.computingAppliance.iaas.repositories.get(0).getName(), Device.latency);
		
		if(!s.app.isSubscribed()) {
			try {
				s.app.restartApplication();
				
			} catch (VMManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NetworkException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}

//DistanceStrategy always install station to the nearest fogApp
class DistanceStrategy extends installionStrategy {
	
	
	public DistanceStrategy(Device d) {
		this.install(d);
	}
	
	public Application getNearestDevice(Device d) {
		double minDistance = Double.MAX_VALUE;
		//TODO change later
		Application nearestApplication = null;
		for (Application app : Application.getFogApplications()) {
			if (minDistance >= d.calculateDistance(app) ) {
				minDistance = d.calculateDistance(app);
				nearestApplication = app;
			}
		}
		return nearestApplication;
	}
	
	@Override
	public void install(Device s) {
		FogApp nearestApp = (FogApp) getNearestDevice(s);
		
		//connect the Stations with the FogApps
		makeRelationBetweenStationsAndApp(s, nearestApp);
		
		Device.lmap.put(s.getDn().repoName, Device.latency);
		Device.lmap.put(s.app.computingAppliance.iaas.repositories.get(0).getName(), Device.latency);
		
		if(!s.app.isSubscribed()) {
			try {
				s.app.restartApplication();
				
			} catch (VMManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NetworkException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}	
	
}

//Not fully working
//need a valid ratio that depends on network capabilities
class DistancePerInBandWidthStrategy extends installionStrategy {
	
	public DistancePerInBandWidthStrategy(Device d) {
		this.install(d);
	}
	
	
	
	
	public Application getDeviceWithBetterDistancePerInBWRatio (Device d) {
		double maxRatio = Double.MIN_VALUE;
		Application deviceWithBetterRatio = null;
		for (Application app : Application.getFogApplications()) {
			//TODO rethink the ratio
			//.getInput returns max incoming network data but we need other properties (latency)
			double ratio = (d.calculateDistance(app) / app.computingAppliance.iaas.repositories.get(0).getInputbw());
			System.out.println(ratio + " " + app.computingAppliance.iaas.repositories.get(0).getInputbw());
			if (maxRatio <= ratio) {
				maxRatio = ratio;
				deviceWithBetterRatio = app;
			}
		}
		
		return deviceWithBetterRatio;
	}
	
	@Override
	public void install(Device s) {
		FogApp appWithBetterRatio = (FogApp) getDeviceWithBetterDistancePerInBWRatio(s);
		
		//connect the Stations with the FogApps
		makeRelationBetweenStationsAndApp(s, appWithBetterRatio);
		
		Device.lmap.put(s.getDn().repoName, Device.latency);
		Device.lmap.put(s.app.computingAppliance.iaas.repositories.get(0).getName(), Device.latency);
		
		if(!s.app.isSubscribed()) {
			try {
				s.app.restartApplication();
				
			} catch (VMManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NetworkException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}	
	
}



class CostStrategy extends installionStrategy{

	public CostStrategy(Device d) {
		this.install(d);
	}
	@Override
	public void install(Device s) {
		 double min=Integer.MAX_VALUE-1.0;
		 int choosen=-1;
		for(int i=0;i<Application.getFogApplications().size();++i){
			if(Application.getFogApplications().get(i).instance.getPricePerTick()<min){
				min = Application.getFogApplications().get(i).instance.getPricePerTick();
				choosen = i;
			}
		}
		
		makeRelationBetweenStationsAndApp(s, Application.getFogApplications().get(choosen));
	
		/*
		s.setApp(Application.getFogApplications().get(choosen));
		Application.getFogApplications().get(choosen).ownStations.add(s);
		*/
		
		Device.lmap.put(s.getDn().repoName, Device.latency);
		Device.lmap.put(s.app.computingAppliance.iaas.repositories.get(0).getName(), Device.latency);
		
		if(!s.app.isSubscribed()) {
			try {
				s.app.restartApplication();
				
			} catch (VMManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NetworkException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}

class RuntimeStrategy extends installionStrategy{
	
	public RuntimeStrategy(Device s) {
		this.install(s);
	}
	
	@Override
	public void install(final Device s) {
		new DeferredEvent(s.startTime) {
			
			@Override
			protected void eventAction() {
				double min = Double.MAX_VALUE-1.0;
				int choosen = -1;
				for(int i=0;i< Application.getFogApplications().size();i++ ){
					double loadRatio = (Application.getFogApplications().get(i).ownStations.size())/(Application.getFogApplications().get(i).computingAppliance.iaas.machines.size());
					if(loadRatio<min){
						min=loadRatio;
						choosen = i;
					}
				}
				
				makeRelationBetweenStationsAndApp(s, Application.getFogApplications().get(choosen));
				
				/*
				s.setApp(Application.getFogApplications().get(choosen));
				Application.getFogApplications().get(choosen).ownStations.add(s);
				*/
				
				Device.lmap.put(s.getDn().repoName, Device.latency);
				Device.lmap.put(s.app.computingAppliance.iaas.repositories.get(0).getName(), Device.latency);
				
				if(!s.app.isSubscribed()) {
					try {
						s.app.restartApplication();
						
					} catch (VMManagementException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NetworkException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		
		
	}
	
}

class FuzzyStrategy extends installionStrategy{
	Device d;
	public FuzzyStrategy(Device s) {
		this.d=s;
		this.install(s);
	}
	@Override
	public void install(final Device s) {
		new DeferredEvent(s.startTime) {
			
			@Override
			protected void eventAction() {
				int rsIdx = fuzzyDecision(d);
				
				makeRelationBetweenStationsAndApp(s, Application.getFogApplications().get(rsIdx));
				/*
				s.setApp(Application.getFogApplications().get(rsIdx));
				Application.getFogApplications().get(rsIdx);
				*/
				
				Device.lmap.put(d.getDn().repoName, Device.latency);
				Device.lmap.put(d.app.computingAppliance.iaas.repositories.get(0).getName(), Device.latency);
				if(!s.app.isSubscribed()) {
					try {
						s.app.restartApplication();
						
					} catch (VMManagementException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (NetworkException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
			
		};
		

		// 
		// Application.applications.get(i).instance.pricePerTick // ar
		// Application.applications.get(i).vmlist.size()() hany van most
		// Station.sd. (start stop time) -> ekkor kuld jeleket
		// Station.sd. freq -> adatgyujtes gyakoris�ga
		// Cloud.
		
		// Application.applications.get(i).stations
		// Application.applications.get(i).tasksize
		// Application.applications.get(i).vmsize
	
		//cloud info
		// Cloud.clouds.get(1)
		// Cloud.clouds.get(1).iaas.machines fizikai gepek
		// Application.applications.get(0).cloud - application cloud kapcsolat
		
		// Application.applications.get(0).allprocessed mennyit hajtott vegre
		//gyenge vm tovabb
		
		//vm gep tipusa
		// Application.applications.get(0).instance.arc.getRequiredCPUs()
		//
		//fel van-e iratkozva, ha felvan akkor fut
		// s.isSubscribed() 
		
	}
	
private int fuzzyDecision(Device s) {
		
		//Vector<Double> temp2 = new Vector<Double>();
		Kappa kappa = new Kappa(3.0,0.4);
		//System.out.println("test");
		Sigmoid sig = new Sigmoid(Double.valueOf(-1.0/96.0), Double.valueOf(15));
		Vector<Double> price = new Vector<Double>();
		for(int i=0;i<Application.getFogApplications().size();++i){
			price.add(kappa.getAt(sig.getat(Application.getFogApplications().get(i).instance.getPricePerTick()*1000000000)));
			//System.out.println(Application.applications.get(i).instance.pricePerTick*1000000000);
			//System.out.println("Cost: " + Application.applications.get(i).getCurrentCostofApp());
			//System.out.println("Load: " + Application.applications.get(i).getLoadOfCloud());
			//temp2.add((double)Application.applications.get(i).getCurrentCostofApp());
			//temp2.add((Double.parseDouble((Application.applications.size()))));
		}
		//System.out.println(temp2);
		
		//System.out.println(price);
		double minprice = Double.MAX_VALUE;
		double maxprice= Double.MIN_VALUE;
		for(int i=0;i<Application.getFogApplications().size();++i){
			double currentprice = Application.getFogApplications().get(i).getCurrentCostofApp();
			if(currentprice > maxprice)
				maxprice = currentprice;
			if(currentprice < minprice)
				minprice = currentprice;
		}
		
		
		Vector<Double> currentprice = new Vector<Double>();
		//System.out.println("test");
		sig = new Sigmoid(Double.valueOf(-1.0), Double.valueOf((maxprice-minprice)/2.0));
		for(int i=0;i<Application.getFogApplications().size();++i){
			currentprice.add(kappa.getAt(sig.getat(Application.getFogApplications().get(i).getCurrentCostofApp())));
		}
		
	
		//System.out.println(currentprice);
		
		
		double minworkload = Double.MAX_VALUE;
		double maxworkload= Double.MIN_VALUE;
		for(int i=0;i<Application.getFogApplications().size();++i){
			double workload = Application.getFogApplications().get(i).getLoadOfCloud();
			if(workload > maxworkload)
				maxworkload = workload;
			if(workload < minworkload)
				minworkload = workload;
		}
		
		Vector<Double> workload = new Vector<Double>();
		//System.out.println("test");
		sig = new Sigmoid(Double.valueOf(-1.0), Double.valueOf(maxworkload));
		for(int i=0;i<Application.getFogApplications().size();++i){
			workload.add(kappa.getAt(sig.getat(Application.getFogApplications().get(i).getLoadOfCloud())));
			//temp2.add(Application.applications.get(i).getLoadOfCloud());
		}
		//System.out.println(temp2);
		//System.out.println(workload);
		
		
		Vector<Double> numberofvm = new Vector<Double>();
		sig = new Sigmoid(Double.valueOf(-1.0/8.0),Double.valueOf(3));
		for(int i=0;i<Application.getFogApplications().size();++i){			
			numberofvm.add(kappa.getAt(sig.getat(Double.valueOf(Application.getFogApplications().get(i).vmlist.size()))));
			//temp2.add((double)Application.applications.get(i).vmlist.size());
		}
		//System.out.println(temp2);
		//System.out.println(numberofvm);
		
		double sum_stations = 0.0;
		for(int i=0;i<Application.getFogApplications().size();++i){			
			sum_stations += Application.getFogApplications().get(i).ownStations.size();
		}
		
		Vector<Double> numberofstation = new Vector<Double>();
		sig = new Sigmoid(Double.valueOf(-0.125),Double.valueOf(sum_stations/(Application.getFogApplications().size())));
		for(int i=0;i<Application.getFogApplications().size();++i){		
			numberofstation.add(kappa.getAt(sig.getat(Double.valueOf(Application.getFogApplications().get(i).ownStations.size()))));
			//temp2.add((double)Application.applications.get(i).stations.size());
		}
		
		Vector<Double> numberofActiveStation = new Vector<Double>();
		for(int i=0;i<Application.getFogApplications().size();++i){		
			double sum = 0.0;
			for(int j=0;j<Application.getFogApplications().get(i).ownStations.size();j++) {
				Station stat = (Station) Application.getFogApplications().get(i).ownStations.get(j);
				long time = Timed.getFireCount();
				if(stat.startTime >= time && stat.stopTime >= time)
					sum +=1;
			}	
			numberofActiveStation.add(sum);
		}
		sum_stations = 0.0;
		for(int i=0;i<numberofActiveStation.size();++i){			
			sum_stations += numberofActiveStation.get(i);
		}
		
		sig = new Sigmoid(Double.valueOf(-0.125),Double.valueOf(sum_stations/(numberofActiveStation.size())));
		for(int i=0;i<numberofActiveStation.size();++i){
			double a = numberofActiveStation.get(i);
			double b = sig.getat(a);
			double c = kappa.getAt(b);
			numberofActiveStation.set(i, c);
		}
		
		
		//System.out.println(numberofstation);
		//System.out.println(temp2);
		
		Vector<Double> preferVM = new Vector<Double>();
		sig = new Sigmoid(Double.valueOf(1.0/32),Double.valueOf(3));
		for(int i=0;i<Application.getFogApplications().size();++i){
			preferVM.add(kappa.getAt(sig.getat(Double.valueOf(Application.getFogApplications().get(i).instance.getArc().getRequiredCPUs()))));
		}
		//System.out.println(preferVM);
		
		Vector<Double> preferVMMem = new Vector<Double>();
		sig = new Sigmoid(Double.valueOf(1.0/256.0),Double.valueOf(350.0));
		for(int i=0;i<Application.getFogApplications().size();++i){	
			preferVMMem.add(kappa.getAt(sig.getat(Double.valueOf(Application.getFogApplications().get(i).instance.getArc().getRequiredMemory() / 10000000))));
		}
		//System.out.println(preferVMMem);
		
		
		
		
		Vector<Double> score = new Vector<Double>();
		for(int i=0;i<price.size();++i){
			Vector<Double> temp = new Vector<Double>();
			temp.add(price.get(i));
			//temp.add(numberofvm.get(i));
			temp.add(numberofstation.get(i));
			temp.add(numberofActiveStation.get(i));
			temp.add(preferVM.get(i));
			temp.add(workload.get(i));
			temp.add(currentprice.get(i));
			//temp.add(preferVMMem.get(i));
			score.add(FuzzyIndicators.getAggregation(temp)*100);
		}
		Vector<Integer> finaldecision = new Vector<Integer>();
		for(int i=0;i<Application.getFogApplications().size();++i){
			finaldecision.add(i);	
		}
		for(int i=0;i<score.size();++i){
			for(int j = 0; j< score.get(i); j++) {
				finaldecision.add(i);
			}
		}
		Random rnd = new Random();
		Collections.shuffle(finaldecision);			
		int temp = rnd.nextInt(finaldecision.size());
		return finaldecision.elementAt(temp);		
		
		
	}
}