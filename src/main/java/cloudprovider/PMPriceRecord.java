/*
 *  ========================================================================
 *  dcf-exercises
 *  ========================================================================
 *  
 *  This file is part of dcf-exercises.
 *  
 *  dcf-exercises is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or (at
 *  your option) any later version.
 *  
 *  dcf-exercises is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with dcf-exercises.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  (C) Copyright 2017, Gabor Kecskemeti (g.kecskemeti@ljmu.ac.uk)
 *  (C) Copyright 2015, Gabor Kecskemeti (kecskemeti@iit.uni-miskolc.hu)
 */
package cloudprovider;

import java.util.Map;

import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;

public class PMPriceRecord {
	public static final double hourlyAmortisaiton = Double.parseDouble(System.getProperty("hourlyAmortisaiton"));
	public static final double perMachineBaseCost = Double.parseDouble(System.getProperty("perMachineBaseCost")); // GBP
	public static final double maxResourceCombination = Integer.parseInt(System.getProperty("maxCoreCount")) 
			* Double.parseDouble(System.getProperty("maxProcessingCap"))
			* Long.parseLong(System.getProperty("maxMem")) * Long.parseLong(System.getProperty("maxDisk")); //systempropertybe kivenni

	public final PhysicalMachine pm;
	private double currentMachinePrice;
	private final double amortizationRate;

	public PMPriceRecord(PhysicalMachine forMe) {
		pm = forMe;
		ResourceConstraints pmComputePower = forMe.getCapacities();  
		double combinedResources = pmComputePower.getTotalProcessingPower() * pmComputePower.getRequiredMemory()
				* forMe.localDisk.getMaxStorageCapacity();
		double performanceRatio = Math.pow(combinedResources / maxResourceCombination, 0.33);
		
		PowerState maxConsumingState=pm.hostPowerBehavior.get(PhysicalMachine.State.RUNNING.toString());
		double maxConsumption = maxConsumingState.getConsumptionRange() + maxConsumingState.getMinConsumption();
		double inverseEnergyRatio = Math.pow(Double.parseDouble(System.getProperty("maxMaxPower")) / maxConsumption,0.8);
		
		//double inverseEnergyRatio = Math.pow(Double.parseDouble(System.getProperty("maxMaxPower")) / pm.getMaxConsumption(),0.8);
		//pm.hostPowerBehavior
			//PowerState maxConsumingState = powerTransitions.get(PhysicalMachine.State.RUNNING.toString());
			//maxConsumption = maxConsumingState.getConsumptionRange() + maxConsumingState.getMinConsumption();
		//public final Map<String, PowerState> hostPowerBehavior;	

		currentMachinePrice = inverseEnergyRatio * performanceRatio * perMachineBaseCost; // /pm.getReliMult();
		amortizationRate = currentMachinePrice * hourlyAmortisaiton;
	}

	public void amortizefor(final int hours) {
		currentMachinePrice = Math.max(0, currentMachinePrice - hours * amortizationRate);
	}

	public double getCurrentMachinePrice() {
		return currentMachinePrice;
	}

	@Override
	public String toString() {
		return "(PMPrice: GBP" + currentMachinePrice + " " + pm.toString() + ")";
	}
}