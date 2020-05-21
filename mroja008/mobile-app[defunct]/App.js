import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { Icon, Button, ButtonGroup, ThemeProvider, Header } from "react-native-elements";
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import Ionicons from 'react-native-vector-icons/Ionicons';

function CameraScreen() {
  var flashStatus = false;

  function openMenu() {
    console.log('This will open up the menu.');
    alert('This will open up the menu.');
  }

  function bluetoothConnection() {
    console.log('This will check for the bluetooth connection.');
    alert('This will check for the bluetooth connection.');
  }

  function moveUp() {
    console.log('Camera will move upwards.');
    alert('Camera will move upwards.');
  }

  function moveLeft() {
    console.log('Camera will move to the left.');
    alert('Camera will move to the left.');
  }

  function moveRight() {
    console.log('Camera will move to the right.');
    alert('Camera will move to the right.');
  }

  function moveDown() {
    console.log('Camera will move downwards.');
    alert('Camera will move downwards.');
  }

  function takePicture() {
    console.log('Camera will snap picture.');
    alert('Camera will snap picture.');
  }

  function flash() {
    if (flashStatus) {
      console.log('Flash will turn off.');
      alert('Flash will turn off.');
      flashStatus=false;
    }
    else {
      console.log('Flash will turn on.');
      alert('Flash will turn on.');
      flashStatus=true;
    }
  }

  return (
    <View style={styles.container}>
      <Header
        leftComponent=<Icon
          raised
          reverse
          name='bars'
          type='font-awesome'
          color='#000'
          onPress={openMenu} />
        centerComponent={{ text: 'Smart Tripod Camera', style: { fontSize:24, color: '#fff', fontWeight: 'bold' } }}
        rightComponent=<Icon
          raised
          reverse
          name='bluetooth'
          type='font-awesome'
          color='#000'
          onPress={bluetoothConnection}/>
        containerStyle={styles.title}
      />
      <View style={styles.camera}>
         <Text style={{fontSize:20, color:"#000"}}>Camera Area</Text>
      </View>
      <View style={styles.controls}>
        <View>
          <Button id="up-button"
            icon={
              <Icon
                raised
                reverse
                name="arrow-up"
                type='font-awesome'
                size={40}
                color='#000'
                onPress={moveUp}
              />
            }
            />
        </View>
        <View style={styles.fixToText}>
          <Button id="left-button"
              icon={
                <Icon
                  raised
                  reverse
                  name="arrow-left"
                  type='font-awesome'
                  size={40}
                  color='#000'
                  onPress={moveLeft}
                />
              }
              />
          <Button id="right-button"
            icon={
              <Icon
                raised
                reverse
                name="arrow-right"
                type='font-awesome'
                size={40}
                color='#000'
                onPress={moveRight}
              />
            }
            />
        </View>
        <View style={styles.fixToText}>
        <Button id="camera-button"
          icon={
            <Icon
              raised
              reverse
              name="camera"
              type='font-awesome'
              size={40}
              color='#32CD32'
              onPress={takePicture}
            />
          }
          />
          <Button id="down-button"
              icon={
                <Icon
                  raised
                  reverse
                  name="arrow-down"
                  type='font-awesome'
                  size={40}
                  onPress={moveDown}
                />
              }
              />
          <Button id="flash-button"
            icon={
              <Icon
                raised
                reverse
                name="bolt"
                type='font-awesome'
                size={40}
                color='#B22222'
                onPress={flash}
              />
            }
          />
        </View>
      </View>
    </View>
  );
}

function CarScreen() {

  function openMenu() {
    console.log('This will open up the menu.');
    alert('This will open up the menu.');
  }

  function bluetoothConnection() {
    console.log('This will check for the bluetooth connection.');
    alert('This will check for the bluetooth connection.');
  }

  function moveUp() {
    console.log('Car will move upwards.');
    alert('Car will move upwards.');
  }

  function moveLeft() {
    console.log('Car will move to the left.');
    alert('Car will move to the left.');
  }

  function moveRight() {
    console.log('Car will move to the right.');
    alert('Car will move to the right.');
  }

  function moveDown() {
    console.log('Car will move downwards.');
    alert('Car will move downwards.');
  }

  return (
    <View style={styles.container}>
      <Header
        leftComponent=<Icon
          raised
          reverse
          name='bars'
          type='font-awesome'
          color='#000'
          onPress={openMenu} />
        centerComponent={{ text: 'Smart Tripod Camera', style: { fontSize:24, color: '#fff', fontWeight: 'bold' } }}
        rightComponent=<Icon
          raised
          reverse
          name='bluetooth'
          type='font-awesome'
          color='#000'
          onPress={bluetoothConnection}/>
        containerStyle={styles.title}
      />
      <View style={styles.camera}>
         <Text style={{fontSize:20, color:"#000"}}>Camera Area</Text>
      </View>
      <View style={styles.controls}>
        <View>
          <Button id="up-button"
            icon={
              <Icon
                raised
                reverse
                name="arrow-up"
                type='font-awesome'
                size={40}
                color='#000'
                onPress={moveUp}
              />
            }
            />
        </View>
        <View style={styles.fixToText}>
          <Button id="left-button"
              icon={
                <Icon
                  raised
                  reverse
                  name="arrow-left"
                  type='font-awesome'
                  size={40}
                  color='#000'
                  onPress={moveLeft}
                />
              }
              />
          <Button id="right-button"
            icon={
              <Icon
                raised
                reverse
                name="arrow-right"
                type='font-awesome'
                size={40}
                color='#000'
                onPress={moveRight}
              />
            }
            />
        </View>
        <View style={styles.fixToText}>
          <Button id="down-button"
              icon={
                <Icon
                  raised
                  reverse
                  name="arrow-down"
                  type='font-awesome'
                  size={40}
                  onPress={moveDown}
                />
              }
              />
        </View>
      </View>
    </View>
  );
}

function LAScreen() {

  function openMenu() {
    console.log('This will open up the menu.');
    alert('This will open up the menu.');
  }

  function bluetoothConnection() {
    console.log('This will check for the bluetooth connection.');
    alert('This will check for the bluetooth connection.');
  }

  function moveLAUp() {
    console.log('Linear Actuator will move upwards.');
    alert('Linear Actuator will move upwards.');
  }

  function moveLADown() {
    console.log('Linear Actuator will move downwards.');
    alert('Linear Actuator will move downwards.');
  }

  return (
    <View style={styles.container}>
      <Header
        leftComponent=<Icon
          raised
          reverse
          name='bars'
          type='font-awesome'
          color='#000'
          onPress={openMenu} />
        centerComponent={{ text: 'Smart Tripod Camera', style: { fontSize:24, color: '#fff', fontWeight: 'bold' } }}
        rightComponent=<Icon
          raised
          reverse
          name='bluetooth'
          type='font-awesome'
          color='#000'
          onPress={bluetoothConnection}/>
        containerStyle={styles.title}
      />
      <View style={styles.camera}>
         <Text style={{fontSize:20, color:"#000"}}>Camera Area</Text>
      </View>
      <View style={styles.controls}>
        <View>
          <Button id="up-button"
            icon={
              <Icon
                raised
                reverse
                name="arrow-up"
                type='font-awesome'
                size={40}
                color='#000'
                onPress={moveLAUp}
              />
            }
            />
        </View>
        <View style={styles.fixToText}>
          <Button id="down-button"
              icon={
                <Icon
                  raised
                  reverse
                  name="arrow-down"
                  type='font-awesome'
                  size={40}
                  onPress={moveLADown}
                />
              }
              />
        </View>
      </View>
    </View>
  );
}

const Tab = createBottomTabNavigator();

export default function App() {
  return (
   <NavigationContainer>
     <Tab.Navigator
       barStyle={{ backgroundColor: '#694fad' }}
       screenOptions={({ route }) => ({
         tabBarIcon: ({ focused, color, size }) => {
           let iconName;

           if (route.name === 'Camera') {
             iconName = focused ? 'ios-camera' : 'ios-camera';
           } else if (route.name === 'Car') {
             iconName = focused ? 'ios-car' : 'ios-car';
           } else if (route.name === 'Height') {
             iconName = focused ? 'ios-resize' : 'ios-resize';
           }

           // You can return any component that you like here!
           return <Ionicons name={iconName} size={size} color={color} />;
         },
       })}
       tabBarOptions={{
         activeTintColor: 'blue',
         inactiveTintColor: 'gray',
       }}
     >
       <Tab.Screen name="Camera" component={CameraScreen} />
       <Tab.Screen name="Car" component={CarScreen} />
       <Tab.Screen name="Height" component={LAScreen} />
     </Tab.Navigator>
   </NavigationContainer>
 );
}

const styles = StyleSheet.create({
  container: {
    flex:1,
    backgroundColor: '#000',
    borderBottomColor: 'black',
  },
  title: {
   flex:0.15,
   backgroundColor:"#000",
   position: "relative",
   borderBottomColor: 'black',
  },
  camera: {
   backgroundColor:"#f3e9d2",
   flex:1,
   justifyContent: "center",
   alignItems: "center",
  },
  controls: {
   backgroundColor:"rgb(32, 137, 220)",
   flex:1,
   alignItems: "center",
 },
  fixToText: {
    backgroundColor:"rgb(32, 137, 220)",
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
});
