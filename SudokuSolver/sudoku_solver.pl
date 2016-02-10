/* TEAM MEMBERS:  */
/* MATT REIGADA   */
/* SREEDHAR KUMAR */
/* ELLIOT WAY     */
/* Algorithm based on code found at: */
/* http://programmablelife.blogspot.co.at/2012/07/adventures-in-declarative-programming.html */

:- use_module(library(clpfd)).

/*solve_prolog receives list X, verifies that X is length 256*/
/*remaps hex char atomics to numeral atomics, solves and returns string R with answer*/
solve_prolog(X,R):-
  X = [  
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,
  _,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_
  ],
  remap(Y,X), /*create Y with hex vals*/
  sudoSolver(Y), /*solve sudoku puzzle Y*/
  remap(Y,X),  /*X now has no open variables, close open variables in Y*/
  atomic_list_concat(X,R).  /*unify R with string for solution*/

/*Open variables should trivially unify*/
num_map(X,Y):-var(Y),var(X).
/*Otherwise remap between valid decimals and hex chars*/
num_map(I,I):-
  nonvar(I), number(I), I < 10, I >= 0.
num_map(10,a).
num_map(11,b).
num_map(12,c).
num_map(13,d).
num_map(14,e).
num_map(15,f).

/*remap between hex char and numeral atomic list*/
remap([],[]).
remap([H1|T1],[H2|T2]):- num_map(H1,H2), remap(T1,T2).

/**to matrix receives list X and makes matrix Y*/
to_matrix([],[]).
to_matrix(X,Y):-
  X = [A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P|TailX],
  Y = [[A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P]|TailY],
  to_matrix(TailX, TailY).

/*creates matrix from list and solves sudoku*/
sudoSolver(X):-
  to_matrix(X,Y),
  Y = [A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P],  
  sudoku(Y).

/*algorithm for solving 16x16 sudoku matrix*/
sudoku(Row) :-  
  append(Row, Vars),
  Vars ins 0..15,
  maplist(all_distinct, Row),
  transpose(Row, Col),     
  maplist(all_distinct, Col),     
  Row = [A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P],
  blocks(A,B,C,D),
  blocks(E,F,G,H),
  blocks(I,J,K,L),
  blocks(M,N,O,P),
  maplist(label, Row).

/*checks each block in matrix is unique*/ 
blocks([], [], [], []).
blocks([A,B,C,D|BL1], [E,F,G,H|BL2], [I,J,K,L|BL3], [M,N,O,P|BL4]) :-
  all_distinct([A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P]),      
  blocks(BL1, BL2, BL3, BL4).

