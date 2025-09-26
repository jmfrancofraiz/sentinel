# Firebase Cloud Functions Setup for WhatsApp Sentinel

This guide explains how to set up and deploy Firebase Cloud Functions to monitor conversations and check participants against a whitelist.

## 🔧 Prerequisites

1. **Firebase CLI installed**:
   ```bash
   npm install -g firebase-tools
   ```

2. **Firebase project configured** (already done for this project):
   - Project ID: `sentinel-85186`
   - Firestore database enabled
   - Authentication enabled

3. **Node.js 18+** (required for Cloud Functions)

## 📁 Project Structure

```
WhatsAppSentinel/
├── functions/
│   ├── src/
│   │   └── index.ts          # Cloud Functions code
│   ├── package.json          # Dependencies
│   └── tsconfig.json         # TypeScript configuration
├── firebase.json             # Firebase configuration
├── .firebaserc              # Firebase project settings
└── firestore.indexes.json   # Firestore indexes
```

## 🚀 Deployment Instructions

### Step 1: Install Dependencies

```bash
cd functions
npm install
```

### Step 2: Build the Functions

```bash
npm run build
```

### Step 3: Deploy to Firebase

```bash
# From the project root directory
firebase deploy --only functions
```

### Step 4: Verify Deployment

```bash
# Check function logs
firebase functions:log

# List deployed functions
firebase functions:list
```

## 🔍 How It Works

### Data Structure

The Cloud Function monitors conversations stored in this structure:
```
whatsapp/
├── {userId}/
│   ├── whitelist: ["John Doe", "Jane Smith", "Bob Wilson"]
│   └── conversations/
│       ├── {conversationId1}
│       │   ├── participants: "John Doe"  # Single participant
│       │   ├── conversationType: "individual"
│       │   └── ...
│       └── {conversationId2}
│           ├── participants: "John, Jane, Unknown Person"  # Multiple participants
│           ├── conversationType: "group"
│           └── ...
```

### Function Behavior

1. **Triggers**: The function triggers on any write operation to `whatsapp/{userId}/conversations/{conversationId}`

2. **Participant Parsing**: 
   - Single participant: `"John Doe"` → `["John Doe"]`
   - Multiple participants: `"John, Jane, Bob"` → `["John", "Jane", "Bob"]`

3. **Whitelist Check**: Compares each participant against the user's whitelist array

4. **Logging**: 
   - ✅ **Whitelisted**: Logs success message
   - 🚨 **Non-whitelisted**: Logs alert with details

5. **Alert Storage**: Non-whitelisted conversations are stored in `security_alerts` collection

## 📊 Available Functions

### 1. `monitorConversations` (Automatic)
- **Trigger**: Firestore document write
- **Purpose**: Monitors all conversation writes in real-time
- **Logs**: Console logs and security alerts collection

### 2. `checkAllConversations` (Manual)
- **Trigger**: HTTP callable function
- **Purpose**: Check all existing conversations for a user
- **Usage**: Call from client app or Firebase Console

```javascript
// Example usage in client app
const checkAllConversations = firebase.functions().httpsCallable('checkAllConversations');
const result = await checkAllConversations({ userId: 'user123' });
console.log(result.data);
```

### 3. `dailyWhitelistCheck` (Optional)
- **Trigger**: Scheduled (daily at 9 AM UTC)
- **Purpose**: Daily automated check of all users
- **Status**: Commented out by default

## 🔧 Configuration

### User Whitelist Setup

Each user document should have a `whitelist` field:

```javascript
// Example user document structure
{
  "userId": "user123",
  "email": "user@example.com",
  "whitelist": [
    "John Doe",
    "Jane Smith", 
    "Bob Wilson",
    "Family Group"
  ],
  "createdAt": 1702642200000
}
```

### Security Alerts Collection

Non-whitelisted conversations are logged to `security_alerts`:

```javascript
{
  "conversationId": "conv123",
  "userId": "user123",
  "nonWhitelistedParticipants": ["Unknown Person"],
  "allParticipants": ["John", "Jane", "Unknown Person"],
  "whitelist": ["John", "Jane", "Bob"],
  "timestamp": "2024-12-15T14:30:00Z",
  "conversationType": "group",
  "group": "Family Group",
  "alertType": "non_whitelisted_participants"
}
```

## 📈 Monitoring and Logs

### View Function Logs

```bash
# Real-time logs
firebase functions:log --follow

# Filter by function
firebase functions:log --only monitorConversations

# Filter by severity
firebase functions:log --only functions:monitorConversations --severity ERROR
```

### Firebase Console Monitoring

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project (`sentinel-85186`)
3. Navigate to **Functions** tab
4. View function metrics, logs, and performance

### Security Alerts Dashboard

1. Go to **Firestore Database**
2. Navigate to `security_alerts` collection
3. View all non-whitelisted conversation alerts

## 🛠️ Development and Testing

### Local Development

```bash
# Start Firebase emulators
firebase emulators:start --only functions,firestore

# In another terminal, test functions
npm run serve
```

### Testing Functions

```bash
# Test specific function
firebase functions:shell

# In the shell:
monitorConversations({...})
checkAllConversations({...})
```

## 🔒 Security Considerations

### Firestore Security Rules

The functions work with the existing security rules in `firestore_security_rules.txt`:

```javascript
// Users can only access their own data
match /whatsapp/{userId} {
  allow read, write: if request.auth != null && request.auth.uid == userId;
  
  match /conversations/{conversationId} {
    allow read, write: if request.auth != null && request.auth.uid == userId;
  }
}
```

### Function Permissions

- Functions run with **Firebase Admin SDK** privileges
- Can read/write any document in the project
- No additional IAM configuration needed

## 🚨 Troubleshooting

### Common Issues

1. **Function not triggering**:
   - Check Firestore security rules
   - Verify function deployment
   - Check function logs for errors

2. **Whitelist not found**:
   - Ensure user document has `whitelist` field
   - Check user document structure

3. **Participants not parsed correctly**:
   - Verify participants field format
   - Check for special characters in names

4. **Deployment failures**:
   - Check Node.js version (requires 18+)
   - Verify Firebase CLI is logged in
   - Check project permissions

### Debug Commands

```bash
# Check function status
firebase functions:list

# View detailed logs
firebase functions:log --only functions:monitorConversations --limit 50

# Test function locally
firebase emulators:start --only functions
```

## 📝 Example Usage Scenarios

### Scenario 1: Individual Chat
- **Participant**: `"John Doe"`
- **Whitelist**: `["John Doe", "Jane Smith"]`
- **Result**: ✅ Allowed (whitelisted)

### Scenario 2: Group Chat with Unknown
- **Participants**: `"John, Jane, Unknown Person"`
- **Whitelist**: `["John", "Jane", "Bob"]`
- **Result**: 🚨 Alert (Unknown Person not whitelisted)

### Scenario 3: Family Group
- **Participants**: `"Mom, Dad, Sister"`
- **Whitelist**: `["Mom", "Dad", "Sister", "Brother"]`
- **Result**: ✅ Allowed (all whitelisted)

## 🔄 Updates and Maintenance

### Updating Functions

```bash
# Make changes to functions/src/index.ts
npm run build
firebase deploy --only functions
```

### Adding New Features

1. Modify `functions/src/index.ts`
2. Update `package.json` if new dependencies needed
3. Test locally with emulators
4. Deploy to Firebase

### Monitoring Performance

- Check function execution time in Firebase Console
- Monitor memory usage and cold starts
- Set up alerts for function failures

## 📞 Support

For issues with Cloud Functions:

1. Check Firebase Console for error details
2. Review function logs for specific errors
3. Verify Firestore security rules
4. Test with Firebase emulators locally

## 🎯 Next Steps

1. **Deploy the functions** using the instructions above
2. **Set up user whitelists** in Firestore
3. **Monitor the logs** to see function activity
4. **Configure alerts** for non-whitelisted participants
5. **Consider adding** scheduled checks or additional monitoring

The Cloud Functions will now automatically monitor all conversation writes and alert you when non-whitelisted participants are detected!
