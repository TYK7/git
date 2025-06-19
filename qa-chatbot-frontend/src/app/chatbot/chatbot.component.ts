import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ChatService, AskResponsePayload } from '../services/chat.service'; // Import ChatService

export interface ChatMessage {
  sender: 'user' | 'bot';
  text: string;
  timestamp: Date;
}

@Component({
  selector: 'app-chatbot',
  templateUrl: './chatbot.component.html',
  styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent implements OnInit {
  chatForm: FormGroup;
  messages: ChatMessage[] = [];
  isLoading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private chatService: ChatService // Inject ChatService
  ) {
    this.chatForm = this.fb.group({
      question: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.messages.push({
      sender: 'bot',
      text: 'Hello! Ask me anything about the document.',
      timestamp: new Date()
    });
  }

  sendMessage(): void {
    if (this.chatForm.invalid) {
      return;
    }

    const questionText = this.chatForm.value.question;
    this.messages.push({ sender: 'user', text: questionText, timestamp: new Date() });
    this.chatForm.reset();
    this.isLoading = true;

    this.chatService.askQuestion(questionText).subscribe({
      next: (response: AskResponsePayload) => {
        this.messages.push({ sender: 'bot', text: response.answer, timestamp: new Date() });
        this.isLoading = false;
      },
      error: (error) => {
        // Display error message in chat
        const errorMessage = error.message || 'Sorry, something went wrong. Please try again.';
        this.messages.push({ sender: 'bot', text: errorMessage, timestamp: new Date() });
        this.isLoading = false;
        console.error('Error fetching answer:', error);
      }
    });
  }
}
