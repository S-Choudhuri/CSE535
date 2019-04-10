package edu.asu.sml535.assignment2.features;

import android.util.Log;

import com.google.common.math.Quantiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeatureExtractor {

    public static List<Double> extractFeatures(List<String[]> rows) {
        // count, mean, std, min, 25%, 50%, 75%, max for each column

        Map<Integer,List<Double>> column_features = new HashMap<>();

        String[] cols = rows.get(0);

        for(int i=0;i<cols.length;i++) {
            List<Double> features = new ArrayList<>();
            column_features.put(i,features);
        }

        for(String[] row : rows.subList(1,rows.size()-1)) {
            for(int i=0;i<row.length;i++) {
                Double val = 0.0;
                try {
                    String s = row[i].replaceAll("\"","");
                    val = Double.parseDouble(s);
                }
                catch (NumberFormatException ex) {
                    Log.i("FE","Exception for parsing:"+row[i]);
                }

                column_features.get(i).add(val);
            }
        }

        List<Double> finalFeatures = new ArrayList<>();

        // Count
        for(int i=0;i<cols.length;i++) {
            List<Double> colFeatures = column_features.get(i);
            Double count = (double) colFeatures.size();
            finalFeatures.add(count);
        }

        // Mean
        for(int i=0;i<cols.length;i++) {
            List<Double> colFeatures = column_features.get(i);
            Double count = (double) colFeatures.size();
            Double sum = 0.0;
            for (Double f:colFeatures) {
                sum+=f;
            }
            finalFeatures.add(sum/count);
        }

        //SD
        for(int i=0;i<cols.length;i++) {
            List<Double> colFeatures = column_features.get(i);
            Double count = (double) colFeatures.size();
            Double sum = 0.0;
            for (Double f:colFeatures) {
                sum+=f;
            }
            Double mean = sum/count;
            Double sd = 0.0;
            for (Double f:colFeatures) {
                sd = sd + Math.pow(f - mean, 2);
            }
            finalFeatures.add(sd);
        }

        //min
        for(int i=0;i<cols.length;i++) {
            List<Double> colFeatures = column_features.get(i);
            Double percentile0th = Quantiles.percentiles().index(0).compute(colFeatures);
            finalFeatures.add(percentile0th);
        }

        //25%
        for(int i=0;i<cols.length;i++) {
            List<Double> colFeatures = column_features.get(i);
            Double percentile25th = Quantiles.percentiles().index(25).compute(colFeatures);
            finalFeatures.add(percentile25th);
        }

        //50%
        for(int i=0;i<cols.length;i++) {
            List<Double> colFeatures = column_features.get(i);
            Double percentile50th = Quantiles.percentiles().index(50).compute(colFeatures);
            finalFeatures.add(percentile50th);
        }

        //75%
        for(int i=0;i<cols.length;i++) {
            List<Double> colFeatures = column_features.get(i);
            Double percentile75th = Quantiles.percentiles().index(75).compute(colFeatures);
            finalFeatures.add(percentile75th);
        }

        //Max
        for(int i=0;i<cols.length;i++) {
            List<Double> colFeatures = column_features.get(i);
            Double percentile100th = Quantiles.percentiles().index(100).compute(colFeatures);
            finalFeatures.add(percentile100th);
        }

        assert finalFeatures.size() == 424;

        return finalFeatures;
    }
}
