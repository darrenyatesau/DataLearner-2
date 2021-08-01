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
	WifiDirectBroadcastReceiver.java
    (C) Copyright Darren Yates 2018-2021
	Developed using a combination of Weka 3.8.5 and algorithms developed by Charles Sturt University
	DataLearner is licensed GPLv3.0, source code is available on GitHub
	Weka 3.8.5 is licensed GPLv3.0, source code is available on GitHub
*/

package au.com.darrenyates.datalearner2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
	private WifiP2pManager mManager;
	private WifiP2pManager.Channel mChannel;
	private NetFragment13 fragment;
	
	public WifiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, NetFragment13 fragment) {
		this.mManager = mManager;
		this.mChannel = mChannel;
		this.fragment = fragment;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
//			if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) Toast.makeText(context, "Wifi is ON",Toast.LENGTH_SHORT).show();
//			else Toast.makeText(context, "Wifi is OFF", Toast.LENGTH_SHORT).show();
			
		} else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
			System.out.println(">>>> WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION");
			if (mManager!=null) mManager.requestPeers(mChannel, fragment.peerListListener);
		
		} else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
			if (mManager == null) return;
			NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
//			System.out.println(">>>> WIFI_P2P_CONNECTION_CHANGED_ACTION");
			if (networkInfo.isConnected()) 	mManager.requestConnectionInfo(mChannel, fragment.connectionInfoListener);
//			 else Toast.makeText(context, "Device DISCONNECTED.", Toast.LENGTH_LONG).show();

		} else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//			Toast.makeText(context, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION", Toast.LENGTH_LONG).show();
		
		}
	}
}
