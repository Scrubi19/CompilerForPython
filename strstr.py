#!/usr/bin/python

def strstr(text, sub):
    txt = text.split(sub)
    out = []
    if len(txt)==1:
        return None
    elif len(txt)==2:
        return text.index(txt[0]) + len(txt[0])
    else:
        for i in txt:
            if i != txt[len(txt)-1]:
                out.append(text.index(i) + len(i))
        return out

text = 'StringForExample'
sub = 'For'

print("Index=", strstr(text, sub))