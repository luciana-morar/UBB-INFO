export interface Book {
  id: number;
  title: string;
  author: string;
  category: string;
  publisher: string;
  year: number;
  totalCopies: number;
  availableCopies: number;
  availabilityDisplay: string;
}
