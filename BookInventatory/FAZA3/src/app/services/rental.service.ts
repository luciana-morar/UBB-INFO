import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Rental, RentalRequest } from '../models/rental.model';

@Injectable({
  providedIn: 'root'
})
export class RentalService {
  private apiUrl = 'http://localhost:8081/api/rentals';

  constructor(private http: HttpClient) {}

  rentBooks(userId: number, request: RentalRequest): Observable<Rental[]> {
    return this.http.post<Rental[]>(`${this.apiUrl}/rent?userId=${userId}`, request, { withCredentials: true });
  }

  returnBooks(userId: number, rentalIds: number[]): Observable<Rental[]> {
    return this.http.post<Rental[]>(`${this.apiUrl}/return?userId=${userId}`, rentalIds, { withCredentials: true });
  }

  approveReturn(rentalId: number): Observable<Rental> {
    return this.http.post<Rental>(`${this.apiUrl}/${rentalId}/approve`, {}, { withCredentials: true });
  }

  getMyRentals(userId: number): Observable<Rental[]> {
    return this.http.get<Rental[]>(`${this.apiUrl}/my?userId=${userId}`, { withCredentials: true });
  }

  getMyActiveRentals(userId: number): Observable<Rental[]> {
    return this.http.get<Rental[]>(`${this.apiUrl}/my/active?userId=${userId}`, { withCredentials: true });
  }

  getPendingReturns(): Observable<Rental[]> {
    return this.http.get<Rental[]>(`${this.apiUrl}/pending`, { withCredentials: true });
  }

  getAllRentals(): Observable<Rental[]> {
    return this.http.get<Rental[]>(`${this.apiUrl}/all`, { withCredentials: true });
  }
}
