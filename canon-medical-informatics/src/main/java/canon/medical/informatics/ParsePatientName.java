package canon.medical.informatics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ParsePatientName
{
    private static final char PID_SEGMENT_DELIMITER = ',';
    private static final char PATIENT_NAME_DELIMITER = '^';

    private class Pair {
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
            Map<Integer, List<String>> output = ppn.parseInputFile(args[0]);
            ppn.printOutput(output);
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private Map<Integer, List<String>> parseInputFile(String inputFileName) throws IOException {
        Map<Integer, List<String>> result = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                int patientNameHash = extractPatientName(line);
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
        Pair patientFullName = extractPatientFullName(line);

        int finding = 0, lastNameEndIndex = patientFullName.endIndex, index = patientFullName.beginIndex;
        while (finding < 2 && index < patientFullName.endIndex) {
            char c = line.charAt(index++);
            if (c == PATIENT_NAME_DELIMITER) {
                finding++;
            }
            else {
                lastNameEndIndex = index;
            }
        }

        return hashCode(line, patientFullName.beginIndex, lastNameEndIndex);
    }

    private Pair extractPatientFullName(String line) {
        int finding = 0, index = 0, beginIndex = 0, endIndex = 0;
        while (finding < 2 && index < line.length()) {
            char c = line.charAt(index++);
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

    private int hashCode(String str, int beginIndex, int endIndex) {
        int hash = 0;
        if (str.length() > 0) {
            int index = beginIndex;
            while (index < endIndex) {
                hash = 31 * hash + Character.toLowerCase(str.charAt(index++));
            }
        }

        return hash;
    }

    private void printOutput(Map<Integer, List<String>> output) {
        int index = 0;
        for (Integer key : output.keySet()) {
            System.out.println(index++ + ":");
            for (String patient : output.get(key)) {
                System.out.println(patient);
            }
        }
    }
}
