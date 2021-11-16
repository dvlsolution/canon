package canon.medical.informatics;

import java.io.*;
import java.util.*;

public class ParsePatientName
{
    private static final char PID_SEGMENT_DELIMITER = ',';
    private static final char PATIENT_NAME_DELIMITER = '^';

    private static class Pair {
        int beginIndex;
        int endIndex;

        public Pair(int beginIndex, int endIndex) {
            this.beginIndex = beginIndex;
            this.endIndex = endIndex;
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Wrong input, please add an input file name.");
            return;
        }

        ParsePatientName ppn = new ParsePatientName();
        try {
            final Map<Integer, List<String>> output = ppn.parseInputFile(args[0]);
            ppn.printOutput(output);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private Map<Integer, List<String>> parseInputFile(String inputFileName) throws IOException {
        final Map<Integer, List<String>> result = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
            String line;
            int patientNameHash;
            while ((line = br.readLine()) != null) {
                patientNameHash = extractPatientName(line);
                if (result.containsKey(patientNameHash)) {
                    result.get(patientNameHash).add(line);
                } else {
                    result.put(patientNameHash, new ArrayList<>(Arrays.asList(line)));
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
        final Pair patientFullName = extractPatientFullName(line);

        char c;
        int hash = 0;
        int finding = 0, index = patientFullName.beginIndex;
        while (finding < 2 && index < patientFullName.endIndex) {
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

    private Pair extractPatientFullName(String line) {
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

        return new Pair(beginIndex, endIndex);
    }

    private void printOutput(final Map<Integer, List<String>> output) {
        int index = 0;
        try (OutputStream out = new BufferedOutputStream(System.out)) {
            for (Integer key : output.keySet()) {
                out.write((index++ + "\n").getBytes());
                for (String patient : output.get(key)) {
                    out.write((patient + "\n").getBytes());
                }
            }
            out.flush();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
