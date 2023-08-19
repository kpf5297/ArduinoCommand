import com.fazecast.jSerialComm.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.Timer;
import java.util.TimerTask;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class ArduinoCommand extends Application {

    private SerialPort serialPort;
    private SerialPort selectedPort;
    private boolean readingData = false;

    private TextArea dataTextArea;
    private TextField sendDataField; 
    private Button sendButton;

    private Label portLabel;
    private ComboBox<SerialPort> portComboBox;
    private Button connectButton;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // Create the layout stucture for the top menu
        BorderPane root = new BorderPane();

        // Create and set up the menu bar
        MenuBar menuBar = new MenuBar();
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());
        root.setTop(menuBar);

        // File menu - new, save, print, and exit
        Menu fileMenu = new Menu("Menu");
        MenuItem exitMenuItem = new MenuItem("Exit");
        
        // Set the action for the exit menu item
        exitMenuItem.setOnAction(actionEvent -> Platform.exit());
   
        // Add menu items to the menu bar
        menuBar.getMenus().addAll(fileMenu);

        // Add menu items to the file menu
        fileMenu.getItems().addAll(exitMenuItem);

        // Create UI elements for COM port selection
        Region spacer1 = new Region();
        portLabel = new Label("Select Port:");
        portComboBox = new ComboBox<>();
        connectButton = new Button("Connect/Disconnect");
        
        // Create UI elements for status text display
        Region spacer2 = new Region();
        Label dataTextLabel = new Label("Status Text:");
        dataTextArea = new TextArea();
        dataTextArea.setMaxWidth(200);

        VBox infoBox = new VBox(spacer1, portLabel, portComboBox, connectButton, spacer2,dataTextLabel, dataTextArea);
        infoBox.setPadding(new Insets(10));
        infoBox.setSpacing(10);
        
        Region spacer3 = new Region();
        //spacer2.setMinHeight(50);
        Button clockwiseSlow = new Button("Clockwise Slow");
        Button clockwiseFast = new Button("Clockwise Fast");
        Button counterClockwiseSlow = new Button("Counter Clockwise Slow");
        Button counterClockwiseFast = new Button("Counter Clockwise Fast");
        
        Region spacer4 = new Region();
        //spacer3.setMinHeight(50);
        Button sine = new Button("Sine Mode");
       
        
        counterClockwiseFast.setOnAction(event -> {
            String command = String.format("M%dS%dV%d\n", 10, -2000, 1000);
            
            if (serialPort.isOpen()) {
                
                serialPort.writeBytes(command.getBytes(),command.length());
                    sendDataField.clear();
            }
        });
        
        counterClockwiseSlow.setOnAction(event -> {
            String command = String.format("M%dS%dV%d\n", 10, -2000, 100);
            
            if (serialPort.isOpen()) {
                
                serialPort.writeBytes(command.getBytes(),command.length());
                    sendDataField.clear();
            }
        });
        
        clockwiseFast.setOnAction(event -> {
            String command = String.format("M%dS%dV%d\n", 10, 2000, 1000);
            
            if (serialPort.isOpen()) {
                
                serialPort.writeBytes(command.getBytes(),command.length());
                    sendDataField.clear();
            }
        });
        
        clockwiseSlow.setOnAction(event -> {
            String command = String.format("M%dS%dV%d\n", 10, 2000, 100);
            
            if (serialPort.isOpen()) {
                
                serialPort.writeBytes(command.getBytes(),command.length());
                    sendDataField.clear();
            }
        });
        
        sine.setOnAction(event -> {
            String command = String.format("M%dS%dV%d\n", 20, 1000, 1000);
            
            if (serialPort.isOpen()) {
                
                serialPort.writeBytes(command.getBytes(),command.length());
                    sendDataField.clear();
            }
        });

        VBox buttonBox = new VBox(spacer3,counterClockwiseFast, counterClockwiseSlow, clockwiseSlow, clockwiseFast, spacer4,sine);
        buttonBox.setPadding(new Insets(10));
        buttonBox.setSpacing(10);
        
        // Populate the port ComboBox with available serial ports
        SerialPort[] ports = SerialPort.getCommPorts();
        portComboBox.getItems().addAll(ports);

        // Set up the "Connect" button action
        connectButton.setOnAction(event -> {
            selectedPort = portComboBox.getValue();
            if (selectedPort != null) {
                toggleReading();
            }
        });
        
        // Set up the scene and stage
        root.setTop(menuBar);
        root.setLeft(infoBox);
        root.setRight(buttonBox);

        Scene scene = new Scene(root, 500, 400);

        primaryStage.setTitle("ArduinoGUI v0.10");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Toggle reading data from the serial port
    private void toggleReading() {
        if (!readingData) {
            startReading();
        } else {
            stopReading();
        }
    }

    // Start reading data from the serial port
    private void startReading() {
        readingData = true;
        serialPort = selectedPort;

        if (serialPort.openPort()) {
            // Configure serial port parameters
            serialPort.setComPortParameters(9600, 8, 
                    SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);

            // Set up a timer to read data periodically
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (serialPort.bytesAvailable() > 0) {
                        byte[] newData = new byte[serialPort.bytesAvailable()];
                        int numBytes = serialPort.readBytes(newData, newData.length);
                        String receivedData = new String(newData, 0, numBytes);
                        // Update the UI thread with received data
                        Platform.runLater(() -> dataTextArea.appendText(receivedData));
                    }
                }
            }, 0, 1000); // Check every 1 second
        } else {
            System.out.println("Failed to open the COM port.");
        }
    }

    // Stop reading data from the serial port
    private void stopReading() {
        readingData = false;
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
    }

    @Override
    public void stop() {
        stopReading();
    }
}

/*


 * **********************
 *  Arduino Test Code   *
 * **********************
 
#include <AccelStepper.h>

// Initialize the stepper library on pins 2 through 5:
AccelStepper myStepper(AccelStepper::HALF4WIRE, 2, 3, 4, 5);

void setup() {
  // Set the max speed
  myStepper.setMaxSpeed(1000);
  myStepper.setAcceleration(100);
  myStepper.setSpeed(200);

  // Initialize the serial port:
  Serial.begin(9600);
}

void loop() {
  if (Serial.available() > 0) {
    String command = Serial.readStringUntil('\n');
    int mode, steps, speed;
    sscanf(command.c_str(), "M%dS%dV%d", &mode, &steps, &speed);

    Serial.print("Mode: ");
    Serial.println(mode);

    Serial.print("Steps: ");
    Serial.println(steps);

    Serial.print("Speed: ");
    Serial.println(speed);

    switch (mode) {
      case 10:
        Serial.println("Relative Move Mode");
        myStepper.setMaxSpeed(speed);
        myStepper.move(steps);
        break;
      case 20:
        Serial.println("Sine Mode");
        while(true) {
          myStepper.setMaxSpeed(speed);
          myStepper.move(steps);
          while (myStepper.distanceToGo() != 0) {
          myStepper.run();
          }
          myStepper.move(-steps);
          while (myStepper.distanceToGo() != 0) {
          myStepper.run();
          }
        }
        break;
      default:
        Serial.println("Unknown Mode");
    }
  }

  myStepper.run();
}



 */