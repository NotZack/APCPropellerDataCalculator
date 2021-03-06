package dataParsing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A loader that loads all propeller data into an ArrayList< PropellerDataOrganizer >
 */
public class PropellerDataLoader {

    private static final double MOTOR_EFFICIENCY = 0.9;
    private static final int MOTOR_KV = 310;

    public static final double MAX_FORWARD_AIRSPEED = (MOTOR_KV * 22.2*13/12/60) * 0.6818182; //In MPH
    public static final double POWER_CONSTANT = 1.34 * MOTOR_EFFICIENCY;

    //The list that contains all propeller data of all propellers
    private final ArrayList<PropellerDataSet> allPropellerData = new ArrayList<>();

    /**
     * A loaded propeller dataset
     * @param dataPath The root path of propeller data files
     */
    public PropellerDataLoader(String dataPath) {
        loadDataFiles(dataPath);
    }

    /**
     * Parses each file from the given folder path into a PropellerDataSet, then adds that set to allPropellerData.
     * @param dataPath The folder path to parse data from
     */
    private void loadDataFiles(String dataPath) {
        try (Stream<Path> paths = Files.walk(Paths.get(dataPath))) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                try {
                    //Parses every file
                    parseDataFile(new FileReader(path.toFile()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("Organized " + allPropellerData.size() + " propeller data files.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses the given single data file into its own PropellerDataSet
     * @param fileToParse The given propeller data file to parse
     */
    private void parseDataFile(FileReader fileToParse) {
        BufferedReader br = new BufferedReader(fileToParse);
        try {
            String currentLine = br.readLine();
            String propName = currentLine.substring(currentLine.indexOf(" (") + 2, currentLine.lastIndexOf("."));

            //Parsed data store
            ArrayList<String[]> dataTable = new ArrayList<>();

            //Parses each line of file into usable data
            while ((currentLine = br.readLine()) != null) {
                String[] parsedLine = parseLine(currentLine);
                if (parsedLine != null) {
                    dataTable.add(parsedLine);
                }
            }
            allPropellerData.add(new PropellerDataSet(propName, dataTable));
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * Parses the given line into usable data, which is either an RPM number, or a list of propeller data values
     * @param lineToParse The given line to parse
     * @return Each element sequentially, parsed
     */
    @Nullable
    private String[] parseLine(@NotNull String lineToParse) {
        //Replace characters to allow for easier regex
        lineToParse = lineToParse.replace("-", " -");
        lineToParse = lineToParse.replace("-NaN", " 0.00");
        lineToParse = lineToParse.trim().replaceAll(" +", " ");

        //Splits between every number
        String[] lineSplit = lineToParse.split("[^0-9.,-]");
        for (String s : lineSplit) {

            //Filter garbage data
            if (!s.isEmpty() && (s.length() > 2)) {

                //RPM is always whole number, not a float (RPM has no '.')
                if (!s.contains(".")) {
                    String[] rpm = new String[1];
                    rpm[0] = s.trim();
                    return rpm;
                } else {
                    return lineSplit;
                }
            }
        }

        //Returns null if the line is unable to be parsed
        return null;
    }

    /**
     * @return The size of the loaded propeller data set
     */
    public int getNumOfProps() {
        return allPropellerData.size();
    }

    /**
     * @param propIndex The propeller data file index to find RPM values of
     * @return The propeller data set at the given index
     */
    public PropellerDataSet getPropellerDataAt(int propIndex) {
        return allPropellerData.get(propIndex);
    }
}