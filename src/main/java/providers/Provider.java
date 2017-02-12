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
		
	class Tier{

		@Override
		public String toString() {
			return "Tier [tierName=" + tierName + ", mbFrom=" + mbFrom + ", mbTo=" + mbTo + ", constantFee="
					+ constantFee + ", price=" + price + ", devicecount=" + devicecount + ", messagecount="
					+ messagecount + ", blockofdata=" + blockofdata + "]";
		}
		
		private void setTierName(String tierName) {
			this.tierName = tierName;
		}
		private void setMbFrom(long mbFrom) {
			this.mbFrom = mbFrom;
		}
		private void setMbTo(long mbTo) {
			this.mbTo = mbTo;
		}
		private void setConstantFee(boolean constantFee) {
			this.constantFee = constantFee;
		}
		private void setPrice(double price) {
			this.price = price;
		}
		private void setDevicecount(long devicecount) {
			this.devicecount = devicecount;
		}
		private void setMessagecount(long messagecount) {
			this.messagecount = messagecount;
		}
		private void setBlockofdata(long blockofdata) {
			this.blockofdata = blockofdata;
		}
		
		

		protected String getTierName() {
			return tierName;
		}

		protected long getMbFrom() {
			return mbFrom;
		}

		protected long getMbTo() {
			return mbTo;
		}

		protected boolean isConstantFee() {
			return constantFee;
		}

		protected double getPrice() {
			return price;
		}

		protected long getDevicecount() {
			return devicecount;
		}

		protected long getMessagecount() {
			return messagecount;
		}

		protected long getBlockofdata() {
			return blockofdata;
		}



		private String tierName;
		private long mbFrom;
		private long mbTo;
		private boolean constantFee;
		
		private double price;
		private long devicecount;
		private long messagecount;
		private long blockofdata;
	}
	
	
protected abstract Tier searchCategory();
	
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

	public double getUserCost() {
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
			System.out.println("~~~~~~~~~~~~");
			System.out.println(p);
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
			
			
			p.setName(nList.item(temp).getAttributes().item(0).getNodeValue());
			p.setFreq(Long.parseLong(nList.item(temp).getAttributes().item(1).getNodeValue())*86400000);
			
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
						t.setMbFrom(Long.parseLong(eElement2.getAttributes().item(1).getNodeValue()));
						long tmp =Long.parseLong(eElement2.getAttributes().item(2).getNodeValue());
						t.setMbTo(tmp==-1? Long.MAX_VALUE : tmp);
						t.setTierName(eElement2.getAttributes().item(2).getNodeValue());
						t.setPrice(Double.parseDouble(eElement2.getElementsByTagName("price").item(0).getTextContent()));
						t.setDevicecount(Long.parseLong(eElement2.getElementsByTagName("devicecount").item(0).getTextContent()));
						t.setMessagecount(Long.parseLong(eElement2.getElementsByTagName("messagecount").item(0).getTextContent()));
						t.setBlockofdata(Long.parseLong(eElement2.getElementsByTagName("blockofdata").item(0).getTextContent()));
						
						p.tierList.add(t);
						
					}
				}
			}
		}
		Provider.providerList.add(p);
	}
}
