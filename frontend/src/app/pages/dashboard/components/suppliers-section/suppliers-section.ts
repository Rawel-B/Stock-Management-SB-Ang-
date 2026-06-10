import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import type { Dashboard } from '../../dashboard';

@Component({
  selector: 'app-suppliers-section',
  imports: [CommonModule, FormsModule],
  templateUrl: './suppliers-section.html',
  styleUrl: './suppliers-section.scss'
})
export class SuppliersSection {
  @Input({ required: true }) vm!: Dashboard;
}
