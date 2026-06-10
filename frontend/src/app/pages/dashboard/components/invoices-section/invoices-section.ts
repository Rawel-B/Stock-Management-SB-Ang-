import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import type { Dashboard } from '../../dashboard';

@Component({
  selector: 'app-invoices-section',
  imports: [CommonModule, FormsModule],
  templateUrl: './invoices-section.html',
  styleUrl: './invoices-section.scss'
})
export class InvoicesSection {
  @Input({ required: true }) vm!: Dashboard;
}
