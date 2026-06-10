import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import type { Dashboard } from '../../dashboard';

@Component({
  selector: 'app-deliveries-section',
  imports: [CommonModule, FormsModule],
  templateUrl: './deliveries-section.html',
  styleUrl: './deliveries-section.scss'
})
export class DeliveriesSection {
  @Input({ required: true }) vm!: Dashboard;
}
