import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms'; // For ngModel
import { HttpClientModule } from '@angular/common/http'; // For HttpClient

import { AppComponent } from './app.component';
import { QaComponent } from './qa/qa.component'; // Will create this
import { ApiService } from './api.service'; // Will create this

@NgModule({
  declarations: [
    AppComponent,
    QaComponent // Declare QaComponent
  ],
  imports: [
    BrowserModule,
    FormsModule, // Import FormsModule
    HttpClientModule // Import HttpClientModule
  ],
  providers: [ApiService], // Provide ApiService
  bootstrap: [AppComponent]
})
export class AppModule { }
