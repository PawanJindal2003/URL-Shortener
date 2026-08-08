import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { ShortUrl } from '../models/short-url.model';

@Injectable({ providedIn: 'root' })
export class UrlShortenerService {
  private readonly apiBase = environment.apiBaseUrl;

  constructor(private readonly http: HttpClient) {}

  createShortUrl(originalUrl: string): Observable<ShortUrl> {
    return this.http.post<ShortUrl>(`${this.apiBase}/v1/urls`, {
      url: originalUrl.trim(),
    });
  }

  getMetadata(shortCode: string): Observable<ShortUrl> {
    const code = this.parseShortCode(shortCode);
    return this.http.get<ShortUrl>(`${this.apiBase}/v1/urls/${code}/metadata`);
  }

  deleteUrl(shortCode: string): Observable<void> {
    const code = this.parseShortCode(shortCode);
    return this.http.delete<void>(`${this.apiBase}/v1/urls/${code}`);
  }

  buildShortLink(shortCode: string): string {
    return `${window.location.origin}/${this.parseShortCode(shortCode)}`;
  }

  getBackendRedirectUrl(shortCode: string): string {
    return `${this.apiBase}/v1/urls/${this.parseShortCode(shortCode)}`;
  }

  parseShortCode(input: string): string {
    const trimmed = input.trim();
    if (!trimmed) {
      return '';
    }

    if (trimmed.includes('://') || trimmed.startsWith('//')) {
      try {
        const url = new URL(trimmed.startsWith('//') ? `http:${trimmed}` : trimmed);
        const pathSegment = url.pathname.split('/').filter(Boolean).pop();
        if (pathSegment) {
          return pathSegment;
        }
      } catch {
        // Fall through to path-style parsing.
      }
    }

    const pathSegment = trimmed.replace(/^\/+/, '').split('/').filter(Boolean).pop();
    return pathSegment ?? trimmed;
  }
}
