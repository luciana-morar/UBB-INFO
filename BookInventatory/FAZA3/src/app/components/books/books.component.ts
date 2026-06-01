import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BookService } from '../../services/book.service';
import { ReaderService } from '../../services/reader.service';
import { AuthService } from '../../services/auth.service';
import { RentalService } from '../../services/rental.service';
import { WebSocketService } from '../../services/websocket.service';
import { Book } from '../../models/book.model';
import { Rental } from '../../models/rental.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-books',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './books.component.html',
  styleUrls: ['./books.component.css']
})
export class BooksComponent implements OnInit, OnDestroy {
  books: Book[] = [];
  isLoading = true;
  isLibrarian = false;
  username = '';
  readersCount = 0;
  availableBooksCount = 0;
  searchKeyword = '';
  showAddBook = false;

  // === Proprietăți pentru Approve Returns ===
  activeTab: 'books' | 'approve' = 'books';
  pendingReturns: Rental[] = [];
  allRentals: Rental[] = [];
  isLoadingRentals = false;
  isApprovingId: number | null = null;

  newBook: Partial<Book> = {
    title: '',
    author: '',
    category: '',
    publisher: '',
    year: new Date().getFullYear(),
    totalCopies: 1,
    availableCopies: 1,
  };

  // WebSocket
  private wsSubscriptions: Subscription = new Subscription();

  constructor(
    private bookService: BookService,
    private readerService: ReaderService,
    private authService: AuthService,
    private rentalService: RentalService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private webSocketService: WebSocketService
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
    this.initWebSocket();
  }

  ngOnDestroy() {
    this.wsSubscriptions.unsubscribe();
    this.webSocketService.disconnect();
  }

  initWebSocket() {
    this.webSocketService.connect();

    this.wsSubscriptions.add(
      this.webSocketService.bookUpdates$.subscribe((updatedBook) => {
        const index = this.books.findIndex(b => b.id === updatedBook.id);
        if (index !== -1) {
          this.books[index] = updatedBook;
        }
        this.cdr.detectChanges();
      })
    );

    this.wsSubscriptions.add(
      this.webSocketService.rentalUpdates$.subscribe(() => {
        if (this.activeTab === 'approve') {
          this.loadPendingReturns();
        }
        this.cdr.detectChanges();
      })
    );
  }

  getCategoryClass(category: string): string {
    const map: { [key: string]: string } = {
      Fiction: 'badge badge-fiction',
      Technology: 'badge badge-technology',
      History: 'badge badge-history',
      Biography: 'badge badge-biography',
      'Self Help': 'badge badge-self-help',
    };
    return map[category] || 'badge badge-default';
  }

  loadBooks() {
    this.isLoading = true;
    this.bookService.getAllBooks().subscribe({
      next: (data) => {
        this.books = data;
        this.availableBooksCount = data.filter((b) => b.availableCopies > 0).length;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error:', error);
        this.isLoading = false;
        this.cdr.detectChanges();
      },
    });
  }

  loadReadersCount() {
    this.readerService.getAllReaders().subscribe({
      next: (data) => {
        this.readersCount = data.length;
      },
      error: (error) => {
        console.error('Error loading readers count:', error);
      },
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
      },
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
      availableCopies: 1,
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
      },
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
        },
      });
    }
  }

  clearSearch() {
    this.searchKeyword = '';
    this.loadBooks();
  }

  // === Metode pentru Approve Returns ===
  switchToApprove() {
    this.activeTab = 'approve';
    this.loadPendingReturns();
  }

  loadPendingReturns() {
    this.isLoadingRentals = true;
    this.rentalService.getPendingReturns().subscribe({
      next: (data) => {
        this.pendingReturns = data;
        this.isLoadingRentals = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isLoadingRentals = false;
      }
    });

    this.rentalService.getAllRentals().subscribe({
      next: (data) => {
        this.allRentals = data;
        this.cdr.detectChanges();
      },
      error: () => {}
    });
  }

  approveReturn(rentalId: number) {
    this.isApprovingId = rentalId;
    this.rentalService.approveReturn(rentalId).subscribe({
      next: () => {
        this.isApprovingId = null;
        this.loadPendingReturns();
        this.loadBooks();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isApprovingId = null;
        alert('Error: ' + err.message);
      }
    });
  }

  getStatusClass(status: string): string {
    const map: Record<string, string> = {
      'ACTIVE': 'rental-badge badge-active',
      'PENDING_APPROVAL': 'rental-badge badge-pending',
      'APPROVED': 'rental-badge badge-approved'
    };
    return map[status] || 'rental-badge';
  }

  getStatusLabel(status: string): string {
    const map: Record<string, string> = {
      'ACTIVE': 'Active',
      'PENDING_APPROVAL': 'Pending Approval',
      'APPROVED': 'Approved'
    };
    return map[status] || status;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  goToReaders() {
    this.router.navigate(['/admin/readers']);
  }

  goToBooks() {
    this.activeTab = 'books';
    this.loadBooks();
  }
}
