import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import type { Dashboard } from '../../dashboard';

@Component({
  selector: 'app-overview-section',
  imports: [CommonModule],
  templateUrl: './overview-section.html',
  styleUrl: './overview-section.scss'
})
export class OverviewSection {
  @Input({ required: true }) vm!: Dashboard;
}
