#include <Servo.h>

Servo actuator;
const int pwmPin = 9; //change to pwm pin
int pwm = 1000;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  actuator.attach(pwmPin);
  actuator.writeMicroseconds(1000);
  delay(2000);
}

void loop() {

}

void serialEvent(){
  uint8_t temp = 0x00;
  if (Serial.available() > 0) {
    temp = Serial.read();
    switch (temp){
      case 0x01:
        if(pwm > 1000){
          pwm -= 5;
        }
        Serial.println("retract received");
        break;

      case 0x02:
        if(pwm < 2000){
          pwm += 5;
        }
        Serial.println("retract received");
        break;

      default:
        Serial.println("unrecognized");
        break;
    }
    actuator.writeMicroseconds(pwm);
 //   delay(10);

  }
//  while (Serial.available() > 0){
//    temp = Serial.read();
//  }
}