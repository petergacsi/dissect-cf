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

public class ResourceDependentProvider extends CloudPricing {
	// Cloud side variables
	private long periodInTick;
	
	private double Scpu;
	private long Smemory; 
	private double SpricePerTick;
	
	private double Mcpu;
	private long Mmemory; 
	private double MpricePerTick;

	private double Lcpu;
	private long Lmemory; 
	private double LpricePerTick;

	
	public void setPeriodInTick(long periodInTick) {
		this.periodInTick = periodInTick;
	}


	public void setScpu(double scpu) {
		Scpu = scpu;
	}


	public void setSmemory(long smemory) {
		Smemory = smemory;
	}


	public void setSpricePerTick(double spricePerTick) {
		SpricePerTick = spricePerTick;
	}


	public void setMcpu(double mcpu) {
		Mcpu = mcpu;
	}


	public void setMmemory(long mmemory) {
		Mmemory = mmemory;
	}


	public void setMpricePerTick(double mpricePerTick) {
		MpricePerTick = mpricePerTick;
	}


	public void setLcpu(double lcpu) {
		Lcpu = lcpu;
	}


	public void setLmemory(long lmemory) {
		Lmemory = lmemory;
	}


	public void setLpricePerTick(double lpricePerTick) {
		LpricePerTick = lpricePerTick;
	}


	@Override
	public double getPerTickQuote(ResourceConstraints rc) {
		
		//System.out.println(this.ShourPrice/ticUnit);
		//System.out.println(this.MhourPrice/ticUnit);
		//System.out.println(this.LhourPrice/ticUnit);
		
		if(rc.getRequiredCPUs()<=this.Scpu && rc.getRequiredMemory()<=this.Smemory){
			
			//System.out.println("a");
			return this.SpricePerTick;
		}
		if(rc.getRequiredCPUs()<=this.Mcpu && rc.getRequiredMemory()<=this.Mmemory){
			//System.out.println("b");
			return this.MpricePerTick;
		}
		if(rc.getRequiredCPUs()<=this.Lcpu && rc.getRequiredMemory()<=this.Lmemory){
			//System.out.println("c");
			return this.LpricePerTick;
			
		}
		
		if(rc.getRequiredCPUs()<=this.Scpu && rc.getRequiredMemory()>this.Smemory){
			//System.out.println("d");
			return 1.5*this.SpricePerTick;
		}
		if(rc.getRequiredCPUs()<=this.Mcpu && rc.getRequiredMemory()>this.Mmemory){
			//System.out.println("e");
			return 1.5*this.MpricePerTick;
		}
		if(rc.getRequiredCPUs()<=this.Lcpu && rc.getRequiredMemory()>this.Lmemory){
			//System.out.println("f");
			return 1.5*this.LpricePerTick;
		}
		if(rc.getRequiredCPUs()>this.Lcpu && rc.getRequiredMemory()>this.Lmemory){
			//System.out.println("g");
			return 2*this.LpricePerTick;
		}
		
		return 0;
	}

	
	@Override
	public long getPeriodInTick(){
		return periodInTick;
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
				this.setSpricePerTick(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(5).getTextContent()));
				this.setPeriodInTick(Long.parseLong(nList.item(0).getChildNodes().item(temp).getChildNodes().item(7).getTextContent()));
			}
			if(nList.item(0).getChildNodes().item(temp).getNodeName().equals("medium")){
				this.setMmemory(Long.parseLong(nList.item(0).getChildNodes().item(temp).getChildNodes().item(1).getTextContent()));
				this.setMcpu(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(3).getTextContent()));
				this.setMpricePerTick(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(5).getTextContent()));
				this.setPeriodInTick(Long.parseLong(nList.item(0).getChildNodes().item(temp).getChildNodes().item(7).getTextContent()));
			}
			if(nList.item(0).getChildNodes().item(temp).getNodeName().equals("large")){
				this.setLmemory(Long.parseLong(nList.item(0).getChildNodes().item(temp).getChildNodes().item(1).getTextContent()));
				this.setLcpu(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(3).getTextContent()));
				this.setLpricePerTick(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(5).getTextContent()));
				this.setPeriodInTick(Long.parseLong(nList.item(0).getChildNodes().item(temp).getChildNodes().item(7).getTextContent()));
			}
		}
	}
	
	public ResourceDependentProvider(String datafile, String provider) throws ParserConfigurationException, SAXException, IOException{
		this.readCloudProviderXml(datafile,provider);
	}
}
