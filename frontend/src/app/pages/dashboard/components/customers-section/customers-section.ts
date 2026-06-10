import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import type { Dashboard } from '../../dashboard';

@Component({
  selector: 'app-customers-section',
  imports: [CommonModule, FormsModule],
  templateUrl: './customers-section.html',
  styleUrl: './customers-section.scss'
})
export class CustomersSection {
  @Input({ required: true }) vm!: Dashboard;
}
