// Test script to debug whitelist comparison
// This script creates a test conversation to trigger the Cloud Function

const admin = require('firebase-admin');

// Initialize Firebase Admin (you'll need to set up service account credentials)
// For testing, you can use the Firebase emulator or set up service account credentials
// admin.initializeApp({
//   credential: admin.credential.applicationDefault(),
//   projectId: 'sentinel-85186'
// });

const db = admin.firestore();

async function testWhitelistLogic() {
  console.log('🧪 Testing whitelist logic...');
  
  const testUserId = 'test-user-123';
  
  try {
    // 1. Create a test user document with whitelist
    const testUserDoc = {
      userId: testUserId,
      email: 'test@example.com',
      whitelist: [
        'John Doe',
        'Jane Smith',
        'Bob Wilson',
        'Family Group'
      ],
      createdAt: new Date().toISOString()
    };
    
    await db.collection('whatsapp').doc(testUserId).set(testUserDoc);
    console.log('✅ Test user document created with whitelist:', testUserDoc.whitelist);
    
    // 2. Create test conversations with different scenarios
    const testConversations = [
      {
        // Scenario 1: All participants whitelisted (should NOT trigger alert)
        participants: 'John Doe, Jane Smith',
        conversationType: 'group',
        group: 'Test Group 1',
        timestamp: new Date().toISOString(),
        sample: ['Hello everyone!']
      },
      {
        // Scenario 2: Mixed case (should NOT trigger alert)
        participants: 'john doe, JANE SMITH',
        conversationType: 'group', 
        group: 'Test Group 2',
        timestamp: new Date().toISOString(),
        sample: ['Hi there!']
      },
      {
        // Scenario 3: With spaces (should NOT trigger alert)
        participants: ' John Doe , Jane Smith ',
        conversationType: 'group',
        group: 'Test Group 3', 
        timestamp: new Date().toISOString(),
        sample: ['How are you?']
      },
      {
        // Scenario 4: One non-whitelisted participant (SHOULD trigger alert)
        participants: 'John Doe, Unknown Person',
        conversationType: 'group',
        group: 'Test Group 4',
        timestamp: new Date().toISOString(),
        sample: ['Who is this?']
      },
      {
        // Scenario 5: All non-whitelisted (SHOULD trigger alert)
        participants: 'Stranger 1, Stranger 2',
        conversationType: 'group',
        group: 'Test Group 5',
        timestamp: new Date().toISOString(),
        sample: ['Hello strangers!']
      }
    ];
    
    // 3. Create conversations to trigger the Cloud Function
    const conversationsRef = db.collection('whatsapp').doc(testUserId).collection('conversations');
    
    for (let i = 0; i < testConversations.length; i++) {
      const conversation = testConversations[i];
      console.log(`\n📝 Creating test conversation ${i + 1}:`);
      console.log(`   Participants: "${conversation.participants}"`);
      console.log(`   Expected: ${i < 3 ? 'Should be whitelisted' : 'Should trigger alert'}`);
      
      await conversationsRef.add({
        ...conversation,
        createdAt: Date.now(),
        deviceId: 'test_device',
        appVersion: '1.0',
        userId: testUserId,
        userEmail: 'test@example.com'
      });
      
      console.log(`   ✅ Conversation created`);
      
      // Wait a moment for the Cloud Function to process
      await new Promise(resolve => setTimeout(resolve, 2000));
    }
    
    console.log('\n🎉 Test conversations created!');
    console.log('📊 Check the Cloud Function logs to see the debug output:');
    console.log('   firebase functions:log --follow');
    console.log('\n🔍 Look for debug messages showing:');
    console.log('   - Debug - Whitelist: [...]');
    console.log('   - Debug - Normalized whitelist: [...]');
    console.log('   - Debug - Participants: [...]');
    console.log('   - Debug - Participant "..." is whitelisted: true/false');
    console.log('   - Debug - Non-whitelisted participants: [...]');
    
  } catch (error) {
    console.error('❌ Error during test:', error);
  }
}

// Uncomment to run the test
// testWhitelistLogic();

module.exports = { testWhitelistLogic };
