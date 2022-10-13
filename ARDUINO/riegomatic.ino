// C++ code

#define SERIAL_DEBUG_ENABLED 1

#if SERIAL_DEBUG_ENABLED
    // For monitoring
  #define DebugPrint(str)\
      {\
        Serial.println(str);\
      }
#else
  #define DebugPrint(str)
#endif


// For monitoring State and Event
#define DebugPrintStatus(status,event)\
      {\
        String est = status;\
        String evt = event;\
        String str;\
        str = "******************************************************";\
        DebugPrint(str);\
        str = "State:[" + est + "] " + "Event:[" + evt + "].";\
        DebugPrint(str);\
      }
#define DebugPrintMetric(sensor,value)\
      {\
        String sen = sensor;\
        int val = value;\
        String str;\
        str = "Valor " + sen + " : " + val + "...";\
        DebugPrint(str);\
      }
#define DebugPrintNovedad(novedad)\
      {\
        String str = novedad;\
        str = "******" + str + "******";\
        DebugPrint(str);\
      }

/*CONSTANTES*/
#define PIN_GREEN_LED 9
#define PIN_RED_LED 4
#define PIN_BUTTON 7
#define PIN_HUMIDITY_SENSOR A0
#define PIN_DISTANCE_SENSOR_TRIGGER 13
#define PIN_DISTANCE_SENSOR_ECHO 12
#define PIN_TEMPERATURE_SENSOR 0
#define PIN_BUZZER 10
#define PIN_RELE 2

#define MAX_CANT_SENSORES 4
#define SENSOR_BUTTON 0
#define SENSOR_DISTANCE_TRIGGER 1
#define SENSOR_DISTANCE_ECHO 2
#define SENSOR_HUMIDITY 3
#define ACT_RIEGO 10

/*RESPUESTAS*/
#define R_OK 1
#define R_INTERRUPTION 7
#define R_ERROR 9

/*UMBRALES*/
#define HUMIDITY_LOW 200
#define HUMIDITY_HIGH 800
#define DISTANCE_MIN 128
#define TIEMPO_MAX_MILIS 5


#define TIME_MAX_MILLIS 1200
#define TIMEOUT_WARNING 500
#define HIGH_LEVEL_BRIGHTNESS 255
#define LOW_LEVEL_BRIGHTNESS 0

#define MAX_STATES 4
#define MAX_EVENTS 7


//----------------------------------------------
//-------------- GLOBAL VARIABLES --------------
/*Sensor structure*/
struct stSensor
{
    int pin;
    int state;
    long current_value;
    long previous_value;
};
stSensor sensors[MAX_CANT_SENSORES];

//----------------------------------------------

enum states     {    ST_OFF,    ST_STATUS_CHECK,    ST_WATERING,    ST_WARNING} current_state, last_state;
String states_s[] = {"OFF",     "STATUS_CHECK",     "WATERING",     "WARNING"};

enum events         {    EV_BUTTON,          EV_CONTROL,            EV_WARNING_1,                         EV_WARNING_2 ,    EV_NEED_WATER,    EV_TIMEOUT,    EV_UNKNOW } new_event, last_event;
String events_s[] = {"BUTTON_PRESSED", "CONTINUE_MONITORING",   "WARNING_LOW_WATER_LEVEL",    "WARNING_LOW_WATER_LEVEL_2" ,"NEED_WATER",          "TIMEOUT",      "UNKNOW"};

typedef void (*transition)();
transition state_table[MAX_STATES][MAX_EVENTS] =
{
  {status_check_ , off_		       , off_         , off_        , off_         , error_      , off_      } , // state ST_OFF
  {off_          , status_check_ , warning_     , warning2_   , watering_    , warning_    , error_    } , // state ST_STATUS_CHECK
  {off_          , status_check_ , watering_     , error_     , watering_    , warning_    , error_    } , // state ST_WATERING
  {off_          , status_check_ , warning_     , error_      , warning_     , warning_    , error_    }   // state ST_WARNING
  //EV_BUTTON    , EV_CONTROL    , EV_WARNING_1 , EV_WARNING_2 , EV_NEED_WATER, EV_TIMEOUT  , EV_UNKNOW
};
// EV_WARNING1 = Event that enables the alarm's first tone  & red led light 
// EV_WARNING2 = Event that enables the alarm's second tone  & red led light


//----------------------------------------------
//-------------- GLOBAL VARIABLES --------------
long humidity;
long distance;
unsigned long currentTimeDistance;
unsigned long lastCurrentTimeDistance;
unsigned long current_time_water_pump;
unsigned long past_time_water_pump;
int state_water_pump;
bool state_water_pump2;


bool flagAlarmLaunched = false;
int current_time;
int current_time2;
int prev_time;
int prev_time2;
bool flag = true;

//-----------------------------------------------
//----------------- INITIALIZE ------------------
void do_init()
{
    Serial.begin(9600); // Enable serial port.
    /*LEDS*/
    pinMode(PIN_GREEN_LED, OUTPUT);
    pinMode(PIN_RED_LED, OUTPUT);

    /*BUZZER*/
    pinMode(PIN_BUZZER, OUTPUT);

    /*BUTTON*/
    pinMode(PIN_BUTTON, INPUT);
    sensors[SENSOR_BUTTON].pin = PIN_BUTTON;
    sensors[SENSOR_BUTTON].state = 0; // inicia sin presionar

    /*HUMIDITY SENSOR*/
    pinMode(PIN_HUMIDITY_SENSOR, INPUT);
    sensors[SENSOR_HUMIDITY].pin = PIN_HUMIDITY_SENSOR;
    sensors[SENSOR_BUTTON].state = 0; // inicia sin presionar

    /*DISTANCE SENSOR*/
    pinMode(PIN_DISTANCE_SENSOR_TRIGGER, OUTPUT);
    pinMode(PIN_DISTANCE_SENSOR_ECHO, INPUT);
    sensors[SENSOR_DISTANCE_TRIGGER].pin = PIN_DISTANCE_SENSOR_TRIGGER;
    sensors[SENSOR_DISTANCE_ECHO].pin = PIN_DISTANCE_SENSOR_ECHO;

    //BOMBA AGUA 
    pinMode(PIN_RELE, OUTPUT);
    state_water_pump2 = false;

    past_time_water_pump=millis();
    state_water_pump=0;

    /* INTIALIZE FIRST EVENT*/
    last_state = ST_STATUS_CHECK;
    current_state = ST_OFF;
    last_event = EV_UNKNOW;
    new_event = EV_UNKNOW;
}

//----------------------------------------------
//------------------ ACTIONS -------------------
void turn_off_green_led()
{
    digitalWrite(PIN_GREEN_LED, LOW);
}

void turn_on_green_led()
{
    digitalWrite(PIN_GREEN_LED, HIGH);
}

void water_pump_action(bool state)
{
    if(state){
        digitalWrite(PIN_RELE, HIGH);
      //  DebugPrintNovedad("Regando");
   
    } else {
        digitalWrite(PIN_RELE, LOW);
        last_state = current_state;
        current_state = ST_STATUS_CHECK;
       // DebugPrintNovedad("Se termino de regar");
    }
}

void turn_off_red_led()
{
    analogWrite(PIN_RED_LED, LOW_LEVEL_BRIGHTNESS);
}

int read_sensor_humidity()
{
    sensors[SENSOR_HUMIDITY].previous_value = sensors[SENSOR_HUMIDITY].current_value;
    sensors[SENSOR_HUMIDITY].current_value = analogRead(PIN_HUMIDITY_SENSOR);
	  //DebugPrintMetric("Humedad",sensors[SENSOR_HUMIDITY].current_value);
    return sensors[SENSOR_HUMIDITY].current_value;
}

long read_sensor_distance()
{
    lastCurrentTimeDistance = 0;
    //Limpio el trigger
    digitalWrite(PIN_DISTANCE_SENSOR_TRIGGER,LOW);
    currentTimeDistance=millis();
    //Dejo pasar 5 milisegundos
    if( (currentTimeDistance-lastCurrentTimeDistance) >= (TIEMPO_MAX_MILIS))
    {
      //Pongo el trigger en HIGH
      digitalWrite(PIN_DISTANCE_SENSOR_TRIGGER,HIGH);
      lastCurrentTimeDistance = currentTimeDistance;
      currentTimeDistance=0;
      //Dejo pasar 10 milisegundos
      if( (currentTimeDistance-lastCurrentTimeDistance) >= (TIEMPO_MAX_MILIS*2))
      {
          //Apago el trigger
          digitalWrite(PIN_DISTANCE_SENSOR_TRIGGER,LOW); 
          //Leo la señal echo y retorno el tiempo del sonido
          return pulseIn(PIN_DISTANCE_SENSOR_ECHO,HIGH);
      } 
    } 
}
//Checks if the button was pressed to turn on or off the system..
bool check_button()
{	
	bool there_was_system_changed = false;
    /*Sistema se mantiene prendido hasta volver a apretar*/
    sensors[SENSOR_BUTTON].current_value = digitalRead(PIN_BUTTON); //read button value
    if ((sensors[SENSOR_BUTTON].current_value == HIGH) && (sensors[SENSOR_BUTTON].previous_value == LOW))
    {
        sensors[SENSOR_BUTTON].state = 1 - sensors[SENSOR_BUTTON].state;
        there_was_system_changed = true;

        if (sensors[SENSOR_BUTTON].state == 1)
        {
            DebugPrintNovedad("Sistema encendido");
        }else{
            DebugPrintNovedad("Sistema apagado");
        }
    }
    sensors[SENSOR_BUTTON].previous_value = sensors[SENSOR_BUTTON].current_value;
    return there_was_system_changed;
}

int check_water()
{
  distance = read_sensor_distance()/58;
	//DebugPrintMetric("Distancia sin agua en tanque",distance);
  if(distance < DISTANCE_MIN){
    return R_OK;
  }
  else { //distance to water too long..    
    return R_INTERRUPTION;
  }
  	
}
int check_humidity()
{
	read_sensor_humidity();

	if (sensors[SENSOR_HUMIDITY].current_value <= HUMIDITY_LOW)
	{
	//	DebugPrintNovedad("Hay poca humedad. Se debe regar.");
		return R_INTERRUPTION;
	}
	return R_OK;

	
}

//----------------------------------------------
//------------------ ESTADOS -------------------

void off_()
{
  last_state = current_state;
  current_state = ST_OFF;
  turn_off_green_led();
  turn_off_red_led();
  digitalWrite(PIN_RELE, LOW);
  state_water_pump = 0; // para dejar el estado de la bomba en apagado
}

// launch the alarm & start the twinkle red led
void warning_()
{
  DebugPrint("Inside warning_ function");
  last_state = current_state;
  current_state = ST_WARNING;

  // turn on the light
  analogWrite(PIN_RED_LED, HIGH_LEVEL_BRIGHTNESS); // Analog write ( PWM ) in the PIN_RED_LED
  tone(PIN_BUZZER, 1915, 200);
}
// 2nd tone alarm & put led off
void warning2_() 
{
  DebugPrint("Inside warning2_ function");
  last_state = current_state;
  current_state = ST_WARNING;
  // turn on the light
  analogWrite(PIN_RED_LED, HIGH_LEVEL_BRIGHTNESS); // Analog write ( PWM ) in the PIN_RED_LED
  // turn on the alarm's 2nd tone
  tone(PIN_BUZZER, 1432, 200);
}

void status_check_()
{
  last_state = current_state;
  current_state = ST_STATUS_CHECK;
  turn_off_red_led();
  turn_on_green_led();
}

void watering_()
{
    // state_water_pump2 = true;
    if(state_water_pump2 == true)
    {
      last_state = current_state;
      current_state = ST_WATERING;
      water_pump_action(state_water_pump2);

    } else 
    {
      state_water_pump = !state_water_pump;
      water_pump_action(state_water_pump2); // cuando apaga la bomba pasa a satus check
    }
    // current_time_water_pump = millis();
    // if( current_time_water_pump - past_time_water_pump > 2000 ) // solo voy a regar por 2 segundos
    // {
    //     past_time_water_pump = current_time_water_pump;
    //     state_water_pump = !state_water_pump;
    //     water_pump_action(state_water_pump); // cuando apaga la bomba pasa a satus check
    // }
}

void error_()
{
	// turn_on_red_led();
    //
    //
    //
    //
    //
    //
    //
    //
    //
    //
    // Ver si queremos hacer algo aca.. la funcion anterior no corre mas..
    //
    //
    //
    //
    //
    //
    //
    //
    DebugPrint("<<<<<<<<<<<<<<<<< OCCURIO UN ERROR >>>>>>>>>>>>>>>>>>>>>");
}
//----------------------------------------------
//----------- CHECK FOR NEW EVENTS -------------

void getNewEvent()
{   
    last_event = new_event;
    if (check_button()) // ON/OFF BUTTON..
    {
        new_event = EV_BUTTON;
        return;
    }
    //chekeo si esta regando
    if(state_water_pump2 == true) {
      current_time_water_pump = millis();
      if( current_time_water_pump - past_time_water_pump > 2000 ) // solo voy a regar por 2 segundos
      {
          past_time_water_pump = current_time_water_pump;
          state_water_pump2 = false;
          // state_water_pump = !state_water_pump;
          // water_pump_action(state_water_pump); // cuando apaga la bomba pasa a satus check
      }
    } else {
      return;
    }
  

    // ACA CHEQUEAR TIMEOUT
    if (check_water() == R_INTERRUPTION)
    {
      if(flag)
      {
        prev_time = millis();
        flag = false;
      }

      current_time = millis(); // take current..
      if ( current_time - prev_time > TIME_MAX_MILLIS)
      { // if the timer is not launched, launch it.
        flag = true;
        prev_time = millis();
        prev_time2 = millis();
        new_event = EV_WARNING_1; // fire first event..
        flagAlarmLaunched = true;
        return;
      }   
      current_time2 = millis();
      if (current_time2 - prev_time2 > TIMEOUT_WARNING && flagAlarmLaunched) //after 1500 ms...
      {
        new_event = EV_WARNING_2;
        flagAlarmLaunched = false;
        return;
      }
    }
    else if (check_humidity() == R_INTERRUPTION)
    {
      // new_event = EV_NEED_WATER;
      new_event = ST_WATERING;
      state_water_pump2 = true;
      return;
    }


    /*si no se genero ningun evento nuevo*/
    new_event = EV_CONTROL;
}

//-----------------------------------------------
//---------------- STATE MACHINE ----------------
void state_machine()
{
    getNewEvent();
    if ((new_event >= 0) && (new_event < MAX_EVENTS) && (current_state >= 0) && (current_state < MAX_STATES))
    {
        if (last_state != current_state || last_event != new_event)
        {
            DebugPrintStatus(states_s[current_state], events_s[new_event]);
        }
        // Launch the action
        state_table[current_state][new_event]();
		
    }
    else
    {
        DebugPrint("<<<<<<<<<<<<<<<<< EL EVENTO O ESTADO FUERA DE RANGO ESPERADO >>>>>>>>>>>>>>>>>>>>>");
		    DebugPrint("<<<<<<<<<<<<<<<<<    				      REVISAR CODIGO		 				  >>>>>>>>>>>>>>>>>>>>>");
    }

}

//-----------------------------------------------
//-------------- ARDUINO FUNCTIONS --------------
void setup()
{
    do_init();
}

void loop()
{
    state_machine();
}

//FIN 