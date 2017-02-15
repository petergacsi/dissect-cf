package providers;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import iot.extension.Station;

public class CloudsProvider extends Provider{

	public CloudsProvider(String datafile,String providername,String tiername) {
		super();
	}
	
	@Override
	protected void costCounter(int filesize) {
		if(this.getName().equals("amazon")){
			Tier t = this.getTierList().get(0);
			if(filesize<=t.getBlockOfData()){
				this.setUserCost(Station.allstationsize/filesize*t.getBofPrice()*this.getExchangeRate()/t.getBofMessagecount());
			}else{
				this.setUserCost(((Station.allstationsize/t.getBlockOfData())+1)*t.getBofPrice()*this.getExchangeRate()/t.getBofMessagecount());
			}
			System.out.println("Amazon cost of IoT side: "+this.getUserCost());
			System.out.println(Station.allstationsize/filesize);
		}
	}
}
