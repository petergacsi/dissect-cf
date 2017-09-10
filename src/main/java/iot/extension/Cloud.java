package iot.extension;

import hu.mta.sztaki.lpds.cloud.simulator.util.*;
import hu.unimiskolc.iit.distsys.forwarders.IaaSForwarder;

import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import cloudprovider.CloudProviderInterface;
import cloudprovider.IaaSCloudProvider;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.*;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;

/**
 * This class contains an IaaS service and also contains a virtual applicance
 * and its resource constraint. Az osztaly peldanyositasakor letrejon a publikus
 * IaaS felho, illetve tarolja a virtualis kepfajlt es a virtualis gep
 * eroforrasigenyet is.
 */
public class Cloud {
	private IaaSCloudProvider icprovider;
	
	/**
	 * The IaaS service which contains phisical machines and repositories. Az
	 * IaaS szolgalatatas, ami fizikai gepeket es tarolokat tartalmaz.
	 */
	private IaaSService iaas;

	/**
	 * It contains a picture for the virtual machines. Ez a valtozo tartalmazza
	 * a kepfajl a letrehozando virtualis gephez.
	 */
	private VirtualAppliance va;

	/**
	 * It contains the need of a virtual machnine. A virtualis gep
	 * eroforrasszukseglete.
	 */
	private AlterableResourceConstraints arc;

	/**
	 * Static ArrayList to collect the created clouds which we use during the
	 * simulation. Statikus ArrayList, amely tarolja a szimulacio soran hasznalt
	 * felhoket.
	 */
	private static ArrayList<Cloud> clouds = new ArrayList<Cloud>();

	/**
	 * Getter method for the list of clouds. Getter metodus a felho listahoz.
	 */
	public static ArrayList<Cloud> getClouds() {
		return clouds;
	}

	/**
	 * Getter method for the IaaS service. Getter metodus az IaaS felhohoz.
	 */
	IaaSService getIaas() {
		return iaas;
	}

	/**
	 * Getter method for the virtual appliance. Getter metodus a virtualis
	 * kepfajlhoz.
	 */
	VirtualAppliance getVa() {
		return va;
	}

	/**
	 * Getter method for the resource constraint of VM. Getter metodus a VM
	 * eroforrasigenyhez.
	 */
	AlterableResourceConstraints getArc() {
		return arc;
	}

	
	
	public IaaSCloudProvider getIcprovider() {
		return icprovider;
	}

	public void setIcprovider(IaaSCloudProvider icprovider) {
		this.icprovider = icprovider;
	}

	public void setIaas(IaaSService iaas) {
		this.iaas = iaas;
	}

	public void setVa(VirtualAppliance va) {
		this.va = va;
	}

	public void setArc(AlterableResourceConstraints arc) {
		this.arc = arc;
	}

	/**
	 * The constructor creates the cloud with default VirtualAppliance and
	 * ResoirceConstraints if the value of those parameters are null. If we have
	 * an XML which describe an Iaas Service then it creates based on it,
	 * otherwise based on the 'iaas' parameter. Az osztaly peldanyositaskor vagy
	 * egy IaaS-t leiro XML fajl alapjan, vagy az 'iaas' parameter alapjan
	 * letrehozza az IaaS szolgaltatast. Ha az elso ket parameter null,akkor
	 * alapertelmezett ertekeket fog haszalni VM generalasakor. Default values /
	 * Alapertelmezett ertekek: - VirtualAppliance("BaseVA", 100, 0, false,
	 * 1000000); - AlterableResourceConstraints(8, 0.001, 4294967296l);
	 * @param <P>
	 * 
	 * @param p_va
	 *            the virtual appliance - a virtualis kepfajl
	 * @param p_arc
	 *            the resource constraint of a VM - a VM eroforrasigenye
	 * @param cloudfile
	 *            the path of the XML file - az IaaS felhot tartalmazo XML
	 *            eleresi utvonala
	 */
	public <P extends CloudProviderInterface> Cloud(String cloudfile,VirtualAppliance p_va, IaaSService iaas,P p)
			throws IOException, SAXException, ParserConfigurationException {
		if (p_va == null) {
			this.va = new VirtualAppliance("BaseVA", 100, 0, false, 1000000);
		} else {
			this.va = p_va;
		}
		
		if (iaas == null) {
			this.iaas = CloudLoader.loadNodes(cloudfile);
		} else {
			this.iaas = iaas;
		}
		this.iaas.repositories.get(0).registerObject(this.getVa());
		p.setIaaSService(this.iaas);
		this.arc = p.getArc();
	}
	
}
