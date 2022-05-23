[![linter](https://github.com/Sensors-in-Paradise/android-native-xsens/actions/workflows/linter.yml/badge.svg)](https://github.com/Sensors-in-Paradise/android-native-xsens/actions/workflows/linter.yml)
[![unit-test](https://github.com/Sensors-in-Paradise/android-native-xsens/actions/workflows/unit-test.yml/badge.svg)](https://github.com/Sensors-in-Paradise/android-native-xsens/actions/workflows/unit-test.yml)
[![instrumented-test](https://github.com/Sensors-in-Paradise/android-native-xsens/actions/workflows/instrumented-test.yml/badge.svg)](https://github.com/Sensors-in-Paradise/android-native-xsens/actions/workflows/instrumented-test.yml)

# SONAR - A Generic HAR App

This is a tool for recording data through Xsens DOT sensors and labeling it when saving on the Device. It is designed to be very generic so that changing the sensors is possible while keeping most of the existing code and functionality. 

## Available functionalities

- Connection and synchronisation for Xsens DOT sensors
- Labeling with one or multiple labels per recording
- On-Device prediction after adding a model
- Ability to use multiple use cases on one device
- Chance to display current sensor rotation and received data
- Additional camera recording
- Pose estimation from camera to store anonymous visual data
- Relabeling during and after recording
- Simple Connection to any WebDAV Cloud to make storing data easy
- Data visualisation on device
- On-Device training

## How to use the app

TODO (quick guide to unclear operations within the app?)

The Connection Screen works well with Xsens DOT Sensors. For connected Sensors you can make them flash their LED to identify them making a long press on their item.

The Recording Screen is used for making recordings, labeling and storing them. You can add and select subjects as well as the performed action to record. There can be only one subject per recording and the one currently selected at the end of the recording will be saved. For Activities however there can be multiple ones. When you open the Dialog a timestamp will be saved instantly with the label you select afterwards. This way you can take your time to select the new label and the timestamp will not change. If you want to discard this timestamp (because the activity did not change) you can close the dialog.
If you did not select a Label and the app crashes/ is stopped by android then the null - activity label and unknown subject will be chosen as a default value.
During an recording you can use the activities tab to change labels and see the label history of this recording. For Changing Timestamps together with Labels you should edit the recording afterwards using the video recording for instance. Please note that we do not record audio together with video data.

On the Data Screen "Filter for Training" hides the amount of collected data that has already been used for training.

On the Prediction Screen you can see the confidence of the model for the highest predictions at the top. Below a history will be visible for the predicted activities. If one prediction stays the same then the space for it widens and the confidence for that label will be displayed as the average.

Not all Functions are activated by default and can be added in the settings menu at the top right.

## Architecture overview

TODO (give an overview where in the code what is done e.g. packages)

## External sources used

[Xsens DOT Manual](https://www.xsens.com/hubfs/Downloads/Manuals/Xsens%20DOT%20User%20Manual.pdf)
[WebDAV Client on Github](https://github.com/thegrizzlylabs/sardine-android)
[Charts Library on Github](https://github.com/PhilJay/MPAndroidChart)
