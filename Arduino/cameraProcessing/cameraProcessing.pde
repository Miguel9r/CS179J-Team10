import processing.serial.*;       // import the Processing serial library
import java.util.Random; 
import java.util.concurrent.TimeUnit; // Numbering Photos
 
 
Serial myPort;                    // The serial port
 
 
 
//NEW ADDITIONS
 
byte rawBytes[];

int sensorNum = 0;
 
PImage img;  
 
long picNum = 0;

boolean isCapture = false;
boolean isStream = true;


 
 
void setup() {
 
  size(640, 480);
 
 
 
  // List all the available serial ports
 
  // if using Processing 2.1 or later, use Serial.printArray()
 
  printArray(Serial.list());
 
 
 
 
 
  // Change the 0 to the appropriate number of the serial port
 
  // that your microcontroller is attached to.
 
  myPort = new Serial(this, Serial.list()[0], 250000);
 
 
 
  // read bytes into a buffer until you get a linefeed (ASCII 10):
 
  myPort.bufferUntil('\n');
 
 
 
  // draw with smooth edges:
 
  smooth();
 
 
 
  img = createImage(320, 240, RGB);
 
  img.loadPixels();
 
  //frameRate(600);
 
  myPort.write(0x10);
}
 
 
 
 
 
void draw() {
 
  
 
  background(255);
 
  image(img, 0, 0, width, height-100);
 
  fill(0);
  delay(100);
  
  if (isStream == true)
  {
  myPort.write(0x10); //livestreams
  isStream = false;
  
  }
 

  if(isCapture == true)
  {
         text("- Press Y to save capture or N to continue camera stream ", 30, height-20);
  }
  else
  {
    text("- Press key C to capture", 30, height-20);
  }
  
}
 
 
 
 
 
// serialEvent  method is run automatically by the Processing applet
 
// whenever the buffer reaches the  byte value set in the bufferUntil()
 
// method in the setup():
 
 
 
void serialEvent(Serial myPort) {
 
  while (myPort.available() > 0) {
 
 
 
    String incoming[];
 
    String myString = myPort.readStringUntil('\n');
 
 
 
    myString = trim(myString);
 
    incoming = split(myString, ',');
 
 
 
    if (incoming.length > 1) {
 
      if (incoming[0].equals("FifoLength:")) {
 
        //initialize raw data byte array to the size of the picture
 
        rawBytes = new byte[int(incoming[1])];
 
        println("Picture Size: "+incoming[1]+" bytes");
      } else if (incoming[0].equals("Image:")) {
 
        int x = 0;
 
        for (int i = 1; i < incoming.length; i++) {
 
          try {
 
            //add raw jpeg incoming bytes to byte array
 
            rawBytes[x]= (byte)int(incoming[i]);
 
            x++;

          }
 
          catch(RuntimeException e) {
 
            println(e.getMessage());
          }
        }
 
        try {
 
          //Save raw data to file
 
          String fname = "temp"+".jpg";
 
          saveBytes("data/"+fname, rawBytes);
 
 
 
          // Open saved picture for local display
 
          img = loadImage("data/"+fname);
 
          picNum++;
          if(isCapture == false)
            {
              isStream = true;
            }
          
        }
 
        catch(RuntimeException e) {
 
          println(e.getMessage());
        }
      } else if (incoming[0].equals("Ready:")) {
 
        myPort.write(0x10);
 
        println("Starting Capture");
      }
    } else {
 
      println(myString);
    }
  }
}
 
 
 
void keyPressed() {
 
  switch(key) {
 
  case 's':
 
    myPort.write(0x10);
 
    println("Starting Capture");
 
    break;
 
 
 
  case 'c':
    
    isCapture = true;

 
    break;

  case 'y':
    if(isCapture == true){
      long time = System.currentTimeMillis();
      String name = Long.toString(time) + ".jpg";
      saveBytes("data/capture/"+name, rawBytes);
      myPort.write(0x10);
      isCapture = false;
    }
    break;
  
  case 'n':
    if(isCapture == true){
      myPort.write(0x10);
      isCapture = false;
    }
    break;
 
  default:
 
    println("Unknown Command: "+key);
 
    break;
  }
}
