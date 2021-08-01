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
	SelectBFragment.java
    (C) Copyright Darren Yates 2018-2021
	Developed using a combination of Weka 3.8.5 and algorithms developed by Charles Sturt University
	DataLearner is licensed GPLv3.0, source code is available on GitHub
	Weka 3.8.5 is licensed GPLv3.0, source code is available on GitHub
*/

package au.com.darrenyates.datalearner2;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import static au.com.darrenyates.datalearner2.MainActivity.nameClassifier;

public class SelectBFragment extends Fragment {
	
//	ArrayList<Algorithm> algorithms;
	View rootView;
	ListView lvOpt;
	EditText editText;
	static int alType = 1;
	static String optionsString = "";
	
	int listPosition;
	String[] algList = new String[]{"Bayes","Functions","Lazy","Meta","Rules","Trees","Clusterers","Associators"};
	Integer[] rowPosition = new Integer[algList.length];
	ArrayList<String> arrayValues = new ArrayList<>();
	ArrayList<String> arrayOptType = new ArrayList<>();
	static ArrayList<String> arrayOptValues = new ArrayList<>();
	static ArrayList<String> arraySwitches = new ArrayList<>();
	static ArrayList<String> arrayStdRev = new ArrayList<>();
	
	OptionsArrayAdapter oaa;
//	int[] arrayResources = new int[]{R.array.J48, R.array.RandomForest, R.array.BayesNet, R.array.NaiveBayes, R.array.Logistic,
//			R.array.SimpleLogistic, R.array.MultilayerPerceptron, R.array.IBk, R.array.KStar,
//			R.array.AdaBoostM1, R.array.Bagging, R.array.LogitBoost, R.array.MultiBoostAB, R.array.RandomCommittee,
//			R.array.RandomSubSpace, R.array.RotationForest, R.array.ConjunctiveRule, R.array.DecisionTable,
//			R.array.DTNB, R.array.JRip, R.array.OneR, R.array.PART, R.array.Ridor, R.array.ZeroR,
//			R.array.ADTree, R.array.BFTree};
	
	public SelectBFragment() {
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
								Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.fragment_main_tab2c, container, false);
		ListView lvAlg = rootView.findViewById(R.id.lvAlgor);
		AlgorithmArrayAdapter aaa = new AlgorithmArrayAdapter(getContext(), algList);
		lvAlg.setAdapter(aaa);
		lvAlg.requestLayout();
		
		lvOpt = rootView.findViewById(R.id.lvOptions);
		oaa = new OptionsArrayAdapter(getContext(), arrayValues);
		lvOpt.setAdapter(oaa);
		
		ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) lvAlg.getLayoutParams();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int height = displayMetrics.heightPixels;
		params.height = (height/3)-30;
		lvAlg.setLayoutParams(params);
		
		for (int i = 0; i < algList.length; i++) {
			rowPosition[i] = 0;
		}

//		int resource = R.array.J45;
//		String[] array = getResources().getStringArray(resource);
//		List<String> list = Arrays.asList(array);
//		ArrayList<String> arrayList = new ArrayList<>(list);
		
		return rootView;
	}
	
	public void collectOptions(int resource) {
//		String[] array = getResources().getStringArray(arrayResources[r]);
		String[] array = getResources().getStringArray(resource);
		arrayValues = new ArrayList<>(Arrays.asList(array));
		ArrayList<String> alAlgs = new ArrayList<>();
		ArrayList<String> alOpts = new ArrayList<>();
		ArrayList<String> alOptVals = new ArrayList<>();
		ArrayList<String> alSw = new ArrayList<>();
		ArrayList<String> alStdRev = new ArrayList<>();
		
		for (int i = 0; i < arrayValues.size(); i++) {
//			alAlgs.add(arrayValues.get(i).substring(0,arrayValues.get(i).indexOf(',')));
			String[] optSplits = arrayValues.get(i).split(",");
			alAlgs.add(optSplits[0]);
			alOpts.add(optSplits[1]);
			alOptVals.add(optSplits[2]);
			alSw.add(optSplits[3]);
			alStdRev.add(optSplits[4]);
		}
		arrayValues.clear();
		arrayValues.addAll(alAlgs);
		arrayOptType.clear();
		arrayOptType.addAll(alOpts);
		arrayOptValues.clear();
		arrayOptValues.addAll(alOptVals);
		arraySwitches.clear();
		arraySwitches.addAll(alSw);
		arrayStdRev.clear();
		arrayStdRev.addAll(alStdRev);
		oaa = new OptionsArrayAdapter(getContext(), arrayValues);
		lvOpt.setAdapter(oaa);
		
	}
	
	//-------------------------------------------------------------------------------------------------------------------
	class OptionsArrayAdapter extends ArrayAdapter {

		private final Context context;
		private ArrayList<String> alValues = new ArrayList<>();
		String[] OptMemory = new String[arrayOptValues.size()];
		
		public OptionsArrayAdapter(Context context, ArrayList<String> values) {
			super(context, R.layout.tab2_listrow_layouttf, values);
			this.context = context;
			this.alValues = values;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			//-----------------------------------------------------------------------------------------

			class OptSpinnerListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
				
				
				boolean userSelect = false;
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					userSelect = true;
					return false;
				}
				
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					System.out.println("POSITION ============================================================================================:::::: "+pos);
					if (pos == 0) arrayOptValues.set(position, "true");
					else arrayOptValues.set(position, "false");

					createOptionsString();
//					optionsString = "";
//					for (int i = 0; i < arrayOptValues.size(); i++) {
//						if (arrayOptType.get(i).equals("edit")) optionsString += arraySwitches.get(i)+" "+arrayOptValues.get(i)+" ";
//						else {
//							if (arrayOptValues.get(i).equals("true")) optionsString += arraySwitches.get(i)+" ";
//						}
//						System.out.println(optionsString);
//					}
				}
				
				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {
				
				}
			}
			
			//-----------------------------------------------------------------------------------------
			
			
			class OptEditWatcher implements TextWatcher, AdapterView.OnItemSelectedListener {
				
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				}
				
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
											  int arg3) {
				}
				
				public void afterTextChanged(Editable arg0) {
					System.out.println(arg0);
//					OptMemory[position] = arg0.toString();
					arrayOptValues.set(position, arg0.toString());
					createOptionsString();
				}
				
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				}
				
				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {
				
				}
				
			}
			
			//-----------------------------------------------------------------------------------------
			
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView;
			
			
			if (arrayOptType.get(position).equals("tf")) {
				rowView = inflater.inflate(R.layout.tab2_listrow_layouttf,parent,false);
				Spinner spinnerTF = rowView.findViewById(R.id.spinnerTF);
				spinnerTF.setOnTouchListener(new OptSpinnerListener());
				spinnerTF.setOnItemSelectedListener(new OptSpinnerListener());
				
				if (arrayOptValues.get(position).equals("false")) {
					spinnerTF.setSelection(1);
				} else {
					spinnerTF.setSelection(0);
				}
			} else {
				rowView = inflater.inflate(R.layout.tab2_listrow_layoutedit, parent, false);
				editText = rowView.findViewById(R.id.editText);
				editText.setText(arrayOptValues.get(position));
				OptEditWatcher oew = new OptEditWatcher();
				editText.addTextChangedListener(oew);
			}
			TextView tvType = rowView.findViewById(R.id.tvAlgOpt);
			tvType.setText(arrayValues.get(position));
			return rowView;

		}
		
		void createOptionsString() {
			optionsString = "";
			for (int i = 0; i < arrayOptValues.size(); i++) {
				if (arrayOptType.get(i).equals("edit")) {
					if (!arrayOptValues.get(i).trim().equals("")) optionsString += arraySwitches.get(i)+" "+arrayOptValues.get(i)+" ";
				}
				else {
					if ((arrayOptValues.get(i).equals("true") && arrayStdRev.get(i).equals("norm")) ||
							(arrayOptValues.get(i).equals("false") && arrayStdRev.get(i).equals("rev"))
					) optionsString += arraySwitches.get(i)+" ";

				}
				System.out.println("OPTIONS STRING: =====> "+optionsString);
			}
			if (optionsString.contains("-num-slots") && Integer.parseInt(arrayOptValues.get(0)) != 1)  {
				new AlertDialog.Builder(context)
						.setTitle("More cores, more heat")
						.setMessage("Data mining is a CPU-intensive task and setting the number of cores used to more than one will cause your device to generate more heat - in short, the more cores, the more heat." +
								" Be aware and probably avoid using it on larger dataset, ok?")
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {

							}
						})
						.show();
			}
		}
		
	}

	
	
	//-------------------------------------------------------------------------------------------------------------------
	class AlgorithmArrayAdapter extends ArrayAdapter {
		
		private final Context context;
		private String[] values;
		ArrayList<ArrayList<String>> listAlgs = new ArrayList<>();
		ArrayList<String> algs0 = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.arrayBayes)));
		ArrayList<String> algs1 = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.arrayFunctions)));
		ArrayList<String> algs2 = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.arrayLazy)));
		ArrayList<String> algs3 = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.arrayMeta)));
		ArrayList<String> algs4 = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.arrayRules)));
		ArrayList<String> algs5 = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.arrayTrees)));
		ArrayList<String> algs6 = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.arrayCluster)));
		ArrayList<String> algs7 = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.arrayAssociate)));
		TextView tvAlgSel = rootView.findViewById(R.id.tvAlgSel);
		
		ArrayList<ArrayList<Integer>> opts = new ArrayList<>();
		ArrayList<Integer> res0 = new ArrayList<Integer>(Arrays.asList(R.array.BayesNet,R.array.NaiveBayes));
		ArrayList<Integer> res1 = new ArrayList<Integer>(Arrays.asList(R.array.Logistic,R.array.SimpleLogistic,R.array.MultilayerPerceptron));
		ArrayList<Integer> res2 = new ArrayList<Integer>(Arrays.asList(R.array.IBk, R.array.KStar));
		ArrayList<Integer> res3 = new ArrayList<Integer>(Arrays.asList(R.array.AdaBoostM1,R.array.Bagging,R.array.LogitBoost,R.array.MultiBoostAB,
				R.array.RandomCommittee,R.array.RandomSubSpace,R.array.RotationForest));
		ArrayList<Integer> res4 = new ArrayList<Integer>(Arrays.asList(R.array.ConjunctiveRule,R.array.DecisionTable,R.array.DTNB,R.array.JRip,
				R.array.OneR,R.array.PART,R.array.Ridor,R.array.ZeroR));
		ArrayList<Integer> res5 = new ArrayList<Integer>(Arrays.asList(R.array.ADTree,R.array.BFTree,R.array.DecisionStump,R.array.FastForest,
				R.array.ForestPA,R.array.HoeffdingTree,R.array.J48,R.array.LADTree,R.array.RandomForest,R.array.RandomTree,R.array.REPTree,R.array.SimpleCART,
				R.array.SPAARC, R.array.SysFor));
		
				
		public AlgorithmArrayAdapter(Context context, String[] values) {
			super(context,R.layout.tab2_listrow_layoutalg, values);
			this.context = context;
			this.values = values;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			
			listPosition = position;
			
			class SpinnerSelectionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {
				
				boolean userSelect = false;
				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					userSelect = true;
					return false;
				}
				
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					if (userSelect) {
						//------------ set algorithm type - classifier 1, clusterer 2 or associator 3
						if (position < 6) alType = 1;
						else if (position == 6) alType = 2;
						else alType = 3;
						MainActivity.RunFragment.changeFragment();
						//------------ set algorithm type - classifier 1, clusterer 2 or associator 3
						
						if (pos > 0) {
							System.out.println("rowSelected:"+position+" spinnerItem:"+pos);
							rowPosition[position] = pos;
							for (int i = 0; i < algList.length; i++) {
								if (i != position) rowPosition[i] = 0;
							}
							tvAlgSel.setText(listAlgs.get(position).get(pos));
							nameClassifier = tvAlgSel.getText().toString();
							notifyDataSetChanged();
							if (position <= 5 && pos > 0) {
								collectOptions(opts.get(position).get(pos-1));
								lvOpt.setVisibility(View.VISIBLE);
								oaa.notifyDataSetChanged();
								createOptionsString();
							} else {
								lvOpt.setVisibility(View.INVISIBLE);
							}
						}
						userSelect = false;
					}
				}
				
				@Override
				public void onNothingSelected(AdapterView<?> adapterView) {
				
				}
			}
			
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.tab2_listrow_layoutalg,parent,false);
			TextView tvAlg = (TextView) rowView.findViewById(R.id.tvALG);
			ArrayList<String> algs = new ArrayList<>();
			listAlgs.add(algs0);
			listAlgs.add(algs1);
			listAlgs.add(algs2);
			listAlgs.add(algs3);
			listAlgs.add(algs4);
			listAlgs.add(algs5);
			listAlgs.add(algs6);
			listAlgs.add(algs7);
			opts.add(res0);
			opts.add(res1);
			opts.add(res2);
			opts.add(res3);
			opts.add(res4);
			opts.add(res5);
			
			algs = listAlgs.get(position);
			ArrayAdapter<String> algAdapter = new ArrayAdapter<String>(context, R.layout.tab2_listrow_layoutspinner, algs);
			algAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			Spinner spinnerAlg = (Spinner) rowView.findViewById(R.id.spinnerALG);
			tvAlg.setText(values[position]);
			spinnerAlg.setAdapter(algAdapter);
			SpinnerSelectionListener ssl = new SpinnerSelectionListener();
			spinnerAlg.setOnItemSelectedListener(ssl);
			spinnerAlg.setOnTouchListener(ssl);
			
			if (rowPosition[position] != 0) {
				spinnerAlg.setSelection(rowPosition[position]);
			}
			
			return rowView;
		}
		
		void createOptionsString() {
			optionsString = "";
			for (int i = 0; i < arrayOptValues.size(); i++) {
				if (arrayOptType.get(i).equals("edit")) optionsString += arraySwitches.get(i)+" "+arrayOptValues.get(i)+" ";
				else {
					if (arrayOptValues.get(i).equals("true")) optionsString += arraySwitches.get(i)+" ";
				}
				System.out.println(optionsString);
			}
		}
		
	}
	
}
