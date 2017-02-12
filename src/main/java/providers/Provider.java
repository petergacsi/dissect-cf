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

public abstract class Provider extends Timed{
	
	@Override
	public String toString() {
		return "Provider [name=" + name + ", freq=" + freq + ", startTime=" + startTime + ", stopTime=" + stopTime
				+ ", tierList=" + tierList + "]";
	}

	protected static ArrayList<Provider> getProviderList() {
		return providerList;
	}

	protected String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	protected long getFreq() {
		return freq;
	}

	private void setFreq(long freq) {
		this.freq = freq;
	}

	private double getUserCost() {
		return userCost;
	}

	protected void setUserCost(double userCost) {
		this.userCost = userCost;
	}

	protected ArrayList<Tier> getTierList() {
		return tierList;
	}

	private static ArrayList<Provider> providerList = new ArrayList<Provider>();
	private double userCost = 0.0;
	private String name;
	private long freq;
	private long startTime;
	private long stopTime;
	private ArrayList<Tier> tierList;
	
	class Tier{
		@Override
		public String toString() {
			return "Tier [tierName=" + tierName + ", mbFrom=" + mbFrom + ", mbTo=" + mbTo + ", constantFee="
					+ constantFee + ", devicePerMonth=" + devicePerMonth + ", messagesPerMonthPerDevice="
					+ messagesPerMonthPerDevice + ", pricePerMonth=" + pricePerMonth + ", pricePerMb=" + pricePerMb
					+ ", messagesPerDay=" + messagesPerDay + ", messagesizePerUnitInKb=" + messagesizePerUnitInKb
					+ ", blockOfData=" + blockOfData + ", bodPrize=" + bodPrize + ", bodmessagenumber="
					+ bodmessagenumber + "]";
		}
		private void setTierName(String tierName) {
			this.tierName = tierName;
		}
		private void setMbFrom(int mbFrom) {
			this.mbFrom = mbFrom;
		}
		private void setMbTo(int mbTo) {
			this.mbTo = mbTo;
		}
		private void setConstantFee(boolean constantFee) {
			this.constantFee = constantFee;
		}
		private void setDevicePerMonth(int devicePerMonth) {
			this.devicePerMonth = devicePerMonth;
		}
		private void setMessagesPerMonthPerDevice(int messagesPerMonthPerDevice) {
			this.messagesPerMonthPerDevice = messagesPerMonthPerDevice;
		}
		private void setPricePerMonth(double pricePerMonth) {
			this.pricePerMonth = pricePerMonth;
		}
		private void setPricePerMb(double pricePerMb) {
			this.pricePerMb = pricePerMb;
		}
		private void setMessagesPerDay(int messagesPerDay) {
			this.messagesPerDay = messagesPerDay;
		}
		private void setMessagesizePerUnitInKb(int messagesPerUnitInKb) {
			this.messagesizePerUnitInKb = messagesPerUnitInKb;
		}
		private void setBlockOfData(int blockOfData) {
			this.blockOfData = blockOfData;
		}
		private void setBodPrize(double bodPrize) {
			this.bodPrize = bodPrize;
		}
		private void setBodmessagenumber(int bodmessagenumber) {
			this.bodmessagenumber = bodmessagenumber;
		}
		private String tierName;
		private int mbFrom;
		private int mbTo;
		private boolean constantFee;
		
		private int devicePerMonth;
		private int messagesPerMonthPerDevice;
		private double pricePerMonth;
		private double pricePerMb;
		private int messagesPerDay;
		private int messagesizePerUnitInKb;
		private int blockOfData;
		private double bodPrize;
		private int bodmessagenumber;
	}
	/**
	 * Should not be called directly
	 * @param freq
	 * @param name
	 */
	protected Provider(){
		this.tierList = new ArrayList<Tier>();
	}
	
	public static void startProvider(){
		for(Provider p : providerList){
			p.subscribe(p.freq);
			p.startTime=Timed.getFireCount();
		}
	}

	public static void stopProvider(){
		for(Provider p : providerList){
			p.unsubscribe();
			p.stopTime=Timed.getFireCount();
		}
	}
	
	protected <P extends Provider> void readProviderXml(String datafile,P p) throws ParserConfigurationException, SAXException, IOException {
		File fXmlFile = new File(datafile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("provider");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			
			
			p.setName(nList.item(temp).getAttributes().item(1).getNodeValue());
			p.setFreq(Long.parseLong(nList.item(temp).getAttributes().item(0).getNodeValue())*86400000);
			
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				NodeList nList2 = eElement.getElementsByTagName("tier");
				for (int temp2 = 0; temp2 < nList2.getLength(); temp2++) {
					Node nNode2 = nList2.item(temp2);
					if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
						Tier t  = p.new Tier();
						Element eElement2 = (Element) nNode2;
						t.setConstantFee(((Integer.parseInt(eElement2.getAttributes().item(0).getNodeValue()) == 1 ) ? true : false));
						t.setMbFrom(Integer.parseInt(eElement2.getAttributes().item(1).getNodeValue()));
						t.setMbTo(Integer.parseInt(eElement2.getAttributes().item(2).getNodeValue()));
						t.setTierName(eElement2.getAttributes().item(2).getNodeValue());
						t.setDevicePerMonth(Integer.parseInt(eElement2.getElementsByTagName("device-per-month").item(0).getTextContent()));
						t.setMessagesPerMonthPerDevice(Integer.parseInt(eElement2.getElementsByTagName("messages-per-month-per-device").item(0).getTextContent()));
						t.setPricePerMonth(Double.parseDouble(eElement2.getElementsByTagName("price-per-month").item(0).getTextContent()));//f
						t.setPricePerMb(Double.parseDouble(eElement2.getElementsByTagName("price-per-MB").item(0).getTextContent()));//f
						t.setMessagesPerDay(Integer.parseInt(eElement2.getElementsByTagName("messages-per-day").item(0).getTextContent()));
						t.setMessagesizePerUnitInKb(Integer.parseInt(eElement2.getElementsByTagName("messagesize-per-KB").item(0).getTextContent()));
						t.setBlockOfData(Integer.parseInt(eElement2.getElementsByTagName("block-of-data").item(0).getTextContent()));
						t.setBodmessagenumber(Integer.parseInt(eElement2.getElementsByTagName("block-of-data").item(0).getAttributes().item(0).getNodeValue()));
						t.setBodPrize(Double.parseDouble(eElement2.getElementsByTagName("block-of-data").item(0).getAttributes().item(1).getNodeValue())); //f
						p.tierList.add(t);
					}
				}
			}
		System.out.println(p);
		}
	}

}
