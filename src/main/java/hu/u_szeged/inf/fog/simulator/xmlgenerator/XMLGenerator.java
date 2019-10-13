package hu.u_szeged.inf.fog.simulator.xmlgenerator;

import static java.lang.Math.pow;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import hu.u_szeged.inf.fog.simulator.examples.ScenarioBase;
import hu.u_szeged.inf.fog.simulator.xmlgenerator.Appliances.Appliance;
import hu.u_szeged.inf.fog.simulator.xmlgenerator.Appliances.Appliance.Applications;
import hu.u_szeged.inf.fog.simulator.xmlgenerator.Appliances.Appliance.NeighbourAppliances;
import hu.u_szeged.inf.fog.simulator.xmlgenerator.Appliances.Appliance.Applications.Application;
import hu.u_szeged.inf.fog.simulator.xmlgenerator.Appliances.Appliance.NeighbourAppliances.Device;
import hu.u_szeged.inf.fog.simulator.xmlgenerator.Devices.Device.Shutdown;

public class XMLGenerator {

	private static String LOCATION_OF_XML_OUTPUT = ScenarioBase.resourcePath + "CompareToIFogSim_XMLs";
	private static int NUMBER_OF_CLOUDS = 3;
	private static int NUMBER_OF_TYPE1_FOGS = 30;
	private static int NUMBER_OF_TYPE2_FOGS = 100;
	private static int RADIUS_OF_CIRCLE = 20;
	private static int NUMBER_OF_STATIONS = 300;
	private static int MINIMAL_NUMBER_OF_NEIGHBOURS = 2;
	
	public static void main(String[] args) {
		Appliances appliances = new Appliances();
		appliances.appliance = new ArrayList<Appliance>();
		for (int i = 0; i < NUMBER_OF_CLOUDS; i++) {
			appliances.appliance.add(generateAppliance("cloud", "cloud", 2500000, i, RADIUS_OF_CIRCLE, "1CPU_Strong_VM", 0));
		}
		for (int x = 0; x < NUMBER_OF_TYPE1_FOGS; x++) {
			appliances.appliance.add(generateAppliance("fog", "fog_type1", 2500000, x, RADIUS_OF_CIRCLE * 2, "1CPU_Strong_VM", 1));
		}
		for (int y = NUMBER_OF_TYPE1_FOGS * NUMBER_OF_CLOUDS; y < NUMBER_OF_TYPE1_FOGS * NUMBER_OF_CLOUDS + NUMBER_OF_TYPE2_FOGS; y++) {
			appliances.appliance.add(generateAppliance("fog", "fog_type2", 2500000, y, RADIUS_OF_CIRCLE * 4, "1CPU_Strong_VM", 2));
			
		}
		updateParent(appliances.appliance);
		createNeighbours(appliances.appliance);
		createApplianceXMLFile(appliances);
		
		//createStationsXML(createDevices());
		
	}
	
	
	
	
	public static void createNeighbours(List<Appliance> appliances) {
		for (Appliance appliance : appliances) {
			Map<Appliance, Double> allNeighbourApplianceAndDistance = getAllNeighbourApplianceWithDistanceValues(appliance, appliances);
			int numberOfNeighbours = (int) (getAllApplianceInTheSameLevel(appliance, appliances).size() * 0.20);
			if (numberOfNeighbours < MINIMAL_NUMBER_OF_NEIGHBOURS) {
				numberOfNeighbours = MINIMAL_NUMBER_OF_NEIGHBOURS;
			}
			List<Appliance> allNeighbourAppliance = getXNearestapplication(numberOfNeighbours, appliance, allNeighbourApplianceAndDistance);
			List<Device> deviceList = convertListOfAppliancesToDevice(allNeighbourAppliance);
			appliance.neighbourAppliances.device.addAll(deviceList);
		}
	}
	
	public static Device convertApplianceToDevice(Appliance appliance) {
		Device device = new Device();
		device.setDeviceName(appliance.getName());
		return device;
	}
	
	public static List<Device> convertListOfAppliancesToDevice(List<Appliance> appliances) {
		List<Device> deviceList = new ArrayList<Device>();
		for (Appliance appliance : appliances) {
			deviceList.add(convertApplianceToDevice(appliance));
		}
		return deviceList;
	}


	public static Map<Appliance, Double> getAllApplianceWithDistanceValues(Appliance applianceToGetNeighbour, List<Appliance> appliances){
		Map<Appliance, Double> allApplianceWithDistance = new HashMap<Appliance, Double>();
		for (Appliance appliance : appliances) {
			allApplianceWithDistance.put(appliance, measureDistanceBetweenAppliances(applianceToGetNeighbour, appliance));
		}
		return allApplianceWithDistance;
	}
	
	
	public static Map<Appliance, Double> getAllNeighbourApplianceWithDistanceValues(Appliance applianceToGetNeighbour, List<Appliance> appliances){
		Map<Appliance, Double> allNeighbourApplianceWithDistance = new HashMap<Appliance, Double>();
		for (Appliance appliance : appliances) {
			if (applianceToGetNeighbour.name.equals(appliance.name) || applianceToGetNeighbour.getLevel() != appliance.getLevel()) {
			} else {
				allNeighbourApplianceWithDistance.put(appliance, measureDistanceBetweenAppliances(applianceToGetNeighbour, appliance));
			}
		}
		return allNeighbourApplianceWithDistance;
	}
	
	
	public static List<Appliance> getXNearestapplication(int howManyNeighbour, Appliance appliance , Map<Appliance, Double> appliancesAndDistanceValues){
		List<Entry<Appliance, Double>> xNearestApplianceUnsorted = new ArrayList<Map.Entry<Appliance,Double>>(appliancesAndDistanceValues.entrySet());
		xNearestApplianceUnsorted.sort(new CoordComparator());
		List<Appliance> xNearestapplianceSorted = new ArrayList<Appliance>();
		for (Entry<Appliance, Double> entry : xNearestApplianceUnsorted) {
			xNearestapplianceSorted.add(entry.getKey());
		}
		return xNearestapplianceSorted.subList(0, howManyNeighbour);
	}
	
	
	public static double measureDistanceBetweenAppliances(Appliance appliance1, Appliance appliance2) {
		return Math.sqrt( (pow(appliance1.getXcoord() - appliance2.getXcoord() , 2) + pow(appliance1.getYcoord() - appliance2.getYcoord() , 2)));	
	}
	
	
	public static Appliance generateAppliance(String typeOfAppliance, String typeOfFile, int taskSize, int indexForName, int radius, String instance, int level) {
		Appliance appliance = new Appliance();
		appliance.setName(typeOfAppliance + indexForName);
		appliance.setFile(typeOfFile);
		appliance.setParentApp("");
		XMLGenerator.Coord coord = new Coord(radius);
		appliance.setXcoord(coord.getX());
		appliance.setYcoord(coord.getY());
		
		appliance.setLevel(level);
		
		Applications applications = new Applications();
		Application application = generateApplication(300000, taskSize, typeOfAppliance, appliance.getName(), instance);
		applications.application = new ArrayList<Application>();
		applications.application.add(application);
		appliance.setApplications(applications);
		
		NeighbourAppliances neighbourAppliances = new NeighbourAppliances();
		neighbourAppliances.device = new ArrayList<Device>();
		appliance.setNeighbourAppliances(neighbourAppliances);
		return appliance;
	}
	
	public static List<Appliance> getAllApplianceInLowerLevel(Appliance surveyedAppliance, List<Appliance> allAppliances) {
		List<Appliance> allApplianceInLowerLevel = new ArrayList<Appliance>();
		for (Appliance appliance : allAppliances ) {
			if (appliance.getLevel() == surveyedAppliance.getLevel() - 1) {
				allApplianceInLowerLevel.add(appliance);
			}
		}
		return allApplianceInLowerLevel;
	}
	
	public static List<Appliance> getAllApplianceInTheSameLevel(Appliance surveyedAppliance, List<Appliance> allAppliances) {
		List<Appliance> allApplianceInLowerLevel = new ArrayList<Appliance>();
		for (Appliance appliance : allAppliances ) {
			if (appliance.getLevel() == surveyedAppliance.getLevel()) {
				allApplianceInLowerLevel.add(appliance);
			}
		}
		return allApplianceInLowerLevel;
	}
	
	public static void updateParent(List<Appliance> appliances) {
		for (Appliance appliance : appliances) {
			if (appliance.getLevel() != 0) {
				List<Appliance> applianceInTheLowerLevel = getAllApplianceInLowerLevel(appliance, appliances);
				Map<Appliance, Double> applianceInTheLowerLevelAndDistances = getAllApplianceWithDistanceValues(appliance, applianceInTheLowerLevel);
				Appliance nearestAppliance = getXNearestapplication(1, appliance, applianceInTheLowerLevelAndDistances).get(0);
				appliance.setParentApp(nearestAppliance.getName()+"-app");
			} else {
				System.out.println("Cloud doesn't have parentApps");
			}
		}
		
	}
	
	
	public static Application generateApplication(int freq, int tasksize, String type, String parent, String instance) {
		Application application = new Application();
		application.setFreq(freq);
		application.setTasksize(tasksize);
		application.setParentDevice(parent);
		application.setName(application.getParentDevice()+"-app");
		application.setType(type);
		application.setInstance(instance);
		return application;
	}
	
	public static Devices createDevices() {
		Devices devices =  new Devices();
		devices.device = new ArrayList<hu.u_szeged.inf.fog.simulator.xmlgenerator.Devices.Device>();
		for (int x = 0; x < NUMBER_OF_STATIONS; x++) {
			devices.device.add(createDevice(x, 1, "distance", new Coord(RADIUS_OF_CIRCLE*5), 10000000, 1, 500));
		}
		return devices;
	}
	
	public static hu.u_szeged.inf.fog.simulator.xmlgenerator.Devices.Device createDevice(int id, int numberOfSensor, String strategy, Coord coord,
			int stopTime, int numberOfDevices, int fileSize){
		hu.u_szeged.inf.fog.simulator.xmlgenerator.Devices.Device device = new hu.u_szeged.inf.fog.simulator.xmlgenerator.Devices.Device();
		device.setName("station-"+id);
		device.setFreq(5100);
		device.setSensor(numberOfSensor);
		device.setMaxinbw((short) 1000);
		device.setMaxoutbw((short) 1000);
		device.setDiskbw((short) 1000);
		device.setReposize(60000);
		device.setRatio(1);
		device.setStrategy(strategy);
		device.setXCoord(coord.getX());
		device.setYCoord(coord.getY());
		
		
		Shutdown shutdown = new Shutdown();
		shutdown.setNumber((byte) 5);
		shutdown.setFrom(2);
		shutdown.setTo(8);
		device.setShutdown(shutdown);
		
		device.setStarttime(0);
		device.setStoptime(stopTime);
		device.setNumber((short) numberOfDevices);
		device.setFilesize(fileSize);
		
		return device;
	}
	
	
	
	public static void createStationsXML(Devices devices) {
		try {
			JAXBContext context = JAXBContext.newInstance(Devices.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			File outputFile = new File(LOCATION_OF_XML_OUTPUT+"/stations_output.xml");
			marshaller.marshal(devices, outputFile);
			
			System.out.println("Creating stations XML");
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	private static void createApplianceXMLFile(Appliances appliances) {
		
		try {
		JAXBContext context = JAXBContext.newInstance(Appliances.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		File outputfile = new File(LOCATION_OF_XML_OUTPUT+"/output3.xml");
		marshaller.marshal(appliances, outputfile);
		
		System.out.println("Creating appliances XML");
		
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	

	public static class Coord {
		private double x;
		private double y;
		
		public Coord(int radius) {
			double angle = Math.random() * Math.PI * 2;
			this.x = Math.cos(angle) * radius;
			this.y = Math.sin(angle) * radius;
		}
		
		public double getX() {
			return x;
		}
		
		public double getY() {
			return y;
		}
	}
	
	public static class CoordComparator implements Comparator<Entry<Appliance, Double>> {

		@Override
		public int compare(Entry<Appliance, Double> o1, Entry<Appliance, Double> o2) {
			if (o1.getValue() > o2.getValue()) {
				return 1;
			} else if (o1.getValue() < o2.getValue()) {
				return -1;
			} else {
				return 0;
			}
		}
	
	}
	
}
