#!/bin/bash

case $1 in
	"--dump-tokens"	) 
		java -classpath ./target/classes Main --dump-tokens $2
	;;

	"--dump-ast"	) 
		java -classpath ./target/classes Main --dump-ast $2
	;;

	"--dump-asm"	) 
		java -classpath ./target/classes Main --dump-asm $2
	;;

	"compile"	) 
		mvn compile
	;;
esac

case $1 in
	"gcd.py"	) 
		java -classpath ./target/classes Main --dump-tokens gcd.py
	;;

	"min.py"	) 
		java -classpath ./target/classes Main --dump-ast min.py
	;;

	"strstr.py"	) 
		java -classpath ./target/classes Main --dump-asm strstr.py
	;;
esac

