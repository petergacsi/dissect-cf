package iot.extension;

import hu.mta.sztaki.lpds.cloud.simulator.util.*;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;

/**
 * Az osztaly peldanyositasakor letrejon a publikus IaaS felho, 
 * illetve tarolja a virtualis kepfajlt es a virtualis gep eroforrasigenyet is.
 * */
public class Cloud {
	private static IaaSService iaas;
	private static VirtualAppliance va ;//= new VirtualAppliance("BaseVA", 100, 0, false, 1000000); // default static VA to reach from everywhere
	private static AlterableResourceConstraints arc;// = new AlterableResourceConstraints(8, 0.001, 4294967296l); // 8 magos,4gb memoria
	
	/**
	 * Getter metodus az IaaS felhohoz
	 * @return IaaSService 
	 */
	 static IaaSService getIaas() {
		return iaas;
	}
	
	/**
	 * Getter metodus a virtualis kepfajlhoz
	 * @return VirtualAppliance
	 */
	static VirtualAppliance getVa() {
		return va;
	}
	
	/**
	 * Getter metodus a VM eroforrasigenyhez
	 * @return AlterableResourceConstraints
	 */
	static AlterableResourceConstraints getArc() {
		return arc;
	}

	/**
	 * Az osztaly peldanyositaskor a statikus adattagok megkapjak a parameterek ertekeit, ha azok nem null ertekuek.
	 * Ellenkezo esetben default ertekeket kapnak: 
	 * 	- VirtualAppliance("BaseVA", 100, 0, false, 1000000); 
	 *  - AlterableResourceConstraints(8, 0.001, 4294967296l); 
	 * @param p_va a virtualis kepfajl
	 * @param p_arc a VM eroforrasigenye
	 * @param cloudfile az IaaS felhot tartalmazo XML eleresi utvonala
	 */
	public Cloud(VirtualAppliance p_va,AlterableResourceConstraints p_arc,String cloudfile) throws IOException, SAXException, ParserConfigurationException {
		if (p_va==null){
			Cloud.va= new VirtualAppliance("BaseVA", 100, 0, false, 1000000);
		}else{
			Cloud.va= p_va;
		}
		if(p_arc==null){
			Cloud.arc= new AlterableResourceConstraints(8, 0.001, 4294967296l);
		}else{
			Cloud.arc=p_arc;
		}
		iaas = CloudLoader.loadNodes(cloudfile);
		iaas.repositories.get(0).registerObject(Cloud.getVa());
	}
}
