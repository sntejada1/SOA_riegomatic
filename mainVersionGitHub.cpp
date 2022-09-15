// C++ code
//

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
#define DebugPrintStatus(statuss,event)\
      {\
        String est = statuss;\
        String evt = event;\
        String str;\
        str = "******************************************************";\
        DebugPrint(str);\
        str = "State:[" + est + "] " + "Event:[" + evt + "].";\
        DebugPrint(str);\
      }



/*CONSTANTES*/
#define PIN_GREEN_LED 9
#define PIN_RED_LED 8
#define PIN_BUTTON 7
#define PIN_HUMIDITY_SENSOR A0
#define PIN_DISTANCE_SENSOR 12
#define PIN_TEMPERATURE_SENSOR 0

#define MAX_CANT_SENSORES 4
#define SENSOR_BUTTON 0
#define SENSOR_DISTANCE 1
#define SENSOR_TEMPERATURE 2
#define SENSOR_HUMIDITY 3
#define ACT_RIEGO 10

/*UMBRALES*/
#define HUMIDITY_LOW 200
#define HUMIDITY_HIGH 800

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

enum states     {    ST_OFF,    ST_STATUS_CHECK,    ST_WATERING,    ST_WARNING} current_state;
String states_s[] = {"OFF",     "STATUS_CHECK",     "WATERING",     "WARNING"};

enum events         {    EV_BUTTON,    EV_CONTROL,           EV_WARNING,    EV_NEED_WATER,    EV_TIMEOUT,    EV_UNKNOW,    EV_SOUND1,    EV_SOUND2} new_event;
String events_s[] = {"BUTTON_PRESSED", "CONTINUE_MONITORING", "WARNING", "NEED_WATER",          "TIMEOUT",      "UNKNOW",   "SOUND1",    "SOUND2"};

#define MAX_STATES 4
#define MAX_EVENTS 8

typedef void (*transition)();
transition state_table[MAX_STATES][MAX_EVENTS] =
{
  {status_check_    , error_       , error_     , error_       , error_       , off_      } , // state ST_OFF
  {off_       , status_check_     , warning_   , watering_  , warning_    , error_       } , // state ST_STATUS_CHECK
  {off_       , status_check_     , warning_   , error_      , warning_    , error_       } , // state ST_WATERING
  {off_       , error_            , warning_   , error_       , warning_    , error_       }   // state ST_WARNING
  //EV_BUTTON  , EV_CONTROL , EV_WARNING , EV_NEED_WATER , EV_TIMEOUT  , EV_UNKNOW
};


//----------------------------------------------
//-------------- GLOBAL VARIABLES --------------
// int lct; //last current time.
int system_status = 0;
long humidity;
int there_was_system_changed = 0; // variables temporales para las pruebas NI IDEA QUE ES ESTA VARIABLE

//-----------------------------------------------
//----------------- INITIALIZE ------------------
void do_init()
{
    Serial.begin(9600); // Enable serial port.
    /*LEDS*/
    pinMode(PIN_GREEN_LED, OUTPUT);
    pinMode(PIN_RED_LED, OUTPUT);

    /*BUTTON*/
    pinMode(PIN_BUTTON, INPUT);
    sensors[SENSOR_BUTTON].pin = PIN_BUTTON;
    sensors[SENSOR_BUTTON].state = 0; // inicia sin presionar

    /*HUMIDITY SENSOR*/
    pinMode(PIN_HUMIDITY_SENSOR, INPUT);
    sensors[SENSOR_HUMIDITY].pin = PIN_HUMIDITY_SENSOR;
    sensors[SENSOR_BUTTON].state = 0; // inicia sin presionar

    /*DISTANCE SENSOR*/
    // pinMode(PIN_DISTANCE_SENSOR, INPUT);
    // sensors[SENSOR_DISTANCE].pin    = PIN_DISTANCE_SENSOR;
    // sensors[SENSOR_DISTANCE].state = 0;

    /* INTIALIZE FIRST EVENT*/
    current_state = ST_OFF;
    new_event = EV_UNKNOW;

    // lct = millis(); // ultimo tiempo actual.
}

void turn_off_led_green()
{
    digitalWrite(PIN_GREEN_LED, LOW);
    // Serial.println("Led apagado");
}

void turn_on_led_green()
{
    digitalWrite(PIN_GREEN_LED, HIGH);
    // Serial.println("Led prendido");
}

void turn_on_led_red()
{
    digitalWrite(PIN_RED_LED, HIGH);
}

void turn_off_led_red()
{
    digitalWrite(PIN_RED_LED, LOW);
}

int read_sensor_humidity()
{
    sensors[SENSOR_HUMIDITY].previous_value = sensors[SENSOR_HUMIDITY].current_value;
    sensors[SENSOR_HUMIDITY].current_value = analogRead(PIN_HUMIDITY_SENSOR);
    Serial.println(sensors[SENSOR_HUMIDITY].current_value);
    return sensors[SENSOR_HUMIDITY].current_value;
}

long read_sensor_distance()
{
    return analogRead(PIN_DISTANCE_SENSOR);
}

int check_button()
{
    /*Sistema se mantiene prendido hasta volver a apretar*/
    sensors[SENSOR_BUTTON].current_value = digitalRead(PIN_BUTTON);
    //  Serial.println(sensors[SENSOR_BUTTON].current_value);
    if ((sensors[SENSOR_BUTTON].current_value == HIGH) && (sensors[SENSOR_BUTTON].previous_value == LOW))
    {
        sensors[SENSOR_BUTTON].state = 1 - sensors[SENSOR_BUTTON].state;
        there_was_system_changed = 1;
    }
    sensors[SENSOR_BUTTON].previous_value = sensors[SENSOR_BUTTON].current_value;

    /*Seccion que loggea si el sistema esta prendido o apagado cuando esta en modo debug*/
    if (SERIAL_DEBUG_ENABLED && there_was_system_changed == 1)
    {
        if (sensors[SENSOR_BUTTON].state == 1)
        {
            Serial.println("Sistema encendido");
            system_status = 1;
        }
        else
        {
            Serial.println("Sistema apagado");
            system_status = 0;
        }
    }
    /*******************/
    return there_was_system_changed;
}

//-----------------------------------------------
/** ESTADOS **/

void off_()
{
    turn_off_led_green();
    turn_off_led_red();

    if (SERIAL_DEBUG_ENABLED && there_was_system_changed)
    {
        Serial.println("Sistema apagado");
        there_was_system_changed = 0;
    }
}

void warning_()
{
    turn_on_led_red();
    if (SERIAL_DEBUG_ENABLED && there_was_system_changed)
    {
        Serial.println("Sistema con en alarma o con errores");
        there_was_system_changed = 0;
    }
}

void status_check_()
{
    turn_off_led_red();
    turn_on_led_green();
    if (SERIAL_DEBUG_ENABLED && there_was_system_changed)
    {
        Serial.println("Inicio de monitoreo de temperatura humedad y cantidad de agua");
        there_was_system_changed = 0;
    }
}

void watering_()
{
    Serial.println("Habilitando salida de agua");
    digitalWrite(ACT_RIEGO, HIGH);
    delay(2000);
    digitalWrite(ACT_RIEGO, LOW);
}

void error_()
{
    turn_on_led_red;
    Serial.println("<<<<<<<<<<<<<<<<< OCCURIO UN ERROR >>>>>>>>>>>>>>>>>>>>>");
}

void getNewEvent()
{
    there_was_system_changed = 0;
    if (check_button())
    {
        new_event = EV_BUTTON;
        return;
    }
    if (system_status == 1)
    {
        read_sensor_humidity();

        if (sensors[SENSOR_HUMIDITY].current_value <= HUMIDITY_LOW)
        {
            Serial.println("Hay poca humedad. Regar");
            turn_on_led_red(); // Solo para probar la conexion del led
            //  new_event   = EV_NEED_WATER;
            //  return;
        }
        if (sensors[SENSOR_HUMIDITY].current_value < HUMIDITY_HIGH && sensors[SENSOR_HUMIDITY].current_value > HUMIDITY_LOW)
        {
            turn_off_led_red(); // Solo para probar la conexion del led
            Serial.println("humedad NORMAL");
        }

        if (sensors[SENSOR_HUMIDITY].current_value >= HUMIDITY_HIGH)
        {
            Serial.println("Hay mucha humedad");
        }

        /*si no se genero ningun evento nuevo*/

        new_event = EV_CONTROL;
    }
    else
    {
        new_event = EV_UNKNOW;
    }

    // Genero evento dummy ....
    new_event = EV_CONTROL;
}

//-----------------------------------------------
//---------------- STATE MACHINE ----------------
void state_machine()
{
    getNewEvent();
    if ((new_event >= 0) && (new_event < MAX_EVENTS) && (current_state >= 0) && (current_state < MAX_STATES))
    {
        if (new_event != EV_CONTROL)
        {
            DebugPrintStatus(states_s[current_state], events_s[new_event]);
        }
        // Launch the action
        state_table[current_state][new_event]();
    }
    else
    {
        Serial.println("<<<<<<<<<<<<<<<<< OCCURIO UN ERROR CON EL EVENTO O ESTADO >>>>>>>>>>>>>>>>>>>>>");
        DebugPrintStatus(states_s[ST_WARNING], events_s[EV_UNKNOW]);
    }

    // Consume the event..
    new_event = EV_CONTROL;
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