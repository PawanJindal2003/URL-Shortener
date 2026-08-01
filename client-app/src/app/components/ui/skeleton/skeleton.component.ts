import { Component, input } from '@angular/core';

@Component({
  selector: 'app-skeleton',
  template: `
    <div
      class="skeleton"
      [class.skeleton--text]="variant() === 'text'"
      [class.skeleton--input]="variant() === 'input'"
      [class.skeleton--button]="variant() === 'button'"
      [style.width]="width()"
      [style.height]="height()"
      aria-hidden="true"
    ></div>
  `,
})
export class SkeletonComponent {
  readonly variant = input<'text' | 'input' | 'button'>('text');
  readonly width = input<string | undefined>(undefined);
  readonly height = input<string | undefined>(undefined);
}
