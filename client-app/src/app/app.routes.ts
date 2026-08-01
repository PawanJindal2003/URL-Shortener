import { Routes } from '@angular/router';

import { ShortUrlRedirectComponent } from './pages/short-url-redirect.component';
import { UrlShortenerHomeComponent } from './pages/url-shortener-home.component';

export const routes: Routes = [
  { path: '', component: UrlShortenerHomeComponent },
  { path: ':shortCode', component: ShortUrlRedirectComponent },
];
