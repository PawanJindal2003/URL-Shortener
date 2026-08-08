import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { UrlShortenerService } from '../services/url-shortener.service';

type RedirectState = 'loading' | 'not-found' | 'expired' | 'error';

@Component({
  selector: 'app-short-url-redirect',
  imports: [RouterLink],
  template: `
    @if (state === 'loading') {
      <p class="redirecting">Redirecting…</p>
    } @else {
      <main class="page">
        <header class="hero">
          <span class="badge">sharpen.ly</span>
        </header>

        <div class="panel">
          @if (state === 'not-found') {
            <div class="message">
              <p class="status-code">404</p>
              <h1>Link not found</h1>
              <p class="description">
                This short link doesn't exist or may have been deleted.
              </p>
            </div>
          } @else if (state === 'expired') {
            <div class="message">
              <p class="status-code expired">410</p>
              <h1>Link expired</h1>
              <p class="description">
                This short link has expired and is no longer available.
              </p>
            </div>
          } @else {
            <div class="message">
              <h1>Something went wrong</h1>
              <p class="description">
                We couldn't redirect you. Please try again in a moment.
              </p>
            </div>
          }

          <a routerLink="/" class="home-link">Go to homepage</a>
        </div>
      </main>
    }
  `,
  styles: `
    :host {
      display: block;
      min-height: 100vh;
      background: #09090b;
      font-family: 'DM Sans', system-ui, sans-serif;
      color: #fafafa;
    }

    .redirecting {
      display: flex;
      min-height: 100vh;
      align-items: center;
      justify-content: center;
      margin: 0;
      color: #71717a;
      font-size: 0.95rem;
      animation: pulse 1.5s ease-in-out infinite;
    }

    .page {
      max-width: 520px;
      margin: 0 auto;
      padding: 4rem 1.25rem 3rem;
    }

    .hero {
      margin-bottom: 2.5rem;
    }

    .badge {
      display: inline-block;
      padding: 0.3rem 0.7rem;
      border-radius: 999px;
      background: rgba(163, 230, 53, 0.1);
      color: #a3e635;
      font-size: 0.75rem;
      font-weight: 600;
      letter-spacing: 0.06em;
      text-transform: uppercase;
    }

    .panel {
      background: #18181b;
      border: 1px solid #27272a;
      border-radius: 16px;
      padding: 2rem 1.5rem;
      text-align: center;
    }

    .message {
      margin-bottom: 1.75rem;
    }

    .status-code {
      margin: 0 0 0.75rem;
      font-size: 3rem;
      font-weight: 600;
      line-height: 1;
      letter-spacing: -0.04em;
      color: #a3e635;
    }

    .status-code.expired {
      color: #fbbf24;
    }

    h1 {
      margin: 0 0 0.75rem;
      font-size: 1.5rem;
      font-weight: 600;
      letter-spacing: -0.02em;
    }

    .description {
      margin: 0;
      font-size: 0.95rem;
      line-height: 1.5;
      color: #a1a1aa;
    }

    .home-link {
      display: inline-block;
      padding: 0.65rem 1.25rem;
      border-radius: 8px;
      background: #a3e635;
      color: #09090b;
      font-size: 0.875rem;
      font-weight: 600;
      text-decoration: none;
      transition: transform 0.1s;
    }

    .home-link:hover {
      transform: scale(1.02);
    }

    @keyframes pulse {
      0%, 100% { opacity: 0.4; }
      50% { opacity: 1; }
    }

    @media (max-width: 480px) {
      .page {
        padding-top: 2.5rem;
      }

      .panel {
        padding: 1.5rem 1.25rem;
      }
    }
  `,
})
export class ShortUrlRedirectComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly urlShortenerService = inject(UrlShortenerService);

  state: RedirectState = 'loading';

  ngOnInit(): void {
    void this.resolveShortCode();
  }

  private async resolveShortCode(): Promise<void> {
    const shortCode = this.route.snapshot.paramMap.get('shortCode');
    if (!shortCode) {
      this.state = 'not-found';
      return;
    }

    try {
      const metadata = await firstValueFrom(this.urlShortenerService.getMetadata(shortCode));

      if (this.isExpired(metadata.expiresAt)) {
        this.state = 'expired';
        return;
      }

      window.location.assign(this.urlShortenerService.getBackendRedirectUrl(shortCode));
    } catch (error) {
      this.state = this.resolveErrorState(error);
    }
  }

  private isExpired(expiresAt: string): boolean {
    return new Date(expiresAt).getTime() < Date.now();
  }

  private resolveErrorState(error: unknown): RedirectState {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 404) {
        return 'not-found';
      }
      if (error.status === 410) {
        return 'expired';
      }
    }
    return 'error';
  }
}
