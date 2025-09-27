import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import { TelegramService } from './TelegramService';

// Initialize Firebase Admin SDK
admin.initializeApp();

const db = admin.firestore();

// Initialize Telegram service
const TELEGRAM_BOT_TOKEN = functions.config().telegram?.bot_token || process.env.TELEGRAM_BOT_TOKEN;
const TELEGRAM_CHAT_ID = functions.config().telegram?.chat_id || process.env.TELEGRAM_CHAT_ID;

let telegramService: TelegramService | null = null;

if (TELEGRAM_BOT_TOKEN && TELEGRAM_CHAT_ID) {
  telegramService = new TelegramService(TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID);
  console.log('Telegram service initialized successfully');
} else {
  console.warn('Telegram bot token or chat ID not configured. Telegram alerts will be disabled.');
}

/**
 * Cloud Function that monitors interactions and checks participants against whitelist
 * Triggers on any write operation to the interactions subcollection
 */
export const monitorInteractions = functions.firestore
  .document('whatsapp/{userId}/conversations/{participant}/interactions/{interactionId}')
  .onWrite(async (change, context) => {
    const userId = context.params.userId;
    const interactionId = context.params.interactionId;
    
    // Get the interaction data
    const interactionData = change.after.exists ? change.after.data() : null;
    
    if (!interactionData) {
      console.log(`Interaction ${interactionId} was deleted, skipping whitelist check`);
      return;
    }
    
    try {
      // Get the user document to access the whitelist
      const userDoc = await db.collection('whatsapp').doc(userId).get();
      
      if (!userDoc.exists) {
        console.log(`User document not found for userId: ${userId}`);
        return;
      }
      
      const userData = userDoc.data();
      const whitelist: string[] = userData?.whitelist || [];
      whitelist.push('Tú','You'); // Add yourself to the whitelist
      
      // Get participants from the interaction
      const participants: string = interactionData.participants;
      
      if (!participants) {
        console.log(`No participants field found in interaction ${interactionId}`);
        return;
      }
      
       // Parse participants (can be single name or comma-separated names)
       const participantList: string[] = [];      
       const normalizedParticipantList: string[] = participants
         .split(',')
         .map((name: string) => {
           participantList.push(name);
           // Remove invisible Unicode characters and normalize
           return name.replace(/[\u200B-\u200D\uFEFF\u200E\u200F]/g, '').trim().toLowerCase();
         }).filter((name: string) => name.length > 0);
       
       // Normalize whitelist for comparison (remove invisible characters, trim and convert to lowercase)
       const normalizedWhitelist = whitelist
         .map((name: any) => String(name).replace(/[\u200B-\u200D\uFEFF\u200E\u200F]/g, '').trim().toLowerCase())
         .filter((name: string) => name.length > 0);
      
      // Debug logging
      console.log(`Debug - Normalized whitelist:`, normalizedWhitelist);
      console.log(`Debug - Normalized Participants:`, normalizedParticipantList);
      
      // Check if any participant is not in the whitelist (case-insensitive comparison)
      const nonWhitelistedParticipants = normalizedParticipantList.filter((participant: string) => {
        const isWhitelisted = normalizedWhitelist.includes(participant);
        console.log(`Debug - Normalized participant "${participant}" is whitelisted: ${isWhitelisted}`);
        return !isWhitelisted;
      });
      
      console.log(`Debug - Non-whitelisted participants:`, nonWhitelistedParticipants);
      
      if (nonWhitelistedParticipants.length > 0) {
        console.log(`🚨 ALERT: Interaction ${interactionId} contains non-whitelisted participants:`, {
          interactionId,
          userId,
          participants,
          nonWhitelistedParticipants,
          allParticipants: participantList,
          whitelist,
          timestamp: new Date().toISOString(),
          conversationType: interactionData.conversationType || 'unknown',
          group: interactionData.group || null,
          sample: interactionData.sample || []
        });
        
        // Store the alert in the user's security_alerts subcollection
        await db.collection('whatsapp').doc(userId).collection('security_alerts').add({
          interactionId,
          userId,
          participants,
          nonWhitelistedParticipants,
          allParticipants: participantList,
          whitelist,
          timestamp: admin.firestore.FieldValue.serverTimestamp(),
          conversationType: interactionData.conversationType || 'unknown',
          group: interactionData.group || null,
          sample: interactionData.sample || [],
          alertType: 'non_whitelisted_participants'
        });
        
        // Send Telegram alert if service is available
        if (telegramService) {
          try {
            const alertData = {
              interactionId,
              participant: participants,
              nonWhitelistedParticipants,
              allParticipants: participantList,
              conversationType: interactionData.conversationType || 'unknown',
              group: interactionData.group || null,
              sample: interactionData.sample || [],
              timestamp: new Date().toISOString()
            };
            
            const telegramSuccess = await telegramService.sendSecurityAlert(alertData);
            if (telegramSuccess) {
              console.log('Telegram alert sent successfully');
            } else {
              console.error('Failed to send Telegram alert');
            }
          } catch (error) {
            console.error('Error sending Telegram alert:', error);
          }
        } else {
          console.warn('Telegram service not available - alert not sent to Telegram');
        }
        
      } else {
        console.log(`✅ Interaction ${interactionId} participants are all whitelisted:`, {
          interactionId,
          userId,
          participant: participants,
          participants: participantList,
          timestamp: new Date().toISOString()
        });
      }
      
    } catch (error) {
      console.error(`Error monitoring interaction ${interactionId}:`, error);
    }
  });


