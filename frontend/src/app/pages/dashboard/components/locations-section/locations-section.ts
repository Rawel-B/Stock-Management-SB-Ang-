import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import type { Dashboard } from '../../dashboard';

@Component({
  selector: 'app-locations-section',
  imports: [CommonModule, FormsModule],
  templateUrl: './locations-section.html',
  styleUrl: './locations-section.scss'
})
export class LocationsSection {
  @Input({ required: true }) vm!: Dashboard;
}
