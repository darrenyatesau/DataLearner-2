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
	DataAnalysis3.java
    (C) Copyright Darren Yates 2018-2021
	Developed using a combination of Weka 3.8.5 and algorithms developed by Charles Sturt University
	DataLearner is licensed GPLv3.0, source code is available on GitHub
	Weka 3.8.5 is licensed GPLv3.0, source code is available on GitHub
*/

package au.com.darrenyates.datalearner2;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Random;

import weka.associations.Apriori;
import weka.associations.Associator;
import weka.associations.FPGrowth;
import weka.associations.FilteredAssociator;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.meta.RandomCommittee;
import weka.classifiers.meta.RandomSubSpace;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.FastForest;
import weka.classifiers.trees.ForestPA;
import weka.classifiers.trees.HoeffdingTree;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.classifiers.trees.SPAARC;
import weka.classifiers.trees.SimpleCart;
import weka.classifiers.trees.SysFor;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.Clusterer;
import weka.clusterers.EM;
import weka.clusterers.FarthestFirst;
import weka.clusterers.FilteredClusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import static au.com.darrenyates.datalearner2.MainActivity.isThreadRunning;
import static au.com.darrenyates.datalearner2.MainActivity.killThread;
import static au.com.darrenyates.datalearner2.MainActivity.modelLoaded;
import static au.com.darrenyates.datalearner2.MainActivity.statusUpdateStore;
import static au.com.darrenyates.datalearner2.MainActivity.traindata;
import static au.com.darrenyates.datalearner2.MainActivity.testdata;
import static au.com.darrenyates.datalearner2.NetFragment13.scroll;
import static au.com.darrenyates.datalearner2.NetFragment13.tvNetStatus;
import static au.com.darrenyates.datalearner2.SelectBFragment.alType;
import static au.com.darrenyates.datalearner2.SelectBFragment.optionsString;

class DataAnalysis3 implements Runnable {

	private Context context;
	private TextView tv;
	private TextView tvModel;
	private TextView tvsl3;
	private TextView cci;
	private TextView ici;
	private TextView kappa;
	private TextView mae;
	private TextView rmse;
	private TextView rae;
	private TextView rrse;
	private TextView tni;
	private Button btnRun;
	private Button btnCM;
	public String algorithm;
	private int validate;
	private Instances data;
	private boolean isRunning;
	private long timeBuildStart;
	private long timeBuildEnd;
	private long timeEvalStart;
	private long timeEvalEnd;
	private long timeBuild;
	private long timeEval;
//	static EvaluationTS returnEval;
	static Evaluation returnEval;
	
	static Classifier buildModel;
	static Instances buildData;

//	ClassifierDLP classifier;
	
	static String classifierTree;
	static int classType = 0;
	private Handler handler = new Handler();
	private Instances clusterdata;
	private int dotCount = 0;
	
	static boolean returnModelToHost = false;
	static int modelsReturned = 0;
	
	Instances test;
	
	DataAnalysis3(Context context, String algorithm, int validate, Instances dataset) {
		
		this.context = context;
		this.tv = ((Activity) context).findViewById(R.id.tvStatus);
		this.tvModel = ((Activity) context).findViewById(R.id.tvStatusTest);
		this.tvsl3 = ((Activity) context).findViewById(R.id.tvLabel1);
		this.btnRun = ((Activity) context).findViewById(R.id.btnRun);
		this.btnCM = ((Activity) context).findViewById(R.id.btnSP);
		this.algorithm = algorithm;
		this.validate = validate;
		this.data = dataset;
		this.cci = ((Activity) context).findViewById(R.id.tvCCI);
		this.ici = ((Activity) context).findViewById(R.id.tvICI);
		this.kappa = ((Activity) context).findViewById(R.id.tvKappa);
		this.mae = ((Activity) context).findViewById(R.id.tvMAE);
		this.rmse = ((Activity) context).findViewById(R.id.tvRMSE);
		this.rae = ((Activity) context).findViewById(R.id.tvRAE);
		this.rrse = ((Activity) context).findViewById(R.id.tvRRSE);
		this.tni = ((Activity) context).findViewById(R.id.tvTNI);
		
	}
	
	@Override
	public void run() {
		runAlgorithm();
	}
	
	private Runnable progressRun = new Runnable() {
		@Override
		public void run() {
			statusUpdateStore += ".";
			dotCount++;
			if (dotCount == 1 || dotCount % 35 == 0) statusUpdate("\r\n");
			statusUpdate(".");
			handler.postDelayed(this, 2000);
		}
	};
	
	private void runAlgorithm() {
		
		
		Classifier classifier = new RandomForest();
		
		if (algorithm.equals("Bagging")) classifier = new Bagging();
		else if (algorithm.equals("HoeffdingTree")) classifier = new HoeffdingTree();
		else if (algorithm.equals("NaiveBayes")) classifier = new NaiveBayes();
		else if (algorithm.equals("Decision Table")) classifier = new DecisionTable();
		else if (algorithm.equals("JRip")) classifier = new JRip();
		else if (algorithm.equals("OneR")) classifier = new OneR();
		else if (algorithm.equals("PART")) classifier = new PART();
		else if (algorithm.equals("ZeroR")) classifier = new ZeroR();
		else if (algorithm.equals("DecisionStump")) classifier = new DecisionStump();
		else if (algorithm.equals("J48 (C4.5)")) classifier = new J48();
		else if (algorithm.equals("RandomForest")) classifier = new RandomForest();
		else if (algorithm.equals("FastForest")) classifier = new FastForest();
		else if (algorithm.equals("RandomTree")) classifier = new RandomTree();
		else if (algorithm.equals("*REPTree")) classifier = new REPTree();
		else if (algorithm.equals("SimpleCART")) classifier = new SimpleCart();
		else if (algorithm.equals("AdaBoostM1")) classifier = new AdaBoostM1();
		else if (algorithm.equals("LogitBoost")) classifier = new LogitBoost();
		else if (algorithm.equals("Random Committee")) classifier = new RandomCommittee();
		else if (algorithm.equals("IBk (KNN)")) classifier = new IBk();
		else if (algorithm.equals("Logistic")) classifier = new Logistic();
		else if (algorithm.equals("SimpleLogistic")) classifier = new SimpleLogistic();
		else if (algorithm.equals("KStar")) classifier = new KStar();
		else if (algorithm.equals("SysFor")) classifier = new SysFor();
		else if (algorithm.equals("ForestPA")) classifier = new ForestPA();
		else if (algorithm.equals("SPAARC")) classifier = new SPAARC();
		else if (algorithm.equals("*MultilayerPerceptron")) classifier = new MultilayerPerceptron();
		else if (algorithm.equals("RandomSubSpace")) classifier = new RandomSubSpace();
		
//		System.out.println("CLASSIFIER: "+classifier.toString());
		
		Clusterer clusterer = new SimpleKMeans();
		clusterdata = removeClass(data);
//
		if (algorithm.equals("SimpleKMeans")) {
//			clusterdata = removeClass(data);
			clusterer = new SimpleKMeans();
			//			cl.simpleK.buildClusterer(new Instances(clusterdata));
		}
		else if (algorithm.equals("EM")) {
//			clusterdata = removeClass(data);
			clusterer = new EM();
		}
//		else if (algorithm.equals("DBSCAN")) {
//			clusterdata = removeClass(data);
//			clusterer = new DBSCANTS();
//		}
		else if (algorithm.equals("FarthestFirst")) {
//			clusterdata = removeClass(data);
			clusterer = new FarthestFirst();
		}
		else if (algorithm.equals("FilteredClusterer")) {
			clusterdata = removeClass(data);
			clusterer = new FilteredClusterer();
		}
//
		Associator associator = new Apriori();
//
		if (algorithm.equals("Apriori")) associator = new Apriori();
		else if (algorithm.equals("FilteredAssociator")) associator = new FilteredAssociator();
		else if (algorithm.equals("FPGrowth")) associator = new FPGrowth();
		
		
		if (data.classAttribute().isNominal()) classType = 0;
		else classType = 1;
		try {
			isRunning = true;
//            Instances data = getData();
//			System.out.println(data.toSummaryString());
//			System.out.println("ALGORITHM TO BUILD: " + algorithm);
			statusUpdate("\r\n[" + algorithm + "] model build started.");
			dotCount = 0;
			handler.post(progressRun);
			timeBuildStart = System.nanoTime();
			
			if (alType == 1) {
				
				String[] options = weka.core.Utils.splitOptions(optionsString);
				((AbstractClassifier)classifier).setOptions(options);

//--------------------------------------------------------------------------------------------------------
			
				if (validate == 0 || validate == 1)	{
					classifier.buildClassifier(data);
					buildData = data;
				} else if (validate == 2) {
					classifier.buildClassifier(traindata);
					buildData = traindata;
				}
				
				
//				for (int i = 0; i < 100; i++) {
//					System.out.println(i+" ===============================================================================================");
//					System.out.println(((RandomForestDLP)classifier).getClassifier(i).toString());
//				}

//--------------------------------------------------------------------------------------------------------
				
				buildModel = classifier;

			} else if (alType == 2) {
				clusterer.buildClusterer(new Instances(clusterdata));
			} else if (alType == 3) {
				associator.buildAssociations(data);
				statusUpdate(associator.toString());
			}
			
			timeBuildEnd = System.nanoTime();
			timeBuild = timeBuildEnd - timeBuildStart;
			handler.removeCallbacks(progressRun);
			
			
			if (killThread == false) {
				statusUpdateStore += "\r\n[" + algorithm + "] model build complete.\r\n";
				statusUpdate("\r\n[" + algorithm + "] model build complete.\r\n");
				classifierTree = "";
				classifierTree = classifier.toString();
			} else {
				statusUpdateStore += "\r\n[" + algorithm + "] model build stopped.\r\n";
				statusUpdate("\r\n[" + algorithm + "] model build stopped.\r\n");
				classifierTree = "";
				classifier = null;
				restoreCode();
			}
			
			enableBtnCM();
			updateModelStatus("\nMODEL       : "+algorithm +" from 'Run' screen");
			modelLoaded = true;
			//--------------------------------------------------------------------------------------------
			//--------------------------------------------------------------------------------------------
			if (validate > 0 && killThread == false) {
				if (alType == 1) {
					if (validate == 1)	runEvaluation(algorithm, classifier, null, null, data);
					else runEvaluation(algorithm, classifier, null, null, testdata);
				} else if (alType == 2) {
					runEvaluation(algorithm, null, clusterer, null, data);
				}
			} else {
				classifierTree = "";
				restoreSettings();
			}
//--------------------------------------------------------------------------------------------------------------
//				NetFragment11.analysisThreadListener();
//--------------------------------------------------------------------------------------------------------------
//		   System.out.println(testdata.toSummaryString());
			
		} catch (Exception e) {
			if (killThread == true) {
				statusUpdateStore += "\r\n[" + algorithm + "] model build stopped.\r\nReady.";
				statusUpdate("\r\n[" + algorithm + "] model build stopped.\r\nReady.");
			} else {
				statusUpdate("\n=== ERROR: "+e.getMessage());
				statusUpdate("\n\n... also check:\n");
				statusUpdate(context.getString(R.string.classerror));
			}
			restoreCode();
			System.out.print("ERROR: " + Log.getStackTraceString(e));
//			statusUpdate("=== ERROR: "+ Log.getStackTraceString(e));
		}
	}

	//---------------------------------------------------------------------------------------------------------------
	
	
	//---------------------------------------------------------------------------------------------------------------
	
	private void runEvaluation(String algorithm, Classifier classifier, Clusterer clusterer, Associator associator, Instances data) {
		
		try {
			ClusterEvaluation ceval = new ClusterEvaluation();
			Evaluation eval = new Evaluation(data);
			if (alType == 1) {
				
				if (validate == 1) {
//				ClusterEvaluation ceval = new ClusterEvaluation();
//					System.out.println("ORIG ALGORITHM: " + algorithm + "(XV)");
					statusUpdate("\r\n[" + algorithm + "] model evaluation started (X-V).");
					dotCount = 0;
					handler.post(progressRun);
					timeEvalStart = System.nanoTime();
					eval.crossValidateModel(classifier, data, 10, new Random(1));
				} else if (validate == 2) {
//					System.out.println("ORIG ALGORITHM: " + algorithm);
					statusUpdate("\r\n[" + algorithm + "] model evaluation started (T-T).");
					dotCount = 0;
					handler.post(progressRun);
					timeEvalStart = System.nanoTime();
					eval.evaluateModel(classifier, testdata);
				}
				
			} else if (alType == 2) {
				timeEvalStart = System.nanoTime();
				ceval.setClusterer(clusterer);
//				System.out.println("numInstances: "+clusterdata.numInstances());
				ceval.evaluateClusterer(clusterdata);
//				System.out.println("NUM CLUSTERS: "+ceval.getNumClusters());
				statusUpdate("\r\n" + ceval.clusterResultsToString());
			}
			timeEvalEnd = System.nanoTime();
			timeEval = timeEvalEnd - timeEvalStart;
			handler.removeCallbacks(progressRun);
			isRunning = false;
			if (killThread == false) {
				if (alType == 1) {
					DecimalFormat df = new DecimalFormat("#.####");
					enableBtnCM();
					if (classType == 0) {
						updateResults(cci, (int) eval.correct() + " (" + df.format(eval.pctCorrect()) + "%)");
						updateResults(ici, (int) eval.incorrect() + " (" + df.format(eval.pctIncorrect()) + "%)");
						updateResults(kappa, "" + df.format(eval.kappa()));
					}
					updateResults(mae, df.format(eval.meanAbsoluteError()));
					updateResults(rmse, df.format(eval.rootMeanSquaredError()));
					updateResults(rae, df.format(eval.relativeAbsoluteError()) + "%");
					updateResults(rrse, df.format(eval.rootRelativeSquaredError()) + "%");
					updateResults(tni, "" + (int) eval.numInstances());
					//					if (!algorithm.equals("Apriori") && !algorithm.equals("FPGrowth")) {
					//						restoreSettings();
					//					}
				}
				statusUpdateStore += "\r\n[" + algorithm + "] model evaluation complete.";
				statusUpdate("\r\n[" + algorithm + "] model evaluation complete.");
				restoreSettings();
			} else {
				statusUpdateStore += "\r\n[" + algorithm + "] evaluation stopped.\r\nReady.";
				statusUpdate("\r\n[" + algorithm + "] evaluation stopped.\r\nReady.");
			}
			resetRunBtn();
			isThreadRunning = false;
			returnEval = eval;
		} catch (Exception e) {
			restoreCode();
//			System.out.print("ERROR: " + Log.getStackTraceString(e));
//            statusUpdate("=== ERROR: "+ Log.getStackTraceString(e));
			if (killThread == false) {
				statusUpdate("=== ERROR: this dataset is not supported.");
			} else {
				statusUpdate("\r\n[" + algorithm + "] evaluation stopped.\r\nReady.");
			}
		}
	}
	
	private void restoreCode() {
		resetRunBtn();
		handler.removeCallbacks(progressRun);
		isRunning = false;
		isThreadRunning = false;
	}
	
	private Instances removeClass(Instances inst) {
		Remove af = new Remove();
		Instances retI = null;
//		System.out.println(inst.classIndex());
		
		try {
			if (inst.classIndex() < 0) {
				retI = inst;
			} else {
				af.setAttributeIndices("" + (inst.classIndex() + 1));
				af.setInvertSelection(false);
				af.setInputFormat(inst);
				retI = Filter.useFilter(inst, af);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retI;
	}
	
	private void restoreSettings() {
		if (killThread == false) {
			DecimalFormat df2 = new DecimalFormat("#.#####");
			statusUpdateStore += "\r\n[" + algorithm + "] build: " + (df2.format(timeBuild / 1000000000.0) + "s");
			statusUpdate("\r\n[" + algorithm + "] build: " + (df2.format(timeBuild / 1000000000.0)) + "s");
			if (validate == 1) {
				statusUpdateStore += "\r\n[" + algorithm + "]  eval: " + (df2.format(timeEval / 1000000000.0) + "s");
				statusUpdate("\r\n[" + algorithm + "]  eval: " + (df2.format(timeEval / 1000000000.0)) + "s");
			}
		}
		statusUpdateStore += "\r\nReady.";
		statusUpdate("\r\nReady.");
		resetRunBtn();
		handler.removeCallbacks(progressRun);
		isRunning = false;
		isThreadRunning = false;
//		System.out.println("Ready.");
	}
	
	private void statusUpdate(String status) {
		final String newStatus;
		newStatus = status;
		tv.post(new Runnable() {
			@Override
			public void run() {
				tv.append(newStatus);
				tv.setMovementMethod(new ScrollingMovementMethod());
			}
		});
	}
	
	private void updateModelStatus(String status) {
		final String newStatus;
		newStatus = status;
		tvModel.post(new Runnable() {
			@Override
			public void run() {
				tvModel.append(newStatus);
				tvModel.setMovementMethod(new ScrollingMovementMethod());
			}
		});
		
	}
	
	private void updateResults(TextView tvui, String text) {
		final String displayText = text;
		final TextView tvGUI = tvui;
		tvui.post(new Runnable() {
			@Override
			public void run() {
				tvGUI.setText(displayText);
			}
		});
	}
	
	private void resetRunBtn() {
		btnRun.post(new Runnable() {
			@Override
			public void run() {
				btnRun.setText(context.getString(R.string.str_run));
				tvsl3.setText("Tap 'Run' to model your data:");
			}
		});
	}
	
	private void enableBtnCM() {
		btnCM.post(new Runnable() {
			@Override
			public void run() {
				btnCM.setEnabled(true);
			}
		});
	}

	//---------------------------------------------------------------------------------------------------
	public class ReturnModelToSender extends Thread {
		private Socket socket;
		private ObjectOutputStream objectOutputStream;

		public ReturnModelToSender(Socket skt, ObjectOutputStream oos) throws IOException {
			this.socket = skt;
			objectOutputStream = oos;
		}
		
		@Override
		public void run() {
			try {
//				objectOutputStream.writeObject(data);
//				System.out.println("============================================================================== RETURN MODEL TO SENDER...");
				objectOutputStream.writeObject(buildModel);
				modelsReturned++;
				updateStatus();

			} catch (IOException e) {
			
			}
		}
	}
	//---------------------------------------------------------------------------------------------------
	
	private void updateStatus() {
		tvNetStatus.post(new Runnable() {
			@Override
			public void run() {
				tvNetStatus.append("\n > MODELS SENT: "+modelsReturned);
				scroll.post(new Runnable() {
					@Override
					public void run() {
						scroll.fullScroll(View.FOCUS_DOWN);
					}
				});
				
			}
		});
	}
	//---------------------------------------------------------------------------------------------------
	
	class TabChanger extends Thread {
		private int m_frag;
		public TabChanger(int frag) {
			this.m_frag = frag;
		}
		@Override
		public void run() {
			MainActivity.mViewPager.setCurrentItem(m_frag);
		}
	}
	
}

