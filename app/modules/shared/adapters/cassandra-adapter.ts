import * as ExpressCassandra from 'express-cassandra';

export class CassandraAdapter {
    private static cassandraAdapter:any;
    private constructor(){
        const cassandra = ExpressCassandra.createClient({
            clientOptions: {
                contactPoints: ['127.0.0.1'],
                protocolOptions: { port: 9042 },
                keyspace: 'notification-scheduler',
                queryOptions: {consistency: ExpressCassandra.consistencies.one}
            },
            ormOptions: {
                defaultReplicationStrategy : {
                    class: 'SimpleStrategy',
                    replication_factor: 1
                },
                migration: 'safe',
            }
        });
        return cassandra;
    }
    public static connect(){
        // always return singleton instance of CassandraAdapter
        if(!CassandraAdapter.cassandraAdapter){
            CassandraAdapter.cassandraAdapter = new CassandraAdapter();
        }
        return CassandraAdapter.cassandraAdapter;
    }

}