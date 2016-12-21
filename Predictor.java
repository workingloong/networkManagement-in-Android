package GBDT;

import java.io.File;
import java.util.ArrayList;

import android.os.Environment;
import android.util.Log;

public class Predictor {
	GradientBoostingDecisionTree GBDT;
	public Predictor(){
		GBDT = new GradientBoostingDecisionTree(50,6,20,1);
	}
	public void train(){
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File file = new File(path,"network_info_log.txt");
		ReadFile reader = new ReadFile(file);
		ArrayList<ArrayList<Double>> data = reader.readData();
		int num_samples = data.size();
		int num_features = data.get(0).size()-1;
		
		// transform train dataset to matrix;
		double[][] Xtrain = new double[num_samples][num_features];
		double[] y = new double[num_samples];
		for(int i = 0;i< num_samples;i++){
			for(int j = 0;j<num_features;j++){
				Xtrain[i][j] = data.get(i).get(j);
			}
			y[i] = data.get(i).get(num_features);
			if(y[i] == 2){
				y[i] = 0;
			}
		}
		// tree_num, max_depth, min_leafs, learn_rate
		GBDT.fit(Xtrain,y);
	}
	
	public double predict(String info){
		ReadFile rf = new ReadFile();
		ArrayList<Double> xlist = rf.splitLine(info);
		int num_features = xlist.size()-1;
		double[][] x = new double[1][num_features];
		for(int i = 0;i<num_features;i++){
			x[0][i] = xlist.get(i);
		}
		double[] preds = GBDT.predict_proba(x);
		return preds[0];
	}

}
