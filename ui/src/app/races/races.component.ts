import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { RacesGQL, Race } from '../query/RacesGQL';

@Component({
  selector: 'app-races',
  templateUrl: './races.component.html',
  styleUrls: ['./races.component.css']
})
export class RacesComponent implements OnInit {
  selectedRace: Race;
  public races$: Observable<Race[]>;

  constructor(private racesGQL: RacesGQL) { }

  ngOnInit() {
    this.races$ = this.racesGQL.watch()
    .valueChanges
    .pipe(
      map(result => result.data.races)
    );
  }

  onSelect(race: Race): void {
    this.selectedRace = race;
  }
}
