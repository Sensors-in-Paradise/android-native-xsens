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

## Architecture overview

TODO (give an overview where in the code what is done)

## External sources used

[Xsens DOT Manual](https://www.xsens.com/hubfs/Downloads/Manuals/Xsens%20DOT%20User%20Manual.pdf)
[WebDAV Client on Github](https://github.com/thegrizzlylabs/sardine-android)
[Charts Library on Github](https://github.com/PhilJay/MPAndroidChart)

TODO (link more Documents or Repos)
