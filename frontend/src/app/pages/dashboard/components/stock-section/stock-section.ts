import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import type { Dashboard } from '../../dashboard';

@Component({
  selector: 'app-stock-section',
  imports: [CommonModule, FormsModule],
  templateUrl: './stock-section.html',
  styleUrl: './stock-section.scss'
})
export class StockSection {
  @Input({ required: true }) vm!: Dashboard;
}
