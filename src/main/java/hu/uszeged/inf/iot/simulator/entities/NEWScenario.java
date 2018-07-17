package hu.uszeged.inf.iot.simulator.entities;

public class NEWScenario {

	public static void main(String[] args) throws Exception {
		String cloudfile="/home/student/Desktop/markus/dissect-cf/src/main/resources/LPDSCloud.xml";
		String appconfigfile="/home/student/Desktop/dissect/dissect-cf/src/main/java/hu/uszeged/xml/model/Application.xml";
		
		Cloud cloud1=new Cloud(cloudfile,"cloud1");
		cloud1.applications.add();
	}
}
