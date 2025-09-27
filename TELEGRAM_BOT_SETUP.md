# Telegram Bot Setup for WhatsApp Sentinel

This guide explains how to set up the Telegram bot integration for WhatsApp Sentinel alerts.

## Prerequisites

1. A Telegram account
2. Access to the @elsa_spy_bot bot
3. Firebase Functions deployment access

## Step 1: Get Bot Token

1. Contact the bot administrator to get the bot token for @elsa_spy_bot
2. The bot token should look like: `123456789:ABCdefGHIjklMNOpqrsTUVwxyz`

## Step 2: Get Group Chat ID

To get the group chat ID for @elsa_spy_bot:

### Method 1: Using getUpdates API
1. Add @elsa_spy_bot to your group (if not already added)
2. Send a message in the group mentioning the bot: `@elsa_spy_bot hello`
3. Visit: `https://api.telegram.org/bot<BOT_TOKEN>/getUpdates`
4. Look for the `chat.id` value in the response
5. Group chat IDs are negative numbers like: `-123456789`

### Method 2: Using @userinfobot
1. Add @userinfobot to your group
2. Send any message in the group
3. @userinfobot will reply with the group's chat ID
4. Remove @userinfobot from the group after getting the ID

### Method 3: Using @raw_data_bot
1. Add @raw_data_bot to your group
2. Send any message in the group
3. @raw_data_bot will show the raw data including the chat ID
4. Remove @raw_data_bot from the group after getting the ID

**Note:** Group chat IDs are always negative numbers (e.g., `-123456789`)

## Step 3: Configure Firebase Functions

### Option A: Using Firebase Functions Config (Recommended)

```bash
# Set the bot token
firebase functions:config:set telegram.bot_token="YOUR_BOT_TOKEN"

# Set the group chat ID (negative number for groups)
firebase functions:config:set telegram.chat_id="-YOUR_GROUP_CHAT_ID"

# Deploy the functions
firebase deploy --only functions
```

### Option B: Using Environment Variables

1. In your Firebase project console, go to Functions > Configuration
2. Add the following environment variables:
   - `TELEGRAM_BOT_TOKEN`: Your bot token
   - `TELEGRAM_CHAT_ID`: Your group chat ID (negative number)

## Step 4: Test the Integration

### Using Firebase Console

1. Go to Firebase Console > Functions
2. Find the `testTelegramBot` function
3. Click "Test" and run it
4. Check your Telegram group for the test message

### Important: Bot Permissions in Group

Make sure @elsa_spy_bot has the necessary permissions in your group:

1. **Admin Rights**: The bot should be an admin or have permission to send messages
2. **Message Permissions**: Ensure the bot can post messages without restrictions
3. **Group Settings**: Check that the group allows bots to send messages

If the bot doesn't have proper permissions, you may need to:
- Make the bot an admin in the group, or
- Configure the group settings to allow bot messages

### Using Firebase CLI

```bash
# Call the test function
firebase functions:shell
# In the shell:
testTelegramBot()
```

## Step 5: Verify Alerts

1. Trigger a non-whitelisted participant interaction in WhatsApp
2. Check your Telegram group for the security alert
3. The alert should include:
   - Timestamp
   - Interaction details
   - Non-whitelisted participants
   - Sample messages
   - Action required

## Alert Message Format

The Telegram alerts will be formatted as:

```
🚨 SECURITY ALERT 🚨

📱 WhatsApp Sentinel Alert
⏰ Time: 2024-01-15T10:30:00.000Z
🆔 Interaction ID: 1705312200000
👤 User ID: user123

💬 Conversation Details:
• Type: individual
• Participant: John Doe

⚠️ Non-whitelisted participants:
• John Doe

👥 All participants:
• John Doe

📝 Sample messages:
1. Hello, how are you?
2. Can we meet tomorrow?
3. Thanks for the update

🔒 Action Required: Review this interaction and update whitelist if needed.
```

## Troubleshooting

### Bot Not Responding

1. Check if the bot token is correct
2. Verify the group chat ID is correct (should be negative)
3. Ensure the bot is not blocked in the group
4. Check if the bot has proper permissions in the group
5. Verify the bot is still a member of the group
6. Check Firebase Functions logs for errors

### Missing Alerts

1. Verify the bot configuration is deployed
2. Check if the whitelist is properly configured
3. Ensure the interaction contains non-whitelisted participants
4. Check Firebase Functions logs

### Configuration Issues

1. Verify environment variables are set correctly
2. Check Firebase Functions configuration
3. Ensure the bot has permission to send messages
4. Verify the chat ID is correct

## Security Notes

- Keep your bot token secure and never commit it to version control
- Use Firebase Functions config or environment variables for sensitive data
- Regularly rotate your bot token if possible
- Monitor bot usage and alerts

## Support

If you encounter issues:

1. Check the Firebase Functions logs
2. Verify the bot configuration
3. Test the bot manually by sending a message
4. Contact the bot administrator for @elsa_spy_bot issues
