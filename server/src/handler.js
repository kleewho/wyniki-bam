const {
    graphql,
    GraphQLSchema,
    GraphQLObjectType,
    GraphQLString,
    GraphQLNonNull
  
  } = require('graphql')
  
  const getRace = raceId => promisify(callback =>
    dynamoDb.get({
      TableName: process.env.DYNAMODB_TABLE,
      Key: { "raceId": raceId },
    }, callback))
    .then(result => {
      return {dnfs: result.Item.numberOfDnfs,
      raceId: result.Item.raceId}
    })
    .then(race => `Hello in ${race.raceId}, was ${race.dnfs} of DNFs.`)

  const AWS = require('aws-sdk');

  const dynamoDb = new AWS.DynamoDB.DocumentClient();

  const promisify = foo => new Promise((resolve, reject) => {
    foo((error, result) => {
      if(error) {
        reject(error)
      } else {
        resolve(result)
      }
    })
  })
  
  const schema = new GraphQLSchema({
  
    query: new GraphQLObjectType({
      name: 'RootQueryType', // an arbitrary name
      fields: {
        race: {
          args: { id: { name: 'id', type: new GraphQLNonNull(GraphQLString) } },
          type: GraphQLString,
          resolve: (parent, args) => getRace(args.id)
        }
      }
    }),
  })
  
  // We want to make a GET request with ?query=<graphql query>
  // The event properties are specific to AWS. Other providers will differ.
  module.exports.query = (event, context, callback) => graphql(schema, event.queryStringParameters.query)
    .then(
      result => callback(null, {statusCode: 200, body: JSON.stringify(result)}),
      err => callback(err)
    )
  