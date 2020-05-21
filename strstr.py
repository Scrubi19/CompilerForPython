#!/usr/bin/python

str1 = "searching string index"
str2 = "string"

	i = 0
	j = 0
	m = len(str1)
	n = len(haystack)
	if m ==0:
		return 0
	while i<n and n-i+1>=m:
    	if haystack[i] == str1[j]:
        	temp = i
		while j<m and i<n and str1[j]==haystack[i]:
        	i+=1
        	j+=1
		if j == m:
        	return temp
		i= temp+1
		j = 0
	else:
		i+=1
	return -1

print(strStr(haystack, str1))