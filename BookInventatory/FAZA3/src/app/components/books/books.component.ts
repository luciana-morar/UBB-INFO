import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BookService } from '../../services/book.service';
import { ReaderService } from '../../services/reader.service';
import { AuthService } from '../../services/auth.service';
import { Book } from '../../models/book.model';
import { Component, OnInit, ChangeDetectorRef } from '@angular/core';


@Component({
  selector: 'app-books',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './books.component.html',
  styleUrls: ['./books.component.css']
})
export class BooksComponent implements OnInit {
  books: Book[] = [];
  isLoading = true;
  isLibrarian = false;
  username = '';
  readersCount = 0;
  availableBooksCount = 0;
  searchKeyword = '';
  showAddBook = false;

  newBook: Partial<Book> = {
    title: '',
    author: '',
    category: '',
    publisher: '',
    year: new Date().getFullYear(),
    totalCopies: 1,
    availableCopies: 1
  };


constructor(
  private bookService: BookService,
  private readerService: ReaderService,
  private authService: AuthService,
  private router: Router,
  private cdr: ChangeDetectorRef
) {
  this.isLibrarian = this.authService.isLibrarian();
  this.username = this.authService.getUsername() || 'User';
}

  ngOnInit() {
    console.log('ngOnInit - loading books');
    this.loadBooks();
    if (this.isLibrarian) {
      this.loadReadersCount();
    }
  }
  getCategoryClass(category: string): string {
    const map: { [key: string]: string } = {
      'Fiction': 'badge badge-fiction',
      'Technology': 'badge badge-technology',
      'History': 'badge badge-history',
      'Biography': 'badge badge-biography',
      'Self Help': 'badge badge-self-help',
    };
    return map[category] || 'badge badge-default';
  }

  loadBooks() {
    this.isLoading = true;
    this.bookService.getAllBooks().subscribe({
      next: (data) => {
        this.books = data;
        this.availableBooksCount = data.filter(b => b.availableCopies > 0).length;
        this.isLoading = false;
        this.cdr.detectChanges(); // forțează update-ul view-ului
      },
      error: (error) => {
        console.error('Error:', error);
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  loadReadersCount() {
    this.readerService.getAllReaders().subscribe({
      next: (data) => {
        this.readersCount = data.length;
      },
      error: (error) => {
        console.error('Error loading readers count:', error);
      }
    });
  }

  searchBooks() {
    if (!this.searchKeyword.trim()) {
      this.loadBooks();
      return;
    }
    this.isLoading = true;
    this.bookService.searchBooks(this.searchKeyword).subscribe({
      next: (data) => {
        this.books = data;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error searching books:', error);
        this.isLoading = false;
      }
    });
  }

  showAddBookForm() {
    this.showAddBook = true;
    this.newBook = {
      title: '',
      author: '',
      category: '',
      publisher: '',
      year: new Date().getFullYear(),
      totalCopies: 1,
      availableCopies: 1
    };
  }

  cancelAddBook() {
    this.showAddBook = false;
  }

  addBook() {
    if (!this.newBook.title || !this.newBook.author || !this.newBook.totalCopies) {
      alert('Please fill required fields: Title, Author, and Total Copies');
      return;
    }

    this.bookService.addBook(this.newBook as Book).subscribe({
      next: () => {
        this.loadBooks();
        this.cancelAddBook();
      },
      error: (error) => {
        console.error('Error adding book:', error);
        alert('Error adding book: ' + (error.error?.message || error.message));
      }
    });
  }

  deleteBook(id: number) {
    if (confirm('Are you sure you want to delete this book?')) {
      this.bookService.deleteBook(id).subscribe({
        next: () => {
          this.loadBooks();
        },
        error: (error) => {
          console.error('Error deleting book:', error);
          alert('Error deleting book: ' + (error.error?.message || error.message));
        }
      });
    }
  }

  clearSearch() {
    this.searchKeyword = '';
    this.loadBooks();
  }
  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  goToReaders() {
    this.router.navigate(['/admin/readers']);
  }

  goToBooks() {
    this.loadBooks();
  }
}
