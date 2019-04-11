package hu.uszeged.inf.iot.simulator.refactored;

import java.util.List;

public class Group {
	private List<? extends ComputingAppliance> neighbourDevices;
	
	public Group(List<? extends ComputingAppliance> listOfNeighbours) {
		
		this.neighbourDevices = listOfNeighbours;
		
		for (ComputingAppliance computingAppliance : neighbourDevices) {
			computingAppliance.setGroup(this);
		}
	}

	@Override
	public String toString() {
		String dv = "";
		for (int i = 0; i < neighbourDevices.size(); i++) {
			dv += neighbourDevices.get(i).name +" ";
		}
		return "Group: " + dv;
	}
	
	
}
