#!/bin/bash

# Test script for Firebase Cloud Functions
# This script helps test the whitelist monitoring functionality

echo "🧪 Testing Firebase Cloud Functions for WhatsApp Sentinel"
echo "=================================================="

# Check if Firebase CLI is installed
if ! command -v firebase &> /dev/null; then
    echo "❌ Firebase CLI not found. Please install it first:"
    echo "   npm install -g firebase-tools"
    exit 1
fi

# Check if user is logged in
if ! firebase projects:list &> /dev/null; then
    echo "❌ Not logged in to Firebase. Please run:"
    echo "   firebase login"
    exit 1
fi

echo "✅ Firebase CLI is installed and user is logged in"

# Check if functions are deployed
echo ""
echo "🔍 Checking deployed functions..."
firebase functions:list

echo ""
echo "📋 Available test commands:"
echo "1. Deploy functions: firebase deploy --only functions"
echo "2. View function logs: firebase functions:log --follow"
echo "3. Test locally: firebase emulators:start --only functions,firestore"
echo "4. Check function status: firebase functions:list"

echo ""
echo "🧪 To test the whitelist monitoring:"
echo "1. Deploy the functions: firebase deploy --only functions"
echo "2. Set up a user document with whitelist in Firestore:"
echo "   Collection: whatsapp"
echo "   Document: {userId}"
echo "   Fields: { whitelist: ['John Doe', 'Jane Smith'] }"
echo "3. Create a conversation with non-whitelisted participant:"
echo "   Collection: whatsapp/{userId}/conversations"
echo "   Document: {conversationId}"
echo "   Fields: { participants: 'John, Jane, Unknown Person' }"
echo "4. Check the logs for the alert message"

echo ""
echo "📊 To view security alerts:"
echo "1. Go to Firebase Console > Firestore Database"
echo "2. Navigate to 'security_alerts' collection"
echo "3. View all non-whitelisted conversation alerts"

echo ""
echo "🔧 Function details:"
echo "- monitorConversations: Triggers on conversation writes"
echo "- checkAllConversations: Manual check via HTTP call"
echo "- dailyWhitelistCheck: Scheduled daily check (optional)"

echo ""
echo "✅ Test script completed. Follow the instructions above to test the functions."
