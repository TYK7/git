import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../environments/environment'; // For API base URL

export interface AskRequestPayload {
  question: string;
}

export interface AskResponsePayload {
  answer: string;
  error?: string; // Optional error field in response
}

@Injectable({
  providedIn: 'root' // Provide this service at the root level
})
export class ChatService {
  private apiUrl = `${environment.apiBaseUrl}/chat/ask`; // From environment.ts

  constructor(private http: HttpClient) { }

  askQuestion(question: string): Observable<AskResponsePayload> {
    const payload: AskRequestPayload = { question };
    console.log('ChatService: Sending question to API:', payload);
    console.log('ChatService: API URL:', this.apiUrl);

    return this.http.post<AskResponsePayload>(this.apiUrl, payload)
      .pipe(
        map(response => {
          console.log('ChatService: Received response from API:', response);
          return response;
        }),
        catchError(this.handleError)
      );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred!';
    if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = `Error: ${error.error.message}`;
    } else {
      // Backend returned an unsuccessful response code
      if (error.status === 0) {
        errorMessage = 'Could not connect to the server. Please ensure the backend is running and accessible.';
      } else if (error.error && error.error.error) {
        errorMessage = `Server error: ${error.error.error} (Status: ${error.status})`;
      } else if (error.error && typeof error.error === 'string') { // Plain text error
        errorMessage = `Server error: ${error.error} (Status: ${error.status})`;
      }
       else {
        errorMessage = `Server returned code: ${error.status}, error message is: ${error.message}`;
      }
    }
    console.error('ChatService: API Error:', errorMessage, error);
    return throwError(() => new Error(errorMessage)); // Return an observable error
  }
}
