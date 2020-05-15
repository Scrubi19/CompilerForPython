#!/usr/bin/python

int array = [5, 2, 3]

def min_elt2(arr):
	int mina=arr[0]
	for a in arr:
		if a < mina : 
			mina=a
	return mina	
	
print(min_elt2(array))