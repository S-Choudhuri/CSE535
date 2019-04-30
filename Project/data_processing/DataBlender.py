# library imports, global assignemnts
import glob
import ast
import pandas as pd
import numpy as np
import csv
buff1 = []
buff2 = []

# data, annotation files for a particular user
name = 'Shailaja'
filelist = glob.glob('./'+name+'/*_EMG.txt')
annot = './Annots/'+name+'_annots.txt'
writefile = './Out/'+name+'_compressed.csv'

# get list of all datafiles, remove empty files
for i in filelist:
	if os.stat(i).st_size==0:
		filelist.remove(i)

# parse manual annotations for a user
with open(annot,'r') as a:
	eattrue = ast.literal_eval(a.read())

# label data with manual annotation - 0:non-eating activity, 1:eating activity
def label(ts):
	for i in eattrue:
		if ts in range(i[0],i[1]+1):
			return "1"	
	return "0" 

# iterate over all valid files
for file in filelist:

	print(file)
	file1 = file
	file2 = './'+file.strip('_EMG.txt')+'_IMU.txt'
	data1arr = []
	data2arr = []

	# parse emg file 
	with open(file1,'r') as f1:
		print('emg')
		data1 = f1.read().splitlines()
		try:
			for d1 in data1:
				if d1.count('15545')==1:
					d1 = d1.split(' ',1)
					data1arr.append([int(ast.literal_eval(d1[0])/1000), ast.literal_eval(d1[1])])
				else:
					print(d1)
		except:
			print(d1)

	# parse imu file
	with open(file2,'r') as f2:
		print('imu')
		data2 = f2.read().splitlines()
		try:
			for d2 in data2:
				if d2.count('15545')==1:
					d2 = d2.split()
					data2arr.append([int(ast.literal_eval(d2[0])/1000), [ast.literal_eval(i) for i in d2[1:]] ])
				else:
					print(d2)
		except:
			print(d2)

	# identify range of timestamps from files		
	start = min(data1arr[0][0],data2arr[0][0])
	end = max(data1arr[-1][0],data2arr[-1][0])
	print(start, end)

	# define interval 
	interval = 59 
	
	while start <= end:
		endtemp = start+interval-1
		if endtemp<end:

			temparrdata2 = []
			temparrdata1 = []

			if buff1:
				data1arr = buff1+data1arr
				buff1 = []
			if buff2:
				data2arr = buff2+data2arr
				buff2 = []
			
			for j in data1arr: 
				if j[0] in range(start,endtemp+1):
					temparrdata1.append(j)
			dfr1 = [m[1] for m in temparrdata1]
			avg1 = np.mean(dfr1, axis=0)
			data1arr = data1arr[len(temparrdata1):]
			
			for i in data2arr: 
				if i[0] in range(start,endtemp+1):
					temparrdata2.append(i)
			dfr2 = [k[1] for k in temparrdata2]
			avg2 = np.mean(dfr2, axis=0)
			data2arr = data2arr[len(temparrdata2):]
			
			with open(writefile,'a+') as w:
				write = csv.writer(w)
				ts = (start+endtemp)/2
				row = [ts]+list(avg1)+list(avg2)+[label(ts),name]
				write.writerow(row)
		else: 
			buff1 = data1arr
			buff2 = data2arr
		start=start+interval
	