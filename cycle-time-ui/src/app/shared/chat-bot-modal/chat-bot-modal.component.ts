import {Component, ElementRef, ViewChild, OnInit, AfterViewChecked} from '@angular/core';
import {RetrievedDocument} from "../../services/api-chat-bot/models/retrieved-document";
import {ChatMessage} from "../../services/api-chat-bot/models/chat-message";
import {RagControllerService} from "../../services/api-chat-bot/services/rag-controller.service";
import {RagResponse} from "../../services/api-chat-bot/models/rag-response";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ChatStorageService} from "../../services/api-chat-bot/services/chat-storage.service";


@Component({
  selector: 'chat-bot-modal',
  templateUrl: './chat-bot-modal.component.html',
  styleUrls: ['./chat-bot-modal.component.css'],
  standalone:false
})
export class ChatBotModalComponent implements AfterViewChecked, OnInit {
  @ViewChild('chatContainer') chatContainer!: ElementRef;

  messages: ChatMessage[] = [];
  userInput: string = '';
  loading: boolean = false;
  showSources: boolean = false;
  currentSources: RetrievedDocument[] = [];
  systemHealth: boolean = true;
  quickQuestionsCollapsed: boolean = true; // New property for collapse state


  quickQuestions = [
    "Which steps require manual tracking?",
    "What are the cycle times for project Alpha?",
    "Which machines have the most steps?"
  ];

  constructor(
    private ragService: RagControllerService,
    public activeModal: NgbActiveModal,
    private chatStorageService: ChatStorageService
  ) {}

  ngOnInit() {
    this.checkSystemHealth();
    this.messages = this.chatStorageService.loadChatHistory();
    if (this.messages.length === 0) {
      this.addWelcomeMessage();
    }
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  checkSystemHealth() {
    this.ragService.health().subscribe({
      next: (health) => {
        this.systemHealth = true;
        console.log('RAG system healthy:', health);
      },
      error: (error) => {
        this.systemHealth = false;
        console.error('RAG system health check failed:', error);
        if (this.messages.length === 0 || !this.systemHealth) {
          this.addWelcomeMessage();
        }
      }
    });
  }

  addWelcomeMessage() {
    const welcomeMessage = this.systemHealth
      ? 'Hello! I\'m your Production AI Assistant. I can help you find information about your system. Try asking me a question or use one of the quick questions above!'
      : 'Hello! I\'m your Production AI Assistant, but I\'m currently having trouble connecting to the knowledge base. Please try again later or contact support.';

    if (this.messages.length === 0 || this.messages[0].text !== welcomeMessage) {
      this.messages.unshift({
        sender: 'bot',
        text: welcomeMessage,
        timestamp: new Date()
      });
    }
  }

  send() {
    if (!this.userInput.trim() || this.loading) {
      return;
    }

    const userMessage: ChatMessage = {
      sender: 'user',
      text: this.userInput,
      timestamp: new Date()
    };

    this.messages.push(userMessage);
    this.loading = true;

    this.chatStorageService.saveChatHistory(this.messages);

    const question = this.userInput;
    this.userInput = '';

    this.ragService.askSimple({body:{question}}).subscribe({
      next: (response: RagResponse) => {
        const botMessage: ChatMessage = {
          sender: 'bot',
          text: response.answer!,
          timestamp: new Date(),
          sources: response.sources,
          confidence: response.confidence
        };
        console.log("----confidence----: "+botMessage.confidence);


        this.messages.push(botMessage);
        this.loading = false;
        this.chatStorageService.saveChatHistory(this.messages);
      },
      error: (error) => {
        console.error('RAG Error:', error);
        const errorMessage: ChatMessage = {
          sender: 'bot',
          text: 'Sorry, I encountered an error while processing your question. Please try again or check if the system is running properly.',
          timestamp: new Date()
        };
        this.messages.push(errorMessage);
        this.loading = false;
        this.chatStorageService.saveChatHistory(this.messages);
      }
    });
  }

  startNewChat() {
    this.messages = [];
    this.chatStorageService.clearChatHistory();
    this.addWelcomeMessage();
    this.userInput = '';
    this.currentSources = [];
    this.showSources = false;
    this.quickQuestionsCollapsed = true; // Reset collapse state
  }

  askQuickQuestion(question: string) {
    this.userInput = question;
    this.send();
    // Optionally collapse quick questions after one is asked
    this.quickQuestionsCollapsed = true;
  }

  toggleSources(sources: RetrievedDocument[]) {
    this.currentSources = sources;
    this.showSources = !this.showSources;
  }

  reindexKnowledgeBase() {
    this.loading = true;
    const reindexStartMessage: ChatMessage = {
      sender: 'bot',
      text: 'Initiating knowledge base reindexing. This operation may take some time. Please do not close the application or interrupt the process.',
      timestamp: new Date()
    };
    this.messages.push(reindexStartMessage);
    this.chatStorageService.saveChatHistory(this.messages);

    this.ragService.reindex().subscribe({
      next: (response) => {
        const botMessage: ChatMessage = {
          sender: 'bot',
          text: 'Knowledge base reindexing started successfully. This may take a few minutes to complete.',
          timestamp: new Date()
        };
        this.messages.push(botMessage);
        this.loading = false;
        this.chatStorageService.saveChatHistory(this.messages);
      },
      error: (error) => {
        console.error('Reindex error:', error);
        const errorMessage: ChatMessage = {
          sender: 'bot',
          text: 'Failed to start knowledge base reindexing. Please try again later.',
          timestamp: new Date()
        };
        this.messages.push(errorMessage);
        this.loading = false;
        this.chatStorageService.saveChatHistory(this.messages);
      }
    });
  }

  getConfidenceColor(confidence: number): string {
    if (confidence >= 0.8) return 'text-success';
    if (confidence >= 0.5) return 'text-warning';
    return 'text-danger';
  }

  getConfidenceText(confidence: number): string {
    if (confidence >= 0.8) return 'High';
    if (confidence >= 0.5) return 'Medium';
    return 'Low';
  }

  // New method to toggle quick questions collapse state
  toggleQuickQuestions() {
    this.quickQuestionsCollapsed = !this.quickQuestionsCollapsed;
  }

  close() {
    this.activeModal.close();
  }

  private scrollToBottom(): void {
    try {
      this.chatContainer.nativeElement.scrollTop = this.chatContainer.nativeElement.scrollHeight;
    } catch (err) {
      console.error('Could not scroll to bottom:', err);
    }
  }

    protected readonly document = document;
  protected readonly window = window;
}
