package cloudprovider;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;

public class MyProvider extends CloudPricing {
	
	public static final long ticUnit = 60*60*1000;
	IaaSService iaas;
	// Cloud side variables
	private double Scpu;
	private long Smemory; 
	private double SinstancePrice;
	private double ShourPrice;
	

	private double Mcpu;
	private long Mmemory; 
	private double MinstancePrice;
	private double MhourPrice;
	
	private double Lcpu;
	private long Lmemory; 
	private double LinstancePrice;
	private double LhourPrice;


	public IaaSService getIaas() {
		return iaas;
	}

	public void setIaas(IaaSService iaas) {
		this.iaas = iaas;
	}

	public double getScpu() {
		return Scpu;
	}

	public void setScpu(double scpu) {
		Scpu = scpu;
	}

	public long getSmemory() {
		return Smemory;
	}

	public void setSmemory(long smemory) {
		Smemory = smemory;
	}

	public double getSinstancePrice() {
		return SinstancePrice;
	}

	public void setSinstancePrice(double sinstancePrice) {
		SinstancePrice = sinstancePrice;
	}

	public double getShourPrice() {
		return ShourPrice;
	}

	public void setShourPrice(double shourPrice) {
		ShourPrice = shourPrice;
	}

	public double getMcpu() {
		return Mcpu;
	}

	public void setMcpu(double mcpu) {
		Mcpu = mcpu;
	}

	public long getMmemory() {
		return Mmemory;
	}

	public void setMmemory(long mmemory) {
		Mmemory = mmemory;
	}

	public double getMinstancePrice() {
		return MinstancePrice;
	}

	public void setMinstancePrice(double minstancePrice) {
		MinstancePrice = minstancePrice;
	}

	public double getMhourPrice() {
		return MhourPrice;
	}

	public void setMhourPrice(double mhourPrice) {
		MhourPrice = mhourPrice;
	}

	public double getLcpu() {
		return Lcpu;
	}

	public void setLcpu(double lcpu) {
		Lcpu = lcpu;
	}

	public long getLmemory() {
		return Lmemory;
	}

	public void setLmemory(long lmemory) {
		Lmemory = lmemory;
	}

	public double getLinstancePrice() {
		return LinstancePrice;
	}

	public void setLinstancePrice(double linstancePrice) {
		LinstancePrice = linstancePrice;
	}

	public double getLhourPrice() {
		return LhourPrice;
	}

	public void setLhourPrice(double lhourPrice) {
		LhourPrice = lhourPrice;
	}

	@Override
	public double getPerTickQuote(ResourceConstraints rc) {
		
		//System.out.println(this.ShourPrice/ticUnit);
		//System.out.println(this.MhourPrice/ticUnit);
		//System.out.println(this.LhourPrice/ticUnit);
		
		if(rc.getRequiredCPUs()<=this.Scpu && rc.getRequiredMemory()<=this.Smemory){
			
			//System.out.println("a");
			return this.ShourPrice/ticUnit;
		}
		if(rc.getRequiredCPUs()<=this.Mcpu && rc.getRequiredMemory()<=this.Mmemory){
			//System.out.println("b");
			return this.MhourPrice/ticUnit;
		}
		if(rc.getRequiredCPUs()<=this.Lcpu && rc.getRequiredMemory()<=this.Lmemory){
			//System.out.println("c");
			return this.LhourPrice/ticUnit;
			
		}
		
		if(rc.getRequiredCPUs()<=this.Scpu && rc.getRequiredMemory()>this.Smemory){
			//System.out.println("d");
			return 1.5*this.ShourPrice/ticUnit;
		}
		if(rc.getRequiredCPUs()<=this.Mcpu && rc.getRequiredMemory()>this.Mmemory){
			//System.out.println("e");
			return 1.5*this.MhourPrice/ticUnit;
		}
		if(rc.getRequiredCPUs()<=this.Lcpu && rc.getRequiredMemory()>this.Lmemory){
			//System.out.println("f");
			return 1.5*this.LhourPrice/ticUnit;
		}
		if(rc.getRequiredCPUs()>this.Lcpu && rc.getRequiredMemory()>this.Lmemory){
			//System.out.println("g");
			return 2*this.LhourPrice/ticUnit;
		}
		
		return 0;
	}

	
	private void readCloudProviderXml(String datafile,String target) throws ParserConfigurationException, SAXException, IOException{
		File fXmlFile = new File(datafile);
		NodeList nList;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
				
		nList = doc.getElementsByTagName(target);
		for (int temp = 0; temp < nList.item(0).getChildNodes().getLength(); temp++){
			
			if(nList.item(0).getChildNodes().item(temp).getNodeName().equals("small")){
				this.setSmemory(Long.parseLong(nList.item(0).getChildNodes().item(temp).getChildNodes().item(1).getTextContent()));
				this.setScpu(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(3).getTextContent()));
				this.setSinstancePrice(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(5).getTextContent()));
				this.setShourPrice(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(7).getTextContent()));
			}
			if(nList.item(0).getChildNodes().item(temp).getNodeName().equals("medium")){
				this.setMmemory(Long.parseLong(nList.item(0).getChildNodes().item(temp).getChildNodes().item(1).getTextContent()));
				this.setMcpu(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(3).getTextContent()));
				this.setMinstancePrice(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(5).getTextContent()));
				this.setMhourPrice(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(7).getTextContent()));
			}
			if(nList.item(0).getChildNodes().item(temp).getNodeName().equals("large")){
				this.setLmemory(Long.parseLong(nList.item(0).getChildNodes().item(temp).getChildNodes().item(1).getTextContent()));
				this.setLcpu(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(3).getTextContent()));
				this.setLinstancePrice(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(5).getTextContent()));
				this.setLhourPrice(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(7).getTextContent()));
			}
		}
	}
	
	public MyProvider(String datafile, String provider) throws ParserConfigurationException, SAXException, IOException{
		this.readCloudProviderXml(datafile,provider);
	}
}
