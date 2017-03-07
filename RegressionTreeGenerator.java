package GBDT;

import java.util.Arrays;
import java.util.Comparator;

class SplitNode{
	int feature_index;
	int left_samples_num;
	double split_value;
	double split_gain;
	public SplitNode(int index, int num, double value,double gain){
		this.feature_index = index;
		this.left_samples_num = num;
		this.split_value = value;
		this.split_gain = gain;
 	};
}
public class RegressionTreeGenerator {
	public Tree createRegressionTree(double[][] Xtrain, double[] y,
			int max_depth,int min_leaves){
		
		Tree root = new Tree(max_depth,min_leaves,0.0);
		double sum = 0.0;
		for(int i =0;i<y.length;i++){
			sum += y[i];
		}
		root.samples_num = y.length;
		root.predict_value = sum/y.length;
		root.friedmanMse = friedman_mse(y);
		// generate leaf nodes
		if(y.length < min_leaves || max_depth <= 0){
			root.split_value = 0.0;
			root.left = null;
			root.right = null;
			return root;
		}
		SplitNode split_node = checkBestFeatureSplit(Xtrain,y);

		root.feature_index = split_node.feature_index;
		root.split_value = split_node.split_value;
		
		int left_samples_num = split_node.left_samples_num;
		int right_samples_num = Xtrain.length - left_samples_num;
		int features_num = Xtrain[0].length;
		
		double[][] Xtrain_left = new double[left_samples_num][features_num];
		double[][] Xtrain_right = new double[right_samples_num][features_num];
		double[] y_left = new double[left_samples_num];
		double[] y_right = new double[right_samples_num];
		int left_index = 0;
		int right_index = 0;
		for(int i = 0;i<Xtrain.length;i++){
			if(Xtrain[i][root.feature_index] < root.split_value){
				Xtrain_left[left_index] = Xtrain[i];
				y_left[left_index] = y[i];
				left_index++;
			}
			else{
				Xtrain_right[right_index] = Xtrain[i];
				y_right[right_index] = y[i];
				right_index++;
			}
		}
		root.left = createRegressionTree(Xtrain_left,y_left,max_depth -1,min_leaves);
		root.right = createRegressionTree(Xtrain_right,y_right,max_depth -1,min_leaves);
		return root;
	}
	public SplitNode checkBestFeatureSplit(double[][] Xtrain, double[] y){
		//浠庢墍鏈夌壒寰佷腑閫夊彇涓�涓垎瑁傚鐩婃渶澶х殑鐗瑰緛鏉ヤ綔涓烘渶浣冲垎瑁傜壒寰�
		int num_features = Xtrain[0].length;
		SplitNode[] gains = new SplitNode[num_features];
		double max_gain = 0.0;
		int best_feature_index = 0;
		for(int i = 0;i<num_features;i++){
			gains[i] = gainBySplit(Xtrain,y,i);
			if(max_gain < gains[i].split_gain){
				max_gain = gains[i].split_gain;
				best_feature_index = i;
			}
		}
		return gains[best_feature_index];
	}
	
	public SplitNode gainBySplit(double[][] Xtrain, double[] y,int feature_index){
		int samples = Xtrain.length;
		int best_split_index = 0;
		double delta_gain = 0.0;
		double[][] features = new double[samples][2];
		for(int i = 0;i<samples;i++){
			features[i][0] = Xtrain[i][feature_index];
			features[i][1] = y[i];
		}
		Arrays.sort(features,new FeatureComparator());
		double sum_left = 0.0;
		double sum_right = 0.0;
		
		for(int i = 0;i<samples;i++){
			sum_right += features[i][1];
		}
		double sum = sum_right;
		double prev_value = features[0][0];
		int prev_index = 0;
		double total_error = Math.pow(sum,2)/samples;
		for(int i = 1;i<samples;i++){
			if(features[i][0] > prev_value){
				sum_left = calSumLeft(features,sum_left,i,prev_index);
				sum_right = calSumRight(features,sum_right,i,prev_index);
				double temp_gain = Math.pow(sum_left,2)/i + Math.pow(sum_right,2)/(samples-i) - total_error;
				if(temp_gain > delta_gain){
					delta_gain = temp_gain;
					best_split_index = i;
				}
				prev_value = features[i][0];
				prev_index = i;
			}
		}
		int left_samples_num = best_split_index;
		double split_value = features[best_split_index][0];
		if(best_split_index > 0){
			split_value = (features[best_split_index][0]+features[best_split_index-1][0])/2;
		}
		SplitNode node = new SplitNode(feature_index,left_samples_num,split_value,delta_gain);
		return node;
	}
	public class FeatureComparator implements Comparator<double[]>{
		public int compare(double[] f1,double[] f2){
			if(f1[0] > f2[0]) return 1;
			else if(f1[0] < f2[0]) return -1;
			else return 0;
		}
	}
	
	public double calSumLeft(double[][] features, double sum_left,int split_index,int prev){
		for(int i = prev;i<split_index;i++){
			sum_left += features[i][1];
		}
		return sum_left;
	}
	public double calSumRight(double[][] features, double sum_right,int split_index,int prev{
		for(int i = prev;i<split_index;i++){
			sum_right -= features[i][1];
		}
		return sum_right;
	}
	public double friedmanMse(double[] y){
		double sum = 0;
		for(int i = 0;i<y.length;i++){
			sum += y[i];
		}
		double avg = sum/y.length;
		double mse = 0.0;
		for(int i = 0;i<y.length;i++){
			mse += Math.pow(y[i] - avg,2);
		}
		return mse/y.length;
	}
}
