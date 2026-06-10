import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import type { Dashboard } from '../../dashboard';

@Component({
  selector: 'app-carriers-section',
  imports: [CommonModule, FormsModule],
  templateUrl: './carriers-section.html',
  styleUrl: './carriers-section.scss'
})
export class CarriersSection {
  @Input({ required: true }) vm!: Dashboard;
}
