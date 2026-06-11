import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Dashboard } from '../../dashboard';

@Component({
  selector: 'app-users-section',
  imports: [CommonModule, FormsModule],
  templateUrl: './users-section.html',
  styleUrl: './users-section.scss'
})
export class UsersSection {
  @Input({ required: true }) vm!: Dashboard;
}
