#include <Wire.h>
#include <ArduCAM.h>
#include <SPI.h>
#include "memorysaver.h"
#if !(defined OV2640_MINI_2MP)
  #error Please select the hardware platform and camera module in the ../libraries/ArduCAM/memorysaver.h file
#endif


// set pin 7 as the slave select for the digital pot:
const int CS = 7;
bool video = true;//false
bool camExists = false;
ArduCAM myCam(OV2640, CS);

void setup(){
  uint8_t vid, pid;
  uint8_t temp;
  Wire.begin();
  Serial.begin(250000); //921600
  Serial.println(F("ArduCAM Start"));
  pinMode(CS, OUTPUT);//output

  SPI.begin();

  //check if SPI Bus
  while (1) {
    myCam.write_reg(ARDUCHIP_TEST1, 0x55);
    temp = myCam.read_reg(ARDUCHIP_TEST1);
    if (temp != 0x55){
      Serial.println(F("SPI1 interface Error!"));
    } 
    else {
      camExists = true;
      Serial.println(F("SPI1 interface OK."));
      break;
    }
  }
  //init cam with JPEG
  myCam.set_format(JPEG);
  myCam.InitCAM();
  myCam.clear_fifo_flag();
  myCam.OV2640_set_JPEG_size(OV2640_320x240); 
  delay(1000);
  myCam.clear_fifo_flag();
  Serial.println("Ready:,1");
}

void loop(){
  if(camExists && video){
    sendToSerial(myCam);
  }
  
}

void sendToSerial(ArduCAM myCam){
  char str[8];
  byte buf[5];
  static int i = 0;
  static int k = 0;
  uint8_t temp = 0, temp_last = 0;
  uint32_t length = 0;
  bool is_header = false;
  
  myCam.flush_fifo(); //Flush the FIFO
  myCam.clear_fifo_flag(); //Clear the capture done flag
  myCam.start_capture();//Start capture
  while (!myCam.get_bit(ARDUCHIP_TRIG , CAP_DONE_MASK));

  length = myCam.read_fifo_length();
  Serial.print(F("FifoLength:,"));
  Serial.print(length, DEC);
  Serial.println(",");
 
  if (length >= MAX_FIFO_SIZE){ //8M
    Serial.println(F("Over size."));
    return ;
  }
  if (length == 0 ){ //0 kb
    Serial.println(F("Size is 0."));
    return ;
  }
  
  myCam.CS_LOW();
  myCam.set_fifo_burst();
  Serial.print("Image:,");

  while ( length-- ){
    temp_last = temp;
    temp =  SPI.transfer(0x00);
    //Read JPEG data from FIFO
    if ((temp == 0xD9) && (temp_last == 0xFF)){ //If find the end ,break while,
      buf[i++] = temp;  //save the last  0XD9
      //Write the remain bytes in the buffer
      myCam.CS_HIGH();
 
      for (int i = 0; i < sizeof(buf); i++) {
        Serial.print(buf[i]); Serial.print(",");
      }
      Serial.println();
      Serial.println(F("Image transfer OK."));
      is_header = false;
      i = 0;
    }
    if (is_header == true){
      //Write image data to buffer if not full
      if (i < 5) {
        buf[i++] = temp;
      } 
      else{
        //Stream 5 bytes of raw image data to serial
        myCam.CS_HIGH();
        for (int i = 0; i < sizeof(buf); i++) {
          Serial.print(buf[i]); Serial.print(",");
        }
        i = 0;
        buf[i++] = temp;
        myCam.CS_LOW();
        myCam.set_fifo_burst();
      }
    }
    else if ((temp == 0xD8) & (temp_last == 0xFF)){
      is_header = true;
      buf[i++] = temp_last;
      buf[i++] = temp;
    }
  }
}

//runs automatically when there's serial event
void serialEvent() {
  if (Serial.available() > 0) {
    uint8_t temp = 0xff, temp_last = 0;
    uint8_t start_capture = 0;
    temp = Serial.read();
    switch (temp){
//      case 3:{
//          if (video)
//            video = false;
//          else
//            video = true;
//          Serial.println("Video Enabled: " + String(video));
// 
//        }
//        break;

      case 0x10:
        if (camExists) {
          sendToSerial(myCam);
        }
        break;
      default:
        break;
    }
  }
}
