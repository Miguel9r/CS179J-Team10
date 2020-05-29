//Collaborated Code with Kathleen & Bailey
//Code Merged with Camera and Linear Actuator
#include <Servo.h>
#include <Wire.h>
#include <ArduCAM.h>
#include <SPI.h>
#include "memorysaver.h"

//This code can only work on OV5642_MINI_5MP_PLUS platform.
#if !(defined (OV2640_MINI_2MP))
#endif
#define   FRAMES_NUM    0x00

// set pin 4 as the slave select for SPI:
//camera declarations
const int CS1 = 7;
bool CAM1_EXIST = false;
bool stopMotion = false;
ArduCAM myCAM1(OV2640, CS1);
long int streamStartTime;

//linear actuator declarations
Servo actuator;
const int pwmPin = 10; //change to pwm pin
int pwm = 1000;

//chasis declarations
const int A1A = 3;
const int A1B = 2;
const int B1A = 4;
const int B1B = 5;

//pantilt declarations
Servo servo1; // first servo will control left/right movement
Servo servo2; // second servo will control up/down movement

const int pantiltLR = 9;//@Kathleen may need to change
const int pantiltUD = 6;//@Kathleen may need to change
int s1_angle = 90;  // servo1 will start at 90 degrees
int s2_angle = 90;  // servo2 will start at 90 degrees
int angleStep = 5;  // each servo will only move at 5 degrees per quarter-second

//BLE declarations none

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600); //921600
  linearActuatorSetup();
  cameraSetup();
  chasisSetup();
  pantiltSetup();
}

void loop() {
// cameraLoop();
}

void serialEvent(){
  if (Serial.available() > 0) {
    uint8_t temp = 0xff;
    temp = Serial.read();
    if(temp == 0x10){
      cameraSerial(temp);
    }
    if(temp >= 0x07 && temp <= 0x08){
      linearActuatorSerial(temp);
    }
    else if(temp >= 0x09 && temp <= 0x0C){
      chasisSerial(temp);
    }
    else if(temp >= 0x03 && temp <= 0x06){
      pantiltSerial(temp);
    }
  }
}

void linearActuatorSetup(){
  actuator.attach(pwmPin);
  actuator.writeMicroseconds(1000);
  delay(2000);
}

void linearActuatorSerial(uint8_t temp){
  switch (temp){
    case 0x08:
      if(pwm > 1000){
        pwm -= 5;
      }
      Serial.println("retract received");
      break;

    case 0x07:
      if(pwm < 2000){
        pwm += 5;
      }
      Serial.println("retract received");
      break;

    default:
     // Serial.println("unrecognized");
      break;
  }
  actuator.writeMicroseconds(pwm);
}

void cameraSetup(){
  uint8_t vid, pid;
  uint8_t temp;
  Wire.begin();
  Serial.println(F("ArduCAM Start!"));
  // set the CS output:
  pinMode(CS1, OUTPUT);
  // initialize SPI:
  SPI.begin();
  //Check if the 4 ArduCAM Mini 5MP PLus Cameras' SPI bus is OK
  while (1) {
    myCAM1.write_reg(ARDUCHIP_TEST1, 0x55);
    temp = myCAM1.read_reg(ARDUCHIP_TEST1);
    if (temp != 0x55)
    {
      Serial.println(F("SPI1 interface Error!"));
    } else {
      CAM1_EXIST = true;
      Serial.println(F("SPI1 interface OK."));
    }
    if (!(CAM1_EXIST)) {
      delay(1000); continue;
    } else
      break;
  }
 
  while (1) {
    //Check if the camera module type is OV5642
    myCAM1.rdSensorReg8_8(OV2640_CHIPID_HIGH, &vid);
    myCAM1.rdSensorReg8_8(OV2640_CHIPID_LOW, &pid);
    if ((vid != 0x26 ) && (( pid != 0x41 ) || ( pid != 0x42 ))) {
      Serial.println(F("Can't find OV2640 module!"));
      delay(1000); continue;
    } else {
      Serial.println(F("OV2640 detected.")); break;
    }
  }
 
  //Change to JPEG capture mode and initialize the OV2640 module
  myCAM1.set_format(JPEG);
  myCAM1.InitCAM();
  myCAM1.clear_fifo_flag();
  myCAM1.OV2640_set_JPEG_size(OV2640_320x240); delay(1000);
 
  delay(1000);
 
  myCAM1.clear_fifo_flag();
  Serial.println("Ready:,1");
}

void chasisSetup(){
  pinMode(A1A, OUTPUT);
  pinMode(A1B, OUTPUT);
  pinMode(B1A, OUTPUT);
  pinMode(B1B, OUTPUT);
}

void pantiltSetup(){
  servo1.attach(pantiltLR);   // attaches the servo on pin 9 to the servo object: LEFT AND RIGHT
  servo2.attach(pantiltUD);   // attaches the servo on pin 10 to the servo object :UP AND DOWN
  servo1.write(s1_angle);  // send servo1 to the middle at 90 degrees
  servo2.write(s2_angle);  // send servo2 to the middle at 90 degrees
}

void cameraLoop(){
  if (CAM1_EXIST) {
    streamStartTime = millis();
    myCAMSendToSerial(myCAM1);
    double fps = ((millis() - streamStartTime) / 1000);
    Serial.println("fps: " + String(1 / fps ));
  }
}

void myCAMSendToSerial(ArduCAM myCAM) {
  char str[8];
  byte buf[5];
  static int i = 0;
  static int k = 0;
  uint8_t temp = 0, temp_last = 0;
  uint32_t length = 0;
  bool is_header = false;
 
  myCAM.flush_fifo(); //Flush the FIFO
  myCAM.clear_fifo_flag(); //Clear the capture done flag
  myCAM.start_capture();//Start capture
 
  while (!myCAM.get_bit(ARDUCHIP_TRIG , CAP_DONE_MASK));
  length = myCAM.read_fifo_length();
  Serial.print(F("FifoLength:,"));
  Serial.print(length, DEC);
  Serial.println(",");
 
  if (length >= MAX_FIFO_SIZE) //8M
  {
    Serial.println(F("Over size."));
    return ;
  }
 
  if (length == 0 ) //0 kb
  {
    Serial.println(F("Size is 0."));
    return ;
  }
  myCAM.CS_LOW();
  myCAM.set_fifo_burst();
  Serial.print("Image:,");
 
  while ( length-- )
  {
    temp_last = temp;
    temp =  SPI.transfer(0x00);
    //Read JPEG data from FIFO
    if ( (temp == 0xD9) && (temp_last == 0xFF) ) //If find the end ,break while,
    {
      buf[i++] = temp;  //save the last  0XD9
      //Write the remain bytes in the buffer
      myCAM.CS_HIGH();
      for (int i = 0; i < sizeof(buf); i++) {
        Serial.print(buf[i]); Serial.print(",");
      }
      Serial.println();
      Serial.println(F("Image transfer OK."));
      is_header = false;
      i = 0;
    }
    if (is_header == true)
    {
      //Write image data to buffer if not full
      if (i < 5) {
        buf[i++] = temp;
      } else
      {
        //Stream 5 bytes of raw image data to serial
        myCAM.CS_HIGH();
        for (int i = 0; i < sizeof(buf); i++) {
          Serial.print(buf[i]); Serial.print(",");
        }
        i = 0;
        buf[i++] = temp;
        myCAM.CS_LOW();
        myCAM.set_fifo_burst();
      }
    }
    else if ((temp == 0xD8) & (temp_last == 0xFF))
    {
      is_header = true;
      buf[i++] = temp_last;
      buf[i++] = temp;
    }
  }
}

void cameraSerial(uint8_t temp){
  uint8_t temp_last = 0;
  uint8_t start_capture = 0;
  switch (temp)
  {
//    case 0:
//      temp = 0xff;
//      myCAM1.OV2640_set_JPEG_size(OV2640_320x240);
//      Serial.println(F("OV2640_320x240")); delay(1000);
//      myCAM1.clear_fifo_flag();
//      break;
//    case 1:
//      temp = 0xff;
//      myCAM1.OV2640_set_JPEG_size(OV2640_640x480);
//      Serial.println(F("OV2640_640x480")); delay(1000);
//      myCAM1.clear_fifo_flag();
//      break;
//    case 2:
//      temp = 0xff;
//      myCAM1.OV2640_set_JPEG_size(OV2640_1024x768);
//      Serial.println(F("OV2640_1024x768")); delay(1000);
//      myCAM1.clear_fifo_flag();
//      break;
//    case 3:
//      {
//        if (stopMotion)
//          stopMotion = false;
//        else
//          stopMotion = true;
//        Serial.println("Stop Motion Enabled: " + String(stopMotion));
//      }
//      break;
    case 0x10:
      if (CAM1_EXIST) {
        streamStartTime = millis();
        myCAMSendToSerial(myCAM1);
        double fps = ((millis() - streamStartTime) / 1000);
        Serial.println("Total Time: " + String(fps));
      }
      break;
    default:
      break;
  }
}

void chasisSerial(uint8_t temp){
  switch(temp){
    case 0x0B:
      //Left
      digitalWrite(B1A, HIGH);
      digitalWrite(B1B, LOW);
      digitalWrite(A1A, LOW);
      digitalWrite(A1B, HIGH);
      delay(30);
      digitalWrite(A1A, LOW);
      digitalWrite(A1B, LOW);
      digitalWrite(B1A, LOW);
      digitalWrite(B1B, LOW);
      break;

    case 0x0C:
      //Right
      digitalWrite(B1A, LOW);
      digitalWrite(B1B, HIGH); 
      digitalWrite(A1A, HIGH);
      digitalWrite(A1B, LOW);
      delay(30);
      digitalWrite(A1A, LOW);
      digitalWrite(A1B, LOW);
      digitalWrite(B1A, LOW);
      digitalWrite(B1B, LOW);
      break;

    case 0x0A:
      //Come Closer
      digitalWrite(B1A, HIGH);
      digitalWrite(B1B, LOW);
      digitalWrite(A1A, HIGH);
      digitalWrite(A1B, LOW);
      delay(30);
      digitalWrite(A1A, LOW);
      digitalWrite(A1B, LOW);
      digitalWrite(B1A, LOW);
      digitalWrite(B1B, LOW);

      break;

    case 0x09:
      //Back up
      digitalWrite(B1A, LOW);
      digitalWrite(B1B, HIGH);
      digitalWrite(A1A, LOW);
      digitalWrite(A1B, HIGH);
      delay(30);
      digitalWrite(A1A, LOW);
      digitalWrite(A1B, LOW);
      digitalWrite(B1A, LOW);
      digitalWrite(B1B, LOW);

      break;

    default:
      digitalWrite(A1A, LOW);
      digitalWrite(A1B, LOW);
      digitalWrite(B1A, LOW);
      digitalWrite(B1B, LOW);
      break;
  }
}

void pantiltSerial(uint8_t temp){
  switch(temp){
    case 0x05:
      //SERVO1 left
      if (s1_angle > 0 && s1_angle <= 180) {
        s1_angle = s1_angle - angleStep;
        if (s1_angle < 0) {
          s1_angle = 0;
        }
        else {
          servo1.write(s1_angle); // move the servo to desired angle
          Serial.print("Moved to: ");
          Serial.print(s1_angle);   // print the angle
          Serial.println(" degree");
        }
      }
      delay(250); // waits for the servo to get there
      break;

    case 0x06:
      //SERVO1 Right
      if (s1_angle >= 0 && s1_angle < 180) {
        s1_angle = s1_angle + angleStep;
        if (s1_angle > 180) {
          s1_angle = 180;
        }
        else {
          servo1.write(s1_angle); // move the servo to desired angle
          Serial.print("Moved to: ");
          Serial.print(s1_angle);   // print the angle
          Serial.println(" degree");
        }
      }
      delay(250); // waits for the servo to get there
      break;

    case 0x03:
      //SERVO2 up
      if (s2_angle > 90 && s2_angle <= 180) {
        s2_angle = s2_angle - angleStep;
        if (s2_angle < 90) {
          s2_angle = 90;
        }
        else {
          servo2.write(s2_angle); // move the servo to desired angle
          Serial.print("Moved to: ");
          Serial.print(s2_angle);   // print the angle
          Serial.println(" degree");
        }
      }
      delay(250); // waits for the servo to get there]
      break;

    case 0x04:
      //SERVO2 down
      if (s2_angle >= 90 && s2_angle < 180) {
        s2_angle = s2_angle + angleStep;
        if (s1_angle > 180) {
          s2_angle = 180;
        }
        else {
          servo2.write(s2_angle); // move the servo to desired angle
          Serial.print("Moved to: ");
          Serial.print(s2_angle);   // print the angle
          Serial.println(" degree");
        }
      }
      delay(250); // waits for the servo to get there
      break;

    default:
      break;
  }
}
