//Collaborated Code with Kathleen & Bailey
#include <Servo.h>

Servo actuator;
const int pwmPin = 9; //change to pwm pin
int pwm = 1000;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(250000); //921600
  linearActuatorSetup();
}

void loop() {

}

void serialEvent(){
  if (Serial.available() > 0) {
    uint8_t temp = 0xff;
    temp = Serial.read();
    linearActuatorSerial(temp);
  }
}

void linearActuatorSetup(){
  actuator.attach(pwmPin);
  actuator.writeMicroseconds(1000);
  delay(2000);
}

void linearActuatorSerial(uint8_t temp){
  if (Serial.available() > 0) {
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
  }
}
