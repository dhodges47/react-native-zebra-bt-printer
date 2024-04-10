
# react-native-zebra-bluetooth-printer

## Getting started

`$ yarn add https://github.com/dhodges47/react-native-zebra-bt-printer

## Acknowledgements

This is an alternative to /GeekyAnts/react-native-zebra-bluetooth-printer, which wasn't working with our devices.
This package is not as feature rich but works for our purposes. Additional features could be added if necessary.
There is no npm package.

## Usage

# AtechBluetoothModule

AtechBluetoothModule is a module for Bluetooth management and supports print functionality for zebra bluetooth printers ( only ble devices ). 
Sending data to paired device whose address is known:
```javascript
import { NativeModules } from "react-native"; // for Bluetooth support
const { AtechBluetoothModule } = NativeModules;
 public sendData(sDataStream: string): Promise<any> {
    return new Promise<any>(async (resolve, reject) => {
      /* if (Platform.OS == 'web') {
       reject('Printing not available on Windows');
       return;
     };  */
      if (this.MacAddress == null || this.MacAddress == '') {
        reject('Bluetooth device is not paired.')
      }
      try {
        let result = await AtechBluetoothModule.print(this.MacAddress, sDataStream);
        resolve(result);
      }
      catch (error: any) {

        if (Object.prototype.toString.call(error) === '[object Error]') {
          const typedError = error as Error;
          // console.log(typedError.name); // Access standard Error properties
          // console.log(typedError.message);
          // console.log(typedError.stack); // Note: stack is non-standard and may not be present in all environments
          reject(typedError.message);
        } else {
          reject(error);
        }
      }
    })
  }
```
Finding paired devices:
```
 const getPairedDevices = async () => {
        try {
            let arrayDevices: Array<string> = await AtechBluetoothModule.getPairedDevices();
            setDeviceArray(arrayDevices);
        }
        catch (error: any) {
            console.log('get paired devices promise rejected:', error.message);
        }
    }
```

  
