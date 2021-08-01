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
	ConfusionMatrixActivity.java
    (C) Copyright Darren Yates 2018-2021
	Developed using a combination of Weka 3.8.5 and algorithms developed by Charles Sturt University
	DataLearner is licensed GPLv3.0, source code is available on GitHub
	Weka 3.8.5 is licensed GPLv3.0, source code is available on GitHub
*/

package au.com.darrenyates.datalearner2;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import static au.com.darrenyates.datalearner2.DataAnalysis3.buildModel;
import static au.com.darrenyates.datalearner2.DataAnalysis3.buildData;
import static au.com.darrenyates.datalearner2.DataAnalysis3.classType;
import static au.com.darrenyates.datalearner2.DataAnalysis3.classifierTree;
import static au.com.darrenyates.datalearner2.DataAnalysis3.returnEval;


public class ConfusionMatrixActivity extends AppCompatActivity {
	
	private static final int WRITE_REQUEST_CODE = 43;
	TextView tv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.matrix_main);
		
		tv = findViewById(R.id.tvMatrix);
		Button btnCopy = findViewById(R.id.btnCopy);
		btnCopy.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				copyToClipBoard();
			}
		});
		
		Button btnSave = findViewById(R.id.btnSave);
		btnSave.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveFile();
			}
		});
		
		tv.setMovementMethod(new ScrollingMovementMethod());
		try {
			if (classType == 0 && returnEval != null) {
				tv.append(returnEval.toClassDetailsString("=== Detailed Accuracy by Class ===\r\n"));
				tv.append("\r\n");
				tv.append(returnEval.toMatrixString("=== Confusion Matrix ===\r\n"));
			}
			if (classifierTree != "") {
				tv.append("\r\n");
				tv.append("=== Generated Classifier Model ===\n\n");
				tv.append(classifierTree);
			}
			tv.append("\r\n");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void copyToClipBoard() {
		ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		ClipData cd = ClipData.newPlainText("Confusion Matrx/Model Data", tv.getText());
		cb.setPrimaryClip(cd);
		showCopySteps();
	}
	
	void showCopySteps() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("Model data has been copied");
		alertDialog.setIcon(R.mipmap.ic_launcher);
		alertDialog.setMessage(getText(R.string.copy));
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.str_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		alertDialog.show();
		
	}
	
	void saveFile() {
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_TITLE, "<Set model name>");
		startActivityForResult(intent, WRITE_REQUEST_CODE);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent returnIntent) {
		if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			try {
//				String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
//				ActivityCompat.requestPermissions(this, permissions, WRITE_REQUEST_CODE);
				Uri filename = returnIntent.getData();
				ParcelFileDescriptor pfd = this.getContentResolver().openFileDescriptor(filename, "w");
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(pfd.getFileDescriptor()));
				oos.writeObject(buildModel);

				buildData = buildData.stringFreeStructure();
				if (buildData != null) oos.writeObject(buildData);

				oos.close();
				pfd.close();
				
				Toast toast = Toast.makeText(getApplicationContext(), "Model has been saved successfully.", Toast.LENGTH_LONG);
				toast.show();
			} catch (Exception e) {
				e.printStackTrace();
				tv.append(e.toString());
				System.out.println(e.toString());
			}
		}
	}
}