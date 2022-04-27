char Incoming_value = 0; 
void setup() {
  Serial.begin(9600);         //Sets the data rate in bits per second (baud) for serial data transmission
  pinMode(13, OUTPUT);
  pinMode(12, OUTPUT);
  pinMode(11, OUTPUT);
  pinMode(8, OUTPUT);
  pinMode(7, OUTPUT);
  pinMode(6, OUTPUT); 

}

void loop() {
    if(Serial.available() > 0)  
  {
    Incoming_value = Serial.read();      //Read the incoming data and store it into variable Incoming_value
    
    Serial.print(Incoming_value);        //Print Value of Incoming_value in Serial monitor
    Serial.print("\n");        //New line 
    
    if(Incoming_value == '49') {
      digitalWrite(6, HIGH); //ena
      digitalWrite(7, HIGH); //in1
      digitalWrite(8, LOW); //in2
    }
    else if(Incoming_value == '50') {
      digitalWrite(6, HIGH); //ena
      digitalWrite(7, LOW); //in1
      digitalWrite(8, HIGH); //in2
    }
    else if(Incoming_value == '51') {
      digitalWrite(11, HIGH); //ena
      digitalWrite(12, HIGH); //in1
      digitalWrite(13, LOW); //in2
    }
    else if(Incoming_value == '52') {
      digitalWrite(11, HIGH); //ena
      digitalWrite(12, LOW); //in1
      digitalWrite(13, HIGH); //in2
    }
      
  } 

}
