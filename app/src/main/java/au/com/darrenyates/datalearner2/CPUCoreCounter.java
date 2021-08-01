/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
	DataLearner 2 - a data-mining app for Android
	CPUCoreCounter.java
    (C) Copyright Darren Yates 2018-2021
	Developed using a combination of Weka 3.8.5 and algorithms developed by Charles Sturt University
	DataLearner is licensed GPLv3.0, source code is available on GitHub
	Weka 3.8.5 is licensed GPLv3.0, source code is available on GitHub
*/

package au.com.darrenyates.datalearner2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CPUCoreCounter {
	int coreCount;
	
	public CPUCoreCounter () {
		
		try {
			String[] args = new String[] {"/system/bin/cat", "/proc/cpuinfo"};
			byte[] byteArray = new byte[4096];
			String output = "";
			Process proc  = new ProcessBuilder(args).start();
			InputStream inputStream = proc.getInputStream();

//			while (inputStream.read(byteArray)!= -1) {
//				output += new String(byteArray);
//			}
			BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder sb = new StringBuilder();
			for (String line; (line = r.readLine()) != null; ) {
				sb.append(line).append('\n');
			}
			output = sb.toString();
//			System.out.println(output);
			inputStream.close();
			output = output.toLowerCase();
			String[] cores = output.split("\n");
			String previouscore = "";
			int[] coreCounts = new int[4];
			int coreno = -1;
			output = "";
			for (int i = 0; i < cores.length-1; i++) {
				if (cores[i].contains("cpu part")) {
					String[] partno = cores[i].split(":");
					if (!partno[1].trim().equals(previouscore)) coreno++;
					coreCounts[coreno]++;
					previouscore = partno[1].trim();
				}
			}
			int minCount = Integer.MAX_VALUE;
			for (int i = 0; i < coreCounts.length-1; i++) {
				if (coreCounts[i] > 0 && coreCounts[i] < minCount) minCount = coreCounts[i];
			}
			int totalCores = Runtime.getRuntime().availableProcessors();
			if (totalCores >= 8 && totalCores == minCount) minCount = totalCores/2;
			coreCount = minCount;
//
//
//			coreCount = totalCores;
//
//
//
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
