type Race {
    id: String!
    numberOfDnfs: Int!
}
  
type Query {
    races: [Race]
    race(id: String!): Race!
}