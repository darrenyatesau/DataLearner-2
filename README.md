# DataLearner 2 - Data Mining and Knowledge Discovery on Android

DataLearner 2 is the second release of an open-source easy-to-use tool for data mining and knowledge discovery from your own compatible training datasets. It’s fully self-contained, requires no external storage or network connectivity – it builds machine-learning models directly on your phone or tablet.

--- NEW FEATURES ---
* DataWalker AR data viewer - use your ARCore-ready phone to walk through your training dataset using augmented reality.
* multi-core CPU support for faster modelling.
* distributed data-mining using multiple Android devices over a closed-network (experimental).
* Models are now Weka 3.8-compatible - learn on Android, test on Weka.
* Meta-parameter setting for all machine-learning algorithms. 

DataLearner 2 features classification, association and clustering algorithms from the open-source Weka (Waikato Environment for Knowledge Analysis) package, plus new algorithms developed by the Data Science Research Unit (DSRU) at Charles Sturt University. Combined, the app provides over 40 machine-learning/data-mining algorithms, including RandomForest, C4.5 (J48) and NaiveBayes.

<H3>CSV file support</H3>
To use CSV files in DataLearner 2, the file MUST include a header row.

<H3>Privacy Policy</H3>
DataLearner 2's privacy policy is pretty simple:
<br>1. DataLearner does not collect any user data. It models data in the training datasets you provide in ARFF or CSV files (otherwise, it doesn't do anything).
<br>2. DataLearner does not save any usage data, including any data regarding any training dataset you load for data-mining. Any training data remaining in RAM after use can be erased by just rebooting your phone. DataLearner 2 does allow you to save models that represent your data as Weka-compatible .model files - what you do with them after that is up to you.
<br>(PLEASE NOTE: Google Play collects general app download and usage analytics about DataLearner 2, which we use to correct bugs and issues, but this is no different to any other app on Google Play. We have no control over this.)
<br>By installing and using the app, you agree to this policy. Cheers!

<H3>Android Permissions</H3>
DataLearner 2 requires a number of permissions in order to perform its tasks. These include:
<br>* CAMERA - DataLearner 2 requires your camera to deliver the ARCore-based augmented reality data viewer we call 'DataWalker'. Without the camera (and ARCore support), this feature doesn't happen.
<br>* WIFI - DataLearner 2 incorporates a new experimental feature for combining multiple Android devices into a closed network where data mining can be distributed over multiple devices. WiFi is the connection framework we use for this. Again, no WiFi, no distributed data mining.
<br>* STORAGE - For DataLearner 2 to access and build models from your training data, as well as be able to save those models, it needs access to your device storage. Again, without storage access, why are we even here?

<H3>Where DataLearner is being used</H3>

DataLearner was designed as a research project for my PhD during 2018-2021 to see just how far we could push smartphone technology in providing self-contained data mining/machine-learning without relying on external server/network-based hardware. DataLearner is being used as a teaching tool in the ITC573 Data and Knowledge Engineering subject for the Master of Information Technology post-graduate degree at Charles Sturt University. It was also presented at ADMA 2019 (15th International Conference on Advanced Data Mining and Applications) and published in 'Lecture Notes in Artificial Intelligence' (Springer).

DISCLAIMER: While this software has been tested, it is open-source and supplied AS-IS. No warranty is implied or given and no fitness for any particular application is to be inferred. Your use of this software implies you agree to these terms.

Cheers,
Darren.
