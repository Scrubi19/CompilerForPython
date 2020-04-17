#!/usr/bin/python
a=int(input())
b=int(input())

while a!=0 and b!=0:
	if a>b:
		a%=b
	else:
		b%=a
		
gcd=a+b
gcd = "str" + s

# print('Func Result =', gcd)

# gcd = a + "str"