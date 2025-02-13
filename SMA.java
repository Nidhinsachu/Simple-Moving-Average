import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SMA {
    public static void main(String[] args) {
        String inputFile = "RELIANCE_CLEANED.csv";
        String outputFile = "RELIANCE_SMA.csv";
        
       
        List<String[]> cleanedData = cleanInputFile(inputFile);
        calculateSMA(cleanedData, outputFile);
    }

    public static List<String[]> cleanInputFile(String inputFile) {
        List<String[]> cleanedData = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(inputFile), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",", -1);
                if (values.length < 7 || Arrays.stream(values, 1, 7).anyMatch(String::isEmpty)) {
                    continue; 
                }
                cleanedData.add(values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cleanedData;
    }

    public static void calculateSMA(List<String[]> data, String outputFile) {
        try {
          
            String[] headers = Arrays.copyOf(data.get(0), data.get(0).length + 6);
            String[] smaColumns = {"Open SMA(10)", "High SMA(10)", "Low SMA(10)", "Close SMA(10)", "Adj Close SMA(10)", "Volume SMA(10)"};
            System.arraycopy(smaColumns, 0, headers, data.get(0).length, smaColumns.length);
            
            List<String[]> result = new ArrayList<>();
            result.add(headers);
            
           
            double[] sum = new double[6];
            Queue<double[]> window = new LinkedList<>();
            
            for (int i = 1; i < data.size(); i++) {
                String[] row = Arrays.copyOf(data.get(i), headers.length);
                double[] values = new double[6];
                
                try {
                    for (int j = 0; j < 6; j++) {
                        values[j] = Double.parseDouble(data.get(i)[j + 1]);
                        sum[j] += values[j];
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid number at row " + i);
                    continue;
                }
                
                window.add(values);
                if (window.size() > 10) {
                    double[] removed = window.poll();
                    for (int j = 0; j < 6; j++) {
                        sum[j] -= removed[j];
                    }
                }
                
                if (window.size() == 10) {
                    for (int j = 0; j < 6; j++) {
                        row[data.get(0).length + j] = String.format("%.6f", sum[j] / 10);
                    }
                }
                
                result.add(row);
            }
            
            
            writeCSV(outputFile, result);
            System.out.println("Updated CSV saved as " + outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeCSV(String outputFile, List<String[]> data) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputFile), StandardCharsets.UTF_8)) {
            for (String[] row : data) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
        }
    }
}
