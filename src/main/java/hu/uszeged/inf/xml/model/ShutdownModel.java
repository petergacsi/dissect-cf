/*
 *  ========================================================================
 *  DIScrete event baSed Energy Consumption simulaTor 
 *    					             for Clouds and Federations (DISSECT-CF)
 *  ========================================================================
 *  
 *  This file is part of DISSECT-CF.
 *  
 *  DISSECT-CF is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at
 *  your option) any later version.
 *  
 *  DISSECT-CF is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *  General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with DISSECT-CF.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  (C) Copyright 2019, Andras Markus (markusa@inf.u-szeged.hu)
 */

package hu.uszeged.inf.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "shutdown" )
@XmlAccessorType(XmlAccessType.PROPERTY) 
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