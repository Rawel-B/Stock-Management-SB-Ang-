import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import type { Dashboard } from '../../dashboard';

@Component({
  selector: 'app-orders-section',
  imports: [CommonModule, FormsModule],
  templateUrl: './orders-section.html',
  styleUrl: './orders-section.scss'
})
export class OrdersSection {
  @Input({ required: true }) vm!: Dashboard;
}
