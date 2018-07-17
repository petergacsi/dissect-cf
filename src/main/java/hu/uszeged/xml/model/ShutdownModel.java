package hu.uszeged.xml.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "shutdown" )
public class ShutdownModel{

    int number;
    long from;
    long to;
    
	@Override
	public String toString() {
		return "ShutdownModel [number=" + number + ", from=" + from + ", to=" + to + "]";
	}

	public int getNumber() {
		return number;
	}

	@XmlElement( name = "number" )
	public void setNumber(int number) {
		this.number = number;
	}

	public long getFrom() {
		return from;
	}
	@XmlElement( name = "from" )
	public void setFrom(long from) {
		this.from = from;
	}

	public long getTo() {
		return to;
	}
	@XmlElement( name = "to" )
	public void setTo(long to) {
		this.to = to;
	}

    
}