/*

Group - 4
Matt Reigada
Sreedhar Kumar
Elliot Way

email: skumar19@binghamton.edu

Algorithm based on the code found at:  http://pythontips.com/2013/09/01/sudoku-solver-in-python/

*/

//Checks if two cells belong to the same row
function same_row(i,j){
	return Math.floor(i/16) == Math.floor(j/16);
}

//Checks if two cells belong to the same column
function same_col(i,j){
	return ((i-j) % 16 == 0);
}

//Checks if two cells belong to the same block
function same_block(i,j){
	return ( (Math.floor((i/(4*4*4)))) == (Math.floor(j/(4*4*4))) && 
		 (Math.floor((i%16)/4)) == (Math.floor((j%16)/4)) );
}


//Verifier function that verifies that each entry in the puzzle doesn't violate the constraints
function verify(output){

		for(var i=0; i<256; i++){

			for(var j=0; j<256; j++){

				if(same_row(i,j)||same_col(i,j)||same_block(i,j)){

					if(output[i]==output[j] && i!=j){
						return false;
					}
				}
			}

		}
		return true;

}

//A find function that checks if the given character is present and if present - returns the first index of its occurence 
function find(a, c){
	for(var i = 0; i < a.length; i++){
      		if(a[i] == c){
        		return i;
		}
	}
	return -1;
}

//The generate function that processes the input string and generates the output string that solves the Hexadoku
function r(a){
	var i = find(a,'.');

//If there are no blanks, this block verifies if the solution is valid
	if(i < 0){

		var v = verify(a);

//If block that returns the solution if valid

		if(v){

			return a;
		}//Else block that executes if there are no valid solutions
		else{
			print("\n\nThere is no valid solution\n\n");

			return -1;
		}
		
	}

//Code block that adds the characters to be excluded in to an array

	var ex_num = [];
	for(var j = 0; j < (16*16); j++){
		if(same_row(i,j) || same_col(i,j) || same_block(i,j)){

			ex_num.push(a[j]);
		}
	}

//Recursive code block that generates the required solution

	var c = "0123456789ABCDEF";
	for(var j = 0; j < c.length; j++){

		var found = find(ex_num,c[j]);

//If the character is not in the excluded list, we attempt to recurse with it 

		if(found < 0){
			var rec_val = [];
			for(var x = 0; x < a.length; x++){
				rec_val.push(a[x]);
			}
			rec_val[i] = c[j];
			var tmp = r(rec_val);
			if(tmp != -1){
				return tmp;
			}
		}
	}

	return -1;
}

//The JS solver function
function solve_JS(str){
    
    var j = r(str);
    if(j!=-1){
		j=j.join("");
		return j;
    } 
    return "No valid solution.";
}

