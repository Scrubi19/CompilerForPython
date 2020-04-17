#!/usr/bin/python
a=int(input())
b=int(input())

while a!=0 and b!=0:
	if a>b:
		a%=b
	else:
		b%=a

gcd= a + b

# Variable for test SemanticAnalysis

# var1 = 5.5 +"str"

# var2 = 5 + 2.3

# var3 = 155.2 + 10

# var3 = 223 + "str"

# var4 = "str" + 33