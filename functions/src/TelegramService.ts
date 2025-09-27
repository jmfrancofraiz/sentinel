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
      participant, 
      nonWhitelistedParticipants, 
      allParticipants, 
      conversationType, 
      group, 
      sample, 
      timestamp 
    } = alertData;

    let message = `🚨 <b>ALERTA DE SEGURIDAD</b> 🚨\n\n`;
    
    message += `⏰ <b>Hora:</b> ${timestamp}\n`;
    message += `🆔 <b>Interaction ID:</b> ${interactionId}\n`;
    message += `💬 <b>Detalles de la conversación:</b>\n`;
    
    if (conversationType === 'individual' && participant) {
      message += `• <b>Contacto:</b> ${participant}\n`;
    } else {
      if (conversationType === 'group' && group) {
        message += `• <b>Grupo:</b> ${group}\n`;
        message += `• <b>Participantes:</b> ${allParticipants.join(', ')}\n`;
      } else {
        message += `• <b>Tipo de conversación:</b> ${conversationType}\n`;
      }
      message += `⚠️ <b>Participantes no permitidos:</b>\n`;
      nonWhitelistedParticipants.forEach(name => {
        message += `• ${name}\n`;
      });
    }
    
    if (sample && sample.length > 0) {
      message += `\n📝 <b>Mensajes de muestra:</b>\n`;
      sample.forEach((msg) => {
        message += `• ${msg}\n`;
      });
    }
    
    message += `\n🔒 <b>Acción requerida:</b> Revisa esta interacción y actualiza la lista blanca si es necesario.`;
    
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
