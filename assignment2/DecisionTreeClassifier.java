class DecisionTreeClassifier {

    private static int findMax(int[] nums) {
        int index = 0;
        for (int i = 0; i < nums.length; i++) {
            index = nums[i] > nums[index] ? i : index;
        }
        return index;
    }

    public static int predict(double[] features) {
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
            double[] features = new double[args.length];
            for (int i = 0, l = args.length; i < l; i++) {
                features[i] = Double.parseDouble(args[i]);
            }

            // Prediction:
            int prediction = DecisionTreeClassifier.predict(features);
            System.out.println(prediction);

        }
    }
}