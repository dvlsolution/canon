package canon.medical.informatics;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ParsePatientName
{
    // increase default buffer size, make sense for big files
    private static final int INPUT_BUFFER_SIZE = 65536;
    private static final int OUTPUT_BUFFER_SIZE = 65536;

    private static final double NANO_SEC = 1_000_000_000.;

    private static final char PID_SEGMENT_DELIMITER = ',';
    private static final char PATIENT_NAME_DELIMITER = '^';
    private static final String PID_GROUP_SEPARATOR = ":";
    private static final String END_OF_LINE_SEPARATOR = System.lineSeparator();

    public static void main(String[] args) {
        final long startTime = System.nanoTime();

        if (args == null || args.length == 0) {
            System.out.println("Wrong input, please add an input file name.");
            return;
        }

        ParsePatientName ppn = new ParsePatientName();
        try {
            final Map<Integer, List<String>> output = ppn.parseInputFile(args[0]);
            ppn.printOutput(output, startTime);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private Map<Integer, List<String>> parseInputFile(String inputFileName) throws IOException {
        // HashMap can be used if order is not important
        final Map<Integer, List<String>> result = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFileName), StandardCharsets.UTF_8), INPUT_BUFFER_SIZE))
        {
            String line;
            int patientNameHash;
            while ((line = br.readLine()) != null) {
                patientNameHash = extractPatientName(line);
                if (result.containsKey(patientNameHash)) {
                    result.get(patientNameHash).add(line);
                } else {
                    List<String> patientList = new ArrayList<>();
                    patientList.add(line);
                    result.put(patientNameHash, patientList);
                }
            }
        }

        return result;
    }

    /*
        Brute force solution, creates multiple String objects
        estimatedTime:
            0.012411486
            0.011964998
            0.011312554
            0.012197491        
     */
//    private Integer extractPatientName(String line) {
//        List<String> pidList = Arrays.asList(line.split(","));
//        String patientName = pidList.get(1);
//        List<String> patientNameList = Arrays.asList(patientName.split("\\^"));
//
//        return (patientNameList.get(0) + patientNameList.get(1)).toLowerCase().hashCode();
//    }

    /*
        Optimized solution that minimized number of String objects creation
        estimatedTime:
            0.006491112
            0.007742388
            0.008186967
            0.006614730        
     */
    private Integer extractPatientName(String line) {
        char c;
        int index = 0, finding = 0, hash = 0;
        int length = line.length();
        boolean isCommaFound = false;
        while (finding < 2 && index < length) {
            c = line.charAt(index++);
            if (c == PID_SEGMENT_DELIMITER) {
                if (isCommaFound) {
                    // we found the second comma
                    break;
                } else {
                    isCommaFound = true;
                }
            } else if (isCommaFound) {
                if (c == PATIENT_NAME_DELIMITER) {
                    // we found patient name delimiter
                    finding++;
                }
                else {
                    hash = 31 * hash + Character.toLowerCase(c);
                }
            }
        }

        return hash;
    }

    private void printOutput(final Map<Integer, List<String>> output, long startTime) {
        int index = 0;
        try (OutputStream out = new BufferedOutputStream(System.out, OUTPUT_BUFFER_SIZE)) {
            for (Integer key : output.keySet()) {
                out.write((index++ + PID_GROUP_SEPARATOR + END_OF_LINE_SEPARATOR).getBytes(StandardCharsets.UTF_8));
                for (String patient : output.get(key)) {
                    out.write((patient + END_OF_LINE_SEPARATOR).getBytes(StandardCharsets.UTF_8));
                }
            }

            final long estimatedTime = System.nanoTime() - startTime;
            out.write(END_OF_LINE_SEPARATOR.getBytes(StandardCharsets.UTF_8));
            out.write(("estimatedTime: " + estimatedTime / NANO_SEC).getBytes(StandardCharsets.UTF_8));

            out.flush();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
