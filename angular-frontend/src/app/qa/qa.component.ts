import { Component, OnInit } from '@angular/core';
import { ApiService, AnswerResponse } from '../api.service'; // Adjusted path
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-qa',
  templateUrl: './qa.component.html',
  styleUrls: ['./qa.component.css']
})
export class QaComponent implements OnInit {
  questionText: string = '';
  answerText: string = '';
  errorText: string = '';
  isLoading: boolean = false;

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
  }

  submitQuestion(): void {
    if (!this.questionText.trim()) {
      this.errorText = 'Please enter a question.';
      this.answerText = '';
      return;
    }

    this.isLoading = true;
    this.answerText = '';
    this.errorText = '';

    this.apiService.askQuestion(this.questionText).subscribe(
      (response: AnswerResponse) => {
        this.answerText = response.answer;
        this.isLoading = false;
        this.questionText = ''; // Clear input field after successful submission
      },
      (error: HttpErrorResponse | Error) => {
        if (error instanceof HttpErrorResponse) {
          // Backend error (e.g. API returned 4xx or 5xx)
          this.errorText = `Error from server: ${error.status} - ${error.error?.answer || error.message}`;
          if (error.error && typeof error.error === 'string') { // if backend returns plain text error
             this.errorText = `Error from server: ${error.status} - ${error.error}`;
          }
        } else {
          // Client-side or network error
          this.errorText = `An error occurred: ${error.message}`;
        }
        this.isLoading = false;
      }
    );
  }
}
