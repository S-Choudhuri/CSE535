package edu.asu.sml535.assignment2.classifiers;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import edu.asu.sml535.assignment2.features.CSVFile;

import static android.content.res.AssetManager.ACCESS_BUFFER;

public class SVC {

    private enum Kernel { LINEAR, POLY, RBF, SIGMOID }

    private int nClasses;
    private int nRows;
    private int[] classes;
    private double[][] vectors;
    private double[][] coefficients;
    private double[] intercepts;
    private int[] weights;
    private Kernel kernel;
    private double gamma;
    private double coef0;
    private double degree;

    public SVC (int nClasses, int nRows, double[][] vectors, double[][] coefficients, double[] intercepts, int[] weights, String kernel, double gamma, double coef0, double degree) {
        this.nClasses = nClasses;
        this.classes = new int[nClasses];
        for (int i = 0; i < nClasses; i++) {
            this.classes[i] = i;
        }
        this.nRows = nRows;

        this.vectors = vectors;
        this.coefficients = coefficients;
        this.intercepts = intercepts;
        this.weights = weights;

        this.kernel = Kernel.valueOf(kernel.toUpperCase());
        this.gamma = gamma;
        this.coef0 = coef0;
        this.degree = degree;
    }

    public int predict(double[] features) {
    
        double[] kernels = new double[vectors.length];
        double kernel;
        switch (this.kernel) {
            case LINEAR:
                // <x,x'>
                for (int i = 0; i < this.vectors.length; i++) {
                    kernel = 0.;
                    for (int j = 0; j < this.vectors[i].length; j++) {
                        kernel += this.vectors[i][j] * features[j];
                    }
                    kernels[i] = kernel;
                }
                break;
            case POLY:
                // (y<x,x'>+r)^d
                for (int i = 0; i < this.vectors.length; i++) {
                    kernel = 0.;
                    for (int j = 0; j < this.vectors[i].length; j++) {
                        kernel += this.vectors[i][j] * features[j];
                    }
                    kernels[i] = Math.pow((this.gamma * kernel) + this.coef0, this.degree);
                }
                break;
            case RBF:
                // exp(-y|x-x'|^2)
                for (int i = 0; i < this.vectors.length; i++) {
                    kernel = 0.;
                    for (int j = 0; j < this.vectors[i].length; j++) {
                        kernel += Math.pow(this.vectors[i][j] - features[j], 2);
                    }
                    kernels[i] = Math.exp(-this.gamma * kernel);
                }
                break;
            case SIGMOID:
                // tanh(y<x,x'>+r)
                for (int i = 0; i < this.vectors.length; i++) {
                    kernel = 0.;
                    for (int j = 0; j < this.vectors[i].length; j++) {
                        kernel += this.vectors[i][j] * features[j];
                    }
                    kernels[i] = Math.tanh((this.gamma * kernel) + this.coef0);
                }
                break;
        }
    
        int[] starts = new int[this.nRows];
        for (int i = 0; i < this.nRows; i++) {
            if (i != 0) {
                int start = 0;
                for (int j = 0; j < i; j++) {
                    start += this.weights[j];
                }
                starts[i] = start;
            } else {
                starts[0] = 0;
            }
        }
    
        int[] ends = new int[this.nRows];
        for (int i = 0; i < this.nRows; i++) {
            ends[i] = this.weights[i] + starts[i];
        }
    
        if (this.nClasses == 2) {
    
            for (int i = 0; i < kernels.length; i++) {
                kernels[i] = -kernels[i];
            }
    
            double decision = 0.;
            for (int k = starts[1]; k < ends[1]; k++) {
                decision += kernels[k] * this.coefficients[0][k];
            }
            for (int k = starts[0]; k < ends[0]; k++) {
                decision += kernels[k] * this.coefficients[0][k];
            }
            decision += this.intercepts[0];
    
            if (decision > 0) {
                return 0;
            }
            return 1;
    
        }
    
        double[] decisions = new double[this.intercepts.length];
        for (int i = 0, d = 0, l = this.nRows; i < l; i++) {
            for (int j = i + 1; j < l; j++) {
                double tmp = 0.;
                for (int k = starts[j]; k < ends[j]; k++) {
                    tmp += this.coefficients[i][k] * kernels[k];
                }
                for (int k = starts[i]; k < ends[i]; k++) {
                    tmp += this.coefficients[j - 1][k] * kernels[k];
                }
                decisions[d] = tmp + this.intercepts[d];
                d++;
            }
        }
    
        int[] votes = new int[this.intercepts.length];
        for (int i = 0, d = 0, l = this.nRows; i < l; i++) {
            for (int j = i + 1; j < l; j++) {
                votes[d] = decisions[d] > 0 ? i : j;
                d++;
            }
        }
    
        int[] amounts = new int[this.nClasses];
        for (int i = 0, l = votes.length; i < l; i++) {
            amounts[votes[i]] += 1;
        }
    
        int classVal = -1, classIdx = -1;
        for (int i = 0, l = amounts.length; i < l; i++) {
            if (amounts[i] > classVal) {
                classVal = amounts[i];
                classIdx= i;
            }
        }
        return this.classes[classIdx];
    
    }

    public static int classify(List<Double> inputfeatures, List<String[]> vectorString) throws IOException {

        Log.i("SVC","Classifying with features :"+inputfeatures.size());

        if (inputfeatures.size() == 424) {

            // Features:
            double[] features = new double[inputfeatures.size()];
            for (int i = 0, l = inputfeatures.size(); i < l; i++) {
                features[i] = inputfeatures.get(i);
            }

            // Parameters:
            double[][] vectors = new double[63][424];

            int j=0;
            for (String[] vals: vectorString) {
                for (int i = 0; i < vals.length; i++) {
                    vectors[j][i]=Double.parseDouble(vals[i]);
                }
                j++;
            }

            double[][] coefficients = {{-5.3935088527641276e-11, -1.1569891673955757e-10, -3.450516007707622e-10, -1.7305611457902965e-11, -1.1827195669157558e-10, -4.648056898816175e-10, -2.6575697043415036e-10, -1.1695338773480692e-10, -2.7035317886072262e-11, -2.8668179504315915e-10, -8.110447642200979e-11, -2.0696127631782984e-10, -7.288708118504485e-11, -1.692059468000634e-11, -2.2629059552504471e-10, -1.4822136938652716e-09, -5.173618821180444e-10, -1.751124213304609e-10, -4.8052849118843257e-11, -1.6617093560406882e-12, -2.2044224598617547e-10, -2.6008675702147035e-11, -6.23221317673041e-10, -2.859334274484868e-09, 1.876608104354175e-10, 1.3578075077712885e-10, 1.6293271491558536e-10, 1.0349670034802442e-12, 6.397522766298572e-10, 1.1191592774805866e-10, 6.2188217929278e-10, 2.177399511841345e-10, 1.3534147313304095e-10, 1.1675471306360307e-10, 5.309160308921713e-10, 8.799727518572829e-11, 1.4973071114687195e-10, 3.233251604613293e-11, 3.80346616017052e-10, 1.8907271113501515e-10, 4.581230617922743e-11, 8.30613252572445e-11, 9.877065541134137e-11, 2.178619037245536e-10, 2.100309540717658e-12, 1.0051140300614889e-10, 7.142963527135305e-11, 2.606834333553129e-13, 8.251692940689187e-10, 2.3485420881401085e-12, 6.234634502967687e-10, 4.362666443071713e-10, 2.3050183204466752e-10, 7.230538341464558e-11, 3.4238803493119023e-10, 4.4107370958289054e-11, 2.503010300428537e-10, 1.8346991573345553e-12, 9.967184709020307e-11, 2.999388701337603e-11, 8.508151669557535e-10, 1.1993581460498703e-10, 1.1896658552776978e-10}};
            double[] intercepts = {-0.5634261164521585};
            int[] weights = {24, 39};

            // Prediction:
            SVC clf = new SVC(2, 2, vectors, coefficients, intercepts, weights, "poly", 0.001, 0.0, 3);
            int estimation = clf.predict(features);
            Log.i("SVC","Estimation:"+estimation);
            return estimation;
        }

        return -1;
    }
}