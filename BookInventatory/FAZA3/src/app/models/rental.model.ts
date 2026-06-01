export interface Rental {
  id: number;
  userId: number;
  readerName: string;
  bookId: number;
  bookTitle: string;
  startDate: string;
  dueDate: string;
  returnedDate?: string;
  status: 'ACTIVE' | 'PENDING_APPROVAL' | 'APPROVED';
  durationWeeks: number;
}

export interface RentalRequest {
  bookIds: number[];
  durationWeeks: number;
}
