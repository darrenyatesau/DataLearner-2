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
	MainActivity.java
    (C) Copyright Darren Yates 2018-2021
	Developed using a combination of Weka 3.8.5 and algorithms developed by Charles Sturt University
	DataLearner is licensed GPLv3.0, source code is available on GitHub
	Weka 3.8.5 is licensed GPLv3.0, source code is available on GitHub
*/

package au.com.darrenyates.datalearner2;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.ArCoreApk;
import com.google.ar.sceneform.rendering.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
//import weka.classifiers.EvaluationTS;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.output.prediction.AbstractOutput;
import weka.classifiers.evaluation.output.prediction.PlainText;
import weka.classifiers.trees.FastForest;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

import weka.core.converters.ConverterUtils;
import weka.core.converters.DLCSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;

//import static au.com.darrenyates.datalearner.DataAnalysis.buildModel;
//import static au.com.darrenyates.datalearner2.DataAnalysis2.buildModel;
import static au.com.darrenyates.datalearner2.DataAnalysis3.buildModel;
import static au.com.darrenyates.datalearner2.SelectBFragment.alType;
import static au.com.darrenyates.datalearner2.SelectBFragment.optionsString;

public class MainActivity extends AppCompatActivity {
	
	/*
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link FragmentPagerAdapter} derivative, which will keep every
	 * loaded fragment in memory. If this becomes too memory intensive, it
	 * may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private static Button btnCM;
	private static TextView tvStatus, cci, ici, kappa, mae, rmse, rae, rrse, tni;
	
	static String nameClassifier;
	private static int validate;
	private static Uri uriDataset;
	public static Instances data;
	
	public static Instances testdata;
	public static Instances traindata;
	public static boolean killThread = false;
	static boolean isThreadRunning = false;
	static String statusUpdateStore = "Ready.";
	
	//DataAnalysis task = null;
	private static Thread thread;
	private static ThreadGroup threadGroup;
	//	static int alType = 1;
	static int viewCount = 0;
	static int classType = 0;
	static boolean modelLoaded;
	
	static ArrayList<Double> att1;
	static ArrayList<Double> att2;
	static ArrayList<Double> att3;
	static ArrayList<Color> cl;
	
	static int runType = 1;
	
	LoadFragment fragL = new LoadFragment();
	RunFragment fragR = new RunFragment();
	TestFragment fragT = new TestFragment();
	SelectBFragment fragB = new SelectBFragment();
	NetFragment13 fragD = new NetFragment13();
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	public static ViewPager mViewPager;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		
		// Set up the ViewPager with the sections adapter.
		mViewPager = findViewById(R.id.container);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(4);
		
		TabLayout tabLayout = findViewById(R.id.tabs);
		
		mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
		tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

		
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		if (id == R.id.about) {
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setTitle("About DataLearner 2");
//			builder1.setMessage("Version 2.0.0\r\n© Copyright Darren Yates, Supervisors: Zahid Islam, Junbin Gao\r\nDeveloped as part of a research PhD at the School of Computing and Mathematics, Charles Sturt University, 2018-2019." +
//					"\r\n\r\nDataLearner is a data-mining app powered by the Weka data-mining core and includes " +
//					"algorithms developed by Charles Sturt University.\r\nWeka was created by the University of Waikato.");
			builder1.setMessage(getText(R.string.str_about));
			AlertDialog alert1 = builder1.create();
			alert1.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.str_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			alert1.show();
			return true;
		}
		if (id == R.id.clear) {
			tvStatus.setText(getResources().getString(R.string.str_ready));
			return true;
		}
		
		if (id == R.id.changelog) {
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setTitle("DataLearner 2 - changes");
			builder1.setMessage(getResources().getString(R.string.str_changes));
			AlertDialog alert1 = builder1.create();
			alert1.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.str_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			alert1.show();
			return true;
		}
		if (id == R.id.help) {
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setTitle("DataLearner - Help");
			builder1.setMessage(getText(R.string.str_help));
			AlertDialog alert1 = builder1.create();
			alert1.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.str_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			alert1.show();
			return true;
		}
		if (id == R.id.yt_help) {
			Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:H-7pETJZf-g"));
			Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/H-7pETJZf-g"));
			try {
				startActivity(appIntent);
			} catch (ActivityNotFoundException ex) {
				startActivity(webIntent);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	// [1] ------------------------------------------------------------------------------------------------
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class LoadFragment extends Fragment {
		
		private static final int READ_REQUEST_CODE = 42;
		TextView tvFile, tvIntro, tvTest, tvStats;
		TextView tv16, tvX5, tvY5, tvZ5;
		Spinner spinX4, spinY4, spinZ4;
		Spinner spinClassAtt;
		String tvFileName;
		Button btnForce, btnVis;
		static int selectAttX, selectAttY, selectAttZ;
		ArrayList<Integer> arrayAttIndexes;
		public static ArrayList<String> values = new ArrayList<>();
		public static ArrayList<Color> distinctClassColors = new ArrayList<>();
		
		int isInitialised = 0;
		
		public LoadFragment() {
		}
		
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main_tab1, container, false);
			tvStats = rootView.findViewById(R.id.tvStats);
			tvIntro = rootView.findViewById(R.id.tvIntro);
			spinClassAtt = rootView.findViewById(R.id.spinClass);
			btnForce = rootView.findViewById(R.id.btnForce);
			btnVis = rootView.findViewById(R.id.btnVis3);
			btnForce.setEnabled(false);
			tv16 = rootView.findViewById(R.id.textView16);
			tvX5 = rootView.findViewById(R.id.tvX5);
			tvY5 = rootView.findViewById(R.id.tvY5);
			tvZ5 = rootView.findViewById(R.id.tvZ5);
			spinX4 = rootView.findViewById(R.id.spinX4);
			spinY4 = rootView.findViewById(R.id.spinY4);
			spinZ4 = rootView.findViewById(R.id.spinZ4);
			
			AdapterView.OnItemSelectedListener spinListener = new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
					int id = adapterView.getId();
					if (isInitialised < 3) {
						if (id == R.id.spinX4) selectAttX = i;
						else if (id == R.id.spinY4) selectAttY = i;
						else if (id == R.id.spinZ4) selectAttZ = i;
						isInitialised++;
					} else {
						if (id == R.id.spinX4) selectAttX = arrayAttIndexes.get(i);
						else if (id == R.id.spinY4) selectAttY = arrayAttIndexes.get(i);
						else if (id == R.id.spinZ4) selectAttZ = arrayAttIndexes.get(i);
					}
				}
				
				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {
				
				}
			};
			
			spinX4.setOnItemSelectedListener(spinListener);
			spinY4.setOnItemSelectedListener(spinListener);
			spinZ4.setOnItemSelectedListener(spinListener);
			
			checkArSupport();
			Button btnLoad = rootView.findViewById(R.id.button1);
			Button btnDemo = rootView.findViewById(R.id.button);
			btnLoad.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					performFileSearch();
					
				}
			});
			btnDemo.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					uriDataset = Uri.parse("android.resource://au.com.darrenyates.datalearner/raw/rain.csv");
					tvStats.setText("");
					
					Instances newdata = null;
					try {
						InputStream inputStream = getContext().getResources().openRawResource(R.raw.rain);
						ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(inputStream);
						newdata = csvReader(inputStream, uriDataset.toString());
						inputStream.close();
						data = new Instances(newdata);
						data.setClassIndex(data.numAttributes() - 1);

//--------------------------------------------------------------------------------------------------------------------
						TrainTestSplit tts = new TrainTestSplit();
						tts.initialise(data);
						traindata = tts.train;
						testdata = tts.test;
//--------------------------------------------------------------------------------------------------------------------
						
						displayData();
						spinClassAtt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
							@Override
							public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
								data.setClassIndex(i);
								displayData();
								RunFragment.cleanDisplay();
							}
							
							@Override
							public void onNothingSelected(AdapterView<?> adapterView) {
							}
						});
						setSpinClass(data.numAttributes());
						spinClassAtt.setSelection(data.numAttributes() - 1);
						showDemoSteps();
						btnForce.setEnabled(true);
						setSpinners();
						btnVis.setEnabled(true);
						
					} catch (Exception e) {
						statusUpdateStore += "\r\nERROR: " + e.getMessage() + "\r\n";
						tvStats.append("\r\nERROR: " + e.getMessage() + "\r\n");
						e.printStackTrace();
					}
				}
				
			});
			btnForce.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					showLimits();
				}
			});
			
			btnVis.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					preproc();
					Intent intent = new Intent(getContext(), ARActivity5.class);
					Bundle bundle = new Bundle();
					bundle.putParcelable("uri", uriDataset);
					bundle.putInt("X", selectAttX);
					bundle.putInt("Y", selectAttY);
					bundle.putInt("Z", selectAttZ);
					String xNameShort = data.attribute(selectAttX).name();
					if (xNameShort.length() > 17) xNameShort = xNameShort.substring(0,17);
					String yNameShort = data.attribute(selectAttY).name();
					if (yNameShort.length() > 17) yNameShort = yNameShort.substring(0,17);
					String zNameShort = data.attribute(selectAttZ).name();
					if (zNameShort.length() > 17) zNameShort = zNameShort.substring(0,17);
					bundle.putString("Xname", xNameShort);
					bundle.putString("Yname", yNameShort);
					bundle.putString("Zname", zNameShort);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			return rootView;
		}
		
		public void checkArSupport() { // check if device supports ARCore
			ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(getContext());
			if (availability.isTransient()) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						checkArSupport();
					}
				},200);
			}
			if (!availability.isSupported()) { // if device does NOT support...
				btnVis.setVisibility(View.GONE);
				tvX5.setVisibility(View.GONE);
				tvY5.setVisibility(View.GONE);
				tvZ5.setVisibility(View.GONE);
				tv16.setVisibility(View.GONE);
				spinX4.setVisibility(View.GONE);
				spinY4.setVisibility(View.GONE);
				spinZ4.setVisibility(View.GONE);
				ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) btnForce.getLayoutParams();
				params.bottomMargin = 16;
				params.bottomToBottom = R.id.constraintLayout;
				btnForce.setLayoutParams(params);
				
			}
		}
		
		
		
		
		void showDemoSteps() {
			AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
			alertDialog.setTitle("You've got this!");
			alertDialog.setIcon(R.mipmap.ic_launcher);
			alertDialog.setMessage(getText(R.string.demo_text));
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.str_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			alertDialog.show();
			
		}
		
		void performFileSearch() {
			if (viewCount < 1) {
				viewCount++;
				AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
				alertDialog.setTitle("First-time users");
				alertDialog.setIcon(R.mipmap.ic_launcher);
				alertDialog.setMessage("If you can't find your folder on the next screen, tap the three-dot menu button top-right and select 'Show internal storage'," +
						" then navigate to your dataset file (you only need set this once).");
				alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.str_ok), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						loadOpenFile();
					}
				});
				alertDialog.show();
			} else {
				try {
					loadOpenFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		void loadOpenFile() {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
			startActivityForResult(intent, READ_REQUEST_CODE);
		}
		
		
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
			if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
				uriDataset = null;
				if (returnIntent != null) {
					tvStats.setText("Retrieving data - please wait...");
					uriDataset = returnIntent.getData();
//					System.out.println("FILE: " + uriDataset.toString());
					Cursor returnCursor = getActivity().getContentResolver().query(uriDataset, null, null, null, null);
					returnCursor.moveToFirst();
					int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//					System.out.println(returnCursor.getString(nameIndex));
					String fileCut = returnCursor.getString(nameIndex);
					int split = fileCut.lastIndexOf('/');
					fileCut = fileCut.substring(split + 1);
					split = fileCut.lastIndexOf(':');
//					System.out.println(fileCut);
					if (fileCut.endsWith("arff") || fileCut.endsWith("csv")) {
						tvStats.setText("");
						data = getData();
//						data.setClassIndex(data.numAttributes()-1);
//						displayData();
					} else {
						AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
						alertDialog.setTitle("Warning");
						alertDialog.setMessage("DataLearner only accepts Weka-style ARFF files or CSV files.");
						alertDialog.setIcon(R.mipmap.ic_launcher);
						alertDialog.show();
					}
					//					tvIntro.getLayoutParams().height = 0;
					
				}
			} else {
//				System.out.println("ERROR: Problem with file read.");
			}
		}
		
		
		void displayData() {
			tvStats.setText("");
			tvStats.append(data.toSummaryString());
			tvStats.append("\nClass attribute: (" + ((data.classIndex() + 1) + ") " + data.classAttribute().name()));
			if (data.classAttribute().isNominal())
				tvStats.append("\nAttribute type : Nominal/Categorical");
			else tvStats.append("\nAttribute type : Numeric");
			tvStats.append("\nDistinct values: " + data.numDistinctValues(data.classAttribute()) + "\n");
			tvIntro.setText("");
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvIntro.getLayoutParams();
			params.topMargin = 0;
			params.height = 0;
			tvIntro.setLayoutParams(params);
		}
		
		
		//		public Instances getData(String filePath) {
		public Instances getData() {
			Cursor returnCursor = getActivity().getContentResolver().query(uriDataset, null, null, null, null);
			returnCursor.moveToFirst();
			int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//			System.out.println(returnCursor.getString(nameIndex));
			String fileCut = returnCursor.getString(nameIndex);
			int split = fileCut.lastIndexOf('/');
			fileCut = fileCut.substring(split + 1);
			split = fileCut.lastIndexOf(':');
//			System.out.println(fileCut);
			String filePath = fileCut;
			
			Instances newdata = null;
			try {
				InputStream inputStream = getContext().getContentResolver().openInputStream(uriDataset);
				ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(inputStream);
				if (filePath.endsWith("arff")) {
					newdata = dataSource.getDataSet();
				} else {
					newdata = csvReader(inputStream, filePath);
				}
				inputStream.close();
				newdata.setClassIndex(newdata.numAttributes() - 1);
				data = new Instances(newdata);

//--------------------------------------------------------------------------------------------------------------------
				TrainTestSplit tts = new TrainTestSplit();
				tts.initialise(data);
				traindata = tts.train;
				testdata = tts.test;
//--------------------------------------------------------------------------------------------------------------------
//				System.out.println(traindata.toSummaryString());
				spinClassAtt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
						data.setClassIndex(i);
						displayData();
						RunFragment.cleanDisplay();
					}
					
					@Override
					public void onNothingSelected(AdapterView<?> adapterView) {
					
					}
				});
				setSpinClass(data.numAttributes());
				spinClassAtt.setSelection(data.numAttributes() - 1);
				btnForce.setEnabled(true);
				setSpinners();
				btnVis.setEnabled(true);
				
			} catch (Exception e) {
				statusUpdateStore += "\r\nERROR: " + e.getMessage() + "\r\n";
				tvStats.append("\r\nERROR: " + e.getMessage() + "\r\n");
				e.printStackTrace();
			}
			return data;
		}
		
		void showLimits() {
			double numInstances = data.numInstances();
			double numDistinct = data.numDistinctValues(data.classAttribute());
			double ratio = numInstances / numDistinct;
			if (data.classAttribute().isNominal()) showAlreadyNominal();
			else if (ratio < 3 || numDistinct > 255) { // too many class values for number of instances = better as numeric
				AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
				alertDialog.setTitle("Sure about this?");
				alertDialog.setIcon(R.mipmap.ic_launcher);
				alertDialog.setMessage(getText(R.string.noconvert));
				if (numDistinct <= 255) {
					alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes, do it.", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							try {
								data = convertClass(data);
								displayData();
								showDone();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				} else {
					alertDialog.setMessage("Sorry, your class attribute has more than 255 distinct values.\r\nForcing this attribute to nominal will likely result in slow performance.");
					alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
						}
					});
					alertDialog.show();
				}
			} else {
				try {
					data = convertClass(data);
					displayData();
					showDone();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
		
		void showDone() {
			AlertDialog aD2 = new AlertDialog.Builder(getContext()).create();
			aD2.setTitle("Attribute converted to nominal.");
			aD2.setIcon(R.mipmap.ic_launcher);
			aD2.setMessage("Attribute "+(data.classIndex()+1)+" has been converted.\r\nReload the dataset to revert the attribute back to numeric.");
			aD2.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.str_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			aD2.show();
		}
		
		void showAlreadyNominal() {
			AlertDialog aD2 = new AlertDialog.Builder(getContext()).create();
			aD2.setTitle("Attribute already nominal");
			aD2.setIcon(R.mipmap.ic_launcher);
			aD2.setMessage("Attribute "+(data.classIndex()+1)+" is already nominal.\r\nMove along... Move along...");
			aD2.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.str_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			aD2.show();
			
		}
		
		Instances csvReader(InputStream inputStream, String filePath) throws Exception {
			String fileCut = filePath;
			int split = fileCut.lastIndexOf('/');
			fileCut = fileCut.substring(split + 1);
			DLCSVLoader cl = new DLCSVLoader();
			cl.setSource(inputStream);
			Instances dataSet = cl.getDataSet();
			//----------------------------------------------------------------------------------------
//			NumericToNominal ntn = new NumericToNominal();
//			String[] options = new String[2];
//			options[0] = "-R";
//			options[1] = Integer.toString(dataSet.numAttributes());
//			ntn.setOptions(options);
//			ntn.setInputFormat(dataSet);
//			Instances newData = Filter.useFilter(dataSet, ntn);
//			newData.setRelationName(fileCut);
//			return newData;
			//----------------------------------------------------------------------------------------
			dataSet.setRelationName(fileCut);
			dataSet.setClassIndex(dataSet.numAttributes() - 1);
//-------------------------------------------------------------------------------------------------------------
//			if (dataSet.numDistinctValues(dataSet.classAttribute()) < 256 ) dataSet = convertClass(dataSet);
//			dataSet = convertClass(dataSet);
//-------------------------------------------------------------------------------------------------------------
			return dataSet;
		}
		
		Instances convertClass(Instances input) throws Exception {
			Instances output = new Instances(input);
			NumericToNominal ntn = new NumericToNominal();
			String[] options = new String[2];
			options[0] = "-R";
			options[1] = Integer.toString(output.classIndex() + 1);
			ntn.setOptions(options);
			ntn.setInputFormat(output);
			Instances newData = Filter.useFilter(output, ntn);
			return newData;
		}
		
		void setSpinClass(int number) {
			ArrayList<String> arraySpin = new ArrayList<>();
			for (int i = 0; i < number; i++) {
				arraySpin.add(Integer.toString(i + 1));
			}
			String[] arrayString = arraySpin.toArray(new String[0]);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, arrayString);
			spinClassAtt.setAdapter(adapter);
			spinClassAtt.setSelection(spinClassAtt.getCount());
		}
		
		private void setSpinners() {
			ArrayList<String> arrayAttNames = new ArrayList<>();
			arrayAttIndexes = new ArrayList<>();
			for (int i = 0; i < data.numAttributes()-1; i++) {
				if (data.attribute(i).isNumeric())	{
					arrayAttNames.add(data.attribute(i).name()+" ("+(i+1)+")");
					arrayAttIndexes.add(i);
				}
			}
			String[] arrayAttString = arrayAttNames.toArray(new String[0]);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.tab1_listrow_layoutspinner, arrayAttString);
			spinX4.setAdapter(adapter); spinY4.setAdapter(adapter); spinZ4.setAdapter(adapter);
			if (arrayAttString.length >= 2) {
				spinX4.setSelection(0);
				spinY4.setSelection(1);
				spinZ4.setSelection(2);
				selectAttX = arrayAttIndexes.get(0);
				selectAttY = arrayAttIndexes.get(1);
				selectAttZ = arrayAttIndexes.get(2);
			} else {
				btnVis.setEnabled(false);
			}
		}
		
		
		protected ArrayList<Color> getClassColor(Instances inputdata) {
			ArrayList<Color> listColor = new ArrayList<>();
			int colorCount = 0;
			for (int i = 0; i < inputdata.numInstances(); i++) {
				float r = 0f, g = 0f, b = 0f;
				byte classVal = (byte)(inputdata.instance(i).classValue());
				classVal++;
				if ((classVal & 0x01) == 0x01) r = 1.0f;
				if ((classVal & 0x02) == 0x02) g = 1.0f;
				if ((classVal & 0x04) == 0x04) b = 1.0f;
				if ((classVal & 0x08) == 0x08) r /= 2f;
				if ((classVal & 0x10) == 0x10) g /= 2f;
				if ((classVal & 0x20) == 0x20) b /= 2f;
				Color classColor = new Color(r, g, b);
//				System.out.println(classVal+" > "+classColor.r + ", "+classColor.g+", "+classColor.b);
				listColor.add(classColor);
			}
			return(listColor);
		}
		
		protected ArrayList<Color> getDistinctClassColors(Instances inputdata) {
			ArrayList<Color> listColor = new ArrayList<>();
			int colorCount = 0;
			for (int i = 0; i < inputdata.classAttribute().numValues(); i++) {
				float r = 0f, g = 0f, b = 0f;
				byte classVal = (byte)(inputdata.classAttribute().indexOfValue(inputdata.classAttribute().value(i)));
				classVal++;
				if ((classVal & 0x01) == 0x01) r = 1.0f;
				if ((classVal & 0x02) == 0x02) g = 1.0f;
				if ((classVal & 0x04) == 0x04) b = 1.0f;
				if ((classVal & 0x08) == 0x08) r /= 2f;
				if ((classVal & 0x10) == 0x10) g /= 2f;
				if ((classVal & 0x20) == 0x20) b /= 2f;
				Color classColor = new Color(r, g, b);
//				System.out.println(classVal+" > "+classColor.r + ", "+classColor.g+", "+classColor.b);
				listColor.add(classColor);
			}
			return(listColor);
		}
		
		protected ArrayList<Double> normaliseAttr(Instances inputdata, int attrIndex) {
			ArrayList<Double> modAttr = new ArrayList<>();
			double maxValue = inputdata.attributeStats(attrIndex).numericStats.max;
			double minValue = inputdata.attributeStats(attrIndex).numericStats.min;
			double pointValue = 0;
			double dataValue = 0;
			for (int i = 0; i < inputdata.numInstances(); i++) {
				dataValue = inputdata.instance(i).value(attrIndex);
				pointValue = ((dataValue-minValue)/(maxValue-minValue))*0.5;
//			modAttr.add((data.instance(i).value(attrIndex)/maxValue)*0.5);
				modAttr.add(pointValue);
			}
			return(modAttr);
		}
		
		protected void preproc() {
			att1 = normaliseAttr(data, selectAttX);
			att2 = normaliseAttr(data, selectAttY);
			att3 = normaliseAttr(data, selectAttZ);
			cl = getClassColor(data);
			distinctClassColors = getDistinctClassColors(data);
			values.clear();
			for (int i = 0; i < data.classAttribute().numValues(); i++) {
				//values.add(i);
				values.add(data.classAttribute().value(i));
			}
		}
	}
	
	//-----------------------------------------------------------------------------------------------------
	// [2] ------------------------------------------------------------------------------------------------
//	public static class SelectFragment extends Fragment {
//		public SelectFragment() {
//		}
//

	// [3] --------------------------------------------------------------------------------------------------------------------------------------------------------------------


//																	RUN FRAGMENT

	// [3] --------------------------------------------------------------------------------------------------------------------------------------------------------------------
	public static class RunFragment extends Fragment {
		
		public RunFragment() {
		}
		
		//		TextView cci, ici, kappa, mae, rmse, rae, rrse, tni, tvStatus, tvsl3;
		static TextView tvsl3;
		static Button btnRun;
//		static CheckBox checkBox;

//		public void setInterface(int uitype) {
//			changeFragment();
//		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			
			View rootView = inflater.inflate(R.layout.fragment_main_tab3b, container, false);
			tvStatus = rootView.findViewById(R.id.tvStatus);
//			checkBox = rootView.findViewById(R.id.checkBox);
			RadioButton rbCrossValid = rootView.findViewById(R.id.rbCrossValid);
			RadioButton rbTrainTest = rootView.findViewById(R.id.rbTrainTest);
			RadioButton rbModelOnly = rootView.findViewById(R.id.rbModelOnly);
			RadioGroup rg = rootView.findViewById(R.id.radioGroup);
			rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(RadioGroup radioGroup, int i) {
					if (rbCrossValid.isChecked()) runType = 1;
					else if (rbTrainTest.isChecked()) runType = 2;
					else if (rbModelOnly.isChecked()) runType = 0;
//					setTrainTestData();
				}
				
			});
			
			btnRun = rootView.findViewById(R.id.btnRun);
			btnCM = rootView.findViewById(R.id.btnSP);
			cci = rootView.findViewById(R.id.tvCCI);
			ici = rootView.findViewById(R.id.tvICI);
			kappa = rootView.findViewById(R.id.tvKappa);
			mae = rootView.findViewById(R.id.tvMAE);
			rmse = rootView.findViewById(R.id.tvRMSE);
			rae = rootView.findViewById(R.id.tvRAE);
			rrse = rootView.findViewById(R.id.tvRRSE);
			tni = rootView.findViewById(R.id.tvTNI);
			tvsl3 = rootView.findViewById(R.id.tvLabel1);
			tvStatus.setMovementMethod(new ScrollingMovementMethod());
			tvStatus.setHorizontallyScrolling(true);
			
			changeFragment();
			
			btnRun.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
//					if (uriDataset != null && !nameClassifier.equals("-- none selected --")) {


//					System.out.println("\n\n\nRUN TEST: "+nameClassifier+"\n"+data.toSummaryString());
					
					
					if (data != null && !nameClassifier.equals("-- none selected --")) {
						if (thread == null || !isThreadRunning) {
//							if (rbCrossValid.isChecked()) runType = 1; //launchTask(1);
//							else if (rbModelOnly.isChecked()) runType = 0; //launchTask(0);
//							else if (rbTrainTest.isChecked()) runType = 2; //launchTask (2);
							launchTask(runType);
							
							//							launchTask(checkBox);
//							System.out.println("Pressed for START");
							btnRun.setText("Stop");
							tvsl3.setText("Tap 'Stop' to stop process:");
							killThread = false;
						} else {
//							System.out.println("Pressed for STOP");
							statusUpdateStore += "\r\n[" + nameClassifier + "] Stopping - please wait.";
							tvStatus.append("\r\n[" + nameClassifier + "] Stopping - please wait.");
							thread.interrupt();
							killThread = true;
						}
						
					} else {
						AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
						alertDialog.setTitle("You're missing something...");
						alertDialog.setIcon(R.mipmap.ic_launcher);
						alertDialog.setMessage("Please select a training set file and an algorithm before you try to model something.");
						alertDialog.show();
					}
				}
			});
			
			btnCM.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(new Intent(getContext(), ConfusionMatrixActivity.class));
				}
				
			});
			
			return rootView;
		}
		
//----------------------------------------------------------------------------------------------------
		
		
		static void changeFragment() {
			
			ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) tvStatus.getLayoutParams();
//			if (alType == 2) {
			if (alType > 1) {
				params.topMargin = 3;
				params.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
				params.topToBottom = R.id.btnRun;
				params.bottomToBottom = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
				params.bottomMargin = 16;
			} else if (alType == 1) {
				params.topMargin = 16;
				params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
				params.bottomToTop = R.id.btnSP;
				params.topToBottom = R.id.textView14;
				params.bottomMargin = 8;
			}
			tvStatus.setLayoutParams(params);
			cleanDisplay();
			
		}
		
		void launchTask(int method) {
			btnCM.setEnabled(false);
			cleanDisplay();
			validate = method;
//			DataAnalysis task = new DataAnalysis(getContext());
//			DataAnalysis task = new DataAnalysis(getContext(), tvStatus, tvsl3, btnRun, btnCM, nameClassifier, validate, data,
//					cci, ici, kappa, mae, rmse, rae, rrse, tni);

//			DataAnalysis task = new DataAnalysis(getContext(), nameClassifier, validate, data);
			DataAnalysis3 task = new DataAnalysis3(getContext(), nameClassifier, validate, data);
			threadGroup = new ThreadGroup("null");
			thread = new Thread(threadGroup, task, "dataRunnable", 64000);
			thread.start();
			isThreadRunning = true;
		}
		
		
		static void cleanDisplay() {
			tvStatus.setText(R.string.str_ready);
			cci.setText("---");
			ici.setText("---");
			kappa.setText("---");
			mae.setText("---");
			rmse.setText("---");
			rae.setText("---");
			rrse.setText("---");
			tni.setText("---");
		}
		
	}
	//------------------------------------------------------------------------------------------------------------------------------------------------------
	
	// [4] -------------------------------------------------------------------------------------------------------------------------------------------------
	public static class TestFragment extends Fragment {
		
		private static final int READ_MODEL_REQUEST = 42;
		private static final int READ_TESTSET_REQUEST = 43;
		private static final int WRITE_OUTPUT_REQUEST_CODE = 44;
		
		TextView tvStatusTest;
		Button btnSP;
		Button btnPTS;
		RadioButton rbTest;
		RadioButton rbCross;
		ArrayList<String> outputToSave;
		String modelFile;
		String testFile;

		StringBuffer predSB;
		private Handler handler = new Handler();
		Runnable progressRun;
		
		public TestFragment() {}
		
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
								 Bundle savedInstanceState) {
			
			View rootView = inflater.inflate(R.layout.fragment_main_tab4, container, false);
			TextView tvLabel1 = rootView.findViewById(R.id.tvLabel1);
			TextView tvLabel2 = rootView.findViewById(R.id.tvLabel2);
			TextView tvLabel3 = rootView.findViewById(R.id.tvLabel3);
			tvStatusTest = rootView.findViewById(R.id.tvStatusTest);
			rbTest = rootView.findViewById(R.id.rbTest);
			rbCross = rootView.findViewById(R.id.rbCross);
			
			Button btnLM = rootView.findViewById(R.id.btnLM);
			btnLM.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					statusUpdate("\n\n\n");
					loadModel();
				}
			});
			
			Button btnLTS = rootView.findViewById(R.id.btnLTS);
			btnLTS.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					loadTestSet();
				}
				
			});
			
			
			btnPTS = rootView.findViewById(R.id.btnPTS);
			btnPTS.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick (View v) {
//						runClassifierTest();
					if (testdata != null && modelLoaded == true) {
						if (thread == null || !isThreadRunning) {
							tvStatusTest.setText("");
							if (modelFile != null) {
								tvStatusTest.append("\nMODEL       : " + modelFile + "\n");
							} else {
								tvStatusTest.append("\nMODEL       : " + nameClassifier + " from 'Run' screen");
							}
							if (testFile != null) {
								tvStatusTest.append("\nTEST DATASET: " + testFile.substring(testFile.lastIndexOf("%3A") + 3));
							} else {
								tvStatusTest.append("\nDATASET     : " +data.relationName());
							}
							RunPrediction rp = new RunPrediction();
							threadGroup = new ThreadGroup("null");
							thread = new Thread(threadGroup, rp, "dataRunnable", 64000);
							thread.start();
							btnPTS.setText("STOP TEST");
							isThreadRunning = true;
							killThread = false;
						} else if (isThreadRunning) {
							statusUpdate("\nStopping prediction - please wait...\nReady.");
							isThreadRunning = false;
							thread.interrupt();
							killThread = true;
							resetBtnPTS();
						}
					} else {
						showAlert("Missing something?", "Either a model file or test dataset are missing.");
					}
				}
				
			});
			
			btnSP = rootView.findViewById(R.id.btnSP);
			btnSP.setOnClickListener(new Button.OnClickListener() {
				@Override
				public void onClick (View v) {
					saveOutputFile();
				}
				
			});
			
			return(rootView);
		}
		
		void loadModel() {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
			startActivityForResult(intent, READ_MODEL_REQUEST);
		}
		
		void loadTestSet() {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
			startActivityForResult(intent, READ_TESTSET_REQUEST);
		}
		
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
			if (requestCode == READ_MODEL_REQUEST && resultCode == Activity.RESULT_OK) {
				try {
					Uri filename = returnIntent.getData();
					modelFile = filename.toString();
					ParcelFileDescriptor pfd = getContext().getContentResolver().openFileDescriptor(filename, "r");
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pfd.getFileDescriptor()));
					buildModel = (Classifier) ois.readObject();
					System.out.println(buildModel.toString());
					ois.close();
					pfd.close();
					
					Toast toast = Toast.makeText(getContext().getApplicationContext(), "Model \n\n"+filename.toString()
							+"\n\nloaded successfully.", Toast.LENGTH_LONG);
					toast.show();
					tvStatusTest.setText("");
					tvStatusTest.append("MODEL       : "+filename.toString());
					modelLoaded = true;
				} catch (Exception e) {
					showAlert("Model file invalid", "This file is not a valid model. Try another file.");
					e.printStackTrace();
//					System.out.println(e.toString());
				}
			} else if (requestCode == READ_TESTSET_REQUEST && resultCode == Activity.RESULT_OK) {
				try {
					uriDataset = null;
					if (returnIntent != null) {
						tvStatusTest.append("\nRetrieving data - please wait...");
						uriDataset = returnIntent.getData();
						testFile = uriDataset.toString();
//						System.out.println("FILE: " + uriDataset.toString());
						Cursor returnCursor = getActivity().getContentResolver().query(uriDataset, null, null, null, null);
						returnCursor.moveToFirst();
						int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//						System.out.println(returnCursor.getString(nameIndex));
						String fileCut = returnCursor.getString(nameIndex);
						returnCursor.close();
						int split = fileCut.lastIndexOf('/');
						fileCut = fileCut.substring(split + 1);
						split = fileCut.lastIndexOf(':');
//						System.out.println(fileCut);
						if (fileCut.endsWith("arff") || fileCut.endsWith("csv")) {
							try {
								InputStream inputStream = getContext().getContentResolver().openInputStream(uriDataset);
								ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(inputStream);
								if (fileCut.endsWith("arff")) {
									testdata = dataSource.getDataSet();
								} else {
									testdata = csvReader(inputStream, fileCut);
								}
								inputStream.close();
								tvStatusTest.append("\nTEST DATASET: "+fileCut);
								testdata.setClassIndex(testdata.numAttributes() - 1);
								Toast toast = Toast.makeText(getContext().getApplicationContext(), "Test dataset \n\n"+fileCut
										+"\n\nloaded successfully.", Toast.LENGTH_LONG);
								toast.show();
//								data = new Instances(testdata);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							tvStatusTest.append("\nERROR: Test set file is invalid.");
							showAlert("Test dataset invalid", "This file is not a valid test dataset. Try another file.");
						}
					}
				} catch (Exception e) {
					showAlert("Test dataset invalid", "This file is not a valid test dataset. Try another file.");
					e.printStackTrace();
//					System.out.println(e.toString());
				}
				
			} else if (requestCode == WRITE_OUTPUT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
				try {
//				String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
//				ActivityCompat.requestPermissions(this, permissions, WRITE_REQUEST_CODE);
					Uri filename = returnIntent.getData();
					ParcelFileDescriptor pfd = getContext().getContentResolver().openFileDescriptor(filename, "w");
//					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(pfd.getFileDescriptor()));
//					oos.writeObject(outputToSave);
//					oos.close();
					FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());
					for (int i = 0; i < outputToSave.size(); i++) {
						fos.write(outputToSave.get(i).getBytes());
					}
					fos.close();
					pfd.close();
					
					Toast toast = Toast.makeText(getContext().getApplicationContext(), "Output has been saved successfully.", Toast.LENGTH_LONG);
					toast.show();
				} catch (Exception e) {
					e.printStackTrace();
					tvStatusTest.append(e.toString());
//					System.out.println(e.toString());
				}
			}
			
		}
		
		Instances csvReader(InputStream inputStream, String filePath) throws Exception {
			String fileCut = filePath;
			int split = fileCut.lastIndexOf('/');
			fileCut = fileCut.substring(split + 1);
			DLCSVLoader cl = new DLCSVLoader();
			cl.setSource(inputStream);
			Instances dataSet = cl.getDataSet();
			dataSet.setRelationName(fileCut);
			dataSet.setClassIndex(dataSet.numAttributes() - 1);
			return dataSet;
		}
		
//--------------------------------------------------------------------------------------------------------
		public void runClassifierTest() throws Exception {
			
			statusUpdate("\n");
			progressRun = new Runnable() {
				int dotCount = 0;
				@Override
				public void run() {
					dotCount++;
					if (dotCount == 1 || dotCount % 35 == 0) statusUpdate("\r\n");
					statusUpdate(".");
					handler.postDelayed(this, 2000);
				}
			};

//--------------------------------------------------------------------------------------------------------
			
			Evaluation eval = new Evaluation(testdata);

			AbstractOutput ao = new PlainText();
			StringBuffer sb = new StringBuffer();
			ao.setBuffer(sb);
			ao.setHeader(testdata);

//--------------------------------------------------------------------------------------------------------
//			ClassifierDLP dlanModel = new RandomForestDLP();
//			((RandomForestDLP)dlanModel).setDoNotCheckCapabilities(true);
//			((RandomForestDLP)dlanModel).setRepresentCopiesUsingWeights(true);
//			String[] options = weka.core.Utils.splitOptions(optionsString);
//			((AbstractClassifierDLP) dlanModel).setOptions(options);
//			((RandomForestDLP) dlanModel).setClassifiers(NetFragment12.m_Classifiers);
//--------------------------------------------------------------------------------------------------------
			Classifier dlanModel = new RandomForest();
			String[] options;
			if (nameClassifier != null && nameClassifier.equals("RandomForest")) {
				((RandomForest) dlanModel).setDoNotCheckCapabilities(true);
				((RandomForest) dlanModel).setRepresentCopiesUsingWeights(true);
				options = weka.core.Utils.splitOptions(optionsString);
				((AbstractClassifier) dlanModel).setOptions(options);
				((RandomForest) dlanModel).setClassifiers(NetFragment13.m_Classifiers);
			} else if (nameClassifier != null && nameClassifier.equals("FastForest")) {
				dlanModel = new FastForest();
				((FastForest) dlanModel).setDoNotCheckCapabilities(true);
				((FastForest) dlanModel).setRepresentCopiesUsingWeights(true);
				options = weka.core.Utils.splitOptions(optionsString);
				((AbstractClassifier) dlanModel).setOptions(options);
				((FastForest)dlanModel).setClassifiers(NetFragment13.m_Classifiers);
			} else if (buildModel != null) {
				dlanModel = buildModel;
			}


//--------------------------------------------------------------------------------------------------------
//			if (runType == 1) eval.crossValidateModel(dlanModel, data, 10, new Random(1), ao);
//			else if (runType == 0 || runType == 2) eval.evaluateModel(dlanModel, testdata, ao);

			if (rbCross.isChecked()) eval.crossValidateModel(dlanModel, testdata, 10, new Random(1), ao);
			else if (rbTest.isChecked()) eval.evaluateModel(dlanModel, testdata, ao);

			handler.removeCallbacks(progressRun);
			
			ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
			tg.startTone(ToneGenerator.TONE_CDMA_PIP, 1000);
			
			outputToSave = setCSVTrimOutput(ao.getBuffer());
			statusUpdate("\n\n=== Performance statistics ===");
			statusUpdate("\n" + eval.toSummaryString());
			statusUpdate(eval.toMatrixString("\n=== Confusion Matrix ===\n"));
			statusUpdate("\n\n=== Predictions on test set ===\n\n");
			statusUpdate(" Inst#, Actual, Predicted, Error, Prob. Dist'n\n\n");
			statusUpdate(ao.getBuffer().toString());
			enableBtnSP();
			resetBtnPTS();
			isThreadRunning = false;
		}
		
		void saveOutputFile() {
			Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
			intent.putExtra(Intent.EXTRA_TITLE, "<Set output name>");
			startActivityForResult(intent, WRITE_OUTPUT_REQUEST_CODE);
		}
		
		
		ArrayList<String> setCSVTrimOutput(StringBuffer sb) {
			
			ArrayList<String> outlist = new ArrayList<>();
			String newRec = "";
			String[] newsplits = sb.toString().split("\n");
			for (int i = 0; i < newsplits.length; i++) {
				String[] splitrec = newsplits[i].split(",");
				newRec = "";
				for (int j = 0; j < splitrec.length; j++) {
					String split = splitrec[j].trim();
					if (j < splitrec.length-1) newRec +=split+",";
					else newRec+=split+"\n";
				}
				outlist.add(newRec);
			}
			return(outlist);
		}
		
		class RunPrediction implements Runnable {
			@Override
			public void run() {
				try {
					runClassifierTest();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
		private void statusUpdate(String status) {
			final String newStatus;
			newStatus = status;
			if (newStatus.equals("\n\n\n")) {
				tvStatusTest.post(new Runnable() {
					@Override
					public void run() {
						tvStatusTest.setText("");
					}
				});
			} else {
				tvStatusTest.post(new Runnable() {
					@Override
					public void run() {
						tvStatusTest.append(newStatus);
						tvStatusTest.setMovementMethod(new ScrollingMovementMethod());
					}
				});
				
			}
		}
		
		private void enableBtnSP() {
			btnSP.post(new Runnable() {
				@Override
				public void run() {
					btnSP.setEnabled(true);
				}
			});
		}
		
		private void resetBtnPTS() {
			btnPTS.post(new Runnable() {
				@Override
				public void run() {
					btnPTS.setText("PREDICT TEST SET");}
			});
		}
		
		void showAlert(String title, String message) {
			AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
			alertDialog.setTitle(title);
			alertDialog.setIcon(R.mipmap.ic_launcher);
			alertDialog.setMessage(message);
			alertDialog.show();
		}
	}
	//------------------------------------------------------------------------------------------------
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	class SectionsPagerAdapter extends FragmentStatePagerAdapter {
		
		
		SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return fragL;
				case 1:
					return fragB;
				case 2:
					return fragR;
				case 3:
					return fragT;
				case 4:
					return fragD;
			}
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class below).
			//return PlaceholderFragment.newInstance(position + 1);
			return null;
		}
		
		@Override
		public int getCount() {
			// Show 5 total pages.
			return 5;
		}

	}
}
