import processing.serial.*;

Serial mySerial;

byte rawBytes[];
 String s;
void setup(){
  size(500,500);
  mySerial = new Serial(this, "COM7", 9600); //change COM to appropriate COM
  textSize(30);
  s = "Press a for retract\nPress s for extend";
}

void draw(){
  background(0,0,0);
  text(s,30,30);
}

void keyPressed(){
  switch(key) {
 
    case 'a':
      mySerial.write(0x01);
      println("Sending retract signal");
      break;

    case 's':
      mySerial.write(0x02);
      println("sending extend signal");
      break;

    default:
      println("Unknown Command: "+key);
      break;
  }
  //delay(150);
}

void keyReleased(){
  switch(key) {
 
    case 'a':
      mySerial.write(0x00);
      println("Finish retract signal");
      break;

    case 's':
      mySerial.write(0x00);
      println("Finish extend signal");
      break;

    default:
      println("Unknown Command: "+key);
      break;
  }
}