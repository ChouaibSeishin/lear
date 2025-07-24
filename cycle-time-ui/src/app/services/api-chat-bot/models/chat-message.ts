import {RetrievedDocument} from "./retrieved-document";

export interface ChatMessage {
  sender: 'user' | 'bot';
  text: string;
  timestamp?: Date;
  sources?: RetrievedDocument[];
  confidence?: number;
}
