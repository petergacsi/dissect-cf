package hu.uszeged.inf.iot.simulator.entities;

import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.uszeged.inf.iot.simulator.pliant.FuzzyIndicators;
import hu.uszeged.inf.iot.simulator.pliant.Kappa;
import hu.uszeged.inf.iot.simulator.pliant.Sigmoid;

public interface installionStrategy{
	public void install(final Device s);
}

class RandomStrategy implements installionStrategy{

	public RandomStrategy(Device s) {
		this.install(s);
	}
	@Override
	public void install(Device s) {
		Random randomGenerator = new Random();
		int rnd = randomGenerator.nextInt(Application.applications.size());
		Application.addStation(s, Application.applications.get(rnd));
		
		Device.lmap.put(s.getDn().repoName, Device.latency);
		Device.lmap.put(s.app.cloud.iaas.repositories.get(0).getName(), Device.latency);
		
	}
}

class CostStrategy implements installionStrategy{

	public CostStrategy(Device d) {
		this.install(d);
	}
	@Override
	public void install(Device s) {
		 double min=Integer.MAX_VALUE-1.0;
		 int choosen=-1;
		for(int i=0;i<Application.applications.size();++i){
			if(Application.applications.get(i).instance.pricePerTick<min){
				min = Application.applications.get(i).instance.pricePerTick;
				choosen = i;
			}
		}
		Application.addStation(s, Application.applications.get(choosen));
		Device.lmap.put(s.getDn().repoName, Device.latency);
		Device.lmap.put(s.app.cloud.iaas.repositories.get(0).getName(), Device.latency);
		
	}
	
}
class RuntimeStrategy implements installionStrategy{
	
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
				for(int i=0;i< Application.applications.size();i++ ){
					double loadRatio = (Application.applications.get(i).stations.size())/(Application.applications.get(i).cloud.iaas.machines.size());
					if(loadRatio<min){
						min=loadRatio;
						choosen = i;
					}
				}
				Application.addStation(s, Application.applications.get(choosen));
				Device.lmap.put(s.getDn().repoName, Device.latency);
				Device.lmap.put(s.app.cloud.iaas.repositories.get(0).getName(), Device.latency);
			}
		};
		
	}
	
}

class FuzzyStrategy implements installionStrategy{
	Device d;
	public FuzzyStrategy(Device s) {
		this.d=s;
		this.install(s);
	}
	@Override
	public void install(Device s) {
		new DeferredEvent(s.startTime) {
			
			@Override
			protected void eventAction() {
				int rsIdx = fuzzyDecision(d);
				Application.addStation(d, Application.applications.get(rsIdx));
				Device.lmap.put(d.getDn().repoName, Device.latency);
				Device.lmap.put(d.app.cloud.iaas.repositories.get(0).getName(), Device.latency);
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
		for(int i=0;i<Application.applications.size();++i){
			price.add(kappa.getAt(sig.getat(Application.applications.get(i).instance.pricePerTick*1000000000)));
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
		for(int i=0;i<Application.applications.size();++i){
			double currentprice = Application.applications.get(i).getCurrentCostofApp();
			if(currentprice > maxprice)
				maxprice = currentprice;
			if(currentprice < minprice)
				minprice = currentprice;
		}
		
		
		Vector<Double> currentprice = new Vector<Double>();
		//System.out.println("test");
		sig = new Sigmoid(Double.valueOf(-1.0), Double.valueOf((maxprice-minprice)/2.0));
		for(int i=0;i<Application.applications.size();++i){
			currentprice.add(kappa.getAt(sig.getat(Application.applications.get(i).getCurrentCostofApp())));
		}
		
	
		//System.out.println(currentprice);
		
		
		double minworkload = Double.MAX_VALUE;
		double maxworkload= Double.MIN_VALUE;
		for(int i=0;i<Application.applications.size();++i){
			double workload = Application.applications.get(i).getLoadOfCloud();
			if(workload > maxworkload)
				maxworkload = workload;
			if(workload < minworkload)
				minworkload = workload;
		}
		
		Vector<Double> workload = new Vector<Double>();
		//System.out.println("test");
		sig = new Sigmoid(Double.valueOf(-1.0), Double.valueOf(maxworkload));
		for(int i=0;i<Application.applications.size();++i){
			workload.add(kappa.getAt(sig.getat(Application.applications.get(i).getLoadOfCloud())));
			//temp2.add(Application.applications.get(i).getLoadOfCloud());
		}
		//System.out.println(temp2);
		//System.out.println(workload);
		
		
		Vector<Double> numberofvm = new Vector<Double>();
		sig = new Sigmoid(Double.valueOf(-1.0/8.0),Double.valueOf(3));
		for(int i=0;i<Application.applications.size();++i){			
			numberofvm.add(kappa.getAt(sig.getat(Double.valueOf(Application.applications.get(i).vmlist.size()))));
			//temp2.add((double)Application.applications.get(i).vmlist.size());
		}
		//System.out.println(temp2);
		//System.out.println(numberofvm);
		
		double sum_stations = 0.0;
		for(int i=0;i<Application.applications.size();++i){			
			sum_stations += Application.applications.get(i).stations.size();
		}
		
		Vector<Double> numberofstation = new Vector<Double>();
		sig = new Sigmoid(Double.valueOf(-0.125),Double.valueOf(sum_stations/(Application.applications.size())));
		for(int i=0;i<Application.applications.size();++i){		
			numberofstation.add(kappa.getAt(sig.getat(Double.valueOf(Application.applications.get(i).stations.size()))));
			//temp2.add((double)Application.applications.get(i).stations.size());
		}
		
		Vector<Double> numberofActiveStation = new Vector<Double>();
		for(int i=0;i<Application.applications.size();++i){		
			double sum = 0.0;
			for(int j=0;j<Application.applications.get(i).stations.size();j++) {
				Station stat = (Station) Application.applications.get(i).stations.get(j);
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
		for(int i=0;i<Application.applications.size();++i){
			preferVM.add(kappa.getAt(sig.getat(Double.valueOf(Application.applications.get(i).instance.arc.getRequiredCPUs()))));
		}
		//System.out.println(preferVM);
		
		Vector<Double> preferVMMem = new Vector<Double>();
		sig = new Sigmoid(Double.valueOf(1.0/256.0),Double.valueOf(350.0));
		for(int i=0;i<Application.applications.size();++i){	
			preferVMMem.add(kappa.getAt(sig.getat(Double.valueOf(Application.applications.get(i).instance.arc.getRequiredMemory() / 10000000))));
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
		for(int i=0;i<Application.applications.size();++i){
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