import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';  // ← Adaugă OnDestroy
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BookService } from '../../services/book.service';
import { RentalService } from '../../services/rental.service';
import { AuthService } from '../../services/auth.service';
import { Book } from '../../models/book.model';
import { Rental } from '../../models/rental.model';
import { WebSocketService } from '../../services/websocket.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-reader-books',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reader-books.component.html',
  styleUrls: ['./reader-books.component.css']
})
export class ReaderBooksComponent implements OnInit, OnDestroy {  // ← Adaugă OnDestroy
  activeTab: 'browse' | 'rentals' = 'browse';

  // Browse Books
  books: Book[] = [];
  filteredBooks: Book[] = [];
  searchKeyword = '';
  selectedCategory = '';
  categories: string[] = [];

  // Cart
  cart: Book[] = [];
  showCart = false;
  durationWeeks = 2;

  // My Rentals
  activeRentals: Rental[] = [];
  rentalHistory: Rental[] = [];
  selectedRentalIds: number[] = [];

  // State
  isLoadingBooks = true;
  isLoadingRentals = true;
  isRenting = false;
  isReturning = false;

  // User info
  username = '';
  userId = 0;

  // WebSocket
  private wsSubscriptions: Subscription = new Subscription();

  constructor(
    private bookService: BookService,
    private rentalService: RentalService,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private webSocketService: WebSocketService  // ← Adaugă virgulă și acest service
  ) {
    this.username = this.authService.getUsername() || 'Reader';
    this.userId = Number(localStorage.getItem('userId')) || 0;
  }

  ngOnInit() {
    this.loadBooks();
    this.loadRentals();
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
          this.filterBooks();
        }
        this.cdr.detectChanges();
      })
    );

    this.wsSubscriptions.add(
      this.webSocketService.rentalUpdates$.subscribe((updatedRental) => {
        if (updatedRental.userId === this.userId) {
          this.loadRentals();
        }
        this.cdr.detectChanges();
      })
    );
  }

  // ---- Browse Books ----
  loadBooks() {
    this.isLoadingBooks = true;
    this.bookService.getAllBooks().subscribe({
      next: (data) => {
        this.books = data;
        this.filteredBooks = data;
        this.categories = [...new Set(data.map(b => b.category).filter(c => !!c))];
        this.isLoadingBooks = false;
        this.cdr.detectChanges();
      },
      error: () => { this.isLoadingBooks = false; }
    });
  }

  filterBooks() {
    this.filteredBooks = this.books.filter(book => {
      const matchesSearch = !this.searchKeyword ||
        book.title.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
        book.author.toLowerCase().includes(this.searchKeyword.toLowerCase());
      const matchesCategory = !this.selectedCategory || book.category === this.selectedCategory;
      return matchesSearch && matchesCategory;
    });
  }

  clearFilters() {
    this.searchKeyword = '';
    this.selectedCategory = '';
    this.filteredBooks = this.books;
  }

  // ---- Cart ----
  isInCart(book: Book): boolean {
    return this.cart.some(b => b.id === book.id);
  }

  toggleCart(book: Book) {
    if (this.isInCart(book)) {
      this.cart = this.cart.filter(b => b.id !== book.id);
    } else {
      this.cart.push(book);
    }
  }

  removeFromCart(book: Book) {
    this.cart = this.cart.filter(b => b.id !== book.id);
  }

  openCart() { this.showCart = true; }
  closeCart() { this.showCart = false; }

  finalizeRental() {
    if (this.cart.length === 0) return;
    if (this.durationWeeks < 1 || this.durationWeeks > 8) {
      alert('Duration must be between 1 and 8 weeks!');
      return;
    }

    this.isRenting = true;
    const bookIds = this.cart.map(b => b.id);

    this.rentalService.rentBooks(this.userId, { bookIds, durationWeeks: this.durationWeeks }).subscribe({
      next: () => {
        this.isRenting = false;
        this.cart = [];
        this.showCart = false;
        this.loadBooks();
        this.loadRentals();
        this.activeTab = 'rentals';
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isRenting = false;
        alert('Error renting books: ' + (err.error?.message || err.message));
      }
    });
  }

  // ---- My Rentals ----
  loadRentals() {
    this.isLoadingRentals = true;
    this.rentalService.getMyRentals(this.userId).subscribe({
      next: (data) => {
        this.activeRentals = data.filter(r => r.status === 'ACTIVE');
        this.rentalHistory = data.filter(r => r.status !== 'ACTIVE');
        this.isLoadingRentals = false;
        this.cdr.detectChanges();
      },
      error: () => { this.isLoadingRentals = false; }
    });
  }

  toggleRentalSelection(rentalId: number) {
    const idx = this.selectedRentalIds.indexOf(rentalId);
    if (idx >= 0) {
      this.selectedRentalIds.splice(idx, 1);
    } else {
      this.selectedRentalIds.push(rentalId);
    }
  }

  isRentalSelected(rentalId: number): boolean {
    return this.selectedRentalIds.includes(rentalId);
  }

  returnSelected() {
    if (this.selectedRentalIds.length === 0) {
      alert('Select at least one book to return!');
      return;
    }

    this.isReturning = true;
    this.rentalService.returnBooks(this.userId, this.selectedRentalIds).subscribe({
      next: () => {
        this.isReturning = false;
        this.selectedRentalIds = [];
        this.loadRentals();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isReturning = false;
        alert('Error returning books: ' + (err.error?.message || err.message));
      }
    });
  }

  // ---- Helpers ----
  getStatusLabel(status: string): string {
    const map: { [key: string]: string } = {
      'ACTIVE': 'Active',
      'PENDING_APPROVAL': 'Pending Approval',
      'APPROVED': 'Approved'
    };
    return map[status] || status;
  }

  getStatusClass(status: string): string {
    const map: { [key: string]: string } = {
      'ACTIVE': 'badge-active',
      'PENDING_APPROVAL': 'badge-pending',
      'APPROVED': 'badge-approved'
    };
    return 'rental-badge ' + (map[status] || '');
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

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
