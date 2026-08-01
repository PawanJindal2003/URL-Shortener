import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { UrlShortenerService } from '../services/url-shortener.service';

@Component({
  selector: 'app-short-url-redirect',
  template: '<p class="redirecting">Redirecting...</p>',
  styles: `
    :host {
      display: flex;
      min-height: 100vh;
      align-items: center;
      justify-content: center;
      font-family: Inter, system-ui, sans-serif;
      color: #4b5563;
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
