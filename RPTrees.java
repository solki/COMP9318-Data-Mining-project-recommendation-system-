/**
 * 
 */
package comp9318_Project_1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * @author Solki
 *
 */
public class RPTrees {

	private static int d;
	int MaxSizeOfLeaf;
	private TreeNode root;
	private static HashMap<Integer, double[]> Vectors = new HashMap<Integer, double[]>();
	private static ArrayList<Double> rand = new ArrayList<Double>();
	private static PriorityQueue<Median> minDistance;

	/**
	 * 
	 */
	public RPTrees() {

	}

	@SuppressWarnings("static-access")
	public RPTrees(int d, int MaxSizeOfLeaf) {
		this.d = d;
		this.MaxSizeOfLeaf = MaxSizeOfLeaf;

	}

	@SuppressWarnings("resource")
	public static void main(String args[]) throws FileNotFoundException {

		if (args.length != 7) {
			System.out.println("Required 7 arguments");
			return;
		}

		int n = Integer.parseInt(args[0]); // n is the number of points
		int d = Integer.parseInt(args[1]); // d is the dimensionality
		int NumOfTrees = Integer.parseInt(args[2]); // the number of RP-Trees
		int MaxSizeOfLeaf = Integer.parseInt(args[3]); // the maximum number of
														// points that you can
														// put in a leaf node of
														// a RP-Tree

		String DATA_FILE = args[4];
		String RANDOM_GAUSSIAN_FILE = args[5];
		String QUERY_FILE = args[6];

		Scanner s1 = new Scanner(new FileInputStream(DATA_FILE));
		Scanner s2 = new Scanner(new FileInputStream(RANDOM_GAUSSIAN_FILE));
		Scanner s3 = new Scanner(new FileInputStream(QUERY_FILE));
		// use an ArrayList to store Guassian random numbers as a global static
		// variable
		while (s2.hasNext()) {
			rand.add(Double.parseDouble(s2.next()));
		}
		s2.close();
		Double[][] datas = new Double[d][n];
		int i = 0, j = 0;
		while (s1.hasNext()) {
			datas[i][j++] = Double.parseDouble(s1.next());
			if (j == n) {
				i++;
				j = 0;
			}
			if (i == d) {
				break;
			}
		}
		s1.close();
		for (j = 0; j < n; ++j) {
			double[] v = new double[d];
			for (i = 0; i < d; ++i) {
				v[i] = datas[i][j];
			}
			Vectors.put(j + 1, v);
		}
		ArrayList<Integer> S = new ArrayList<Integer>();
		for (int k = 0; k < n; ++k) {
			S.add(k + 1);
		}
		RPTrees[] tree = new RPTrees[NumOfTrees];
		for (int num = 0; num < NumOfTrees; ++num) {
			tree[num] = new RPTrees(d, MaxSizeOfLeaf);
			tree[num].root = tree[num].MakeTree(S);
		}
		//rewrite the compatator method for Priority Queue use
		Comparator<Median> c = new Comparator<Median>() {
			@Override
			public int compare(Median o1, Median o2) {
				double numbera = o1.getValue();
				double numberb = o2.getValue();
				if (numberb > numbera) {
					return -1;
				} else if (numberb < numbera) {
					return 1;
				} else {
					return 0;
				}
			}
		};
		while (s3.hasNext()) {
			minDistance = new PriorityQueue<Median>(n, c);
			int q = Integer.parseInt(s3.next());
			for (int num = 0; num < NumOfTrees; ++num) {
				FindNN(tree[num].root, q);
			}
			boolean[] tmp = new boolean[n];
			for (int k = 0; k < n; ++k) {
				tmp[k] = false;
			}
			int count = 0;
			while (!minDistance.isEmpty()) {
				int index = minDistance.poll().index;
				if (tmp[index - 1] == false) {
					count++;
					tmp[index - 1] = true;
					System.out.print(index);
					if (count < 3 && !minDistance.isEmpty()) {
						System.out.print(" ");
					}
					if (count == 3) {
						break;
					}
				}
			}
			if (s3.hasNext()) {
				System.out.println();
			}
		}
		s3.close();
	}

	public static void FindNN(TreeNode node, int q) {
		if (node.is_Leaf) {
			for (int i : node.vectors) {
				if (i == q) {
					continue;
				}
				double[] X = Vectors.get(i);
				double[] Q = Vectors.get(q);
				double sum = 0;
				for (int j = 0; j < d; ++j) {
					sum += Math.pow((X[j] - Q[j]), 2);
				}
				Median min = new Median();
				min.setIndex(i);
				min.setValue(sum);
				minDistance.add(min);
			}
			// System.out.print(min_dis + "->");
			return;
		} else {
			double[] Q = Vectors.get(q);
			double[] U = node.getU();
			double projection = 0;
			for (int i = 0; i < d; ++i) {
				projection += (Q[i] * U[i]);
			}
			if (projection <= node.boundary) {
				FindNN(node.left, q);
			} else {
				FindNN(node.right, q);
			}
		}
	}

	public TreeNode MakeTree(ArrayList<Integer> S) {
		TreeNode node = new TreeNode();
		if (S.size() <= MaxSizeOfLeaf) {
			node.setLeft(null);
			node.setRight(null);
			node.setVectors(S);
			node.setIs_Leaf(true);
			node.setU(null);
			return node;
		}
		double[] U = new double[d];
		for (int j = 0; j < d; ++j) {
			U[j] = rand.remove(0);
		}
		node.setU(U);
		node.boundary = Median(S, U);
		ArrayList<Integer> L_S = new ArrayList<Integer>();
		ArrayList<Integer> R_S = new ArrayList<Integer>();
		for (int k = 0; k < S.size(); ++k) {
			boolean rule = false;
			rule = this.ChooseRule(S.get(k), U, node.boundary);
			if (rule) {
				L_S.add(S.get(k));
			} else {
				R_S.add(S.get(k));
			}
		}
		node.left = MakeTree(L_S);
		node.right = MakeTree(R_S);

		return node;
	}
	//rewrite the compatator method for Priority Queue use
	private double Median(ArrayList<Integer> S, double[] U) {
		Comparator<Median> c = new Comparator<Median>() {
			@Override
			public int compare(Median o1, Median o2) {
				double numbera = o1.getValue();
				double numberb = o2.getValue();
				if (numberb > numbera) {
					return -1;
				} else if (numberb < numbera) {
					return 1;
				} else {
					return 0;
				}
			}
		};
		PriorityQueue<Median> medians = new PriorityQueue<Median>(S.size(), c);

		for (int i = 0; i < S.size(); ++i) {
			double[] Z = Vectors.get(S.get(i));
			double tmpsum = 0;
			for (int j = 0; j < d; ++j) {
				tmpsum += (Z[j] * U[j]);
			}
			Median med = new Median();
			med.setIndex(S.get(i));
			med.setValue(tmpsum);
			medians.add(med);
		}

		double median = 0;
		if (medians.size() % 2 == 1) {
			int index = medians.size() / 2 + 1;
			for (int k = 1; k <= index - 1; ++k) {
				medians.poll();
			}
			median = medians.poll().value;
		} else {
			int index = medians.size() / 2;
			for (int k = 1; k <= index - 1; ++k) {
				medians.poll();
			}
			double a = medians.poll().value;
			double b = medians.poll().value;
			median = (a + b) / 2;
		}
		return median;
	}

	/*
	 */
	public boolean ChooseRule(int x, double[] U, double med) {

		double y = 0;
		double[] X = Vectors.get(x);
		for (int i = 0; i < d; ++i) {
			y += (X[i] * U[i]);
		}
		return (y <= med);
	}
}

class Median {
	int index;
	double value;

	public Median() {
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}

class TreeNode {

	TreeNode left;
	TreeNode right;
	ArrayList<Integer> vectors;
	double[] U;
	double boundary;
	boolean is_Leaf;

	public TreeNode() {
		this.is_Leaf = false;
	}

	public ArrayList<Integer> getVectors() {
		return vectors;
	}

	public void setVectors(ArrayList<Integer> s) {
		this.vectors = s;
	}

	public TreeNode getLeft() {
		return left;
	}

	public void setLeft(TreeNode left) {
		this.left = left;
	}

	public TreeNode getRight() {
		return right;
	}

	public void setRight(TreeNode right) {
		this.right = right;
	}

	public double[] getU() {
		return U;
	}

	public void setU(double[] u) {
		U = u;
	}

	public double getBoundary() {
		return boundary;
	}

	public void setBoundary(double boundary) {
		this.boundary = boundary;
	}

	public boolean isIs_Leaf() {
		return is_Leaf;
	}

	public void setIs_Leaf(boolean is_Leaf) {
		this.is_Leaf = is_Leaf;
	}

}
