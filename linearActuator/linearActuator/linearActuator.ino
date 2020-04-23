#include <Servo.h>

Servo actuator;
const int pwmPin = 9; //change to pwm pin


void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  actuator.attach(pwmPin);
}

void loop() {
  // put your main code here, to run repeatedly:

}

void serialEvent(){
  uint8_t temp = 0x00;
  if (Serial.available() > 0) {
    temp = Serial.read();
    switch (temp){
      case 0x01:
        actuator.writeMicroseconds(2000); //2000 ms retracts arm
        //delay(200);
        actuator.writeMicroseconds(0);
        Serial.println("expand received");

      case 0x02:
        actuator.writeMicroseconds(1000); //1000 ms extends arm
        //delay(200);
        actuator.writeMicroseconds(0);
        Serial.println("retract received");

      default:
        Serial.println("unrecognized");
    }
    
  }
}
