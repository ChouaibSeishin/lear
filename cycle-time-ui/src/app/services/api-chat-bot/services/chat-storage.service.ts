import { Injectable } from '@angular/core';
import { ChatMessage } from '../models/chat-message';

@Injectable({
  providedIn: 'root'
})
export class ChatStorageService {
  private readonly CHAT_HISTORY_KEY = 'rag_chat_history';

  constructor() { }


  loadChatHistory(): ChatMessage[] {
    try {
      const history = localStorage.getItem(this.CHAT_HISTORY_KEY);
      if (history) {
        // Parse and ensure timestamps are Date objects
        return JSON.parse(history).map((msg: ChatMessage) => ({
          ...msg,
          timestamp: new Date(msg.timestamp!) // Ensure it's a Date object
        }));
      }
    } catch (e) {
      console.error('Error loading chat history from local storage', e);
    }
    return [];
  }


  saveChatHistory(messages: ChatMessage[]): void {
    try {
      localStorage.setItem(this.CHAT_HISTORY_KEY, JSON.stringify(messages));
    } catch (e) {
      console.error('Error saving chat history to local storage', e);
    }
  }


  clearChatHistory(): void {
    try {
      localStorage.removeItem(this.CHAT_HISTORY_KEY);
    } catch (e) {
      console.error('Error clearing chat history from local storage', e);
    }
  }
}
