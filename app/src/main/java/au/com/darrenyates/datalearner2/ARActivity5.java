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
 * DataLearner 2 - a data-mining app for Android
 * ARActivity5.java
 * (C) Copyright Darren Yates 2018-2021
*/

package au.com.darrenyates.datalearner2;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.Iterator;

import static au.com.darrenyates.datalearner2.MainActivity.LoadFragment.distinctClassColors;
import static au.com.darrenyates.datalearner2.MainActivity.LoadFragment.values;
import static au.com.darrenyates.datalearner2.MainActivity.att1;
import static au.com.darrenyates.datalearner2.MainActivity.att2;
import static au.com.darrenyates.datalearner2.MainActivity.att3;
import static au.com.darrenyates.datalearner2.MainActivity.cl;

public class ARActivity5 extends AppCompatActivity {
	
	private ArFragment arFragment;
	private Renderable nodeRenderable;
	
	private ArrayList<Vector3> axesLength = new ArrayList<>();
	private ArrayList<Vector3> axesZero = new ArrayList<>();
	private ArrayList<Color> listColor = new ArrayList<>();
	
	static private boolean drawnComplete = false;
	
	private int intAttX, intAttY, intAttZ;
	private float scaleFactor;
	TextView tvAtt1, tvAtt2, tvAtt3;
	ListView mClassList;
	TreeArrayAdapter mAdapter;
	String xName, yName, zName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ar);
		
		tvAtt1 = findViewById(R.id.tvX);
		tvAtt2 = findViewById(R.id.tvY);
		tvAtt3 = findViewById(R.id.tvZ);
		
		mClassList = findViewById(R.id.classList);
		mAdapter = new TreeArrayAdapter(this, values);
		mClassList.setAdapter(mAdapter);
		
		drawnComplete = false;
		Bundle bundle = getIntent().getExtras();
		Uri uriDataset = null;
		String filePath = "";
		if (bundle != null) {
			uriDataset = bundle.getParcelable("uri");
			intAttX = bundle.getInt("X");
			intAttY = bundle.getInt("Y");
			intAttZ = bundle.getInt("Z");
			xName = bundle.getString("Xname");
			yName = bundle.getString("Yname");
			zName = bundle.getString("Zname");
		}
		
		String xText = xName+" ("+(intAttX+1)+")";
		String yText = yName+" ("+(intAttY+1)+")";
		String zText = zName+" ("+(intAttZ+1)+")";
		tvAtt1.setText(xText);
		tvAtt2.setText(yText);
		tvAtt3.setText(zText);
		
		arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
		arFragment.setOnTapArPlaneListener(this::handleTap);
		
		axesLength.add(new Vector3(0.5f, 0.01f, 0.01f));
		axesLength.add(new Vector3(0.01f, 0.5f, 0.01f));
		axesLength.add(new Vector3(0.01f, 0.01f, 0.5f));
		
		axesZero.add(new Vector3(0.0f, 0.02f, 0.25f));
		axesZero.add(new Vector3(-0.25f, 0.26f, 0.25f));
		axesZero.add(new Vector3(-0.25f, 0.02f, 0f));
		
		listColor.add(new Color(android.graphics.Color.RED));
		listColor.add(new Color(android.graphics.Color.GREEN));
		listColor.add(new Color(android.graphics.Color.BLUE));
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (arFragment == null) return;
	}
	
	
	void handleTap(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
		
		if (drawnComplete) return;

//------------------------------------------------------------------------------------------------------
		
		Anchor anchor = hitResult.createAnchor();
		AnchorNode anchorNode = new AnchorNode(anchor);
		anchorNode.setParent(arFragment.getArSceneView().getScene());
		
		TransformableNode a0 = new TransformableNode(arFragment.getTransformationSystem());
		a0.setLocalScale(new Vector3(0.9f, 0.9f, 0.9f));
		a0.getScaleController().setMinScale(0.4f);
		a0.getScaleController().setMaxScale(4.0f);
		MaterialFactory.makeOpaqueWithColor(this, new Color(0f, 0f, 0f))
				.thenAccept(material -> {
					this.nodeRenderable = ShapeFactory.makeCube(new Vector3(0.72f, 0.01f, 0.72f),
							new Vector3(0.0f, 0.001f, 0.0f), material);
					a0.setRenderable(nodeRenderable);
					a0.setParent(anchorNode);
				});
		
		// - transparent cube around the data points...
		Node a2 = new Node();
		MaterialFactory.makeTransparentWithColor(this, new Color(0f, 0f, 0f, 0.005f))
				.thenAccept(material -> {
					this.nodeRenderable = ShapeFactory.makeCube(new Vector3(0.5f, 0.5f, 0.5f), new Vector3(0.0f, 0.26f, 0.0f), material);
					a2.setRenderable(nodeRenderable);
					a2.setParent(a0);
				});

//---------------------------------------------------------------------------------------------------------
// draw the three coloured axes
		Iterator<Vector3> aL = axesLength.iterator();
		Iterator<Vector3> aZ = axesZero.iterator();
		Iterator<Color> c = listColor.iterator();
		
		while (aL.hasNext()) {
			Vector3 vectorL = aL.next();
			Vector3 vectorZ = aZ.next();
			Color colorC = c.next();
			
			Node anode = new Node();
			MaterialFactory.makeOpaqueWithColor(this, colorC)
					.thenAccept(material -> {
						this.nodeRenderable = ShapeFactory.makeCube(vectorL, vectorZ, material);
						anode.setRenderable(nodeRenderable);
						anode.setParent(a0);
					});
			
		}
		
		Vector3 pointSize = new Vector3(0.01f, 0.01f, 0.01f);
		Iterator<Double> it1 = att1.iterator();
		Iterator<Double> it2 = att2.iterator();
		Iterator<Double> it3 = att3.iterator();
		Iterator<Color> itclass = cl.iterator();
//---------------------------------------------------------------------------------------------------------
		
		if (att1.size() > 2000) showAlert("Dataset too big, but...", "We'll attempt to display the first 2,000 records. " +
				"You may see reduced performance on slower phones (we're researching a fix).");

//---------------------------------------------------------------------------------------------------------
// Plot attributes and class values as tiny cubes...
		int recordCount = 0;
		while (it1.hasNext()) {
			recordCount++; if (recordCount > 2000) break;
			Float f1 = (float) (double) it1.next() - 0.25f;
			Float f2 = (float) (double) it2.next() + 0.01f;
			Float f3 = (float) (double) it3.next() - 0.25f;
			Vector3 pointPos = new Vector3(f1, f2, f3);
			Color pointColor = itclass.next();
			
			Node bnode = new Node();
			MaterialFactory.makeOpaqueWithColor(this, pointColor)
					.thenAccept(material -> {
						this.nodeRenderable = ShapeFactory.makeCube(pointSize, pointPos, material);
						bnode.setRenderable(nodeRenderable);
						bnode.setParent(a0);
					});
		}
//---------------------------------------------------------------------------------------------------------
// --------- THE THREE AXIS LABEL PANELS ------------------------------------------------------------------

		ViewRenderable.builder()
				.setView(this, R.layout.panel_controls).build()
				.thenAccept( viewRenderable -> {
					Node p1 = new Node();
					p1.setParent(a0);
					p1.setRenderable(viewRenderable);
					p1.setLocalPosition(new Vector3(0f, 0.02f, 0.36f));
					p1.setLocalRotation(Quaternion.axisAngle(new Vector3(1.0f, 0f,0f), -90f));
					TextView tv = viewRenderable.getView().findViewById(R.id.tvPanel);
					tv.setText(xName);
				});
		
		ViewRenderable.builder()
				.setView(this, R.layout.panel_controls).build()
				.thenAccept( viewRenderable -> {
					Node p2 = new Node();
					p2.setParent(a0);
					p2.setRenderable(viewRenderable);
					p2.setLocalPosition(new Vector3(-0.35f, 0.27f, 0.25f));
					p2.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 0f,1f), -90f));
					TextView tv = viewRenderable.getView().findViewById(R.id.tvPanel);
					tv.setText(yName);
				});
		
		ViewRenderable.builder()
				.setView(this, R.layout.panel_controls).build()
				.thenAccept( viewRenderable -> {
					Node p3 = new Node();
					p3.setParent(a0);
					p3.setRenderable(viewRenderable);
					p3.setLocalPosition(new Vector3(-0.36f, 0.03f, -0.01f));
					Quaternion rot1 = Quaternion.axisAngle(new Vector3(1f, 0f,0f), -90f);
					Quaternion rot2 = Quaternion.axisAngle(new Vector3(0f, 0f,1f), 270f);
					p3.setLocalRotation(Quaternion.multiply(rot1, rot2));
					TextView tv = viewRenderable.getView().findViewById(R.id.tvPanel);
					tv.setText(zName);
				});

		drawnComplete = true;
	}
	
//----------------------------------------------------------------------------------------------------
	
	void showAlert(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(title);
		alertDialog.setIcon(R.mipmap.ic_launcher);
		alertDialog.setMessage(message);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.str_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		alertDialog.show();
	}


// ------ THE INFORMATION OVERLAY PANELS --------------------------------------------------------------
	
	class TreeArrayAdapter extends ArrayAdapter {
		private final Context context;
		private final ArrayList<String> values;
		
		public TreeArrayAdapter(Context context, ArrayList<String> values) {
			super(context, R.layout.class_item, values);
			this.context = context;
			this.values = values;
		}
		
		@Override
		public View getView (int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.class_item, parent, false);
			TextView tvBlock = (TextView) rowView.findViewById(R.id.tvBlock);
			TextView tvClass = (TextView) rowView.findViewById(R.id.tvClass);
			Color classColor = distinctClassColors.get(position);
			tvBlock.setTextColor(android.graphics.Color.rgb((int)(classColor.r*255),(int)(classColor.g*255),(int)(classColor.b*255)));
			tvBlock.setText("■");
			tvClass.setText(values.get(position));
			return(rowView);
		}
	}
}