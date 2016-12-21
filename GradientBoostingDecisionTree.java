package GBDT;

import java.util.ArrayList;
import java.util.HashSet;

public class GradientBoostingDecisionTree {
	public Tree[] trees;
	public int max_depth;
	public int min_leafs;
	public int tree_num;
	public double learn_rate;
	public GradientBoostingDecisionTree(int tree_num,int max_depth,int min_leafs,double learn_rate){
		trees = new Tree[tree_num];
		this.tree_num = tree_num;
		this.max_depth = max_depth;
		this.min_leafs = min_leafs;
		this.learn_rate = learn_rate;
	}
	
	public void fit(double[][] Xtrain, double[] y){
		RegressionTreeGenerator rtg = new RegressionTreeGenerator();
		double[] residual = new double[y.length];
		for(int i = 0;i< y.length;i++){
			residual[i] = 1-2*y[i];
		}
		// 灏� 0,1绫诲埆杞崲涓� 1 鍜� -1
		binaryToPN(y);
		
		// initialize the predictions
		double[] y_pred = new double[y.length];
		for(int tree_index = 0;tree_index<tree_num;tree_index++){
			// choose the best split feature and split value by regression tree using MSE
			Tree root = rtg.createRegressionTree(Xtrain,residual,max_depth,min_leafs);
			//update leaves
			update_terminal_region(root,Xtrain,y,y_pred);
			trees[tree_index] = root;
			// updates the predictions by the new tree
			predict_regression(Xtrain,root,y_pred);
			//calculate the negative gradient by exponential loss
			residual = negativeGradient(y,y_pred);
		}
	}
	
	public double[] predict_proba(double[][] X){
		double[] pred_proba = new double[X.length];
		double[] pred_score = new double[X.length];
		for(int i = 0;i<tree_num;i++){
			predict_regression(X,trees[i],pred_score);
		}
		for(int i = 0;i<pred_score.length;i++){
			// score of regression to probability by expit function
			pred_proba[i] = Math.exp(2.0*pred_score[i])/(1+Math.exp(2.0*pred_score[i])); 
		}
		return pred_proba;
	}
	
	public void predict_regression(double[][] X,Tree tree,double[] y_pred){
		for(int i = 0;i<X.length;i++){
			y_pred[i] += learn_rate *treePredict(tree, X[i]);
		}
	}
	
	public double treePredict(Tree tree, double[] x){
		int feature_index = tree.feature_index;
		double pred = 0.0;
		if(tree.left ==  null && tree.right ==  null) return tree.predict_value;
		if(x[feature_index] < tree.split_value){
			pred = treePredict(tree.left,x);
		}
		else{
			pred = treePredict(tree.right,x);
		}
		return pred;
	}
	
	public void binaryToPN(double[] y){
		for(int i = 0;i<y.length;i++){
			y[i] = 2*y[i] - 1;
		}
	}
	public double[] negativeGradient(double[] y,double[] y_pred){
		double[] residual = new double[y.length];
		for(int i = 0;i<y.length;i++){
			residual[i] = -1.0*y[i]*Math.exp(-1.0*y[i]*y_pred[i]); 
		}
		return residual;
	}
	
	public void update_terminal_region(Tree tree,double[][] Xtrain,double[] y,double[] pred){
	    for(int i = 0;i<Xtrain.length;i++){
	    	collect_samples_on_leaf(tree,Xtrain[i],y[i],pred[i]);
	    }
	    update_leaves_value(tree);
	}
	
	public void collect_samples_on_leaf(Tree tree, double[] x,double y,double pred){
		if(tree.left ==  null && tree.right ==  null){
			tree.numerator += y*Math.exp(-1.0*y*pred);
			tree.denominator += Math.exp(-1.0*y*pred);
			return;
		} 
		int feature_index = tree.feature_index;
		if(x[feature_index] < tree.split_value){
			collect_samples_on_leaf(tree.left,x,y,pred);
		}
		else{
			collect_samples_on_leaf(tree.right,x,y,pred);
		}
	}
	public void update_leaves_value(Tree tree){
		if(tree.left ==  null && tree.right ==  null){
			tree.predict_value = tree.numerator/tree.denominator;
			return;
		}
		if(tree.left != null){
			update_leaves_value(tree.left);
		}
		if(tree.right != null){
			update_leaves_value(tree.right);
		}
	}
	
	public double exp_loss(double[] y,double[] y_pred){
		double loss = 0.0;
		for(int i = 0;i<y.length;i++){
			loss += Math.exp(-1.0 * y[i] *y_pred[i]);
		}
		return loss;
	}
	public double friedman_mse(double[] y){
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

