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
	TrainTestSplit.java
    (C) Copyright Darren Yates 2018-2021
	Developed using a combination of Weka 3.8.5 and algorithms developed by Charles Sturt University
	DataLearner is licensed GPLv3.0, source code is available on GitHub
	Weka 3.8.5 is licensed GPLv3.0, source code is available on GitHub
*/

package au.com.darrenyates.datalearner2;

import java.util.Random;
import weka.core.Instances;

public class TrainTestSplit {
	Instances train;
	Instances test;
	
	
	public void initialise(Instances newdata) {
		Instances datacopy = new Instances(newdata);
		datacopy.randomize(new Random(1));
		int trainSize = (int) Math.round(datacopy.numInstances() * 0.66);
		int testSize = datacopy.numInstances() - trainSize;
		train = new Instances(datacopy, 0, trainSize);
		test = new Instances(datacopy, trainSize, testSize);
	}
	
}
