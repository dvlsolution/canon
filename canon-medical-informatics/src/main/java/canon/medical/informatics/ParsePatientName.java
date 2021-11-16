package canon.medical.informatics;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ParsePatientName
{
    // double default buffer size, make sense for big files
    private static final int INPUT_BUFFER_SIZE = 16384;
    private static final int OUTPUT_BUFFER_SIZE = 16384;

    private static final double NANO_SEC = 1_000_000_000.;

    private static final char PID_SEGMENT_DELIMITER = ',';
    private static final char PATIENT_NAME_DELIMITER = '^';
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
                    // ArrayList will work faster for small files
                    List<String> patientList = new LinkedList<>();
                    patientList.add(line);
                    result.put(patientNameHash, patientList);
                }
            }
        }

        return result;
    }

    /*
        Brute force solution, creates multiple String objects
     */
//    private String extractPatientName(String line) {
//        List<String> pidList = Arrays.asList(line.split(","));
//        String patientName = pidList.get(1);
//        List<String> patientNameList = Arrays.asList(patientName.split("\\^"));
//
//        return (patientNameList.get(0) + patientNameList.get(1)).toLowerCase();
//    }

    /*
        Optimized solution that minimized number of String objects creation
     */
    private Integer extractPatientName(String line) {
        final int[] patientFullName = extractPatientFullName(line);

        char c;
        int hash = 0;
        int finding = 0, index = patientFullName[0];
        while (finding < 2 && index < patientFullName[1]) {
            c = line.charAt(index++);
            if (c == PATIENT_NAME_DELIMITER) {
                finding++;
            }
            else {
                hash = 31 * hash + Character.toLowerCase(c);
            }
        }

        return hash;
    }

    private int[] extractPatientFullName(String line) {
        char c;
        int finding = 0, index = 0, beginIndex = 0, endIndex = 0;
        int length = line.length();
        while (finding < 2 && index < length) {
            c = line.charAt(index++);
            if (c == PID_SEGMENT_DELIMITER) {
                if (finding == 0) {
                    beginIndex = index;
                }
                else {
                    endIndex = index - 1;
                }

                finding++;
            }
        }

        return new int[] {beginIndex, endIndex};
    }

    private void printOutput(final Map<Integer, List<String>> output, long startTime) {
        int index = 0;
        try (OutputStream out = new BufferedOutputStream(System.out, OUTPUT_BUFFER_SIZE)) {
            for (Integer key : output.keySet()) {
                out.write((index++ + END_OF_LINE_SEPARATOR).getBytes(StandardCharsets.UTF_8));
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
