#!/bin/bash

if [[ -n "$2" ]]
	then
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
	esac
fi

if [[ -n "$1" ]]
	then
		if [[ $1 == "compile" ]]
			then
			mvn compile
		else
			java -classpath ./target/classes Main $1
		fi
	else 
		echo "No parameters found. "
fi
