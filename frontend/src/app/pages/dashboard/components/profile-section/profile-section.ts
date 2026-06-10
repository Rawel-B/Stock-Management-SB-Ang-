import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import type { Dashboard } from '../../dashboard';

@Component({
  selector: 'app-profile-section',
  imports: [CommonModule, FormsModule],
  templateUrl: './profile-section.html',
  styleUrl: './profile-section.scss'
})
export class ProfileSection {
  @Input({ required: true }) vm!: Dashboard;
}
