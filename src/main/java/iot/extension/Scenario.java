package iot.extension;

import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import at.ac.uibk.dps.cloud.simulator.test.simple.cloud.vmscheduler.BasicSchedulingTest.AssertFulScheduler;
import org.w3c.dom.Node;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import iot.extension.Application.VmCollector;
import iot.extension.Station.Stationdata;
import providers.AmazonProvider;
import providers.BluemixProvider;
import providers.OracleProvider;
import providers.Provider;

/**
 * Az osztaly fo feladata az egyes Szcenariok lefuttasa, illetve logolasa
 *
 */
public class Scenario {
	private static ArrayList<Station> stations = new ArrayList<Station>();
	private static ArrayList<Cloud> clouds = new ArrayList<Cloud>();
	private static ArrayList<Application> app = new ArrayList<Application>();
	public static long scenscan = 0;
	public static long[] stationvalue; 
	private long simulatedTime;
	
	
	public static ArrayList<Application> getApp() {
		return app;
	}

	/**
	 * Feldolgozza a Station-oket leiro XML fajlt es elinditja a szimulaciot.
	 * @param va a virtualis kepfajl, default ertek hasznalathoz null-kent keruljon atadasra
	 * @param arc a VM eroforrasigenye, default ertek hasznalathoz null-kent keruljon atadasra
	 * @param datafile a Station-okat definialo XML fajl 
	 * @param cloudfile az IaaS felhot definialo XML fajl
	 * @param print logolasi funkcio, 1 - igen, 2 - nem
	 */
	public Scenario(VirtualAppliance va,AlterableResourceConstraints arc, String datafile,String cloudfile,String providerfile,int print,int cloudcount,long appfreq) throws Exception {
		long tasksize=-1; // TODO: ez miert kell?!
		stationvalue = new long[cloudcount];
		if(cloudcount<1){
			System.out.println("Cloudcount ertekenek legalabb 1-nek kell lennie!");
			System.exit(0);
		}
		
		if (datafile.isEmpty()) {
			System.out.println("Datafile nem lehet null");
			System.exit(0);
		} else {
			File fXmlFile = new File(datafile);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			NodeList NL = doc.getElementsByTagName("application");
			tasksize = Long.parseLong(NL.item(0).getAttributes().item(0).getNodeValue());
			 if(tasksize<=0){
				 System.out.println("rossz tasksize ertek! ");
					System.exit(0);
			 }
			NodeList nList = doc.getElementsByTagName("Station");
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					final long freq = Long.parseLong(eElement.getElementsByTagName("freq").item(0).getTextContent());
					if (freq <= 0) {
						System.out.println("rossz freq ertek! ");
						System.exit(0);
					}
					final long time = Long.parseLong(eElement.getElementsByTagName("time").item(0).getTextContent());
					this.simulatedTime = time;
					if (time < -1) {
						System.out.println("rossz time ertek! ");
						System.exit(0);
					}
					final long starttime = Long.parseLong(
							eElement.getElementsByTagName("time").item(0).getAttributes().item(0).getNodeValue());
					final long stoptime = Long.parseLong(
							eElement.getElementsByTagName("time").item(0).getAttributes().item(1).getNodeValue());
					if (starttime < 0 || (starttime > time && starttime < -1) || starttime > stoptime) {
						System.out.println("rossz starttime/stoptime ertek! ");
						System.exit(0);
					}
					final int snumber = Integer
							.parseInt(eElement.getElementsByTagName("snumber").item(0).getTextContent());
					if (snumber <= 0) {
						System.out.println("rossz snumber ertek! ");
						System.exit(0);
					}
					final int filesize=Integer.parseInt(eElement.getElementsByTagName("snumber").item(0)
							.getAttributes().item(0).getNodeValue());
					if (filesize < 1) {
						System.out.println("rossz filesize ertek! ");
						System.exit(0);
					}
					
					final long maxinbw = Long
							.parseLong(eElement.getElementsByTagName("maxinbw").item(0).getTextContent());
					if (maxinbw <= 0) {
						System.out.println("rossz maxinbw ertek! ");
						System.exit(0);
					}
					final long maxoutbw = Long
							.parseLong(eElement.getElementsByTagName("maxoutbw").item(0).getTextContent());
					if (maxoutbw <= 0) {
						System.out.println("rossz maxoutbw ertek! ");
						System.exit(0);
					}
					final long diskbw = Long
							.parseLong(eElement.getElementsByTagName("diskbw").item(0).getTextContent());
					if (diskbw <= 0) {
						System.out.println("rossz diskbw ertek! ");
						System.exit(0);
					}
					final long reposize = Long
							.parseLong(eElement.getElementsByTagName("reposize").item(0).getTextContent());
					if (reposize <= 0) {
						System.out.println("rossz reposize ertek! ");
						System.exit(0);
					}
					final int ratio = Integer.parseInt(eElement.getElementsByTagName("ratio").item(0).getTextContent());
					if (ratio < 1) {
						System.out.println("rossz ratio ertek! ");
						System.exit(0);
					}
					
					final int stationnumber=Integer.parseInt(eElement.getElementsByTagName("name")
								.item(0).getAttributes().item(0).getNodeValue());
					if (stationnumber < 1) {
						System.out.println("rossz stationnumber ertek! ");
						System.exit(0);
					}
					for(int i=0;i<stationnumber;i++){
						Stationdata sd = new Stationdata(time, starttime, stoptime, filesize, snumber, freq,
								eElement.getElementsByTagName("name").item(0).getTextContent()+" "+i,
								eElement.getElementsByTagName("torepo").item(0).getTextContent(), ratio);
						Scenario.stations.add(new Station(maxinbw, maxoutbw, diskbw, reposize, sd, false));
						
					}

					}
				}
			}
			
			if(tasksize!=-1){

				int maxstation = Scenario.stations.size() / cloudcount;
				for(int i=0;i<cloudcount;i++){
					Scenario.stationvalue[i]=0;
					Cloud cloud = new Cloud(null,null,cloudfile);
					Scenario.clouds.add(cloud);
					ArrayList<Station> stations = new ArrayList<Station>();
					int stationcounter=Scenario.stations.size()-1;
					while(stationcounter>=0){
						Scenario.stations.get(stationcounter).setCloud(cloud);
						Scenario.stations.get(stationcounter).setCloudnumber(i);
						stations.add(Scenario.stations.get(stationcounter));
						Scenario.stations.remove(stationcounter);
						stationcounter--;
						if(stations.size()>maxstation){
							break;
						}
					}
					app.add(new Application(appfreq,tasksize,true,print,cloud,stations,(i+1)+". app:"));
				}
				
				new BluemixProvider(providerfile, this.simulatedTime,"bluemix");
				new OracleProvider(providerfile, this.simulatedTime,"oracle");
				new AmazonProvider(providerfile, this.simulatedTime,"amazon");
				/*new AzureProvider();*/

				Provider.startProvider();
				Timed.simulateUntilLastEvent();
				
			}
			// hasznos infok:
			if(print==1){
				
				
				int i=0;
				int j=0;
				for(Application a : app){
					System.out.println(a.getName()+ " stations: "+ a.stations.size());
					for(VmCollector vmcl : a.vmlist){
						if(vmcl.worked){
							j++;
							i+=vmcl.tasknumber;
						}
					}
					for(VmCollector vmcl : a.vmlist){
						if(vmcl.worked && vmcl.tasknumber>0){
							System.out.println(vmcl.vm+" : "+vmcl.tasknumber);
						}
					}
					
					PrintWriter writer = new PrintWriter("tasks-"+a.stations.get(0).getCloudnumber()+".csv", "UTF-8");	
					for( Long s : a.tmap.keySet() )
					{
						writer.println(s + "," + a.tmap.get(s));

					}
					writer.close();
				}
				
				System.out.println("~~~~~~~~~~~~");
				System.out.println("VM: "+j + " tasks: "+i);
				System.out.println("~~~~~~~~~~~~");
				System.out.println("All filesize: "+Station.allstationsize);
				System.out.println("~~~~~~~~~~~~");
				System.out.println("Scneario finished at: "+Scenario.scenscan);
				System.out.println("~~~~~~~~~~~~");
				for(Cloud c : Scenario.clouds){
					System.out.println(c.getIaas().repositories.toString());
					for (PhysicalMachine p : c.getIaas().machines) {
						if ( p.localDisk.getFreeStorageCapacity() != p.localDisk.getMaxStorageCapacity()) {
							System.out.println(p);
						}
					}
					System.out.println("~~~~~~~~~~~~");
				}
				
			}
			
		}

		/**
		 * @param args Az elso argumentumkent adhato meg a Station-okat leiro XML eleresi utvonala
		 * 			masodik argumentumkent az IaaS-t leiro XML eleresi utvonala
		 * 			harmadik argumentumkent a provider-eket leiro XML fajl eleresi utvonala 
		 * 			negyedikkent egy szam, ami ha 1-es, akkor a logolasi funkcio be van kapcsolva
		 */
		public static void main(String[] args) throws Exception {
			String datafile=args[0];
			String cloudfile=args[1];
			String providerfile=args[2];
			int print=Integer.parseInt(args[3]);
			new Scenario(null,null,datafile,cloudfile,providerfile,print,1,60000);	
		}
}