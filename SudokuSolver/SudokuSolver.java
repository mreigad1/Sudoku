import java.util.regex.*;
import java.nio.file.*;
import java.io.*;
import java.util.ArrayList;
import javax.script.*; //For javascript.

public class SudokuSolver {

	private static final String JS_FILE = "hexadoku.js";

	private static final String PYTHON_INTERPRETER = "python";
	private static final String PYTHON_FILE = "sudoku_solver.py";

	private static final String PROLOG_FILE = "sudoku_solver.pl";

	static {
		//Load library to access C code through JNI.
		try {
			String currentDir = new File(".").getCanonicalPath();
			String cHexadokuLib = currentDir + File.separator + "libhexadoku.so";
			System.load(cHexadokuLib);
		} catch (IOException e) {
			System.err.println("Error: failed to load c library.");
		}
	}

	public static void main(String[] args) {

		if (args.length < 4) {
			System.out.println("Too few arguments. Usage is:\n" +
					"-lang=<language> -o <outputfile> <inputfile>\n" +
					"where <language> is one of: c, java, prolog, python, js");
			return;
		}

		//Read in the language parameter.
		Matcher langMatcher = Pattern.compile("-lang=(.*)$").matcher(args[0]);
		if (!langMatcher.matches()) {
			System.out.println("Bad language arg: " + args[0] + "\n" +
					"Needs format -lang=<language>");
			return;
		}

		String langString = langMatcher.group(1); //Matches inside the (), group 0 is the whole expression.

		Language language;
		if (langString.equals("c")) {
			language = Language.C;
		} else if (langString.equals("java")) {
			language = Language.JAVA;
		} else if (langString.equals("prolog")) {
			language = Language.PROLOG;
		} else if (langString.equals("python")) {
			language = Language.PYTHON;
		} else if (langString.equals("js")) {
			language = Language.JS;
		} else {
			System.out.println("Bad language: " + langString + "\n" +
					"Valid languages are: c, java, prolog, python, js");
			return;
		}

		String dashO = args[1];
		if (!dashO.equals("-o")) {
			System.out.println("Weird 2nd token: " + args[1] + "\n" +
					"Needs to be -o");
			return;
		}

		String outFileName = args[2];
		String inFileName = args[3];

		//Read in the input file.
		String inString;
		try {
			byte[] bytes = Files.readAllBytes(Paths.get(inFileName));
			inString = new String(bytes);
		} catch (IOException e) {
			System.out.println("There was an error reading input file \"" + inFileName + "\"\n" +
					e.getMessage());
			return;
		}

		//Check for good input.
		String puzzle;
		puzzle = inString.substring(0, inString.length() - 2).toUpperCase(); //Cut off the \n, convert to uppercase hex.
		
		//Two test cases. Uncomment to use them instead.
		//puzzle = "5....A.1...9........8..F....2C.....4B2...5.3D..A.26A.C..B47..183.4.F...26...0E....A.0...4..56...6C0.EF..83A...9...7.3..8.......43...D...07...51..9.E1....8..FA....1...4.A.......F..7.9..E2..8.6.......7..A........B.2.9....0.3E876.......B..5.C.CD.2..5.71.6B4..";
		//puzzle = "................................................................................................................................................................................................................................................................";
		if (puzzle.length() != 256) {
			System.out.println("Error, input length is " + inString.length() + ", needs to be 256.");
			return;
		}
		
		//Solve the puzzle.
		String outString = "";
		long startTime = System.nanoTime();
		switch (language) {
			case C:
				outString = solve_c(puzzle);
				break;
			case JAVA:
				outString = solve_java(puzzle);
				break;
			case PROLOG:
				outString = solve_prolog(puzzle);
				break;
			case PYTHON:
				outString = solve_python(puzzle);
				break;
			case JS:
				outString = solve_JS(puzzle);
				break;
		}
		long endTime = System.nanoTime();
		long duration = endTime - startTime;

		//Write to the output file.
		String timeString = String.format("Time taken = %.3f seconds\n", ((double) duration) / 1_000_000_000.0);

		try {
			String all = outString + "\n" + timeString;
			Files.write(Paths.get(outFileName), all.getBytes());
		} catch (IOException e) {
			System.out.println("There was an error writing to output file \"" + outFileName + "\"\n" +
					e.getMessage() + "\nSolution was: " + outString);
		}

		//Done.
	}

	private static String solve_JS(String puzzle) {
		try {
			ScriptEngineManager factory = new ScriptEngineManager();
			ScriptEngine engine = factory.getEngineByName("JavaScript");
			engine.eval(new FileReader(JS_FILE));
			Invocable invocable = (Invocable) engine;
			String output = (String) invocable.invokeFunction("solve_JS", puzzle);

			return output;
		} catch (FileNotFoundException e) {
			System.out.println("JavaScript file \"" + JS_FILE + "\" missing.\n" + e.getMessage());
			return e.getMessage();
		} catch (ScriptException e) {
			System.out.println("Error parsing JavaScript. Shouldn't happen unless you changed javascript.\n" + e.getMessage());
			return e.getMessage();
		} catch (NoSuchMethodException e) {
			System.out.println("Error parsing JavaScript. Shouldn't happen unless you changed javascript.\n" + e.getMessage());
			return e.getMessage();
		}
	}

	private static String solve_java(String puzzle) {
		return JavaSolver.solve_Java(puzzle);
	}

	private native static String solve_c(String puzzle);

	private static String solve_python(String puzzle) {
		try {
			Process process = new ProcessBuilder(PYTHON_INTERPRETER, PYTHON_FILE, puzzle).start();
			InputStream processOutput = process.getInputStream();
			OutputStream processInput = process.getOutputStream();

			String line = readUntil(processOutput, "\n");

			process.destroy(); //It should be dead by now, but just in case.

			return line;
		} catch (IOException e) {
			System.out.println("Error running python process.\n" + e.getMessage());
			return e.getMessage();
		}
	}

	private static String solve_prolog(String puzzleUpCase) {
		try {	
			String puzzle = puzzleUpCase.toLowerCase(); //Prolog code expects lower case.

			Process process = new ProcessBuilder("swipl", "-s", PROLOG_FILE).start();
			InputStream processOutput = process.getErrorStream();
			OutputStream processInput = process.getOutputStream();

			//Skip over 17 lines to get to the prompt.
			//I tried to skip until "\n?- " but the prompt isn't written to stderr or stdout.
			for (int i = 0; i < 17; i++) {
				readUntil(processOutput, "\n");
				//System.out.println("read: " + line);
			}

			String command = "solve_prolog([" + (puzzle.charAt(0) == '.' ? '_' : puzzle.charAt(0));
			for (int i = 1; i < puzzle.length(); i++) {
				if (puzzle.charAt(i) == '.')
					command += ", _";
				else
					command += ", " + puzzle.charAt(i);
			}
			command += "], Y).\n";

			processInput.write(command.getBytes());
			processInput.flush();

			//Next line...
			readUntil(processOutput, "\n");

			char next = (char) processOutput.read();
			String output;
			if (next == 'f') {
				output = "No valid solution.";
			} else {
				//next == Y
				//Skip over a few characters.
				readUntil(processOutput, " = '");
				//Then read until the next quote.
				output = readUntil(processOutput, "'");
			}

			//System.out.println("read: " + output);

			process.destroy();

			return output.toUpperCase();
		} catch (IOException e) {
			System.out.println("Error running prolog process.\n" + e.getMessage());
			return e.getMessage();
		}
	}

	/**
	 * Read a string from the stream until the separator string is reached.
	 * Not terribly efficient; I would rewrite this with char arrays as buffers
	 * if efficiency was important. Output string does not include the separator.
	 */
	private static String readUntil(InputStream in, String separator) throws IOException {
		ArrayList<Character> chars = new ArrayList<Character>();
		int cnt = 0;
		char next = (char) in.read();
		int separatorIndex = 0;
		while (cnt < 100000) {
			if (next == separator.charAt(separatorIndex)) {
				separatorIndex++;
				if (separatorIndex == separator.length())
					break;
			} else {
				//Copy out the partial separator.
				if (separatorIndex > 0) {
					for (int i = 0; i < separatorIndex; i++) {
						chars.add(separator.charAt(i));
						cnt++;
					}
					separatorIndex = 0;
				}

				chars.add(next);
				cnt++;
			}
			next = (char) in.read();
		}
		//System.out.println(cnt + " chars read from process.");
		Character[] charArr = new Character[chars.size()];
		charArr = chars.toArray(charArr);
		String out = new String();
		for (int i = 0; i < charArr.length; i++)
			out += charArr[i];
		return out;
	}

	private enum Language {
		C, JAVA, PROLOG, PYTHON, JS
	}

}
