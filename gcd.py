#!/usr/bin/python
a=10
b=2

while a!=0 and b!=0:
	if a>b:
		a%=b
	else:
		b%=a
		
gcd=a+b

print('Func Result =', gcd)