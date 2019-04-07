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

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement( name = "instances" )
@XmlAccessorType(XmlAccessType.PROPERTY) 
public class InstancesModel
{
    ArrayList<InstanceModel> instanceList;

    public ArrayList<InstanceModel> getInstances(){
        return instanceList;
    }

    @XmlElement( name = "instance" )
    public void setInstances( ArrayList<InstanceModel> instances ){
        this.instanceList = instances;
    }

    public void add( InstanceModel instances ){
        if( this.instanceList == null )
        {
            this.instanceList = new ArrayList<InstanceModel>();
        }
        this.instanceList.add( instances );

    }

    @Override
    public String toString(){
        StringBuffer str = new StringBuffer();
        for( InstanceModel instance : this.instanceList )
        {	
            str.append( instance.toString() );
            str.append("\n");
        }
        return str.toString();
    }

}