import { Injectable, NgZone } from '@angular/core';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Subject } from 'rxjs';
import { Book } from '../models/book.model';
import { Rental } from '../models/rental.model';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private client: Client;
  private bookSubject = new Subject<Book>();
  private rentalSubject = new Subject<Rental>();

  public bookUpdates$ = this.bookSubject.asObservable();
  public rentalUpdates$ = this.rentalSubject.asObservable();

  constructor(private ngZone: NgZone) {
    // Folosește SockJS pentru a crea conexiunea
    const socket = new SockJS('http://localhost:8081/ws');

    this.client = new Client({
      webSocketFactory: () => socket as any,
      reconnectDelay: 5000,
      debug: (str) => console.log('[STOMP]', str)
    });
  }

  connect() {
    this.client.activate();

    this.client.onConnect = () => {
      console.log('✅ WebSocket connected!');

      this.client.subscribe('/topic/books', (message) => {
        this.ngZone.run(() => {
          const book = JSON.parse(message.body);
          console.log('📚 Book update received:', book);
          this.bookSubject.next(book);
        });
      });

      this.client.subscribe('/topic/rentals', (message) => {
        this.ngZone.run(() => {
          const rental = JSON.parse(message.body);
          console.log('📝 Rental update received:', rental);
          this.rentalSubject.next(rental);
        });
      });
    };

    this.client.onStompError = (frame) => {
      console.error('STOMP error:', frame);
    };
  }

  disconnect() {
    if (this.client.active) {
      this.client.deactivate();
    }
  }
}
