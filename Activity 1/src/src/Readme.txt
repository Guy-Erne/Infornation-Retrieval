README

!!!!NOTE: ALL THE ARGUMENT FILES SHOULD BE FIRST PRESENT IN SAME FOLDER where .class FILE IS.
	  INPUT FILE IS TAKEN FROM CURRENT DIRECTORY!!!!


Task1
--------------------------
javac task1.java
java task1 source.txt

source.txt follows pattern as provided on moodle pt1_results.txt file.

output is shown on console + creates a file EvaluationTask1.txt file.




Task2
------------------------------------
javac task2.java
javac task2 pt2_evalABC.txt pt2_weights.txt

pt2_evalABC.txt 
	- evaluation file with each engine evaluation in a column
	- file structure follows that from moodle.
	
pt2_weights.txt 
	- weights file in single line 
	-file structure follows same to that from moodle.
	
	
	
Task3
---------------------------------------------
javac task3.java
java task3 source.txt 4 live.txt
	source.txt - historical data file from task1
	integer number between 3-20 -> this argument is used for segmentation
	live.txt - test data file.  Follows following order.
	
	A;[15,22,1,56,38,66,141,87,103,88,216,101,213,194,35,65,162,152,44,31]	B;[65,38,194,22,1,141,56,31,103,15,66,213,88,35,101,152,44,87,216,162]	C;[152,213,44,66,87,1,22,56,101,162,141,38,31,15,65,216,88,103,35,194]
	
	{EngineID;[DocumentRetrievals]\t}+