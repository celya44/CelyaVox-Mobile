// Code à ajouter dans votre application web (https://freepbx17-dev.celya.fr/celyavox/)

import { registerPlugin } from '@capacitor/core';

// Définir l'interface du plugin
const CallEvents = registerPlugin('CallEvents');

// Écouter les événements d'appel accepté
CallEvents.addListener('callAnswered', (data) => {
    console.log('Appel accepté depuis le ConnectionService:', data.callId);
    
    // ICI : Ajoutez votre logique pour décrocher l'appel SIP
    // Par exemple, si vous utilisez SIP.js ou une autre librairie :
    // sipSession.accept();
    
    // Ou déclencher votre fonction de décrochage existante
    // answerCall();
});

// Écouter les événements d'appel rejeté
CallEvents.addListener('callRejected', (data) => {
    console.log('Appel rejeté depuis le ConnectionService:', data.callId);
    
    // ICI : Ajoutez votre logique pour rejeter l'appel SIP
    // sipSession.reject();
    
    // Ou déclencher votre fonction de rejet existante
    // rejectCall();
});

// Exemple d'utilisation avec SIP.js (si vous l'utilisez)
/*
let currentSipSession = null;

// Stocker la session quand un appel arrive
function onIncomingCall(session) {
    currentSipSession = session;
}

// Dans le listener callAnswered
CallEvents.addListener('callAnswered', (data) => {
    if (currentSipSession) {
        currentSipSession.accept().then(() => {
            console.log('Appel SIP accepté');
        }).catch(error => {
            console.error('Erreur lors de l\'acceptation de l\'appel:', error);
        });
    }
});

// Dans le listener callRejected
CallEvents.addListener('callRejected', (data) => {
    if (currentSipSession) {
        currentSipSession.reject();
        currentSipSession = null;
    }
});
*/
