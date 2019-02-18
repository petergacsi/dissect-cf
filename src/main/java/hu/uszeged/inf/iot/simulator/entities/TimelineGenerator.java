package hu.uszeged.inf.iot.simulator.entities;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;

public class TimelineGenerator {

	public static void generate() throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter writer = new PrintWriter("timeline.html", "UTF-8");
		writer.println("<!DOCTYPE html><html><head>");
		writer.println("<script type=\'text/javascript\' src=\'https://www.gstatic.com/charts/loader.js\'></script>");
		writer.println("<script type=\'text/javascript\'>");
		writer.println("google.charts.load(\'current\', {packages:[\'timeline\']});");
		writer.println("  google.charts.setOnLoadCallback(drawChart);");
		writer.println("function drawChart(){");
		writer.println("var container = document.getElementById('example');");
		writer.println("var chart = new google.visualization.Timeline(container);");
		writer.println("var dataTable = new google.visualization.DataTable();");
		writer.println("dataTable.addColumn({ type: 'string', id: 'Application' });");
		writer.println("dataTable.addColumn({ type: 'string', id: 'VM' });");
		writer.println("    dataTable.addColumn({ type: 'date', id: 'Start' });");
		writer.println("dataTable.addColumn({ type: 'date', id: 'End' });");
		writer.println("dataTable.addRows([");
		
		for (Cloud c : Cloud.clouds.values()) {
			for (Application a : c.applications) {
				for(TimelineCollector tc : a.timelineList) {
					writer.println("[ '"+a.name+"', '"+tc.vmId+"', new Date(0,0,0,0,0,0,"+tc.start +"), new Date(0,0,0,0,0,0,"+tc.stop+")],");
				}
			}
		}

		writer.println("]);");
		writer.println("chart.draw(dataTable);");
		writer.println("}</script>");
		writer.println("</head><body>");
		writer.println("<div id=\"example\" style=\"height: 1500px; width=100%;\"></div>");
		writer.println(" </body></html>");

		writer.close();
	}
	
	public static class TimelineCollector{
		public TimelineCollector(long start, long stop, String vmId) {
			super();
			this.start = start;
			this.stop = stop;
			this.vmId = vmId;
		}
		public long start;
		public long stop;
		public String vmId;
		
	}
}
