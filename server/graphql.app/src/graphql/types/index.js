import { mergeTypes } from 'merge-graphql-schemas';

import race from './race.graphql'

export default mergeTypes(
    [race],
    { all: true },
);
