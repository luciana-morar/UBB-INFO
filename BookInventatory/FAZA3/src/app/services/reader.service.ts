import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, ReaderRequest } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class ReaderService {
  private apiUrl = 'http://localhost:8081/api/readers';

  constructor(private http: HttpClient) {}

  getAllReaders(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl, { withCredentials: true });
  }

  addReader(reader: ReaderRequest): Observable<User> {
    return this.http.post<User>(this.apiUrl, reader, { withCredentials: true });
  }

  updateReader(id: number, reader: ReaderRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, reader, { withCredentials: true });
  }

  deleteReader(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }
}
