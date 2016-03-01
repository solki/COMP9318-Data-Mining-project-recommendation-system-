/**
 * 
 */
package comp9318_Project_1;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @author Solki
 *
 */
public class SGD {

	private static int n;
	private static int m;

	/**
	 * 
	 */
	public SGD() {
	}

	@SuppressWarnings({ "unused", "resource" })
	public static void main(String[] args) throws FileNotFoundException {

		if (args.length != 9) {
			System.out.println("Required 9 arguments");
			return;
		}

		int n = Integer.parseInt(args[0]); // n is the number of users
		int m = Integer.parseInt(args[1]); // m is the number of items
		int f = Integer.parseInt(args[2]); // f is the number of factors
		int r = Integer.parseInt(args[3]); // r is the number of ratings
		double mu = Double.parseDouble(args[4]); // mu is the learning rate.
		double lamdba = Double.parseDouble(args[5]); // lamdba is the
														// regularization

		String RATING_FILE = args[6];
		String ITER_FILE = args[7];
		String RANDOM_FILE = args[8];

		Scanner s1 = new Scanner(new FileInputStream(RATING_FILE));
		Scanner s2 = new Scanner(new FileInputStream(ITER_FILE));
		Scanner s3 = new Scanner(new FileInputStream(RANDOM_FILE));

		SGD obj = new SGD();
		SGD.m = m;
		SGD.n = n;
		// use hashmap to store datasets in the rating_file
		HashMap<Integer, ratingTuple> ratingList = obj.fileHandlerR(s1);
		// a is the average of all ratings
		double a = obj.average(ratingList);
		// ranP contains the random numbers for building P matrix
		double[] ranP = new double[n * f];
		// ranQ contains the random numbers for building Q matrix
		double[] ranQ = new double[m * f];
		for (int i = 0; i < n * f; ++i) {
			if (s3.hasNext()) {
				ranP[i] = Double.parseDouble(s3.next());
			}
		}
		for (int i = 0; i < f * m; ++i) {
			if (s3.hasNext()) {
				ranQ[i] = Double.parseDouble(s3.next());
			}
		}
		// build P matrix and Q matrix
		double[][] P = obj.buildP(f, n, ranP, a);
		double[][] Q = obj.buildQ(f, m, ranQ, a);

		while (s2.hasNext()) {
			int ratingID = Integer.parseInt(s2.next());
			ratingTuple entry = (ratingTuple) ratingList.get(ratingID);
			int x = entry.getUserID();
			int i = entry.getItemID();
			// when getting a tuple whose user id exceeds the input argument n
			// or item id exceeds the m, just drop it.
			if (i > m || x > n) {
				continue;
			}
			double rxi = entry.getRating();
			double[] px = new double[f];
			double[] qi = new double[f];
			for (int j = 0; j < f; ++j) {
				px[j] = P[x - 1][j];
				qi[j] = Q[j][i - 1];
			}
			// doing P Q updating by updating each vactors in P and Q
			// respectively
			obj.process(P, Q, x - 1, i - 1, rxi, px, qi, f, mu, lamdba);
		}

		for (int i = 0; i < f; ++i) {
			for (int j = 0; j < m; ++j) {
				System.out.printf("%f", Q[i][j]);
				if (j != (m - 1)) {
					System.out.print(" ");
				}
			}
			if (i != (f - 1)) {
				System.out.println();
			}
		}
	}

	protected void process(double[][] P, double[][] Q, int x, int i,
			double rxi, double[] px, double[] qi, int f, double mu,
			double lamdba) {
		double product = 0;
		for (int k = 0; k < f; ++k) {
			product += px[k] * qi[k];
		}
		double epsilon = 2 * (rxi - product);
		double[] tempQ = new double[f];
		double[] tempP = new double[f];
		for (int k = 0; k < f; ++k) {
			tempQ[k] = qi[k] + mu * (epsilon * px[k] - 2 * lamdba * qi[k]);
			tempP[k] = px[k] + mu * (epsilon * qi[k] - 2 * lamdba * px[k]);

		}
		for (int j = 0; j < f; ++j) {
			P[x][j] = tempP[j];
			Q[j][i] = tempQ[j];
		}
	}

	protected double[][] buildP(int num_of_factors, int num_of_users,
			double[] ranP, double a) {
		double[][] f = new double[num_of_users][num_of_factors];
		for (int i = 0; i < num_of_users; ++i) {
			for (int j = 0; j < num_of_factors; ++j) {
				f[i][j] = Math.sqrt(a / num_of_factors)
						+ ranP[num_of_factors * i + j];
			}
		}
		return f;
	}

	protected double[][] buildQ(int num_of_factors, int num_of_items,
			double[] ranQ, double a) {
		double[][] f = new double[num_of_factors][num_of_items];
		for (int i = 0; i < num_of_factors; ++i) {
			for (int j = 0; j < num_of_items; ++j) {
				f[i][j] = Math.sqrt(a / num_of_factors)
						+ ranQ[num_of_items * i + j];
			}
		}
		return f;
	}

	public HashMap<Integer, ratingTuple> fileHandlerR(Scanner file) {
		HashMap<Integer, ratingTuple> ratingList = new HashMap<Integer, ratingTuple>();
		while (file.hasNextLine()) {
			String line = file.nextLine();
			if (!line.isEmpty()) {
				String[] tmp = line.split("::");
				ratingTuple entry = new ratingTuple();
				entry.setUserID(Integer.parseInt(tmp[1]));
				entry.setItemID(Integer.parseInt(tmp[2]));
				entry.setRating(Double.parseDouble(tmp[3]));
				ratingList.put(Integer.parseInt(tmp[0]), entry);
			}
		}
		return ratingList;
	}

	/* RatingID::UserID::ItemID::Rating */
	private double average(HashMap<Integer, ratingTuple> list) {
		double sum = 0;
		int num = list.size();
		for (int i = 1; i <= num; ++i) {
			ratingTuple rt = (ratingTuple) list.get(i);
			// when getting a tuple whose user id exceeds the input argument n
			// or item id exceeds the m, just ignore its rating value.
			if (rt.getItemID() > m || rt.getUserID() > n) {
				num--;
				continue;
			}
			sum += rt.getRating();
		}
		return sum / num;
	}
}

/**
 * @author Solki 
 * used to store content in rating_file
 */
class ratingTuple {

	private int userID;
	private int itemID;
	private double rating;

	public ratingTuple() {

	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public int getItemID() {
		return itemID;
	}

	public void setItemID(int itemID) {
		this.itemID = itemID;
	}

	public double getRating() {
		return rating;
	}

	public void setRating(double rating) {
		this.rating = rating;
	}

}
