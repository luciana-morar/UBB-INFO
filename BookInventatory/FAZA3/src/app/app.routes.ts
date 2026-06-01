import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { BooksComponent } from './components/books/books.component';
import { ReadersComponent } from './components/readers/readers.component';
import { ReaderBooksComponent } from './components/reader-books/reader-books.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'books', component: ReaderBooksComponent },
  { path: 'admin/books', component: BooksComponent },
  { path: 'admin/readers', component: ReadersComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];
