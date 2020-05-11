all:
	mvn compile

startTest:
	java -classpath ./target/classes Main test.py

startGcd:
	java -classpath ./target/classes Main gcd.py
	
startMin:
	java -classpath ./target/classes Main min.py

startAsm:
	java -classpath ./target/classes Main --dump-asm gcd.py