export interface ShortUrl {
  id: number;
  originalUrl: string;
  shortCode: string;
  expiresAt: string | null;
  clickCount: number;
}

export class ApiError extends Error {
  constructor(
    readonly statusCode: number,
    message: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}
