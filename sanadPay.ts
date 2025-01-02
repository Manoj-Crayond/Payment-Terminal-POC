import { NativeEventEmitter, NativeModules } from 'react-native';

const { SanadPay } = NativeModules;
const SanadPayEmitter = new NativeEventEmitter(SanadPay);

type TBroadcastResponse = {
    success: boolean;
    message: string;
};

type TTransactionData = {
    RTransactionAmount: string;
    RTransactionStatusCode: string;
    RTransactionStatusDescription: string;
    RAuthCode: string;
    RDate: string;
    RCardNo: string;
    RTerminalID: string;
};

const triggerSanadPay = (amount: string, transactionId: string, callback: (params: TBroadcastResponse) => void) => {
    try {
        SanadPay.sendBroadcastMessage(amount, transactionId, callback);
    } catch (error: any) {
        callback({ success: false, message: error?.message || '' });
    }
};

const receivingData = SanadPay.receivingData;

export { SanadPayEmitter, type TTransactionData, type TBroadcastResponse, triggerSanadPay, receivingData };
