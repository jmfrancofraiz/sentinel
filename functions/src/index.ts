import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';

// Initialize Firebase Admin SDK
admin.initializeApp();

const db = admin.firestore();

/**
 * Cloud Function that monitors conversations and checks participants against whitelist
 * Triggers on any write operation to the conversations subcollection
 */
export const monitorConversations = functions.firestore
  .document('whatsapp/{userId}/conversations/{conversationId}')
  .onWrite(async (change, context) => {
    const { userId, conversationId } = context.params;
    
    // Get the conversation data
    const conversationData = change.after.exists ? change.after.data() : null;
    
    if (!conversationData) {
      console.log(`Conversation ${conversationId} was deleted, skipping whitelist check`);
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
      const whitelist = userData?.whitelist || [];
      
      // Get participants from the conversation
      const participants = conversationData.participants;
      
      if (!participants) {
        console.log(`No participants field found in conversation ${conversationId}`);
        return;
      }
      
      // Parse participants (can be single name or comma-separated names)
      const participantList = participants
        .split(',')
        .map((name: string) => name.trim())
        .filter((name: string) => name.length > 0);
      
      // Normalize whitelist for comparison (trim and convert to lowercase)
      const normalizedWhitelist = whitelist
        .map((name: any) => String(name).trim().toLowerCase())
        .filter((name: string) => name.length > 0);
      
      // Debug logging
      console.log(`Debug - Original whitelist:`, whitelist);
      console.log(`Debug - Normalized whitelist:`, normalizedWhitelist);
      console.log(`Debug - Participants:`, participantList);
      
      // Check if any participant is not in the whitelist (case-insensitive comparison)
      const nonWhitelistedParticipants = participantList.filter((participant: string) => {
        const normalizedParticipant = participant.trim().toLowerCase();
        const isWhitelisted = normalizedWhitelist.includes(normalizedParticipant);
        console.log(`Debug - Participant "${participant}" (normalized: "${normalizedParticipant}") is whitelisted: ${isWhitelisted}`);
        return !isWhitelisted;
      });
      
      console.log(`Debug - Non-whitelisted participants:`, nonWhitelistedParticipants);
      
      if (nonWhitelistedParticipants.length > 0) {
        console.log(`🚨 ALERT: Conversation ${conversationId} contains non-whitelisted participants:`, {
          conversationId,
          userId,
          nonWhitelistedParticipants,
          allParticipants: participantList,
          whitelist,
          timestamp: new Date().toISOString(),
          conversationType: conversationData.conversationType || 'unknown',
          group: conversationData.group || null
        });
        
        // Optional: Store the alert in a separate collection for tracking
        await db.collection('security_alerts').add({
          conversationId,
          userId,
          nonWhitelistedParticipants,
          allParticipants: participantList,
          whitelist,
          timestamp: admin.firestore.FieldValue.serverTimestamp(),
          conversationType: conversationData.conversationType || 'unknown',
          group: conversationData.group || null,
          alertType: 'non_whitelisted_participants'
        });
        
      } else {
        console.log(`✅ Conversation ${conversationId} participants are all whitelisted:`, {
          conversationId,
          userId,
          participants: participantList,
          timestamp: new Date().toISOString()
        });
      }
      
    } catch (error) {
      console.error(`Error monitoring conversation ${conversationId}:`, error);
    }
  });

