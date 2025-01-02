/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React, {useEffect, useState} from 'react';
import {
  Alert,
  Button,
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TextInput,
  useColorScheme,
  View,
} from 'react-native';
import uuid from 'react-native-uuid';
import {Colors, Header} from 'react-native/Libraries/NewAppScreen';
import {SanadPayEmitter, triggerSanadPay, receivingData} from './sanadPay';

type TTransactionData = {
  RTransactionAmount: string;
  RTransactionStatusCode: string;
  RTransactionStatusDescription: string;
  RAuthCode: string;
  RDate: string;
  RCardNo: string;
  RTerminalID: string;
};

function App(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const [transactionId, setTransactionId] = useState<string>('');
  const [transactionData, setTransactionData] =
    useState<TTransactionData | null>(null);
  const [amount, setAmount] = useState<string>('');

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  useEffect(() => {
    const listener = SanadPayEmitter.addListener(
      'ReceivingData',
      (data: TTransactionData | null) => {
        console.log(data);
        setTransactionData(data);
      },
    );

    return () => {
      listener.remove();
    };
  }, []);

  const onPay = () => {
    const id = uuid.v4();
    setTransactionId(id);

    triggerSanadPay(amount, id, ({success, message}) => {
      if (success) {
        Alert.alert('Transaction Successfull');
      } else {
        Alert.alert(message || 'Transaction Failed');
      }
    });
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        <Header />
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
            ...styles.container,
          }}>
          <View style={styles.inputContainer}>
            <Text style={styles.label}>Enter your Amount:</Text>
            <TextInput
              style={styles.input}
              placeholder="10.00"
              value={amount}
              onChangeText={setAmount}
              keyboardType="decimal-pad"
            />
            <Button title="Send" onPress={onPay} disabled={!amount}/>
            <Button title="Receive" onPress={()=>receivingData()}/>
          </View>
          <Text>Transaction Details:</Text>
          <Text>Transaction Id: {transactionId || ''}</Text>
          <Text>Amount: {transactionData?.RTransactionAmount || ''}</Text>
          <Text>
            Status Code: {transactionData?.RTransactionStatusCode || ''}
          </Text>
          <Text>
            Description: {transactionData?.RTransactionStatusDescription || ''}
          </Text>
          <Text>Auth Code: {transactionData?.RAuthCode || ''}</Text>
          <Text>Date: {transactionData?.RDate || ''}</Text>
          <Text>Card No: {transactionData?.RCardNo || ''}</Text>
          <Text>Terminal ID: {transactionData?.RTerminalID || ''}</Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 24,
  },
  inputContainer: {
    paddingVertical: 20,
  },
  label: {
    fontSize: 16,
    marginBottom: 8,
  },
  input: {
    height: 64,
    borderColor: 'gray',
    borderWidth: 1,
    borderRadius: 8,
    padding: 8,
    marginBottom: 12,
  },
});

export default App;
