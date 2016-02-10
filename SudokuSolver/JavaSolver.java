//Elliot Way, CS571, Final Project
//Adapted from:
//https://raw.githubusercontent.com/attractivechaos/plb/master/sudoku/sudoku_v1.java
//See also:
//https://raw.githubusercontent.com/attractivechaos/plb/master/sudoku/sudoku_v1.c
//
//I barely understand this myself, so don't worry too much if you can't follow
//what's being done, even with my comments.
//The general idea is that we keep track of every possible constraint on the puzzle,
//which one's are used and which one's are not, and which values are possible as a result.
//The values are maintained in arrays, which act like hash tables to speed things up, in that
//we need only access the intended index of the array instead of parsing through the array
//for a calculation.
//
//The comments without a space at the beginning are mine, the one's with a space are from
//the original author. //Mine // not mine

public class JavaSolver {
	
	//Change just these 2.
	public static int SQUARE_SIZE = 4;
	public static int MIN_VALUE = 0; //0 - 15 (use 1 for 0 - 9)
	//There are also lines regarding input and output that you'd need
	//to change to take in 1-9 instead of 0-f.
	
	public static int NUM_SQUARES = SQUARE_SIZE;
	public static int GRID_LENGTH = SQUARE_SIZE * NUM_SQUARES;
	public static int GRID_SIZE = GRID_LENGTH * GRID_LENGTH;
	public static int NUM_VALUES = SQUARE_SIZE * SQUARE_SIZE;
	public static int MAX_VALUE = MIN_VALUE + NUM_VALUES - 1;
	public static int POSSIBLE_CHOICES = NUM_VALUES * GRID_SIZE;
	public static int TYPES_OF_CONSTRAINTS = 4;
	public static int NUM_CONSTRAINTS = GRID_SIZE * TYPES_OF_CONSTRAINTS;
	public static int ROW_COLUMN_INDEX = 0;
	public static int BOX_NUMBER_INDEX = ROW_COLUMN_INDEX + GRID_SIZE;
	public static int ROW_NUMBER_INDEX = BOX_NUMBER_INDEX + GRID_SIZE;
	public static int COL_NUMBER_INDEX = ROW_NUMBER_INDEX + GRID_SIZE;
	
	
	private int[][] R, C;
	public void genmat() {
		R = new int[NUM_CONSTRAINTS][NUM_VALUES];		//Rules.
		C = new int[POSSIBLE_CHOICES][TYPES_OF_CONSTRAINTS];	//Constraints.
		int[] nr = new int[NUM_CONSTRAINTS];
		int i, j, k, r, c, c2;
		for (i = r = 0; i < GRID_LENGTH; ++i) // generate c[729][4]
			for (j = 0; j < GRID_LENGTH; ++j)
				for (k = 0; k < NUM_VALUES; ++k) { //Each cell has 16 possible numbers.
					C[r][0] = NUM_VALUES * i + j + ROW_COLUMN_INDEX; // row-column constraint
					C[r][1] = (i/NUM_SQUARES*NUM_SQUARES + j/NUM_SQUARES) * NUM_VALUES + k 
							+ BOX_NUMBER_INDEX; // box-number constraint
					C[r][2] = NUM_VALUES * i + k + ROW_NUMBER_INDEX; // row-number constraint
					C[r][3] = NUM_VALUES * j + k + COL_NUMBER_INDEX; // col-number constraint
					++r;
				}
		for (c = 0; c < NUM_CONSTRAINTS; ++c) nr[c] = 0;
		for (r = 0; r < POSSIBLE_CHOICES; ++r) // generate r[][] from c[][]
			for (c2 = 0; c2 < TYPES_OF_CONSTRAINTS; ++c2) {
				k = C[r][c2]; R[k][nr[k]++] = r;
			}
	}
	private int sd_update(int[] sr, int[] sc, int r, int v) {
		int c2, min = NUM_VALUES + 1, min_c = 0;
		for (c2 = 0; c2 < TYPES_OF_CONSTRAINTS; ++c2) sc[C[r][c2]] += v<<7; //The author took a lot of pains to make things optimized, such
										//as with this bit-shifting. What he's really doing here is maintaining
										//2 variables in one 32 bit integer.
		for (c2 = 0; c2 < TYPES_OF_CONSTRAINTS; ++c2) { // update # available choices
			int r2, rr, cc2, c = C[r][c2];
			if (v > 0) { // move forward //He uses the same function for moving forward and backtracking. v represents whether to move forward.
				for (r2 = 0; r2 < GRID_LENGTH; ++r2) {
					if (sr[rr = R[c][r2]]++ != 0) continue; // update the row status
					for (cc2 = 0; cc2 < TYPES_OF_CONSTRAINTS; ++cc2) {
						int cc = C[rr][cc2];
						if (--sc[cc] < min) { // update # allowed choices
							min = sc[cc]; min_c = cc; // register the minimum number
							//Find the constraint with the minimum number of possible choices.
						}
					}
				}
			} else { // revert
				int[] p;
				for (r2 = 0; r2 < GRID_LENGTH; ++r2) {
					if (--sr[rr = R[c][r2]] != 0) continue; // update the row status
					p = C[rr]; ++sc[p[0]]; ++sc[p[1]]; ++sc[p[2]]; ++sc[p[3]]; // update the count array
				}
			}
		}
		return min<<16 | min_c; // return the col that has been modified and with the minimal available choices
		//Same thing with the bit-shifting, he's returning 2 numbers at once: which constraint to choose next, and how many choices it has.
	}
	// solve a Sudoku; _s is the standard dot/number representation
	public String solve(String _s) {
		int i, j, r, c, r2, dir, cand, min, hints = 0; // dir=1: forward; dir=-1: backtrack
		int[] sr = new int[POSSIBLE_CHOICES];
		int[] cr = new int[GRID_SIZE];
		int[] sc = new int[NUM_CONSTRAINTS];
		int[] cc = new int[GRID_SIZE];
		int[] out = new int[GRID_SIZE];
		for (r = 0; r < POSSIBLE_CHOICES; ++r) sr[r] = 0; // no row is forbidden
		for (c = 0; c < NUM_CONSTRAINTS; ++c) sc[c] = 0<<7|NUM_VALUES; // 16 allowed choices; no constraint has been used //More bit-shifting.
		for (i = 0; i < GRID_SIZE; ++i) {
			//I wrote this bit, the original character decoder only worked for 1 - 9.
			int a = Character.digit(_s.charAt(i), 16); //number from -1 to 15
			//ALSO SWITCH THIS LINE.
			//int a = _s.charAt(i) >= '1' && _s.charAt(i) <= '9'? _s.codePointAt(i) - '1' : -1; // number from -1 to 8
			if (a >= 0) sd_update(sr, sc, i * GRID_LENGTH + a, 1); // set the choice
			if (a >= 0) ++hints; // count the number of hints
			cr[i] = cc[i] = -1; out[i] = a;
		}
		i = 0; dir = 1; cand = (NUM_VALUES + 1)<<16|0; //This value corresponds to the return from the function, this is a default used on the first iteration.
		for (;;) {
			while (i >= 0 && i < GRID_SIZE - hints) { // maximum 81-hints steps
				if (dir == 1) {
					min = cand>>16; cc[i] = cand&0xffff; //Break cand into its 2 composite variables.
					if (min > 1) {
						for (c = 0; c < NUM_CONSTRAINTS; ++c) {
							if (sc[c] < min) {
								min = sc[c]; cc[i] = c; // choose the top constraint
								if (min <= 1) break; // this is for acceleration; slower without this line
							}
						}
					}
					if (min == 0 || min == (NUM_VALUES + 1)) cr[i--] = dir = -1; // backtrack
				}
				c = cc[i];
				if (dir == -1 && cr[i] >= 0) sd_update(sr, sc, R[c][cr[i]], -1); // revert the choice
				for (r2 = cr[i] + 1; r2 < NUM_VALUES; ++r2) // search for the choice to make
					if (sr[R[c][r2]] == 0) break; // found if the state equals 0
				if (r2 < NUM_VALUES) {
					cand = sd_update(sr, sc, R[c][r2], 1); // set the choice
					cr[i++] = r2; dir = 1; // moving forward
				} else cr[i--] = dir = -1; // backtrack
			}
			if (i < 0) break;
			
			//Create the output string.
			char[] y = new char[GRID_SIZE];
			//for (j = 0; j < GRID_SIZE; ++j) y[j] = (char)(out[j] + '1');
			for (j = 0; j < GRID_SIZE; ++j) y[j] = Character.toUpperCase(Character.forDigit(out[j], 16));
			//for (j = 0; j < i; ++j) { r = R[cc[j]][cr[j]]; y[r/GRID_LENGTH] = (char)(r%GRID_LENGTH + '1'); }
			for (j = 0; j < i; ++j) {
				r = R[cc[j]][cr[j]];
				y[r/GRID_LENGTH] = Character.toUpperCase(Character.forDigit(r%GRID_LENGTH,16));
			}
			return new String(y);
		}
		return null;
	}

	public static String solve_Java(String puzzle) {
		JavaSolver s = new JavaSolver();
		s.genmat();
		String output = s.solve(puzzle);
		if (verify(output))
			return output;
		else
			return "No valid solution.";
	}

	public static boolean verify(String puzzle) {
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				if (i != j && puzzle.charAt(i) == puzzle.charAt(j)) {
					if (i/16 == j/16) //Same row.
						return false;
					if ((i - j) % 16 == 0) //Same column.
						return false;
					if ((i/(4*4*4)) == (j/(4*4*4)) && ((i%16)/4 == (j%16)/4)) // Same box.
						return false;
				}
			}
		}

		return true;
	}

	/* Test main.
	public static void main(String[] args) throws Exception {
		String problems[] = {
				"5....A.1...9........8..F....2C.....4B2...5.3D..A.26A.C..B47..183.4.F...26...0E....A.0...4..56...6C0.EF..83A...9...7.3..8.......43...D...07...51..9.E1....8..FA....1...4.A.......F..7.9..E2..8.6.......7..A........B.2.9....0.3E876.......B..5.C.CD.2..5.71.6B4..",
				"...F.E......C18A...B.C.A.2..3F9.C....9....D.E....4..FB.D...56....FB.0A..C..E..568.C......6..D.AE.D569....F7.....E...7..4D.B.....19..87ACB........B....9.23.....C0..A6DBF5..7...3..6.35.E..9..........4...0E.9.......E..61B82.....3.D..F.47.A8B.2.8..D.........F.",
				"......DABF.4..95.15.....3E92...CC.......8.7......D.7EB2...1......6E.7241F..85B..B.A..8CF9.6.D..4...4....D7C...E...8........B.......B6.F..3....C.A..6C7..E..1...FF....0B.....86.....521.....D....9....A7E..3.CD..0.C..4.9A8...7.B.4F81.......E..A.B6..F0......9..",
				"13.D..5....E.B...0B....7..4..8....9...A8.7.3F.6D....E...F.16....D.1.93E5..BF.....A.5DF...3...7.C..8...72.......FE...8..A....34....EA.D..9.0C..4.3CF..8...46...B.B.6..4.07...E.8A..D8...E.1.A7..9....B...5.....F8......6..9.....7....F...DC...3..2B.6C..9..F0.E..",
				".1....BD....3.............47.8F..04F8..9..........9..7....ED.B.C....68..3..57.4D2D.7..5.49.E...8....2.....A...1.......E..0..A5.B.40...6.....B.E2..5E0.1.D..BF........3..1.5.94A.......8.2A..5..3..F4..3.0B..8..A..C.7.F..D......9.3..0C8.2.6.F...A2.5...73...96."
		};
		String solutions[] = {
				"5EC87A31D6294B0F973B84DF1E0A2C5610F4B2E6C583D97AD26A5C09B47FE183845F97A26D1B0E3C23A90D1C4FE568B76C0DEFB483A71295EB71356890C2ADF43A26D8FE07B4C51909DE1327586CFA4BB81C6045A9FD372EF547A9CBE231806D4FE3CB702A5896D1A1B5269DFC4073E87690418A3BDE5FC2CD82FE537196B4A0",
				"20DF5E67943BC18A5E7B1C4A82063F9DC6A8293071DFE5B43491FB8DECA562709FB70AD3C82E145682C0BF153649D7AE4D5698E2AF7103CBEA1376C4D5B0F829193287ACBD645E0FDBE5409123F87A6C0C8A6DBF5E172943F764352E0A9CBD18B12CA478F0ED9635A5F9E3061B824CD7630DC1F9475A8BE2784ED25B69C3A0F1",
				"682E3CDABF0471954150F6873E92BADCCABF95108D763E423D97EB245C1A6F80D6EC7241F0A85B39B7A308CF956ED2141204A95BD7C3F8E65F89D3E6124B0CA7294B6EFD0387A5C1A0D6C798E45123BFFE3140B5CA29867D8C7521A36BFD940E9512BA7E4630CDF803CD5469A8EF172B74F81D3C29B5E06AEB6A8F0271DC4953",
				"13AD605F82CE9B7460BF1237AD49C85ECE924BA80753F16D8574E9DCFB16A032D71C93E540BF82A69A25DF4B638107EC46830172CEADB59FEF0B86CA259734D152EA7DB6980C1F433CF7A891E4625DB0B16934207FD5EC8A04D85CFEB13A7629ADC1BE03567429F8F830256D19EB4AC7795EFA14DC28630B2B46C7893AF0DE15",
				"71E6CFBD98203A54A2DC3105B647E8F9B04F8EA9C531D72683954726AFED1B0CFEA0689B31C5724D2D17FA5049BE63C85CB92D4367A80E1F48631CE7F0D2A59BD40A956F8C73B1E2395E021AD46BFC876B82D37C1E5F94A0CF71B48E2A0956D315F4E6320B9C8D7AE6CB79F45D8A2031973DA0C8E2164FB50A285BD173F4C96E"
		};
		
		JavaSolver a = new JavaSolver();
		a.genmat();
		
		for (int i = 0; i < problems.length; i++) {
			String output = a.solve(problems[i]);
			if (output.equals(solutions[i])) {
				System.out.println(i + ": match!");
			} else {
				System.out.println(i + ": no match!");
				System.out.println("Expected:\n" + solutions[i]);
				System.out.println("Was:\n" + output);
			}
		}
	}
	*/
}
