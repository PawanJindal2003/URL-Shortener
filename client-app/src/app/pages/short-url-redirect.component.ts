import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { UrlShortenerService } from '../services/url-shortener.service';

@Component({
  selector: 'app-short-url-redirect',
  template: '<p class="redirecting">Redirecting</p>',
  styles: `
    :host {
      display: flex;
      min-height: 100vh;
      align-items: center;
      justify-content: center;
      background: #09090b;
      font-family: 'DM Sans', system-ui, sans-serif;
      color: #71717a;
      font-size: 0.95rem;
    }

    .redirecting {
      animation: pulse 1.5s ease-in-out infinite;
    }

    @keyframes pulse {
      0%, 100% { opacity: 0.4; }
      50% { opacity: 1; }
    }
  `,
})
export class ShortUrlRedirectComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly urlShortenerService = inject(UrlShortenerService);

  ngOnInit(): void {
    const shortCode = this.route.snapshot.paramMap.get('shortCode');
    if (!shortCode) {
      return;
    }

    window.location.assign(this.urlShortenerService.getBackendRedirectUrl(shortCode));
  }
}
