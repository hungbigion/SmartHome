#include <FS.h>
#include <WiFiManager.h>
#include <ArduinoJson.h>
#include <FirebaseESP8266.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <Servo.h>
#include <EEPROM.h>

#define API_KEY "AIzaSyD6ewByFq4uz05WEAJrUy2mB2V4bN4fSas"
#define DATABASE_URL "https://smart-home-android-studio-default-rtdb.asia-southeast1.firebasedatabase.app"
#define DATABASE_SECRET "PnU3en5BtSR0DVkc1gFx0uQjHhgUbLYa1EHVBUcE"

char uid[29];
bool shouldSaveConfig = false;
bool wifiConnected = false;
unsigned long previousMillis = 0;
const unsigned long interval = 1000;
volatile bool dataChanged = false;
String childPath[4] = {"/relay1", "/relay2", "/relay3", "/servo"};

WiFiManager wm;
WiFiManagerParameter custom_uid("uid", "", uid, sizeof(uid));
WiFiManagerParameter custom_js("<script>uid=document.getElementById('uid');uid.value=window.location.search.replace('?uid=','');uid.style.display='none';document.getElementsByTagName('br')[2].replaceWith('');document.getElementsByTagName('br')[2].replaceWith('');</script>");

WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "pool.ntp.org");

FirebaseData fbdo;
FirebaseData stream;
FirebaseAuth auth;
FirebaseConfig config;

Servo servo;

const int button1 = D1;
const int button2 = D2;
const int button3 = D5;
const int button4 = D6;
const int button5 = D7;

const int relay1 = D0;
const int relay2 = D4;
const int relay3 = D8;

const int servoPin = D3;

int servoValue = 0;
int relay1Value = 0;
int relay2Value = 0;
int relay3Value = 0;
String relay1Type = "toggle";
String relay2Type = "toggle";
String relay3Type = "toggle";

int servoValuePrevios = 0;
int relay1ValuePrevios = 0;
int relay2ValuePrevios = 0;
int relay3ValuePrevios = 0;
String relay1TypePrevios = "toggle";
String relay2TypePrevios = "toggle";
String relay3TypePrevios = "toggle";

bool servoAvailable = false;
bool relay1Available = false;
bool relay2Available = false;
bool relay3Available = false;


void setup() {
  Serial.begin(115200);

  setupWifiManager();

  pinMode(button1, INPUT);
  pinMode(button2, INPUT);
  pinMode(button3, INPUT);
  pinMode(button4, INPUT);
  pinMode(button5, INPUT);

  pinMode(relay1, OUTPUT);
  pinMode(relay2, OUTPUT);
  pinMode(relay3, OUTPUT);

  servo.attach(servoPin);

  timeClient.begin();
}

void loop() {
  wm.process();

  if (digitalRead(button1)) {
    delay(20);
    if (digitalRead(button1)) {

      SPIFFS.format();
      
      wm.resetSettings();
      ESP.restart();

      Serial.println("Reseting and Restart");

    }
    while (digitalRead(button1));
  }

  if (digitalRead(button2)) {
    delay(20);
    if (digitalRead(button2)) {

      if (servoValue == 1) {
        servoValue = 0;
        Serial.println("Servo is 1");
      } else {
        servoValue = 1;
        Serial.println("Servo is 0");
      }

    }
    while (digitalRead(button2));
  }

  if (servoValue != servoValuePrevios) {
    if (servoValue == 1)
      servo.write(0);
    else
      servo.write(120);
    if (servoAvailable)
      Firebase.setIntAsync(fbdo, "/users/" + String(uid) + "/devices/servo/status", servoValue);
    servoValuePrevios = servoValue;
  }
  
  if (digitalRead(button3)) {
    delay(20);
    if (digitalRead(button3)) {

      if (strcmp(relay1Type.c_str(), "toggle") == 0) {
        if (relay1Value == 1) {
          relay1Value = 0;
          Serial.println("Relay 1 is ON");
        } else {
          relay1Value = 1;
          Serial.println("Relay 1 is OFF");
        }
      } else if (strcmp(relay1Type.c_str(), "adjust") == 0) {
        if (relay1Value >= 0 && relay1Value < 25)
          relay1Value = 25;
        else if (relay1Value >= 25 && relay1Value < 50)
          relay1Value = 50;
        else if (relay1Value >= 50 && relay1Value < 75)
          relay1Value = 75;
        else if (relay1Value >= 75 && relay1Value < 100)
          relay1Value = 100;
        else
          relay1Value = 0;
        if (relay1Available) {
          Firebase.setIntAsync(fbdo, "/users/" + String(uid) + "/devices/relay1/speed", relay1Value);
          Firebase.setIntAsync(fbdo, "/users/" + String(uid) + "/devices/relay1/status", relay1Value);
        }
      }

    }
    while (digitalRead(button3));
  }

  if (strcmp(relay1Type.c_str(), relay1TypePrevios.c_str()) != 0 || relay1Value != relay1ValuePrevios) {
    if (strcmp(relay1Type.c_str(), "toggle") == 0) {
      digitalWrite(relay1, relay1Value);
      if (relay1Available)
        Firebase.setIntAsync(fbdo, "/users/" + String(uid) + "/devices/relay1/status", relay1Value);
      relay1ValuePrevios = relay1Value;
    } else if (strcmp(relay1Type.c_str(), "adjust") == 0) {
      analogWrite(relay1, relay1Value * 2.55);
      relay1ValuePrevios = relay1Value;
    }
  }
  
  if (digitalRead(button4)) {
    delay(20);
    if (digitalRead(button4)) {

      if (strcmp(relay2Type.c_str(), "toggle") == 0) {
        if (relay2Value == 1) {
          relay2Value = 0;
          Serial.println("Relay 2 is ON");
        } else {
          relay2Value = 1;
          Serial.println("Relay 2 is OFF");
        }
      } else if (strcmp(relay2Type.c_str(), "adjust") == 0) {
        if (relay2Value >= 0 && relay2Value < 25)
          relay2Value = 25;
        else if (relay2Value >= 25 && relay2Value < 50)
          relay2Value = 50;
        else if (relay2Value >= 50 && relay2Value < 75)
          relay2Value = 75;
        else if (relay2Value >= 75 && relay2Value < 100)
          relay2Value = 100;
        else
          relay2Value = 0;
        if (relay2Available) {
          Firebase.setIntAsync(fbdo, "/users/" + String(uid) + "/devices/relay2/speed", relay2Value);
          Firebase.setIntAsync(fbdo, "/users/" + String(uid) + "/devices/relay2/status", relay2Value);
        }
      }

    }
    while (digitalRead(button4));
  }

  if (strcmp(relay2Type.c_str(), relay2TypePrevios.c_str()) != 0 || relay2Value != relay2ValuePrevios) {
    if (strcmp(relay2Type.c_str(), "toggle") == 0) {
      digitalWrite(relay2, relay2Value);
      if (relay2Available)
        Firebase.setIntAsync(fbdo, "/users/" + String(uid) + "/devices/relay2/status", relay2Value);
      relay2ValuePrevios = relay2Value;
    } else if (strcmp(relay2Type.c_str(), "adjust") == 0) {
      analogWrite(relay2, relay2Value * 2.55);
      relay2ValuePrevios = relay2Value;
    }
  }
  
  if (digitalRead(button5)) {
    delay(20);
    if (digitalRead(button5)) {

      if (strcmp(relay3Type.c_str(), "toggle") == 0) {
        if (relay3Value == 1) {
          relay3Value = 0;
          Serial.println("Relay 3 is ON");
        } else {
          relay3Value = 1;
          Serial.println("Relay 3 is OFF");
        }
      } else if (strcmp(relay3Type.c_str(), "adjust") == 0) {
        if (relay3Value >= 0 && relay3Value < 25)
          relay3Value = 25;
        else if (relay3Value >= 25 && relay3Value < 50)
          relay3Value = 50;
        else if (relay3Value >= 50 && relay3Value < 75)
          relay3Value = 75;
        else if (relay3Value >= 75 && relay3Value < 100)
          relay3Value = 100;
        else
          relay3Value = 0;
        if (relay3Available) {
          Firebase.setIntAsync(fbdo, "/users/" + String(uid) + "/devices/relay3/speed", relay3Value);
          Firebase.setIntAsync(fbdo, "/users/" + String(uid) + "/devices/relay3/status", relay3Value);
        }
      }
      
    }
    while (digitalRead(button5));
  }

  if (strcmp(relay3Type.c_str(), relay3TypePrevios.c_str()) != 0 || relay3Value != relay3ValuePrevios) {
    if (strcmp(relay3Type.c_str(), "toggle") == 0) {
      digitalWrite(relay3, relay3Value);
      if (relay3Available)
        Firebase.setIntAsync(fbdo, "/users/" + String(uid) + "/devices/relay3/status", relay3Value);
      relay3ValuePrevios = relay3Value;
    } else if (strcmp(relay3Type.c_str(), "adjust") == 0) {
      analogWrite(relay3, relay3Value * 2.55);
      relay3ValuePrevios = relay3Value;
    }
  }

  bool currentWifiConnected = (WiFi.status() == WL_CONNECTED);
  if (currentWifiConnected != wifiConnected) {
    wifiConnected = currentWifiConnected;
    if (!wifiConnected) {
      return;
    }
  }
  
  timeClient.update();
  long unixTime = timeClient.getEpochTime();
  unsigned long currentMillis = millis();

  if (currentMillis - previousMillis >= interval) {
    previousMillis = currentMillis;
    Firebase.setIntAsync(fbdo, "/users/" + String(uid) + "/time", unixTime);
  }

  if (dataChanged)
    dataChanged = false;
  if (!stream.httpConnected()) {}
}

void streamCallback(MultiPathStreamData stream) {
  size_t numChild = sizeof(childPath) / sizeof(childPath[0]);

  for (size_t i = 0; i < numChild; i++) {
    if (stream.get(childPath[i])) {
      if (strcmp(stream.dataPath.c_str(), "/servo/status") == 0) {
        servoValue = atoi(stream.value.c_str());
      }
      if (strcmp(stream.dataPath.c_str(), "/relay1/status") == 0) {
        relay1Value = atoi(stream.value.c_str());
      }
      if (strcmp(stream.dataPath.c_str(), "/relay2/status") == 0) {
        if (relay2ValuePrevios != atoi(stream.value.c_str()))
          relay2Value = atoi(stream.value.c_str());
      }
      if (strcmp(stream.dataPath.c_str(), "/relay3/status") == 0) {
        relay3Value = atoi(stream.value.c_str());
      }
      if (strcmp(stream.dataPath.c_str(), "/servo") == 0) {
        if (strcmp(stream.value.c_str(), "null") == 0)
          servoAvailable = false;
        else
          servoAvailable = true;
        DynamicJsonDocument jsonBuffer(512);
        deserializeJson(jsonBuffer, stream.value.c_str());
        String value = jsonBuffer["status"];
        servoValue = atoi(value.c_str());
      }
      if (strcmp(stream.dataPath.c_str(), "/relay1") == 0) {
        if (strcmp(stream.value.c_str(), "null") == 0)
          relay1Available = false;
        else
          relay1Available = true;
        DynamicJsonDocument jsonBuffer(512);
        deserializeJson(jsonBuffer, stream.value.c_str());
        String type = jsonBuffer["type"];
        String value = jsonBuffer["status"];
        relay1Type = type;
        relay1Value = atoi(value.c_str());
      }
      if (strcmp(stream.dataPath.c_str(), "/relay2") == 0) {
        if (strcmp(stream.value.c_str(), "null") == 0)
          relay2Available = false;
        else
          relay2Available = true;
        DynamicJsonDocument jsonBuffer(512);
        deserializeJson(jsonBuffer, stream.value.c_str());
        String type = jsonBuffer["type"];
        String value = jsonBuffer["status"];
        relay2Type = type;
        relay2Value = atoi(value.c_str());
      }
      if (strcmp(stream.dataPath.c_str(), "/relay3") == 0) {
        if (strcmp(stream.value.c_str(), "null") == 0)
          relay3Available = false;
        else
          relay3Available = true;
        DynamicJsonDocument jsonBuffer(512);
        deserializeJson(jsonBuffer, stream.value.c_str());
        String type = jsonBuffer["type"];
        String value = jsonBuffer["status"];
        relay3Type = type;
        relay3Value = atoi(value.c_str());
      }
      Serial.printf("path: %s, value: %s%s", stream.dataPath.c_str(), stream.value.c_str(), i < numChild - 1 ? "\n" : "");
    } 
  }
  
  dataChanged = true;
}

void streamTimeoutCallback(bool timeout) {
  if (timeout)
    Serial.println("stream timed out, resuming...\n");
  if (!stream.httpConnected())
    Serial.printf("error code: %d, reason: %s\n\n", stream.httpCode(), stream.errorReason().c_str());
}

void saveConfigCallback () {
  Serial.println("Should save config");
  shouldSaveConfig = true;
  Serial.println("saving config");

  strcpy(uid, custom_uid.getValue());
  
  DynamicJsonDocument json(1024);
  json["uid"] = uid;
  File configFile = SPIFFS.open("/config.json", "w");
  if (!configFile)
    Serial.println("failed to open config file for writing");
  serializeJson(json, Serial);
  serializeJson(json, configFile);
  configFile.close();

  setupFirebase();
  Serial.println("Setup Firebase!");
}

void setupWifiManager() {
  Serial.println();
  Serial.println("mounting FS...");
  if (SPIFFS.begin()) {
    Serial.println("mounted file system");
    if (SPIFFS.exists("/config.json")) {
      Serial.println("reading config file");
      File configFile = SPIFFS.open("/config.json", "r");
      if (configFile) {
        Serial.println("opened config file");
        size_t size = configFile.size();
        std::unique_ptr<char[]> buf(new char[size]);
        configFile.readBytes(buf.get(), size);
        DynamicJsonDocument json(1024);
        auto deserializeError = deserializeJson(json, buf.get());
        serializeJson(json, Serial);
        if (!deserializeError) {
          Serial.println("\nparsed json");
          strcpy(uid, json["uid"]);
        } else {
          Serial.println("failed to load json config");
        }
        configFile.close();
      }
    }
  } else {
    Serial.println("failed to mount FS");
  }

  wm.addParameter(&custom_uid);
  wm.addParameter(&custom_js);
  wm.setConfigPortalBlocking(false);
  wm.setSaveConfigCallback(saveConfigCallback);
  if (wm.autoConnect("Smart Home", "88888888")) {
    Serial.println("Wifi Connected");
    setupFirebase();
    Serial.println("Setup Firebase");
  } else
    Serial.println("Configportal running");
}

void setupFirebase() {
  config.api_key = API_KEY;
  config.database_url = DATABASE_URL;
  config.signer.tokens.legacy_token = DATABASE_SECRET;

  Firebase.begin(&config, &auth);

  Firebase.reconnectWiFi(true);

  Firebase.setStringAsync(fbdo, "/users/" + String(uid) + "/status", "connected");

  stream.setBSSLBufferSize(2048 /* Rx in bytes, 512 - 16384 */, 512 /* Tx in bytes, 512 - 16384 */);

  String parentPath = "/users/" + String(uid) + "/devices";
  if (!Firebase.beginMultiPathStream(stream, parentPath))
    Serial.printf("stream begin error, %s\n\n", stream.errorReason().c_str());

  Firebase.setMultiPathStreamCallback(stream, streamCallback, streamTimeoutCallback);
}
