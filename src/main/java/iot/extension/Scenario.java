package iot.extension;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cloudprovider.ResourceDependentProvider;

import org.w3c.dom.Node;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import iot.extension.Application.VmCollector;
import iot.extension.Station.Stationdata;
import iotprovider.CloudsProvider;
import iotprovider.Provider;

/**
  *  Main task of this class to run the whole simulation based on XML files.
  *  Az osztaly fo feladata, hogy lefuttassa a teljes szimulaciot XML fajlok alapjan.
  */
public class Scenario {
	
	private static int filesize;
	private static long simulatedTime;
	

	private void loging() throws FileNotFoundException, UnsupportedEncodingException{
			int i=0;
			int j=0;
			for(Application a : Application.getApp()){
				System.out.println(a.getName()+ " stations: "+ a.stations.size());
				for(VmCollector vmcl : a.vmlist){
					if(vmcl.worked){
						j++;
						i+=vmcl.tasknumber;
					}
				}
				
				for(VmCollector vmcl : a.vmlist){						
						if(vmcl.isWorked()){
							//System.out.println(vmcl.vm+" : "+vmcl.tasknumber+" : "+vmcl.workingTime);
						}
						System.out.println(vmcl.vm+" : "+vmcl.tasknumber+" : "+vmcl.workingTime+" : "+vmcl.letehozva+ " : "+ vmcl.pm);
				}
				
				
				/*PrintWriter writer = new PrintWriter("src/main/java/iot/extension/experiments/scenario3/task-5L4"+".csv", "UTF-8");	
				for( Long s : a.tmap.keySet() )
				{
					writer.println(s + "," + a.tmap.get(s));

				}
				writer.close();*/
			}
			
			System.out.println("~~~~~~~~~~~~");
			System.out.println("VM: "+j + " tasks: "+i);
			System.out.println("~~~~~~~~~~~~");
			System.out.println("All filesize: "+Station.allstationsize);
			System.out.println("~~~~~~~~~~~~");
			System.out.println("Scneario finished at: "+Application.getFinishedTime());
			System.out.println("~~~~~~~~~~~~");
			for(Cloud c : Cloud.getClouds()){
				System.out.println(c.getIaas().repositories.toString());
				for (PhysicalMachine p : c.getIaas().machines) {
					if ( p.localDisk.getFreeStorageCapacity() != p.localDisk.getMaxStorageCapacity()) {
						System.out.println(p);
					}
				}
				System.out.println("~~~~~~~~~~~~");
		}
	}
	
	
	private static void readStationXml(String stationfile,String cloudfile,ArrayList<String> arrayOfProviderfiles,String cproviderfile,int print,long appfreq) throws SAXException, IOException, ParserConfigurationException, NetworkException{
		long tasksize=-1; // TODO: ez miert kell?!

		Station.setStationvalue(new long[arrayOfProviderfiles.size()]); 
		if(arrayOfProviderfiles.size()<1){
			System.out.println("Cloudcount ertekenek legalabb 1-nek kell lennie!");
			System.exit(0);
		}
		
		if (stationfile.isEmpty()) {
			System.out.println("Datafile nem lehet null");
			System.exit(0);
		} else {
			File fXmlFile = new File(stationfile);
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
					
					String strategy = eElement.getElementsByTagName("strategy").item(0).getTextContent();
							
					
					final long time = Long.parseLong(eElement.getElementsByTagName("time").item(0).getTextContent());
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
					Scenario.filesize=filesize;
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
						Station.getStations().add(new Station(maxinbw, maxoutbw, diskbw, reposize, sd, false,strategy));
						
					}
					Scenario.simulatedTime=(Scenario.simulatedTime<time)?time:Scenario.simulatedTime;
					}
				}
			}
			// TODO: TIMED
			if(tasksize!=-1 && Timed.getFireCount()==1){
				
				ArrayList<ArrayList<Station>> stationok = new ArrayList<ArrayList<Station>>();
				/*
				 *  letrehozzuk az xml fajlok alapjan az iot providereket es a felhoket a megfelelo vm eroforrasokkal
				 */
				for(int i=0;i<arrayOfProviderfiles.size();++i) {
					CloudsProvider cp1 = new CloudsProvider(Scenario.simulatedTime);
					Provider.readProviderXml(cp1, arrayOfProviderfiles.get(i),cproviderfile,filesize);
					AlterableResourceConstraints arc1 = new AlterableResourceConstraints(cp1.getCpu(),0.001,cp1.getMemory());
					new Cloud(cloudfile,null,null,arc1);
					stationok.add(new ArrayList<Station>());
				}
					
					
				for(int i=0;i<arrayOfProviderfiles.size();++i) {
					
					
					for(Station s : Station.getStations()){
						if(s.strategy.equals("random") && s.founded==false){
							s.founded=true;
							Random randomGenerator = new Random();
							int rnd = randomGenerator.nextInt(arrayOfProviderfiles.size());
							stationok.get(rnd).add(s);
							s.setCloud(Cloud.getClouds().get(rnd));
							s.setCloudnumber(rnd);

						}/*else if(s.strategy.equals("cost")) {
							if(cp1.instancePrice<cp2.instancePrice) {
								stations1.add(s);
								s.setCloud(cloud);
								s.setCloudnumber(0);
							}else if(cp1.hourPrice<cp2.hourPrice) {
								stations1.add(s);
								s.setCloud(cloud);
								s.setCloudnumber(0);
							}else {
								stations2.add(s);
								s.setCloud(cloud2);
								s.setCloudnumber(1);
							}
							
						}*/
						
					}
					

				}
				for(int i=0;i<arrayOfProviderfiles.size();++i) {
					if(stationok.get(i).size()>0){
						Application.getApp().add(new Application(appfreq,tasksize,true,print,Cloud.getClouds().get(i),stationok.get(i),(i+1)+". app:",Provider.getProviderList().get(i)));
					}
				}
				
	
				
			}
		
	}

	/**
	 * Feldolgozza a Station-oket leiro XML fajlt es elinditja a szimulaciot.
	 * @param va a virtualis kepfajl, default ertek hasznalathoz null-kent keruljon atadasra
	 * @param arc a VM eroforrasigenye, default ertek hasznalathoz null-kent keruljon atadasra
	 * @param datafile a Station-okat definialo XML fajl 
	 * @param cloudfile az IaaS felhot definialo XML fajl
	 * @param print logolasi funkcio, 1 - igen, 2 - nem
	 */
	public Scenario(String datafile,String cloudfile,ArrayList<String> arrayOfProviderfiles,String cproviderfile,int print,long appfreq) throws Exception {
			Scenario.readStationXml(datafile, cloudfile, arrayOfProviderfiles, cproviderfile, print, appfreq);	
			Timed.simulateUntilLastEvent();
			if(print==1) this.loging();
		}

		/**
		 * @param args Az elso argumentumkent adhato meg a Station-okat leiro XML eleresi utvonala
		 * 			masodik argumentumkent az IaaS-t leiro XML eleresi utvonala
		 * 			harmadik argumentumkent a provider-eket leiro XML fajl eleresi utvonala 
		 * 			negyedikkent egy szam, ami ha 1-es, akkor a logolasi funkcio be van kapcsolva
		 */
		public static void main(String[] args) throws Exception {
			/*String datafile=args[0];
			String cloudfile=args[1];
			String providerfile=args[2];
			String providerfile2=args[3];
			String cproviderfile=args[4];
			int print=Integer.parseInt(args[5]);*/
			String datafile="//home//andras//Documents//projektek//dissect-cf//src//main//resources//WeatherStation.xml";
			String cloudfile="//home//andras//Documents//projektek//dissect-cf//src//main//resources//LPDSCloud.xml";
			String providerfile="//home//andras//Documents//projektek//dissect-cf//src//main//resources//Provider.xml";
			String providerfile2="//home//andras//Documents//projektek//dissect-cf//src//main//resources//Provider2.xml";
			String providerfile3="//home//andras//Documents//projektek//dissect-cf//src//main//resources//Provider3.xml";
			String cproviderfile="//home//andras//Documents//projektek//dissect-cf//src//main//resources//CProvider.xml";
			ArrayList<String> arrayOfProviderfiles = new ArrayList<String>();
			arrayOfProviderfiles.add(providerfile);
			arrayOfProviderfiles.add(providerfile2);
			arrayOfProviderfiles.add(providerfile3);
			
			new Scenario(datafile,cloudfile,arrayOfProviderfiles,cproviderfile,1,5*60000);	
			
			
			Timed.simulateUntilLastEvent();
		}
		
		
	
		
		
		
}