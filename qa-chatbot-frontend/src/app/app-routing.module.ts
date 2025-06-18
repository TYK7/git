import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ChatbotComponent } from './chatbot/chatbot.component'; // Import your component

const routes: Routes = [
  { path: '', redirectTo: '/chat', pathMatch: 'full' }, // Default route redirects to /chat
  { path: 'chat', component: ChatbotComponent },        // Route for the chatbot
  // You can add more routes here, e.g., { path: '**', component: PageNotFoundComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
