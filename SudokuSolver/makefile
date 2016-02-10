#Elliot Way, Sreedhar Kumar, Matthew Reigada
#Makefile for CS571 Final Project
#16x16 Sudoku Solvers in Various Languages
#
#No rules for prolog and python since they're interpreted.

all: SudokuSolver.class

SudokuSolver.class: SudokuSolver.java JavaSolver.java libhexadoku.so
	javac $< JavaSolver.java

libhexadoku.so:  hexadoku.c SudokuSolver.h
	gcc hexadoku.c -I/usr/lib/jvm/default-java/include -o $@ -shared -rdynamic -fPIC -lc


SudokuSolver.h:
	javah -jni SudokuSolver

clean:
	rm -f *.class libhexadoku.so SudokuSolver.h SudokuSolver_Language.h
