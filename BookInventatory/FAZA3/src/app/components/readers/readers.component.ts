import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ReaderService } from '../../services/reader.service';
import { AuthService } from '../../services/auth.service';
import { User, ReaderRequest } from '../../models/user.model';

@Component({
  selector: 'app-readers',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './readers.component.html',
  styleUrls: ['./readers.component.css']
})
export class ReadersComponent implements OnInit {
  readers: User[] = [];
  isLoading = true;
  showAddForm = false;
  showEditForm = false;
  editingReader: User | null = null;

  // Pentru formularul de adăugare/editare
  formData: ReaderRequest = {
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    email: ''
  };

constructor(
  private readerService: ReaderService,
  private authService: AuthService,
  private router: Router,
  private cdr: ChangeDetectorRef
) {}

loadReaders() {
  this.readerService.getAllReaders().subscribe({
    next: (data) => {
      this.readers = data;
      this.isLoading = false;
      this.cdr.detectChanges();
    },
    error: (error) => {
      console.error('Error loading readers:', error);
      this.isLoading = false;
      this.cdr.detectChanges();
      if (error.status === 401) {
        this.router.navigate(['/login']);
      }
    }
  });
}

  ngOnInit() {
    // Verifică dacă utilizatorul este librarian
    if (!this.authService.isLibrarian()) {
      this.router.navigate(['/books']);
      return;
    }
    this.loadReaders();
  }


  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  showAddReaderForm() {
    this.showAddForm = true;
    this.showEditForm = false;
    this.editingReader = null;
    this.resetForm();
  }

  showEditReaderForm(reader: User) {
    this.showEditForm = true;
    this.showAddForm = false;
    this.editingReader = reader;
    this.formData = {
      username: reader.username,
      password: '', // Gol, utilizatorul poate introduce parolă nouă sau lăsa gol
      firstName: reader.firstName,
      lastName: reader.lastName,
      email: reader.email || ''
    };
  }

  cancelForm() {
    this.showAddForm = false;
    this.showEditForm = false;
    this.editingReader = null;
    this.resetForm();
  }

  resetForm() {
    this.formData = {
      username: '',
      password: '',
      firstName: '',
      lastName: '',
      email: ''
    };
  }

  addReader() {
    if (!this.formData.username || !this.formData.password ||
      !this.formData.firstName || !this.formData.lastName) {
      alert('Te rugăm să completezi toate câmpurile obligatorii!');
      return;
    }

    this.readerService.addReader(this.formData).subscribe({
      next: () => {
        this.loadReaders();
        this.cancelForm();
      },
      error: (error) => {
        console.error('Error adding reader:', error);
        alert('Eroare la adăugare: ' + (error.error?.message || error.message));
      }
    });
  }

  updateReader() {
    if (!this.editingReader) return;

    if (!this.formData.username || !this.formData.firstName || !this.formData.lastName) {
      alert('Te rugăm să completezi toate câmpurile obligatorii!');
      return;
    }

    this.readerService.updateReader(this.editingReader.id, this.formData).subscribe({
      next: () => {
        this.loadReaders();
        this.cancelForm();
      },
      error: (error) => {
        console.error('Error updating reader:', error);
        alert('Eroare la actualizare: ' + (error.error?.message || error.message));
      }
    });
  }

  deleteReader(id: number, username: string) {
    if (confirm(`Ești sigur că vrei să ștergi cititorul "${username}"?`)) {
      this.readerService.deleteReader(id).subscribe({
        next: () => {
          this.loadReaders();
        },
        error: (error) => {
          console.error('Error deleting reader:', error);
          alert('Eroare la ștergere: ' + (error.error?.message || error.message));
        }
      });
    }
  }

  goToBooks() {
    this.router.navigate(['/admin/books']);
  }
}
