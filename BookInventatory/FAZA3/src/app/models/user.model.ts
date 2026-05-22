export interface User {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
}

export interface ReaderRequest {
  username: string;
  password: string;
  firstName: string;
  lastName: string;
  email: string;
}
