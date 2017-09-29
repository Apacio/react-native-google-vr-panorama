# react-native-google-vr-panorama
React Native component for the Google VR Panorama Widget

**Note: Required Android 4.4 or higher and at least Android SDK Version 23**

## Features

1. Supports `mono` and `stereo` images
1. Image loading from the internet

## Installation

1. NPM install

```
npm install --save react-native-google-vr-panorama
```

1. Add the following to your `settings.gradle` file located in the `android` folder:

```
include ':react-native-google-vr-panorama'
project(':react-native-google-vr-panorama').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-google-vr-panorama/android')
```




1. Update your project dependencies in `build.gradle` under `app` folder to include the required dependencies:

```
...

dependencies {
    ...
    compile 'com.google.vr:sdk-panowidget:1.10.0'
    ...
}
```

1. Add the package to the `getPackages` method in the `MainApplication.java` file:

```
import com.xebia.googlevrpanorama.RNGoogleVRPanoramaPackage;

@Override
protected List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
        new MainReactPackage(),
        new RNGoogleVRPanoramaPackage()
    );
}
```

## Usage

Import the package and view:

```
import GoogleVRPanorama, { PanoramaView } from 'react-native-google-vr-panorama'
```

Render the view:

```
render() {
    // load local files with file:///storage/...
    const imageUrl = 'http://www.google.com/image.jpg'
    const inputType = GoogleVRPanorama.inputType.stereo

    return (
        <PanoramaView imageUrl={imageUrl} inputType={inputType} />
    )
}
```

## Example

An example has been provided in [`example/index.android.js`](https://github.com/XebiaStudio/react-native-google-vr-panorama/blob/master/example/index.android.js)

### Image dimensions

Due to the constrained system resources that mobile devices have, loading large images as a `Bitmap` can cause out of memory errors.

To prevent OOM errors, supply `dimensions` to the component. When you specify dimensions the component will automatically calculate the sample size and reduce the `Bitmap` size to the exact dimensions.

If not specified, the original image size will be loaded.

## Props

| Prop | Type | Required | Description | Default |
|---|---|---|---|---|
| `imageUrl` | `string` | Required | The URL of the image that the component should display | N/A |
| `dimensions` | `object` | Optional | The dimensions of the image | `{ width: 0, height: 0 }` |
| `inputType` | `number` | Optional | The input type for the image. One of `GoogleVRPanorama.inputType.mono`,
| `showSterio` | `boolean` | Optional | If true the widget will display the google cadboard icon | false |
| `showInfo` | `boolean` | Optional | If true the widget will display the google info icon | false |
| `showFullScreen` | `boolean` | Optional | If true the widget will display the full screen icon | false | `GoogleVRPanorama.inputType.stereo` | `GoogleVRPanorama.inputType.mono` |

## Callback methods

| Prop | Properties | Description |
|---|---|---|---|---|
| `onImageLoaded` | `undefined` | Fired when the image has successfully loaded |
| `onImageLoadingFailed` | `undefined` | Fired when the image failed to load |
