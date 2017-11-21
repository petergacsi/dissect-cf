package hu.uszeged.inf.helper.model;

import com.ljmu.andre.SimulationHelpers.Device;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import hu.mta.sztaki.lpds.cloud.simulator.helpers.trace.GenericTraceProducer;

/**
 * Created by Andre on 30/03/2017.
 */
public class DeviceModel {
    @XmlElement(name = "CustomDevice")
    public String deviceClass;

    @XmlAttribute(name="custom_attr")
    public String customAttributes;

    @XmlElement(name = "ID")
    public String id;

    @XmlElement(name = "SimulateFrom")
    public long simFrom;

    @XmlElement(name = "SimulateTo")
    public long simTo = -1;

    @XmlElement(name = "TraceFileReader")
    public TraceFileReaderModel fileReaderModel;

    @XmlElement(name = "TraceProducer")
    public TraceProducerModel traceProducerModel;

    public Device generateDevice() {
        GenericTraceProducer traceProducer = null;

        if (fileReaderModel != null)
            traceProducer = fileReaderModel.generateFileReader(simFrom, simTo);

        if (traceProducer == null && traceProducerModel != null)
            traceProducer = traceProducerModel.generateProducer(simFrom, simTo, id);

        System.out.println("custom: " + customAttributes);
        if (deviceClass == null)
            return new Device(id, traceProducer, customAttributes);

        try {
            Class<? extends Device> customDeviceClass = (Class<? extends Device>) Class.forName(deviceClass);
            Constructor deviceConstructor = customDeviceClass.getConstructor(String.class, GenericTraceProducer.class, String.class);

            return (Device) deviceConstructor.newInstance(id, traceProducer, customAttributes);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
