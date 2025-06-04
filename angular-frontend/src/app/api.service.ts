import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface QuestionRequest {
  question: string;
}

export interface AnswerResponse {
  answer: string;
}

@Injectable({
  providedIn: 'root' // Make service available application-wide
})
export class ApiService {
  // Adjust the API URL to where your Spring Boot backend is running
  private apiUrl = 'http://localhost:8080/api/ask'; // Backend API endpoint

  constructor(private http: HttpClient) { }

  askQuestion(question: string): Observable<AnswerResponse> {
    const requestBody: QuestionRequest = { question: question };
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
        // Other headers can be added here if needed
      })
    };

    return this.http.post<AnswerResponse>(this.apiUrl, requestBody, httpOptions)
      .pipe(
        catchError(this.handleError)
      );
  }

  private handleError(error: any) {
    console.error('API Error:', error); // Log error to console
    // You could transform the error into a user-friendly message
    return throwError(() => new Error('Something bad happened; please try again later. Details: ' + error.message));
  }
}
