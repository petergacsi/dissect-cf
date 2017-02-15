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

public abstract class Provider{
		
	@Override
	public String toString() {
		return "Provider [userCost=" + userCost + ", name=" + name + ", tierList=" + tierList + ", exchangeRate="
				+ exchangeRate + "]";
	}

	class Tier{
		private String tierName;
		private long mbFrom;
		private long mbTo;
		private double pricePerMonth;
		private double pricePerMB;
		private long devicePerMonth;
		private long messagesPerMonthPerDevice;
		private long messagesPerDay;
		private long messagesizePerKB;
		private double bofPrice;
		private long bofMessagecount;
		private long blockOfData;
		
		@Override
		public String toString() {
			return "Tier [tierName=" + tierName + ", mbFrom=" + mbFrom + ", mbTo=" + mbTo + ", pricePerMonth="
					+ pricePerMonth + ", pricePerMB=" + pricePerMB + ", devicePerMonth=" + devicePerMonth
					+ ", messagesPerMonthPerDevice=" + messagesPerMonthPerDevice + ", messagesPerDay=" + messagesPerDay
					+ ", messagesPerKB=" + messagesizePerKB + ", bofprice=" + bofPrice + ", bofMessagecount="
					+ bofMessagecount + ", blockOfData=" + blockOfData + "]";
		}
		protected String getTierName() {
			return tierName;
		}
		protected void setTierName(String tierName) {
			this.tierName = tierName;
		}
		protected long getMbFrom() {
			return mbFrom;
		}
		protected void setMbFrom(long mbFrom) {
			this.mbFrom = mbFrom;
		}
		protected long getMbTo() {
			return mbTo;
		}
		protected void setMbTo(long mbTo) {
			this.mbTo = mbTo;
		}
		protected double getPricePerMonth() {
			return pricePerMonth;
		}
		protected void setPricePerMonth(double pricePerMonth) {
			this.pricePerMonth = pricePerMonth;
		}
		protected double getPricePerMB() {
			return pricePerMB;
		}
		protected void setPricePerMB(double pricePerMB) {
			this.pricePerMB = pricePerMB;
		}
		protected long getDevicePerMonth() {
			return devicePerMonth;
		}
		protected void setDevicePerMonth(long devicePerMonth) {
			this.devicePerMonth = devicePerMonth;
		}
		protected long getMessagesPerMonthPerDevice() {
			return messagesPerMonthPerDevice;
		}
		protected void setMessagesPerMonthPerDevice(long messagesPerMonthPerDevice) {
			this.messagesPerMonthPerDevice = messagesPerMonthPerDevice;
		}
		protected long getMessagesPerDay() {
			return messagesPerDay;
		}
		protected void setMessagesPerDay(long messagesPerDay) {
			this.messagesPerDay = messagesPerDay;
		}
		protected long getMessagesizePerKB() {
			return messagesizePerKB;
		}
		protected void setMessagesizePerKB(long messagesizePerKB) {
			this.messagesizePerKB = messagesizePerKB;
		}
		protected double getBofPrice() {
			return bofPrice;
		}
		protected void setBofprice(double bofprice) {
			this.bofPrice = bofprice;
		}
		protected long getBofMessagecount() {
			return bofMessagecount;
		}
		protected void setBofMessagecount(long bofMessagecount) {
			this.bofMessagecount = bofMessagecount;
		}
		protected long getBlockOfData() {
			return blockOfData;
		}
		protected void setBlockOfData(long blockOfData) {
			this.blockOfData = blockOfData;
		}
	}
	
	private double userCost;
	private String name;
	protected ArrayList<Tier> tierList;
	private double exchangeRate;
	
	protected Provider(){
		this.tierList = new ArrayList<Tier>();
		this.userCost = 0.0;
	}
	
	protected double getExchangeRate() {
		return exchangeRate;
	}

	protected void setExchangeRate(double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	protected double getUserCost() {
		return userCost;
	}

	protected void setUserCost(double userCost) {
		this.userCost = userCost;
	}

	protected String getName() {
		return name;
	}

	protected void setName(String name) {
		this.name = name;
	}

	protected ArrayList<Tier> getTierList() {
		return tierList;
	}

	protected void setTierList(ArrayList<Tier> tierList) {
		this.tierList = tierList;
	}
	
	protected abstract void costCounter(int filsize);
	
	public static void readProviderXml(String datafile, String providername,String tiername,int filesize) throws ParserConfigurationException, SAXException, IOException {
		
		File fXmlFile = new File(datafile);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("provider");
		for (int temp = 0; temp < nList.getLength(); temp++) {
				CloudsProvider cp = new CloudsProvider(datafile, providername, tiername);
				
				cp.setName(nList.item(temp).getAttributes().item(1).getNodeValue());
				cp.setExchangeRate(Double.parseDouble(nList.item(temp).getAttributes().item(0).getNodeValue()));
				
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					NodeList nList2 = eElement.getElementsByTagName("tier");
					for (int temp2 = 0; temp2 < nList2.getLength(); temp2++) {
						Node nNode2 = nList2.item(temp2);
						if (nNode2.getNodeType() == Node.ELEMENT_NODE) {
							Tier t  = cp.new Tier();
							Element eElement2 = (Element) nNode2;
							t.setMbFrom(Long.parseLong(eElement2.getAttributes().item(0).getNodeValue()));
							long tmp =Long.parseLong(eElement2.getAttributes().item(1).getNodeValue());
							t.setMbTo(tmp==-1? Long.MAX_VALUE : tmp);
							t.setTierName(eElement2.getAttributes().item(2).getNodeValue());
							
							t.setDevicePerMonth(Long.parseLong(eElement2.getElementsByTagName("device-per-month").item(0).getTextContent()));
							t.setMessagesPerMonthPerDevice(Long.parseLong(eElement2.getElementsByTagName("messages-per-month-per-device").item(0).getTextContent()));
							t.setPricePerMonth(Double.parseDouble(eElement2.getElementsByTagName("price-per-month").item(0).getTextContent()));
							t.setPricePerMB(Double.parseDouble(eElement2.getElementsByTagName("price-per-MB").item(0).getTextContent()));
							t.setMessagesPerDay(Long.parseLong(eElement2.getElementsByTagName("messages-per-day").item(0).getTextContent()));
							t.setMessagesizePerKB(Long.parseLong(eElement2.getElementsByTagName("messagesize-per-KB").item(0).getTextContent()));
							t.setBlockOfData(Long.parseLong(eElement2.getElementsByTagName("block-of-data").item(0).getTextContent()));
							t.setBofprice(Double.parseDouble(eElement2.getElementsByTagName("block-of-data").item(0).getAttributes().item(1).getNodeValue()));
							t.setBofMessagecount(Long.parseLong(eElement2.getElementsByTagName("block-of-data").item(0).getAttributes().item(0).getNodeValue()));
							
							cp.tierList.add(t);
						}
					}
				}
				//System.out.println(cp);
				if(providername==null || providername.equals(cp.getName())){
					cp.costCounter(filesize);
				}
				
			
		}
	}
}
