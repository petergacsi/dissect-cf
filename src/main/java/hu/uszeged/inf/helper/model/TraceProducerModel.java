package hu.uszeged.inf.helper.model;

import com.ljmu.andre.SimulationHelpers.SimulationTraceProducer;

import java.util.Random;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import hu.mta.sztaki.lpds.cloud.simulator.helpers.trace.GenericTraceProducer;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.trace.random.DistributionSpecifier;

/**
 * Created by Andre on 09/04/2017.
 */
public class TraceProducerModel {
    @XmlElement(name="TargetDevice")
    String deviceID;

    @XmlElement(name="Distributions")
    Distributions distributions;

    @XmlElement(name="MaxPacketSize")
    public int maxPacketSize;

    @XmlElement(name="MaxJobDistance")
    public int maxJobDistance;

    @XmlElement(name="JobCount")
    public int jobCount = -1;

    @XmlElement(name="StorePackets")
    public boolean shouldSave = false;

    public GenericTraceProducer generateProducer(long simFrom, long simTo, String source) {
        try {
            return new SimulationTraceProducer(source, deviceID,
                    simFrom, simTo, jobCount, shouldSave,
                    distributions.generateSize(), maxPacketSize,
                    distributions.generateGap(), maxJobDistance);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static class Distributions {
        @XmlElement(name="Size")
        public DistributionModel sizeModel;

        @XmlElement(name="Gap")
        public DistributionModel gapModel;

        @XmlAttribute(name="template")
        public String template;

        public DistributionSpecifier generateSize() {
            if(sizeModel != null)
                return sizeModel.generateDistribution();
            else {
                if(template != null) {
                    DistributionSpecifier sizeDistribution = new DistributionSpecifier(new Random());
                    if(template.equals("max"))
                        sizeDistribution.addRange(1,1,1);
                    else
                        sizeDistribution.addRange(0,0,1);

                    return sizeDistribution;
                }
            }

            throw new IllegalStateException("No Size Distribution Model Supplied!");
        }

        public DistributionSpecifier generateGap() {
            if(gapModel != null)
                return gapModel.generateDistribution();
            else {
                if(template != null) {
                    DistributionSpecifier gapDistribution = new DistributionSpecifier(new Random());
                    if(template.equals("max"))
                        gapDistribution.addRange(1,1,1);
                    else
                        gapDistribution.addRange(0,0,1);

                    return gapDistribution;
                }
            }

            throw new IllegalStateException("No Gap Distribution Model Supplied!");
        }
    }
}
