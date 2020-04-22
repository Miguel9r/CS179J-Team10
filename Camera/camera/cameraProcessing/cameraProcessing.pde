import processing.serial.*;

Serial mySerial;

PImage photo;
byte rawBytes[];
 
void setup(){
  size(800,800);
  mySerial = new Serial(this, "COM3", 250000); //change COM to appropriate COM
  mySerial.bufferUntil('\n');
  //output = createOutput("test.jpg");
  photo = createImage(320, 240, RGB);
  photo.loadPixels();
  mySerial.write(0x10);
}

void draw(){
  background(255,255,255);
  image(photo, 0, 0);
}

void keyReleased(){
  switch(key) {
 
    case 's':
      mySerial.write(0x10);
      println("Starting Capture");
      break;
    
    case 'c':
      mySerial.write(7);
      break;
    
    default:
      println("Unknown Command: "+key);
      break;
  }
  
}

void serialEvent(Serial mySerial) {
  while (mySerial.available() > 0) {
    String incoming[];
    String myString = mySerial.readStringUntil('\n');
    myString = trim(myString);
    incoming = split(myString, ',');
    if (incoming.length > 1) {
      if (incoming[0].equals("FifoLength:")) {
        //initialize raw data byte array to the size of the picture
        rawBytes = new byte[int(incoming[1])];
      } 
      else if (incoming[0].equals("Image:")) {
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
          String fname = "testPic.jpg";
          saveBytes(fname, rawBytes);
          // Open saved picture for local display
          photo = loadImage(fname);
        }
        catch(RuntimeException e) {
          println(e.getMessage());
        }
      }
      else if (incoming[0].equals("Ready:")) {
        mySerial.write(0x10);
        println("Starting Capture");
      }
    } 
    else {
      println(myString);
    }
  }
}
