package providers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import iot.extension.Scenario;
import iot.extension.Station;
import iot.extension.Station.Stationdata;

public class Provider extends Timed{
	
	private static ArrayList<Provider> providerList = new ArrayList<Provider>();
	
	private long freq;
	private String name;
	private long startTime;
	private long stopTime;
	
	/**
	 * Should not be called directly
	 * @param freq
	 * @param name
	 */
	protected Provider(long freq,String name){
		this.freq=freq;
		this.name=name;
	}
	
	protected void startProvider(){
		subscribe(this.freq);
		this.startTime=Timed.getFireCount();
	}

	protected void stopProvider(){
		unsubscribe();
		this.stopTime=Timed.getFireCount();
	}
	
	@Override
	public void tick(long fires) {
		// TODO Auto-generated method stub
		
	}

	
	public static void readProviderXml(String datafile) throws ParserConfigurationException, SAXException, IOException {
		File fXmlFile = new File(datafile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("provider");
		
		for (int temp = 0; temp < nList.getLength(); temp++) {
			System.out.println("provider-name: "+nList.item(temp).getAttributes().item(0).getNodeValue());
			System.out.println("tiercount: "+nList.item(temp).getAttributes().item(1).getNodeValue());
			
			Node nNode = nList.item(temp);
			
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				NodeList nList2 = eElement.getElementsByTagName("tier");
				for (int temp2 = 0; temp2 < nList2.getLength(); temp2++) {
					Node nNode2 = nList2.item(temp2);
					if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement2 = (Element) nNode2;
						System.out.println(eElement2.getAttributes().item(0));//.getNodeValue());
						System.out.println(eElement2.getAttributes().item(1));//.getNodeValue());
						System.out.println(eElement2.getAttributes().item(2));//.getNodeValue());
						System.out.println(eElement2.getAttributes().item(3));//.getNodeValue());
						System.out.println(eElement2.getElementsByTagName("device-per-month").item(0).getTextContent());
					}
				}
			}
		}
	}

}
