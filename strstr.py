#!/usr/bin/python

def str_str(text, sub):
	return text.find(sub)

str1 = "searching string index"
str2 = "string"

print(str_str(str1, str2))