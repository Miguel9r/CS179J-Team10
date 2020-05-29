import processing.serial.*;       // import the Processing serial library
import java.util.Random; 
import java.util.concurrent.TimeUnit; // Numbering Photos
 
 
Serial mySerial;                    // The serial port
 
 
 
//camera declarations 
byte rawBytes[];
int sensorNum = 0;
PImage img;  
long picNum = 0;

boolean isCapture = false;
boolean isStream = true;

//linear Actuator declarations
String laString = "- Linear actuator: t = extend, g = retract";

//pantilt declarations
String pantiltString = "- Pan tilt: i = up, k = down, j = left, l = right";

//chasis declarations
String chasisString = "- Chasis: w = forward, s = backward, a = left, d = right";
 
 
void setup() {
 
  size(640, 480);
 
  mySerial = new Serial(this, Serial.list()[0], 250000);
 
  // read bytes into a buffer until you get a linefeed (ASCII 10):
  mySerial.bufferUntil('\n');

  // draw with smooth edges:
 
  smooth();
  textSize(10);
  img = createImage(320, 240, RGB);
 
  img.loadPixels();
 
  mySerial.write(0x10);
}
 
void draw() {
 
  background(255);
 
  cameraDraw();
  laDraw();
  pantiltDraw();
  chasisDraw();
}

void cameraDraw(){
  image(img, 0, 0, width, height-100);
 
  fill(0);
  delay(100);
  
  if (isStream == true)
  {
  mySerial.write(0x10); //livestreams
  isStream = false;
  
  }
 

  if(isCapture == true)
  {
    text("- Press Y to save capture or N to continue camera stream ", 30, height-10);
  }
  else
  {
    text("- Press key C to capture", 30, height-10);
  }
}

void laDraw(){
  text(laString,30,height-25);
}

void pantiltDraw(){
 text(pantiltString, 30, height-40); 
}

void chasisDraw(){
 text(chasisString, 30, height-55); 
}
 
 
void keyPressed() {
   laKP();
   pantiltKP();
   chasisKP();
}

void keyReleased(){
  cameraKR();
  laKR();
  pantiltKR();
  chasisKR();
}

void cameraKR(){
  switch(key) {
 
  case 's':
    mySerial.write(0x10);
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
      mySerial.write(0x10);
      isCapture = false;
    }
    break;
  
  case 'n':
    if(isCapture == true){
      mySerial.write(0x10);
      isCapture = false;
    }
    break;
 
  default:
    break;
  }
}

void laKP(){
  switch(key) {
 
    case 'g':
      mySerial.write(0x08);
      println("Sending retract signal");
      break;

    case 't':
      mySerial.write(0x07);
      println("sending extend signal");
      break;

    default:
      break;
  }
}

void laKR(){
  switch(key) {
 
    case 'g':
      mySerial.write(0x13);
      println("Finish retract signal");
      break;

    case 't':
      mySerial.write(0x13);
      println("Finish extend signal");
      break;

    default:
      break;
  }
}

void pantiltKP(){
  switch(key) {
 
    case 'i':
      mySerial.write(0x03);
      println("Sending up signal");
      break;

    case 'k':
      mySerial.write(0x04);
      println("sending down signal");
      break;
    
    case 'l':
      mySerial.write(0x06);
      println("sending right signal");
      break;
    
    case 'j':
      mySerial.write(0x05);
      println("sending left signal");
      break;

    default:
      break;
  }
}

void pantiltKR(){
 switch(key) {
 
    case 'i':
      mySerial.write(0x35);
      println("finished signal");
      break;

    case 'k':
      mySerial.write(0x35);
      println("finished signal");
      break;
    
    case 'l':
      mySerial.write(0x35);
      println("finished signal");
      break;
    
    case 'j':
      mySerial.write(0x35);
      println("finished signal");
      break;

    default:
      break;
  } 
}

void chasisKP(){
  switch(key) {
 
    case 'w':
      mySerial.write(0x09);
      println("Farther Back signal");
      break;

    case 's':
      mySerial.write(0x0A);
      println("Come Closer signal");
      break;
    
    case 'd':
      mySerial.write(0x0C);
      println("sending right signal");
      break;
    
    case 'a':
      mySerial.write(0x0B);
      println("sending left signal");
      break;

    default:
      break;
  }
}

void chasisKR(){
  switch(key) {
 
    case 'w':
      mySerial.write(0x25);
      println("finished signal");
      break;

    case 's':
      mySerial.write(0x25);
      println("finished signal");
      break;
    
    case 'a':
      mySerial.write(0x25);
      println("finished signal");
      break;
    
    case 'd':
      mySerial.write(0x25);
      println("finished signal");
      break;

    default:
      break;
  }
}

void serialEvent(Serial mySerial) {
 //camera serial
  while (mySerial.available() > 0) {
 
    String incoming[];
 
    String myString = mySerial.readStringUntil('\n');
 
 
 
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
 
        mySerial.write(0x10);
 
        println("Starting Capture");
      }
    } else {
 
      println(myString);
    }
  }
}
