import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-form-field',
  template: `
    <div class="form-field">
      <label class="form-field__label" [for]="fieldId">{{ label() }}</label>
      <input
        class="form-field__input"
        [id]="fieldId"
        [type]="type()"
        [name]="name()"
        [value]="value()"
        [disabled]="disabled()"
        [attr.placeholder]="placeholder() || null"
        [attr.aria-invalid]="error() ? true : null"
        [attr.aria-describedby]="error() ? errorId : null"
        (input)="onInput($event)"
      />
      @if (error()) {
        <span class="form-field__error" [id]="errorId" role="alert">{{ error() }}</span>
      }
    </div>
  `,
})
export class FormFieldComponent {
  private static nextId = 0;
  readonly fieldId = `field-${FormFieldComponent.nextId}`;
  readonly errorId = `field-error-${FormFieldComponent.nextId++}`;

  readonly label = input.required<string>();
  readonly name = input.required<string>();
  readonly type = input<'text' | 'url' | 'search'>('text');
  readonly value = input('');
  readonly placeholder = input<string | undefined>(undefined);
  readonly disabled = input(false);
  readonly error = input<string | undefined>(undefined);

  readonly valueChange = output<string>();

  onInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.valueChange.emit(target.value);
  }
}
