import axios from 'axios';

/**
 * Service for sending messages to Telegram bot
 */
export class TelegramService {
  private chatId: string;
  private baseUrl: string;

  constructor(botToken: string, chatId: string) {
    this.chatId = chatId;
    this.baseUrl = `https://api.telegram.org/bot${botToken}`;
  }

  /**
   * Send a text message to the Telegram bot
   * @param message The message to send
   * @returns Promise<boolean> indicating success
   */
  async sendMessage(message: string): Promise<boolean> {
    try {
      const response = await axios.post(`${this.baseUrl}/sendMessage`, {
        chat_id: this.chatId,
        text: message,
        parse_mode: 'HTML'
      });

      if (response.status === 200) {
        console.log('Telegram message sent successfully');
        return true;
      } else {
        console.error('Failed to send Telegram message:', response.status);
        return false;
      }
    } catch (error) {
      console.error('Error sending Telegram message:', error);
      return false;
    }
  }

  /**
   * Send a formatted alert message for non-whitelisted participants
   * @param alertData The alert data to format and send
   * @returns Promise<boolean> indicating success
   */
  async sendSecurityAlert(alertData: {
    interactionId: string;
    userId: string;
    participant: string;
    nonWhitelistedParticipants: string[];
    allParticipants: string[];
    conversationType: string;
    group?: string;
    sample?: any[];
    timestamp: string;
  }): Promise<boolean> {
    const message = this.formatSecurityAlert(alertData);
    return this.sendMessage(message);
  }

  /**
   * Format security alert data into a readable Telegram message
   * @param alertData The alert data to format
   * @returns Formatted message string
   */
  private formatSecurityAlert(alertData: {
    interactionId: string;
    userId: string;
    participant: string;
    nonWhitelistedParticipants: string[];
    allParticipants: string[];
    conversationType: string;
    group?: string;
    sample?: any[];
    timestamp: string;
  }): string {
    const { 
      interactionId, 
      userId, 
      participant, 
      nonWhitelistedParticipants, 
      allParticipants, 
      conversationType, 
      group, 
      sample, 
      timestamp 
    } = alertData;

    let message = `🚨 <b>SECURITY ALERT</b> 🚨\n\n`;
    
    message += `📱 <b>WhatsApp Sentinel Alert</b>\n`;
    message += `⏰ <b>Time:</b> ${timestamp}\n`;
    message += `🆔 <b>Interaction ID:</b> ${interactionId}\n`;
    message += `👤 <b>User ID:</b> ${userId}\n\n`;
    
    message += `💬 <b>Conversation Details:</b>\n`;
    message += `• <b>Type:</b> ${conversationType}\n`;
    
    if (conversationType === 'group' && group) {
      message += `• <b>Group:</b> ${group}\n`;
    }
    
    message += `• <b>Participant:</b> ${participant}\n\n`;
    
    message += `⚠️ <b>Non-whitelisted participants:</b>\n`;
    nonWhitelistedParticipants.forEach(name => {
      message += `• ${name}\n`;
    });
    
    message += `\n👥 <b>All participants:</b>\n`;
    allParticipants.forEach(name => {
      message += `• ${name}\n`;
    });
    
    if (sample && sample.length > 0) {
      message += `\n📝 <b>Sample messages:</b>\n`;
      sample.slice(0, 3).forEach((msg, index) => {
        const truncatedMsg = typeof msg === 'string' && msg.length > 50 
          ? msg.substring(0, 50) + '...' 
          : msg;
        message += `${index + 1}. ${truncatedMsg}\n`;
      });
      
      if (sample.length > 3) {
        message += `... and ${sample.length - 3} more messages\n`;
      }
    }
    
    message += `\n🔒 <b>Action Required:</b> Review this interaction and update whitelist if needed.`;
    
    return message;
  }

  /**
   * Send a test message to verify bot connectivity
   * @returns Promise<boolean> indicating success
   */
  async sendTestMessage(): Promise<boolean> {
    const testMessage = `🤖 <b>WhatsApp Sentinel Bot Test</b>\n\n` +
      `✅ Bot is working correctly!\n` +
      `⏰ Test time: ${new Date().toISOString()}\n` +
      `🔗 Connected to WhatsApp Sentinel monitoring system.`;
    
    return this.sendMessage(testMessage);
  }
}
