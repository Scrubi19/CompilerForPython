#!/usr/bin/python

stack = "searching string index"
search = "ing"

i = 0
j = 0
count = 0

n = len(stack)
m = len(search)

while i < n:
	if stack[i] == search[j]:
		j+=1
		count+=1
	else:
		j = 0
		count = 0
	if m == count:
		print("is substring")
		i = n
	i+=1
