package edu.asu.sml535.assignment2.classifiers;

import android.util.Log;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DecisionTreeClassifier {

    private static int findMax(int[] nums) {
        int index = 0;
        for (int i = 0; i < nums.length; i++) {
            index = nums[i] > nums[index] ? i : index;
        }
        return index;
    }

    public static int predict(List<Double> inputfeatures) {

        double[] features = new double[inputfeatures.size()];
        for (int i = 0, l = inputfeatures.size(); i < l; i++) {
            features[i] = inputfeatures.get(i);
            Log.i("DTree","Vals"+features[i]);
        }

        int[] classes = new int[2];
            
        if (features[246] <= 163.59510803222656) {
            if (features[403] <= 0.9494742751121521) {
                if (features[122] <= 3.516611337661743) {
                    classes[0] = 0; 
                    classes[1] = 95; 
                } else {
                    classes[0] = 2; 
                    classes[1] = 3; 
                }
            } else {
                if (features[405] <= 261.5179748535156) {
                    classes[0] = 13; 
                    classes[1] = 0; 
                } else {
                    classes[0] = 1; 
                    classes[1] = 4; 
                }
            }
        } else {
            if (features[296] <= 287.86492919921875) {
                if (features[277] <= 121.37104797363281) {
                    classes[0] = 3; 
                    classes[1] = 2; 
                } else {
                    classes[0] = 136; 
                    classes[1] = 0; 
                }
            } else {
                if (features[186] <= 18.94429349899292) {
                    classes[0] = 12; 
                    classes[1] = 1; 
                } else {
                    classes[0] = 0; 
                    classes[1] = 16; 
                }
            }
        }
    
        return findMax(classes);
    }

    public static void main(String[] args) {
        if (args.length == 424) {

            // Features:
            Double[] features = new Double[args.length];
            for (int i = 0, l = args.length; i < l; i++) {
                features[i] = Double.parseDouble(args[i]);
            }

            // Prediction:
            int prediction = DecisionTreeClassifier.predict(Lists.newArrayList(features));
            System.out.println(prediction);

        }
    }
}