const {
    ApolloServer, gql
} = require('apollo-server-lambda');
const AWS = require('aws-sdk');
const dynamoDb = new AWS.DynamoDB.DocumentClient();

const promisify = foo => new Promise((resolve, reject) => {
    foo((error, result) => {
        if (error) {
            reject(error)
        } else {
            resolve(result)
        }
    })
})

const typeDefs = gql`
type Race {
  id: String!
  numberOfDnfs: Int!
}

type Season {
    races: [Race]
}

type Query {
  seasons: [Season]
  season(year: Int!): Season 
  races: [Race]
  race(id: String!): Race!
}
`;

const races = [
    {
        id: 'Rybnik:1:2019',
        numberOfDnfs: 16
    },
    {
        id: 'Rybnik:1:2018',
        numberOfDnfs: 31
    },
    {
        id: 'Ustron:3:2019',
        numberOfDnfs: 48
    }
];


const getSeason = year => ({races: races.filter(race => race.id.includes(year))})
const getSeasons = () => []
const getRace = raceId => promisify(callback =>
    dynamoDb.get({
        TableName: process.env.DYNAMODB_TABLE,
        Key: { "raceId": raceId },
    }, callback))
    .then(result => {
        return {
            numberOfDnfs: result.Item.numberOfDnfs,
            id: result.Item.raceId
        }
    })
const getRaces = () => races

const resolvers = {
    Query: {
        season: (parent, args) => getSeason(args.year),
        seasons: (parent, args) => getSeasons(),
        races: (parent, args) => getRaces(),
        race: (parent, args) => getRace(args.id)
}};


// creating the server
const server = new ApolloServer({ typeDefs, resolvers });

module.exports.handler = (event, context, callback) => {
    const handler = server.createHandler();

    // tell AWS lambda we do not want to wait for NodeJS event loop
    // to be empty in order to send the response
    context.callbackWaitsForEmptyEventLoop = false;

    // process the request
    return handler(event, context, callback);
};