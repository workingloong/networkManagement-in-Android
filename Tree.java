package GBDT;

public class Tree {
	Tree left;
	Tree right;
	int max_depth = 0;
	int min_leafs = 0;
	int feature_index;
	int samples_num = 0;
	double friedman_mse;
	double split_value;
	double predict_value = 0;
	double numerator = 0.0;
	double denominator = 0.0;
	public Tree(int max_depth,int min_leafs,double predict_value){
		this.max_depth = max_depth;
		this.min_leafs = min_leafs;
		this.predict_value = predict_value;
	}
	public static void inOrderTraverse(Tree tree){
		if(tree == null) return;
		System.out.println(tree.feature_index+" "+ tree.split_value+" "
				+tree.predict_value+" "+tree.samples_num+" "+tree.friedman_mse);
		inOrderTraverse(tree.left);
		inOrderTraverse(tree.right);
	}
}

