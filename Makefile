all:
	mvn compile

startTest:
	java -classpath ./target/classes Main test.py

startGcd:
	java -classpath ./target/classes Main prog.py
	
startMin:
	java -classpath ./target/classes Main min.py