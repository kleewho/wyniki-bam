import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { RacesComponent } from './races/races.component';
import { RaceDetailComponent } from './race-detail/race-detail.component';
import { HttpClientModule } from "@angular/common/http";
import { ApolloModule, APOLLO_OPTIONS } from "apollo-angular";
import { HttpLinkModule, HttpLink } from "apollo-angular-link-http";
import { InMemoryCache } from "apollo-cache-inmemory";


@NgModule({
  declarations: [
    AppComponent,
    RacesComponent,
    RaceDetailComponent
  ],
  imports: [
    BrowserModule,
    //AppRoutingModule,
    //GraphQLModule,
    HttpClientModule,
    ApolloModule,
    HttpLinkModule
  ],
  providers: [{
    provide: APOLLO_OPTIONS,
    useFactory: (httpLink: HttpLink) => {
      return {
        cache: new InMemoryCache(),
        link: httpLink.create({
          uri: 'https://3c4mi5bjg9.execute-api.eu-central-1.amazonaws.com/dev/query'
        })
      };
    },
    deps: [HttpLink]
  }],
  bootstrap: [AppComponent]
})
export class AppModule { }
