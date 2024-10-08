# qrcode
A flutter plugin for scanning QR codes. Use AVCaptureSession in iOS and zxing in Android.

## Usage

### Use this package as a library

#### Add dependency

Add this to your package's pubspec.yaml file:

```dart
dependencies:
  qrcode: ^1.0.5
```

#### Install it

You can install packages from the command line:

with Flutter v3.3.10

```
$ flutter pub get
```

#### Import it

Now in your Dart code, you can use:

```dart
import 'package:qrcode/qrcode.dart';
```

### Basic

```dart
class _MyAppState extends State<MyApp> {
  QRCaptureController _captureController = QRCaptureController();

  @override
  void initState() {
    super.initState();

    _captureController.onCapture((data) {
      print('onCapture----$data');
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Stack(
          alignment: Alignment.center,
          children: <Widget>[
            QRCaptureView(controller: _captureController),
            Align(
              alignment: Alignment.bottomCenter,
              child: _buildToolBar(),
            )
          ],
        ),
      ),
    );
  }

  Widget _buildToolBar() {
    return Row(
          mainAxisSize: MainAxisSize.max,
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            FlatButton(
              onPressed: () {
                _captureController.pause();
              },
              child: Text('pause'),
            ),
            FlatButton(
              onPressed: () {
                _captureController.resume();
              },
              child: Text('resume'),
            ),
          ],
        );
  }
}
```

## Integration

### iOS
To use on iOS, you must add the following to your Info.plist


```
<key>NSCameraUsageDescription</key>
<string>Camera permission is required for qrcode scanning.</string>
<key>io.flutter.embedded_views_preview</key>
<true/>
```
