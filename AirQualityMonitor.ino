#include <Adafruit_Sensor.h>
#include "Adafruit_BME680.h"

#define SEALEVELPRESSURE_HPA (1013.25)
Adafruit_BME680 bme; // I2C

#define ledPin 13

//Dirty init
int ID = 4;
char deviceName[] = "HC-05-V2";

int state = 0;

void setup() {
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  Serial.begin(9600); // Default communication rate of the Bluetooth module

  if (!bme.begin(0x76)) {
    Serial.println("Could not find a valid BME680 sensor, check wiring!");
    while (1);
  }

  // Set up oversampling and filter initialization
  bme.setTemperatureOversampling(BME680_OS_8X);
  bme.setHumidityOversampling(BME680_OS_2X);
  bme.setPressureOversampling(BME680_OS_4X);
  bme.setIIRFilterSize(BME680_FILTER_SIZE_3);
  bme.setGasHeater(320, 150); // 320*C for 150 ms

  bme.performReading();
}

void sendParameters()
{
  double temp = 0.0;
  double hum = 0.0;
  double pres = 0.0;
  double gas = 0.0;
  
  bme.performReading();
  
  bme.performReading();
  temp = temp + bme.temperature;
  hum = hum + bme.humidity;
  pres = pres + bme.pressure;
  gas = gas + bme.gas_resistance;
  
  bme.performReading();
  temp = temp + bme.temperature;
  hum = hum + bme.humidity;
  pres = pres + bme.pressure;
  gas = gas + bme.gas_resistance;
  
  bme.performReading();
  temp = temp + bme.temperature;
  hum = hum + bme.humidity;
  pres = pres + bme.pressure;
  gas = gas + bme.gas_resistance;
  
  bme.performReading();
  temp = temp + bme.temperature;
  hum = hum + bme.humidity;
  pres = pres + bme.pressure;
  gas = gas + bme.gas_resistance;
  
  if (! bme.performReading())
  {
    Serial.println("Starting transmission");
    Serial.println(ID);
    Serial.println(deviceName);
    Serial.println(0.0f);
    Serial.println(0.0f);
    Serial.println(0.0f);
    Serial.println(0.0f);
    Serial.println(0.0f);
  }
  else{
    Serial.println("Starting transmission");
    Serial.println(ID);
    Serial.println(deviceName);
    Serial.println((temp + bme.temperature)/5.0);
    Serial.println((hum + bme.humidity)/5.0);
    Serial.println((((pres + bme.pressure)/5.0) / 98.7) * 0.75006);
    Serial.println(((gas + bme.gas_resistance)/5.0) / 1000.0);
    Serial.println(bme.readAltitude(SEALEVELPRESSURE_HPA));
  }
}
void loop()
{
  if(Serial.available() > 0)
  { // Checks whether data is comming from the serial port
    state = Serial.read(); // Reads the data from the serial port
  }
  if (state == '0') 
  {
    digitalWrite(ledPin, LOW); // Turn LED OFF
    //Serial.println("LED: OFF"); // Send back, to the phone, the String "LED: ON"
    state = 0;
    sendParameters();
  }else if (state == '1') {
    digitalWrite(ledPin, HIGH);
    //Serial.println("LED: ON");
    state = 0;
    sendParameters();
  } 
}
