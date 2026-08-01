import { Component, input } from '@angular/core';
import { LucideAngularModule } from 'lucide-angular';

@Component({
  selector: 'app-empty-state',
  imports: [LucideAngularModule],
  template: `
    <div class="empty-state">
      <div class="empty-state__icon">
        <lucide-icon [name]="icon()" [size]="40" [strokeWidth]="1.5"></lucide-icon>
      </div>
      <h3 class="empty-state__title">{{ title() }}</h3>
      <p class="empty-state__description">{{ description() }}</p>
      <ng-content></ng-content>
    </div>
  `,
})
export class EmptyStateComponent {
  readonly icon = input<string>('link');
  readonly title = input.required<string>();
  readonly description = input.required<string>();
}
