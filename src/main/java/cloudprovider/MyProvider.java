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
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.util.CloudLoader;
import iot.extension.Cloud;
import iotprovider.Provider;

public class MyProvider implements CloudProviderInterface {
	private AlterableResourceConstraints arc;
	IaaSService iaas;
	// Cloud side variables
	private double cpu;
	private long memory; 
	private double instancePrice;
	private double hourPrice;
	

	public AlterableResourceConstraints getArc() {
		return arc;
	}

	public void setArc(AlterableResourceConstraints arc) {
		this.arc = arc;
	}

	public IaaSService getIaas() {
		return iaas;
	}

	public void setIaas(IaaSService iaas) {
		this.iaas = iaas;
	}

	protected double getCpu() {
		return cpu;
	}

	protected void setCpu(double cpu) {
		this.cpu = cpu;
	}

	protected long getMemory() {
		return memory;
	}

	protected void setMemory(long memory) {
		this.memory = memory;
	}

	protected double getInstancePrice() {
		return instancePrice;
	}

	protected void setInstancePrice(double instancePrice) {
		this.instancePrice = instancePrice;
	}

	protected double getHourPrice() {
		return hourPrice;
	}

	protected void setHourPrice(double hourPrice) {
		this.hourPrice = hourPrice;
	}


	@Override
	public double getPerTickQuote(ResourceConstraints rc) {
		return 0;
	}

	
	@Override
	public void setIaaSService(IaaSService iaas) {
		this.iaas=iaas;	
	}
	
	private void readCloudProviderXml(String datafile,String size,String target) throws ParserConfigurationException, SAXException, IOException{
		File fXmlFile = new File(datafile);
		NodeList nList,nList2;
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		
		System.out.println(datafile + " "+ size + " "+ target);
		
		nList = doc.getElementsByTagName(target);
		for (int temp = 0; temp < nList.item(0).getChildNodes().getLength(); temp++){
			
			if(nList.item(0).getChildNodes().item(temp).getNodeName().equals(size)){
				System.out.println("l444444");
				this.setMemory(Long.parseLong(nList.item(0).getChildNodes().item(temp).getChildNodes().item(1).getTextContent()));
				this.setCpu(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(3).getTextContent()));
				this.setInstancePrice(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(5).getTextContent()));
				this.setHourPrice(Double.parseDouble(nList.item(0).getChildNodes().item(temp).getChildNodes().item(7).getTextContent()));
			}
		}
	}
	
	public MyProvider(String datafile,String size,String target) throws ParserConfigurationException, SAXException, IOException {
		this.readCloudProviderXml(datafile, size, target);
		arc = new AlterableResourceConstraints(this.getCpu(), 0.001, this.getMemory());
		//arc = new AlterableResourceConstraints(8, 0.001,15032385536L);
		System.out.println(arc + "rossz?");
	}

}
