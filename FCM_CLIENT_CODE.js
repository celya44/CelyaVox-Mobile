import { PushNotifications } from '@capacitor/push-notifications';
import { registerPlugin } from '@capacitor/core';

// Enregistrer le plugin CallEvents
const CallEvents = registerPlugin('CallEvents');

const addListeners = async () => {
    await PushNotifications.addListener('registration', token => {
        console.info('Registration token: ', token.value);
        // Send token.value to your server here
    });

    await PushNotifications.addListener('registrationError', err => {
        console.error('Registration error: ', err.error);
    });

    await PushNotifications.addListener('pushNotificationReceived', notification => {
        console.log('Push notification received: ', notification);
    });

    await PushNotifications.addListener('pushNotificationActionPerformed', notification => {
        console.log('Push notification action performed', notification.actionId, notification.inputValue);
    });
    
    // Écouter les événements d'appel du ConnectionService
    await CallEvents.addListener('callAnswered', data => {
        console.log('🟢 APPEL ACCEPTÉ depuis ConnectionService:', data);
        // ICI : Appelez votre fonction pour décrocher l'appel SIP réel
        // Exemple: window.answerSipCall(data.callId);
    });
    
    await CallEvents.addListener('callRejected', data => {
        console.log('🔴 APPEL REJETÉ depuis ConnectionService:', data);
        // ICI : Appelez votre fonction pour rejeter l'appel SIP réel
        // Exemple: window.rejectSipCall(data.callId);
    });
}

const registerNotifications = async () => {
    let permStatus = await PushNotifications.checkPermissions();

    if (permStatus.receive === 'prompt') {
        permStatus = await PushNotifications.requestPermissions();
    }

    if (permStatus.receive !== 'granted') {
        throw new Error('User denied permissions!');
    }

    await PushNotifications.register();
}

// Call this when the app starts or when you want to request permission
// await addListeners();
// await registerNotifications();
