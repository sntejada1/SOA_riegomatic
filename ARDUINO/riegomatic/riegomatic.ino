// C++ code
#include <SoftwareSerial.h>

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
#define CONVERTER_CM 58
#define BAUD_RATE 9600
#define TONE1 1915
#define TONE2 1432
#define TONE_DURATION 200
#define ACTIVE 1 
#define INACTIVE 0
#define PORCENTAJE1024 10.24
#define CIENPORC 100

/*RESPUESTAS*/
#define R_OK 1
#define R_INTERRUPTION 7
#define R_ERROR 9

/*UMBRALES*/
#define HUMIDITY_LOW 30
#define HUMIDITY_HIGH 80
#define DISTANCE_MIN 16
#define TIME_TRIGGER_HIGH 5
#define TIME_TRIGGER_LOW 10

/*TIMERS*/
#define TIME_MAX_MILLIS 1200
#define TIMEOUT_WARNING 500
#define HIGH_LEVEL_BRIGHTNESS 255
#define LOW_LEVEL_BRIGHTNESS 0
#define TIME_WATERING 2000
#define TIME_REPORT 2000

/*MAQUINA DE ESTADOS*/
#define MAX_STATES 4
#define MAX_EVENTS 8

/*Senales Bluetooh*/
#define SENAL_ONOF 1 
#define SENAL_WATERING 2 

const char senal_onof ='1';
const char senal_water ='2';
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

enum events         {    EV_BUTTON,          EV_CONTROL,            EV_WARNING_1,                    EV_WARNING_2 ,       EV_NEED_WATER,    EV_UNKNOW , EV_BT_ONOF, EV_BT_WATER} new_event, last_event;
String events_s[] = {"BUTTON_PRESSED", "CONTINUE_MONITORING",   "WARNING_LOW_WATER_LEVEL",    "WARNING_LOW_WATER_LEVEL_2" ,"NEED_WATER",      "UNKNOW",   "EV_BT_ONOF", "EV_BT_WATER"};

SoftwareSerial miBT(5, 6); // pin 5 Rx BT y Tx Arduino, Pin 6 Rx BT Tx arduino

//----------------------------------------------
//-------------- GLOBAL VARIABLES --------------
long humidity;
long distance;
unsigned long currentTimeDistance;
unsigned long lastCurrentTimeDistance;
unsigned long currentTimeDistance2;
unsigned long lastCurrentTimeDistance2;
unsigned long current_time_water_pump;
unsigned long past_time_water_pump;
bool state_water_pump;
bool watering_flag;


bool flagAlarmLaunched = false;
int current_time;
int current_time2;
int prev_time;
int prev_time2;
bool flag = true;
bool flagDistance = true;
bool flagDistance2 = true;
bool checkDistance = false;
int timeDistance = 0;
int currenttime_report = 0;
int prevtime_report = 0;
int senal = 0;

//-----------------------------------------------
//----------------- INITIALIZE ------------------
void do_init()
{
    Serial.begin(BAUD_RATE); // Enable serial port.
    miBT.begin(BAUD_RATE);
    /*LEDS*/
    pinMode(PIN_GREEN_LED, OUTPUT);
    pinMode(PIN_RED_LED, OUTPUT);

    /*BUZZER*/
    pinMode(PIN_BUZZER, OUTPUT);

    /*BUTTON*/
    pinMode(PIN_BUTTON, INPUT);
    sensors[SENSOR_BUTTON].pin = PIN_BUTTON;
    sensors[SENSOR_BUTTON].state = INACTIVE; // inicia sin presionar

    /*HUMIDITY SENSOR*/
    pinMode(PIN_HUMIDITY_SENSOR, INPUT);
    sensors[SENSOR_HUMIDITY].pin = PIN_HUMIDITY_SENSOR;
    sensors[SENSOR_BUTTON].state = INACTIVE; // inicia sin presionar

    /*DISTANCE SENSOR*/
    pinMode(PIN_DISTANCE_SENSOR_TRIGGER, OUTPUT);
    pinMode(PIN_DISTANCE_SENSOR_ECHO, INPUT);
    sensors[SENSOR_DISTANCE_TRIGGER].pin = PIN_DISTANCE_SENSOR_TRIGGER;
    sensors[SENSOR_DISTANCE_ECHO].pin = PIN_DISTANCE_SENSOR_ECHO;

    //BOMBA AGUA 
    pinMode(PIN_RELE, OUTPUT);
    state_water_pump = false;
    watering_flag = false;

    /* INTIALIZE FIRST EVENT*/
    last_state = ST_STATUS_CHECK;
    current_state = ST_OFF;
    last_event = EV_UNKNOW;
    new_event = EV_UNKNOW;

    prevtime_report = millis();
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
  if (state)
  {
    digitalWrite(PIN_RELE, HIGH);
  }
  else
  {
    digitalWrite(PIN_RELE, LOW);
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
  DebugPrintMetric("Humedad", sensors[SENSOR_HUMIDITY].current_value);
  return sensors[SENSOR_HUMIDITY].current_value;
}

long read_sensor_distance()
{
  if (timeDistance == 0)
  {
    digitalWrite(PIN_DISTANCE_SENSOR_TRIGGER, LOW);
  }
  if (timeDistance == TIME_TRIGGER_HIGH)
  {
    digitalWrite(PIN_DISTANCE_SENSOR_TRIGGER, HIGH);
  }
  if (timeDistance == TIME_TRIGGER_LOW)
  {
    digitalWrite(PIN_DISTANCE_SENSOR_TRIGGER, LOW);
    checkDistance = true;
    flagDistance = true;
    flagDistance2 = true;
    timeDistance = 0;
    return pulseIn(PIN_DISTANCE_SENSOR_ECHO, HIGH);
  }
}

// Checks if the button was pressed to turn on or off the system..
bool check_button()
{
  bool there_was_system_changed = false;
  /*Sistema se mantiene prendido hasta volver a apretar*/
  sensors[SENSOR_BUTTON].current_value = digitalRead(PIN_BUTTON); // read button value
  if ((sensors[SENSOR_BUTTON].current_value == HIGH) && (sensors[SENSOR_BUTTON].previous_value == LOW))
  {
    sensors[SENSOR_BUTTON].state = 1 - sensors[SENSOR_BUTTON].state;
    there_was_system_changed = true;

    if (sensors[SENSOR_BUTTON].state == ACTIVE)
    {
      DebugPrintNovedad("Sistema encendido");
    }
    else
    {
      DebugPrintNovedad("Sistema apagado");
    }
  }
  sensors[SENSOR_BUTTON].previous_value = sensors[SENSOR_BUTTON].current_value;
  return there_was_system_changed;
}

int check_bth()
{
  fflush(stdin);

  if (miBT.available())
  { // si hay informacion disponible desde modulobluetooth
    char rta = miBT.read();
    DebugPrint("Se recibio:");
    DebugPrint(rta);
    if (rta == senal_onof)
    {
      DebugPrint("Senal 1 SENAL_ONOF");
      return SENAL_ONOF;
    }
    if (rta == senal_water)
    {
      DebugPrint("Senal 2 SENAL_WATERING");
      return SENAL_WATERING;
    }
  }
  return 0;
}

int check_water()
{
  distance = read_sensor_distance() / CONVERTER_CM;
  if (checkDistance == true)
  {
    if (distance < DISTANCE_MIN)
    {
      checkDistance = false;
      return R_OK;
    }
    else
    { // distance to water too long..
      checkDistance = false;
      return R_INTERRUPTION;
    }
  }
}

int check_humidity()
{
  read_sensor_humidity();

  // se calcula la humedad en porcentaje
  if (-((sensors[SENSOR_HUMIDITY].current_value / PORCENTAJE1024) - CIENPORC) <= HUMIDITY_LOW)
  {
    return R_INTERRUPTION;
  }
  return R_OK;
}

void report_sensors_bth()
{

  // enviar información se distancia y humedad.
  char cstr[10];
  int valorHumedad = sensors[SENSOR_HUMIDITY].current_value;
  valorHumedad = -((valorHumedad / PORCENTAJE1024) - CIENPORC);
  String strValorHumedad = String(valorHumedad);
  String strValorDistancia = String(distance);
  String str = String(' ');
  str = "#" + strValorHumedad + 'a' + strValorDistancia + "\n" ;
  str.toCharArray(cstr, 10);
  miBT.write(cstr);
  return;
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
  state_water_pump = false; // para dejar el estado de la bomba en apagado
}

// launch the alarm & start the twinkle red led
void warning_()
{
  last_state = current_state;
  current_state = ST_WARNING;

  // turn on the light
  analogWrite(PIN_RED_LED, HIGH_LEVEL_BRIGHTNESS); // Analog write ( PWM ) in the PIN_RED_LED
  tone(PIN_BUZZER, TONE1, TONE_DURATION);
}
// 2nd tone alarm & put led off
void warning2_()
{
  last_state = current_state;
  DebugPrint("current state 2 " + current_state);
  current_state = ST_WARNING;
  // turn on the light
  analogWrite(PIN_RED_LED, HIGH_LEVEL_BRIGHTNESS); // Analog write ( PWM ) in the PIN_RED_LED
  // turn on the alarm's 2nd tone
  tone(PIN_BUZZER, TONE2, TONE_DURATION);
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
  if (!watering_flag)
  { // si no estoy regando
    state_water_pump = true;
    watering_flag = !watering_flag; // para a true
  }
  if (state_water_pump == true)
  {
    last_state = current_state;
    current_state = ST_WATERING;
    water_pump_action(state_water_pump);
  }
  else
  {
    water_pump_action(state_water_pump); // cuando apaga la bomba pasa a satus check
    last_state = current_state;
    current_state = ST_STATUS_CHECK;
    watering_flag = !watering_flag; // pasa a false
  }
}

void error_()
{
  DebugPrint("<<<<<<<<<<<<<<<<< OCCURIO UN ERROR >>>>>>>>>>>>>>>>>>>>>");
}

typedef void (*transition)();
transition state_table[MAX_STATES][MAX_EVENTS] =
{
  {status_check_ , off_           , off_        , off_        , off_            , off_      , status_check_ , off_        } , // state ST_OFF
  {off_          , status_check_ , warning_     , warning2_   , watering_       , error_    , off_          , watering_   } , // state ST_STATUS_CHECK
  {off_          , status_check_ , watering_    , watering_   , watering_       , error_    , off_          , watering_   } , // state ST_WATERING
  {off_          , status_check_ , warning_     , warning2_   , warning_        , error_    , off_          , warning_    }   // state ST_WARNING
  //EV_BUTTON    , EV_CONTROL    , EV_WARNING_1 , EV_WARNING_2 , EV_NEED_WATER  ,EV_UNKNOW , EV_BT_ONOF   , EV_BT_WATER
};
// EV_WARNING1 = Event that enables the alarm's first tone  & red led light 
// EV_WARNING2 = Event that enables the alarm's second tone  & red led light
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

  // ESCUCHAMOS SENAL DE BLUETOOTH..
  senal = check_bth();
  if (senal != 0)
  {
    if (senal == SENAL_ONOF)
    {
      new_event = EV_BT_ONOF;
      return;
    }
  }

  // timer para reportar informacion
  currenttime_report = millis();
  if ((currenttime_report - prevtime_report) >= TIME_REPORT)
  {
    report_sensors_bth();
    prevtime_report = currenttime_report;
  }

  // TIMER DISTANCE
  if (flagDistance == true)
  {
    lastCurrentTimeDistance = millis();
    flagDistance = false;
  }

  currentTimeDistance = millis();
  // Dejo pasar 5 milisegundos
  if ((currentTimeDistance - lastCurrentTimeDistance) >= (TIME_TRIGGER_HIGH))
  {
    // evaluo distancia hasta el agua de la reserva
    timeDistance = TIME_TRIGGER_HIGH;

    if (flagDistance2 == true)
    {
      lastCurrentTimeDistance2 = millis();
      flagDistance2 = false;
    }

    currentTimeDistance2 = millis();
    if ((currentTimeDistance2 - lastCurrentTimeDistance2) >= (TIME_TRIGGER_LOW))
    {
      timeDistance = TIME_TRIGGER_LOW;
    }
  }

  if (check_water() == R_INTERRUPTION && state_water_pump != true)
  {
    if (flag)
    {
      prev_time = millis();
      flag = false;
    }

    current_time = millis(); // take current..
    if (current_time - prev_time > TIME_MAX_MILLIS)
    { // if the timer is not launched, launch it.
      flag = true;
      prev_time = millis();
      prev_time2 = millis();
      new_event = EV_WARNING_1; // fire first event..
      flagAlarmLaunched = true;
      return;
    }
    current_time2 = millis();
    if (current_time2 - prev_time2 > TIMEOUT_WARNING && flagAlarmLaunched) // after 1500 ms...
    {
      new_event = EV_WARNING_2;
      flagAlarmLaunched = false;
      return;
    }
  } else if (check_humidity() == R_INTERRUPTION || state_water_pump == true)
  {

    new_event = EV_NEED_WATER;

    if (!watering_flag) // si no estoy regando
    {
      past_time_water_pump = millis();
      return;
    }

    if (state_water_pump == true)
    {
      current_time_water_pump = millis();
      if (current_time_water_pump - past_time_water_pump > TIME_WATERING) // solo voy a regar por 2 segundos
      {
        past_time_water_pump = current_time_water_pump;
        state_water_pump = false;
        new_event = EV_NEED_WATER;
        return;
      }
      else
      {
        return;
      }
    }
  }

  // ESCUCHAMOS SENAL DE BLUETOOTH..
  if (senal != 0)
  {
    if (senal == SENAL_WATERING)
    {
      new_event = EV_BT_WATER;
      past_time_water_pump = millis();

      return;
    }
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

// FIN
