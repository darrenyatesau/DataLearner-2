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
	NetFragment13.java
    (C) Copyright Darren Yates 2018-2021
	Developed using a combination of Weka 3.8.5 and algorithms developed by Charles Sturt University
	DataLearner is licensed GPLv3.0, source code is available on GitHub
	Weka 3.8.5 is licensed GPLv3.0, source code is available on GitHub
*/

package au.com.darrenyates.datalearner2;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import pub.devrel.easypermissions.EasyPermissions;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.RandomizableParallelIteratedSingleClassifierEnhancer;
import weka.classifiers.meta.Bagging;
import weka.classifiers.trees.FastForest;
import weka.classifiers.trees.FastForestRT;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Instances;
import weka.core.Randomizable;

import static android.os.Looper.getMainLooper;
import static au.com.darrenyates.datalearner2.MainActivity.nameClassifier;
import static au.com.darrenyates.datalearner2.MainActivity.runType;
import static au.com.darrenyates.datalearner2.MainActivity.traindata;
import static au.com.darrenyates.datalearner2.SelectBFragment.optionsString;

//import android.widget.Toast;

public class NetFragment13 extends Fragment {

	View lanView;
	WifiManager wifiManager;
	WifiP2pManager mManager;
	WifiP2pManager.Channel mChannel;
	BroadcastReceiver mReceiver;
	IntentFilter mIntentFilter;
	ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	String[] deviceNameArray;
	WifiP2pDevice[] deviceArray;

	ArrayList<String> deviceNameList = new ArrayList<>();
	ArrayList<WifiP2pDevice> deviceList = new ArrayList<>();

	WifiP2pManager.PeerListListener peerListListener;
	WifiP2pManager.ConnectionInfoListener connectionInfoListener;

	ClientClass clientClass;

	static int numModelsSent = 0;
	volatile static int numModelsReceived = 0;

	Button btnSetWifi, btnDlanStart, btnConnect;
	TextView tvConnStatus;
	static TextView tvNetStatus;
	static ScrollView scroll;

	ListView listDevices;
	ListArrayAdapter listAdapter;
	ArrayList<String> listChecked = new ArrayList<>();

	static int typeMode = 0;
	static int userStart = 0;

	static Socket returnSocket;
	static Socket sendSocket;
	static Socket clientSocket;
//	static ObjectOutputStream oos;
//	static ObjectInputStream ois;
//	static int transferCount = 0;
	static int numIterations = 100;

	static Classifier[] m_Classifiers;
	//static ArrayList<Integer> phoneDevices = new ArrayList<>();
	static int deviceNumber = 0;

	ArrayList<PhoneConnection> phoneConnections = new ArrayList<>();
	PhoneConnection thisPhone = new PhoneConnection();

	static int serverInitialise = 0;

	//volatile static int treeSeedCount = 0;
	static AtomicInteger treeSeedCount = new AtomicInteger(0);

	//static int cpuCoreCount = 0;
	static ArrayList<String> seedsList = new ArrayList<>();
	static int threadCompletions = 0;
	static ArrayList<Classifier> clientClassifierList = new ArrayList<>();
	static ArrayList<String> clientSeedList = new ArrayList<>();
	static ArrayList<Thread> buildThreadList = new ArrayList<>();

	long timeTestStart;
	long timeTestFinish;

	Classifier baseClassifier;

	//int serverThreadCompletions = 0;
	int serverCpuCoreCount = 0;
	int clientCpuCoreCount = 0;

	static int serverThreads = 0;

	final Semaphore semaphore = new Semaphore(1);
	String clientSeeds = "";

//	static BaggingDLP bag;
//	static RandomForestDLP bag;
	static RandomizableParallelIteratedSingleClassifierEnhancer bag;
	int runBefore = 0;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {


		lanView = inflater.inflate(R.layout.fragment_main_tab5, container, false);
		wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		mManager = (WifiP2pManager) getContext().getApplicationContext().getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(getContext(), getMainLooper(), null);
		mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		btnSetWifi = (Button) lanView.findViewById(R.id.btnWifi);
		tvConnStatus = (TextView) lanView.findViewById(R.id.tvConnStatus);
		scroll = (ScrollView) lanView.findViewById(R.id.svNetStatus);
		tvNetStatus = (TextView) lanView.findViewById(R.id.tvNetStatus);
		tvNetStatus.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				scroll.post(new Runnable() {
					@Override
					public void run() {
						scroll.fullScroll(View.FOCUS_DOWN);
					}
				});
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		listDevices = lanView.findViewById(R.id.list_devices);

		if (wifiManager.isWifiEnabled()) {
			btnSetWifi.setText("Wifi Off");
			tvConnStatus.setText("WiFi Status: ON");
			tvConnStatus.setBackgroundColor(0x4000FF00);
		} else {
			btnSetWifi.setText("Wifi On");
			tvConnStatus.setText("WiFi Status: OFF");
			tvConnStatus.setBackgroundColor(0x40FF8000);
		}
		btnSetWifi.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (wifiManager.isWifiEnabled()) {
					wifiManager.setWifiEnabled(false);
					btnSetWifi.setText("Wifi On");
					tvConnStatus.setText("WiFi Status: OFF");
//					tvConnStatus.setBackgroundColor(0x80FFFFFF);
					tvConnStatus.setBackgroundColor(0x40FF8000);

				} else {
					wifiManager.setWifiEnabled(true);
					btnSetWifi.setText("Wifi Off");
					tvConnStatus.setText("WiFi Status: ON");
//					tvConnStatus.setBackgroundColor(0x80FFFFFF);
					tvConnStatus.setBackgroundColor(0x4000FF00);
				}
			}
		});

		Button btnPing = (Button) lanView.findViewById(R.id.btnPing);
		btnPing.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (typeMode == 2) { // CLIENT
					String pingMessage = clientSocket.getLocalSocketAddress().toString() + " ("+android.os.Build.MODEL+")";
					tvNetStatus.append("\nPING: "+pingMessage);
					PingServer pingServer = new PingServer(thisPhone.pcOOS, pingMessage);
					pingServer.start();
				} else {
					for (int i = 0; i < phoneConnections.size(); i++) {
						String pingMessage = "\nPING: SERVER ("+android.os.Build.MODEL+")";
						PingClients pc = new PingClients(phoneConnections.get(i), pingMessage);
						pc.start();
					}
				}
			}
		});

		Button btnDiscover = (Button) lanView.findViewById(R.id.btnDiscover);
		btnDiscover.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (deviceNameList.size() > 0) {
					deviceNameList.clear();
					listAdapter.notifyDataSetChanged();
				}

				IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
				Intent batteryStatus = getContext().registerReceiver(null, iFilter);
				int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				double batPercent = level * 100 / (double)scale;
				if (batPercent < 20) {
					showAlert("Battery too low.", "Your device battery is below 20% capacity. Recharge first before attempting this option.");
				} else {
					listChecked.clear();
					tvNetStatus.setText("");
					deviceNumber = 0;
					phoneConnections.clear();
					permissionsCheck();
				}
			}
		});


		btnConnect = (Button) lanView.findViewById(R.id.btnConnect);
		btnConnect.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				tvNetStatus.setText("Connecting...");
				userStart = 1;
				connect2();
//				launchTest();
			}
		});

		btnDlanStart = (Button) lanView.findViewById(R.id.btnDlanStart);
		btnDlanStart.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				buildThreadList.clear(); serverThreads = 0;
				treeSeedCount.set(0); numModelsReceived = 0; numModelsSent = 0; threadCompletions = 0;

				timeTestStart = System.nanoTime();
				if (findNumIterations() == 0) {
//----------------------------------------------------------------------------------------------------------------------------
					for (int i = 0; i < phoneConnections.size(); i++) {
						new InitialiseClients(phoneConnections.get(i).pcOOS).start();
						statusUpdate("\nSERVER: classifier > "+nameClassifier+" > "+phoneConnections.get(i).pcSocket.getLocalAddress());
						Instances mData;
						if (runType < 2) mData = MainActivity.data;
						else mData = traindata;
						statusUpdate("\nSERVER: dataset > "+mData.relationName() + " ("+mData.numInstances()+")");
					}
//----------------------------------------------------------------------------------------------------------------------------
					startNextLocalTask();
//----------------------------------------------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------------------------------------------
				}
			}
		});

		peerListListener = new WifiP2pManager.PeerListListener() {
			@Override
			public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
//				System.out.println(">>>> onPeersAvailable");
				if (!wifiP2pDeviceList.getDeviceList().equals(peers)) {
					peers.clear();
					peers.addAll(wifiP2pDeviceList.getDeviceList());

					deviceNameArray = new String[wifiP2pDeviceList.getDeviceList().size()];
					deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];

					deviceNameList = new ArrayList<>();
					deviceList = new ArrayList<>();

					int index = 0;
//					System.out.println(">>>> peerList size: "+peers.size());
					for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
						deviceNameList.add(device.deviceName);
						deviceList.add(device);
						index++;
//						System.out.println(device.deviceName + " >>>>> " +device.deviceAddress);
					}

					listAdapter = new ListArrayAdapter(getContext(), deviceNameList);
					listDevices.setAdapter(listAdapter);
					listAdapter.notifyDataSetChanged();

				}
//				if (peers.size() == 0) {
////					Toast.makeText(getContext(), "No device found.", Toast.LENGTH_SHORT).show();
//					return;
//				}
			}
		};

		connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
			@Override
			public void onConnectionInfoAvailable(WifiP2pInfo info) {

//				System.out.println("----------------------------------------------------- CONNECTIONINFOLISTENER --------------");
				InetAddress groupOwnerAddress = info.groupOwnerAddress;
				if (info.groupFormed && info.isGroupOwner) {
					typeMode = 1; // SERVER
//					Toast.makeText(getContext(), "Host", Toast.LENGTH_LONG).show();
//					System.out.println("@@@@@ SERVER MODE");
					tvConnStatus.setText("Mode:\nSERVER");
					tvConnStatus.setBackgroundColor(0x60004BC8);
//					System.out.println( " ======== device number before class caled: "+deviceNumber);
					if (serverInitialise == 0) {
						ServerClass sc = new ServerClass();
						sc.start();
						serverInitialise = 1;
					}
//					System.out.println(" ==================================== DEVICE NUMBER: "+deviceNumber);
					connect2();

				} else if (info.groupFormed) {
					typeMode = 2; // CLIENT
//					Toast.makeText(getContext(), "Client", Toast.LENGTH_LONG).show();
//					System.out.println("@@@@@ CLIENT MODE");
					tvConnStatus.setText("Mode:\nCLIENT");
					tvConnStatus.setBackgroundColor(0x8032B432);
					clientClass = new ClientClass(groupOwnerAddress);
					clientClass.start();
//					System.out.println("---------------------------- CLIENTCLASS START()");
				}
			}
		};

		return lanView;
	}

	void showAlert(String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
		alertDialog.setTitle(title);
		alertDialog.setIcon(R.mipmap.ic_launcher);
		alertDialog.setMessage(message);
		alertDialog.show();
	}

	public void findPeers() {
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {

				tvConnStatus.setText("Discovery STARTED");
				tvConnStatus.setBackgroundColor(0xA0FFFF00);

			}

			@Override
			public void onFailure(int i) {

				tvConnStatus.setText("Discovery FAILED");
				tvConnStatus.setBackgroundColor(0x80C80000);

			}
		});
	}


	//---------------------------------------------------------------------------------------------------------------------------------
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	private void permissionsCheck() {
		String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION};
		if (EasyPermissions.hasPermissions(getContext(), perms)) {
//			createGroup();
			findPeers();
		} else {
			EasyPermissions.requestPermissions(this,
					"DataLearner needs LOCATION permission to find WiFi-Direct-enabled devices to work with. Otherwise, it won't work.",3801,perms);
		}

	}

	//-------------------------------------------------------------------------------------------------

	@Override
	public void onResume() {
		super.onResume();
//		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, mIntentFilter);
		getContext().registerReceiver(mReceiver, mIntentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();
		getContext().unregisterReceiver(mReceiver);
	}

	//-------------------------------------------------------------------------------------------------
	class ListArrayAdapter extends ArrayAdapter {
		private final Context context;
		private final ArrayList<String> devices;

		public ListArrayAdapter(Context context, ArrayList<String> devices) {
			super(context, R.layout.item_device,devices);
			this.context = context;
			this.devices = devices;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.item_device, parent, false);
			CheckBox cb = (CheckBox) rowView.findViewById(R.id.checkBoxDevice);
			cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
					if(isChecked) {
						listChecked.add(String.valueOf(position));
					} else {
						listChecked.remove(String.valueOf(position));
					}
//					System.out.println(Arrays.toString(listChecked.toArray()));
				}
			});
			cb.setText(devices.get(position));

			return rowView;
		}
	}
	//-------------------------------------------------------------------------------------------------
	public void connect2() {
//		System.out.println("*********************************** device number of list: "+deviceNumber+" of "+listChecked.size());

		if (deviceNumber < listChecked.size()) {
			WifiP2pDevice device = peers.get(Integer.parseInt(listChecked.get(deviceNumber)));
			WifiP2pConfig config = new WifiP2pConfig();
			config.deviceAddress = device.deviceAddress;
			config.wps.setup = WpsInfo.PBC;
			if (userStart == 1) config.groupOwnerIntent = 0; else config.groupOwnerIntent = 15;
			mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
				@Override
				public void onSuccess() {
//					System.out.println("--------------------------------------------------- CONNECT: SUCCESS "+config.groupOwnerIntent+"\tDevice: "+device.deviceName);
					tvNetStatus.append(" done.");
				}
				@Override
				public void onFailure(int reason) {
//					System.out.println("--------------------------------------------------- CONNECT: FAIL - "+reason+"\tDevice: "+device.deviceName);
				}
			});
		}
	}

//------------------------------------------------------------------------------------------------------

	public class PhoneConnection {
		Socket pcSocket;
		ObjectInputStream pcOIS;
		ObjectOutputStream pcOOS;
		int pcCores;
	}

//------------------------------------------------------------------------------------------------------

	//   SERVER CLASS

//------------------------------------------------------------------------------------------------------

	public class ServerClass extends Thread {
		Socket socket;

		@Override
		public void run() {
			try {
//				System.out.println("---------------------------------------------------------- SERVERCLASS run...");
				ServerSocket scServerSocket = new ServerSocket(38010,250);
//				System.out.println("====================== server address    : " + scServerSocket.getLocalSocketAddress().toString());
//				System.out.println("====================== server socket port: " + (38010));
				while (true) {
					Socket newSocket = scServerSocket.accept();
					PhoneConnection phoneConnection = new PhoneConnection();
					phoneConnection.pcSocket = newSocket;
					phoneConnection.pcOOS = new ObjectOutputStream(newSocket.getOutputStream());
					phoneConnection.pcOIS = new ObjectInputStream(newSocket.getInputStream());
					phoneConnections.add(phoneConnection);
					SendReceive2 sendReceive2 = new SendReceive2(phoneConnection);
					sendReceive2.start();
//					System.out.println(">>>>>>> SENDRECEIVE2 STARTED...");
					CPUCoreCounter cc = new CPUCoreCounter();
					serverCpuCoreCount = cc.coreCount;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
//------------------------------------------------------------------------------------------------------

	class SendReceive2 extends Thread {
		PhoneConnection pc;
		int returningModelCount = 0;
		int modelsReturnedThisTrip = 0;
		String[] returningSeedsSplit = {"1","2","3","4"};


		public SendReceive2(PhoneConnection pc) {
			this.pc = pc;
		}

		@Override
		public void run() {
			while (pc.pcSocket != null) {
				try {
					Object objCode = pc.pcOIS.readObject();
					int mesCode = (int)objCode;
					Object object = pc.pcOIS.readObject();
					ProcessMessage pm = new ProcessMessage(pc.pcSocket, mesCode, object);
					pm.start();

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					break;
				} catch (IOException e) {
					e.printStackTrace();
					break;
				} catch (NullPointerException e) {
					e.printStackTrace();
					break;
				}
			}
		}

//-------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------

//                       WRITING MESSAGE/DATA AREA

//-------------------------------------------------------------------------------------------------------------
//-------------------------------------------------------------------------------------------------------------

		public class ProcessMessage extends Thread {
			int messageCode;
			Object object;
			Socket socket;

			public ProcessMessage(Socket socket, int messageCode, Object object) {
				this.socket = socket;
				this.messageCode = messageCode;
				this.object = object;
			}
			@Override
			public void run() { // READ RECEIVED MESSAGES
				switch (messageCode) {
					case 0: // read by CLIENT
						nameClassifier = (String)object;
						statusUpdate("\n > SERVER: classifier > "+nameClassifier);
						runBefore = 0;
						break;
					case 1: // read by CLIENT
						MainActivity.data = (Instances)object;
						statusUpdate("\n > SERVER: dataset > "+MainActivity.data.relationName() + " ("+MainActivity.data.numInstances()+")");
						clientCpuCoreCount = new CPUCoreCounter().coreCount;
//						System.out.println("clientCPUCoreCOunt: "+clientCpuCoreCount);
						write("CLIENT",2, clientCpuCoreCount);
						break;
					case 2: // read by SERVER
						pc.pcCores = (Integer)object;
						statusUpdate("\nCLIENT: CPU cores > " + pc.pcCores + " > " + socket.getInetAddress().getHostName());
						try {
							semaphore.acquire();
						} catch (Exception e) {
							e.printStackTrace();
						}
							String seedsToSend = generateSeedString(pc.pcCores, treeSeedCount.get());
						semaphore.release();
						write("SERVER",3, seedsToSend);			// send seed(s)
//						startNextLocalTask();
						break;
					case 3: // read by CLIENT
						clientSeeds = (String)object;
						statusUpdate("\nSERVER: build seeds > "+clientSeeds);
						String[] seedSplit = clientSeeds.split(",");
						seedsList = new ArrayList<String>(Arrays.asList(seedSplit));
//						launchTest();
						if (runBefore == 0)	{
							runBefore = 1;
							launchTest2();
						} else {
							try {
								continueTest2(bag);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						break;
					case 4: // read by SERVER
						//returningModelCount = (int)object;
						String returningSeeds = (String)object;
//						System.out.println(returningSeeds);
						returningSeedsSplit = returningSeeds.split(",");
						returningModelCount = returningSeedsSplit.length;
//						System.out.println("--------------------------------------------- RETURNING SEEDS: "+returningSeeds);
//						System.out.println(treeSeedCount.get());
						break;
					case 5: // read by SERVER
						try {
							semaphore.acquire();
						} catch (Exception e) {
							e.printStackTrace();
						}
//							m_Classifiers[numModelsReceived] = (ClassifierDLP) object;
							int modelPos = Integer.parseInt(returningSeedsSplit[modelsReturnedThisTrip]);
							m_Classifiers[modelPos-1] = (Classifier)object;

							numModelsReceived++;
							modelsReturnedThisTrip++;
							statusUpdate("\nModels Received: "+ (numModelsReceived));
							if (modelsReturnedThisTrip == returningModelCount) {
								modelsReturnedThisTrip = 0;
								returningModelCount = 0;
								int currentCount = treeSeedCount.get();
								if (currentCount < numIterations) {
									if (currentCount + pc.pcCores > numIterations) {
										new SendNextTask(pc.pcOOS, generateSeedString(numIterations - currentCount, currentCount)).start();
									} else	new SendNextTask(pc.pcOOS, generateSeedString(pc.pcCores, currentCount)).start();
								} else {
									if (numModelsReceived == numIterations) {
										DecimalFormat df5 = new DecimalFormat("#.#####");
										timeTestFinish = System.nanoTime();
										statusUpdate("\nEnsemble completed.");
										statusUpdate("\n >>> ENSEMBLE BUILD TIME: "+df5.format((timeTestFinish-timeTestStart)/1000000000.0) + "s");
										MainActivity.modelLoaded = true;
//										for (int i = 0; i < 100; i++) {
//											System.out.println(i + " ===============================================================================================");
//											System.out.println(m_Classifiers[i].toString());
//										}

									}
								}
							}
						semaphore.release();
						break;

					case 8: // read by SERVER
						String pingFromWho = (String)object;
						statusUpdate("\nPING "+pingFromWho);
						statusUpdate(" > sent return ping...");
						try {
							pc.pcOOS.writeObject(9);
							pc.pcOOS.writeObject("\n  <<< PING: SERVER (" + android.os.Build.MODEL + ")");
						} catch (IOException e) {

						}
						break;
					case 9: // read by CLIENT
						String pingFromServer = (String)object;
						statusUpdate(pingFromServer);

				}
			}
		}
//----------------------------------------------------------------------------------------------------------------
		public String generateSeedString(int coreCount, int count) {
			String output = "";
			for (int i = 0; i < coreCount; i++) {
				treeSeedCount.incrementAndGet();
				if (i < coreCount-1) output += treeSeedCount.get()+",";
				else output += treeSeedCount.get();
			}
//			System.out.println("----- OUTPUT SEEDS: "+output);
			return output;
		}
//----------------------------------------------------------------------------------------------------------------





//----------------------------------------------------------------------------------------------------------------

		public void write(String node, int messageType, Object data) {
			switch (messageType) {
				case 2: // PHONE CPU CORES
					try {
						pc.pcOOS.writeObject(2);
						pc.pcOOS.writeObject(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case 3: // NEXT ROUND OF SEEDS
					try {
						pc.pcOOS.writeObject(3);
						pc.pcOOS.writeObject(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
			}
		}
	}
//	//------------------------------------------------------------------------------------------------------

//	public void launchTest() { // CLIENT - start another model build on the other device
//
//		clientClassifierList.clear();
//
//		for (int i = 0; i < seedsList.size(); i++) {
////			final RandomForestDLP bag = new RandomForestDLP();
//			final ClassifierDLP bag = new RandomForestDLP();
//			((RandomForestDLP)bag).setRepresentCopiesUsingWeights(true);
//			int baseSeed = ((RandomForestDLP)bag).getSeed();
//			final Random r = new Random(baseSeed + Integer.parseInt(seedsList.get(i)) - 1);
//			final int iteration = Integer.parseInt(seedsList.get(i)) - 1;
//			final Instances trainBag = MainActivity.data.resampleWithWeights(r, null,
//					((RandomForestDLP)bag).getRepresentCopiesUsingWeights(), ((RandomForestDLP)bag).getBagSizePercent());
//			final String thisSeed = seedsList.get(i);
//			final ClassifierDLP classifier = new RandomTreeDLP();
//			((RandomTreeDLP)classifier).setDoNotCheckCapabilities(true);
//			if (classifier instanceof Randomizable) {
//				Random ran = new Random(1);
//				for (int j = 0; j < iteration; j++) {
//					ran.nextInt();
//				}
//				final int nextran = ran.nextInt();
////				System.out.println(nextran);
//				((Randomizable)classifier).setSeed(nextran);
//			}
//
//			Thread thread = new Thread() {
//				@Override
//				public void run() {
//					try {
//						classifier.buildClassifier(trainBag);
//						clientClassifierList.add(classifier);
//						clientSeedList.add(thisSeed);
//						analysisThreadChecker();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			};
//			thread.start();
//			buildThreadList.add(thread);
//		}
//
//	}
////	//------------------------------------------------------------------------------------------------------

	public void launchTest2() {

		clientClassifierList.clear();
		Classifier m_Classifier;
		if(nameClassifier.equals("RandomForest")) {
			bag = new RandomForest();
			((RandomForest)bag).setRepresentCopiesUsingWeights(true);
			m_Classifier = new RandomTree();
			((RandomTree)m_Classifier).setDoNotCheckCapabilities(true);
		} else {
			bag = new FastForest();
			((FastForest)bag).setRepresentCopiesUsingWeights(true);
			m_Classifier = new FastForestRT();
			((FastForestRT)m_Classifier).setDoNotCheckCapabilities(true);
		}
		bag.setClassifier(m_Classifier);
		try {
			m_Classifiers = AbstractClassifier.makeCopies(m_Classifier, 100);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Random m_random = new Random(1);
		for (int j = 0; j < m_Classifiers.length; j++) {
			((Randomizable) m_Classifiers[j]).setSeed(m_random.nextInt());
		}
		try {
			continueTest2(bag);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void continueTest2(Classifier bag) throws Exception {
		ExecutorService executorPool = Executors.newFixedThreadPool(clientCpuCoreCount);
		final CountDownLatch doneSignal = new CountDownLatch(seedsList.size());

		int baseSeed = 0;
		if (nameClassifier.equals("RandomForest")) {
			bag = new RandomForest();
			baseSeed = ((RandomForest)bag).getSeed();
		}
		else {
			bag = new FastForest();
			baseSeed = ((FastForest)bag).getSeed();
		}

			//		int baseSeed = bag.getSeed();
		for (int i = 0; i < seedsList.size(); i++ ) {
			final String thisSeed = seedsList.get(i);
			final int seedValue = Integer.parseInt(thisSeed);
			final Random r = new Random(baseSeed + seedValue - 1);
			Instances trainBag;
			if (nameClassifier.equals("RandomForest")) {
				trainBag = MainActivity.data.resampleWithWeights(r, null,
						((Bagging)bag).getRepresentCopiesUsingWeights(), ((Bagging)bag).getBagSizePercent());
			} else {
				Instances tempData = new Instances(MainActivity.data);
				tempData.randomize(r);
				trainBag = new Instances(tempData, 0, (int) (0.5 * MainActivity.data.numInstances()));
			}
			
			
			final Classifier currentClassifier = m_Classifiers[seedValue - 1];
//			System.out.println(currentClassifier.toString());
			Runnable newTask = new Runnable() {
				@Override
				public void run() {
					try {
						currentClassifier.buildClassifier(trainBag);
//						clientClassifierList.add(currentClassifier);
						clientSeedList.add(thisSeed);
					} catch (Throwable ex) {
						ex.printStackTrace();
					} finally {
						doneSignal.countDown();
					}
				}
			};
			executorPool.submit(newTask);
		}
		doneSignal.await();
		executorPool.shutdownNow();
		sendModelsHome();

	}

//	//------------------------------------------------------------------------------------------------------

	public void sendModelsHome() {
		ReturnModelsToServer2 rms = new ReturnModelsToServer2 (thisPhone.pcOOS);
		rms.start();
	}


//	//------------------------------------------------------------------------------------------------------

	public void analysisThreadChecker() {
		threadCompletions++;
		if (threadCompletions == seedsList.size()) {
			threadCompletions = 0;
			ReturnModelsToServer rms = new ReturnModelsToServer(thisPhone.pcOOS, clientClassifierList);
			rms.start();
			buildThreadList.clear();
		}
	}

//	//------------------------------------------------------------------------------------------------------

	public void statusUpdate (String text) {
		tvNetStatus.post(new Runnable() {
			@Override
			public void run() {
				tvNetStatus.append(text);
			}
		});
	}

//	//------------------------------------------------------------------------------------------------------
	public class ClientClass extends Thread {
		Socket socket;
		String hostAdd;

		public ClientClass(InetAddress hostAddress) {
			this.hostAdd = hostAddress.getHostAddress();
			this.socket = new Socket();
			returnSocket = socket;
			sendSocket = socket;
		}
		@Override
		public void run() {
//			System.out.println(">>>><<<< USERSTART: "+userStart);
			try {
				socket.connect(new InetSocketAddress(hostAdd, 38010),600000);
				clientSocket = socket;
//				System.out.println("**************************************** Client socket: "+socket.getLocalSocketAddress());
				PhoneConnection pc = new PhoneConnection();
				pc.pcOOS = new ObjectOutputStream(socket.getOutputStream());
				pc.pcOIS = new ObjectInputStream(socket.getInputStream());
				pc.pcSocket = socket;
				thisPhone = pc;

//				System.out.println("=====================================================================CREATED SOCKET.CONNECT");
				SendReceive2 sendReceive2 = new SendReceive2(pc);
//				System.out.println("=====================================================================CREATED SENDRECEIVE2");
				sendReceive2.start();
//				System.out.println("HERE - CLIENT");

			} catch (IOException e) {
//				System.out.println("---------------- CLASSCLIENT.RUN ERROR -------------------");
				e.printStackTrace();
			}
		}
	}
//
//-------------------------------------------------------------------------------------------------------------

	public int findNumIterations() {
		Classifier classifier = new RandomForest();
		if (nameClassifier == null || nameClassifier.equals("")) {
			AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
			alertDialog.setTitle("Missing something...");
			alertDialog.setIcon(R.mipmap.ic_launcher);
			alertDialog.setMessage("You haven't selected a classifier to run on your DLAN. Please go back and choose one now.");
			alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.str_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
				}
			});
			alertDialog.show();
			return 1;
		}
		else if (nameClassifier.equals("Bagging")) {classifier = new Bagging();}
		else if (nameClassifier.equals("FastForest")) {classifier = new FastForest();}
		else if (nameClassifier.equals("RandomForest")) {classifier = new RandomForest();}
		try {
			String[] options = weka.core.Utils.splitOptions(optionsString);
			((AbstractClassifier) classifier).setOptions(options);

			numIterations = ((RandomizableParallelIteratedSingleClassifierEnhancer) classifier).getNumIterations();
			Classifier base = ((RandomizableParallelIteratedSingleClassifierEnhancer) classifier).getClassifier();
//			System.out.println(base.toString());
			m_Classifiers = AbstractClassifier.makeCopies(base, numIterations);

			Random ran = new Random(1);
			for (int j = 0; j < m_Classifiers.length; j++) {
				if (base instanceof Randomizable) {
					int nextran = ran.nextInt();
					((Randomizable) m_Classifiers[j]).setSeed(nextran);
				}
			}

			baseClassifier = base;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	//---------------------------------------------------------------------------------------------------

	public class InitialiseClients extends Thread {
		private ObjectOutputStream objectOutputStream;

		public InitialiseClients(ObjectOutputStream oos) {
			this.objectOutputStream = oos;
		}

		@Override
		public void run() {
			try {
				objectOutputStream.writeObject(0);
				objectOutputStream.writeObject(nameClassifier);

				Instances mData;
				if (runType < 2) mData = new Instances(MainActivity.data);
				else mData = new Instances(traindata);
				objectOutputStream.writeObject(1);
				objectOutputStream.writeObject(mData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//---------------------------------------------------------------------------------------------------

//	public void sendNewTask(ObjectOutputStream oos, String seeds) {
//		try {
//			SendNextTask snt = new SendNextTask(oos, seeds);
//			snt.start();
//		} catch (Exception e) {
//
//		}
//	}

	//---------------------------------------------------------------------------------------------------
	public class SendNextTask extends Thread {
		private ObjectOutputStream objectOutputStream;
		private String seeds;

		public SendNextTask(ObjectOutputStream oos, String seeds) {
			this.objectOutputStream = oos;
			this.seeds = seeds;
		}

		@Override
		public void run() {
			try {
				objectOutputStream.writeObject(3);
				objectOutputStream.writeObject(seeds);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//---------------------------------------------------------------------------------------------------
//	public class ReturnCoreCount  extends Thread {
//		private ObjectOutputStream rccOOS;
//		private int cores;
//
//		public ReturnCoreCount(ObjectOutputStream oos, int cores) {
//			this.rccOOS = oos;
//			this.cores = cores;
//		}
//
//		@Override
//		public void run() {
//			try {
////				System.out.println("------------------------- ReturnCoreCount.run");
//				rccOOS.writeObject(1);
//				rccOOS.writeObject(cores);
////				System.out.println("------------------------- SENT");
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	//---------------------------------------------------------------------------------------------------
	public class ReturnModelsToServer2 extends Thread {
		private ObjectOutputStream rmsOOS;
		private ArrayList<Classifier> m_Models;

		public ReturnModelsToServer2 (ObjectOutputStream oos) {
			this.rmsOOS = oos;
			this.m_Models = m_Models;
		}
		@Override
		public void run() {
			try {

				rmsOOS.writeObject(4);
				rmsOOS.writeObject(clientSeeds);

				String[] output = clientSeeds.split(",");
				for (int i = 0; i < output.length; i++) {
					rmsOOS.writeObject(5);
					rmsOOS.writeObject(m_Classifiers[Integer.parseInt(output[i])-1]);
				}
				clientSeedList.clear();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//---------------------------------------------------------------------------------------------------
	public class ReturnModelsToServer extends Thread {
		private ObjectOutputStream rmsOOS;
		private ArrayList<Classifier> m_Models;

		public ReturnModelsToServer(ObjectOutputStream oos, ArrayList<Classifier> m_Models) {
			this.rmsOOS = oos;
			this.m_Models = m_Models;
		}
		@Override
		public void run() {
			try {

				String output = clientSeedList.toString().substring(1, clientSeedList.toString().length()-1);
				output = output.replaceAll("\\s","");
//				System.out.println(output);
				rmsOOS.writeObject(4);
//				rmsOOS.writeObject(clientSeeds);
				rmsOOS.writeObject(output);

//				for (int i = 0; i < m_Models.size(); i++) {
//					rmsOOS.writeObject(5);
//					rmsOOS.writeObject(m_Models.get(i));
//				}

				for (int i = 0; i < clientSeedList.size(); i++) {
					rmsOOS.writeObject(5);
					rmsOOS.writeObject(m_Classifiers[Integer.parseInt(clientSeedList.get(i))-1]);
				}
				clientClassifierList.clear();
				clientSeedList.clear();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//---------------------------------------------------------------------------------------------------
	public class PingServer extends Thread {
//		private Socket socket;
		private ObjectOutputStream rccobjectOutputStream;
		private String message;

//		public PingServer(Socket skt, ObjectOutputStream oos, String message) {
//			this.socket = skt;
		public PingServer(ObjectOutputStream oos, String message) {
			this.rccobjectOutputStream = oos;
			this.message = message;
		}

		@Override
		public void run() {
			try {
//				System.out.println("-------------------------------------------------- PingServer.run");
				rccobjectOutputStream.writeObject(8);
				rccobjectOutputStream.writeObject(message);
//				System.out.println("-------------------------------------------------- SENT");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	//---------------------------------------------------------------------------------------------------
	public class PingClients extends Thread {
		private ObjectOutputStream pcObjectOutputStream;
		private String message;
		private PhoneConnection pc;

		public PingClients(PhoneConnection pc, String message) {
			this.pc = pc;
//			this.pcObjectOutputStream = pc.pcOOS;
			this.message = message;
		}

		@Override
		public void run() {
			try {
//				System.out.println("-------------------------------------------------- PingClients.run");
//				pcObjectOutputStream.writeObject(8);
//				pcObjectOutputStream.writeObject(message);
				pc.pcOOS.writeObject(9);
				pc.pcOOS.writeObject(message);
				String name = pc.pcSocket.getInetAddress().getHostName();
				statusUpdate(message +" > "+name);
//				System.out.println("-------------------------------------------------- SENT");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
//---------------------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------------------

	public void startNextLocalTask() {

		int dloop = 0;
		int count = treeSeedCount.get();
		if (count < numIterations) {
			if (count + serverCpuCoreCount > numIterations) {
				dloop = numIterations - count;
			} else {
				dloop = serverCpuCoreCount;
			}
			count = treeSeedCount.addAndGet(dloop);
			serverThreads = 0;
			for (int i = 0; i < dloop; i++) {
				ServerModelBuild smb = new ServerModelBuild(count-dloop+i);
				smb.start();
				serverThreads++;
				buildThreadList.add(smb);
			}
		}
	}

	public class ServerModelBuild extends Thread {
		private int modelNumber;

		public ServerModelBuild(int modelNumber) {
			this.modelNumber = modelNumber;
		}

		@Override
		public void run() {
			Classifier bag;
			Classifier classifier = m_Classifiers[modelNumber];
			int baseSeed = 0;
			Random r;
			Instances trainBag;
			if (nameClassifier.equals("RandomForest")) {
				bag = new RandomForest();
				((RandomForest)bag).setRepresentCopiesUsingWeights(true);
				((RandomForest)bag).setClassifier(classifier);
				baseSeed = ((RandomForest)bag).getSeed();
				r = new Random(baseSeed + modelNumber);
				trainBag = traindata.resampleWithWeights(r, null, ((RandomForest)bag).getRepresentCopiesUsingWeights(),
						((RandomForest)bag).getBagSizePercent());
				((RandomTree)classifier).setDoNotCheckCapabilities(true);
			} else {
				bag = new FastForest();
				((FastForest)bag).setRepresentCopiesUsingWeights(true);
				((FastForest)bag).setClassifier(classifier);
				baseSeed = ((FastForest)bag).getSeed();
				r = new Random(baseSeed + modelNumber);
				Instances tempData = new Instances(traindata);
				tempData.randomize(r);
				trainBag = new Instances(tempData, 0, (int)(0.5 *traindata.numInstances()) );
				((FastForestRT)classifier).setDoNotCheckCapabilities(true);
			}
//			Instances trainBag = traindata;
//			System.out.println(traindata.toSummaryString());
//			if (nameClassifier.equals("FastForest")) classifier = new FastForestRT();
			try {
//				classifier.buildClassifier(trainBag);
//				semaphore.acquire();
//					m_Classifiers[numModelsReceived] = classifier;
					m_Classifiers[modelNumber].buildClassifier(trainBag);
					numModelsReceived++;
					buildThreadChecker();
//				semaphore.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void buildThreadChecker() {
		statusUpdate("\nModels Received: "+(numModelsReceived)+" (SERVER)");
		if (serverThreads > 0) serverThreads--;
		if (numModelsReceived == numIterations) {
			DecimalFormat df5 = new DecimalFormat("#.#####");
			timeTestFinish = System.nanoTime();
			statusUpdate("\nEnsemble completed.");
			statusUpdate("\n >>> ENSEMBLE BUILD TIME: " + df5.format((timeTestFinish - timeTestStart) / 1000000000.0) + "s");
			MainActivity.modelLoaded = true;
		} else if (serverThreads == 0) {
			buildThreadList.clear();
			startNextLocalTask();
		}
//		System.out.println("---------------------------------------------------------------");
//		System.out.println("numModelsReceived == numIterations: "+(numModelsReceived == numIterations) + "("+numModelsReceived+")");
//		System.out.println("serverThreads == 0: "+(serverThreads == 0) + "("+serverThreads+")");
	}
//---------------------------------------------------------------------------------------------------

}

