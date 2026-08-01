import { DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ShortUrl } from '../models/short-url.model';
import { UrlShortenerService } from '../services/url-shortener.service';

@Component({
  selector: 'app-url-shortener-home',
  imports: [DatePipe, FormsModule],
  templateUrl: './url-shortener-home.component.html',
  styleUrl: './url-shortener-home.component.css',
})
export class UrlShortenerHomeComponent {
  longUrl = '';
  lookupCode = '';

  createdUrl: ShortUrl | null = null;
  lookedUpUrl: ShortUrl | null = null;

  shortenLoading = false;
  lookupLoading = false;
  errorMessage = '';
  copyMessage = '';

  constructor(private readonly urlShortenerService: UrlShortenerService) {}

  shortenUrl(): void {
    this.resetMessages();
    this.lookedUpUrl = null;

    if (!this.longUrl.trim()) {
      this.errorMessage = 'Please enter a URL.';
      return;
    }

    this.shortenLoading = true;
    this.urlShortenerService.createShortUrl(this.longUrl).subscribe({
      next: (result) => {
        this.createdUrl = result;
        this.shortenLoading = false;
      },
      error: (error) => {
        this.errorMessage = this.extractErrorMessage(error);
        this.shortenLoading = false;
      },
    });
  }

  lookupUrl(): void {
    this.resetMessages();
    this.createdUrl = null;

    const shortCode = this.urlShortenerService.parseShortCode(this.lookupCode);
    if (!shortCode) {
      this.errorMessage = 'Please enter a short code.';
      return;
    }

    this.lookupLoading = true;
    this.urlShortenerService.getMetadata(shortCode).subscribe({
      next: (result) => {
        this.lookupCode = result.shortCode;
        this.lookedUpUrl = result;
        this.lookupLoading = false;
      },
      error: (error) => {
        this.errorMessage = this.extractErrorMessage(error);
        this.lookupLoading = false;
      },
    });
  }

  deleteUrl(shortCode: string): void {
    this.resetMessages();
    this.lookupLoading = true;

    this.urlShortenerService.deleteUrl(shortCode).subscribe({
      next: () => {
        this.lookedUpUrl = null;
        this.lookupCode = '';
        this.lookupLoading = false;
      },
      error: (error) => {
        this.errorMessage = this.extractErrorMessage(error);
        this.lookupLoading = false;
      },
    });
  }

  copyShortLink(shortCode: string): void {
    const link = this.urlShortenerService.buildShortLink(shortCode);
    navigator.clipboard.writeText(link).then(() => {
      this.copyMessage = 'Copied to clipboard!';
      setTimeout(() => (this.copyMessage = ''), 2000);
    });
  }

  shortLink(shortCode: string): string {
    return this.urlShortenerService.buildShortLink(shortCode);
  }

  private resetMessages(): void {
    this.errorMessage = '';
    this.copyMessage = '';
  }

  private extractErrorMessage(error: { error?: { message?: string }; message?: string; status?: number }): string {
    if (error.error?.message) {
      return error.error.message;
    }
    if (error.status === 404) {
      return 'Short URL not found.';
    }
    if (error.status === 400) {
      return 'Invalid URL.';
    }
    return error.message ?? 'Something went wrong. Please try again.';
  }
}
