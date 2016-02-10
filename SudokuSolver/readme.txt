Elliot Way, Sreedhar Kumar, Matthew Reigada
CS 571 Final Project

To compile: make

To run: java SudokuSolver -lang=<lang> -o <output_file> <input_file>

ie, the format given in the assignment. The order of the arguments must be the
	same, the program will complain if there's a problem.
Input files MUST be 256 puzzle characters followed by \n.
Input files can be in either upper case hex or lower case hex.
Output puzzle will be in upper case hex.
Output times include overhead of process creation and similar.

Included are several input files named *Input.txt.
Only java and prolog solve hardInput.txt in a reasoable time, invalidInput.txt
causes some variation on "No valid solution" to be returned.
