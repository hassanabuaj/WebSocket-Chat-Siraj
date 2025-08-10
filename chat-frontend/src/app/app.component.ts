import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router'; // <-- add this import

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
/**
 * קומפוננטת השורש של האפליקציה - מציגה את יציאת הניתוב.
 */
export class AppComponent {}
