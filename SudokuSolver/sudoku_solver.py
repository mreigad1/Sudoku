#TEAM MEMBERS:
#MATT REIGADA
#SREEDHAR KUMAR
#ELLIOT WAY
#ALGORITHM BASED UPON CODE FOUND AT:
#http://pythontips.com/2013/09/01/sudoku-solver-in-python/

import sys

#boolean functions for checking row, columns and blocks
def same_row(i,j): return (i/16 == j/16)
def same_col(i,j): return (i-j) % 16 == 0
def same_block(i,j): return (i/(4*4*4) == j/(4*4*4) and i%16/4 == j%16/4)

def verify(outp):
  for i in range(16*16):
    for j in range(16*16):
      if same_row(i,j) or same_col(i,j) or same_block(i,j):
        if outp[i] == outp[j] and i != j:
          return False
  return True

def solve_python(a):
  #if no unassigned index then return solution
  i = a.find('.')
  if i == -1:
    v = verify(a)
    if v == True:
      print(a)
    else:
      print("No valid solution")
    sys.exit("")

  #iterate grid, determine values to exclude from solution
  excluded_numbers = set()
  for j in range(16*16):
    if same_row(i,j) or same_col(i,j) or same_block(i,j):
      excluded_numbers.add(a[j])

  #for each candidate solution spile list, recurse, check validity
  for m in '0123456789ABCDEF':
    if m not in excluded_numbers:
      solve_python(a[:i]+m+a[i+1:])

#driver code to receive command line argument for input
if __name__ == '__main__':
  if len(sys.argv) == 2 and len(sys.argv[1]) == (16*16):
    solve_python(sys.argv[1])
  else:
    print 'Usage: python sudoku.py puzzle'
    print '  where puzzle is an 256 character string representing the puzzle read left-to-right, top-to-bottom, and . is a blank'
