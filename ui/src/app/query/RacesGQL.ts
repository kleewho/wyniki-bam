import {Injectable} from '@angular/core';
import {Query} from 'apollo-angular';
import gql from 'graphql-tag';

export class Race {
  id: string;
  numberOfDnfs: number;
}

export interface AllRacesResponse {
  races: Race[];
}

@Injectable({
  providedIn: 'root',
})
export class RacesGQL extends Query<AllRacesResponse> {
  document = gql`
  query allRaces {
    races {
      id
      numberOfDnfs
    }
  }`;
}
