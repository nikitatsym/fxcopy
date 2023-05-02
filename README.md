## CopyApp
CopyApp is a GUI application that uses Java NIO FileChannels to efficiently copy large files.

## Paths
CopyApp retrieves the source and target file paths from the config.properties file and verifies their correctness during initialization. If you modify a path in the properties file, please restart the application.

## Run
To run the application, you can use the entry point in the `com.example.fxcopy.CopyApp` class or `mvn javafx:run`.

## Errors
CopyApp displays errors related to the source and target paths during the initialization stage in the GUI. Other errors will be displayed in the console.