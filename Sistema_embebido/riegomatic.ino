// C++ code
//
#define SERIAL_DEBUG_ENABLED  1

#if SERIAL_DEBUG_ENABLED
  #define DebugPrint(str)\
      {\
        Serial.println(str);\
      }
#else
  #define DebugPrint(str)
#endif

#define DebugPrintStatus(current_state,event)\
      {\
        String est = current_state;\
        String evt = event;\
        String str;\
        str = "******************************************************";\
        DebugPrint(str);\
        str = "State:[" + est + "] " + "Event:[" + evt + "].";\
        DebugPrint(str);\
      }


/*CONSTANTES*/
#define PIN_LED_GREEN                          9
#define PIN_LED_RED                            8
#define PIN_BUTTON                             7
#define PIN_SENSOR_HUMIDITY                   A0
#define PIN_SENSOR_DISTANCE                   12
#define PIN_SENSOR_TEMPERATURE                 0


#define MAX_CANT_SENSORES                      4
#define SENSOR_BUTTON                          0
#define SENSOR_DISTANCE                        1
#define SENSOR_TEMPERATURE                     2                       
#define SENSOR_HUMIDITY                        3
#define ACT_RIEGO							   10								


/*UMBRALES*/
#define HUMIDITY_LOW                           200
#define HUMIDITY_HIGH                          800



struct stSensor
{
  int  pin;
  int  state;
  long current_value;
  long previous_value;
};

stSensor sensors[MAX_CANT_SENSORES];

//----------------------------------------------

enum states          { ST_OFF,  ST_STATUS_CHECK       , ST_WATERING   , ST_WARNING   } current_state;
String states_s [] = {"OFF", 	"STATUS_CHECK"     	 , "WATERING" , 	"WARNING"};

enum events          { EV_BUTTON,          EV_CONTROL   			, EV_WARNING    , EV_NEED_WATER  , EV_TIMEOUT   , EV_UNKNOW   } new_event;
String events_s [] = {"BUTTON_PRESSED", "CONTINUE_MONITORING" , "WARNING"  , 	"NEED_WATER", 		"TIMEOUT" ,	 "UNKNOW" };

#define MAX_STATES 4
#define MAX_EVENTS 6

typedef void (*transition)();

transition state_table[MAX_STATES][MAX_EVENTS] =
{
  {status_check_    , error_       , error_     , error_       , error_       , off_      } , // state ST_OFF
  {off_       , status_check_     , warning_   , watering_  , warning_    , error_       } , // state ST_STATUS_CHECK
  {off_       , status_check_     , warning_   , error_      , warning_    , error_       } , // state ST_WATERING
  {off_       , error_            , warning_   , error_       , warning_    , error_       }   // state ST_WARNING
  //EV_BUTTON  , EV_CONTROL , EV_WARNING , EV_NEED_WATER , EV_TIMEOUT  , EV_UNKNOW
};

int new_event_table[MAX_STATES][MAX_EVENTS] =
{
  {ST_STATUS_CHECK   , 9       , 9     , 9       , 9       , ST_OFF       } , // state ST_OFF
  {ST_OFF      , ST_STATUS_CHECK   , ST_WARNING   , ST_WATERING  , ST_WARNING    , 9       } , // state ST_STATUS_CHECK
  {ST_OFF       , ST_STATUS_CHECK     , ST_WARNING   , 9      , ST_WARNING    , 9       } , // state ST_WATERING
  {ST_OFF     , 9            , ST_WARNING   , 9       , ST_WARNING    , 9       }   // state ST_WARNING
  //EV_BUTTON  , EV_CONTROL , EV_WARNING , EV_NEED_WATER , EV_TIMEOUT  , EV_UNKNOW
};

int system_status = 0;
bool timeout;
long humidity;

  

//variables temporales para las pruebas
int there_was_system_changed=0;


//----------------------------------------------
void do_init()
{

  
  
  /*LEDS*/
  pinMode(PIN_LED_GREEN, OUTPUT);
  pinMode(PIN_LED_RED, OUTPUT);
  
   /*PULSADOR*/
  pinMode(PIN_BUTTON, INPUT);
  sensors[SENSOR_BUTTON].pin    = PIN_BUTTON;
  sensors[SENSOR_BUTTON].state    = 0; // inicia sin presionar
  
  
  /*SENSORES*/
  pinMode(PIN_SENSOR_HUMIDITY, INPUT);
  sensors[SENSOR_HUMIDITY].pin  = PIN_SENSOR_HUMIDITY;
  
 /* pinMode(PIN_SENSOR_DISTANCE, INPUT);
   sensors[SENSOR_DISTANCE].pin    = PIN_SENSOR_DISTANCE;
  sensors[SENSOR_DISTANCE].state = 108;*/
  Serial.begin(9600);//se usa para cada cuanto seva a imprimir los prints(segun entendi)
  
 /*EVENTOS*/
  current_state = ST_OFF;
  new_event   = EV_UNKNOW;
  
}

void turn_off_led_green( )
{
  digitalWrite(PIN_LED_GREEN , LOW);
 // Serial.println("Led apagado");
}

void turn_on_led_green( )
{
  digitalWrite(PIN_LED_GREEN , HIGH);
  //Serial.println("Led prendido");
}

void turn_on_led_red()
{
 digitalWrite(PIN_LED_RED, HIGH);
}

void turn_off_led_red()
{
  digitalWrite(PIN_LED_RED, LOW);
}

int read_sensor_humidity( )
{
  sensors[SENSOR_HUMIDITY].previous_value = sensors[SENSOR_HUMIDITY].current_value;
  sensors[SENSOR_HUMIDITY].current_value = analogRead(PIN_SENSOR_HUMIDITY);
  Serial.println( sensors[SENSOR_HUMIDITY].current_value );
  return  sensors[SENSOR_HUMIDITY].current_value ;
}


long read_sensor_distance( )
{
  return analogRead(PIN_SENSOR_DISTANCE);
}


int check_button( )
{
  /*Sistema se mantiene prendido hasta volver a apretar*/
 sensors[SENSOR_BUTTON].current_value = digitalRead(PIN_BUTTON);
//  Serial.println(sensors[SENSOR_BUTTON].current_value);
 if ((sensors[SENSOR_BUTTON].current_value == HIGH) && (sensors[SENSOR_BUTTON].previous_value == LOW))
 {
   sensors[SENSOR_BUTTON].state = 1- sensors[SENSOR_BUTTON].state ;  
   there_was_system_changed =1;
   
 }
 sensors[SENSOR_BUTTON].previous_value = sensors[SENSOR_BUTTON].current_value;

/*Seccion que loggea si el sistema esta prendido o apagado cuando esta en modo debug*/
  if(SERIAL_DEBUG_ENABLED && there_was_system_changed== 1)
  {
    if (sensors[SENSOR_BUTTON].state == 1)
    {
      Serial.println("Sistema encendido");
      system_status = 1;
    }else{
      Serial.println("Sistema apagado"); 
      system_status = 0;
   }
  }
  /*******************/
  return there_was_system_changed;
}



/** ESTADOS **/

void off_()
{
   turn_off_led_green();
   turn_off_led_red();

   if(SERIAL_DEBUG_ENABLED && there_was_system_changed){
   	Serial.println("Sistema apagado"); 
     there_was_system_changed=0;
     
   }
  
}

void warning_()
{
  turn_on_led_red();
  if(SERIAL_DEBUG_ENABLED && there_was_system_changed){
 	Serial.println("Sistema con en alarma o con errores"); 
   	there_was_system_changed=0;
  }

}

void status_check_()
{
  turn_off_led_red();
  turn_on_led_green();
  if(SERIAL_DEBUG_ENABLED && there_was_system_changed){
   Serial.println("Inicio de monitoreo de temperatura humedad y cantidad de agua"); 
   there_was_system_changed=0;
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

void get_event( )
{
	there_was_system_changed =0;
    if(check_button( ))
    {
       new_event = EV_BUTTON;
      return;
    }
    if(system_status == 1)
    {
       read_sensor_humidity( );

      if(sensors[SENSOR_HUMIDITY].current_value <= HUMIDITY_LOW)
      {
        Serial.println("Hay poca humedad. Regar");
        turn_on_led_red(); //Solo para probar la conexion del led
      //  new_event   = EV_NEED_WATER;
      //  return;
      }
      if(sensors[SENSOR_HUMIDITY].current_value < HUMIDITY_HIGH && sensors[SENSOR_HUMIDITY].current_value > HUMIDITY_LOW)
      {
        turn_off_led_red(); //Solo para probar la conexion del led
        Serial.println("humedad NORMAL");
      }

      if(sensors[SENSOR_HUMIDITY].current_value >= HUMIDITY_HIGH)
      {
        Serial.println("Hay mucha humedad");
      }


    /*si no se genero ningun evento nuevo*/

      new_event = EV_CONTROL;
    }else{
      new_event = EV_UNKNOW;
    }

}

void state_machine( )
{
  get_event( );
  if( (new_event >= 0) && (new_event < MAX_EVENTS) && (current_state >= 0) && (current_state < MAX_STATES) )
  {
    state_table[current_state][new_event]();
    DebugPrintStatus(states_s[current_state], events_s[new_event]);
    if( new_event_table[current_state][new_event]== ST_OFF )
    {
      	current_state = ST_OFF; 
        return;
    }
    if( new_event_table[current_state][new_event]== ST_STATUS_CHECK )
    {
      	current_state = ST_STATUS_CHECK; 
        return;
    }
    if( new_event_table[current_state][new_event]== ST_WATERING )
    {
      	current_state = ST_WATERING; 
        return;
    }
    if( new_event_table[current_state][new_event]== ST_WARNING )
    {
      	current_state = ST_WARNING; 
        return;
    }
   

  }else{
    	Serial.println("<<<<<<<<<<<<<<<<< OCCURIO UN ERROR CON EL EVENTO O ESTADO >>>>>>>>>>>>>>>>>>>>>");
  }

}



//-------FUNCIONES DE ARDUINO--------

void setup()
{
  do_init();
}

void loop()
{
	state_machine( );  

}