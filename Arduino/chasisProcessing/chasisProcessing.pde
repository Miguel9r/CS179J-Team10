import processing.serial.*;

Serial mySerial;

byte rawBytes[];
 String s;
void setup(){
  size(500,500);
  mySerial = new Serial(this, Serial.list()[0], 28800); //change COM to appropriate COM
  textSize(30);
  s = "Press w for forward\nPress s for backward\nPress a for left\nPress d for right\n";
}

void draw(){
  background(0,0,0);
  text(s,30,30);
}

void keyPressed(){
  switch(key) {
 
    case 'w':
      mySerial.write(0x21);
      println("Sending forward signal");
      break;

    case 's':
      mySerial.write(0x22);
      println("sending backward signal");
      break;
    
    case 'd':
      mySerial.write(0x23);
      println("sending right signal");
      break;
    
    case 'a':
      mySerial.write(0x24);
      println("sending left signal");
      break;

    default:
      println("Unknown Command: "+key);
      break;
  }
  //delay(150);
}

void keyReleased(){
  switch(key) {
 
    case 'w':
      mySerial.write(0x00);
      println("finished signal");
      break;

    case 's':
      mySerial.write(0x00);
      println("finished signal");
      break;
    
    case 'a':
      mySerial.write(0x00);
      println("finished signal");
      break;
    
    case 'd':
      mySerial.write(0x00);
      println("finished signal");
      break;

    default:
      println("Unknown Command: "+key);
      break;
  }
}
