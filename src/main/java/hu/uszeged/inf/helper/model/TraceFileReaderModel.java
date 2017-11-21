package hu.uszeged.inf.helper.model;

import com.ljmu.andre.SimulationHelpers.Application;
import com.ljmu.andre.SimulationHelpers.SimulationFileReader;

import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Andre on 09/04/2017.
 */
public class TraceFileReaderModel {
    @XmlElement(name="SimulationFilePath")
    public String simFilePath;

    @XmlElement(name="SimulationFileName")
    public String simFileName;

    public SimulationFileReader generateFileReader(long simFrom, long simTo) {
        if(simFilePath == null && simFileName != null)
            simFilePath = Application.USER_DIR + "/" + simFileName;

        if(simFilePath != null) {
            if(simTo == -1)
                simTo = Integer.MAX_VALUE;

            try {
                return new SimulationFileReader("None", simFilePath, simFrom, simTo, false);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
