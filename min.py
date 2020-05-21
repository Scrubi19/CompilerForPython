#!/usr/bin/python

array = [5, 1, 3]

def min_elt2(arr):
	mina=arr[0]
	for a in arr:
		if a < mina : 
			mina=a
	return mina	
	
print(min_elt2(array))