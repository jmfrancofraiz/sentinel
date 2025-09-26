// Example script to set up user document with whitelist
// This shows how to structure the user document for the Cloud Functions to work

const admin = require('firebase-admin');

// Initialize Firebase Admin (you'll need to set up service account credentials)
// admin.initializeApp({
//   credential: admin.credential.applicationDefault(),
//   projectId: 'sentinel-85186'
// });

const db = admin.firestore();

// Example user document structure
const exampleUserDocument = {
  userId: 'user123',
  email: 'user@example.com',
  whitelist: [
    'John Doe',
    'Jane Smith',
    'Bob Wilson',
    'Family Group',
    'Work Team'
  ],
  createdAt: new Date().toISOString(),
  lastUpdated: new Date().toISOString()
};

// Example conversation documents that would trigger alerts
const exampleConversations = [
  {
    // This would trigger an alert - "Unknown Person" not in whitelist
    participants: 'John, Jane, Unknown Person',
    conversationType: 'group',
    group: 'Random Group',
    timestamp: '15/12/2024 14:30',
    sample: ['Hello everyone!', 'Who is this?']
  },
  {
    // This would NOT trigger an alert - all participants whitelisted
    participants: 'John Doe',
    conversationType: 'individual',
    timestamp: '15/12/2024 15:00',
    sample: ['Hi John!', 'How are you?']
  },
  {
    // This would trigger an alert - "Stranger" not in whitelist
    participants: 'John, Jane, Stranger',
    conversationType: 'group',
    group: 'New Group',
    timestamp: '15/12/2024 16:00',
    sample: ['Hello!', 'Nice to meet you']
  }
];

// Function to set up example user document
async function setupExampleUser() {
  try {
    await db.collection('whatsapp').doc('user123').set(exampleUserDocument);
    console.log('✅ Example user document created');
    
    // Add example conversations
    const conversationsRef = db.collection('whatsapp').doc('user123').collection('conversations');
    
    for (let i = 0; i < exampleConversations.length; i++) {
      await conversationsRef.add({
        ...exampleConversations[i],
        createdAt: Date.now(),
        deviceId: 'test_device',
        appVersion: '1.0',
        userId: 'user123',
        userEmail: 'user@example.com'
      });
      console.log(`✅ Example conversation ${i + 1} created`);
    }
    
    console.log('🎉 Example data setup complete!');
    console.log('Now check the Cloud Function logs to see the whitelist monitoring in action.');
    
  } catch (error) {
    console.error('❌ Error setting up example data:', error);
  }
}

// Uncomment to run the setup
// setupExampleUser();

module.exports = {
  exampleUserDocument,
  exampleConversations,
  setupExampleUser
};
