#!/usr/bin/python

def min_elt2(arr):
	mina=arr[0]
	for a in arr:
		if a < mina : 
			mina=a
	return mina

array = [5, 2, 3]

print(min_elt2(array))	