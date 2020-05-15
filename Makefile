all:
	mvn compile

compile:
	mvn compile

startMin:
	java -classpath ./target/classes Main min.py	

startMin_Tokens:
	java -classpath ./target/classes Main --dump-tokens min.py

startMin_Ast:
	java -classpath ./target/classes Main --dump-ast min.py

startGcd:
	java -classpath ./target/classes Main gcd.py

startGcd_Tokens:
	java -classpath ./target/classes Main --dump-tokens gcd.py

startGcd_Ast:
	java -classpath ./target/classes Main --dump-ast gcd.py

startGcd_ASM:
	java -classpath ./target/classes Main --dump-asm gcd.py