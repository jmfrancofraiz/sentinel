import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

// Initialize Firebase Admin SDK
admin.initializeApp();

const db = admin.firestore();

/**
 * Cloud Function that monitors interactions and checks participants against whitelist
 * Triggers on any write operation to the interactions subcollection
 */
export const monitorInteractions = functions.firestore
  .document('whatsapp/{userId}/conversations/{participant}/interactions/{interactionId}')
  .onWrite(async (change, context) => {
    const { userId, participant, interactionId } = context.params;
    
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
      
      // Get participants from the interaction
      const participants = interactionData.participants;
      
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
          participant,
          nonWhitelistedParticipants,
          allParticipants: participantList,
          whitelist,
          timestamp: new Date().toISOString(),
          conversationType: interactionData.conversationType || 'unknown',
          group: interactionData.group || null,
          sample: interactionData.sample || []
        });
        
        // Optional: Store the alert in the user's security_alerts subcollection
        await db.collection('whatsapp').doc(userId).collection('security_alerts').add({
          interactionId,
          userId,
          participant,
          nonWhitelistedParticipants,
          allParticipants: participantList,
          whitelist,
          timestamp: admin.firestore.FieldValue.serverTimestamp(),
          conversationType: interactionData.conversationType || 'unknown',
          group: interactionData.group || null,
          sample: interactionData.sample || [],
          alertType: 'non_whitelisted_participants'
        });
        
      } else {
        console.log(`✅ Interaction ${interactionId} participants are all whitelisted:`, {
          interactionId,
          userId,
          participant,
          participants: participantList,
          timestamp: new Date().toISOString()
        });
      }
      
    } catch (error) {
      console.error(`Error monitoring interaction ${interactionId}:`, error);
    }
  });

