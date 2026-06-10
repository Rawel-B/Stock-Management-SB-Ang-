import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import type { Dashboard } from '../../dashboard';

@Component({
  selector: 'app-support-section',
  imports: [CommonModule, FormsModule],
  templateUrl: './support-section.html',
  styleUrl: './support-section.scss'
})
export class SupportSection {
  @Input({ required: true }) vm!: Dashboard;
}
