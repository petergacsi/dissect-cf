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

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import iot.extension.Station;

public abstract class Provider extends Timed{
	protected  class Bluemix{
		double mbto;
		double mbfrom;
		double price;
		
		public Bluemix(double mbto, double mbfrom, double price) {
			this.mbto=mbto;
			this.mbfrom=mbfrom;
			this.price=price;
		}
	}
	
	public Provider(long simulatedTime){
		this.userIotCost=0.0;
		this.userCloudCost=0.0;
		this.pricePerMB=0.0;
		this.blockOfData=0;
		this.exchangeRate=0.0;
		this.bofMessagecount=0;
		this.bofPrice=0.0;
		this.devicepricePerMonth=0.0;
		this.messagesPerMonthPerDevice=0;
		this.amDevicepricePerMonth=0.0;
		this.amMessagesPerMonthPerDevice=0;
		this.pricePerMonth=0.0;
		this.messagesPerDay=0;
		this.messagesizePerKB=0;
		this.period=0;
		this.freq=simulatedTime;
		this.bmList=new ArrayList<Bluemix>();
	}
	
	public void startProvider(){
		
		subscribe(this.freq);
		//subscribe(this.getFreq());
	}
	
	public void stopProvider(){
		new DeferredEvent(Provider.lateStart){
			@Override
			protected void eventAction() {
				unsubscribe();
			}
		};
		System.out.println(this.toString());
		System.out.println("~~~~~~~~~~~~");
	}
	
	private static ArrayList<Provider> providerList = new ArrayList<Provider>();
	
	
	@Override
	public String toString() {
		return "Provider [instancePrice=" + instancePrice + ", gbHourPrice=" + gbHourPrice + ", filesize=" + filesize
				+ ", freq=" + freq + ", userIotCost=" + userIotCost + ", userCloudCost=" + userCloudCost
				+ ", pricePerMB=" + pricePerMB + ", blockOfData=" + blockOfData + ", exchangeRate=" + exchangeRate
				+ ", bofMessagecount=" + bofMessagecount + ", bofPrice=" + bofPrice + ", devicepricePerMonth="
				+ devicepricePerMonth + ", messagesPerMonthPerDevice=" + messagesPerMonthPerDevice
				+ ", amDevicepricePerMonth=" + amDevicepricePerMonth + ", amMessagesPerMonthPerDevice="
				+ amMessagesPerMonthPerDevice + ", period=" + period + ", pricePerMonth=" + pricePerMonth
				+ ", messagesPerDay=" + messagesPerDay + ", messagesizePerKB=" + messagesizePerKB + ", bmList=" + bmList
				+ "]";
	}

	// Cloud side variables
	/* private double cpu; TODO: currently not necessary
	private long memory; */
	private double instancePrice;
	private double gbHourPrice;

	// IoT side variables
	public static long lateStart;
	protected int filesize;
	protected long freq;
	private double userIotCost;
	private double userCloudCost;
	private double pricePerMB;

	private long blockOfData;
	private double exchangeRate;
	private long bofMessagecount;
	private double bofPrice;

	private double devicepricePerMonth;
	private long messagesPerMonthPerDevice;
	private double amDevicepricePerMonth;
	private long amMessagesPerMonthPerDevice;
	private long period;

	private double pricePerMonth;
	private long messagesPerDay;
	private long messagesizePerKB;

	protected ArrayList<Bluemix> bmList;
	public static ArrayList<Provider> getProviderList() {
		return providerList;
	}

		
	protected double getInstancePrice() {
		return instancePrice;
	}

	protected void setInstancePrice(double instancePrice) {
		this.instancePrice = instancePrice;
	}

	protected double getGbHourPrice() {
		return gbHourPrice;
	}

	protected void setGbHourPrice(double gbHourPrice) {
		this.gbHourPrice = gbHourPrice;
	}

	protected long getFreq() {
		return freq;
	}

	protected void setFreq(long freq) {
		this.freq = freq;
	}

	protected double getUserIotCost() {
		return userIotCost;
	}

	protected void setUserIotCost(double userCost) {
		this.userIotCost = userCost;
	}
	protected double getUserCloudCost() {
		return userCloudCost;
	}

	protected void setUserCloudCost(double userCost) {
		this.userCloudCost = userCost;
	}

	protected double getPricePerMB() {
		return pricePerMB;
	}

	protected void setPricePerMB(double pricePerMB) {
		this.pricePerMB = pricePerMB;
	}

	protected long getBlockOfData() {
		return blockOfData;
	}

	protected void setBlockOfData(long blockOfData) {
		this.blockOfData = blockOfData;
	}

	protected double getExchangeRate() {
		return exchangeRate;
	}

	protected void setExchangeRate(double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}

	protected long getBofMessagecount() {
		return bofMessagecount;
	}

	protected void setBofMessagecount(long bofMessagecount) {
		this.bofMessagecount = bofMessagecount;
	}

	protected double getBofPrice() {
		return bofPrice;
	}

	protected void setBofPrice(double bofPrice) {
		this.bofPrice = bofPrice;
	}

	protected double getDevicepricePerMonth() {
		return devicepricePerMonth;
	}

	protected void setDevicepricePerMonth(double devicepricePerMonth) {
		this.devicepricePerMonth = devicepricePerMonth;
	}

	protected long getMessagesPerMonthPerDevice() {
		return messagesPerMonthPerDevice;
	}

	protected void setMessagesPerMonthPerDevice(long messagesPerMonthPerDevice) {
		this.messagesPerMonthPerDevice = messagesPerMonthPerDevice;
	}

	protected double getAmDevicepricePerMonth() {
		return amDevicepricePerMonth;
	}

	protected void setAmDevicepricePerMonth(double amDevicepricePerMonth) {
		this.amDevicepricePerMonth = amDevicepricePerMonth;
	}

	protected long getAmMessagesPerMonthPerDevice() {
		return amMessagesPerMonthPerDevice;
	}

	protected void setAmMessagesPerMonthPerDevice(long amMessagesPerMonthPerDevice) {
		this.amMessagesPerMonthPerDevice = amMessagesPerMonthPerDevice;
	}

	protected double getPricePerMonth() {
		return pricePerMonth;
	}

	protected void setPricePerMonth(double pricePerMonth) {
		this.pricePerMonth = pricePerMonth;
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

	protected long getPeriod() {
		return period;
	}

	protected void setPeriod(long period) {
		this.period = period;
	}

	protected abstract void IotCostCounter(int filesize);
	protected abstract void CloudCostCounter();
	
	private static <P extends Provider> void readCProviderXml(P p,String datafile) throws ParserConfigurationException, SAXException, IOException{
		File fXmlFile = new File(datafile);
		NodeList nList;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		
		nList = doc.getElementsByTagName("gbhour-per-price");
		p.setGbHourPrice(Double.parseDouble(nList.item(0).getTextContent()));
		nList = doc.getElementsByTagName("instance-price");
		p.setInstancePrice(Double.parseDouble(nList.item(0).getTextContent()));
	}
	
	public static <P extends Provider> void readProviderXml(P p, String provider,String cprovider, int filesize)
			throws ParserConfigurationException, SAXException, IOException {
		p.filesize=filesize;
		File fXmlFile = new File(provider);
		NodeList nList;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();

		nList = doc.getElementsByTagName("amazon");
		if(nList.item(0).getAttributes().item(0).getNodeValue().equals("true")){
			p.setExchangeRate(Double.parseDouble(nList.item(0).getAttributes().item(1).getNodeValue()));
			p.setBofMessagecount(Long.parseLong(nList.item(0).getAttributes().item(2).getNodeValue()));
			p.setBofPrice(Double.parseDouble(nList.item(0).getAttributes().item(3).getNodeValue()));
			p.setBlockOfData(Long.parseLong(nList.item(0).getTextContent()));
		}

		nList = doc.getElementsByTagName("oracle");
		if(nList.item(0).getAttributes().item(0).getNodeValue().equals("true")){
			p.setPeriod(Long.parseLong(nList.item(0).getAttributes().item(1).getNodeValue()));
			p.setFreq(p.getPeriod()*24*60*60*1000);
			nList = doc.getElementsByTagName("messages-per-month-per-device");
			p.setMessagesPerMonthPerDevice(Long.parseLong(nList.item(0).getTextContent()));
			nList = doc.getElementsByTagName("deviceprice-per-month");
			p.setDevicepricePerMonth(Double.parseDouble(nList.item(0).getTextContent()));
			nList = doc.getElementsByTagName("am-messages-per-month-per-device");
			p.setAmMessagesPerMonthPerDevice(Long.parseLong(nList.item(0).getTextContent()));
			nList = doc.getElementsByTagName("am-deviceprice-per-month");
			p.setAmDevicepricePerMonth(Double.parseDouble(nList.item(0).getTextContent()));
		}
		
		nList = doc.getElementsByTagName("azure");
		if(nList.item(0).getAttributes().item(0).getNodeValue().equals("true")){
			p.setPeriod(Long.parseLong(nList.item(0).getAttributes().item(1).getNodeValue()));
			p.setFreq(p.getPeriod()*24*60*60*1000);
			nList = doc.getElementsByTagName("price-per-month");
			p.setPricePerMonth(Double.parseDouble(nList.item(0).getTextContent()));
			nList = doc.getElementsByTagName("messages-per-day");
			p.setMessagesPerDay(Long.parseLong(nList.item(0).getTextContent()));
			nList = doc.getElementsByTagName("messagesize-per-KB");
			p.setMessagesizePerKB(Long.parseLong(nList.item(0).getTextContent()));
		}

		nList = doc.getElementsByTagName("bluemix");
		
		if(nList.item(0).getAttributes().item(0).getNodeValue().equals("true")){
			nList = doc.getElementsByTagName("price-per-MB");

			double price, mbfrom, mbto;
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				mbfrom = Double.parseDouble(nNode.getAttributes().item(0).getTextContent());
				mbto = Double.parseDouble(nNode.getAttributes().item(1).getTextContent());
				price = Double.parseDouble(nNode.getTextContent());
				
				if (mbto == -1) {
					mbto = Double.MAX_VALUE;
				}
				System.out.println(mbto +" "+mbfrom+" "+price);
				p.bmList.add(p.new Bluemix(mbto, mbfrom, price));
			}
		}
		if(cprovider!=null){
			Provider.readCProviderXml(p,cprovider);
		}
		//System.out.println(p);
		Provider.providerList.add(p);
	}
}
