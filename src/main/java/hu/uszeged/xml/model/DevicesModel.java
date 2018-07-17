package hu.uszeged.xml.model;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "devices" )
public class DevicesModel
{
    ArrayList<DeviceModel> deviceList;

    public ArrayList<DeviceModel> getDevices(){
        return deviceList;
    }

    @XmlElement( name = "device" )
    public void setDevices( ArrayList<DeviceModel> devices ){
        this.deviceList = devices;
    }

    public void add( DeviceModel device ){
        if( this.deviceList == null )
        {
            this.deviceList = new ArrayList<DeviceModel>();
        }
        this.deviceList.add( device );

    }

    @Override
    public String toString(){
        StringBuffer str = new StringBuffer();
        for( DeviceModel device : this.deviceList )
        {	
            str.append( device.toString() );
            str.append("\n");
        }
        return str.toString();
    }

}
