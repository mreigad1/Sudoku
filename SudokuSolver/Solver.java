public class Solver {
	int puzzle[16][16];

	public String solve(String input) {
		for (int i = 0; i < 16; i++)
			for (int j = 0; j < 16; j++)
				puzzle[i][j] = -1;

		//Read input string.
		
		boolean solutionFound = solve(0, 0);
	}

	private boolean solve(int row, int column) {
		// >= 0 means the cell was initially filled.
		if (puzzle[row][column] < 0) {
			


		} else {
			if (column < 16) {
				return solve(row, column + 1);
			} else {
				return solve(row + 1, 0);
			}
		}

		boolean solutionFound;
		if (column < 16) {
			solutionFound = solve(row, column + 1);
		} else {
			solutionFound = solve(row + 1, 0);
		}
		if (!solutionFound) {
			//Reset this cell so we don't assume it was initially filled.
			puzzle[row][column] -1;
		}

		return solutionFound;

	}
}
